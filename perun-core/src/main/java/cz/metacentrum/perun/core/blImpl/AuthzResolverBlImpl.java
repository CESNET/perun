package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.FacilityManagerEvents.AdminAddedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.AdminGroupAddedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.AdminGroupRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.AdminRemovedForFacility;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminAddedForGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminGroupAddedForGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminGroupRemovedFromGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminRemovedForGroup;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminGroupAddedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminGroupRemovedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminUserAddedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.AdminUserRemovedForResource;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminAddedForSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminGroupAddedForSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminGroupRemovedFromSecurityTeam;
import cz.metacentrum.perun.audit.events.SecurityTeamsManagerEvents.AdminRemovedFromSecurityTeam;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserPromotedToPerunAdmin;
import cz.metacentrum.perun.audit.events.VoManagerEvents.AdminAddedForVo;
import cz.metacentrum.perun.audit.events.VoManagerEvents.AdminGroupAddedForVo;
import cz.metacentrum.perun.audit.events.VoManagerEvents.AdminGroupRemovedForVo;
import cz.metacentrum.perun.audit.events.VoManagerEvents.AdminRemovedForVo;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AuthzResolverBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.AuthzResolverImpl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Authorization resolver. It decides if the perunPrincipal has rights to do the provided operation.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 */
public class AuthzResolverBlImpl implements AuthzResolverBl {

	private final static Logger log = LoggerFactory.getLogger(AuthzResolverBlImpl.class);
	private static AuthzResolverImplApi authzResolverImpl;
	private static PerunBl perunBl;

	private static final String UNSET_ROLE = "UNSET";
	private static final String SET_ROLE = "SET";

	private final static Set<String> extSourcesWithMultipleIdentifiers = BeansUtils.getCoreConfig().getExtSourcesMultipleIdentifiers();

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess                perunSession
	 * @param role                required role
	 * @param complementaryObject object which specifies particular action of the role (e.g. group)
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	public static boolean isAuthorized(PerunSession sess, Role role, PerunBean complementaryObject) throws InternalErrorException {
		log.trace("Entering isAuthorized: sess='" + sess + "', role='" + role + "', complementaryObject='" + complementaryObject + "'");
		Utils.notNull(sess, "sess");

		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		// If the user has no roles, deny access
		if (sess.getPerunPrincipal().getRoles() == null) {
			return false;
		}

		// Perun admin can do anything
		if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)) {
			return true;
		}

		// If user doesn't have requested role, deny request
		if (!sess.getPerunPrincipal().getRoles().hasRole(role)) {
			return false;
		}

		// Check if the principal has the privileges
		if (complementaryObject != null) {

			String beanName = BeansUtils.convertRichBeanNameToBeanName(complementaryObject.getBeanName());

			// Check various combinations of role and complementary objects
			if (role.equals(Role.VOADMIN) || role.equals(Role.VOOBSERVER)) {
				// VO admin (or VoObserver) and group, get vo id from group and check if the user is vo admin (or VoObserver)
				if (beanName.equals(Group.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Group) complementaryObject).getVoId());
				}
				// VO admin (or VoObserver) and resource, check if the user is vo admin (or VoObserver)
				if (beanName.equals(Resource.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Resource) complementaryObject).getVoId());
				}
				// VO admin (or VoObserver) and member, check if the member is from that VO
				if (beanName.equals(Member.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Member) complementaryObject).getVoId());
				}
			} else if (role.equals(Role.FACILITYADMIN)) {
				// Facility admin and resource, get facility id from resource and check if the user is facility admin
				if (beanName.equals(Resource.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Facility.class.getSimpleName(), ((Resource) complementaryObject).getFacilityId());
				}
			} else if (role.equals(Role.RESOURCEADMIN)) {
				// Resource admin, check if the user is admin of resource
				if (beanName.equals(Resource.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Resource.class.getSimpleName(), complementaryObject.getId());
				}
			} else if (role.equals(Role.SECURITYADMIN)) {
				// Security admin, check if security admin is admin of the SecurityTeam
				if (beanName.equals(SecurityTeam.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, SecurityTeam.class.getSimpleName(), complementaryObject.getId());
				}
			} else if (role.equals(Role.GROUPADMIN) || role.equals(Role.TOPGROUPCREATOR)) {
				// Group admin can see some of the date of the VO
				if (beanName.equals(Vo.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), complementaryObject.getId());
				}
			} else if (role.equals(Role.SELF)) {
				// Check if the member belongs to the self role
				if (beanName.equals(Member.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, User.class.getSimpleName(), ((Member) complementaryObject).getUserId());
				}
			}

			return sess.getPerunPrincipal().getRoles().hasRole(role, complementaryObject);
		} else {
			return true;
		}
	}

	private static Boolean doBeforeAttributeRightsCheck(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException {
		Utils.notNull(sess, "sess");
		Utils.notNull(actionType, "ActionType");
		Utils.notNull(attrDef, "AttributeDefinition");
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);

		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		// If the user has no roles, deny access
		if (sess.getPerunPrincipal().getRoles() == null) {
			return false;
		}

		// Perun admin can do anything
		if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)) {
			return true;
		}

		// Engine, Service, RPC and Perunobserver can read attributes
		if ((actionType.equals(ActionType.READ) ||
			actionType.equals(ActionType.READ_PUBLIC) ||
			actionType.equals(ActionType.READ_VO)) &&
			(sess.getPerunPrincipal().getRoles().hasRole(Role.RPC) ||
			sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNOBSERVER) ||
			sess.getPerunPrincipal().getRoles().hasRole(Role.ENGINE))) {
			return true;
		}

		return null;
	}

	/**
	 * From given attributes filter out the ones which are not allowed for the current principal.
	 *
	 * @param sess session
	 * @param bean perun bean
	 * @param attributes attributes
	 * @return list of attributes which can be accessed by current principal.
	 */
	public static List<Attribute> filterNotAllowedAttributes(PerunSession sess, PerunBean bean, List<Attribute> attributes) {
		List<Attribute> allowedAttributes = new ArrayList<>();
		for(Attribute attribute: attributes) {
			try {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attribute, bean)) {
					attribute.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, bean));
					allowedAttributes.add(attribute);
				}
			} catch (InternalErrorException e) {
				throw new RuntimeException(e);
			}
		}
		return allowedAttributes;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member, Resource resource) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, resource, member);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, resource, member);

		if (roles.containsKey(Role.VOADMIN)) {
			if (isAuthorized(sess, Role.VOADMIN, member)) return true;
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			if (isAuthorized(sess, Role.VOOBSERVER, member)) return true;
		}
		if (roles.containsKey(Role.FACILITYADMIN)) {
			if (isAuthorized(sess, Role.FACILITYADMIN, resource)) return true;
		}
		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				if (isAuthorized(sess, Role.SELF, member)) return true;
			}
			if (roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO)) {
				Vo attributeMemberVo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);
				if (sess.getPerunPrincipal().getUser() != null) {
					List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
					for (Member userMember : principalUserMembers) {
						if (userMember.getVoId() == attributeMemberVo.getId() && userMember.getStatus() == Status.VALID) {
							return true;
						}
					}
				}
			}
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			//If groupManager has right on any group assigned to resource
			List<Group> groups = getPerunBl().getGroupsManagerBl().getGroupsByPerunBean(sess, resource);
			for (Group g : groups) {
				if (isAuthorized(sess, Role.GROUPADMIN, g)) return true;
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Group group, Resource resource) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, group, resource);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, group, resource);

		if (roles.containsKey(Role.VOADMIN)) {
			if (isAuthorized(sess, Role.VOADMIN, resource)) return true;
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			if (isAuthorized(sess, Role.VOOBSERVER, resource)) return true;
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			//If groupManager has right on the group
			if (isAuthorized(sess, Role.GROUPADMIN, group)) return true;
		}
		if(roles.containsKey(Role.FACILITYADMIN)) {
			if (isAuthorized(sess, Role.FACILITYADMIN, resource)) return true;
		}
