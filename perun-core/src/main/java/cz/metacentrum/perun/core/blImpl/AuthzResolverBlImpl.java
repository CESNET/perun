package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleSetForGroup;
import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleSetForUser;
import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleUnsetForGroup;
import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleUnsetForUser;
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
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleManagementRules;
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
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleAlreadySetException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSetException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AuthzResolverBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.AuthzResolverImpl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authorization resolver. It decides if the perunPrincipal has rights to do the provided operation.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 */
public class AuthzResolverBlImpl implements AuthzResolverBl {

	private final static Logger log = LoggerFactory.getLogger(AuthzResolverBlImpl.class);
	private static AuthzResolverImplApi authzResolverImpl;
	private static PerunBl perunBl;
	private final static Set<String> extSourcesWithMultipleIdentifiers = BeansUtils.getCoreConfig().getExtSourcesMultipleIdentifiers();

	/**
	 * Prepare necessary structures and resolve access rights for the session's principal.
	 *
	 * @param sess perunSession which contains the principal.
	 * @param policyDefinition is a definition of a policy which will define authorization rules.
	 * @param objects as list of PerunBeans on which will be authorization provided. (e.g. groups, Vos, etc...)
	 * @return true if the principal has particular rights, false otherwise.
	 * @throws PolicyNotExistsException when the given policyDefinition does not exist in the PerunPoliciesContainer.
	 */
	public static boolean authorized(PerunSession sess, String policyDefinition, List<PerunBean> objects) throws PolicyNotExistsException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		// If the user has no roles, deny access
		if (sess.getPerunPrincipal().getRoles() == null) {
			return false;
		}

		List<PerunPolicy> allPolicies = AuthzResolverImpl.fetchPolicyWithAllIncludedPolicies(policyDefinition);

		List<Map<String, String>> policyRoles = new ArrayList<>();
		for (PerunPolicy policy : allPolicies) policyRoles.addAll(policy.getPerunRoles());

		//Fetch super objects like Vo for group etc.
		Map <String, Set<Integer>> mapOfBeans = fetchAllRelatedObjects(objects);

