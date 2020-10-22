package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunBeanNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.core.impl.Privileges;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.model.Application;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AuthzResolver {

	/**
	 * Checks if the principal is authorized.
	 * This method should be accessed through external components.
	 *
	 * @param sess PerunSession which contains the principal.
	 * @param policyDefinition of policy which contains authorization rules.
	 * @param objects as list of PerunBeans on which will be authorization provided. (e.g. groups, Vos, etc...)
	 * @return true if the principal has particular rights, false otherwise.
	 * @throws PolicyNotExistsException when the given policyDefinition does not exist in the PerunPoliciesContainer.
	 */
	public static boolean authorizedExternal(PerunSession sess, String policyDefinition, List<PerunBean> objects) throws PolicyNotExistsException {
		return AuthzResolverBlImpl.authorized(sess, policyDefinition, objects);
	}

	/**
	 * Checks if the principal is authorized.
	 * This method should be used in the internal code.
	 *
	 * @param sess PerunSession which contains the principal.
	 * @param policyDefinition of policy which contains authorization rules.
	 * @param objects as list of PerunBeans on which will be authorization provided. (e.g. groups, Vos, etc...)
	 * @return true if the principal has particular rights, false otherwise.
	 */
	public static boolean authorizedInternal(PerunSession sess, String policyDefinition, List<PerunBean> objects) {
		try {
			return AuthzResolverBlImpl.authorized(sess, policyDefinition, objects);
		} catch (PolicyNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Checks if the principal is authorized.
	 * This method should be used in the internal code.
	 *
	 * @param sess PerunSession which contains the principal.
	 * @param policyDefinition of policy which contains authorization rules.
	 * @param objects an array of PerunBeans on which will be authorization provided. (e.g. groups, Vos, etc...)
	 * @return true if the principal has particular rights, false otherwise.
	 */
	public static boolean authorizedInternal(PerunSession sess, String policyDefinition, PerunBean... objects) {
		try {
			return AuthzResolverBlImpl.authorized(sess, policyDefinition, Arrays.asList(objects));
		} catch (PolicyNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Checks if the principal is authorized.
	 * Used when there are no PerunBeans needed for authorization.
	 * This method should be used in the internal code.
	 *
	 * @param sess PerunSession which contains the principal.
	 * @param policyDefinition of policy which contains authorization rules.
	 * @return true if the principal has particular rights, false otherwise.
	 */
	public static boolean authorizedInternal(PerunSession sess, String policyDefinition) {
		try {
			return AuthzResolverBlImpl.authorized(sess, policyDefinition, Collections.emptyList());
		} catch (PolicyNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Check if the principal is the owner of the application.
	 *
	 * @param sess PerunSession which contains the principal.
	 * @param app application which principal wants to access
	 * @return true if the principal has particular rights, false otherwise.
	 */
	public static boolean selfAuthorizedForApplication(PerunSession sess, Application app) {
		return AuthzResolverBlImpl.selfAuthorizedForApplication(sess, app);
	}

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess perunSession
	 * @param role required role
	 * @param complementaryObject object which specifies particular action of the role (e.g. group)
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	@Deprecated
	public static boolean isAuthorized(PerunSession sess, String role, PerunBean complementaryObject) {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return AuthzResolverBlImpl.isAuthorized(sess, role, complementaryObject);
	}

	/**
	 * Checks if the principal is authorized to do some action of group-resource attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param group primary Bean of Attribute (can't be null)
	 * @param resource secondary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Group group, Resource resource) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, group, resource);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of resource-member attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param resource primary Bean of Attribute (can't be null)
	 * @param member secondary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member, Resource resource) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, member, resource);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of user-facility attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param user primary Bean of Attribute (can't be null)
	 * @param facility secondary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, User user, Facility facility) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, user, facility);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of member-group attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param member primary Bean of Attribute (can't be null)
	 * @param group secondary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member, Group group) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, member, group);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of user attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param user primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, User user) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, user);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of member attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param member primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Member member) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, member);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of vo attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param vo primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Vo vo) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, vo);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of PerunBean attribute.
	 *
	 * @param sess session
	 * @param actionType action type
	 * @param attrDef attr def
	 * @param bean bean
	 * @return true, if principal is authorized for attribute and action
	 */
	@SuppressWarnings("unused")
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, PerunBean bean) {
		if (bean instanceof Vo) return isAuthorizedForAttribute(sess, actionType, attrDef, (Vo)bean);
		if (bean instanceof User) return isAuthorizedForAttribute(sess, actionType, attrDef, (User)bean);
		if (bean instanceof Member) return isAuthorizedForAttribute(sess, actionType, attrDef, (Member)bean);
		if (bean instanceof Group) return isAuthorizedForAttribute(sess, actionType, attrDef, (Group)bean);
		if (bean instanceof Resource) return isAuthorizedForAttribute(sess, actionType, attrDef, (Resource)bean);
		if (bean instanceof Facility) return isAuthorizedForAttribute(sess, actionType, attrDef, (Facility)bean);
		if (bean instanceof Host) return isAuthorizedForAttribute(sess, actionType, attrDef, (Host)bean);
		if (bean instanceof UserExtSource) return isAuthorizedForAttribute(sess, actionType, attrDef, (UserExtSource)bean);
		throw new UnsupportedOperationException(
			"method - isAuthorizedForAttribute - called with unsupported PerunBean type - " + bean.getBeanName());
	}

	/**
	 * Checks if the principal is authorized to do some action of group attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param group primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Group group) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, group);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of resource attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param resource primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Resource resource) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, resource);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of facility attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param facility primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Facility facility) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, facility);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of host attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param host primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Host host) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, host);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of ues attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param ues primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, UserExtSource ues) {
		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, ues);
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of entityless attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param key primary Bean of Attribute (can't be null)
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, String key) {
		return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, key);
	}

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess perun session
	 * @param role required role
	 *
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	@Deprecated
	public static boolean isAuthorized(PerunSession sess, String role) {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return AuthzResolverBlImpl.isAuthorized(sess, role);
	}

	/**
	 * Returns true if the perun principal inside the perun session is vo admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is vo admin
	 */
	public static boolean isVoAdmin(PerunSession sess) {
		return AuthzResolverBlImpl.isVoAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is group admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is group admin.
	 */
	public static boolean isGroupAdmin(PerunSession sess) {
		return AuthzResolverBlImpl.isGroupAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is facility admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is facility admin.
	 */
	public static boolean isFacilityAdmin(PerunSession sess) {
		return AuthzResolverBlImpl.isFacilityAdmin(sess);
	}

//	/**
//	 * Returns true if the perun principal inside the perun session is resource admin.
//	 *
//	 * @param sess perun session
//	 * @return true if the perun principal is resource admin.
//	 */
//	public static boolean isResourceAdmin(PerunSession sess) {
//		return AuthzResolverBlImpl.isResourceAdmin(sess);
//	}
//
//	/**
//	 * Returns true if the perun principal inside the perun session is security admin.
//	 *
//	 * @param sess perun session
//	 * @return true if the perun principal is security admin.
//	 */
//	public static boolean isSecurityAdmin(PerunSession sess) {
//		return AuthzResolverBlImpl.isSecurityAdmin(sess);
//	}
//
//	/**
//	 * Returns true if the perun principal inside the perun session is vo observer.
//	 *
//	 * @param sess perun session
//	 * @return true if the perun principal is vo observer
//	 */
//	public static boolean isVoObserver(PerunSession sess) {
//		return AuthzResolverBlImpl.isVoObserver(sess);
//	}
//
//	/**
//	 * Returns true if the perun principal inside the perun session is top group creator.
//	 *
//	 * @param sess perun session
//	 * @return true if the perun principal is top group creator.
//	 */
//	public static boolean isTopGroupCreator(PerunSession sess) {
//		return AuthzResolverBlImpl.isTopGroupCreator(sess);
//	}

	/**
	 * Returns true if the perun principal inside the perun session is perun admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is perun admin.
	 */
	public static boolean isPerunAdmin(PerunSession sess) {
		return AuthzResolverBlImpl.isPerunAdmin(sess);
	}

	/**
	 * Get all principal role names.
	 *
	 * @param sess perun session
	 * @return list of strings, which represents roles.
	 */
	public static List<String> getPrincipalRoleNames(PerunSession sess) {
		return AuthzResolverBlImpl.getPrincipalRoleNames(sess);
	}

	/**
	 * Get all user role names.
	 *
	 * @param sess perun session
	 * @param user User
	 * @return list of strings, which represents roles.
	 */
	public static List<String> getUserRoleNames(PerunSession sess, User user) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);

		//Authorization
		if (!authorizedInternal(sess, "getUserRoleNames_User_policy"))
			throw new PrivilegeException("getUserRoleNames.");

		return AuthzResolverBlImpl.getUserRoleNames(sess, user);
	}

	/**
	 * Get all roles for a given user.
	 *
	 * @param sess perun session
	 * @param userId id of a user
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @return AuthzRoles object which contains all roles with perunbeans
	 */
	public static AuthzRoles getUserRoles(PerunSession sess, int userId) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		User user = ((PerunBl) sess.getPerun()).getUsersManagerBl().getUserById(sess, userId);

		//Authorization
		if (!authorizedInternal(sess, "getUserRoles_int_policy"))
			throw new PrivilegeException("getUserRoles.");

		return AuthzResolverBlImpl.getUserRoles(sess, user);
	}

	/**
	 * Get all group role names.
	 *
	 * @param sess perun session
	 * @param group Group
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @return list of strings, which represents roles.
	 */
	public static List<String> getGroupRoleNames(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, group);

		//Authorization
		if (!authorizedInternal(sess, "getGroupRoleNames_Group_policy"))
			throw new PrivilegeException("getGroupRoleNames.");

		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getGroupRoleNames(sess, group);
	}

	/**
	 * Get all roles for a given group.
	 *
	 * @param sess perun session
	 * @param groupId id of a group
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @return AuthzRoles object which contains all roles with perunbeans
	 */
	public static AuthzRoles getGroupRoles(PerunSession sess, int groupId) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Group group = ((PerunBl) sess.getPerun()).getGroupsManagerBl().getGroupById(sess, groupId);

		//Authorization
		if (!authorizedInternal(sess, "getGroupRoles_int_policy"))
			throw new PrivilegeException("getGroupRoles.");

		return AuthzResolverBlImpl.getGroupRoles(sess, group);
	}

	/**
	 * Returns user which is associated with credentials used to log-in to Perun.
	 *
	 * @param sess perun session
	 * @return currently logged user
	 */
	public static User getLoggedUser(PerunSession sess) {
		return AuthzResolverBlImpl.getLoggedUser(sess);
	}

	/**
	 * Returns true if the perunPrincipal has requested role.
	 *
	 * @param perunPrincipal acting person for whom the role is checked
	 * @param role role to be checked
	 */
	public static boolean hasRole(PerunPrincipal perunPrincipal, String role) {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return AuthzResolverBlImpl.hasRole(perunPrincipal, role);
	}

	/**
	 * Check if principal is allowed to manage the given role to the given object.
	 *
	 * @param sess session
	 * @param complementaryObject complementary object
	 * @param role role
	 * @return true, if the current principal can unset the given role for the given object, false otherwise
	 * @throws InternalErrorException internal error
	 */
	public static boolean isAuthorizedToManageRole(PerunSession sess, PerunBean complementaryObject, String role) {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return hasOneOfTheRolesForObject(sess, complementaryObject, Privileges.getRolesWhichCanManageRole(role));
	}

	/**
	 * Check wheter the principal is authorized to manage the role on the object.
	 *
	 * @param sess principal's perun session
	 * @param complementaryObject bounded with the role
	 * @param role which will be managed
	 * @return
	 * @throws RoleManagementRulesNotExistsException when the role does not have the management rules.
	 */
	public static boolean authorizedToManageRole(PerunSession sess, PerunBean complementaryObject, String role) throws RoleManagementRulesNotExistsException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return AuthzResolverBlImpl.authorizedToManageRole(sess, complementaryObject, role);
	}

	/**
	 * Set role for user and <b>all</b> complementary objects.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role of user in a session
	 * @param complementaryObjects objects for which role will be set
	 */
	public static void setRole(PerunSession sess, User user, String role, List<PerunBean> complementaryObjects) throws PrivilegeException, UserNotExistsException, AlreadyAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		for (PerunBean complementaryObject : complementaryObjects) {
			setRole(sess, user, complementaryObject, role);
		}
	}

	/**
	 * Set role for user and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, User user, PerunBean complementaryObject, String role) throws PrivilegeException, UserNotExistsException, AlreadyAdminException, RoleCannotBeManagedException {
		Utils.notNull(role, "role");

		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);
		try {
			if(!authorizedToManageRole(sess, complementaryObject, role)) {
				throw new PrivilegeException("You are not privileged to use the method setRole.");
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		AuthzResolverBlImpl.setRole(sess, user,complementaryObject, role);
	}

	/**
	 * Set role for auhtorizedGroup and <b>all</b> complementary objects.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for setting role
	 * @param role role of user in a session
	 * @param complementaryObjects objects for which role will be set
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, String role, List<PerunBean> complementaryObjects) throws PrivilegeException, GroupNotExistsException, AlreadyAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		for (PerunBean complementaryObject : complementaryObjects) {
			setRole(sess, authorizedGroup, complementaryObject, role);
		}
	}

	/**
	 * Set role for authorizedGroup and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for setting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, String role) throws PrivilegeException, GroupNotExistsException, AlreadyAdminException, RoleCannotBeManagedException {
		Utils.notNull(role, "role");
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: " + role + " does not exists.");
		}
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		try {
			if(!authorizedToManageRole(sess, complementaryObject, role)) {
				throw new PrivilegeException("You are not privileged to use the method setRole.");
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		AuthzResolverBlImpl.setRole(sess, authorizedGroup, complementaryObject, role);
	}

	/**
	 * Set role for authorizedGroups and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param authorizedGroups the groups for setting role
	 * @param complementaryObject object for which the role will be set
	 * @param role desired role
	 * @throws GroupNotExistsException if the any of the group don't exist
	 * @throws PrivilegeException insufficient permissions
	 * @throws AlreadyAdminException if any of the given groups is already admin
	 * @throws InternalErrorException internal error
	 */
	public static void setRole(PerunSession sess, List<Group> authorizedGroups, PerunBean complementaryObject, String role) throws GroupNotExistsException, PrivilegeException, AlreadyAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		for (Group authorizedGroup : authorizedGroups) {
			setRole(sess, authorizedGroup, complementaryObject, role);
		}
	}

	/**
	 * Set role for given users and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param users users for which the given role is set
	 * @param role desired role
	 * @param complementaryObject object for which the role is set
	 * @throws UserNotExistsException if any of the given users is not found
	 * @throws PrivilegeException insufficient permissions
	 * @throws AlreadyAdminException if any of the given users is already admin
	 * @throws InternalErrorException internal error
	 */
	public static void setRole(PerunSession sess, List<User> users, String role, PerunBean complementaryObject) throws UserNotExistsException, PrivilegeException, AlreadyAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		for (User user : users) {
			setRole(sess, user, complementaryObject, role);
		}
	}

	/**
	 * Set role for authorizedGroups and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param authorizedGroups the groups for setting role
	 * @param complementaryObject object for which the role will be set
	 * @param role desired role
	 * @throws GroupNotExistsException if the any of the group don't exist
	 * @throws PrivilegeException insufficient permissions
	 * @throws GroupNotAdminException if any of the given groups is not admin
	 * @throws InternalErrorException internal error
	 */
	public static void unsetRole(PerunSession sess, List<Group> authorizedGroups, PerunBean complementaryObject, String role) throws GroupNotExistsException, PrivilegeException, GroupNotAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		for (Group authorizedGroup : authorizedGroups) {
			unsetRole(sess, authorizedGroup, complementaryObject, role);
		}
	}

	/**
	 * Set role for given users and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param users users for which the given role is set
	 * @param role desired role
	 * @param complementaryObject object for which the role is set
	 * @throws UserNotExistsException if any of the given users is not found
	 * @throws PrivilegeException insufficient permissions
	 * @throws UserNotAdminException if any of the given users is not admin
	 * @throws InternalErrorException internal error
	 */
	public static void unsetRole(PerunSession sess, List<User> users, String role, PerunBean complementaryObject) throws UserNotExistsException, PrivilegeException, UserNotAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		for (User user : users) {
			unsetRole(sess, user, complementaryObject, role);
		}
	}

	/**
	 * Unset role for user and <b>all</b> complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param user the user for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObjects objects for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, User user, String role, List<PerunBean> complementaryObjects) throws PrivilegeException, UserNotExistsException, UserNotAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		for (PerunBean complementaryObject : complementaryObjects) {
			unsetRole(sess, user, complementaryObject, role);
		}
	}

	/**
	 * Unset role for user and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param user the user for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, User user, PerunBean complementaryObject, String role) throws PrivilegeException, UserNotExistsException, UserNotAdminException, RoleCannotBeManagedException {
		Utils.notNull(role, "role");
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);

		try {
			if(!authorizedToManageRole(sess, complementaryObject, role)) {
				throw new PrivilegeException("You are not privileged to use the method unsetRole.");
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		AuthzResolverBlImpl.unsetRole(sess, user, complementaryObject, role);
	}

	/**
	 * Unset role for group and <b>all</b> complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObjects objects for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, String role, List<PerunBean> complementaryObjects) throws PrivilegeException, GroupNotExistsException, GroupNotAdminException, RoleCannotBeManagedException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		for (PerunBean complementaryObject : complementaryObjects) {
			unsetRole(sess, authorizedGroup, complementaryObject, role);
		}
	}

	/**
	 * Unset role for group and <b>one</b> complementary object
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "PERUNADMIN" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, String role) throws PrivilegeException, GroupNotExistsException, GroupNotAdminException, RoleCannotBeManagedException {
		Utils.notNull(role, "role");
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		try {
			if(!authorizedToManageRole(sess, complementaryObject, role)) {
				throw new PrivilegeException("You are not privileged to use the method unsetRole.");
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, complementaryObject, role);
	}

	/**
	 * Get all richUser administrators for complementary object and role with specified attributes.
	 *
	 * If <b>onlyDirectAdmins</b> is <b>true</b>, return only direct users of the complementary object for role with specific attributes.
	 * If <b>allUserAttributes</b> is <b>true</b>, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param sess perun session
	 * @param complementaryObjectId id of object for which we will get richUser administrators
	 * @param complementaryObjectName name of object for which we will get richUser administrators
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param role expected role to filter managers by (PERUNADMIN | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | RESOURCEADMIN)
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 *
	 * @return list of richUser administrators for complementary object and role with specified attributes.
	 */
	public static List<RichUser> getRichAdmins(PerunSession sess, int complementaryObjectId, String complementaryObjectName, List<String> specificAttributes, String role, boolean onlyDirectAdmins, boolean allUserAttributes) throws PrivilegeException, GroupNotExistsException, VoNotExistsException, FacilityNotExistsException, RoleNotSupportedException, PerunBeanNotSupportedException, UserNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjectName, "complementaryObjectName");
		if(!allUserAttributes) Utils.notNull(specificAttributes, "specificAttributes");

		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		List<RichUser> richUsers;
		//Try to get complementary Object
		switch (complementaryObjectName) {
			case "Group":
				if (!role.equals(Role.GROUPADMIN))
					throw new RoleNotSupportedException("Not supported other role than group manager for object Group.");
				Group group = ((PerunBl) sess.getPerun()).getGroupsManagerBl().getGroupById(sess, complementaryObjectId);
				richUsers = sess.getPerun().getGroupsManager().getRichAdmins(sess, group, specificAttributes, allUserAttributes, onlyDirectAdmins);
				break;
			case "Vo":
				Vo vo = ((PerunBl) sess.getPerun()).getVosManagerBl().getVoById(sess, complementaryObjectId);
				richUsers = sess.getPerun().getVosManager().getRichAdmins(sess, vo, role, specificAttributes, allUserAttributes, onlyDirectAdmins);
				break;
			case "Facility":
				if (!role.equals(Role.FACILITYADMIN))
					throw new RoleNotSupportedException("Not supported other role than facility manager for object Facility.");
				Facility facility = ((PerunBl) sess.getPerun()).getFacilitiesManagerBl().getFacilityById(sess, complementaryObjectId);
				richUsers = sess.getPerun().getFacilitiesManager().getRichAdmins(sess, facility, specificAttributes, allUserAttributes, onlyDirectAdmins);
				break;
			case "Resource":
				Resource resource = ((PerunBl) sess.getPerun()).getResourcesManagerBl().getResourceById(sess, complementaryObjectId);
				if (!Role.RESOURCEADMIN.equals(role)) {
					throw new RoleNotSupportedException("Not supported other role than resource manager for object Resource.");
				}
				richUsers = sess.getPerun().getResourcesManager().getRichAdmins(sess, resource, specificAttributes, allUserAttributes, onlyDirectAdmins);
				break;
			default:
				throw new PerunBeanNotSupportedException("Only Vo, Group and Facility are supported complementary names.");
		}

		return richUsers;
	}

	/**
	 * Get all authorizedGroups for complementary object and role.
	 *
	 * @param sess perun session
	 * @param complementaryObjectId id of object for which we will get richUser administrators
	 * @param complementaryObjectName name of object for which we will get richUser administrators
	 * @param role expected role to filter authorizedGroups by (PERUNADMIN | VOADMIN | GROUPADMIN | SPONSOR | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | RESOURCEADMIN)
	 *
	 * @return list of authorizedGroups for complementary object and role
	 */
	public static List<Group> getAdminGroups(PerunSession sess, int complementaryObjectId, String complementaryObjectName, String role) throws PrivilegeException, GroupNotExistsException, VoNotExistsException, FacilityNotExistsException, RoleNotSupportedException, PerunBeanNotSupportedException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjectName, "complementaryObjectName");

		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}

		List<Group> authorizedGroups;
		//Try to get complementary Object
		switch (complementaryObjectName) {
			case "Group":
				if (!role.equals(Role.GROUPADMIN))
					throw new RoleNotSupportedException("Not supported other role than group manager for object Group.");
				Group group = ((PerunBl) sess.getPerun()).getGroupsManagerBl().getGroupById(sess, complementaryObjectId);
				authorizedGroups = sess.getPerun().getGroupsManager().getAdminGroups(sess, group);
				break;
			case "Vo":
				Vo vo = ((PerunBl) sess.getPerun()).getVosManagerBl().getVoById(sess, complementaryObjectId);
				authorizedGroups = sess.getPerun().getVosManager().getAdminGroups(sess, vo, role);
				break;
			case "Facility":
				if (!role.equals(Role.FACILITYADMIN))
					throw new RoleNotSupportedException("Not supported other role than facility manager for object Facility.");
				Facility facility = ((PerunBl) sess.getPerun()).getFacilitiesManagerBl().getFacilityById(sess, complementaryObjectId);
				authorizedGroups = sess.getPerun().getFacilitiesManager().getAdminGroups(sess, facility);
				break;
			case "Resource":
				Resource resource = ((PerunBl) sess.getPerun()).getResourcesManagerBl().getResourceById(sess, complementaryObjectId);
				if (!Role.RESOURCEADMIN.equals(role)) {
					throw new RoleNotSupportedException("Not supported other role than resource manager for object Resource.");
				}
				authorizedGroups = sess.getPerun().getResourcesManager().getAdminGroups(sess, resource);
				break;
			default:
				throw new PerunBeanNotSupportedException("Only Vo, Group and Facility are supported complementary names.");
		}

		return authorizedGroups;
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
		return AuthzResolverBlImpl.getPerunPrincipal(sess);
	}

	/**
	 * Returns all complementary objects for defined role.
	 *
	 * @param sess perun session
	 * @param role to get object for
	 * @return list of complementary objects
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, String role) {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role);
	}

	/**
	 * Returns complementary objects for defined role filtered by particular class, e.g. Vo, Group, ...
	 *
	 * @param sess perun session
	 * @param role to get object for
	 * @param perunBeanClass particular class ( Vo | Group | ... )
	 * @return list of complementary objects
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, String role, Class perunBeanClass) {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role, perunBeanClass);
	}

	/**
	 * Removes all existing roles for the perunPrincipal and call init again.
	 *
	 * @param sess perun session
	 */
	public static void refreshAuthz(PerunSession sess) {
		AuthzResolverBlImpl.refreshAuthz(sess);
	}

	/**
	 * This methods verifies if the current principal has one of the given roles for the given object.
	 *
	 * @param sess session
	 * @param complementaryObject complementary object
	 * @param allowedRoles set of roles which are tested
	 * @return true, if the principal is authorized, false otherwise
	 * @throws InternalErrorException internal error
	 */
	public static boolean hasOneOfTheRolesForObject(PerunSession sess, PerunBean complementaryObject, Set<String> allowedRoles) {
		if (allowedRoles == null) {
			throw new InternalErrorException("Unsupported role.");
		}
		for (String allowedRole : allowedRoles) {
			if (!roleExists(allowedRole)) {
				throw new InternalErrorException("Role: "+ allowedRole +" does not exists.");
			}
			if (isAuthorized(sess, allowedRole, complementaryObject)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the given role exists in the database.
	 * Check is case insensitive.
	 *
	 * @param role which will be checked
	 * @return true if role exists, false otherwise.
	 */
	public static boolean roleExists(String role) {
		return AuthzResolverBlImpl.roleExists(role);
	}

	/**
	 * Load perun roles and policies from the configuration file perun-roles.yml.
	 * Roles are loaded to the database and policies are loaded to the PerunPoliciesContainer.
	 *
	 * @throws PrivilegeException when the principal is not authorized.
	 */
	public static void loadAuthorizationComponents(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		//Authorization
		if (!authorizedInternal(sess, "loadAuthorizationComponents_policy"))
			throw new PrivilegeException(sess, "loadAuthorizationComponents");

		AuthzResolverBlImpl.loadAuthorizationComponents();
	}

	/**
	 * Return all loaded perun policies.
	 *
	 * @return all loaded policies
	 */
	public static List<PerunPolicy> getAllPolicies() {
		return AuthzResolverBlImpl.getAllPolicies();
	}
}