//	    if (roles.containsKey(Role.SELF)) ; //Not Allowed

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, User user, Facility facility) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, user, facility);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, user, facility);

		if (roles.containsKey(Role.FACILITYADMIN)) if (isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				if (isAuthorized(sess, Role.SELF, user)) return true;
			}
			if ((roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO)) && sess.getPerunPrincipal().getUser() != null) {
				List<Member> attributeUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
				List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());

				for (Member attributeUserMember : attributeUserMembers) {
					for (Member principalUserMember : principalUserMembers) {
						if (attributeUserMember.getVoId() == principalUserMember.getVoId() && principalUserMember.getStatus() == Status.VALID) {
							return true;
						}
					}
				}
			}
		}
		if (roles.containsKey(Role.VOADMIN)) {
			List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
			HashSet<Resource> resourcesFromUser = new HashSet<>();
			for (Member memberElement : membersFromUser) {
				resourcesFromUser.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, memberElement));
			}
			resourcesFromUser.retainAll(getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility));
			for (Resource resourceElement : resourcesFromUser) {
				if (isAuthorized(sess, Role.VOADMIN, resourceElement)) return true;
			}
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
			HashSet<Resource> resourcesFromUser = new HashSet<>();
			for (Member memberElement : membersFromUser) {
				resourcesFromUser.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, memberElement));
			}
			resourcesFromUser.retainAll(getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility));
			for (Resource resourceElement : resourcesFromUser) {
				if (isAuthorized(sess, Role.VOOBSERVER, resourceElement)) return true;
			}
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			//If groupManager has rights on "any group which is assigned to any resource from the facility" and "the user has also member in vo where exists this group"
			List<Vo> userVos = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
			Set<Integer> userVosIds = new HashSet<>();
			for (Vo voElement : userVos) {
				userVosIds.add(voElement.getId());
			}

			List<Group> groupsFromFacility = getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
			for (Group groupElement : groupsFromFacility) {
				if (isAuthorized(sess, Role.GROUPADMIN, groupElement) && userVosIds.contains(groupElement.getVoId()))
					return true;
			}
		}

		return false;
	}


	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member, Group group) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, member, group);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, member, group);

		if (roles.containsKey(Role.VOADMIN)) {
			if (isAuthorized(sess, Role.VOADMIN, member)) return true;
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			if (isAuthorized(sess, Role.VOOBSERVER, member)) return true;
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			if (isAuthorized(sess, Role.GROUPADMIN, group)) return true;
		}
		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				if (isAuthorized(sess, Role.SELF, member)) return true;
			}
			if (roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO)) {
				if (sess.getPerunPrincipal().getUser() != null) {
					List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
					for (Member principalUserMember : principalUserMembers) {
						if (member.getVoId() == principalUserMember.getVoId() && principalUserMember.getStatus() == Status.VALID) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, User user) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, user, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, user);

		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				if (isAuthorized(sess, Role.SELF, user)) return true;
			}
			if ((roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO))
				&& sess.getPerunPrincipal().getUser() != null) {
				List<Member> attributeUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
				List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());

				for (Member attributeUserMember : attributeUserMembers) {
					for (Member principalUserMember : principalUserMembers) {
						if (attributeUserMember.getVoId() == principalUserMember.getVoId() && principalUserMember.getStatus() == Status.VALID) {
							return true;
						}
					}
				}
			}
		}
		if (roles.containsKey(Role.VOADMIN)) {
			//TEMPORARY, PROBABLY WILL BE FALSE
			List<Vo> vosFromUser = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
			for (Vo v : vosFromUser) {
				if (isAuthorized(sess, Role.VOADMIN, v)) return true;
			}
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			//TEMPORARY, PROBABLY WILL BE FALSE
			List<Vo> vosFromUser = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
			for (Vo v : vosFromUser) {
				if (isAuthorized(sess, Role.VOOBSERVER, v)) return true;
			}
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			//If principal is groupManager in any vo where user has member
			List<Vo> userVos = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
			for (Vo voElement : userVos) {
				if (isAuthorized(sess, Role.GROUPADMIN, voElement)) return true;
			}
		}
//			if (roles.containsKey(Role.FACILITYADMIN)) ; //Not allowed

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, member, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, member);

		if (roles.containsKey(Role.VOADMIN)) {
			if (isAuthorized(sess, Role.VOADMIN, member)) return true;
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			if (isAuthorized(sess, Role.VOOBSERVER, member)) return true;
		}
		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				if (isAuthorized(sess, Role.SELF, member)) return true;
			}
			if ((roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO)) && sess.getPerunPrincipal().getUser() != null) {
				List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());

				for (Member principalUserMember : principalUserMembers) {
					if (member.getVoId() == principalUserMember.getVoId() && principalUserMember.getStatus() == Status.VALID) {
						return true;
					}
				}
			}
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			//if principal is groupManager in vo where the member has membership
			Vo v = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);
			if (isAuthorized(sess, Role.GROUPADMIN, v)) return true;
		}
