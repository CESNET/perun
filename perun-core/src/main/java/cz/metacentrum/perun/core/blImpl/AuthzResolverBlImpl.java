package cz.metacentrum.perun.core.blImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.AuthzResolverBl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import java.util.Map;
import java.util.Set;


/**
 * Authorization resolver. It decides if the perunPrincipal has rights to do the provided operation.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 *
 */
public class AuthzResolverBlImpl implements AuthzResolverBl {

	private final static Logger log = LoggerFactory.getLogger(AuthzResolverBlImpl.class);
	private static AuthzResolverImplApi authzResolverImpl;
	private static PerunBlImpl perunBlImpl;

	private static final String UNSET_ROLE = "UNSET";
	private static final String SET_ROLE = "SET";

	/**
	 * Retrieves information about the perun principal (in which VOs the principal is admin, ...)
	 *
	 * @param sess perunSession
	 * @throws InternalErrorException
	 */
	protected static void init(PerunSession sess) throws InternalErrorException {

		log.trace("Initializing AuthzResolver for [{}]", sess.getPerunPrincipal());

		//Prepare service roles like engine, service, registrar, perunAdmin etc.
		prepareServiceRoles(sess);

		if (!sess.getPerunPrincipal().getRoles().isEmpty()) {
			// We have some of the service principal, so we can quit
			sess.getPerunPrincipal().setAuthzInitialized(true);
			return;
		}

		// Prepare first users rights on all subgroups of groups where user is GroupAdmin and add them to AuthzRoles of the user
		AuthzRoles authzRoles = addAllSubgroupsToAuthzRoles(sess, authzResolverImpl.getRoles(sess.getPerunPrincipal().getUser()));

		// Load all user's roles with all possible subgroups
		sess.getPerunPrincipal().setRoles(authzRoles);

		// Add self role for the user
		if (sess.getPerunPrincipal().getUser() != null) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SELF, sess.getPerunPrincipal().getUser());

