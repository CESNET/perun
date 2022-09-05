package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MFAuthenticationException;
import cz.metacentrum.perun.core.api.exceptions.MfaPrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
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

	public final static String MFA_CRITICAL_ATTR = "mfaCriticalObject";

	/**
	 * Checks if the principal is authorized.
	 * This method should be accessed through external components.
	 *
	 * @param sess PerunSession which contains the principal.
	 * @param policyDefinition of policy which contains authorization rules.
	 * @param objects as list of PerunBeans on which will be authorization provided. (e.g. groups, Vos, etc...)
	 * @return true if the principal has particular rights, false otherwise.
	 * @throws PolicyNotExistsException when the given policyDefinition does not exist in the PerunPoliciesContainer.
	 * @throws MfaPrivilegeException when the principal isn't authenticated with MFA but the policy definition requires it
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
	 * @throws MfaPrivilegeException when the principal isn't authenticated with MFA but the policy definition requires it
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
	 * @throws MfaPrivilegeException when the principal isn't authenticated with MFA but the policy definition requires it
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
	 * @throws MfaPrivilegeException when the principal isn't authenticated with MFA but the policy definition requires it
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
	public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, String key) {
		return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, key);
	}

	/**
	 * Checks if the principal is authorized to do some action of group-resource attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param group primary Bean of Attribute (can't be null)
	 * @param resource secondary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Group group, Resource resource, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(group, resource))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, group, resource);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of resource-member attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param resource primary Bean of Attribute (can't be null)
	 * @param member secondary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Member member, Resource resource, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(member, resource))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, member, resource);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of user-facility attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param user primary Bean of Attribute (can't be null)
	 * @param facility secondary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, User user, Facility facility, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(user, facility))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, user, facility);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of member-group attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param member primary Bean of Attribute (can't be null)
	 * @param group secondary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Member member, Group group, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(member, group))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, member, group);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of user attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param user primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, User user, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(user))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, user);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of member attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param member primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Member member, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(member))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, member);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of vo attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param vo primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Vo vo, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(vo))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, vo);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
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
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true, if principal is authorized for attribute and action
	 */
	@SuppressWarnings("unused")
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, PerunBean bean, boolean checkMfa) throws InternalErrorException {
		if (bean instanceof Vo) return isAuthorizedForAttribute(sess, actionType, attrDef, (Vo)bean, checkMfa);
		if (bean instanceof User) return isAuthorizedForAttribute(sess, actionType, attrDef, (User)bean, checkMfa);
		if (bean instanceof Member) return isAuthorizedForAttribute(sess, actionType, attrDef, (Member)bean, checkMfa);
		if (bean instanceof Group) return isAuthorizedForAttribute(sess, actionType, attrDef, (Group)bean, checkMfa);
		if (bean instanceof Resource) return isAuthorizedForAttribute(sess, actionType, attrDef, (Resource)bean, checkMfa);
		if (bean instanceof Facility) return isAuthorizedForAttribute(sess, actionType, attrDef, (Facility)bean, checkMfa);
		if (bean instanceof Host) return isAuthorizedForAttribute(sess, actionType, attrDef, (Host)bean, checkMfa);
		if (bean instanceof UserExtSource) return isAuthorizedForAttribute(sess, actionType, attrDef, (UserExtSource)bean, checkMfa);
		throw new UnsupportedOperationException(
			"method - isAuthorizedForAttribute - called with unsupported PerunBean type - " + bean.getBeanName());
	}

	/**
	 * Checks if the principal is authorized to do some action of group attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param group primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Group group, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(group))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, group);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of resource attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param resource primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Resource resource, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(resource))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, resource);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of facility attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param facility primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Facility facility, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(facility))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, facility);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of host attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param host primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Host host, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(host))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, host);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of ues attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param ues primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, UserExtSource ues, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList(ues))) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, ues);
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Checks if the principal is authorized to do some action of entityless attribute.
	 *
	 * @param sess perun session
	 * @param actionType type of action on attribute
	 * @param attrDef attribute what principal want to work with
	 * @param key primary Bean of Attribute (can't be null)
	 * @param checkMfa if true, checks also MFA rules and throws exception if unmet
	 * @throws MfaPrivilegeException thrown when checkMfa is true and MFA rules are unmet
	 * @return true if principal is authorized, false if not
	 */
	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, String key, boolean checkMfa) throws InternalErrorException {
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		if (checkMfa && !AuthzResolverBlImpl.isMfaAuthorizedForAttribute(sess, attrDef, actionType, Arrays.asList())) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		try {
			return AuthzResolverBlImpl.isAuthorizedForAttribute(sess, actionType, attrDef, key);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}
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
	 * Get all user role names. Does not include membership and sponsorship role.
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
	 * Returns also sponsorship and membership roles.
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
	 * Check whether the principal is authorized to manage the role on the object.
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
	 * Check whether the principal is authorized to read the role on the object.
	 *
	 * @param sess principal's perun session
	 * @param complementaryObject bounded with the role
	 * @param role which will be read
	 * @return
	 * @throws RoleManagementRulesNotExistsException when the role does not have the management rules.
	 */
	public static boolean authorizedToReadRole(PerunSession sess, PerunBean complementaryObject, String role) throws RoleManagementRulesNotExistsException {
		if (!roleExists(role)) {
			throw new InternalErrorException("Role: "+ role +" does not exists.");
		}
		return AuthzResolverBlImpl.authorizedToReadRole(sess, complementaryObject, role);
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
	 * Get all valid richUser administrators (for group-based rights, status must be VALID for both Vo and group) for complementary object and role with specified attributes.
	 *
	 * If <b>onlyDirectAdmins</b> is <b>true</b>, return only direct users of the complementary object for role with specific attributes.
	 * If <b>allUserAttributes</b> is <b>true</b>, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param sess perun session
	 * @param complementaryObject for which we will get administrator
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param role expected role to filter managers by
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 *
	 * @return list of richUser administrators for complementary object and role with specified attributes.
	 */
	public static List<RichUser> getRichAdmins(PerunSession sess, PerunBean complementaryObject, List<String> specificAttributes, String role, boolean onlyDirectAdmins, boolean allUserAttributes) throws PrivilegeException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObject, "complementaryObject");

		if (!roleExists(role)) {
			throw new InternalErrorException("Role: " + role + " does not exists.");
		}

		// Authorization
		try {
			if(!authorizedToReadRole(sess, complementaryObject, role)) {
				throw new PrivilegeException("You are not privileged to use the method getRichAdmins.");
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		return AuthzResolverBlImpl.getRichAdmins(sess, complementaryObject, specificAttributes, role, onlyDirectAdmins, allUserAttributes);
	}

	/**
	 * Get all authorizedGroups for complementary object and role.
	 *
	 * @param sess perun session
	 * @param complementaryObject for which we will get administrator groups
	 * @param role expected role to filter authorizedGroups by
	 *
	 * @return list of authorizedGroups for complementary object and role
	 */
	public static List<Group> getAdminGroups(PerunSession sess, PerunBean complementaryObject, String role) throws PrivilegeException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		Utils.notNull(role, "role");
		Utils.notNull(complementaryObject, "complementaryObject");

		if (!roleExists(role)) {
			throw new InternalErrorException("Role: " + role + " does not exists.");
		}

		// Authorization
		try {
			if(!authorizedToReadRole(sess, complementaryObject, role)) {
				throw new PrivilegeException("You are not privileged to use the method getAdminGroups.");
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		return AuthzResolverBlImpl.getAdminGroups(complementaryObject, role);
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
	 * Calls UserInfo endpoint to obtain the newest information on performed MFA.
	 * Requires access token and issuer to be stored in the additionalInformations.
	 * If user used MFA to log in (MFA acr is returned from the endpoint), endpoint returns MFA timestamp.
	 * This method stores the timestamp into principal's additionalInformations.
	 *
	 * @param sess perun session with required additionalInformation in Principal
	 * @throws ExpiredTokenException expired access token
	 * @throws MFAuthenticationException wrong configuration or missing required information
	 * @throws PrivilegeException unauthorized
	 */
	public static void refreshMfa(PerunSession sess) throws ExpiredTokenException, MFAuthenticationException, PrivilegeException {
		Utils.checkPerunSession(sess);

		//Authorization
		if (!authorizedInternal(sess, "refreshMfa_policy", sess.getPerunPrincipal().getUser()))
			throw new PrivilegeException(sess, "refreshMfa");

		AuthzResolverBlImpl.refreshMfa(sess);
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

	/**
	 * Return all loaded roles management rules.
	 *
	 * @return all roles management rules
	 */
	public static List<RoleManagementRules> getAllRolesManagementRules() {
		return AuthzResolverBlImpl.getAllRolesManagementRules();
	}

	/**
	 * Get all Vos where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 * If user parameter is null then Vos are retrieved for the given principal.
	 *
	 * @param sess Perun session
	 * @param user for who Vos are retrieved
	 * @param roles for which Vos are retrieved
	 * @return List of Vos
	 *
	 * @throws PrivilegeException when the principal is not authorized.
	 */
	public static List<Vo> getVosWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(roles, "roles");

		if (user == null) {
			user = sess.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!authorizedInternal(sess, "getVosWhereUserIsInRoles_User_List<String>_policy", user)) {
				throw new PrivilegeException(sess, "getVosWhereUserIsInRoles");
			}
		}

		return AuthzResolverBlImpl.getVosWhereUserIsInRoles(sess, user, roles);
	}

	/**
	 * Get all Facilities where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 * If user parameter is null then Facilities are retrieved for the given principal.
	 *
	 * @param sess Perun session
	 * @param user for who Facilities are retrieved
	 * @param roles for which Facilities are retrieved
	 * @return List of Facilities
	 *
	 * @throws PrivilegeException when the principal is not authorized.
	 */
	public static List<Facility> getFacilitiesWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(roles, "roles");

		if (user == null) {
			user = sess.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!authorizedInternal(sess, "getFacilitiesWhereUserIsInRoles_User_List<String>_policy", user)) {
				throw new PrivilegeException(sess, "getFacilitiesWhereUserIsInRoles");
			}
		}

		return AuthzResolverBlImpl.getFacilitiesWhereUserIsInRoles(sess, user, roles);
	}

	/**
	 * Get all Resources where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 * If user parameter is null then Resources are retrieved for the given principal.
	 *
	 * @param sess Perun session
	 * @param user for who Resources are retrieved
	 * @param roles for which Resources are retrieved
	 * @return List of Resources
	 *
	 * @throws PrivilegeException when the principal is not authorized.
	 */
	public static List<Resource> getResourcesWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(roles, "roles");

		if (user == null) {
			user = sess.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!authorizedInternal(sess, "getResourcesWhereUserIsInRoles_User_List<String>_policy", user)) {
				throw new PrivilegeException(sess, "getResourcesWhereUserIsInRoles");
			}
		}

		return AuthzResolverBlImpl.getResourcesWhereUserIsInRoles(sess, user, roles);
	}

	/**
	 * Get all Groups where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 * If user parameter is null then Groups are retrieved for the given principal.
	 *
	 * Method does not return subgroups of the fetched groups.
	 *
	 * @param sess Perun session
	 * @param user for who Groups are retrieved
	 * @param roles for which Groups are retrieved
	 * @return List of Groups
	 *
	 * @throws PrivilegeException when the principal is not authorized.
	 */
	public static List<Group> getGroupsWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(roles, "roles");

		if (user == null) {
			user = sess.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!authorizedInternal(sess, "getGroupsWhereUserIsInRoles_User_List<String>_policy", user)) {
				throw new PrivilegeException(sess, "getGroupsWhereUserIsInRoles");
			}
		}

		return AuthzResolverBlImpl.getGroupsWhereUserIsInRoles(sess, user, roles);
	}

	/**
	 * Get all Members where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 * If user parameter is null then Members are retrieved for the given principal.
	 *
	 * @param sess Perun session
	 * @param user for who Members are retrieved
	 * @param roles for which Members are retrieved
	 * @return List of Members
	 *
	 * @throws PrivilegeException when the principal is not authorized.
	 */
	public static List<Member> getMembersWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(roles, "roles");

		if (user == null) {
			user = sess.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!authorizedInternal(sess, "getMembersWhereUserIsInRoles_User_List<String>_policy", user)) {
				throw new PrivilegeException(sess, "getMembersWhereUserIsInRoles");
			}
		}

		return AuthzResolverBlImpl.getMembersWhereUserIsInRoles(sess, user, roles);
	}

	/**
	 * Get all SecurityTeams where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 * If user parameter is null then SecurityTeams are retrieved for the given principal.
	 *
	 * @param sess Perun session
	 * @param user for who SecurityTeams are retrieved
	 * @param roles for which SecurityTeams are retrieved
	 * @return List of SecurityTeams
	 *
	 * @throws PrivilegeException when the principal is not authorized.
	 */
	public static List<SecurityTeam> getSecurityTeamsWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(roles, "roles");

		if (user == null) {
			user = sess.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!authorizedInternal(sess, "getSecurityTeamsWhereUserIsInRoles_User_List<String>_policy", user)) {
				throw new PrivilegeException(sess, "getSecurityTeamsWhereUserIsInRoles");
			}
		}

		return AuthzResolverBlImpl.getSecurityTeamsWhereUserIsInRoles(sess, user, roles);
	}
}