//			if (roles.containsKey(Role.FACILITYADMIN)) ; //Not allowed

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Vo vo) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, vo, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, vo);

		if (roles.containsKey(Role.VOADMIN)) {
			if (isAuthorized(sess, Role.VOADMIN, vo)) return true;
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			if (isAuthorized(sess, Role.VOOBSERVER, vo)) return true;
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			//if Principal is GroupManager in the vo
			if (isAuthorized(sess, Role.GROUPADMIN, vo)) return true;
		}
		if (roles.containsKey(Role.FACILITYADMIN)) {
			// is facility manager of any vo resource
			List<Resource> resourceList = perunBl.getResourcesManagerBl().getResources(sess, vo);
			for (Resource res : resourceList) {
				if (isAuthorized(sess, Role.FACILITYADMIN, res)) return true;
			}
		}
		if (roles.containsKey(Role.SELF)) {
			if (actionType == ActionType.READ ||
				actionType == ActionType.READ_PUBLIC ||
				actionType == ActionType.READ_VO) {

				// any user can read
				return true;
			}
			if (roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) {
				return true;
			}
			if (roles.get(Role.SELF).contains(ActionType.WRITE) ||
				roles.get(Role.SELF).contains(ActionType.WRITE_VO)) {

				if (sess.getPerunPrincipal().getUser() != null) {
					List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
					for (Member principalUserMember : principalUserMembers) {
						if (vo.getId() == principalUserMember.getVoId() && principalUserMember.getStatus() == Status.VALID) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Group group) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, group, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, group);

		if (roles.containsKey(Role.VOADMIN)) {
			if (isAuthorized(sess, Role.VOADMIN, group)) return true;
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			if (isAuthorized(sess, Role.VOOBSERVER, group)) return true;
		}
		if (roles.containsKey(Role.GROUPADMIN)) if (isAuthorized(sess, Role.GROUPADMIN, group)) return true;
		if (roles.containsKey(Role.FACILITYADMIN)) {
			if (roles.get(Role.FACILITYADMIN).contains(actionType)) {
				List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
				for (Resource groupResource : resources) {
					if (isAuthorized(sess, Role.FACILITYADMIN, groupResource)) {
						return true;
					}
				}
			}
		}
		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO)) {
				if (sess.getPerunPrincipal().getUser() != null) {
					List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
					for (Member principalUserMember : principalUserMembers) {
						if (group.getVoId() == principalUserMember.getVoId() && principalUserMember.getStatus() == Status.VALID) {
							return true;
						}
					}
				}
			}
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				if (sess.getPerunPrincipal().getUser() != null) {
					return getPerunBl().getGroupsManagerBl().isUserMemberOfGroup(sess, sess.getPerunPrincipal().getUser(), group);
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Resource resource) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, resource, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, resource);

		if (roles.containsKey(Role.VOADMIN)) {
			if (isAuthorized(sess, Role.VOADMIN, resource)) return true;
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			if (isAuthorized(sess, Role.VOOBSERVER, resource)) return true;
		}
		if (roles.containsKey(Role.FACILITYADMIN)) {
			if (isAuthorized(sess, Role.FACILITYADMIN, resource)) return true;
		}
		if (roles.containsKey(Role.RESOURCEADMIN)) {
			if (isAuthorized(sess, Role.RESOURCEADMIN, resource)) return true;
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			List<Group> groupsFromResource = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
			for (Group g : groupsFromResource) {
				if (isAuthorized(sess, Role.GROUPADMIN, g)) return true;
			}
		}
//			if (roles.containsKey(Role.SELF)) ; //Not allowed

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Facility facility) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, facility, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, facility);

		if (roles.containsKey(Role.FACILITYADMIN)) if (isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
		if (roles.containsKey(Role.VOADMIN)) {
			List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			for (Resource r : resourcesFromFacility) {
				if (isAuthorized(sess, Role.VOADMIN, r)) return true;
			}
		}
		if (roles.containsKey(Role.VOOBSERVER)) {
			List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			for (Resource r : resourcesFromFacility) {
				if (isAuthorized(sess, Role.VOOBSERVER, r)) return true;
			}
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			List<Group> groupsFromFacility = getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
			for (Group g : groupsFromFacility) {
				if (isAuthorized(sess, Role.GROUPADMIN, g)) return true;
			}
		}
		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				List<User> usersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
				if (usersFromFacility.contains(sess.getPerunPrincipal().getUser())) {
					return true;
				}
			}
			if ((roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO)) && sess.getPerunPrincipal().getUser() != null) {
				List<Vo> attributeFacilityVos = getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility);
				List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());

				for (Vo attributeFacilityVo : attributeFacilityVos) {
					for (Member principalUserMember : principalUserMembers) {
						if (attributeFacilityVo.getId() == principalUserMember.getVoId() && principalUserMember.getStatus() == Status.VALID) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Host host) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, host, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, host);