		return resolveAuthorization(sess, policyRoles, mapOfBeans);
	}

	/**
	 * Check wheter the principal is authorized to manage the role on the object.
	 *
	 * @param sess principal's perun session
	 * @param object bounded with the role
	 * @param roleName which will be managed
	 * @return
	 * @throws RoleManagementRulesNotExistsException when the role does not have the management rules.
	 */
	public static boolean authorizedToManageRole(PerunSession sess, PerunBean object, String roleName) throws RoleManagementRulesNotExistsException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		// If the user has no roles, deny access
		if (sess.getPerunPrincipal().getRoles() == null) {
			return false;
		}

		RoleManagementRules rules = AuthzResolverImpl.getRoleManagementRules(roleName);

		Map <String, Set<Integer>> mapOfBeans = new HashMap<>();
		if (object != null) {
			//Fetch super objects like Vo for group etc.
			mapOfBeans = fetchAllRelatedObjects(Collections.singletonList(object));
		}

		return resolveAuthorization(sess, rules.getPrivilegedRoles(), mapOfBeans);
	}

	public static boolean selfAuthorizedForApplication(PerunSession sess, Application app) {
		//fetch necessary information
		LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(app.getFedInfo());
		String appShibIdentityProvider = additionalAttributes.get(UsersManagerBl.ORIGIN_IDENTITY_PROVIDER_KEY);
		String principalShibIdentityProvider = sess.getPerunPrincipal().getAdditionalInformations().get(UsersManagerBl.ORIGIN_IDENTITY_PROVIDER_KEY);

		//Authorization based on the user
		if (app.getUser() != null && sess.getPerunPrincipal().getUserId() == app.getUser().getId()) {
			return true;
		//Authorization based on the extSourceName and extSourceLogin
		} else if (Objects.equals(app.getCreatedBy(), sess.getPerunPrincipal().getActor()) &&
			Objects.equals(app.getExtSourceName(), sess.getPerunPrincipal().getExtSourceName()) &&
			Objects.equals(app.getExtSourceType(), sess.getPerunPrincipal().getExtSourceType())) {
			return true;
		//Authorization based on additional identifiers
		} else if (principalShibIdentityProvider != null &&
			principalShibIdentityProvider.equals(appShibIdentityProvider) &&
			extSourcesWithMultipleIdentifiers.contains(principalShibIdentityProvider) &&
			Objects.equals(app.getExtSourceName(), sess.getPerunPrincipal().getExtSourceName()) &&
			Objects.equals(app.getExtSourceType(), sess.getPerunPrincipal().getExtSourceType())) {

			//fetch necessary information
			String principalAdditionalIdentifiers = sess.getPerunPrincipal().getAdditionalInformations().get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
			String appAdditionalIdentifiers = additionalAttributes.get(UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME);
			if (principalAdditionalIdentifiers == null) {
				throw new InternalErrorException("Entry " + UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME + " is not defined in the principal's additional information. Either it was not provided by external source used for sign-in or the mapping configuration is wrong.");
			}
			if (appAdditionalIdentifiers == null) {
				throw new InternalErrorException("Entry " + UsersManagerBl.ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME + " is not defined in the application's federation information. Either it was not provided by external source used for sign-in or the mapping configuration is wrong.");
			}

			//find match between principal and application identifiers
			List<String> appAdditionalIdentifiersList = Arrays.asList(appAdditionalIdentifiers.split(UsersManagerBl.MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX));
			for (String identifier : principalAdditionalIdentifiers.split(UsersManagerBl.MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX)) {
				if (appAdditionalIdentifiersList.contains(identifier))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess                perunSession
	 * @param role                required role
	 * @param complementaryObject object which specifies particular action of the role (e.g. group)
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	@Deprecated
	public static boolean isAuthorized(PerunSession sess, String role, PerunBean complementaryObject) {
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

	private static Boolean doBeforeAttributeRightsCheck(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) {
		Utils.notNull(sess, "sess");
		Utils.notNull(actionType, "ActionType");
		Utils.notNull(attrDef, "AttributeDefinition");

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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member, Resource resource) throws WrongAttributeAssignmentException {

		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, resource, member);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
						if (userMember.getVoId() == attributeMemberVo.getId() && Objects.equals(userMember.getStatus(), Status.VALID)) {
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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Group group, Resource resource) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, group, resource);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, User user, Facility facility) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, user, facility);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
						if (attributeUserMember.getVoId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
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


	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member, Group group) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, member, group);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
						if (member.getVoId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, User user) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, user, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
						if (attributeUserMember.getVoId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, member, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
					if (member.getVoId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Vo vo) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, vo, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
						if (vo.getId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Group group) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, group, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
						if (group.getVoId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Resource resource) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, resource, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
		if (roles.containsKey(Role.SELF)) {
			if (roles.get(Role.SELF).contains(ActionType.READ_PUBLIC) || roles.get(Role.SELF).contains(ActionType.WRITE_PUBLIC)) return true;
			if (roles.get(Role.SELF).contains(ActionType.READ_VO) || roles.get(Role.SELF).contains(ActionType.WRITE_VO)) {
				if (sess.getPerunPrincipal().getUser() != null) {
					List<Member> principalUserMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
					for (Member principalUserMember : principalUserMembers) {
						if (resource.getVoId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
							return true;
						}
					}
				}
			}
			if (roles.get(Role.SELF).contains(ActionType.READ) || roles.get(Role.SELF).contains(ActionType.WRITE)) {
				if (sess.getPerunPrincipal().getUser() != null) {
					return getPerunBl().getResourcesManagerBl().isUserAssigned(sess, sess.getPerunPrincipal().getUser(), resource);
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Facility facility) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, facility, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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
						if (attributeFacilityVo.getId() == principalUserMember.getVoId() && Objects.equals(principalUserMember.getStatus(), Status.VALID)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Host host) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, host, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, UserExtSource ues) throws WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, ues, null);

		Boolean isAuthorized = doBeforeAttributeRightsCheck(sess, actionType, attrDef);

		if (isAuthorized != null) {
			return isAuthorized;
		}

		//This method get all possible roles which can do action on attribute
		Map<String, Set<ActionType>> roles = AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, String key) {
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
	public static Map<String, Set<ActionType>> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws AttributeNotExistsException, ActionTypeNotExistsException {
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);
		getPerunBl().getAttributesManagerBl().checkActionTypeExists(sess, actionType);
		return AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);
	}

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess perunSession
	 * @param role required role
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	@Deprecated
	public static boolean isAuthorized(PerunSession sess, String role) {
		return isAuthorized(sess, role, null);
	}


	/**
	 * Returns true if the perunPrincipal has requested role.
	 *
	 * @param perunPrincipal acting person for whom the role is checked
	 * @param role           role to be checked
	 */
	public static boolean hasRole(PerunPrincipal perunPrincipal, String role) {
		return perunPrincipal.getRoles().hasRole(role);
	}


	/**
	 * Set role for user and <b>one</b> complementary object.
	 * <p>
	 * If complementary object is wrong for the role, throw an exception.
	 *
	 * @param sess                perun session
	 * @param user                the user for setting role
	 * @param role                role of user in a session ( PERUNADMIN | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | SECURITYADMIN | RESOURCESELFSERVICE | RESOURCEADMIN )
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, User user, PerunBean complementaryObject, String role) throws AlreadyAdminException, RoleCannotBeManagedException {
		if (!objectAndRoleManageableByEntity(user, complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, user);
		}

		Map<String, Integer> mappingOfValues = createMappingOfValues(user, complementaryObject, role);

		try {
			authzResolverImpl.setRole(sess, mappingOfValues, role);
		} catch (RoleAlreadySetException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already "+role+" in " + complementaryObject, e);
		}

		getPerunBl().getAuditer().log(sess, new RoleSetForUser(complementaryObject, user, role));

		if (user != null && sess.getPerunPrincipal() != null) {
			if (user.getId() == sess.getPerunPrincipal().getUserId()) {
				AuthzResolverBlImpl.refreshAuthz(sess);
			}
		}
	}

	/**
	 * Set role for authorizedGroup and <b>one</b> complementary object.
	 * <p>
	 * If complementary object is wrong for the role, throw an exception.
	 *
	 * @param sess                perun session
	 * @param authorizedGroup     the group for setting role
	 * @param role                role of user in a session ( PERUNADMIN | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | RESOURCESELFSERVICE | RESOURCEADMIN )
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, String role) throws AlreadyAdminException, RoleCannotBeManagedException {
		if (!objectAndRoleManageableByEntity(authorizedGroup, complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, authorizedGroup);
		}

		Map<String, Integer> mappingOfValues = createMappingOfValues(authorizedGroup, complementaryObject, role);

		try {
			authzResolverImpl.setRole(sess, mappingOfValues, role);
		} catch (RoleAlreadySetException e) {
			throw new AlreadyAdminException("Group id=" + authorizedGroup.getId() + " is already "+role+" in " + complementaryObject, e);
		}

		getPerunBl().getAuditer().log(sess, new RoleSetForGroup(complementaryObject, authorizedGroup, role));

		if (authorizedGroup != null && sess.getPerunPrincipal() != null && sess.getPerunPrincipal().getUser() != null) {
			List<Member> groupMembers = perunBl.getGroupsManagerBl().getGroupMembers(sess, authorizedGroup);
			List<Member> userMembers = perunBl.getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
			userMembers.retainAll(groupMembers);
			if (!userMembers.isEmpty()) AuthzResolverBlImpl.refreshAuthz(sess);
		}
	}

	/**
	 * Unset role for user and <b>one</b> complementary object.
	 * <p>
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess                perun session
	 * @param user                the user for unsetting role
	 * @param role                role of user in a session ( PERUNADMIN | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | RESOURCESELFSERVICE | RESOURCEADMIN )
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, User user, PerunBean complementaryObject, String role) throws UserNotAdminException, RoleCannotBeManagedException {
		if (!objectAndRoleManageableByEntity(user, complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, user);
		}

		Map<String, Integer> mappingOfValues = createMappingOfValues(user, complementaryObject, role);

		try {
			authzResolverImpl.unsetRole(sess, mappingOfValues, role);
		} catch (RoleNotSetException e) {
			throw new UserNotAdminException("User id=" + user.getId() + " is not "+role+" in " + complementaryObject, e);
		}

		getPerunBl().getAuditer().log(sess, new RoleUnsetForUser(complementaryObject, user, role));

		if (role.equals(Role.SPONSOR) && complementaryObject.getBeanName().equals("Vo"))
			getPerunBl().getVosManagerBl().handleUserLostVoRole(sess, user, (Vo) complementaryObject, Role.SPONSOR);

		if (user != null && sess.getPerunPrincipal() != null) {
			if (user.getId() == sess.getPerunPrincipal().getUserId()) {
				AuthzResolverBlImpl.refreshAuthz(sess);
			}
		}
	}

	/**
	 * Unset role for group and <b>one</b> complementary object
	 * <p>
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess                perun session
	 * @param authorizedGroup     the group for unsetting role
	 * @param role                role of user in a session ( PERUNADMIN | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | RESOURCESELFSERVICE | RESOURCEADMIN )
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, String role) throws GroupNotAdminException, RoleCannotBeManagedException {
		if (!objectAndRoleManageableByEntity(authorizedGroup, complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, authorizedGroup);
		}

		Map<String, Integer> mappingOfValues = createMappingOfValues(authorizedGroup, complementaryObject, role);

		try {
			authzResolverImpl.unsetRole(sess, mappingOfValues, role);
		} catch (RoleNotSetException e) {
			throw new GroupNotAdminException("Group id=" + authorizedGroup.getId() + " is not "+role+" in " + complementaryObject, e);
		}

		getPerunBl().getAuditer().log(sess, new RoleUnsetForGroup(complementaryObject, authorizedGroup, role));

		if (role.equals(Role.SPONSOR) && complementaryObject.getBeanName().equals("Vo"))
			getPerunBl().getVosManagerBl().handleGroupLostVoRole(sess, authorizedGroup, (Vo) complementaryObject, Role.SPONSOR);

		if (authorizedGroup != null && sess.getPerunPrincipal() != null && sess.getPerunPrincipal().getUser() != null) {
			List<Member> groupMembers = perunBl.getGroupsManagerBl().getGroupMembers(sess, authorizedGroup);
			List<Member> userMembers = perunBl.getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
			userMembers.retainAll(groupMembers);
			if (!userMembers.isEmpty()) AuthzResolverBlImpl.refreshAuthz(sess);
		}
	}

	/**
	 * Add owner for a specific user.
	 *
	 * @param sess Principal's session
	 * @param owner of the specific user
	 * @param specificUser for which will be the owner set
	 * @throws AlreadyAdminException
	 */
	public static void addSpecificUserOwner(PerunSession sess, User specificUser, User owner) throws AlreadyAdminException {
		if (owner != null && specificUser != null) authzResolverImpl.addAdmin(sess, specificUser, owner);
		else throw new InternalErrorException("Specific user and its owner cannot be null while adding specific user owner.");
	}

	/**
	 * Remove owner for a specific user.
	 *
	 * @param sess Principal's session
	 * @param owner of the specific user
	 * @param specificUser from which will be the owner unset
	 * @throws UserNotAdminException
	 */
	public static void removeSpecificUserOwner(PerunSession sess, User specificUser, User owner) throws UserNotAdminException {
		if (owner != null && specificUser != null) authzResolverImpl.removeAdmin(sess, specificUser, owner);
		else throw new InternalErrorException("Specific user and its owner cannot be null while removing specific user owner.");
	}

	/**
	 * Make user to be PERUNADMIN!
	 *
	 * @param sess PerunSession with authorization
	 * @param user which will get role "PERUNADMIN" in the system
	 * @throws InternalErrorException When implementation fails
	 * @throws AlreadyAdminException When user is already perun admin
	 */
	public static void makeUserPerunAdmin(PerunSession sess, User user) throws AlreadyAdminException {
		getPerunBl().getAuditer().log(sess, new UserPromotedToPerunAdmin(user));
		authzResolverImpl.makeUserPerunAdmin(sess, user);
	}

	/**
	 * Return all loaded perun policies.
	 *
	 * @return all loaded policies
	 */
	public static List<PerunPolicy> getAllPolicies() {
		return AuthzResolverImpl.getAllPolicies();
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
	 * Get all principal role names.
	 *
	 * @param sess perun session
	 * @return list of roles.
	 */
	public static List<String> getPrincipalRoleNames(PerunSession sess) {
		// We need to load the principals roles
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		return sess.getPerunPrincipal().getRoles().getRolesNames();
	}

	/**
	 * Get all User's roles.
	 *
	 * @param sess perun session
	 * @param user User
	 * @return list of roles.
	 */
	public static List<String> getUserRoleNames(PerunSession sess,User user) {

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
	public static AuthzRoles getUserRoles(PerunSession sess, User user) {

		return authzResolverImpl.getRoles(user);
	}

	/**
	 * Get all Group's roles.
	 *
	 * @param sess perun session
	 * @param group Group
	 * @return list of roles.
	 */
	public static List<String> getGroupRoleNames(PerunSession sess,Group group) {

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
	public static AuthzRoles getGroupRoles(PerunSession sess, Group group) {

		return authzResolverImpl.getRoles(group);
	}

	/**
	 * Returns user which is associated with credentials used to log-in to Perun.
	 *
	 * @param sess perun session
	 * @return currently logged user
	 */
	public static User getLoggedUser(PerunSession sess) {
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
	public static PerunPrincipal getPerunPrincipal(PerunSession sess) {
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
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, String role) {
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
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, String role, Class perunBeanClass) {
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
	public static synchronized void refreshAuthz(PerunSession sess) {
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
				roles = addAllSubgroupsToAuthzRoles(sess, authzResolverImpl.getRoles(user), Role.GROUPADMIN);
				roles = addAllSubgroupsToAuthzRoles(sess, roles, Role.GROUPOBSERVER);
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
	public static synchronized void refreshSession(PerunSession sess) {
		Utils.checkPerunSession(sess);
		log.trace("Refreshing session data for session {}.", sess);

		PerunPrincipal principal = sess.getPerunPrincipal();

		try {
			User user = getPerunBl().getUsersManagerBl().getUserByExtSourceInformation(sess, principal);
			sess.getPerunPrincipal().setUser(user);
		} catch (Exception ex) {
			// we don't care that user was not found - clear it from session
			sess.getPerunPrincipal().setUser(null);
		}

		AuthzResolverBlImpl.refreshAuthz(sess);

	}

	/**
	 * For the given role with association to "Group" add also all subgroups to authzRoles.
	 * If authzRoles is null, return empty AuthzRoles.
	 * If there is no role (given in parameter) or Group object for this role, return not changed authzRoles.
	 *
	 * @param sess       perun session
	 * @param authzRoles authzRoles for some user
	 * @return the same object authzRoles, which is given in parameter, but also with subgroups of groups for given role
	 */
	public static AuthzRoles addAllSubgroupsToAuthzRoles(PerunSession sess, AuthzRoles authzRoles, String role) {
		if (authzRoles == null) return new AuthzRoles();
		if (role == null || !authzRoles.hasRole(role)) return authzRoles;

		Map<String, Set<Integer>> groupRoles = authzRoles.get(role);
		Set<Integer> groupsIds = groupRoles.get("Group");
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
		groupRoles.put("Group", newGroupsIds);
		authzRoles.put(role, groupRoles);

		return authzRoles;
	}

	public static void removeAllAuthzForVo(PerunSession sess, Vo vo) {
		authzResolverImpl.removeAllAuthzForVo(sess, vo);
	}

	static List<Vo> getVosForGroupInRole(PerunSession sess, Group group, String role) {
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

	static void removeAllUserAuthz(PerunSession sess, User user) {
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

	static void removeAllSponsoredUserAuthz(PerunSession sess, User sponsoredUser) {
		authzResolverImpl.removeAllSponsoredUserAuthz(sess, sponsoredUser);
	}

	public static void removeAllAuthzForGroup(PerunSession sess, Group group) {
		//notify vosManager that the deleted group had SPONSOR role for some VOs
		for (Vo vo : getVosForGroupInRole(sess, group, Role.SPONSOR)) {
			getPerunBl().getVosManagerBl().handleGroupLostVoRole(sess, group, vo ,Role.SPONSOR);
		}
		//remove all roles from the group
		authzResolverImpl.removeAllAuthzForGroup(sess, group);
	}

	public static void removeAllAuthzForFacility(PerunSession sess, Facility facility) {
		authzResolverImpl.removeAllAuthzForFacility(sess, facility);
	}

	public static void removeAllAuthzForResource(PerunSession sess, Resource resource) {
		authzResolverImpl.removeAllAuthzForResource(sess, resource);
	}

	public static void removeAllAuthzForService(PerunSession sess, Service service) {
		authzResolverImpl.removeAllAuthzForService(sess, service);
	}

	public static void removeAllAuthzForSecurityTeam(PerunSession sess, SecurityTeam securityTeam) {
		authzResolverImpl.removeAllAuthzForSecurityTeam(sess, securityTeam);
	}

	public static void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, securityTeam, user);
	}

	public static void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, securityTeam, group);
	}

	public static void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException {
		authzResolverImpl.removeAdmin(sess, securityTeam, user);
	}

	public static void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws GroupNotAdminException {
		authzResolverImpl.removeAdmin(sess, securityTeam, group);
	}

	public static boolean roleExists(String role) {
		return authzResolverImpl.roleExists(role);
	}

	public static void loadAuthorizationComponents() { authzResolverImpl.loadAuthorizationComponents(); }

	/**
	 * Checks whether the user is in role for Vo.
	 *
	 * @param session perun session
	 * @param user user
	 * @param role role of user
	 * @param vo virtual organization
	 * @return true if user is in role for VO, false otherwise
	 */
	static boolean isUserInRoleForVo(PerunSession session, User user, String role, Vo vo) {
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
	static boolean isGroupInRoleForVo(PerunSession session, Group group, String role, Vo vo) {
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
	 * Prepare service roles to session AuthzRoles (PERUNADMIN, SERVICE, RPC, ENGINE etc.)
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

	/**
	 * Decide whether a principal has sufficient rights according the the given roles and objects.
	 *
	 * @param sess perunSession which contains the principal.
	 * @param policyRoles is a list of maps where each map entry consists from a role name as a key and a role object as a value.
	 *                    Relation between each map in the list is logical OR and relation between each entry in the map is logical AND.
	 *                    Example list - (Map1, Map2...)
	 *                    Example map - key: VOADMIN ; value: Vo
	 *                                 key: GROUPADMIN ; value: Group
	 * @param mapOfBeans is a map of objects against which will be authorization done.
	 *                    Example map entry - key: Member ; values: (10,15,26)
	 * @return true if the principal has particular rights, false otherwise.
	 */
	private static boolean resolveAuthorization(PerunSession sess, List<Map<String, String>> policyRoles, Map <String, Set<Integer>> mapOfBeans) {
		//Traverse through outer role list which works like logical OR
		for (Map<String, String> roleArray: policyRoles) {

			boolean authorized = true;
			//Traverse through inner role list which works like logical AND
			Set<String> roleArrayKeys = roleArray.keySet();
			for (String role : roleArrayKeys) {

				//fetch the object which is connected with the role
				String roleObject = roleArray.get(role);

				// If policy role is not connected to any object
				if (roleObject == null) {
					//If principal does not have the role, this inner list's result is false
					if (!sess.getPerunPrincipal().getRoles().hasRole(role)) authorized = false;
				//If there is no corresponding type of object in the perunBeans map
				} else if (!mapOfBeans.containsKey(roleObject)) {
					authorized = false;
				// If policy role is connected to some object, like VOADMIN->Vo
				} else {
					//traverse all related objects from perun which are relevant for the authorized method
					for (Integer objectId : mapOfBeans.get(roleObject)) {
						//If the principal does not have rights on role-object, this inner list's result is false
						if (!sess.getPerunPrincipal().getRoles().hasRole(role, roleObject, objectId)) {
							authorized = false;
							break;
						}
					}
				}
				//Some inner role check failed so jump out of the while loop
				if (!authorized) break;
			}
			// If all checks for inner role list pass, return true. Otherwise proceed to another inner role list
			if (authorized) return true;
		}
		//If no check passed, return false. The principal doesn't have sufficient rights.
		return false;
	}

	/**
	 * Fetch all possible PerunBeans for each of the objects from the list according to the id of the bean in the object.
	 *
	 * @param objects for which will be related objects fetched.
	 * @return all related objects together with the objects from the input as a map of PerunBean names and ids.
	 */
	private static Map<String, Set<Integer>> fetchAllRelatedObjects(List<PerunBean> objects) {
		if (objects == null) throw new InternalErrorException("A list of PerunBeans, used in authorization evaluation, cannot be null.");
		List<PerunBean> relatedObjects = new ArrayList<>();
		//Create a map from objects for easier manipulation and duplicity prevention
		Map<String, Set<Integer>> mapOfBeans = new HashMap<>();

		for (PerunBean object: objects) {
			if (object == null) throw new InternalErrorException("A list of PerunBeans, used in authorization evaluation, cannot contain a null value.");
			List<PerunBean> retrievedObjects = RelatedObjectsResolver.getValue(object.getBeanName()).apply(object);
			relatedObjects.addAll(retrievedObjects);
		}

		//Fill map with PerunBean names as keys and a set of unique ids as value for each bean name
		for (PerunBean object : relatedObjects) {
			if (!mapOfBeans.containsKey(object.getBeanName())) mapOfBeans.put(object.getBeanName(), new HashSet<>());
			mapOfBeans.get(object.getBeanName()).add(object.getId());
		}

		return mapOfBeans;
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects for the object with that name.
	 * The source object is returned alongside its related objects.
	 */
	private enum RelatedObjectsResolver implements Function<PerunBean, List<PerunBean>> {
		BanOnVo((object) -> {
			Vo vo = new Vo();
			vo.setId(((BanOnVo)object).getVoId());
			Member member = new Member();
			member.setId(((BanOnVo)object).getMemberId());
			return Arrays.asList(vo, object);
		}),
		UserExtSource((object) -> {
			User user = new User();
			user.setId(((UserExtSource) object).getUserId());
			return Arrays.asList(user, object);
		}),
		Member((object) -> {
			User user = new User();
			user.setId(((Member) object).getUserId());
			Vo vo = new Vo();
			vo.setId(((Member) object).getVoId());
			return Arrays.asList(user,vo, object);
		}),
		Group((object) -> {
			Vo vo = new Vo();
			vo.setId(((Group) object).getVoId());
			return Arrays.asList(vo, object);
		}),
		Resource((object) -> {
			Vo vo = new Vo();
			vo.setId(((Resource) object).getVoId());
			Facility facility = new Facility();
			facility.setId(((Resource) object).getFacilityId());
			return Arrays.asList(vo, facility, object);
		}),
		ResourceTag((object) -> {
			Vo vo = new Vo();
			vo.setId(((ResourceTag) object).getVoId());
			return Arrays.asList(vo, object);
		}),
		RichMember((object) -> {
			User user = new User();
			user.setId(((RichMember) object).getUserId());
			Vo vo = new Vo();
			vo.setId(((Member) object).getVoId());
			Member member = new Member();
			member.setId(object.getId());
			return Arrays.asList(user,vo, member);
		}),
		RichGroup((object) -> {
			Vo vo = new Vo();
			vo.setId(((RichGroup) object).getVoId());
			Group group = new Group();
			group.setId(object.getId());
			return Arrays.asList(vo, group);
		}),
		RichResource((object) -> {
			Vo vo = new Vo();
			vo.setId(((RichResource) object).getVoId());
			Facility facility = new Facility();
			facility.setId(((Resource) object).getFacilityId());
			Resource resource = new Resource();
			resource.setId(object.getId());
			return Arrays.asList(vo, facility, resource);
		}),
		Default((object) -> {
			return Collections.singletonList(object);
		});

		private Function<PerunBean, List<PerunBean>> function;

		RelatedObjectsResolver(final Function<PerunBean, List<PerunBean>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedObjectsResolver value.
		 */
		public static RelatedObjectsResolver getValue(String name) {
			try {
				return RelatedObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedObjectsResolver.Default;
			}
		}

		@Override
		public List<PerunBean> apply(PerunBean object) {
			return function.apply(object);
		}
	}

	/**
	 * Checks whether the given parameters satisfies the rules associated with the role.
	 *
	 * @param entityToManage to which will be the role set or unset
	 * @param complementaryObject which will be bounded with the role
	 * @param role which will be managed
	 * @return true if all given parameters imply with the associated rule, false otherwise.
	 */
	private static boolean objectAndRoleManageableByEntity(PerunBean entityToManage, PerunBean complementaryObject, String role) {
		RoleManagementRules rules;
		try {
			rules = AuthzResolverImpl.getRoleManagementRules(role);
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		Set<String> necessaryObjects = rules.getAssignedObjects().keySet();

		if (rules.getEntitiesToManage().containsKey(entityToManage.getBeanName())) {
			if (complementaryObject == null && necessaryObjects.isEmpty()) {
				return true;
			} else if (complementaryObject != null && !necessaryObjects.isEmpty()) {
				//Fetch super objects like Vo for group etc.
				Map<String, Set<Integer>> mapOfBeans = fetchAllRelatedObjects(Collections.singletonList(complementaryObject));
				return mapOfBeans.keySet().containsAll(necessaryObjects);
			}
		}
		return false;
	}

	/**
	 * Create a mapping of column names and ids which will be used for setting or unsetting of the role.
	 *
	 * @param entityToManage to which will be the role set or unset
	 * @param complementaryObject which will be bounded with the role
	 * @param role which will be managed
	 * @return final mapping of values
	 */
	private static Map<String, Integer> createMappingOfValues(PerunBean entityToManage, PerunBean complementaryObject, String role) {
		Map<String, Integer> mapping = new HashMap<>();

		RoleManagementRules rules;
		try {
			rules = AuthzResolverImpl.getRoleManagementRules(role);
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		Integer role_id = authzResolverImpl.getRoleId(role);
		mapping.put("role_id", role_id);
		mapping.put(rules.getEntitiesToManage().get(entityToManage.getBeanName()), entityToManage.getId());

		Map <String, Set<Integer>> mapOfBeans = new HashMap<>();
		if (complementaryObject != null) {
			//Fetch super objects like Vo for group etc.
			mapOfBeans = fetchAllRelatedObjects(Collections.singletonList(complementaryObject));
		}

		for (String objectType : rules.getAssignedObjects().keySet()) {
			if (!mapOfBeans.containsKey(objectType)) {
				throw new InternalErrorException("Cannot create a mapping for role management, because object of type: " + objectType + " cannot be obtained.");
			}

			if (mapOfBeans.get(objectType).size() != 1) {
				throw new InternalErrorException("Cannot create a mapping for role management, because there is more than one object of type: " + objectType + ".");
			}

			String definition = rules.getAssignedObjects().get(objectType);

			mapping.put(definition, mapOfBeans.get(objectType).iterator().next());
		}

		return mapping;
	}
}