			// Add service user role
			if (sess.getPerunPrincipal().getUser().isServiceUser()) {
				sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SERVICEUSER);
			}
		}
		sess.getPerunPrincipal().setAuthzInitialized(true);
		log.debug("AuthzResolver: Complete PerunPrincipal: {}", sess.getPerunPrincipal());
	}

	private static AuthzRoles getRoles(PerunSession sess) throws InternalErrorException {
		if(sess == null || sess.getPerunPrincipal() == null || sess.getPerunPrincipal().getUser() == null) {
			return new AuthzRoles();
		}
		AuthzRoles authzRoles = addAllSubgroupsToAuthzRoles(sess, authzResolverImpl.getRoles(sess.getPerunPrincipal().getUser()));
		return authzRoles;
	}

	public static boolean isAuthorized(PerunSession sess, Role role, PerunBean complementaryObject) throws InternalErrorException {
		log.trace("Entering isAuthorized: sess='" +  sess + "', role='" +  role + "', complementaryObject='" +  complementaryObject + "'");
		Utils.notNull(sess, "sess");

		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			init(sess);
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

		// Check if the principal has the priviledge
		if (complementaryObject != null) {

			// Check various combinations of role and complementary objects
			if (role.equals(Role.VOADMIN) || role.equals(Role.VOOBSERVER)) {
				// VO admin (or VoObserver) and group, get vo id from group and check if the user is vo admin (or VoObserver)
				if (complementaryObject.getBeanName().equals(Group.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Group) complementaryObject).getVoId());
				}
				// VO admin (or VoObserver) and resource, check if the user is vo admin (or VoObserver)
				if (complementaryObject.getBeanName().equals(Resource.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Resource) complementaryObject).getVoId());
				}
				// VO admin (or VoObserver) and member, check if the member is from that VO
				if (complementaryObject.getBeanName().equals(Member.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Member) complementaryObject).getVoId());
				}
			} else if (role.equals(Role.FACILITYADMIN)) {
				// Facility admin and resource, get facility id from resource and check if the user is facility admin
				if (complementaryObject.getBeanName().equals(Resource.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Facility.class.getSimpleName(), ((Resource) complementaryObject).getFacilityId());
				}
			} else if (role.equals(Role.GROUPADMIN) || role.equals(Role.TOPGROUPCREATOR)) {
				// Group admin can see some of the date of the VO
				if (complementaryObject.getBeanName().equals(Vo.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Vo) complementaryObject).getId());
				}
			} else if (role.equals(Role.SELF)) {
				// Check if the member belogs to the self role
				if (complementaryObject.getBeanName().equals(Member.class.getSimpleName())) {
					return sess.getPerunPrincipal().getRoles().hasRole(role, User.class.getSimpleName(), ((Member) complementaryObject).getUserId());
				}
			}

			return sess.getPerunPrincipal().getRoles().hasRole(role, complementaryObject);
		} else {
			return true;
		}
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Object primaryHolder, Object secondaryHolder) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
		log.trace("Entering isAuthorizedForAttribute: sess='" +  sess + "', actiontType='" + actionType + "', attrDef='" + attrDef + "', primaryHolder='" + primaryHolder + "', secondaryHolder='" + secondaryHolder + "'");

		Utils.notNull(sess, "sess");
		Utils.notNull(actionType, "ActionType");
		Utils.notNull(attrDef, "AttributeDefinition");
		getPerunBlImpl().getAttributesManagerBl().checkActionTypeExists(sess, actionType);
		getPerunBlImpl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);

		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			init(sess);
		}

		// If the user has no roles, deny access
		if (sess.getPerunPrincipal().getRoles() == null) {
			return false;
		}

		// Perun admin can do anything
		if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)) {
			return true;
		}

		// Engine and Service can read attributes
		if ((sess.getPerunPrincipal().getRoles().hasRole(Role.ENGINE) || sess.getPerunPrincipal().getRoles().hasRole(Role.SERVICE)) && actionType.equals(ActionType.READ)) {
			return true;
		}

		//If attrDef is type of entityless, return false (only perunAdmin can read and write to entityless)
		if(getPerunBlImpl().getAttributesManagerBl().isFromNamespace(sess, attrDef, AttributesManager.NS_ENTITYLESS_ATTR)) return false;

		//This method get all possible roles which can do action on attribute
		List<Role> roles = getRolesWhichCanWorkWithAttribute(sess, actionType, attrDef);

		//Now get information about primary and secondary holders to identify them!
		//All possible useful perunBeans
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;

		//Get object for primaryHolder
		if(primaryHolder != null) {
			if(primaryHolder instanceof Vo) vo = (Vo) primaryHolder;
			else if(primaryHolder instanceof Facility) facility = (Facility) primaryHolder;
			else if(primaryHolder instanceof Group) group = (Group) primaryHolder;
			else if(primaryHolder instanceof Member) member = (Member) primaryHolder;
			else if(primaryHolder instanceof User) user = (User) primaryHolder;
			else if(primaryHolder instanceof Host) host = (Host) primaryHolder;
			else if(primaryHolder instanceof Resource) resource = (Resource) primaryHolder;
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder.");
			}
		} else {
			throw new InternalErrorException("Aiding attribtue must have perunBean which is not null.");
		}

		//Get object for secondaryHolder
		if(secondaryHolder != null) {
			if(secondaryHolder instanceof Vo) vo = (Vo) secondaryHolder;
			else if(secondaryHolder instanceof Facility) facility = (Facility) secondaryHolder;
			else if(secondaryHolder instanceof Group) group = (Group) secondaryHolder;
			else if(secondaryHolder instanceof Member) member = (Member) secondaryHolder;
			else if(secondaryHolder instanceof User) user = (User) secondaryHolder;
			else if(secondaryHolder instanceof Host) host = (Host) secondaryHolder;
			else if(secondaryHolder instanceof Resource) resource = (Resource) secondaryHolder;
			else {
				throw new InternalErrorException("There is unrecognized perunBean in secondaryHolder.");
			}
		} // If not, its ok, secondary holder can be null

		//Important: There is no options for other roles like service, serviceUser and other!
		try {
			if(resource != null && member != null) {
				if(roles.contains(Role.VOADMIN)) {
					List<Vo> vos = getPerunBlImpl().getVosManagerBl().getVosByPerunBean(sess, resource);
					for(Vo v: vos) {
						if(isAuthorized(sess, Role.VOADMIN, v)) return true;
					}
				}
				if(roles.contains(Role.VOOBSERVER)) {
					List<Vo> vos = getPerunBlImpl().getVosManagerBl().getVosByPerunBean(sess, resource);
					for(Vo v: vos) {
						if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
					}
				}
				if(roles.contains(Role.GROUPADMIN)) {
					//If groupManager has right on any group assigned to resource
					List<Group> groups = getPerunBlImpl().getGroupsManagerBl().getGroupsByPerunBean(sess, resource);
					for(Group g: groups) {
						if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
					}
				}
				if(roles.contains(Role.FACILITYADMIN)) {
					Facility facilityFromResource = getPerunBlImpl().getResourcesManagerBl().getFacility(sess, resource);
					if(isAuthorized(sess, Role.FACILITYADMIN, facilityFromResource)) return true;
				}
				if(roles.contains(Role.SELF)) {
					if(getPerunBlImpl().getUsersManagerBl().getUserByMember(sess, member).equals(sess.getPerunPrincipal().getUser())) return true;
				}
			} else if(resource != null && group != null) {
				if(roles.contains(Role.VOADMIN)) {
					List<Vo> vos = getPerunBlImpl().getVosManagerBl().getVosByPerunBean(sess, resource);
					for(Vo v: vos) {
						if(isAuthorized(sess, Role.VOADMIN, v)) return true;
					}
				}
				if(roles.contains(Role.VOOBSERVER)) {
					List<Vo> vos = getPerunBlImpl().getVosManagerBl().getVosByPerunBean(sess, resource);
					for(Vo v: vos) {
						if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
					}
				}
				if(roles.contains(Role.GROUPADMIN)) {
					//If groupManager has right on the group
					if(isAuthorized(sess, Role.GROUPADMIN, group)) return true;
				}
				if(roles.contains(Role.FACILITYADMIN)) {
					//IMPORTANT "for now possible, but need to discuss"
					if(getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resource).contains(group)) {
						List<Group> groups = getPerunBlImpl().getGroupsManagerBl().getGroupsByPerunBean(sess, resource);
						for(Group g: groups) {
							if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
						}
					}
				}
				if(roles.contains(Role.SELF)); //Not Allowed
			} else if(user != null && facility != null) {
				if(roles.contains(Role.VOADMIN)) {
					List<Member> membersFromUser = getPerunBlImpl().getMembersManagerBl().getMembersByUser(sess, user);
					List<Resource> resourcesFromUser = new ArrayList<Resource>();
					for(Member memberElement: membersFromUser) {
						resourcesFromUser.addAll(getPerunBlImpl().getResourcesManagerBl().getAssignedResources(sess, memberElement));
					}
					resourcesFromUser = new ArrayList<Resource>(new HashSet<Resource>(resourcesFromUser));
					resourcesFromUser.retainAll(getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility));
					List<Vo> vos = new ArrayList<Vo>();
					for(Resource resourceElement: resourcesFromUser) {
						vos.add(getPerunBlImpl().getResourcesManagerBl().getVo(sess, resourceElement));
					}
					for(Vo v: vos) {
						if(isAuthorized(sess, Role.VOADMIN, v)) return true;
					}
				}
				if(roles.contains(Role.VOOBSERVER)) {
					List<Member> membersFromUser = getPerunBlImpl().getMembersManagerBl().getMembersByUser(sess, user);
					List<Resource> resourcesFromUser = new ArrayList<Resource>();
					for(Member memberElement: membersFromUser) {
						resourcesFromUser.addAll(getPerunBlImpl().getResourcesManagerBl().getAssignedResources(sess, memberElement));
					}
					resourcesFromUser = new ArrayList<Resource>(new HashSet<Resource>(resourcesFromUser));
					resourcesFromUser.retainAll(getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility));
					List<Vo> vos = new ArrayList<Vo>();
					for(Resource resourceElement: resourcesFromUser) {
						vos.add(getPerunBlImpl().getResourcesManagerBl().getVo(sess, resourceElement));
					}
					for(Vo v: vos) {
						if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
					}
				}
				if(roles.contains(Role.GROUPADMIN)) {
					//If groupManager has rights on "any group which is assigned to any resource from the facility" and "the user has also member in vo where exists this group"
					List<Vo> userVos = getPerunBlImpl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
					Set<Integer> userVosIds = new HashSet<>();
					for(Vo voElement: userVos) {
						userVosIds.add(voElement.getId());
					}

					List<Resource> resourcesFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
					Set<Group> groupsFromFacility = new HashSet<Group>();
					for(Resource resourceElement: resourcesFromFacility) {
						groupsFromFacility.addAll(getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resourceElement));
					}

					for(Group groupElement: groupsFromFacility) {
						if(isAuthorized(sess, Role.GROUPADMIN, groupElement) && userVosIds.contains(groupElement.getVoId())) return true;
					}
				}
				if(roles.contains(Role.FACILITYADMIN)) if(isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
				if(roles.contains(Role.SELF)) if(isAuthorized(sess, Role.SELF, user)) return true;
			} else if(user != null) {
				if(roles.contains(Role.VOADMIN)) {
					//TEMPORARY, PROBABLY WILL BE FALSE
					List<Vo> vosFromUser = getPerunBlImpl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
					for(Vo v: vosFromUser) {
						if(isAuthorized(sess, Role.VOADMIN, v)) return true;
					}
				}
				if(roles.contains(Role.VOOBSERVER)) {
					//TEMPORARY, PROBABLY WILL BE FALSE
					List<Vo> vosFromUser = getPerunBlImpl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
					for(Vo v: vosFromUser) {
						if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
					}
				}
				if(roles.contains(Role.GROUPADMIN)) {
					//If principal is groupManager in any vo where user has member
					List<Vo> userVos = getPerunBlImpl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
					for(Vo voElement: userVos) {
							if(isAuthorized(sess, Role.GROUPADMIN, voElement)) return true;
					}
				}
				if(roles.contains(Role.FACILITYADMIN)); //Not allowed
				if(roles.contains(Role.SELF)) if(isAuthorized(sess, Role.SELF, user)) return true;
			} else if(member != null) {
				if(roles.contains(Role.VOADMIN)) {
					Vo v = getPerunBlImpl().getMembersManagerBl().getMemberVo(sess, member);
					if(isAuthorized(sess, Role.VOADMIN, v)) return true;
				}
				if(roles.contains(Role.VOOBSERVER)) {
					Vo v = getPerunBlImpl().getMembersManagerBl().getMemberVo(sess, member);
					if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
				}
				if(roles.contains(Role.GROUPADMIN)) {
					//if principal is groupManager in vo where the member has membership
					Vo v = getPerunBlImpl().getMembersManagerBl().getMemberVo(sess, member);
					if(isAuthorized(sess, Role.GROUPADMIN, v)) return true;
				}
				if(roles.contains(Role.FACILITYADMIN)); //Not allowed
				if(roles.contains(Role.SELF)) {
					User u = getPerunBlImpl().getUsersManagerBl().getUserByMember(sess, member);
					if(isAuthorized(sess, Role.SELF, u)) return true;
				}
			} else if(vo != null) {
				if(roles.contains(Role.VOADMIN)) {
					if(isAuthorized(sess, Role.VOADMIN, vo)) return true;
				}
				if(roles.contains(Role.VOOBSERVER)) {
					if(isAuthorized(sess, Role.VOOBSERVER, vo)) return true;
				}
				if(roles.contains(Role.GROUPADMIN)) {
					//if Principal is GroupManager in the vo
					if(isAuthorized(sess, Role.GROUPADMIN, vo)) return true;
				}
				if(roles.contains(Role.FACILITYADMIN)); //Not allowed
				if(roles.contains(Role.SELF)); //Not allowed
			} else if(group != null) {
				if(roles.contains(Role.VOADMIN)) {
					Vo v = getPerunBlImpl().getGroupsManagerBl().getVo(sess, group);
					if(isAuthorized(sess, Role.VOADMIN, v)) return true;
				}
				if(roles.contains(Role.VOOBSERVER)) {
					Vo v = getPerunBlImpl().getGroupsManagerBl().getVo(sess, group);
					if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
				}
				if(roles.contains(Role.GROUPADMIN)) if(isAuthorized(sess, Role.GROUPADMIN, group)) return true;
				if(roles.contains(Role.FACILITYADMIN)); //Not allowed
				if(roles.contains(Role.SELF)); //Not allowed
			} else if(resource != null) {
				if(roles.contains(Role.VOADMIN)) {
					Vo v = getPerunBlImpl().getResourcesManagerBl().getVo(sess, resource);
					if(isAuthorized(sess, Role.VOADMIN, v)) return true;
				}
				if(roles.contains(Role.VOOBSERVER)) {
					Vo v = getPerunBlImpl().getResourcesManagerBl().getVo(sess, resource);
					if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
				}
				if(roles.contains(Role.GROUPADMIN)); {
					List<Group> groupsFromResource = getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resource);
					for(Group g: groupsFromResource) {
						if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
					}
				}
				if(roles.contains(Role.FACILITYADMIN)) {
					Facility f = getPerunBlImpl().getResourcesManagerBl().getFacility(sess, resource);
					if(isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
				}
				if(roles.contains(Role.SELF)); //Not allowed
			} else if(facility != null) {
				if(roles.contains(Role.VOADMIN)) {
					List<Resource> resourcesFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
					List<Vo> vosFromResources = new ArrayList<Vo>();
					for(Resource resourceElement: resourcesFromFacility) {
						vosFromResources.add(getPerunBlImpl().getResourcesManagerBl().getVo(sess, resourceElement));
					}
					vosFromResources = new ArrayList<Vo>(new HashSet<Vo>(vosFromResources));
					for(Vo v: vosFromResources) {
						if(isAuthorized(sess, Role.VOADMIN, v)) return true;
					}
				}
				if(roles.contains(Role.VOOBSERVER)) {
					List<Resource> resourcesFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
					List<Vo> vosFromResources = new ArrayList<Vo>();
					for(Resource resourceElement: resourcesFromFacility) {
						vosFromResources.add(getPerunBlImpl().getResourcesManagerBl().getVo(sess, resourceElement));
					}
					vosFromResources = new ArrayList<Vo>(new HashSet<Vo>(vosFromResources));
					for(Vo v: vosFromResources) {
						if(isAuthorized(sess, Role.VOOBSERVER, v)) return true;
					}
				}
				if(roles.contains(Role.GROUPADMIN)) {
					List<Resource> resourcesFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
					List<Group> groupsFromFacility = new ArrayList<Group>();
					for(Resource resourceElement: resourcesFromFacility) {
						groupsFromFacility.addAll(getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resourceElement));
					}
					groupsFromFacility = new ArrayList<Group>(new HashSet<Group>(groupsFromFacility));
					for(Group g: groupsFromFacility){
						if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
					}
				}
				if(roles.contains(Role.FACILITYADMIN)) if(isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
				if(roles.contains(Role.SELF)) {
					List<User> usersFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
					if(usersFromFacility.contains(sess.getPerunPrincipal().getUser())) {
						return true;
					}
				}
			} else if(host != null) {
				if(roles.contains(Role.VOADMIN)); //Not allowed
				if(roles.contains(Role.VOOBSERVER)); //Not allowed
				if(roles.contains(Role.GROUPADMIN)); //Not allowed
				if(roles.contains(Role.FACILITYADMIN)) {
					Facility f = getPerunBlImpl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
					if(isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
				}
				if(roles.contains(Role.SELF)); //Not allowed
			} else {
				throw new InternalErrorException("There is no other possible variants for now!");
			}
		} catch (VoNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		return false;
	}

	public static List<Role> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
		getPerunBlImpl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);
		getPerunBlImpl().getAttributesManagerBl().checkActionTypeExists(sess, actionType);
		return cz.metacentrum.perun.core.impl.AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(sess, actionType, attrDef);
	}

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess perunSession
	 * @param role required role
	 *
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	public static boolean isAuthorized(PerunSession sess, Role role) throws InternalErrorException {
		return isAuthorized(sess, role, null);
	}


	/**
	 * Returns true if the perunPrincipal has requested role.
	 *
	 * @param perunPrincipal
	 * @param role role to be checked
	 */
	public static boolean hasRole(PerunPrincipal perunPrincipal, Role role) {
		return perunPrincipal.getRoles().hasRole(role);
	}

	/**
	 * Set role for user and all complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param user user for setting role
	 * @param role role
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, User user, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, AlreadyAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, SET_ROLE, null, user, role, null);
				//These exceptions should never happen
			} catch (GroupNotAdminException ex) {
				throw new InternalErrorException(ex);
			} catch (UserNotAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for(PerunBean compObject: complementaryObjects) {
				try {
					manageRole(sess, SET_ROLE, null, user, role, compObject);
					//These exceptions should never happen
				} catch (GroupNotAdminException ex) {
					throw new InternalErrorException(ex);
				} catch (UserNotAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * @see #setRole(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.User, cz.metacentrum.perun.core.api.Role, java.util.List, boolean)
	 * Only use 1 complementary object!
	 */
	public static void setRole(PerunSession sess, User user, PerunBean complementaryObject, Role role) throws InternalErrorException, AlreadyAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.setRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Set role for group and all complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param authorizedGroup group for setting role
	 * @param role role
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, AlreadyAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, SET_ROLE, authorizedGroup, null, role, null);
				//These exceptions should never happen
			} catch (GroupNotAdminException ex) {
				throw new InternalErrorException(ex);
			} catch (UserNotAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for(PerunBean compObject: complementaryObjects) {
				try {
					manageRole(sess, SET_ROLE, authorizedGroup, null, role, compObject);
					//These exceptions should never happen
				} catch (GroupNotAdminException ex) {
					throw new InternalErrorException(ex);
				} catch (UserNotAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * @see #setRole(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Role, java.util.List, boolean)
	 * Only use 1 complementary object!
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws InternalErrorException, AlreadyAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.setRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Unset role for user and all complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param user user for setting role
	 * @param role role
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	public static void unsetRole(PerunSession sess, User user, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, UserNotAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, UNSET_ROLE, null, user, role, null);
				//These exceptions should never happen
			} catch (GroupNotAdminException ex) {
				throw new InternalErrorException(ex);
			} catch (AlreadyAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for(PerunBean compObject: complementaryObjects) {
				try {
					manageRole(sess, UNSET_ROLE, null, user, role, compObject);
					//These exceptions should never happen
				} catch (GroupNotAdminException ex) {
					throw new InternalErrorException(ex);
				} catch (AlreadyAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * @see #unsetRole(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.User, cz.metacentrum.perun.core.api.Role, java.util.List, boolean) 
	 * Only use 1 complementary object!
	 */
	public static void unsetRole(PerunSession sess, User user, PerunBean complementaryObject, Role role) throws InternalErrorException, UserNotAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.unsetRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Unset role for group and all complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param authorizedGroup group for setting role
	 * @param role role
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, GroupNotAdminException {
		if (complementaryObjects == null || complementaryObjects.isEmpty()) {
			try {
				manageRole(sess, UNSET_ROLE, authorizedGroup, null, role, null);
				//These exceptions should never happen
			} catch (UserNotAdminException ex) {
				throw new InternalErrorException(ex);
			} catch (AlreadyAdminException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			for(PerunBean compObject: complementaryObjects) {
				try {
					manageRole(sess, UNSET_ROLE, authorizedGroup, null, role, compObject);
					//These exceptions should never happen
				} catch (UserNotAdminException ex) {
					throw new InternalErrorException(ex);
				} catch (AlreadyAdminException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}
	}

	/**
	 * @see #unsetRole(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Role, java.util.List, boolean) 
	 * Only use 1 complementary object!
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws InternalErrorException, GroupNotAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Set or unset role for user or authorized group and complementary object
	 *
	 * If user and authorizedGroup are null, throw exception. Only one can be filled at once, if both, throw exception.
	 * If complementaryObject is null, throw an exception if the role is not PerunAdmin.
	 *
	 * IMPORTANT: refresh authz only if user in session is affected
	 *
	 * @param sess
	 * @param user the user for set role
	 * @param authorizedGroup the group for set role
	 * @param operation 'SET' or 'UNSET'
	 * @param role role to set
	 * @param complementaryObject object for setting role on it
	 * @throws InternalErrorException
	 */
	public static void manageRole(PerunSession sess, String operation, Group authorizedGroup, User user, Role role, PerunBean complementaryObject) throws InternalErrorException, AlreadyAdminException, UserNotAdminException, GroupNotAdminException {
		if(authorizedGroup == null && user == null) throw new InternalErrorException("There is no object for setting role (user or authorizedGroup).");
		if(authorizedGroup != null && user != null) throw new InternalErrorException("There are both authorizedGroup and user for setting role, only one is acceptable.");
		if(!role.equals(Role.PERUNADMIN) && complementaryObject == null) throw new InternalErrorException("Complementary object can be null only for the role perunadmin.");

		//Check operation
		if(operation.equals(SET_ROLE)) {
			//Check role
			if(role.equals(Role.PERUNADMIN)) {
				if(user != null) makeUserPerunAdmin(sess, user);
				else throw new InternalErrorException("Not supported perunRole on authorizedGroup.");
			} else if(role.equals(Role.VOOBSERVER)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't set VoObserver rights without Vo.");
				} else if(complementaryObject instanceof Vo) {
					if(user != null) addObserver(sess, (Vo) complementaryObject, user);
					else addObserver(sess, (Vo) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
				}
			} else if(role.equals(Role.VOADMIN)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't set VoAdmin rights without Vo.");
				} else if(complementaryObject instanceof Vo) {
					if(user != null) addAdmin(sess, (Vo) complementaryObject, user);
					else addAdmin(sess, (Vo) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for VoAdmin: " + complementaryObject);
				}
			} else if(role.equals(Role.TOPGROUPCREATOR)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't set TopGroupCreator rights without Vo.");
				} else if(complementaryObject instanceof Vo) {
					if(user != null) addTopGroupCreator(sess, (Vo) complementaryObject, user);
					else addTopGroupCreator(sess, (Vo) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
				}
			} else if(role.equals(Role.GROUPADMIN)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't set GroupAdmin rights without Group.");
				} else if(complementaryObject instanceof Group) {
					if(user != null) addAdmin(sess, (Group) complementaryObject, user);
					else addAdmin(sess, (Group) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for GroupAdmin: " + complementaryObject);
				}
			} else if(role.equals(Role.FACILITYADMIN)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't set FacilityAdmin rights without Facility.");
				} else if(complementaryObject instanceof Facility) {
					if(user != null) addAdmin(sess, (Facility) complementaryObject, user);
					else addAdmin(sess, (Facility) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
				}
			} else {
				throw new InternalErrorException("Not supported role: " + role);
			}
		// Check operation
		} else if(operation.equals(UNSET_ROLE)) {
			//Check role
			if(role.equals(Role.PERUNADMIN)) {
				if(user != null) removePerunAdmin(sess, user);
				else throw new InternalErrorException("Not supported perunRole on authorizedGroup.");
			} else if(role.equals(Role.VOOBSERVER)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't unset VoObserver rights without Vo this way.");
				} else if(complementaryObject instanceof Vo) {
					if(user != null) removeObserver(sess, (Vo) complementaryObject, user);
					else removeObserver(sess, (Vo) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for VoObserver: " + complementaryObject);
				}
			} else if(role.equals(Role.VOADMIN)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't unset VoAdmin rights without Vo this way.");
				} else if(complementaryObject instanceof Vo) {
					if(user != null) removeAdmin(sess, (Vo) complementaryObject, user);
					else removeAdmin(sess, (Vo) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for VoAdmin: " + complementaryObject);
				}
			} else if(role.equals(Role.TOPGROUPCREATOR)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't set TopGroupCreator rights without Vo.");
				} else if(complementaryObject instanceof Vo) {
					if(user != null) removeTopGroupCreator(sess, (Vo) complementaryObject, user);
					else removeTopGroupCreator(sess, (Vo) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
				}
			} else if(role.equals(Role.GROUPADMIN)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't unset GroupAdmin rights without Group this way.");
				} else if(complementaryObject instanceof Group) {
					if(user != null) removeAdmin(sess, (Group) complementaryObject, user);
					else removeAdmin(sess, (Group) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for GroupAdmin: " + complementaryObject);
				}
			} else if(role.equals(Role.FACILITYADMIN)) {
				if(complementaryObject == null) {
					throw new InternalErrorException("Not supported operation, can't unset FacilityAdmin rights without Facility this way.");
				} else if(complementaryObject instanceof Facility) {
					if(user != null) removeAdmin(sess, (Facility) complementaryObject, user);
					else removeAdmin(sess, (Facility) complementaryObject, authorizedGroup);
				} else {
					throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
				}
			} else {
				throw new InternalErrorException("Not supported role: " + role);
			}
		} else {
			throw new InternalErrorException("Unsupported operation. Only set and unset are correct. Operation: " + operation);
		}

		//After set or unset role without exception, refresh authz if user in session is the same like user in parameter
		if(user != null && sess.getPerunPrincipal() != null) {
			if(user.getId() == sess.getPerunPrincipal().getUserId()) {
				AuthzResolverBlImpl.refreshAuthz(sess);
			}
		//If there is authorized group instead of user, try to find intersection in members and if there is at least one, then refresh authz
		} else if(authorizedGroup != null && sess.getPerunPrincipal() != null && sess.getPerunPrincipal().getUser() != null) {
			List<Member> groupMembers = perunBlImpl.getGroupsManagerBl().getGroupMembers(sess, authorizedGroup);
			List<Member> userMembers = perunBlImpl.getMembersManagerBl().getMembersByUser(sess, sess.getPerunPrincipal().getUser());
			userMembers.retainAll(groupMembers);
			if(!userMembers.isEmpty()) AuthzResolverBlImpl.refreshAuthz(sess);
		}
	}

	public String toString() {
		return getClass().getSimpleName() + ":[]";
	}

	public static boolean isVoAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN);
	}

	public static boolean isGroupAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.GROUPADMIN);
	}

	public static boolean isFacilityAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.FACILITYADMIN);
	}

	public static boolean isVoObserver(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.VOOBSERVER);
	}

	public static boolean isTopGroupCreator(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.TOPGROUPCREATOR);
	}

	public static boolean isPerunAdmin(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN);
	}

	/*
	 * Extracts only roles without complementary objects.
	 */
	public static List<String> getPrincipalRoleNames(PerunSession sess) throws InternalErrorException {
		// We need to load the principals roles
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			init(sess);
		}

		return sess.getPerunPrincipal().getRoles().getRolesNames();
	}

	public static User getLoggedUser(PerunSession sess) throws UserNotExistsException, InternalErrorException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			init(sess);
		}
		return sess.getPerunPrincipal().getUser();
	}

	public static PerunPrincipal getPerunPrincipal(PerunSession sess) throws InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			init(sess);
		}

		return sess.getPerunPrincipal();
	}

	/**
	 * Returns all complementary objects for defined role.
	 *
	 * @param sess
	 * @param role
	 * @return list of complementary objects
	 * @throws InternalErrorException
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role) throws InternalErrorException {
		return AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role, null);
	}

	/**
	 * Returns only complementary objects for defined role wich fits perunBeanClass class.
	 *
	 * @param sess
	 * @param role
	 * @param PerunBean particular class (e.g. Vo, Group, ...)
	 * @return list of complementary objects
	 * @throws InternalErrorException
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role, Class perunBeanClass) throws InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(sess.getPerunPrincipal(), "sess.getPerunPrincipal()");

		List<PerunBean> complementaryObjects = new ArrayList<PerunBean>();
		try {
			if (sess.getPerunPrincipal().getRoles().get(role) != null) {
				for (String beanName : sess.getPerunPrincipal().getRoles().get(role).keySet()) {
					// Do we filter results on particular class?
					if (perunBeanClass == null || beanName.equals(perunBeanClass.getSimpleName())) {

						if (beanName.equals(Vo.class.getSimpleName())) {
							for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
								complementaryObjects.add(perunBlImpl.getVosManagerBl().getVoById(sess, beanId));
							}
						}

						if (beanName.equals(Group.class.getSimpleName())) {
							for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
								complementaryObjects.add(perunBlImpl.getGroupsManagerBl().getGroupById(sess, beanId));
							}
						}

						if (beanName.equals(Facility.class.getSimpleName())) {
							for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
								complementaryObjects.add(perunBlImpl.getFacilitiesManagerBl().getFacilityById(sess, beanId));
							}
						}

						if (beanName.equals(Resource.class.getSimpleName())) {
							for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
								complementaryObjects.add(perunBlImpl.getResourcesManagerBl().getResourceById(sess, beanId));
							}
						}

						if (beanName.equals(Service.class.getSimpleName())) {
							for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
								complementaryObjects.add(perunBlImpl.getServicesManagerBl().getServiceById(sess, beanId));
							}
						}
					}
				}
			}

			return complementaryObjects;

		} catch (PerunException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Refresh authorization data inside session.
	 *
	 * Fill in proper roles and their relative entities (vos, groups, ....).
	 * User itself or ext source data is NOT updated.
	 *
	 * @param sess PerunSession to refresh authz for
	 * @throws InternalErrorException
	 */
	public static synchronized void refreshAuthz(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);
		log.debug("Refreshing authz roles for session {}.", sess);

		sess.getPerunPrincipal().setRoles(AuthzResolverBlImpl.getRoles(sess));
		prepareServiceRoles(sess);

		// Add self role for the user
		if (sess.getPerunPrincipal().getUser() != null) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SELF, sess.getPerunPrincipal().getUser());

			// Add service user role
			if (sess.getPerunPrincipal().getUser().isServiceUser()) {
				sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SERVICEUSER);
			}
		}

		sess.getPerunPrincipal().setAuthzInitialized(true);
	}

	/**
	 * Refresh all session data excluding ext source and additionalInformations.
	 *
	 * This method update user in session (try to find user by ext. source data).
	 * Then it updates authorization data in session.
	 *
	 * @param sess PerunSession to refresh data for
	 * @throws InternalErrorException
	 */
	public static synchronized void refreshSession(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);
		log.debug("Refreshing session data for session {}.", sess);

		try {
			User user = perunBlImpl.getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, sess.getPerunPrincipal().getExtSourceName(), sess.getPerunPrincipal().getActor());
			sess.getPerunPrincipal().setUser(user);
		} catch (Exception ex) {
			// we don't care that user was not found
		}

		AuthzResolverBlImpl.refreshAuthz(sess);

	}

	/**
	 * For role GroupAdmin with association to "Group" add also all subgroups to authzRoles.
	 * If authzRoles is null, return empty AuthzRoles.
	 * If there is no GroupAdmin role or Group object for this role, return not changed authzRoles.
	 *
	 * @param sess
	 * @param authzRoles authzRoles for some user
	 * @return authzRoles also with subgroups of groups
	 * @throws InternalErrorException
	 */
	public static AuthzRoles addAllSubgroupsToAuthzRoles(PerunSession sess, AuthzRoles authzRoles) throws InternalErrorException {
		if(authzRoles == null) return new AuthzRoles();
		if(authzRoles.hasRole(Role.GROUPADMIN)) {
			Map<String, Set<Integer>> groupAdminRoles = authzRoles.get(Role.GROUPADMIN);
			Set<Integer> groupsIds = groupAdminRoles.get("Group");
			Set<Integer> newGroupsIds = new HashSet<Integer>(groupsIds);
			for(Integer id: groupsIds) {
				Group parentGroup;
				try {
					parentGroup = getPerunBlImpl().getGroupsManagerBl().getGroupById(sess, id);
				} catch (GroupNotExistsException ex) {
					log.debug("Group with id=" + id + " not exists when initializing rights for user: " + sess.getPerunPrincipal().getUser());
					continue;
				}
				List<Group> subGroups = getPerunBlImpl().getGroupsManagerBl().getAllSubGroups(sess, parentGroup);
				for(Group g: subGroups) {
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

	public static void removeAllUserAuthz(PerunSession sess, User user) throws InternalErrorException {
		authzResolverImpl.removeAllUserAuthz(sess, user);
	}

	public static void removeAllAuthzForGroup(PerunSession sess, Group group) throws InternalErrorException {
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

	public static void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, facility, user);
	}

	public static void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, facility, group);
	}

	public static void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, UserNotAdminException {
		authzResolverImpl.removeAdmin(sess, facility, user);
	}

	public static void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, GroupNotAdminException {
		authzResolverImpl.removeAdmin(sess, facility, group);
	}

	public static void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, group, user);
	}

	public static void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, group, authorizedGroup);
	}

	public static void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, UserNotAdminException {
		authzResolverImpl.removeAdmin(sess, group, user);
	}

	public static void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException {
		authzResolverImpl.removeAdmin(sess, group, authorizedGroup);
	}

	public static void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, vo, user);
	}

	public static void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addAdmin(sess, vo, group);
	}

	public static void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		authzResolverImpl.removeAdmin(sess, vo, user);
	}

	public static void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		authzResolverImpl.removeAdmin(sess, vo, group);
	}

	public static void addObserver(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addObserver(sess, vo, user);
	}

	public static void addObserver(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addObserver(sess, vo, group);
	}

	public static void addTopGroupCreator(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addTopGroupCreator(sess, vo, user);
	}

	public static void addTopGroupCreator(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		authzResolverImpl.addTopGroupCreator(sess, vo, group);
	}

	public static void removeObserver(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		authzResolverImpl.removeObserver(sess, vo, user);
	}

	public static void removeObserver(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		authzResolverImpl.removeObserver(sess, vo, group);
	}

	public static void removeTopGroupCreator(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		authzResolverImpl.removeTopGroupCreator(sess, vo, user);
	}

	public static void removeTopGroupCreator(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		authzResolverImpl.removeTopGroupCreator(sess, vo, group);
	}

	public static void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		authzResolverImpl.makeUserPerunAdmin(sess, user);
	}

	public static void removePerunAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException {
		authzResolverImpl.removePerunAdmin(sess, user);
	}

	// Filled by Spring
	public static AuthzResolverImplApi setAuthzResolverImpl(AuthzResolverImplApi authzResolverImpl) {
		AuthzResolverBlImpl.authzResolverImpl = authzResolverImpl;
		return authzResolverImpl;
	}

	//Filled by Spring
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perunBlImpl) {
		AuthzResolverBlImpl.perunBlImpl = perunBlImpl;
		return perunBlImpl;
	}

	public static PerunBlImpl getPerunBlImpl() {
		return perunBlImpl;
	}

	/**
	 * Prepare service roles to session AuthzRoles (perunadmin, service, rpc, engine etc.)
	 *
	 * @param sess use session to add roles
	 * @throws InternalErrorException
	 */
	private static void prepareServiceRoles(PerunSession sess) throws InternalErrorException {
		// Load list of perunAdmins from the configuration, split the list by the comma
		List<String> perunAdmins = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.admins").split("[ \t]*,[ \t]*")));

		// Check if the PerunPrincipal is in a group of Perun Admins
		if (perunAdmins.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);
			sess.getPerunPrincipal().setAuthzInitialized(true);
			// We can quit, because perun admin has all privileges
			log.trace("AuthzResolver.init: Perun Admin {} loaded", sess.getPerunPrincipal().getActor());
			return;
		}

		String perunRpcAdmin = Utils.getPropertyFromConfiguration("perun.rpc.principal");
		if (sess.getPerunPrincipal().getActor().equals(perunRpcAdmin)) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.RPC);
			log.trace("AuthzResolver.init: Perun RPC {} loaded", perunRpcAdmin);
		}

		List<String> perunServiceAdmins = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.service.principals").split("[ \t]*,[ \t]*")));
		if (perunServiceAdmins.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SERVICE);
			log.trace("AuthzResolver.init: Perun Service {} loaded", perunServiceAdmins);
		}

		List<String> perunEngineAdmins = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.engine.principals").split("[ \t]*,[ \t]*")));
		if (perunEngineAdmins.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.ENGINE);
			log.trace("AuthzResolver.init: Perun Engine {} loaded", perunEngineAdmins);
		}

		List<String> perunSynchronizers = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.synchronizer.principals").split("[ \t]*,[ \t]*")));
		if (perunSynchronizers.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SYNCHRONIZER);
			log.trace("AuthzResolver.init: Perun Synchronizer {} loaded", perunSynchronizers);
		}

		List<String> perunNotifications = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.notification.principals").split("[ \t]*,[ \t]*")));
		if (perunNotifications.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.NOTIFICATIONS);

			//FIXME ted pridame i roli plneho admina
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);

			log.trace("AuthzResolver.init: Perun Notifications {} loaded", perunNotifications);
		}

		List<String> perunRegistrars = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.registrar.principals").split("[ \t]*,[ \t]*")));
		if (perunRegistrars.contains(sess.getPerunPrincipal().getActor())) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.REGISTRAR);

			//FIXME ted pridame i roli plneho admina
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);

			log.trace("AuthzResolver.init: Perun Registrar {} loaded", perunRegistrars);
		}
	}
}