//			if (roles.containsKey(Role.VOADMIN)) ; //Not allowed
//			if (roles.containsKey(Role.VOOBSERVER)) ; //Not allowed
//			if (roles.containsKey(Role.GROUPADMIN)) ; //Not allowed
		if (roles.containsKey(Role.FACILITYADMIN)) {
			Facility f = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			if (isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
		}
//			if (roles.containsKey(Role.SELF)) ; //Not allowed

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, UserExtSource ues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, ues, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<Role, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, ues);

		User sessUser = sess.getPerunPrincipal().getUser();
		User uesUser;
		try {
			uesUser = getPerunBl().getUsersManagerBl().getUserById(sess, ues.getUserId());
		} catch (UserNotExistsException ex) {
			return false;
		}
		if (ues.getUserId() == sessUser.getId()) return true;
		if (roles.containsKey(Role.FACILITYADMIN)) {
			List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getAssignedFacilities(sess, uesUser);
			for (Facility f : facilities) {
				if (isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
			}
		}
		if (roles.containsKey(Role.VOADMIN) || roles.containsKey(Role.VOOBSERVER)) {
			List<Vo> vos = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, uesUser);
			for (Vo v : vos) {
				if (isAuthorized(sess, Role.VOADMIN, v)) return true;
				if (isAuthorized(sess, Role.VOOBSERVER, v)) return true;
			}
		}
		if (roles.containsKey(Role.GROUPADMIN)) {
			List<Vo> vos = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, uesUser);
			for (Vo v : vos) {
				if (isAuthorized(sess, Role.GROUPADMIN, v)) return true;
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, String key) throws InternalErrorException, AttributeNotExistsException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, key, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		// only perun admin can work with entityless attributes
		return false;
	}

	/**
	 * Return map of roles, with allowed actions, which are authorized for doing "action" on "attribute".
	 *
	 * @param sess       perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef    attribute what principal want to work with
	 * @return map of roles with allowed action types
	 */
	public static Map<Role, Set<ActionType>> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);
		getPerunBl().getAttributesManagerBl().checkActionTypeExists(sess, actionType);
		return cz.metacentrum.perun.core.impl.AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);
	}

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess perunSession
	 * @param role required role
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	public static boolean isAuthorized(PerunSession sess, Role role) throws InternalErrorException {
		return isAuthorized(sess, role, null);
	}


	/**
	 * Returns true if the perunPrincipal has requested role.
	 *
	 * @param perunPrincipal acting person for whom the role is checked
	 * @param role           role to be checked
	 */
	public static boolean hasRole(PerunPrincipal perunPrincipal, Role role) {
		return perunPrincipal.getRoles().hasRole(role);
	}

	/**
	 * Set role for user and <b>all</b> complementary objects.
	 * <p>
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess                 perun session
	 * @param user                 the user for setting role
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | securityadmin | resourceselfservice | resourceAdmin )
	 * @param complementaryObjects objects for which role will be set
	 */
	public static void setRole(PerunSession sess, User user, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, AlreadyAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, SET_ROLE, null, user, role, null);
				//These exceptions should never happen
			} catch (GroupNotAdminException | UserNotAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for (PerunBean compObject : complementaryObjects) {
				try {
					manageRole(sess, SET_ROLE, null, user, role, compObject);
					//These exceptions should never happen
				} catch (GroupNotAdminException | UserNotAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * Set role for user and <b>one</b> complementary object.
	 * <p>
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess                perun session
	 * @param user                the user for setting role
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | securityadmin | resourceselfservice | resourceAdmin )
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, User user, PerunBean complementaryObject, Role role) throws InternalErrorException, AlreadyAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.setRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Set role for auhtorizedGroup and <b>all</b> complementary objects.
	 * <p>
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess                 perun session
	 * @param authorizedGroup      the group for setting role
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | resourceselfservice | resourceAdmin )
	 * @param complementaryObjects objects for which role will be set
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, AlreadyAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, SET_ROLE, authorizedGroup, null, role, null);
				//These exceptions should never happen
			} catch (GroupNotAdminException | UserNotAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for (PerunBean compObject : complementaryObjects) {
				try {
					manageRole(sess, SET_ROLE, authorizedGroup, null, role, compObject);
					//These exceptions should never happen
				} catch (GroupNotAdminException | UserNotAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * Set role for authorizedGroup and <b>one</b> complementary object.
	 * <p>
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess                perun session
	 * @param authorizedGroup     the group for setting role
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | resourceselfservice | resourceAdmin )
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws InternalErrorException, AlreadyAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.setRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Unset role for user and <b>all</b> complementary objects
	 * <p>
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess                 perun session
	 * @param user                 the user for unsetting role
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | resourceselfservice | resourceAdmin )
	 * @param complementaryObjects objects for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, User user, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, UserNotAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, UNSET_ROLE, null, user, role, null);
				//These exceptions should never happen
			} catch (GroupNotAdminException | AlreadyAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for (PerunBean compObject : complementaryObjects) {
				try {
					manageRole(sess, UNSET_ROLE, null, user, role, compObject);
					//These exceptions should never happen
				} catch (GroupNotAdminException | AlreadyAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * Unset role for user and <b>one</b> complementary object.
	 * <p>
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess                perun session
	 * @param user                the user for unsetting role
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | resourceselfservice | resourceAdmin )
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, User user, PerunBean complementaryObject, Role role) throws InternalErrorException, UserNotAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.unsetRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Unset role for group and <b>all</b> complementary objects
	 * <p>
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess                 perun session
	 * @param authorizedGroup      the group for unsetting role
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | resourceselfservice | resourceAdmin )
	 * @param complementaryObjects objects for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, GroupNotAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, UNSET_ROLE, authorizedGroup, null, role, null);
				//These exceptions should never happen
			} catch (UserNotAdminException | AlreadyAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for (PerunBean compObject : complementaryObjects) {
				try {
					manageRole(sess, UNSET_ROLE, authorizedGroup, null, role, compObject);
					//These exceptions should never happen
				} catch (UserNotAdminException | AlreadyAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * Unset role for group and <b>one</b> complementary object
	 * <p>
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess                perun session
	 * @param authorizedGroup     the group for unsetting role
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | resourceselfservice | resourceAdmin )
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws InternalErrorException, GroupNotAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Make user to be perunAdmin!
	 *
	 * @param sess
	 * @param user which will get role "PERUNADMIN" in the system
	 * @throws InternalErrorException
	 */
	public static void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, new UserPromotedToPerunAdmin(user));
		authzResolverImpl.makeUserPerunAdmin(sess, user);
	}

	/**
	 * Set or unset role for user or authorized group and complementary object
	 * <p>
	 * If user and authorizedGroup are null, throw exception. Only one can be filled at once, if both, throw exception.
	 * If complementaryObject is null, throw an exception if the role is not PerunAdmin.
	 * <p>
	 * <b>IMPORTANT:</b> refresh authz only if user in session is affected
	 *
	 * @param sess                perun session
	 * @param user                the user for set role
	 * @param authorizedGroup     the group for set role
	 * @param operation           'SET' or 'UNSET'
	 * @param role                role to set
	 * @param complementaryObject object for setting role on it
	 */
	private static void manageRole(PerunSession sess, String operation, Group authorizedGroup, User user, Role role, PerunBean complementaryObject) throws InternalErrorException, AlreadyAdminException, UserNotAdminException, GroupNotAdminException {
		if (authorizedGroup == null && user == null)
			throw new InternalErrorException("There is no object for setting role (user or authorizedGroup).");
		if (authorizedGroup != null && user != null)
			throw new InternalErrorException("There are both authorizedGroup and user for setting role, only one is acceptable.");
		if (!(role.equals(Role.PERUNADMIN) || role.equals(Role.CABINETADMIN) || role.equals(Role.PERUNOBSERVER)) && complementaryObject == null)
			throw new InternalErrorException("Complementary object can be null only for the role perunadmin, cabinetadmin and perunobserver.");

		//Check operation
		switch (operation) {
			case SET_ROLE:
				//Check role
				if (role.equals(Role.PERUNADMIN)) {
					if (user != null) makeUserPerunAdmin(sess, user);
					else throw new InternalErrorException("Not supported perunRole on authorizedGroup.");
				} else if (role.equals(Role.CABINETADMIN)) {
					if (user != null) authzResolverImpl.makeUserCabinetAdmin(sess, user);
					else throw new InternalErrorException("Not supported perunRole on authorizedGroup.");
				} else if (role.equals(Role.PERUNOBSERVER)) {
					if (user != null) authzResolverImpl.makeUserPerunObserver(sess, user);
					else authzResolverImpl.makeAuthorizedGroupPerunObserver(sess, authorizedGroup);
				} else if (role.equals(Role.VOOBSERVER)) {
					if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.addVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, user);
						else authzResolverImpl.addVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
					}
				} else if (role.equals(Role.VOADMIN)) {
					if (complementaryObject instanceof Vo) {
						if (user != null) {
							authzResolverImpl.addVoRole(sess, Role.VOADMIN,(Vo) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminAddedForVo(user, (Vo) complementaryObject));
						} else {
							authzResolverImpl.addVoRole(sess, Role.VOADMIN, (Vo) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupAddedForVo(authorizedGroup, (Vo) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for VoAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.TOPGROUPCREATOR)) {
					if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.addVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, user);
						else authzResolverImpl.addVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
					}
				} else if (role.equals(Role.GROUPADMIN)) {
					if (complementaryObject instanceof Group) {
						if (user != null) {
							authzResolverImpl.addAdmin(sess, (Group) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminAddedForGroup(user, (Group) complementaryObject));
						} else {
							authzResolverImpl.addAdmin(sess, (Group) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupAddedForGroup(authorizedGroup, (Group) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for GroupAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.FACILITYADMIN)) {
					if (complementaryObject instanceof Facility) {
						if (user != null) {
							authzResolverImpl.addAdmin(sess, (Facility) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminAddedForFacility(user, (Facility) complementaryObject));
						} else {
							authzResolverImpl.addAdmin(sess, (Facility) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupAddedForFacility(authorizedGroup, (Facility) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.RESOURCEADMIN)) {
					if (complementaryObject instanceof Resource) {
						if (user != null) {
							authzResolverImpl.addAdmin(sess, (Resource) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminUserAddedForResource(user, (Resource) complementaryObject));
						} else {
							authzResolverImpl.addAdmin(sess, (Resource) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupAddedForResource(authorizedGroup, (Resource) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for ResourceAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.SECURITYADMIN)) {
					if (complementaryObject instanceof SecurityTeam) {
						if (user != null) {
							addAdmin(sess, (SecurityTeam) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminAddedForSecurityTeam(user, (SecurityTeam) complementaryObject));
						} else {
							addAdmin(sess, (SecurityTeam) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupAddedForSecurityTeam(authorizedGroup, (SecurityTeam) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.SPONSOR)) {
					if (complementaryObject instanceof User) {
						if (user != null) authzResolverImpl.addAdmin(sess, (User) complementaryObject, user);
						else authzResolverImpl.addAdmin(sess, (User) complementaryObject, authorizedGroup);
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.addVoRole(sess, Role.SPONSOR, (Vo) complementaryObject, user);
						else authzResolverImpl.addVoRole(sess, Role.SPONSOR, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for SponsoredUser: " + complementaryObject);
					}
				} else if (role.equals(Role.RESOURCESELFSERVICE)) {
					if (complementaryObject instanceof Resource) {
						if (user != null) authzResolverImpl.addResourceRole(sess, user, role, (Resource) complementaryObject);
						else authzResolverImpl.addResourceRole(sess, authorizedGroup, role, (Resource) complementaryObject);
					}

				} else {
					throw new InternalErrorException("Not supported role: " + role);
				}
				// Check operation
				break;
			case UNSET_ROLE:
				//Check role
				if (role.equals(Role.PERUNADMIN)) {
					if (user != null) authzResolverImpl.removePerunAdmin(sess, user);
					else throw new InternalErrorException("Not supported perunRole on authorizedGroup.");
				} else if (role.equals(Role.CABINETADMIN)) {
					if (user != null) authzResolverImpl.removeCabinetAdmin(sess, user);
					else throw new InternalErrorException("Not supported perunRole on authorizedGroup.");
				} else if (role.equals(Role.PERUNOBSERVER)) {
					if (user != null) authzResolverImpl.removePerunObserver(sess, user);
					else authzResolverImpl.removePerunObserverFromAuthorizedGroup(sess, authorizedGroup);
				} else if (role.equals(Role.VOOBSERVER)) {
					if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.removeVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, user);
						else authzResolverImpl.removeVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver: " + complementaryObject);
					}
				} else if (role.equals(Role.VOADMIN)) {
					if (complementaryObject instanceof Vo) {
						if (user != null) {
							authzResolverImpl.removeVoRole(sess, Role.VOADMIN,(Vo) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminRemovedForVo(user, (Vo) complementaryObject));
						} else {
							authzResolverImpl.removeVoRole(sess, Role.VOADMIN,(Vo) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupRemovedForVo(authorizedGroup, (Vo) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for VoAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.TOPGROUPCREATOR)) {
					if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.removeVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, user);
						else authzResolverImpl.removeVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
					}
				} else if (role.equals(Role.GROUPADMIN)) {
					if (complementaryObject instanceof Group) {
						if (user != null) {
							authzResolverImpl.removeAdmin(sess, (Group) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminRemovedForGroup(user, (Group) complementaryObject));
						} else {
							authzResolverImpl.removeAdmin(sess, (Group) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupRemovedFromGroup(authorizedGroup, (Group) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for GroupAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.FACILITYADMIN)) {
					if (complementaryObject instanceof Facility) {
						if (user != null) {
							authzResolverImpl.removeAdmin(sess, (Facility) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminRemovedForFacility(user, (Facility) complementaryObject));
						} else {
							authzResolverImpl.removeAdmin(sess, (Facility) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupRemovedForFacility(authorizedGroup, (Facility) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.RESOURCEADMIN)) {
					if (complementaryObject instanceof Resource) {
						if (user != null) {
							authzResolverImpl.removeAdmin(sess, (Resource) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminUserRemovedForResource(user, (Resource) complementaryObject));
						} else {
							authzResolverImpl.removeAdmin(sess, (Resource) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupRemovedForResource(authorizedGroup, (Resource) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for ResourceAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.SECURITYADMIN)) {
					if (complementaryObject instanceof SecurityTeam) {
						if (user != null) {
							removeAdmin(sess, (SecurityTeam) complementaryObject, user);
							getPerunBl().getAuditer().log(sess, new AdminRemovedFromSecurityTeam(user, (SecurityTeam) complementaryObject));
						} else {
							removeAdmin(sess, (SecurityTeam) complementaryObject, authorizedGroup);
							getPerunBl().getAuditer().log(sess, new AdminGroupRemovedFromSecurityTeam(authorizedGroup, (SecurityTeam) complementaryObject));
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver: " + complementaryObject);
					}
				} else if (role.equals(Role.SPONSOR)) {
					if (complementaryObject instanceof User) {
						if (user != null) authzResolverImpl.removeAdmin(sess, (User) complementaryObject, user);
						else authzResolverImpl.removeAdmin(sess, (User) complementaryObject, authorizedGroup);
					} else if (complementaryObject instanceof Vo) {
						if (user != null) {
							authzResolverImpl.removeVoRole(sess, Role.SPONSOR, (Vo) complementaryObject, user);
							getPerunBl().getVosManagerBl().handleUserLostVoRole(sess, user, (Vo) complementaryObject, Role.SPONSOR);
						} else {
							authzResolverImpl.removeVoRole(sess, Role.SPONSOR, (Vo) complementaryObject, authorizedGroup);
							getPerunBl().getVosManagerBl().handleGroupLostVoRole(sess, authorizedGroup, (Vo) complementaryObject, Role.SPONSOR);
						}
					} else {
						throw new InternalErrorException("Not supported complementary object for Sponsor: " + complementaryObject);
					}
				} else if (role.equals(Role.RESOURCESELFSERVICE)) {
					if (complementaryObject instanceof Resource) {
						if (user != null) authzResolverImpl.removeResourceRole(sess, role, (Resource) complementaryObject, user);
						else authzResolverImpl.removeResourceRole(sess, role, (Resource) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for resourceSelfService: " + complementaryObject);
					}
				} else {
					throw new InternalErrorException("Not supported role: " + role);
				}
				break;
			default:
				throw new InternalErrorException("Unsupported operation. Only set and unset are correct. Operation: " + operation);
		}

		//After set or unset role without exception, refresh authz if user in session is the same like user in parameter
		if (user != null && sess.getPerunPrincipal() != null) {
			if (user.getId() == sess.getPerunPrincipal().getUserId()) {
				AuthzResolverBlImpl.refreshAuthz(sess);
			}
			//If there is authorized group instead of user, try to find intersection in members and if there is at least one, then refresh authz
		} else if (authorizedGroup != null && sess.getPerunPrincipal() != null && sess.getPerunPrincipal().getUser() != null) {
			List<Member> groupMembers = perunBl.getGroupsManagerBl().getGroupMembers(sess, authorizedGroup);
			List<Member> userMembers = perunBl.getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
			userMembers.retainAll(groupMembers);
			if (!userMembers.isEmpty()) AuthzResolverBlImpl.refreshAuthz(sess);
		}
	}

	public String toString() {
		return getClass().getSimpleName() + ":[]";
	}

	/**
	 * Returns true if the perun principal inside the perun session is vo admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is vo admin
	 */
	public static boolean isVoAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN);
	}

	/**
	 * Returns true if the perun principal inside the perun session is group admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is group admin.
	 */
	public static boolean isGroupAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.GROUPADMIN);
	}

	/**
	 * Returns true if the perun principal inside the perun session is facility admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is facility admin.
	 */
	public static boolean isFacilityAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.FACILITYADMIN);
	}

	/**
	 * Returns true if the perun principal inside the perun session is resource admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is resource admin.
	 */
	public static boolean isResourceAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.RESOURCEADMIN);
	}

	/**
	 * Returns true if the perun principal inside the perun session is security admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is security admin.
	 */
	public static boolean isSecurityAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.SECURITYADMIN);
	}

	/**
	 * Returns true if the perun principal inside the perun session is vo observer.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is vo observer
	 */
	public static boolean isVoObserver(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.VOOBSERVER);
	}

	/**
	 * Returns true if the perun principal inside the perun session is top group creator.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is top group creator.
	 */
	public static boolean isTopGroupCreator(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.TOPGROUPCREATOR);
	}

	/**
	 * Returns true if the perun principal inside the perun session is perun admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is perun admin.
	 */
	public static boolean isPerunAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN);
	}

	/**
	 * Get all principal role names. Role is defined as a name, translation table is in Role class.
	 *
	 * @param sess perun session
	 * @return list of integers, which represents role from enum Role.
	 */
	public static List<String> getPrincipalRoleNames(PerunSession sess) throws InternalErrorException {
		// We need to load the principals roles
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		return sess.getPerunPrincipal().getRoles().getRolesNames();
	}

	/**
	 * Get all User's roles. Role is defined as a name, translation table is in Role class.
	 *
	 * @param sess perun session
	 * @param user User
	 * @return list of integers, which represents role from enum Role.
	 */
	public static List<String> getUserRoleNames(PerunSession sess,User user) throws InternalErrorException {

		return authzResolverImpl.getRoles(user).getRolesNames();
	}

	/**
	 * Get all roles for a given user.
	 *
	 * @param sess perun session
	 * @param user user
	 * @return AuthzRoles object which contains all roles with perunbeans
	 * @throws InternalErrorException
	 */
	public static AuthzRoles getUserRoles(PerunSession sess, User user) throws InternalErrorException {

		return authzResolverImpl.getRoles(user);
	}

	/**
	 * Get all Group's roles. Role is defined as a name, translation table is in Role class.
	 *
	 * @param sess perun session
	 * @param group Group
	 * @return list of integers, which represents role from enum Role.
	 */
	public static List<String> getGroupRoleNames(PerunSession sess,Group group) throws InternalErrorException {

		return authzResolverImpl.getRoles(group).getRolesNames();
	}

	/**
	 * Get all roles for a given group.
	 *
	 * @param sess perun session
	 * @param group group
	 * @return AuthzRoles object which contains all roles with perunbeans
	 * @throws InternalErrorException
	 */
	public static AuthzRoles getGroupRoles(PerunSession sess, Group group) throws InternalErrorException {

		return authzResolverImpl.getRoles(group);
	}

	/**
	 * Returns user which is associated with credentials used to log-in to Perun.
	 *
	 * @param sess perun session
	 * @return currently logged user
	 */
	public static User getLoggedUser(PerunSession sess) throws InternalErrorException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}
		return sess.getPerunPrincipal().getUser();
	}

	/**
	 * Returns PerunPrincipal object associated with current session. It contains necessary information,
	 * including user identification, authorization and metadata. Each call of this method refresh the
	 * session including authorization data.
	 *
	 * @param sess perun session
	 * @return perunPrincipal object
	 * @throws InternalErrorException if the PerunSession is not valid.
	 */
	public static PerunPrincipal getPerunPrincipal(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		refreshSession(sess);

		return sess.getPerunPrincipal();
	}

	/**
	 * Returns all complementary objects for defined role.
	 *
	 * @param sess perun session
	 * @param role to get object for
	 * @return list of complementary objects
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role) throws InternalErrorException {
		return AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role, null);
	}

	/**
	 * Returns only complementary objects for defined role which fits perunBeanClass class.
	 *
	 * @param sess           perun session
	 * @param role           to get object for
	 * @param perunBeanClass particular class ( Vo | Group | ... )
	 * @return list of complementary objects
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role, Class perunBeanClass) throws InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(sess.getPerunPrincipal(), "sess.getPerunPrincipal()");

		List<PerunBean> complementaryObjects = new ArrayList<>();
		if (sess.getPerunPrincipal().getRoles().get(role) != null) {
			for (String beanName : sess.getPerunPrincipal().getRoles().get(role).keySet()) {
				// Do we filter results on particular class?
				if (perunBeanClass == null || beanName.equals(perunBeanClass.getSimpleName())) {

					if (beanName.equals(Vo.class.getSimpleName())) {
						for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
							try {
								complementaryObjects.add(perunBl.getVosManagerBl().getVoById(sess, beanId));
							} catch (VoNotExistsException ex) {
								//this is ok, vo was probably deleted but still exists in user session, only log it
								log.debug("Vo not find by id {} but still exists in user session when getComplementaryObjectsForRole method was called.", beanId);
							}
						}
					}

					if (beanName.equals(Group.class.getSimpleName())) {
						for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
							try {
								complementaryObjects.add(perunBl.getGroupsManagerBl().getGroupById(sess, beanId));
							} catch (GroupNotExistsException ex) {
								//this is ok, group was probably deleted but still exists in user session, only log it
								log.debug("Group not find by id {} but still exists in user session when getComplementaryObjectsForRole method was called.", beanId);
							}
						}
					}

					if (beanName.equals(Facility.class.getSimpleName())) {
						for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
							try {
								complementaryObjects.add(perunBl.getFacilitiesManagerBl().getFacilityById(sess, beanId));
							} catch (FacilityNotExistsException ex) {
								//this is ok, facility was probably deleted but still exists in user session, only log it
								log.debug("Facility not find by id {} but still exists in user session when getComplementaryObjectsForRole method was called.", beanId);
							}
						}
					}

					if (beanName.equals(Resource.class.getSimpleName())) {
						for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
							try {
								complementaryObjects.add(perunBl.getResourcesManagerBl().getResourceById(sess, beanId));
							} catch (ResourceNotExistsException ex) {
								//this is ok, resource was probably deleted but still exists in user session, only log it
								log.debug("Resource not find by id {} but still exists in user session when getComplementaryObjectsForRole method was called.", beanId);
							}
						}
					}

					if (beanName.equals(Service.class.getSimpleName())) {
						for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
							try {
								complementaryObjects.add(perunBl.getServicesManagerBl().getServiceById(sess, beanId));
							} catch (ServiceNotExistsException ex) {
								//this is ok, service was probably deleted but still exists in user session, only log it
								log.debug("Service not find by id {} but still exists in user session when getComplementaryObjectsForRole method was called.", beanId);
							}
						}
					}

					if (beanName.equals(SecurityTeam.class.getSimpleName())) {
						for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
							try {
								complementaryObjects.add(perunBl.getSecurityTeamsManagerBl().getSecurityTeamById(sess, beanId));
							} catch (SecurityTeamNotExistsException e) {
								//this is ok, securityTeam was probably deleted but still exists in user session, only log it
								log.debug("SecurityTeam not find by id {} but still exists in user session when getComplementaryObjectsForRole method was called.", beanId);
							}
						}
					}

				}
			}
		}

		return complementaryObjects;
	}

	/**
	 * Refresh authorization data inside session.
	 * <p>
	 * Fill in proper roles and their relative entities (vos, groups, ....).
	 * User itself or ext source data is NOT updated.
	 *
	 * @param sess perun session to refresh authz for
	 */
	public static synchronized void refreshAuthz(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);
		log.trace("Refreshing authz roles for session {}.", sess);

		//set empty set of roles
		sess.getPerunPrincipal().setRoles(new AuthzRoles());
		//Prepare service roles like engine, service, registrar, perunAdmin etc.
		prepareServiceRoles(sess);

		// if have some of the service principal, we do not need to search further
		if (sess.getPerunPrincipal().getRoles().isEmpty()) {
			User user = sess.getPerunPrincipal().getUser();
			AuthzRoles roles;
			if (user == null) {
				roles = new AuthzRoles();
			} else {
				// Load all user's roles with all possible subgroups
				roles = addAllSubgroupsToAuthzRoles(sess, authzResolverImpl.getRoles(user));
				// Add self role for the user
				roles.putAuthzRole(Role.SELF, user);
				// Add service user role
				if (user.isServiceUser()) {
					roles.putAuthzRole(Role.SERVICEUSER);
				}
			}
			sess.getPerunPrincipal().setRoles(roles);
		}

		//for OAuth clients, do not allow delegating roles not allowed by scopes
		if (sess.getPerunClient().getType() == PerunClient.Type.OAUTH) {
			List<String> oauthScopes = sess.getPerunClient().getScopes();
			log.trace("refreshAuthz({}) oauthScopes={}",sess.getLogId(),oauthScopes);
			if(!oauthScopes.contains(PerunClient.PERUN_ADMIN_SCOPE)) {
				log.debug("removing PERUNADMIN role from session of user {}",sess.getPerunPrincipal().getUserId());
				log.trace("original roles: {}", sess.getPerunPrincipal().getRoles());
				sess.getPerunPrincipal().getRoles().remove(Role.PERUNADMIN);
			}
			if(!oauthScopes.contains(PerunClient.PERUN_API_SCOPE)) {
				log.debug("removing all roles from session {}",sess);
				sess.getPerunPrincipal().getRoles().clear();
			}
		}
		log.trace("Refreshed roles: {}", sess.getPerunPrincipal().getRoles());
		sess.getPerunPrincipal().setAuthzInitialized(true);
	}

	/**
	 * Refresh all session data excluding Ext. Source and additional information.
	 * <p>
	 * This method update user in session (try to find user by ext. source data).
	 * Then it updates authorization data in session.
	 *
	 * @param sess Perun session to refresh data for
	 */
	public static synchronized void refreshSession(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);
		log.trace("Refreshing session data for session {}.", sess);

		PerunPrincipal principal = sess.getPerunPrincipal();

		try {
			User user;
				if(extSourcesWithMultipleIdentifiers.contains(principal.getExtSourceName())) {
					UserExtSource ues = perunBl.getUsersManagerBl().getUserExtSourceFromMultipleIdentifiers(sess, principal);
					user = perunBl.getUsersManagerBl().getUserByUserExtSource(sess, ues);
				} else {
					user = perunBl.getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, principal.getExtSourceName(), principal.getActor());
				}
			sess.getPerunPrincipal().setUser(user);
		} catch (Exception ex) {
			// we don't care that user was not found - clear it from session
			sess.getPerunPrincipal().setUser(null);
		}

		AuthzResolverBlImpl.refreshAuthz(sess);

	}

	/**
	 * For role GroupAdmin with association to "Group" add also all subgroups to authzRoles.
	 * If authzRoles is null, return empty AuthzRoles.
	 * If there is no GroupAdmin role or Group object for this role, return not changed authzRoles.
	 *
	 * @param sess       perun session
	 * @param authzRoles authzRoles for some user
	 * @return authzRoles also with subgroups of groups
	 */
	public static AuthzRoles addAllSubgroupsToAuthzRoles(PerunSession sess, AuthzRoles authzRoles) throws InternalErrorException {
		if (authzRoles == null) return new AuthzRoles();
		if (authzRoles.hasRole(Role.GROUPADMIN)) {
			Map<String, Set<Integer>> groupAdminRoles = authzRoles.get(Role.GROUPADMIN);
			Set<Integer> groupsIds = groupAdminRoles.get("Group");
			Set<Integer> newGroupsIds = new HashSet<>(groupsIds);
			for (Integer id : groupsIds) {
				Group parentGroup;
				try {
					parentGroup = getPerunBl().getGroupsManagerBl().getGroupById(sess, id);
				} catch (GroupNotExistsException ex) {
					log.debug("Group with id=" + id + " not exists when initializing rights for user: " + sess.getPerunPrincipal().getUser());
					continue;
				}
				List<Group> subGroups = getPerunBl().getGroupsManagerBl().getAllSubGroups(sess, parentGroup);
				for (Group g : subGroups) {
					newGroupsIds.add(g.getId());
				}
			}
			groupAdminRoles.put("Group", newGroupsIds);
			authzRoles.put(Role.GROUPADMIN, groupAdminRoles);
		}
		return authzRoles;
	}

	public static void removeAllAuthzForVo(PerunSession sess, Vo vo) throws InternalErrorException {
		authzResolverImpl.removeAllAuthzForVo(sess, vo);
	}

	static List<Vo> getVosForGroupInRole(PerunSession sess, Group group, Role role) throws InternalErrorException {
		List<Vo> vos = new ArrayList<>();
		for (Integer voId : authzResolverImpl.getVoIdsForGroupInRole(sess, group, role)) {
			try {
				vos.add(getPerunBl().getVosManagerBl().getVoById(sess, voId));
			} catch (VoNotExistsException e) {
				log.error("vo " + voId + " not found", e);
			}
		}
		return vos;
	}

	static void removeAllUserAuthz(PerunSession sess, User user) throws InternalErrorException {
		//notify vosManager that the deleted user had SPONSOR role for some VOs
		List<Integer> sponsoredVoIds = authzResolverImpl.getVoIdsForUserInRole(sess, user, Role.SPONSOR);
		for (Integer voId : sponsoredVoIds) {
			VosManagerBl vosManagerBl = getPerunBl().getVosManagerBl();
			try {
				vosManagerBl.handleUserLostVoRole(sess, user, vosManagerBl.getVoById(sess, voId),Role.SPONSOR);
			} catch (VoNotExistsException e) {
				log.error("Vo {} has user {} in role SPONSOR, but does not exist",voId,user.getId());
			}
		}
		//remove all roles from the user
		authzResolverImpl.removeAllUserAuthz(sess, user);
	}

	static void removeAllSponsoredUserAuthz(PerunSession sess, User sponsoredUser) throws InternalErrorException {
		authzResolverImpl.removeAllSponsoredUserAuthz(sess, sponsoredUser);
	}

	public static void removeAllAuthzForGroup(PerunSession sess, Group group) throws InternalErrorException {
		//notify vosManager that the deleted group had SPONSOR role for some VOs
		for (Vo vo : getVosForGroupInRole(sess, group, Role.SPONSOR)) {
			getPerunBl().getVosManagerBl().handleGroupLostVoRole(sess, group, vo ,Role.SPONSOR);
		}
		//remove all roles from the group
		authzResolverImpl.removeAllAuthzForGroup(sess, group);
	}

	public static void removeAllAuthzForFacility(PerunSession sess, Facility facility) throws InternalErrorException {
		authzResolverImpl.removeAllAuthzForFacility(sess, facility);
	}

	public static void removeAllAuthzForResource(PerunSession sess, Resource resource) throws InternalErrorException {
		authzResolverImpl.removeAllAuthzForResource(sess, resource);
	}

	public static void removeAllAuthzForService(PerunSession sess, Service service) throws InternalErrorException {
		authzResolverImpl.removeAllAuthzForService(sess, service);
	}

	public static void removeAllAuthzForSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		authzResolverImpl.removeAllAuthzForSecurityTeam(sess, securityTeam);
	}

	public static void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, securityTeam, user);
	}

	public static void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, securityTeam, group);
	}

	public static void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, UserNotAdminException {
		authzResolverImpl.removeAdmin(sess, securityTeam, user);
	}

	public static void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, GroupNotAdminException {
		authzResolverImpl.removeAdmin(sess, securityTeam, group);
	}

	/**
	 * Checks whether the user is in role for Vo.
	 *
	 * @param session perun session
	 * @param user user
	 * @param role role of user
	 * @param vo virtual organization
	 * @return true if user is in role for VO, false otherwise
	 */
	static boolean isUserInRoleForVo(PerunSession session, User user, Role role, Vo vo) {
		return authzResolverImpl.isUserInRoleForVo(session, user, role, vo);
	}

	/**
	 * Checks whether the group is in role for Vo.
	 *
	 * @param session perun session
	 * @param group group
	 * @param role role of group
	 * @param vo virtual organization
	 * @return true if group is in role for VO, false otherwise
	 */
	static boolean isGroupInRoleForVo(PerunSession session, Group group, Role role, Vo vo) {
		return authzResolverImpl.isGroupInRoleForVo(session, group, role, vo);
	}

	// Filled by Spring
	public static AuthzResolverImplApi setAuthzResolverImpl(AuthzResolverImplApi authzResolverImpl) {
		AuthzResolverBlImpl.authzResolverImpl = authzResolverImpl;
		return authzResolverImpl;
	}

	//Filled by Spring
	public static PerunBl setPerunBl(PerunBl perunBl) {
		AuthzResolverBlImpl.perunBl = perunBl;
		return perunBl;
	}

	private static PerunBl getPerunBl() {
		return perunBl;
	}

	/**
	 * Prepare service roles to session AuthzRoles (perunadmin, service, rpc, engine etc.)
	 *
	 * @param sess use session to add roles
	 */
	private static void prepareServiceRoles(PerunSession sess) {
		// Load list of perunAdmins from the configuration, split the list by the comma
		List<String> perunAdmins = BeansUtils.getCoreConfig().getAdmins();

		// Check if the PerunPrincipal is in a group of Perun Admins
		if (perunAdmins.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);
			sess.getPerunPrincipal().setAuthzInitialized(true);
			// We can quit, because perun admin has all privileges
			log.trace("AuthzResolver.init: Perun Admin {} loaded", sess.getPerunPrincipal().getActor());
			return;
		}

		String perunRpcAdmin = BeansUtils.getCoreConfig().getRpcPrincipal();
		if (sess.getPerunPrincipal().getActor().equals(perunRpcAdmin)) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.RPC);
			log.trace("AuthzResolver.init: Perun RPC {} loaded", perunRpcAdmin);
		}

		List<String> perunEngineAdmins = BeansUtils.getCoreConfig().getEnginePrincipals();
		if (perunEngineAdmins.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.ENGINE);
			log.trace("AuthzResolver.init: Perun Engine {} loaded", perunEngineAdmins);
		}

		List<String> perunNotifications = BeansUtils.getCoreConfig().getNotificationPrincipals();
		if (perunNotifications.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.NOTIFICATIONS);

			log.trace("AuthzResolver.init: Perun Notifications {} loaded", perunNotifications);
		}

		List<String> perunRegistrars = BeansUtils.getCoreConfig().getRegistrarPrincipals();
		if (perunRegistrars.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.REGISTRAR);

			//FIXME ted pridame i roli plneho admina
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);

			log.trace("AuthzResolver.init: Perun Registrar {} loaded", perunRegistrars);
		}
	}
}
