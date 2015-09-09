package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunBeanNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;

public class AuthzResolver {

	/**
	 * Checks if the principal is authorized.
	 *
	 * @param sess perunSession
	 * @param role required role
	 * @param complementaryObject object which specifies particular action of the role (e.g. group)
	 * @return true if the principal authorized, false otherwise
	 * @throws InternalErrorException if something goes wrong
	 */
	public static boolean isAuthorized(PerunSession sess, Role role, PerunBean complementaryObject) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isAuthorized(sess, role, complementaryObject);
	}

	/**
	 * Checks if the principal is authorized to do some "action" on "attribute".
	 * - for "primary" holder
	 * - or "primary and secondary" holder if secondary holder is not null.
	 *
	 * @param sess perun session
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

	/**
	 * Return list of roles which are authorized for doing "action" on "attribute".
	 * 
	 * @param sess perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef attribute what principal want to work with
	 * @return list of roles
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws ActionTypeNotExistsException
	 */
	public static List<Role> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getRolesWhichCanWorkWithAttribute(sess, actionType, attrDef);
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
	public static boolean isAuthorized(PerunSession sess, Role role) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isAuthorized(sess, role);
	}

	/**
	 * Returns true if the perun principal inside the perun session is vo admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is vo admin
	 */
	public static boolean isVoAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isVoAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is group admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is group admin.
	 */
	public static boolean isGroupAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isGroupAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is facility admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is facility admin.
	 */
	public static boolean isFacilityAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isFacilityAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is security admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is security admin.
	 */
	public static boolean isSecurityAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isSecurityAdmin(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is vo observer.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is vo observer
	 */
	public static boolean isVoObserver(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isVoObserver(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is top group creator.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is top group creator.
	 */
	public static boolean isTopGroupCreator(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isTopGroupCreator(sess);
	}

	/**
	 * Returns true if the perun principal inside the perun session is perun admin.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is perun admin.
	 */
	public static boolean isPerunAdmin(PerunSession sess) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.isPerunAdmin(sess);
	}

	/**
	 * Get all principal role names. Role is defined as a name, translation table is in Role class.
	 *
	 * @param sess perun session
	 * @throws InternalErrorException
	 * @return list of integers, which represents role from enum Role.
	 */
	public static List<String> getPrincipalRoleNames(PerunSession sess) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getPrincipalRoleNames(sess);
	}

	/**
	 * Returns user which is associated with credentials used to log-in to Perun.
	 *
	 * @param sess perun session
	 * @return currently logged user
	 * @throws UserNotExistsException
	 * @throws InternalErrorException
	 */
	public static User getLoggedUser(PerunSession sess) throws UserNotExistsException, InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getLoggedUser(sess);
	}

	/**
	 * Returns true if the perunPrincipal has requested role.
	 *
	 * @param perunPrincipal acting person for whom the role is checked
	 * @param role role to be checked
	 */
	public static boolean hasRole(PerunPrincipal perunPrincipal, Role role) {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.hasRole(perunPrincipal, role);
	}

	/**
	 * Set role for user and <b>all</b> complementary objects.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role of user in a session
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
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);
		
		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Set role for user and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param user the user for setting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, User user, PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, UserNotExistsException, AlreadyAdminException {
		Utils.notNull(role, "role");
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, user,complementaryObject, role);
	}

	/**
	 * Set role for auhtorizedGroup and <b>all</b> complementary objects.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for setting role
	 * @param role role of user in a session
	 * @param complementaryObjects objects for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AlreadyAdminException {
		Utils.notNull(role, "role");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Set role for authorizedGroup and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for setting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be set
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws AlreadyAdminException
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, GroupNotExistsException, AlreadyAdminException {
		Utils.notNull(role, "role");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.setRole(sess, authorizedGroup, complementaryObject, role);
	}

	/**
	 * Unset role for user and <b>all</b> complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param user the user for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObjects objects for which role will be unset
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws UserNotAdminException
	 */
	public static void unsetRole(PerunSession sess, User user, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, PrivilegeException, UserNotExistsException, UserNotAdminException {
		Utils.notNull(role, "role");
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);
		
		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method unsetRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, user, role, complementaryObjects);
	}

	/**
	 * Unset role for user and <b>one</b> complementary object.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param user the user for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be unset
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws UserNotAdminException
	 */
	public static void unsetRole(PerunSession sess, User user, PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, UserNotExistsException, UserNotAdminException {
		Utils.notNull(role, "role");
		((PerunBl) sess.getPerun()).getUsersManagerBl().checkUserExists(sess, user);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method unsetRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, user, complementaryObject, role);
	}

	/**
	 * Unset role for group and <b>all</b> complementary objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary objects.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObjects objects for which role will be unset
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws GroupNotAdminException
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, Role role, List<PerunBean> complementaryObjects) throws InternalErrorException, PrivilegeException, GroupNotExistsException, GroupNotAdminException {
		Utils.notNull(role, "role");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, role, complementaryObjects);
	}

	/**
	 * Unset role for group and <b>one</b> complementary object
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * @param sess perun session
	 * @param authorizedGroup the group for unsetting role
	 * @param role role of user in a session
	 * @param complementaryObject object for which role will be unset
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws GroupNotAdminException
	 */
	public static void unsetRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, Role role) throws  InternalErrorException, PrivilegeException, GroupNotExistsException, GroupNotAdminException {
		Utils.notNull(role, "role");
		((PerunBl) sess.getPerun()).getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		if(!isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("You are not privileged to use this method setRole.");
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, complementaryObject, role);
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
	 * @param role expected role to filter managers by
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 *
	 * @return list of richUser administrators for complementary object and role with specified attributes.
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws VoNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws RoleNotSupportedException
	 * @throws PerunBeanNotSupportedException
	 * @throws UserNotExistsException
	 */
	public static List<RichUser> getRichAdmins(PerunSession sess, int complementaryObjectId, String complementaryObjectName, List<String> specificAttributes, Role role, boolean onlyDirectAdmins, boolean allUserAttributes) throws  InternalErrorException, PrivilegeException, GroupNotExistsException, VoNotExistsException, FacilityNotExistsException, RoleNotSupportedException, PerunBeanNotSupportedException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjectName, "complementaryObjectName");
		if(!allUserAttributes) Utils.notNull(specificAttributes, "specificAttributes");

		List<RichUser> richUsers;
		//Try to get complementary Object
		if(complementaryObjectName.equals("Group")) {
			if(!role.equals(Role.GROUPADMIN)) throw new RoleNotSupportedException("Not supported other role than group manager for object Group.");
			Group group = ((PerunBl) sess.getPerun()).getGroupsManagerBl().getGroupById(sess, complementaryObjectId);
			richUsers = sess.getPerun().getGroupsManager().getRichAdmins(sess, group, specificAttributes, allUserAttributes, onlyDirectAdmins);
		} else if (complementaryObjectName.equals("Vo")) {
			Vo vo = ((PerunBl) sess.getPerun()).getVosManagerBl().getVoById(sess, complementaryObjectId);
			richUsers = sess.getPerun().getVosManager().getRichAdmins(sess, vo, role, specificAttributes, allUserAttributes, onlyDirectAdmins);
		} else if (complementaryObjectName.equals("Facility")) {
			if(!role.equals(Role.FACILITYADMIN)) throw new RoleNotSupportedException("Not supported other role than facility manager for object Facility.");
			Facility facility = ((PerunBl) sess.getPerun()).getFacilitiesManagerBl().getFacilityById(sess, complementaryObjectId);
			richUsers = sess.getPerun().getFacilitiesManager().getRichAdmins(sess, facility, specificAttributes, allUserAttributes, onlyDirectAdmins);
		} else {
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
	 * @param role expected role to filter authorizedGroups by (perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator)
	 *
	 * @return list of authorizedGroups for complementary object and role
	 *
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws VoNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws RoleNotSupportedException
	 * @throws PerunBeanNotSupportedException
	 */
	public static List<Group> getAdminGroups(PerunSession sess, int complementaryObjectId, String complementaryObjectName, Role role) throws InternalErrorException, UserNotExistsException, PrivilegeException, GroupNotExistsException, VoNotExistsException, FacilityNotExistsException, RoleNotSupportedException, PerunBeanNotSupportedException {
		Utils.checkPerunSession(sess);
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObjectName, "complementaryObjectName");

		List<Group> authorizedGroups;
		//Try to get complementary Object
		if(complementaryObjectName.equals("Group")) {
			if(!role.equals(Role.GROUPADMIN)) throw new RoleNotSupportedException("Not supported other role than group manager for object Group.");
			Group group = ((PerunBl) sess.getPerun()).getGroupsManagerBl().getGroupById(sess, complementaryObjectId);
			authorizedGroups = sess.getPerun().getGroupsManager().getAdminGroups(sess, group);
		} else if (complementaryObjectName.equals("Vo")) {
			Vo vo = ((PerunBl) sess.getPerun()).getVosManagerBl().getVoById(sess, complementaryObjectId);
			authorizedGroups = sess.getPerun().getVosManager().getAdminGroups(sess, vo, role);
		} else if (complementaryObjectName.equals("Facility")) {
			if(!role.equals(Role.FACILITYADMIN)) throw new RoleNotSupportedException("Not supported other role than facility manager for object Facility.");
			Facility facility = ((PerunBl) sess.getPerun()).getFacilitiesManagerBl().getFacilityById(sess, complementaryObjectId);
			authorizedGroups = sess.getPerun().getFacilitiesManager().getAdminGroups(sess, facility);
		} else {
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
	public static PerunPrincipal getPerunPrincipal(PerunSession sess) throws InternalErrorException, UserNotExistsException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getPerunPrincipal(sess);
	}

	/**
	 * Returns all complementary objects for defined role.
	 *
	 * @param sess perun session
	 * @param role to get object for
	 * @return list of complementary objects
	 * @throws InternalErrorException
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role);
	}

	/**
	 * Returns complementary objects for defined role filtered by particular class, e.g. Vo, Group, ...
	 *
	 * @param sess perun session
	 * @param role to get object for
	 * @param perunBeanClass particular class ( Vo | Group | ... )
	 * @return list of complementary objects
	 * @throws InternalErrorException
	 */
	public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role, Class perunBeanClass) throws InternalErrorException {
		return cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role, perunBeanClass);
	}

	/**
	 * Removes all existing roles for the perunPrincipal and call init again.
	 *
	 * @param sess perun session
	 * @throws InternalErrorException
	 */
	public static void refreshAuthz(PerunSession sess) throws InternalErrorException {
		cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl.refreshAuthz(sess);
	}
}
