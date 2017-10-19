package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.AuthzResolverBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

	/**
	 * Retrieves information about the perun principal (in which VOs the principal is admin, ...)
	 *
	 * @param sess perunSession
	 * @throws InternalErrorException when problem
	 */
	protected static void init(PerunSession sess) throws InternalErrorException {
		log.trace("Initializing AuthzResolver for [{}]", sess.getPerunPrincipal());

		refreshAuthz(sess);
		
		log.debug("AuthzResolver: Complete PerunPrincipal: {}", sess.getPerunPrincipal());
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
	public static boolean isAuthorized(PerunSession sess, Role role, PerunBean complementaryObject) throws InternalErrorException {
		log.trace("Entering isAuthorized: sess='" + sess + "', role='" + role + "', complementaryObject='" + complementaryObject + "'");
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

	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Object primaryHolder, Object secondaryHolder) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
		log.trace("Entering isAuthorizedForAttribute: sess='" + sess + "', actionType='" + actionType + "', attrDef='" + attrDef + "', primaryHolder='" + primaryHolder + "', secondaryHolder='" + secondaryHolder + "'");

		Utils.notNull(sess, "sess");
		Utils.notNull(actionType, "ActionType");
		Utils.notNull(attrDef, "AttributeDefinition");
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);

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
		if (sess.getPerunPrincipal().getRoles().hasRole(Role.ENGINE) && actionType.equals(ActionType.READ)) {
			return true;
		}

		//If attrDef is type of entityless, return false (only perunAdmin can read and write to entityless)
		if (getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attrDef, AttributesManager.NS_ENTITYLESS_ATTR))
			return false;

		//This method get all possible roles which can do action on attribute
		List<Role> roles = cz.metacentrum.perun.core.impl.AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(actionType, attrDef);

		//Now get information about primary and secondary holders to identify them!
		//All possible useful perunBeans
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;
		UserExtSource ues = null;

		//Get object for primaryHolder
		if (primaryHolder != null) {
			if (primaryHolder instanceof Vo) vo = (Vo) primaryHolder;
			else if (primaryHolder instanceof Facility) facility = (Facility) primaryHolder;
			else if (primaryHolder instanceof Group) group = (Group) primaryHolder;
			else if (primaryHolder instanceof Member) member = (Member) primaryHolder;
			else if (primaryHolder instanceof User) user = (User) primaryHolder;
			else if (primaryHolder instanceof Host) host = (Host) primaryHolder;
			else if (primaryHolder instanceof Resource) resource = (Resource) primaryHolder;
			else if (primaryHolder instanceof UserExtSource) ues = (UserExtSource) primaryHolder;
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder.");
			}
		} else {
			throw new InternalErrorException("Adding attribute must have perunBean which is not null.");
		}

		//Get object for secondaryHolder
		if (secondaryHolder != null) {
			if (secondaryHolder instanceof Vo) vo = (Vo) secondaryHolder;
			else if (secondaryHolder instanceof Facility) facility = (Facility) secondaryHolder;
			else if (secondaryHolder instanceof Group) group = (Group) secondaryHolder;
			else if (secondaryHolder instanceof Member) member = (Member) secondaryHolder;
			else if (secondaryHolder instanceof User) user = (User) secondaryHolder;
			else if (secondaryHolder instanceof Host) host = (Host) secondaryHolder;
			else if (secondaryHolder instanceof Resource) resource = (Resource) secondaryHolder;
			else if (secondaryHolder instanceof UserExtSource) ues = (UserExtSource) secondaryHolder;
			else {
				throw new InternalErrorException("There is unrecognized perunBean in secondaryHolder.");
			}
		} // If not, its ok, secondary holder can be null

		//Important: There is no options for other roles like service, serviceUser and other!
		if (resource != null && member != null) {
			if (roles.contains(Role.VOADMIN)) {
				if (isAuthorized(sess, Role.VOADMIN, member)) return true;
			}
			if (roles.contains(Role.VOOBSERVER)) {
				if (isAuthorized(sess, Role.VOOBSERVER, member)) return true;
			}
			if (roles.contains(Role.FACILITYADMIN)) {
				if (isAuthorized(sess, Role.FACILITYADMIN, resource)) return true;
			}
			if (roles.contains(Role.SELF)) {
				if (isAuthorized(sess, Role.SELF, member)) return true;
			}
			if (roles.contains(Role.GROUPADMIN)) {
				//If groupManager has right on any group assigned to resource
				List<Group> groups = getPerunBl().getGroupsManagerBl().getGroupsByPerunBean(sess, resource);
				for (Group g : groups) {
					if (isAuthorized(sess, Role.GROUPADMIN, g)) return true;
				}
			}
		} else if (resource != null && group != null) {
			if (roles.contains(Role.VOADMIN)) {
				if (isAuthorized(sess, Role.VOADMIN, resource)) return true;
			}
			if (roles.contains(Role.VOOBSERVER)) {
				if (isAuthorized(sess, Role.VOOBSERVER, resource)) return true;
			}
			if (roles.contains(Role.GROUPADMIN)) {
				//If groupManager has right on the group
				if (isAuthorized(sess, Role.GROUPADMIN, group)) return true;
			}
			if (roles.contains(Role.FACILITYADMIN)) {
				//IMPORTANT "for now possible, but need to discuss"
				if (getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource).contains(group)) {
					List<Group> groups = getPerunBl().getGroupsManagerBl().getGroupsByPerunBean(sess, resource);
					for (Group g : groups) {
						if (isAuthorized(sess, Role.GROUPADMIN, g)) return true;
					}
				}
			}
