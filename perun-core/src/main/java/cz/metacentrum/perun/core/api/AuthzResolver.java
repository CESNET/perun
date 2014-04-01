package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;

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
