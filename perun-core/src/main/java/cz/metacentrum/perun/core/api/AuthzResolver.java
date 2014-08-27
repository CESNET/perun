package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import java.util.ArrayList;

public class AuthzResolver {

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess perunSession
	 * @param role required role
	 * @param complementaryObject object which specifies particular action of the role (e.g. group)
	 *
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	public static boolean isAuthorized(PerunSession sess, Role role, PerunBean complementaryObject) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isAuthorized(sess, role, complementaryObject);
	}

	/**
	 * Checks if the principal is authorized to do some "action" on "attribute"
	 * - for "primary" holder
	 * - or "primary and secondary" holder if secondary holder is not null.
	 *
	 *
	 * @param sess
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @param primaryHolder primary Bean of Attribute (can't be null)
	 * @param secondaryHolder secondary Bean of Attribute (can be null)
	 * @return true if principal is authorized, false if not
	 * @throws InternalErrorException
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Object primaryHolder, Object secondaryHolder) throws InternalErrorException {
		try {
			return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, primaryHolder, secondaryHolder);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		} catch (ActionTypeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public static List<Role> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getRolesWhichCanWorkWithAttribute(sess, actionType, attrDef);
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
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isAuthorized(sess, role);
	}

	/**
	 * Returns true if the perun principal inside the perun session is vo admin.
	 *
	 * @param sess
	 * @return true if the perun principal is vo admin
	 */
	public static boolean isVoAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isVoAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is group admin.
	 *
	 * @param sess
	 * @return true if the perun principal is group admin.
	 */
	public static boolean isGroupAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isGroupAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is facility admin.
	 *
	 * @param sess
	 * @return true if the perun principal is facility admin.
	 */
	public static boolean isFacilityAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isFacilityAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is perun admin.
	 *
	 * @param sess
	 * @return true if the perun principal is perun admin.
	 */
	public static boolean isPerunAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isPerunAdmin(sess);
	}

	/**
	 * Get all principal role names. Role is defined as a name, translation table is in Role class.
	 *
	 * @param sess
	 * @throws InternalErrorException
	 * @return list of integers, which represents role from enum Role.
	 */
	public static List<String> getPrincipalRoleNames(PerunSession sess) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getPrincipalRoleNames(sess);
	}

	/**
	 * Get currenty logged user
	 *
	 * @param sess
	 * @return currenty logged user
	 * @throws UserNotExistsException
	 * @throws InternalErrorException
	 */
	public static User getLoggedUser(PerunSession sess) throws UserNotExistsException, InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getLoggedUser(sess);
	}

	/**
	 * Returns true if the perunPrincipal has requested role.
	 *
	 * @param perunPrincipal
	 * @param role role to be checked
	 */
	public static boolean hasRole(PerunPrincipal perunPrincipal, Role role) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.hasRole(perunPrincipal, role);
	}

	/**
	 * Set role for user and all complementary objects
	 * If list of complementary objects is empty, set general role instead (for no concrete objects)
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role 
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws AlreadyAdminException
	 * @throws GroupNotAdminException
	 * @throws UserNotAdminException
	 */
	public static void setRole(PerunSession sess, User user, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, PrivilegeException, UserNotExistsException, AlreadyAdminException, GroupNotAdminException, UserNotAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjects, "complementaryObjects");
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);
		
		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Set role for user and one complementary object.
	 * If complementary object is null, set general role instead (for no concrete objects)
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role
	 * @param complementaryObject object for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, User user,PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, UserNotExistsException, AlreadyAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObject, "complementaryObject");
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, user,complementaryObject, role);
	}

	/**
	 * Set role for auhtorizedGroup and all complementary objects
	 * If list of complementary objects is empty, set general role instead (for no concrete objects)
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for setting role
	 * @param role role
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AlreadyAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjects, "complementaryObjects");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Set role for authorizedGroup and one complementary object.
	 * If complementary object is null, set general role instead (for no concrete object).
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for setting role
	 * @param role role
	 * @param complementaryObject object for which role will be set
	 * @param refreshAuthzInSession refresh authz in session if true, not if false
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, GroupNotExistsException, AlreadyAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObject, "complementaryObject");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, authorizedGroup, complementaryObject, role);
	}

	/**
	 * Unset role for user and all complementary objects
	 * If list of complementary objects is empty, remove general role isntead (role without concrete objects)
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws UserNotAdminException
	 */
	public static void unsetRole(PerunSession sess, User user, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, PrivilegeException, UserNotExistsException, UserNotAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjects, "complementaryObjects");
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);
		
		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method unsetRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Unset role for user and one complementary object.
	 * If complementary object is empty, remove general role instead (role without concrete objects)
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role
	 * @param complementaryObject object for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws UserNotAdminException
	 */
	public static void unsetRole(PerunSession sess, User user, PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, UserNotExistsException, UserNotAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObject, "complementaryObject");
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method unsetRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, user, complementaryObject, role);
	}

	/**
	 * Unset role for group and all complementary objects
	 * If list of complementary objects is empty, set general role instead (for no concrete objects)
	 *
	 * @param sess perun session
	 * @param group the group for setting role
	 * @param role role
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws GroupNotAdminException
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, PrivilegeException, GroupNotExistsException, GroupNotAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjects, "complementaryObjects");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Unset role for group and one complementary object
	 * If complementary object is null, set general role instead (for no concrete object)
	 *
	 * @param sess perun session
	 * @param group the group for setting role
	 * @param role role
	 * @param complementaryObject object for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws GroupNotAdminException
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, GroupNotExistsException, GroupNotAdminException {
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObject, "complementaryObject");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, complementaryObject, role);
	}

	/**
	 * Get the PerunPrincipal from the session.
	 *
	 * @param sess
	 * @return perunPrincipal
	 * @throws InternalErrorException if the PerunSession is not valid.
	 */
	@Deprecated
	public static PerunPrincipal getPerunPrincipal(PerunSession sess) throws InternalErrorException, UserNotExistsException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getPerunPrincipal(sess);
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
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role);
	}

	/**
	 * Returns complementary objects for defined role filtered by particular class, e.g. Vo, Group, ...
	 *
	 * @param sess
	 * @param role
	 * @return list of complementary objects
	 * @throws InternalErrorException
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role, Class perunBeanClass) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role, perunBeanClass);
	}

	/**
	 * Removes all existing roles for the perunPrincipal and call init again.
	 *
	 * @param sess
	 * @throws InternalErrorException
	 */
	public static void refreshAuthz(PerunSession sess) throws InternalErrorException {
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.refreshAuthz(sess);
	}
}