//			if (roles.contains(Role.SELF)) ; //Not Allowed
		} else if (user != null && facility != null) {
			if (roles.contains(Role.FACILITYADMIN)) if (isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
			if (roles.contains(Role.SELF)) if (isAuthorized(sess, Role.SELF, user)) return true;
			if (roles.contains(Role.VOADMIN)) {
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
			if (roles.contains(Role.VOOBSERVER)) {
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
			if (roles.contains(Role.GROUPADMIN)) {
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
		} else if (member != null && group != null) {
			if (roles.contains(Role.VOADMIN)) {
				if (isAuthorized(sess, Role.VOADMIN, member)) return true;
			}
			if (roles.contains(Role.VOOBSERVER)) {
				if (isAuthorized(sess, Role.VOOBSERVER, member)) return true;
			}
			if (roles.contains(Role.SELF)) {
				if (isAuthorized(sess, Role.SELF, member)) return true;
			}
			if (roles.contains(Role.GROUPADMIN)) {
				if (isAuthorized(sess, Role.GROUPADMIN, group)) return true;
			}
		} else if (user != null) {
			if (roles.contains(Role.SELF)) if (isAuthorized(sess, Role.SELF, user)) return true;
			if (roles.contains(Role.VOADMIN)) {
				//TEMPORARY, PROBABLY WILL BE FALSE
				List<Vo> vosFromUser = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
				for (Vo v : vosFromUser) {
					if (isAuthorized(sess, Role.VOADMIN, v)) return true;
				}
			}
			if (roles.contains(Role.VOOBSERVER)) {
				//TEMPORARY, PROBABLY WILL BE FALSE
				List<Vo> vosFromUser = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
				for (Vo v : vosFromUser) {
					if (isAuthorized(sess, Role.VOOBSERVER, v)) return true;
				}
			}
			if (roles.contains(Role.GROUPADMIN)) {
				//If principal is groupManager in any vo where user has member
				List<Vo> userVos = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
				for (Vo voElement : userVos) {
					if (isAuthorized(sess, Role.GROUPADMIN, voElement)) return true;
				}
			}
//			if (roles.contains(Role.FACILITYADMIN)) ; //Not allowed
		} else if (member != null) {

			if (roles.contains(Role.VOADMIN)) {
				if (isAuthorized(sess, Role.VOADMIN, member)) return true;
			}
			if (roles.contains(Role.VOOBSERVER)) {
				if (isAuthorized(sess, Role.VOOBSERVER, member)) return true;
			}
			if (roles.contains(Role.SELF)) {
				if (isAuthorized(sess, Role.SELF, member)) return true;
			}
			if (roles.contains(Role.GROUPADMIN)) {
				//if principal is groupManager in vo where the member has membership
				Vo v = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);
				if (isAuthorized(sess, Role.GROUPADMIN, v)) return true;
			}
//			if (roles.contains(Role.FACILITYADMIN)) ; //Not allowed
		} else if (vo != null) {
			if (roles.contains(Role.VOADMIN)) {
				if (isAuthorized(sess, Role.VOADMIN, vo)) return true;
			}
			if (roles.contains(Role.VOOBSERVER)) {
				if (isAuthorized(sess, Role.VOOBSERVER, vo)) return true;
			}
			if (roles.contains(Role.GROUPADMIN)) {
				//if Principal is GroupManager in the vo
				if (isAuthorized(sess, Role.GROUPADMIN, vo)) return true;
			}
			if (roles.contains(Role.FACILITYADMIN)) {
				// is facility manager of any vo resource
				List<Resource> resourceList = perunBl.getResourcesManagerBl().getResources(sess, vo);
				for (Resource res : resourceList) {
					if (isAuthorized(sess, Role.FACILITYADMIN, res)) return true;
				}
			}
			if (roles.contains(Role.SELF)) {
				if (actionType.equals(ActionType.READ)) {
					// any user can read
					return true;
				} else if (actionType.equals(ActionType.WRITE)) {
					// only vo member can write
					try {
						perunBl.getMembersManagerBl().getMemberByUser(sess, vo, sess.getPerunPrincipal().getUser());
						return true;
					} catch (MemberNotExistsException ex) {
						// not vo member -> not allowed
					}
				}
			}
		} else if (group != null) {
			if (roles.contains(Role.VOADMIN)) {
				if (isAuthorized(sess, Role.VOADMIN, group)) return true;
			}
			if (roles.contains(Role.VOOBSERVER)) {
				if (isAuthorized(sess, Role.VOOBSERVER, group)) return true;
			}
			if (roles.contains(Role.GROUPADMIN)) if (isAuthorized(sess, Role.GROUPADMIN, group)) return true;
//			if (roles.contains(Role.FACILITYADMIN)) ; //Not allowed
//			if (roles.contains(Role.SELF)) ; //Not allowed
		} else if (resource != null) {
			if (roles.contains(Role.VOADMIN)) {
				if (isAuthorized(sess, Role.VOADMIN, resource)) return true;
			}
			if (roles.contains(Role.VOOBSERVER)) {
				if (isAuthorized(sess, Role.VOOBSERVER, resource)) return true;
			}
			if (roles.contains(Role.FACILITYADMIN)) {
				if (isAuthorized(sess, Role.FACILITYADMIN, resource)) return true;
			}
			if (roles.contains(Role.RESOURCEADMIN)) {
				if (isAuthorized(sess, Role.RESOURCEADMIN, resource)) return true;
			}
			if (roles.contains(Role.GROUPADMIN)) {
				List<Group> groupsFromResource = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
				for (Group g : groupsFromResource) {
					if (isAuthorized(sess, Role.GROUPADMIN, g)) return true;
				}
			}
//			if (roles.contains(Role.SELF)) ; //Not allowed
		} else if (facility != null) {
			if (roles.contains(Role.FACILITYADMIN)) if (isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
			if (roles.contains(Role.VOADMIN)) {
				List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
				for (Resource r : resourcesFromFacility) {
					if (isAuthorized(sess, Role.VOADMIN, r)) return true;
				}
			}
			if (roles.contains(Role.VOOBSERVER)) {
				List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
				for (Resource r : resourcesFromFacility) {
					if (isAuthorized(sess, Role.VOOBSERVER, r)) return true;
				}
			}
			if (roles.contains(Role.GROUPADMIN)) {
				List<Group> groupsFromFacility = getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
				for (Group g : groupsFromFacility) {
					if (isAuthorized(sess, Role.GROUPADMIN, g)) return true;
				}
			}
			if (roles.contains(Role.SELF)) {
				List<User> usersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
				if (usersFromFacility.contains(sess.getPerunPrincipal().getUser())) {
					return true;
				}
			}
		} else if (host != null) {
//			if (roles.contains(Role.VOADMIN)) ; //Not allowed
//			if (roles.contains(Role.VOOBSERVER)) ; //Not allowed
//			if (roles.contains(Role.GROUPADMIN)) ; //Not allowed
			if (roles.contains(Role.FACILITYADMIN)) {
				Facility f = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
				if (isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
			}
//			if (roles.contains(Role.SELF)) ; //Not allowed
		} else //noinspection ConstantConditions
			if (ues != null) {
			User sessUser = sess.getPerunPrincipal().getUser();
			User uesUser;
			try {
				uesUser = getPerunBl().getUsersManagerBl().getUserById(sess, ues.getUserId());
			} catch (UserNotExistsException ex) {
				return false;
			}
			if (ues.getUserId() == sessUser.getId()) return true;
			if (roles.contains(Role.FACILITYADMIN)) {
				List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getAssignedFacilities(sess, uesUser);
				for (Facility f : facilities) {
					if (isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
				}
			}
			if (roles.contains(Role.VOADMIN) || roles.contains(Role.VOOBSERVER)) {
				List<Vo> vos = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, uesUser);
				for (Vo v : vos) {
					if (isAuthorized(sess, Role.VOADMIN, v)) return true;
					if (isAuthorized(sess, Role.VOOBSERVER, v)) return true;
				}
			}
			if (roles.contains(Role.GROUPADMIN)) {
				List<Vo> vos = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, uesUser);
				for (Vo v : vos) {
					if (isAuthorized(sess, Role.GROUPADMIN, v)) return true;
				}
			}
		} else {
			throw new InternalErrorException("There is no other possible variants for now!");
		}

		return false;
	}

	/**
	 * Return list of roles which are authorized for doing "action" on "attribute".
	 *
	 * @param sess       perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef    attribute what principal want to work with
	 * @return list of roles
	 */
	public static List<Role> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
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
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | securityadmin  )
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
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator | securityadmin )
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
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
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
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
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
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
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
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
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
	 * @param role                 role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
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
	 * @param role                role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws InternalErrorException, GroupNotAdminException {
		List<PerunBean> complementaryObjects = new ArrayList<>();
		complementaryObjects.add(complementaryObject);
		AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, role, complementaryObjects);
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
		if (!role.equals(Role.PERUNADMIN) && complementaryObject == null)
			throw new InternalErrorException("Complementary object can be null only for the role perunadmin.");

		//Check operation
		switch (operation) {
			case SET_ROLE:
				//Check role
				if (role.equals(Role.PERUNADMIN)) {
					if (user != null) authzResolverImpl.makeUserPerunAdmin(sess, user);
					else throw new InternalErrorException("Not supported perunRole on authorizedGroup.");
				} else if (role.equals(Role.VOOBSERVER)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set VoObserver rights without Vo.");
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.addVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, user);
						else authzResolverImpl.addVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
					}
				} else if (role.equals(Role.VOADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set VoAdmin rights without Vo.");
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.addVoRole(sess, Role.VOADMIN,(Vo) complementaryObject, user);
						else authzResolverImpl.addVoRole(sess, Role.VOADMIN, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.TOPGROUPCREATOR)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set TopGroupCreator rights without Vo.");
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.addVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, user);
						else authzResolverImpl.addVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
					}
				} else if (role.equals(Role.GROUPADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set GroupAdmin rights without Group.");
					} else if (complementaryObject instanceof Group) {
						if (user != null) authzResolverImpl.addAdmin(sess, (Group) complementaryObject, user);
						else authzResolverImpl.addAdmin(sess, (Group) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for GroupAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.FACILITYADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set FacilityAdmin rights without Facility.");
					} else if (complementaryObject instanceof Facility) {
						if (user != null) authzResolverImpl.addAdmin(sess, (Facility) complementaryObject, user);
						else authzResolverImpl.addAdmin(sess, (Facility) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.RESOURCEADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set ResourceAdmin rights without Resource.");
					} else if (complementaryObject instanceof Resource) {
						if (user != null) authzResolverImpl.addAdmin(sess, (Resource) complementaryObject, user);
						else authzResolverImpl.addAdmin(sess, (Resource) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for ResourceAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.SECURITYADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set SecurityAdmin rights without SecurityTeam.");
					} else if (complementaryObject instanceof SecurityTeam) {
						if (user != null) addAdmin(sess, (SecurityTeam) complementaryObject, user);
						else addAdmin(sess, (SecurityTeam) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.SPONSOR)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set SponsoredUser rights without user.");
					} else if (complementaryObject instanceof User) {
						if (user != null) authzResolverImpl.addAdmin(sess, (User) complementaryObject, user);
						else authzResolverImpl.addAdmin(sess, (User) complementaryObject, authorizedGroup);
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.addVoRole(sess, Role.SPONSOR, (Vo) complementaryObject, user);
						else authzResolverImpl.addVoRole(sess, Role.SPONSOR, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for SponsoredUser: " + complementaryObject);
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
				} else if (role.equals(Role.VOOBSERVER)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't unset VoObserver rights without Vo this way.");
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.removeVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, user);
						else authzResolverImpl.removeVoRole(sess, Role.VOOBSERVER, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver: " + complementaryObject);
					}
				} else if (role.equals(Role.VOADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't unset VoAdmin rights without Vo this way.");
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.removeVoRole(sess, Role.VOADMIN,(Vo) complementaryObject, user);
						else authzResolverImpl.removeVoRole(sess, Role.VOADMIN,(Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.TOPGROUPCREATOR)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't set TopGroupCreator rights without Vo.");
					} else if (complementaryObject instanceof Vo) {
						if (user != null) authzResolverImpl.removeVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, user);
						else authzResolverImpl.removeVoRole(sess, Role.TOPGROUPCREATOR, (Vo) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver role: " + complementaryObject);
					}
				} else if (role.equals(Role.GROUPADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't unset GroupAdmin rights without Group this way.");
					} else if (complementaryObject instanceof Group) {
						if (user != null) authzResolverImpl.removeAdmin(sess, (Group) complementaryObject, user);
						else authzResolverImpl.removeAdmin(sess, (Group) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for GroupAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.FACILITYADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't unset FacilityAdmin rights without Facility this way.");
					} else if (complementaryObject instanceof Facility) {
						if (user != null) authzResolverImpl.removeAdmin(sess, (Facility) complementaryObject, user);
						else authzResolverImpl.removeAdmin(sess, (Facility) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for FacilityAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.RESOURCEADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't unset ResourceAdmin rights without Resource this way.");
					} else if (complementaryObject instanceof Resource) {
						if (user != null) authzResolverImpl.removeAdmin(sess, (Resource) complementaryObject, user);
						else authzResolverImpl.removeAdmin(sess, (Resource) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for ResourceAdmin: " + complementaryObject);
					}
				} else if (role.equals(Role.SECURITYADMIN)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't unset SecurityAdmin rights without Security this way.");
					} else if (complementaryObject instanceof SecurityTeam) {
						if (user != null) removeAdmin(sess, (SecurityTeam) complementaryObject, user);
						else removeAdmin(sess, (SecurityTeam) complementaryObject, authorizedGroup);
					} else {
						throw new InternalErrorException("Not supported complementary object for VoObserver: " + complementaryObject);
					}
				} else if (role.equals(Role.SPONSOR)) {
					if (complementaryObject == null) {
						throw new InternalErrorException("Not supported operation, can't unset Sponsor rights without User this way.");
					} else if (complementaryObject instanceof User) {
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
			init(sess);
		}

		return sess.getPerunPrincipal().getRoles().getRolesNames();
	}

	/**
	 * Returns user which is associated with credentials used to log-in to Perun.
	 *
	 * @param sess perun session
	 * @return currently logged user
	 */
	public static User getLoggedUser(PerunSession sess) throws UserNotExistsException, InternalErrorException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			init(sess);
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
	public static PerunPrincipal getPerunPrincipal(PerunSession sess) throws InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			init(sess);
		} else {
			refreshAuthz(sess);
		}

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
			if(!oauthScopes.contains(PerunClient.PERUN_ADMIN_SCOPE)) {
				log.debug("removing PERUNADMIN role from session {}",sess);
				sess.getPerunPrincipal().getRoles().remove(Role.PERUNADMIN);
			}
			if(!oauthScopes.contains(PerunClient.PERUN_API_SCOPE)) {
				log.warn("removing all roles from session {}",sess);
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
		log.debug("Refreshing session data for session {}.", sess);

		try {
			User user = perunBl.getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, sess.getPerunPrincipal().getExtSourceName(), sess.getPerunPrincipal().getActor());
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
	 */
	static boolean isUserInRoleForVo(PerunSession session, User user, Role role, Vo vo) {
		return authzResolverImpl.isUserInRoleForVo(session, user, role, vo);
	}

	/**
	 * Checks whether the group is in role for Vo.
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
	private static void prepareServiceRoles(PerunSession sess) throws InternalErrorException {
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
