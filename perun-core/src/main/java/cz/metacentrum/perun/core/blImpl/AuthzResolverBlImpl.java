package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleSetForGroup;
import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleSetForUser;
import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleUnsetForGroup;
import cz.metacentrum.perun.audit.events.AuthorizationEvents.RoleUnsetForUser;
import cz.metacentrum.perun.audit.events.UserManagerEvents.UserPromotedToPerunAdmin;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributePolicy;
import cz.metacentrum.perun.core.api.AttributePolicyCollection;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupResourceStatus;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
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
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleAssignmentType;
import cz.metacentrum.perun.core.api.RoleManagementRules;
import cz.metacentrum.perun.core.api.RoleObject;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExpiredTokenException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MFAuthenticationException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MfaInvalidRolesException;
import cz.metacentrum.perun.core.api.exceptions.MfaPrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.MfaRolePrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.MfaRoleTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.MfaTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleAlreadySetException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSetException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
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

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
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
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.api.AuthzResolver.MFA_CRITICAL_ATTR;
import static cz.metacentrum.perun.core.api.PerunPrincipal.ACCESS_TOKEN;
import static cz.metacentrum.perun.core.api.PerunPrincipal.ACR_MFA;
import static cz.metacentrum.perun.core.api.PerunPrincipal.AUTH_TIME;
import static cz.metacentrum.perun.core.api.PerunPrincipal.ISSUER;
import static org.apache.commons.lang3.StringUtils.isBlank;

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
	private final static String groupObjectType = "Group";
	private final static String userObjectType = "User";
	private final static List<String> authorizedDefaultReadRoles = List.of(Role.PERUNADMIN, Role.PERUNADMINBA, Role.PERUNOBSERVER, Role.RPC, Role.ENGINE);
	private final static List<String> authorizedDefaultWriteRoles = List.of(Role.PERUNADMIN, Role.PERUNADMINBA);

	/**
	 * Prepare necessary structures and resolve access rights for the session's principal.
	 *
	 * @param sess perunSession which contains the principal.
	 * @param policyDefinition is a definition of a policy which will define authorization rules.
	 * @param objects as list of PerunBeans on which will be authorization provided. (e.g. groups, Vos, etc...)
	 * @return true if the principal has particular rights, false otherwise.
	 * @throws PolicyNotExistsException when the given policyDefinition does not exist in the PerunPoliciesContainer.
	 * @throws MfaPrivilegeException when the principal isn't authenticated with MFA but the policy definition requires it
	 */
	public static boolean authorized(PerunSession sess, String policyDefinition, List<PerunBean> objects) throws PolicyNotExistsException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		periodicCheckAuthz(sess);

		// If the user has no roles, deny access
		if (sess.getPerunPrincipal().getRoles() == null) {
			return false;
		}

		List<PerunPolicy> allPolicies = AuthzResolverImpl.fetchPolicyWithAllIncludedPolicies(policyDefinition);

		List<Map<String, String>> policyRoles = new ArrayList<>();
		List<Map<String, String>> mfaRules = new ArrayList<>();
		for (PerunPolicy policy : allPolicies) {
			policyRoles.addAll(policy.getPerunRoles());
			if (policy.getMfaRules() != null) mfaRules.addAll(policy.getMfaRules());
		}

		//Fetch super objects like Vo for group etc.
		Map <String, Set<Integer>> mapOfBeans = fetchAllRelatedObjects(objects);

		if (!mfaAuthorized(sess, mfaRules, mapOfBeans) && !updatePrincipalMfa(sess)) {
			throw new MfaPrivilegeException("Multi-Factor authentication required");
		}

		return resolveAuthorization(sess, policyRoles, mapOfBeans);
	}

	/**
	 * If the last check was earlier than the set interval then updates the roles.
	 *
	 * @param sess session
	 */
	private static void periodicCheckAuthz(PerunSession sess) {
		if (System.currentTimeMillis() - sess.getPerunPrincipal().getRolesUpdatedAt() >= TimeUnit.MINUTES.toMillis(BeansUtils.getCoreConfig().getRoleUpdateInterval())) {
			log.debug("Periodic update authz roles for session {}.", sess);

			refreshAuthz(sess);
			sess.getPerunPrincipal().setRolesUpdatedAt(System.currentTimeMillis());
		}
	}

	/**
	 * Checks authorization according to MFA rules.
	 *
	 * Returns false if there is an MFA rule on an object which is marked as critical,
	 * and principal is not authorized by MFA and hasn't got a system role.
	 * If MFA is globally disabled for whole instance, returns true.
	 *
	 * @param sess session
	 * @param mfaRules is a list of maps where each map entry consists from a role name as a key and a role object as a value.
	 *                    Relation between each map in the list is logical OR and relation between each entry in the map is logical AND.
	 *                    Example list - (Map1, Map2...)
	 *                    Example map - key: MFA ; value: Vo
	 *                                 key: MFA ; value: Group
	 * @param mapOfBeans is a map of objects against which will be authorization done.
	 *                    Example map entry - key: Member ; values: (10,15,26)
	 * @return true if MFA requirements are met, false otherwise
	 */
	private static boolean mfaAuthorized(PerunSession sess, List<Map<String, String>> mfaRules, Map<String, Set<Integer>> mapOfBeans) {
		try {
			return !BeansUtils.getCoreConfig().isEnforceMfa() || sess.getPerunPrincipal().getRoles().hasRole(Role.MFA)
						|| hasMFASkippableRole(sess) || !requiresMfa(sess, mfaRules, mapOfBeans);
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Error checking system roles", e);
		}
	}

	/**
	 * Returns true if at least one of the given MFA rules requires MFA on objects which are marked as critical.
	 *
	 * @param sess
	 * @param mfaRules
	 * @param mapOfBeans
	 * @return
	 */
	private static boolean requiresMfa(PerunSession sess, List<Map<String, String>> mfaRules, Map<String, Set<Integer>> mapOfBeans) {
		for (Map<String, String> rule : mfaRules) {
			// every rule should have exactly one map entry (with key 'MFA')
			if (!rule.containsKey(Role.MFA)) continue;

			String ruleObject = rule.get(Role.MFA);
			if (isBlank(ruleObject)) return true;

			Set<Integer> ids = mapOfBeans.get(ruleObject);
			if (ids != null && ids.stream().anyMatch(id -> isCriticalObject(sess, ruleObject, id))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the object of given type with given ID is marked as critical.
	 *
	 * @param sess
	 * @param objectType
	 * @param id
	 * @return
	 */
	private static boolean isCriticalObject(PerunSession sess, String objectType, Integer id) {
		try {
			if ("Group".equals(objectType)) {
				Group group = perunBl.getGroupsManagerBl().getGroupById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(group));
			} else if ("Vo".equals(objectType)) {
				Vo vo = perunBl.getVosManagerBl().getVoById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(vo));
			} else if ("User".equals(objectType)) {
				User user = perunBl.getUsersManagerBl().getUserById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(user));
			} else if ("Member".equals(objectType)) {
				Member member = perunBl.getMembersManagerBl().getMemberById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(member));
			} else if ("Resource".equals(objectType)) {
				Resource resource = perunBl.getResourcesManagerBl().getResourceById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(resource));
			} else if ("Facility".equals(objectType)) {
				Facility facility = perunBl.getFacilitiesManagerBl().getFacilityById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(facility));
			} else if ("Host".equals(objectType)) {
				Host host = perunBl.getFacilitiesManagerBl().getHostById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(host));
			} else if ("UserExtSource".equals(objectType)) {
				UserExtSource ues = perunBl.getUsersManagerBl().getUserExtSourceById(sess, id);
				return isAnyObjectMfaCritical(sess, List.of(ues));
			} else {
				throw new InternalErrorException("Object of type " + objectType + "could not be checked for MFA criticality.");
			}
		} catch (MemberNotExistsException | GroupNotExistsException | UserNotExistsException | VoNotExistsException | HostNotExistsException |
			UserExtSourceNotExistsException | FacilityNotExistsException | ResourceNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Check whether the principal is authorized to manage the role on the object.
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

		periodicCheckAuthz(sess);

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

		return resolveAuthorization(sess, rules.getPrivilegedRolesToManage(), mapOfBeans);
	}

	/**
	 * Check whether the principal is authorized to read the role on the object.
	 *
	 * @param sess principal's perun session
	 * @param object bounded with the role
	 * @param roleName which will be managed
	 * @return true if principal is authorized. False otherwise.
	 * @throws RoleManagementRulesNotExistsException when the role does not have the management rules.
	 */
	public static boolean authorizedToReadRole(PerunSession sess, PerunBean object, String roleName) throws RoleManagementRulesNotExistsException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		periodicCheckAuthz(sess);

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

		return resolveAuthorization(sess, rules.getPrivilegedRolesToRead(), mapOfBeans);
	}

	/**
	 * Returns true if principal has a role which should skip MFA check
	 *
	 * @param sess principal's perun session
	 * @return true if principal has system role
	 * @throws RoleManagementRulesNotExistsException when the role does not have the management rules.
	 */
	public static boolean hasMFASkippableRole(PerunSession sess) throws RoleManagementRulesNotExistsException {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		// If the user has no roles, deny access
		if (sess.getPerunPrincipal().getRoles() == null) {
			return false;
		}

		// System role which was defined in coreConfig with PERUNADMIN rights
		List<String> perunAdmins = new ArrayList<>(BeansUtils.getCoreConfig().getAdmins());
		perunAdmins.addAll(BeansUtils.getCoreConfig().getRegistrarPrincipals());
		if (perunAdmins.contains(sess.getPerunPrincipal().getActor())) {
			return true;
		}

		AuthzRoles roles = sess.getPerunPrincipal().getRoles();
		for (String role : roles.getRolesNames()) {
			if (AuthzResolverImpl.getRoleManagementRules(role).shouldSkipMFA()) {
				return true;
			}
		}

		return false;
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

		// This is same as PERUNADMIN but skips MFA
		if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMINBA)) {
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

	@Deprecated
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

		// This is same as PERUNADMIN but skips MFA
		if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMINBA)) {
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
				if(AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.READ, attribute, bean, true)) {
					attribute.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.WRITE, attribute, bean, false));
					allowedAttributes.add(attribute);
				}
			} catch (InternalErrorException e) {
				throw new RuntimeException(e);
			}
		}
		return allowedAttributes;
	}

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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


	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	@Deprecated
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

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Member member, Resource resource) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		if (member == null && resource != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, resource);
		} else if (resource == null && member != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, member);
		}

		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, resource, member);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, resource, member);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedMemberResourceObjectsResolver.getValue(objectType.name()).callOn(sess, member, resource);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Group group, Resource resource) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		if (group == null && resource != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, resource);
		} else if (resource == null && group != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, group);
		}

		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, group, resource);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, group, resource);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedGroupResourceObjectsResolver.getValue(objectType.name()).callOn(sess, group, resource);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, User user, Facility facility) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		if (user == null && facility != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, facility);
		} else if (facility == null && user != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, user);
		}

		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, user, facility);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, user, facility);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedUserFacilityObjectsResolver.getValue(objectType.name()).callOn(sess, user, facility);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Member member, Group group) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		if (group == null && member != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, member);
		} else if (member == null && group != null) {
			return isAuthorizedForAttribute(sess, actionType, attrDef, group);
		}

		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, member, group);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, member, group);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedMemberGroupObjectsResolver.getValue(objectType.name()).callOn(sess, member, group);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, User user) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, user, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, user);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedUserObjectsResolver.getValue(objectType.name()).apply(sess, user);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Member member) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, member, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, member);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedMemberObjectsResolver.getValue(objectType.name()).apply(sess, member);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Vo vo) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, vo, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, vo);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedVoObjectsResolver.getValue(objectType.name()).apply(sess, vo);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Group group) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, group, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, group);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedGroupObjectsResolver.getValue(objectType.name()).apply(sess, group);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Resource resource) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, resource, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, resource);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedResourceObjectsResolver.getValue(objectType.name()).apply(sess, resource);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Facility facility) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, facility, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, facility);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedFacilityObjectsResolver.getValue(objectType.name()).apply(sess, facility);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, Host host) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, host, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, host);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedHostObjectsResolver.getValue(objectType.name()).apply(sess, host);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, UserExtSource ues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, ues, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Test if handlers are correct for attribute namespace
		getPerunBl().getAttributesManagerBl().checkAttributeAssignment(sess, attrDef, ues);

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedUserExtSourceObjectsResolver.getValue(objectType.name()).apply(sess, ues);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	public static boolean isAuthorizedForAttribute(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef, String key) throws InternalErrorException, AttributeNotExistsException {
		log.trace("Entering isAuthorizedForAttribute: sess='{}', actionType='{}', attrDef='{}', primaryHolder='{}', " +
			"secondaryHolder='{}'", sess, actionType, attrDef, key, null);

		// Check roles which are authorized by default
		if (hasAccessByDefault(sess, actionType)) {
			return true;
		}

		List<AttributePolicyCollection> policyCollections = getAttributePolicyCollections(sess, actionType, attrDef);

		// If the user has no roles deny access
		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		//Get all unique objects from the roles' action types
		Set<RoleObject> uniqueObjectTypes = fetchUniqueObjectTypes(policyCollections);

		//Fetch all possible related objects from the member and the resource according the uniqueObjectTypes
		Map<RoleObject, Set<Integer>> associatedObjects = new HashMap<>();
		for (RoleObject objectType: uniqueObjectTypes) {
			Set<Integer> retrievedObjects = RelatedEntitylessObjectsResolver.getValue(objectType.name()).apply(sess, key);
			associatedObjects.put(objectType, retrievedObjects);
		}

		//Resolve principal's privileges for the attribute according to the rules and objects
		return resolveAttributeAuthorization(sess, policyCollections, associatedObjects);
	}

	/**
	 * Checks authorization for attribute according to MFA rules.
	 * Returns false if attribute action is marked as critical, attribute's object is marked as critical
	 * and principal is not authorized by MFA and hasn't got a system role.
	 * If MFA is globally disabled for whole instance, returns true.
	 *
	 * @param sess session
	 * @param attrDef attribute definition
	 * @param actionType type of action (READ / WRITE)
	 * @param objects objects related to the attribute
	 * @return true if MFA requirements are met, false otherwise
	 */
	public static boolean isMfaAuthorizedForAttribute(PerunSession sess, AttributeDefinition attrDef, AttributeAction actionType, List<Object> objects) {
		if (!BeansUtils.getCoreConfig().isEnforceMfa()) {
			return true;
		}

		try {
			if (hasMFASkippableRole(sess)) {
				return true;
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Error checking system roles", e);
		}

		if (!((PerunBl) sess.getPerun()).getAttributesManagerBl().isAttributeActionCritical(sess, attrDef, actionType)) {
			return true;
		}

		boolean principalMfa = sess.getPerunPrincipal().getRoles().hasRole(Role.MFA);
		if (attrDef.getNamespace().startsWith(AttributesManager.NS_ENTITYLESS_ATTR)) {
			return principalMfa || updatePrincipalMfa(sess);
		}

		boolean globallyCriticalAction  = ((PerunBl) sess.getPerun()).getAttributesManagerBl().isAttributeActionGloballyCritical(sess, attrDef, actionType);

		return principalMfa || (!isAnyObjectMfaCritical(sess, objects) && !globallyCriticalAction) || updatePrincipalMfa(sess);

	}

	/**
	 * Updates principal MFA role by calling UserInfo Endpoint
	 * @param sess perun session
	 * @return true if principal has MFA role
	 */
	private static boolean updatePrincipalMfa(PerunSession sess) {
		try {
			refreshMfa(sess);
		} catch (ExpiredTokenException | MFAuthenticationException ignored) {
			// couldn't recheck with endpoint, either exception would have been thrown already or principal didn't use OIDC
		}

		return sess.getPerunPrincipal().getRoles().hasRole(Role.MFA);
	}

	/**
	 * Returns true if any of the objects is marked as mfaCriticalObject in its attribute.
	 * Not usable for entityless attributes!
	 * @param sess session
	 * @param objects objects to be checked
	 * @return if any object is critical
	 */
	public static boolean isAnyObjectMfaCritical(PerunSession sess, List<Object> objects) {
		AttributesManagerBl attributesManagerBl = ((PerunBl) sess.getPerun()).getAttributesManagerBl();

		for (Object object : objects) {
			if (object == null) continue;
			Attribute attr;
			try {
				if (object instanceof Member m) {
					attr = attributesManagerBl.getAttribute(sess, m, AttributesManager.NS_MEMBER_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else if (object instanceof User u) {
					attr = attributesManagerBl.getAttribute(sess, u, AttributesManager.NS_USER_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else if (object instanceof Resource r) {
					attr = attributesManagerBl.getAttribute(sess, r, AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else if (object instanceof Facility f) {
					attr = attributesManagerBl.getAttribute(sess, f, AttributesManager.NS_FACILITY_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else if (object instanceof Group g) {
					attr = attributesManagerBl.getAttribute(sess, g, AttributesManager.NS_GROUP_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else if (object instanceof Vo v) {
					attr = attributesManagerBl.getAttribute(sess, v, AttributesManager.NS_VO_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else if (object instanceof Host h) {
					attr = attributesManagerBl.getAttribute(sess, h, AttributesManager.NS_HOST_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else if (object instanceof UserExtSource ues) {
					attr = attributesManagerBl.getAttribute(sess, ues, AttributesManager.NS_UES_ATTR_DEF + ":" + MFA_CRITICAL_ATTR);
				} else {
					throw new InternalErrorException("Object of class " + object.getClass().getName() + "could not be checked for MFA criticality.");
				}
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			}

			if (attr.getValue() != null && attr.valueAsBoolean()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Retrieves all attribute policy collections for given attribute definition and action type
	 * @param sess session
	 * @param actionType type of action (READ / WRITE)
	 * @param attrDef attribute definition
	 * @return list of attribute collections for given attribute definition and action type
	 * @throws AttributeNotExistsException
	 */
	private static List<AttributePolicyCollection> getAttributePolicyCollections(PerunSession sess, AttributeAction actionType, AttributeDefinition attrDef) throws AttributeNotExistsException {
		Utils.notNull(sess, "sess");
		Utils.notNull(actionType, "ActionType");
		Utils.notNull(attrDef, "AttributeDefinition");
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);

		return perunBl.getAttributesManagerBl().getAttributePolicyCollections(sess, attrDef.getId()).stream()
			.filter(c -> c.getAction().equals(actionType)).toList();
	}

	/**
	 * Resolve authorization for attribute according to map of privileged roles and map of objects.
	 * Expects policy collection filtered by required action type.
	 *
	 * @param sess PerunSession which want to operate on attribute
	 * @param policyCollections policy collections containing only required action type collections
	 * @param associatedObjects map of object types with actual associated objects
	 * @return true if principal is privileged to operate on attribute (satisfies at least one collection's policies), false otherwise.
	 */
	private static boolean resolveAttributeAuthorization(PerunSession sess, List<AttributePolicyCollection> policyCollections, Map<RoleObject, Set<Integer>> associatedObjects) {
		for (AttributePolicyCollection policyCollection : policyCollections) {
			boolean collectionSatisfied = true;

			if (policyCollection.getPolicies().isEmpty()) {
				throw new InternalErrorException("Policy collection with id " + policyCollection.getId() + " contains no policies.");
			}

			for (AttributePolicy policy : policyCollection.getPolicies()) {
				if (!resolvePolicyPrivileges(sess, associatedObjects, policy, policyCollection.getAction())) {
					collectionSatisfied = false;
					break;
				}
			}

			if (collectionSatisfied) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Resolves single attribute policy - checks, if principal has required role in at least one of retrieved associated objects.
	 * If policy is required for READ action, also associated roles are taken into account.
	 * @param sess session
	 * @param associatedObjects map of object types with actual associated objects
	 * @param policy attribute policy to be checked
	 * @param actionType type of action (READ/WRITE)
	 * @return true if principal is privileged for attribute, false otherwise
	 */
	private static boolean resolvePolicyPrivileges(PerunSession sess, Map<RoleObject, Set<Integer>> associatedObjects, AttributePolicy policy, AttributeAction actionType) {
		AuthzRoles principalRoles = sess.getPerunPrincipal().getRoles();

		if (policy.getObject().equals(RoleObject.None)) {
			if (principalRoles.hasRole(policy.getRole())) {
				return true;
			}
			if (actionType.equals(AttributeAction.READ)) {
				try {
					RoleManagementRules roleRules = AuthzResolverImpl.getRoleManagementRules(policy.getRole());
					if (roleRules.getAssociatedReadRoles().stream().anyMatch(principalRoles::hasRole)) {
						return true;
					}
				} catch (RoleManagementRulesNotExistsException e) {
					throw new InternalErrorException("Management rules not exist for the role " + policy.getRole(), e);
				}
			}
		}

		if (associatedObjects.containsKey(policy.getObject())) {
			Set<Integer> objectsToCheck = associatedObjects.get(policy.getObject());
			for (Integer objectId : objectsToCheck) {
				if (principalRoles.hasRole(policy.getRole(), policy.getObject().name(), objectId)) {
					return true;
				}
				if (actionType.equals(AttributeAction.READ)) {
					try {
						RoleManagementRules roleRules = AuthzResolverImpl.getRoleManagementRules(policy.getRole());
						if (roleRules.getAssociatedReadRoles().stream().anyMatch(role -> principalRoles.hasRole(role, policy.getObject().name(), objectId))) {
							return true;
						}
					} catch (RoleManagementRulesNotExistsException e) {
						throw new InternalErrorException("Management rules not exist for the role " + policy.getRole(), e);
					}
				}
			}
		} else {
			throw new InternalErrorException("Objects of type " + policy.getObject().name() + " were not retrieved for attribute policy check.");
		}

		return false;
	}

	/**
	 * Resolves privileges on attribute for default roles by type.
	 * @param sess from which will be principal fetched
	 * @param actionType type of action (READ / WRITE)
	 * @return true if principal is authorized by default for given action type
	 */
	private static boolean hasAccessByDefault(PerunSession sess, AttributeAction actionType) {
		// We need to load additional information about the principal
		if (!sess.getPerunPrincipal().isAuthzInitialized()) {
			refreshAuthz(sess);
		}

		periodicCheckAuthz(sess);

		if (sess.getPerunPrincipal().getRoles() == null || sess.getPerunPrincipal().getRoles().isEmpty()) {
			return false;
		}

		List<String> principalRoles = sess.getPerunPrincipal().getRoles().getRolesNames();

		return switch (actionType) {
			case READ -> principalRoles.stream().anyMatch(authorizedDefaultReadRoles::contains);
			case WRITE -> principalRoles.stream().anyMatch(authorizedDefaultWriteRoles::contains);
		};
	}

	public static boolean isAuthorizedForGroup(PerunSession sess, String policy, Integer groupId, Integer voId) {
		Group group = new Group();
		group.setId(groupId);
		group.setVoId(voId);

		try {
			return authorized(sess, policy, Collections.singletonList(group));
		} catch (PolicyNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Return map of roles, with allowed actions, which are authorized for doing "action" on "attribute".
	 *
	 * @param sess       perun session
	 * @param actionType type of action on attribute (ex.: write, read, etc...)
	 * @param attrDef    attribute what principal want to work with
	 * @return map of roles with allowed action types
	 */
	@Deprecated
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
	 * @param role                role of user in a session ( PERUNADMIN | PERUNADMINBA | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | SECURITYADMIN | RESOURCESELFSERVICE | RESOURCEADMIN )
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, User user, PerunBean complementaryObject, String role) throws AlreadyAdminException, RoleCannotBeManagedException {
		if (!objectAndRoleManageableByEntity(user.getBeanName(), complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, user);
		}

		checkMfaForSettingRole(sess, user, complementaryObject, role);

		Map<String, Integer> mappingOfValues = createMappingToManageRole(user, complementaryObject, role);

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
	 * @param role                role of user in a session ( PERUNADMIN | PERUNADMINBA | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | RESOURCESELFSERVICE | RESOURCEADMIN )
	 * @param complementaryObject object for which role will be set
	 */
	public static void setRole(PerunSession sess, Group authorizedGroup, PerunBean complementaryObject, String role) throws AlreadyAdminException, RoleCannotBeManagedException {
		if (!objectAndRoleManageableByEntity(authorizedGroup.getBeanName(), complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, authorizedGroup);
		}

		checkMfaForSettingRole(sess, authorizedGroup, complementaryObject, role);

		Map<String, Integer> mappingOfValues = createMappingToManageRole(authorizedGroup, complementaryObject, role);

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
	 * @param role                role of user in a session ( PERUNADMIN | PERUNADMINBA | VOADMIN | GROUPADMIN | SELF | FACILITYADMIN | VOOBSERVER | TOPGROUPCREATOR | RESOURCESELFSERVICE | RESOURCEADMIN )
	 * @param complementaryObject object for which role will be unset
	 */
	public static void unsetRole(PerunSession sess, User user, PerunBean complementaryObject, String role) throws UserNotAdminException, RoleCannotBeManagedException {
		if (!objectAndRoleManageableByEntity(user.getBeanName(), complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, user);
		}

		checkMfaForSettingRole(sess, user, complementaryObject, role);

		Map<String, Integer> mappingOfValues = createMappingToManageRole(user, complementaryObject, role);

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
		if (!objectAndRoleManageableByEntity(authorizedGroup.getBeanName(), complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject, authorizedGroup);
		}

		checkMfaForSettingRole(sess, authorizedGroup, complementaryObject, role);

		Map<String, Integer> mappingOfValues = createMappingToManageRole(authorizedGroup, complementaryObject, role);

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

	/**
	 * Return all loaded roles management rules.
	 *
	 * @return all roles management rules
	 */
	public static List<RoleManagementRules> getAllRolesManagementRules() {
		return AuthzResolverImpl.getAllRolesManagementRules();
	}

	/**
	 * Get all authorizedGroups for complementary object and role.
	 *
	 * @param complementaryObject for which we will get administrator groups
	 * @param role expected role to filter authorizedGroups by
	 *
	 * @return list of authorizedGroups for complementary object and role
	 */
	public static List<Group> getAdminGroups(PerunBean complementaryObject, String role) throws RoleCannotBeManagedException {

		if (!objectAndRoleManageableByEntity(groupObjectType, complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject);
		}

		Map<String, Integer> mappingOfValues = createMappingToReadRoleOnObject(complementaryObject, role);

		return authzResolverImpl.getAdminGroups(mappingOfValues);
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
	public static List<RichUser> getRichAdmins(PerunSession sess, PerunBean complementaryObject, List<String> specificAttributes, String role, boolean onlyDirectAdmins, boolean allUserAttributes) throws RoleCannotBeManagedException {

		if (!objectAndRoleManageableByEntity(userObjectType, complementaryObject, role)) {
			throw new RoleCannotBeManagedException(role, complementaryObject);
		}

		Map<String, Integer> mappingOfValues = createMappingToReadRoleOnObject(complementaryObject, role);

		List<User> admins = authzResolverImpl.getAdmins(mappingOfValues, onlyDirectAdmins);
		List<RichUser> richAdminsWithAttributes;

		if(allUserAttributes) {
			try {
				richAdminsWithAttributes = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(sess, admins);
			} catch (UserNotExistsException e) {
				throw new InternalErrorException(e);
			}
		} else {
			try {
				List<AttributeDefinition> attrDefinitions = getPerunBl().getAttributesManagerBl().getAttributesDefinition(sess, specificAttributes);
				List<RichUser> richAdmins = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(sess, admins);
				richAdminsWithAttributes = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, richAdmins, attrDefinitions);
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("One of the given attributes doesn`t exist.", ex);
			}
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, richAdminsWithAttributes);
	}

	/**
	 * Get all valid richUser administrators (for group-based rights, status must be VALID for both Vo and group) for complementary object and role without any attributes.
	 *
	 * @param sess perun session
	 * @param complementaryObject for which we will get administrator
	 * @param role expected role to filter managers by
	 *
	 * @return list of richUser administrators for complementary object and role.
	 */
	public static List<RichUser> getRichAdmins(PerunSession sess, PerunBean complementaryObject, String role) throws RoleCannotBeManagedException {
		return getRichAdmins(sess, complementaryObject, Collections.emptyList(), role, false, false);
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
	 * Returns true if the perun principal inside the perun session is Perun Observer.
	 *
	 * @param sess perun session
	 * @return true if the perun principal is top group creator.
	 */
	public static boolean isPerunObserver(PerunSession sess) {
		return sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNOBSERVER);
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
		return sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN) || sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMINBA);
	}

	/**
	 * Returns true if perun principal is Vo admin or Vo observer of specific Vo.
	 * @param sess - perun session
	 * @param vo -specific vo
	 * @return bolean
    **/
	public static boolean isVoAdminOrObserver(PerunSession sess, Vo vo) {
		return authzResolverImpl.isVoAdminOrObserver(sess, vo);
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
	 * Get all User's roles. Does not include membership and sponsorship role.
	 *
	 * @param sess perun session
	 * @param user User
	 * @return list of roles.
	 */
	public static List<String> getUserRoleNames(PerunSession sess,User user) {

		return authzResolverImpl.getRoles(user, true).getRolesNames();
	}

	/**
	 * Returns user's direct roles, can also include roles resulting from being a VALID member of authorized groups
	 * Returns also sponsorship and membership roles, which are not stored in DB as authzRoles but retrieved separately.
	 *
	 * @param sess                         perun session
	 * @param user                         user
	 * @param getAuthorizedGroupBasedRoles include roles based on membership in authorized groups
	 * @return AuthzRoles object which contains all roles with perunbeans
	 * @throws InternalErrorException
	 */
	public static AuthzRoles getUserRoles(PerunSession sess, User user, boolean getAuthorizedGroupBasedRoles) {
		AuthzRoles roles = authzResolverImpl.getRoles(user, getAuthorizedGroupBasedRoles);
		addMembershipRole(sess, roles, user);
		setAdditionalRoles(sess, roles, user);
		return roles;
	}

	/**
	 * Returns user's roles resulting from being a VALID member of authorized groups.
	 *
	 * @param sess perun session
	 * @param user user
	 * @return AuthzRoles object which contains roles with perunbeans
	 * @throws InternalErrorException
	 */
	public static AuthzRoles getRolesObtainedFromAuthorizedGroupMemberships(PerunSession sess, User user) {
		return authzResolverImpl.getRolesObtainedFromAuthorizedGroupMemberships(user);
	}

	/**
	 * Returns map of role name and map of corresponding role complementary objects (perun beans) distinguished by type.
	 * together with list of authorized groups where user is member:
	 *     Map< RoleName, Map< BeanName, Map< BeanID, List<AuthzGroup> >>>
	 *
	 * @param user
	 * @return Map<String, Map<String, Map<Integer, List<Group>>>> roles with map of complementary objects with associated authorized groups
	 */
	public static Map<String, Map<String, Map<Integer, List<Group>>>> getRoleComplementaryObjectsWithAuthorizedGroups(PerunSession sess, User user) {
		return authzResolverImpl.getRoleComplementaryObjectsWithAuthorizedGroups(user);
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

		// Create empty variable for set of roles for further fulfillment and replacement
		AuthzRoles roles = new AuthzRoles();
		// Prepare service roles like engine, service, registrar, perunAdmin etc.
		boolean serviceRole = prepareServiceRoles(sess, roles);

		// No need to search further for service principals included in 'dontlookupusers' configuration
		if (!serviceRole || !BeansUtils.getCoreConfig().getDontLookupUsers().contains(sess.getPerunPrincipal().getActor())) {
			User user = sess.getPerunPrincipal().getUser();
			if (user != null)  {
				AuthzRoles userRoles = authzResolverImpl.getRoles(user, true);
				// Add service roles, they don't have complementary objects
				roles.getRolesNames().forEach(userRoles::putAuthzRole);
				roles = userRoles;
				// Load all user's roles with all possible subgroups
				roles = addAllSubgroupsToAuthzRoles(sess, roles, Role.GROUPADMIN);
				roles = addAllSubgroupsToAuthzRoles(sess, roles, Role.GROUPOBSERVER);
				roles = addAllSubgroupsToAuthzRoles(sess, roles, Role.GROUPMEMBERSHIPMANAGER);
				// Add self role for the user
				roles.putAuthzRole(Role.SELF, user);
				// Add service user role
				if (user.isServiceUser()) {
					roles.putAuthzRole(Role.SERVICEUSER);
				}
				addMembershipRole(sess, roles, user);
			}

			setAdditionalRoles(sess, roles, user);
		}

		// Remove roles which are not allowed
		Map<String, List<String>> appAllowedRoles = BeansUtils.getCoreConfig().getAppAllowedRoles();
		for (String reg : appAllowedRoles.keySet()) {
			Pattern pattern = Pattern.compile(reg);
			if (!isBlank(sess.getPerunPrincipal().getReferer())) {
				if (pattern.matcher(sess.getPerunPrincipal().getReferer()).matches()) {
					for (String role : roles.getRolesNames()) {
						if (!appAllowedRoles.get(reg).contains(role)) {
							roles.remove(role);
						}
					}
				}
			}
		}

		sess.getPerunPrincipal().setRoles(roles);

		if (sess.getPerunClient().getType() == PerunClient.Type.OAUTH) {
			//for OAuth clients, do not allow delegating roles not allowed by scopes
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

			if (isAuthorizedByMfa(sess, false)) {
				sess.getPerunPrincipal().getRoles().putAuthzRole(Role.MFA);
			}
		}

		if (!serviceRole && (sess.getPerunPrincipal().getUser() == null || !sess.getPerunPrincipal().getUser().isServiceUser())) {
			checkMfaForHavingRole(sess, sess.getPerunPrincipal().getRoles());
		} else {
			log.debug("skipped MFA role check for {}", serviceRole ? sess.getPerunPrincipal().getActor() : sess.getPerunPrincipal().getUser());
		}

		log.trace("Refreshed roles: {}", sess.getPerunPrincipal().getRoles());
		sess.getPerunPrincipal().setAuthzInitialized(true);
	}

	/**
	 * Set additional roles that are not explicitly saved in DB. If the principal
	 * user is null, nothing is set.
	 *
	 * @param sess session
	 * @param roles roles, where the roles are added
	 * @param user user for whom to add additional roles to
	 */
	private static void setAdditionalRoles(PerunSession sess, AuthzRoles roles, User user) {
		if (user == null) {
			return;
		}

		List<Member> sponsoredMembers = perunBl.getMembersManagerBl().getSponsoredMembers(sess, user);
		for (Member sponsoredMember : sponsoredMembers) {
			roles.putAuthzRole(Role.SPONSORSHIP, sponsoredMember);
		}
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

	public static void refreshMfa(PerunSession sess) throws ExpiredTokenException, MFAuthenticationException {
		if (!BeansUtils.getCoreConfig().isEnforceMfa()) {
			throw new MFAuthenticationException("MFA enforcement is turned off");
		}

		String accessToken = sess.getPerunPrincipal().getAdditionalInformations().get(ACCESS_TOKEN);
		if (accessToken == null) {
			throw new MFAuthenticationException("Cannot verify MFA - access token is missing.");
		}

		String issuer = sess.getPerunPrincipal().getAdditionalInformations().get(ISSUER);
		if (issuer == null) {
			throw new MFAuthenticationException("Cannot verify MFA - issuer is missing.");
		}

		if (isAuthorizedByMfa(sess, true)) {
			sess.getPerunPrincipal().getRoles().putAuthzRole(Role.MFA);
		}
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

	/**
	 * Adds valid membership roles in VOs, groups, facilities and resources to roles.
	 *
	 * @param authzRoles authzRoles for the user to append membership role to
	 * @param user       user
	 * @return the same object authzRoles, which is given in the parameter, with loaded membership roles
	 */
	private static AuthzRoles addMembershipRole(PerunSession sess, AuthzRoles authzRoles, User user) {
		perunBl.getMembersManagerBl().getMembersByUser(sess, user)
			.stream()
			.filter(member -> !member.getStatus().equals(Status.DISABLED))
			.forEach(member -> authzRoles.putAuthzRole(Role.MEMBERSHIP, Vo.class, member.getVoId()));

		perunBl.getResourcesManagerBl().getResources(sess, user, List.of(Status.VALID, Status.INVALID, Status.EXPIRED), List.of(MemberGroupStatus.VALID, MemberGroupStatus.EXPIRED), List.of(GroupResourceStatus.ACTIVE))
				.forEach(resource -> {
					authzRoles.putAuthzRole(Role.MEMBERSHIP, Resource.class, resource.getId());
					authzRoles.putAuthzRole(Role.MEMBERSHIP, Facility.class, resource.getFacilityId());
				});

		perunBl.getGroupsManagerBl().getUserGroups(sess, user, List.of(Status.VALID, Status.INVALID, Status.EXPIRED), List.of(MemberGroupStatus.VALID, MemberGroupStatus.EXPIRED))
				.forEach(group -> authzRoles.putAuthzRole(Role.MEMBERSHIP, Group.class, group.getId()));

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

	/**
	 * Get all Vos where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param sess Perun session
	 * @param user for who Vos are retrieved
	 * @param roles for which Vos are retrieved
	 * @return List of Vos
	 */
	public static List<Vo> getVosWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) {
		for (String role: roles) {
			if (!roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		return new ArrayList<>(authzResolverImpl.getVosWhereUserIsInRoles(user, roles));
	}

	/**
	 * Get all Facilities where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param sess Perun session
	 * @param user for who Facilities are retrieved
	 * @param roles for which Facilities are retrieved
	 * @return List of Facilities
	 */
	public static List<Facility> getFacilitiesWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) {
		for (String role: roles) {
			if (!roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		return new ArrayList<>(authzResolverImpl.getFacilitiesWhereUserIsInRoles(user, roles));
	}

	/**
	 * Get all Resources where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param sess Perun session
	 * @param user for who Resources are retrieved
	 * @param roles for which Resources are retrieved
	 * @return List of Resources
	 */
	public static List<Resource> getResourcesWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) {
		for (String role: roles) {
			if (!roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		return new ArrayList<>(authzResolverImpl.getResourcesWhereUserIsInRoles(user, roles));
	}

	/**
	 * Check if the given group passes the user's roles filter.
	 *
	 * @param sess session
	 * @param user user
	 * @param group group
	 * @param roles list of selected roles (if empty, then return groups by all roles)
	 * @param types list of selected types of roles (if empty, then return by roles of all types)
	 * @return list of groups
	 */
	public static boolean groupMatchesUserRolesFilter(PerunSession sess, User user, Group group, List<String> roles, List<RoleAssignmentType> types) {
		return authzResolverImpl.groupMatchesUserRolesFilter(sess, user, group, roles, types);
	}

	/**
	 * Get all Groups where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * Method does not return subgroups of the fetched groups.
	 *
	 * @param sess Perun session
	 * @param user for who Groups are retrieved
	 * @param roles for which Groups are retrieved
	 * @return List of Groups
	 */
	public static List<Group> getGroupsWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) {
		for (String role: roles) {
			if (!roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		return new ArrayList<>(authzResolverImpl.getGroupsWhereUserIsInRoles(user, roles));
	}

	/**
	 * Get all Members where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param sess Perun session
	 * @param user for who Members are retrieved
	 * @param roles for which Members are retrieved
	 * @return List of Members
	 */
	public static List<Member> getMembersWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) {
		for (String role: roles) {
			if (!roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		Set<Member> members = authzResolverImpl.getMembersWhereUserIsInRoles(user, roles);

		if (roles.contains(Role.SPONSORSHIP)) {
			members.addAll(perunBl.getMembersManagerBl().getSponsoredMembers(sess, user));
		}

		return new ArrayList<>(members);
	}

	/**
	 * Get all SecurityTeams where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param sess Perun session
	 * @param user for who SecurityTeams are retrieved
	 * @param roles for which SecurityTeams are retrieved
	 * @return List of SecurityTeams
	 */
	public static List<SecurityTeam> getSecurityTeamsWhereUserIsInRoles(PerunSession sess, User user, List<String> roles) {
		for (String role: roles) {
			if (!roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		return new ArrayList<>(authzResolverImpl.getSecurityTeamsWhereUserIsInRoles(user, roles));
	}

	private static PerunBl getPerunBl() {
		return perunBl;
	}

	/**
	 * Prepare service roles (PERUNADMIN, SERVICE, RPC, ENGINE etc.)
	 *
	 * @param sess session
	 * @param roles add roles to this parameter
	 * @return true if some service role was added, false otherwise
	 */
	private static boolean prepareServiceRoles(PerunSession sess, AuthzRoles roles) {
		// Load list of perunAdmins from the configuration, split the list by the comma
		List<String> perunAdmins = BeansUtils.getCoreConfig().getAdmins();
		boolean serviceRole = false;

		// Check if the PerunPrincipal is in a group of Perun Admins
		if (perunAdmins.contains(sess.getPerunPrincipal().getActor())) {
			roles.putAuthzRole(Role.PERUNADMIN);
			// We can quit, because perun admin has all privileges
			log.trace("AuthzResolver.init: Perun Admin {} loaded", sess.getPerunPrincipal().getActor());
			return true;
		}

		String perunRpcAdmin = BeansUtils.getCoreConfig().getRpcPrincipal();
		if (sess.getPerunPrincipal().getActor().equals(perunRpcAdmin)) {
			roles.putAuthzRole(Role.RPC);
			log.trace("AuthzResolver.init: Perun RPC {} loaded", perunRpcAdmin);
			serviceRole = true;
		}

		List<String> perunEngineAdmins = BeansUtils.getCoreConfig().getEnginePrincipals();
		if (perunEngineAdmins.contains(sess.getPerunPrincipal().getActor())) {
			roles.putAuthzRole(Role.ENGINE);
			log.trace("AuthzResolver.init: Perun Engine {} loaded", perunEngineAdmins);
			serviceRole = true;
		}

		List<String> perunNotifications = BeansUtils.getCoreConfig().getNotificationPrincipals();
		if (perunNotifications.contains(sess.getPerunPrincipal().getActor())) {
			roles.putAuthzRole(Role.NOTIFICATIONS);
			log.trace("AuthzResolver.init: Perun Notifications {} loaded", perunNotifications);
			serviceRole = true;
		}

		List<String> perunRegistrars = BeansUtils.getCoreConfig().getRegistrarPrincipals();
		if (perunRegistrars.contains(sess.getPerunPrincipal().getActor())) {
			//sess.getPerunPrincipal().getRoles().putAuthzRole(Role.REGISTRAR);

			//FIXME ted pridame i roli plneho admina
			roles.putAuthzRole(Role.PERUNADMIN);

			log.trace("AuthzResolver.init: Perun Registrar {} loaded", perunRegistrars);
			serviceRole = true;
		}

		return serviceRole;
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
	 * Fetch all unique object types from the given policy collections.
	 *
	 * @param policyCollections policy collections from which the object types will be fetched.
	 * @return set of object type names occurring in the policies
	 */
	private static Set<RoleObject> fetchUniqueObjectTypes(List<AttributePolicyCollection> policyCollections) {
		List<AttributePolicy> policies = policyCollections.stream().flatMap(c -> c.getPolicies().stream()).toList();
		return policies.stream().map(AttributePolicy::getObject).collect(Collectors.toSet());
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
	 * Functional interface defining action for member-resource related objects
	 */
	@FunctionalInterface
	private interface MemberResourceRelatedObjectAction<TA extends PerunSession, TS extends Member, TM extends Resource, TV extends Set<Integer>> {
		TV callOn(TA session, TS member, TM resource) throws InternalErrorException;
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the member and resource objects.
	 */
	private enum RelatedMemberResourceObjectsResolver implements MemberResourceRelatedObjectAction<PerunSession, Member, Resource, Set<Integer>> {
		Vo((sess, member, resource) -> {
			return Collections.singleton(member.getVoId());
		}),
		Facility((sess, member, resource) -> {
			return Collections.singleton(resource.getFacilityId());
		}),
		User((sess, member, resource) -> {
			return Collections.singleton(member.getUserId());
		}),
		Group((sess, member, resource) -> {
			List<Group> groups = getPerunBl().getResourcesManagerBl().getAssociatedGroups(sess, resource, member);
			Set<Integer> ids = new HashSet<>();
			groups.forEach(group -> ids.add(group.getId()));
			return ids;
		}),
		Member((sess, member, resource) -> {
			return Collections.singleton(member.getId());
		}),
		Resource((sess, member, resource) -> {
			return Collections.singleton(resource.getId());
		}),
		SecurityTeam((sess, member, resource) -> {
			List<SecurityTeam> securityTeams = getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, member, resource) -> {
			return Collections.emptySet();
		});

		private MemberResourceRelatedObjectAction<PerunSession, Member, Resource, Set<Integer>> function;

		RelatedMemberResourceObjectsResolver(final MemberResourceRelatedObjectAction<PerunSession, Member, Resource, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedMemberResourceObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedMemberResourceObjectsResolver value.
		 */
		public static RelatedMemberResourceObjectsResolver getValue(String name) {
			try {
				return RelatedMemberResourceObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedMemberResourceObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> callOn(PerunSession sess, Member member, Resource resource) {
			return function.callOn(sess, member, resource);
		}
	}

	/**
	 * Functional interface defining action for group-resource related objects
	 */
	@FunctionalInterface
	private interface GroupResourceRelatedObjectAction<TA extends PerunSession, TS extends Group, TM extends Resource, TV extends Set<Integer>> {
		TV callOn(TA session, TS group, TM resource) throws InternalErrorException;
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the group and resource objects.
	 */
	private enum RelatedGroupResourceObjectsResolver implements GroupResourceRelatedObjectAction<PerunSession, Group, Resource, Set<Integer>> {
		Vo((sess, group, resource) -> {
			return Collections.singleton(resource.getVoId());
		}),
		Facility((sess, group, resource) -> {
			return Collections.singleton(resource.getFacilityId());
		}),
		User((sess, group, resource) -> {
			List<User> users = perunBl.getGroupsManagerBl().getGroupUsers(sess, group);
			Set<Integer> userIds = new HashSet<>();
			users.forEach(user -> userIds.add(user.getId()));
			return userIds;
		}),
		Group((sess, group, resource) -> {
			return Collections.singleton(group.getId());
		}),
		Member((sess, group, resource) -> {
			List<Member> members = perunBl.getGroupsManagerBl().getGroupMembers(sess, group);
			Set<Integer> memberIds = new HashSet<>();
			members.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, group, resource) -> {
			return Collections.singleton(resource.getId());
		}),
		SecurityTeam((sess, group, resource) -> {
			List<SecurityTeam> securityTeams = getPerunBl().getFacilitiesManagerBl().
				getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, group, resource) -> {
			return Collections.emptySet();
		});

		private GroupResourceRelatedObjectAction<PerunSession, Group, Resource, Set<Integer>> function;

		RelatedGroupResourceObjectsResolver(final GroupResourceRelatedObjectAction<PerunSession, Group, Resource, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedGroupResourceObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedGroupResourceObjectsResolver value.
		 */
		public static RelatedGroupResourceObjectsResolver getValue(String name) {
			try {
				return RelatedGroupResourceObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedGroupResourceObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> callOn(PerunSession sess, Group group, Resource resource) {
			return function.callOn(sess, group, resource);
		}
	}

	/**
	 * Functional interface defining action for user-facility related objects
	 */
	@FunctionalInterface
	private interface UserFacilityRelatedObjectAction<TA extends PerunSession, TS extends User, TM extends Facility, TV extends Set<Integer>> {
		TV callOn(TA session, TS user, TM facility) throws InternalErrorException;
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the user and facility objects.
	 */
	private enum RelatedUserFacilityObjectsResolver implements UserFacilityRelatedObjectAction<PerunSession, User, Facility, Set<Integer>> {
		Vo((sess, user, facility) -> {
			List<Resource> resources = perunBl.getUsersManagerBl().getAssociatedResources(sess, facility, user);
			Set<Integer> voIds = new HashSet<>();
			resources.forEach(resource -> voIds.add(resource.getVoId()));
			return voIds;
		}),
		Facility((sess, user, facility) -> {
			return Collections.singleton(facility.getId());
		}),
		User((sess, user, facility) -> {
			return Collections.singleton(user.getId());
		}),
		Group((sess, user, facility) -> {
			List<Group> userGroups = getPerunBl().getGroupsManagerBl().getUserGroups(sess, user);
			List<Group> facilityGroups = getPerunBl().getGroupsManagerBl().getAssociatedGroupsToFacility(sess, facility);
			userGroups.retainAll(facilityGroups);
			Set<Integer> groupIds = new HashSet<>();
			userGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, user, facility) -> {
			List<Member> membersFromUser = getPerunBl().getFacilitiesManagerBl().getAssociatedMembers(sess, facility, user);
			Set<Integer> memberIds = new HashSet<>();
			membersFromUser.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, user, facility) -> {
			List<Resource> resources = perunBl.getUsersManagerBl().getAssociatedResources(sess, facility, user);
			Set<Integer> resourceIds = new HashSet<>();
			resources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, user, facility) -> {
			List<SecurityTeam> securityTeams = getPerunBl().getFacilitiesManagerBl().getAssignedSecurityTeams(sess, facility);
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, user, facility) -> {
			return Collections.emptySet();
		});

		private UserFacilityRelatedObjectAction<PerunSession, User, Facility, Set<Integer>> function;

		RelatedUserFacilityObjectsResolver(final UserFacilityRelatedObjectAction<PerunSession, User, Facility, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedUserFacilityObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedUserFacilityObjectsResolver value.
		 */
		public static RelatedUserFacilityObjectsResolver getValue(String name) {
			try {
				return RelatedUserFacilityObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedUserFacilityObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> callOn(PerunSession sess, User user, Facility facility) {
			return function.callOn(sess, user, facility);
		}
	}

	/**
	 * Functional interface defining action for member-group related objects
	 */
	@FunctionalInterface
	private interface MemberGroupRelatedObjectAction<TA extends PerunSession, TS extends Member, TM extends Group, TV extends Set<Integer>> {
		TV callOn(TA session, TS member, TM group) throws InternalErrorException;
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the member and group objects.
	 */
	private enum RelatedMemberGroupObjectsResolver implements MemberGroupRelatedObjectAction<PerunSession, Member, Group, Set<Integer>> {
		Vo((sess, member, group) -> {
			return Collections.singleton(member.getVoId());
		}),
		Facility((sess, member, group) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, group);
			Set<Integer> facilityIds = new HashSet<>();
			resources.forEach(resource -> facilityIds.add(resource.getFacilityId()));
			return facilityIds;
		}),
		User((sess, member, group) -> {
			return Collections.singleton(member.getUserId());
		}),
		Group((sess, member, group) -> {
			return Collections.singleton(group.getId());
		}),
		Member((sess, member, group) -> {
			return Collections.singleton(member.getId());
		}),
		Resource((sess, member, group) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, group);
			Set<Integer> resourceIds = new HashSet<>();
			resources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, member, group) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, group);
			List<SecurityTeam> securityTeams = new ArrayList<>();
			resources.forEach(resource -> securityTeams.addAll(getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource))));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, member, group) -> {
			return Collections.emptySet();
		});

		private MemberGroupRelatedObjectAction<PerunSession, Member, Group, Set<Integer>> function;

		RelatedMemberGroupObjectsResolver(final MemberGroupRelatedObjectAction<PerunSession, Member, Group, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedMemberGroupObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedMemberGroupObjectsResolver value.
		 */
		public static RelatedMemberGroupObjectsResolver getValue(String name) {
			try {
				return RelatedMemberGroupObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedMemberGroupObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> callOn(PerunSession sess, Member member, Group group) {
			return function.callOn(sess, member, group);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the User object.
	 */
	private enum RelatedUserObjectsResolver implements BiFunction<PerunSession, User, Set<Integer>> {
		Vo((sess, user) -> {
			List<Vo> vosFromUser = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
			Set<Integer> voIds = new HashSet<>();
			vosFromUser.forEach(vo -> voIds.add(vo.getId()));
			return voIds;
		}),
		Facility((sess, user) -> {
			List<Resource> resources = getPerunBl().getUsersManagerBl().getAssociatedResources(sess, user);
			Set<Integer> facilityIds = new HashSet<>();
			resources.forEach(resource -> facilityIds.add(resource.getFacilityId()));
			return facilityIds;
		}),
		User((sess, user) -> {
			return Collections.singleton(user.getId());
		}),
		Group((sess, user) -> {
			List<Group> userGroups = getPerunBl().getGroupsManagerBl().getUserGroups(sess, user);
			Set<Integer> groupIds = new HashSet<>();
			userGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, user) -> {
			List<Member> userMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
			Set<Integer> memberIds = new HashSet<>();
			userMembers.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, user) -> {
			List<Resource> userResources = getPerunBl().getUsersManagerBl().getAssociatedResources(sess, user);
			Set<Integer> resourceIds = new HashSet<>();
			userResources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, user) -> {
			List<Resource> resources = getPerunBl().getUsersManagerBl().getAssociatedResources(sess, user);
			List<SecurityTeam> securityTeams = new ArrayList<>();
			resources.forEach(resource -> securityTeams.addAll(getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource))));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, user) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, User, Set<Integer>> function;

		RelatedUserObjectsResolver(final BiFunction<PerunSession, User, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedUserObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedUserObjectsResolver value.
		 */
		public static RelatedUserObjectsResolver getValue(String name) {
			try {
				return RelatedUserObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedUserObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, User user) {
			return function.apply(sess, user);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the member object.
	 */
	private enum RelatedMemberObjectsResolver implements BiFunction<PerunSession, Member, Set<Integer>> {
		Vo((sess, member) -> {
			return Collections.singleton(member.getVoId());
		}),
		Facility((sess, member) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, member);
			Set<Integer> facilityIds = new HashSet<>();
			resources.forEach(resource -> facilityIds.add(resource.getFacilityId()));
			return facilityIds;
		}),
		User((sess, member) -> {
			return Collections.singleton(member.getUserId());
		}),
		Group((sess, member) -> {
			List<Group> memberGroups = getPerunBl().getGroupsManagerBl().getGroupsByPerunBean(sess, member);
			Set<Integer> groupIds = new HashSet<>();
			memberGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, member) -> {
			return Collections.singleton(member.getId());
		}),
		Resource((sess, member) -> {
			List<Resource> memberResources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, member);
			Set<Integer> resourceIds = new HashSet<>();
			memberResources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, member) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, member);
			List<SecurityTeam> securityTeams = new ArrayList<>();
			resources.forEach(resource -> securityTeams.addAll(getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource))));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, member) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, Member, Set<Integer>> function;

		RelatedMemberObjectsResolver(final BiFunction<PerunSession, Member, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedMemberObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedMemberObjectsResolver value.
		 */
		public static RelatedMemberObjectsResolver getValue(String name) {
			try {
				return RelatedMemberObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedMemberObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, Member member) {
			return function.apply(sess, member);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the Vo object.
	 */
	private enum RelatedVoObjectsResolver implements BiFunction<PerunSession, Vo, Set<Integer>> {
		Vo((sess, vo) -> {
			return Collections.singleton(vo.getId());
		}),
		Facility((sess, vo) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			Set<Integer> facilityIds = new HashSet<>();
			resources.forEach(resource -> facilityIds.add(resource.getFacilityId()));
			return facilityIds;
		}),
		User((sess, vo) -> {
			List<Member> voMembers = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
			Set<Integer> userIds = new HashSet<>();
			voMembers.forEach(member -> userIds.add(member.getId()));
			return userIds;
		}),
		Group((sess, vo) -> {
			List<Group> memberGroups = getPerunBl().getGroupsManagerBl().getGroups(sess, vo);
			Set<Integer> groupIds = new HashSet<>();
			memberGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, vo) -> {
			List<Member> voMembers = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
			Set<Integer> memberIds = new HashSet<>();
			voMembers.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, vo) -> {
			List<Resource> voResources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			Set<Integer> resourceIds = new HashSet<>();
			voResources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, vo) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			List<SecurityTeam> securityTeams = new ArrayList<>();
			resources.forEach(resource -> securityTeams.addAll(getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource))));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, vo) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, Vo, Set<Integer>> function;

		RelatedVoObjectsResolver(final BiFunction<PerunSession, Vo, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedVoObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedVoObjectsResolver value.
		 */
		public static RelatedVoObjectsResolver getValue(String name) {
			try {
				return RelatedVoObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedVoObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, Vo vo) {
			return function.apply(sess, vo);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the Group object.
	 */
	private enum RelatedGroupObjectsResolver implements BiFunction<PerunSession, Group, Set<Integer>> {
		Vo((sess, group) -> {
			return Collections.singleton(group.getVoId());
		}),
		Facility((sess, group) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, group);
			Set<Integer> facilityIds = new HashSet<>();
			resources.forEach(resource -> facilityIds.add(resource.getFacilityId()));
			return facilityIds;
		}),
		User((sess, group) -> {
			List<User> groupUsers = getPerunBl().getGroupsManagerBl().getGroupUsers(sess, group);
			Set<Integer> userIds = new HashSet<>();
			groupUsers.forEach(user -> userIds.add(user.getId()));
			return userIds;
		}),
		Group((sess, group) -> {
			return Collections.singleton(group.getId());
		}),
		Member((sess, group) -> {
			List<Member> groupMembers = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
			Set<Integer> memberIds = new HashSet<>();
			groupMembers.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, group) -> {
			List<Resource> groupResources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, group);
			Set<Integer> resourceIds = new HashSet<>();
			groupResources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, group) -> {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssociatedResources(sess, group);
			List<SecurityTeam> securityTeams = new ArrayList<>();
			resources.forEach(resource -> securityTeams.addAll(getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource))));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, group) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, Group, Set<Integer>> function;

		RelatedGroupObjectsResolver(final BiFunction<PerunSession, Group, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedGroupObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedGroupObjectsResolver value.
		 */
		public static RelatedGroupObjectsResolver getValue(String name) {
			try {
				return RelatedGroupObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedGroupObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, Group group) {
			return function.apply(sess, group);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the resource object.
	 */
	private enum RelatedResourceObjectsResolver implements BiFunction<PerunSession, Resource, Set<Integer>> {
		Vo((sess, resource) -> {
			return Collections.singleton(resource.getVoId());
		}),
		Facility((sess, resource) -> {
			return Collections.singleton(resource.getFacilityId());
		}),
		User((sess, resource) -> {
			List<User> resourceUsers = getPerunBl().getResourcesManagerBl().getAssociatedUsers(sess, resource);
			Set<Integer> userIds = new HashSet<>();
			resourceUsers.forEach(user -> userIds.add(user.getId()));
			return userIds;
		}),
		Group((sess, resource) -> {
			List<Group> resourceGroups = getPerunBl().getGroupsManagerBl().getAssociatedGroupsToResource(sess, resource);
			Set<Integer> groupIds = new HashSet<>();
			resourceGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, resource) -> {
			List<Member> resourceMembers = getPerunBl().getResourcesManagerBl().getAssociatedMembers(sess, resource);
			Set<Integer> memberIds = new HashSet<>();
			resourceMembers.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, resource) -> {
			return Collections.singleton(resource.getId());
		}),
		SecurityTeam((sess, resource) -> {
			List<SecurityTeam> securityTeams = getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, resource) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, Resource, Set<Integer>> function;

		RelatedResourceObjectsResolver(final BiFunction<PerunSession, Resource, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedResourceObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedResourceObjectsResolver value.
		 */
		public static RelatedResourceObjectsResolver getValue(String name) {
			try {
				return RelatedResourceObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedResourceObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, Resource resource) {
			return function.apply(sess, resource);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the Facility object.
	 */
	private enum RelatedFacilityObjectsResolver implements BiFunction<PerunSession, Facility, Set<Integer>> {
		Vo((sess, facility) -> {
			List<Vo> vosFromMember = getPerunBl().getVosManagerBl().getVosByPerunBean(sess, facility);
			Set<Integer> voIds = new HashSet<>();
			vosFromMember.forEach(vo -> voIds.add(vo.getId()));
			return voIds;
		}),
		Facility((sess, facility) -> {
			return Collections.singleton(facility.getId());
		}),
		User((sess, facility) -> {
			List<User> resourceUsers = getPerunBl().getFacilitiesManagerBl().getAssociatedUsers(sess, facility);
			Set<Integer> userIds = new HashSet<>();
			resourceUsers.forEach(user -> userIds.add(user.getId()));
			return userIds;
		}),
		Group((sess, facility) -> {
			List<Group> resourceGroups = getPerunBl().getGroupsManagerBl().getAssociatedGroupsToFacility(sess, facility);
			Set<Integer> groupIds = new HashSet<>();
			resourceGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, facility) -> {
			List<Resource> facilityResources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			List<Member> resourceMembers = new ArrayList<>();
			facilityResources.forEach(resource -> resourceMembers.addAll(getPerunBl().getResourcesManagerBl().getAssociatedMembers(sess, resource)));
			Set<Integer> memberIds = new HashSet<>();
			resourceMembers.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, facility) -> {
			List<Resource> facilityResources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			Set<Integer> resourceIds = new HashSet<>();
			facilityResources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, facility) -> {
			List<SecurityTeam> securityTeams = getPerunBl().getFacilitiesManagerBl().getAssignedSecurityTeams(sess, facility);
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, facility) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, Facility, Set<Integer>> function;

		RelatedFacilityObjectsResolver(final BiFunction<PerunSession, Facility, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedFacilityObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedFacilityObjectsResolver value.
		 */
		public static RelatedFacilityObjectsResolver getValue(String name) {
			try {
				return RelatedFacilityObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedFacilityObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, Facility facility) {
			return function.apply(sess, facility);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the Host object.
	 */
	private enum RelatedHostObjectsResolver implements BiFunction<PerunSession, Host, Set<Integer>> {
		Vo((sess, host) -> {
			Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			List<Vo> vosFromMember = getPerunBl().getVosManagerBl().getVosByPerunBean(sess, facility);
			Set<Integer> voIds = new HashSet<>();
			vosFromMember.forEach(vo -> voIds.add(vo.getId()));
			return voIds;
		}),
		Facility((sess, host) -> {
			Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			return Collections.singleton(facility.getId());
		}),
		User((sess, host) -> {
			Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			List<User> resourceUsers = getPerunBl().getFacilitiesManagerBl().getAssociatedUsers(sess, facility);
			Set<Integer> userIds = new HashSet<>();
			resourceUsers.forEach(user -> userIds.add(user.getId()));
			return userIds;
		}),
		Group((sess, host) -> {
			Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			List<Group> resourceGroups = getPerunBl().getGroupsManagerBl().getAssociatedGroupsToFacility(sess, facility);
			Set<Integer> groupIds = new HashSet<>();
			resourceGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, host) -> {
			Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			List<Resource> facilityResources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			List<Member> resourceMembers = new ArrayList<>();
			facilityResources.forEach(resource -> resourceMembers.addAll(getPerunBl().getResourcesManagerBl().getAssociatedMembers(sess, resource)));
			Set<Integer> memberIds = new HashSet<>();
			resourceMembers.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, host) -> {
			Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			List<Resource> facilityResources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			Set<Integer> resourceIds = new HashSet<>();
			facilityResources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, host) -> {
			List<SecurityTeam> securityTeams = getPerunBl().getFacilitiesManagerBl()
				.getAssignedSecurityTeams(sess, getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, host) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, Host, Set<Integer>> function;

		RelatedHostObjectsResolver(final BiFunction<PerunSession, Host, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedHostObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedHostObjectsResolver value.
		 */
		public static RelatedHostObjectsResolver getValue(String name) {
			try {
				return RelatedHostObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedHostObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, Host host) {
			return function.apply(sess, host);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the UserExtSource object.
	 */
	private enum RelatedUserExtSourceObjectsResolver implements BiFunction<PerunSession, UserExtSource, Set<Integer>> {
		Vo((sess, ues) -> {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				log.warn("User not exists for the userExtSource: " + ues);
				return Collections.emptySet();
			}
			List<Vo> vosFromUser = getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user);
			Set<Integer> voIds = new HashSet<>();
			vosFromUser.forEach(vo -> voIds.add(vo.getId()));
			return voIds;
		}),
		Facility((sess, ues) -> {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				log.warn("User not exists for the userExtSource: " + ues);
				return Collections.emptySet();
			}
			List<Resource> resources = getPerunBl().getUsersManagerBl().getAssociatedResources(sess, user);
			Set<Integer> facilityIds = new HashSet<>();
			resources.forEach(resource -> facilityIds.add(resource.getFacilityId()));
			return facilityIds;
		}),
		User((sess, ues) -> {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				log.warn("User not exists for the userExtSource: " + ues);
				return Collections.emptySet();
			}
			return Collections.singleton(user.getId());
		}),
		Group((sess, ues) -> {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				log.warn("User not exists for the userExtSource: " + ues);
				return Collections.emptySet();
			}
			List<Group> userGroups = getPerunBl().getGroupsManagerBl().getUserGroups(sess, user);
			Set<Integer> groupIds = new HashSet<>();
			userGroups.forEach(group -> groupIds.add(group.getId()));
			return groupIds;
		}),
		Member((sess, ues) -> {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				log.warn("User not exists for the userExtSource: " + ues);
				return Collections.emptySet();
			}
			List<Member> userMembers = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
			Set<Integer> memberIds = new HashSet<>();
			userMembers.forEach(member -> memberIds.add(member.getId()));
			return memberIds;
		}),
		Resource((sess, ues) -> {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				log.warn("User not exists for the userExtSource: " + ues);
				return Collections.emptySet();
			}
			List<Resource> userResources = getPerunBl().getUsersManagerBl().getAssociatedResources(sess, user);
			Set<Integer> resourceIds = new HashSet<>();
			userResources.forEach(resource -> resourceIds.add(resource.getId()));
			return resourceIds;
		}),
		SecurityTeam((sess, ues) -> {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
			} catch (UserNotExistsException e) {
				log.warn("User not exists for the userExtSource: " + ues);
				return Collections.emptySet();
			}
			List<Resource> resources = getPerunBl().getUsersManagerBl().getAssociatedResources(sess, user);
			List<SecurityTeam> securityTeams = new ArrayList<>();
			resources.forEach(resource -> securityTeams.addAll(getPerunBl().getFacilitiesManagerBl().
				getAssignedSecurityTeams(sess, getPerunBl().getResourcesManagerBl().getFacility(sess, resource))));
			Set<Integer> ids = new HashSet<>();
			securityTeams.forEach(team -> ids.add(team.getId()));
			return ids;
		}),
		Default((sess, ues) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, UserExtSource, Set<Integer>> function;

		RelatedUserExtSourceObjectsResolver(final BiFunction<PerunSession, UserExtSource, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedUserExtSourceObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedUserExtSourceObjectsResolver value.
		 */
		public static RelatedUserExtSourceObjectsResolver getValue(String name) {
			try {
				return RelatedUserExtSourceObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedUserExtSourceObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, UserExtSource ues) {
			return function.apply(sess, ues);
		}
	}

	/**
	 * Enum defines PerunBean's name and action. The action retrieves all related objects of that name for the Entityless object.
	 */
	private enum RelatedEntitylessObjectsResolver implements BiFunction<PerunSession, String, Set<Integer>> {
		Default((sess, key) -> {
			return Collections.emptySet();
		});

		private BiFunction<PerunSession, String, Set<Integer>> function;

		RelatedEntitylessObjectsResolver(final BiFunction<PerunSession, String, Set<Integer>> function) {
			this.function = function;
		}

		/**
		 * Get RelatedEntitylessObjectsResolver value by the given name or default value if the name does not exist.
		 *
		 * @param name of the value which will be retrieved if exists.
		 * @return RelatedEntitylessObjectsResolver value.
		 */
		public static RelatedEntitylessObjectsResolver getValue(String name) {
			try {
				return RelatedEntitylessObjectsResolver.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return RelatedEntitylessObjectsResolver.Default;
			}
		}

		@Override
		public Set<Integer> apply(PerunSession sess, String key) {
			return function.apply(sess, key);
		}
	}

	/**
	 * Checks whether the given parameters satisfies the rules associated with the role.
	 *
	 * @param entityToManage to which will be the role set, unset or read
	 * @param complementaryObject which will be bounded with the role
	 * @param role which will be managed
	 * @return true if all given parameters imply with the associated rule, false otherwise.
	 */
	private static boolean objectAndRoleManageableByEntity(String entityToManage, PerunBean complementaryObject, String role) {
		RoleManagementRules rules;
		try {
			rules = AuthzResolverImpl.getRoleManagementRules(role);
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		Set<String> necessaryObjects = rules.getAssignedObjects().keySet();

		if (rules.getEntitiesToManage().containsKey(entityToManage)) {
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
	 * Checks if setting role requires MFA based on configuration in perun-roles, throws MfaPrivilegeException if requirements unmet.
	 * @param assigningEntity user or group to which the role will be assigned (User)
	 * @param complementaryObject which will be bounded with the role (Vo)
	 * @param role role name (VOADMIN)
	 * @throws MfaPrivilegeException if MFA requirements are not met
	 */
	private static void checkMfaForSettingRole(PerunSession sess, PerunBean assigningEntity, PerunBean complementaryObject, String role) {
		RoleManagementRules rules;
		try {
			rules = AuthzResolverImpl.getRoleManagementRules(role);
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		List<Map<String, String>> mfaPolicies = rules.getAssignmentCheck();
		Map<String, Set<Integer>> mapOfBeans = new HashMap<>();

		if (mfaPolicies != null && !mfaPolicies.isEmpty() && complementaryObject != null) {
			mapOfBeans = fetchAllRelatedObjects(Arrays.asList(complementaryObject));
		}

		// check complementary objects for MFA requirements
		if (!mfaAuthorized(sess, mfaPolicies, mapOfBeans)) {
			if (complementaryObject == null) {
				throw new MfaPrivilegeException("Multi-Factor authentication is required to set this role.");
			}
			String message = complementaryObject.getBeanName() != null ? complementaryObject.getBeanName() : "object";
			message += " with id " + complementaryObject.getId() + " or related object is critical.";
			throw new MfaPrivilegeException("Multi-Factor authentication is required - " + message);
		}

		// check assigning entity for MFA requirements
		try {
			if (BeansUtils.getCoreConfig().isEnforceMfa() && isAnyObjectMfaCritical(sess, Arrays.asList(assigningEntity))
			 && !sess.getPerunPrincipal().getRoles().hasRole(Role.MFA) && !hasMFASkippableRole(sess)) {
				throw new MfaPrivilegeException("Multi-Factor authentication is required - assigning entity is critical.");
			}
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Error checking system roles", e);
		}
	}

	/**
	 * Checks if having a role requires MFA based on configuration in perun-roles.
	 * Throws uncatched exceptions if MFA roles are inconsistent or MFA requirements are not met.
	 * @param roles roles to be set to principal
	 * @throws MfaRolePrivilegeException if MFA requirements are not met
	 * @throws MfaInvalidRolesException if principal has roles both requiring and skipping MFA check
	 */
	private static void checkMfaForHavingRole(PerunSession sess, AuthzRoles roles) {
		if (!BeansUtils.getCoreConfig().isEnforceMfa()) {
			return;
		}

		RoleManagementRules rules;
		List<String> skipMfaRoles = new ArrayList<>();
		List<String> requireMfaRoles = new ArrayList<>();
		for (String role : roles.keySet()) {
			try {
				rules = AuthzResolverImpl.getRoleManagementRules(role);
			} catch (RoleManagementRulesNotExistsException e) {
				throw new InternalErrorException("Management rules not exist for the role " + role, e);
			}
			if (rules.isMfaCriticalRole()) {
				requireMfaRoles.add(rules.getRoleName());
			}
			if (rules.shouldSkipMFA()) {
				skipMfaRoles.add(rules.getRoleName());
			}
		}

		if (!skipMfaRoles.isEmpty() && !requireMfaRoles.isEmpty()) {
			throw new MfaInvalidRolesException(sess, requireMfaRoles, skipMfaRoles);
		}

		if (!requireMfaRoles.isEmpty() && !sess.getPerunPrincipal().getRoles().hasRole(Role.MFA)) {
			if (checkAuthValidityForMFA(sess)) {
				throw new MfaRolePrivilegeException(sess, requireMfaRoles.get(0));
			} else {
				throw new MfaRoleTimeoutException("Your MFA timestamp is not valid anymore, you'll need to reauthenticate");
			}
		}
	}

	/**
	 * Create a mapping of column names and ids which will be used for setting or unsetting of the role.
	 *
	 * @param entityToManage to which will be the role set or unset
	 * @param complementaryObject which will be bounded with the role
	 * @param role which will be managed
	 * @return final mapping of values
	 */
	private static Map<String, Integer> createMappingToManageRole(PerunBean entityToManage, PerunBean complementaryObject, String role) {
		RoleManagementRules rules;
		try {
			rules = AuthzResolverImpl.getRoleManagementRules(role);
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		Map<String, Integer> mapping = createMappingOfValues(complementaryObject, role, rules);
		mapping.put(rules.getEntitiesToManage().get(entityToManage.getBeanName()), entityToManage.getId());

		return mapping;
	}

	/**
	 * Create a mapping of column names and ids which will be used for reading the role.
	 *
	 * @param complementaryObject which will be bounded with the role
	 * @param role which will be managed
	 * @return final mapping of values
	 */
	private static Map<String, Integer> createMappingToReadRoleOnObject(PerunBean complementaryObject, String role) {
		RoleManagementRules rules;
		try {
			rules = AuthzResolverImpl.getRoleManagementRules(role);
		} catch (RoleManagementRulesNotExistsException e) {
			throw new InternalErrorException("Management rules not exist for the role " + role, e);
		}

		return createMappingOfValues(complementaryObject, role, rules);
	}

	/**
	 * Create a mapping of column names and ids which will be used to read or manage the role.
	 *
	 * @param complementaryObject which will be bounded with the role
	 * @param role which will be managed
	 * @return final mapping of values
	 */
	private static Map<String, Integer> createMappingOfValues(PerunBean complementaryObject, String role, RoleManagementRules rules) {
		Map<String, Integer> mapping = new HashMap<>();

		Integer role_id = authzResolverImpl.getRoleId(role);
		mapping.put("role_id", role_id);

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

	/**
	 * Checks, if principal was authorized by Multi-factor authentication.
	 * The information is resolved from headers (apache IntrospectionEndpoint call) and stored in principal's additionalInformations.
	 * Check if the auth time + mfa timeout is not older than the current time
	 * auth time = time of the first authentication
	 * mfa timeout = amount of time defined in the config for how long the MFA should be valid (since SFA)
	 *
	 * @param sess session
	 * @param throwError if this method should throw errors or just return boolean
	 * @return true if principal authorized by MFA in allowed limit, false otherwise
	 */
	private static boolean isAuthorizedByMfa(PerunSession sess, boolean throwError) {
		if (!BeansUtils.getCoreConfig().isEnforceMfa()) {
			return false;
		}

		if (checkAuthValidityForMFA(sess)) {
			// true if user has MFA and it is still valid
			return sessionHasMfa(sess);
		} else {
			if (!throwError) return false;
			if (sessionHasMfa(sess)) {
				// MFA is no longer valid
				throw new MfaTimeoutException("Your MFA timestamp is not valid anymore, you'll need to reauthenticate");
			} else {
				// user is authenticated by SFA but the mfa timeout would cause an error, so we need to reauthenticate this user
				throw new MfaTimeoutException("Your single factor authentication timestamp is not valid anymore, you'll need to reauthenticate");
			}
		}
	}

	/**
	 * Check if the auth time + mfa timeout (reduced by percentage from config) > the current time
	 * @param sess session
	 * @return true if the auth timestamp is not too old to perform step-up
	 */
	private static boolean checkAuthValidityForMFA(PerunSession sess) {
		String returnedAuthTime = sess.getPerunPrincipal().getAdditionalInformations().get(AUTH_TIME);
		Instant parsedReturnedAuthTime;
		try {
			parsedReturnedAuthTime = Instant.parse(returnedAuthTime);
		} catch (DateTimeParseException e) {
			throw new InternalErrorException("MFA timestamp "  + returnedAuthTime + " could not be parsed", e);
		}
		if (parsedReturnedAuthTime.isAfter(Instant.now())) {
			throw new InternalErrorException("MFA auth timestamp " + returnedAuthTime + " was greater than current time");
		}

		long mfaTimeoutInSec = Duration.ofMinutes(BeansUtils.getCoreConfig().getMfaAuthTimeout()).getSeconds();
		double mfaTimeoutPercentage = 1;
		// if the current session is SFA, we want to force log in with both factors earlier (e.g. 75% of mfaAuthTimeout) due to the first executed MFA since authentication time
		// -> we want to avoid situation when the validity is e.g. 60 minutes, user executes MFA (just second factor) after 59 minutes and after one minute he/she would need to log in again with both factors
		if (!sessionHasMfa(sess)) {
			mfaTimeoutPercentage = (double) BeansUtils.getCoreConfig().getMfaAuthTimeoutPercentageForceLogIn() / 100;
			if (mfaTimeoutPercentage < 0 || mfaTimeoutPercentage > 1) {
				throw new InternalErrorException("MFA auth timestamp percentage force logout " + mfaTimeoutPercentage + " is not between 0 and 100");
			}
		}

		Instant mfaValidUntil = parsedReturnedAuthTime.plusSeconds((long) (mfaTimeoutInSec * mfaTimeoutPercentage));

		return mfaValidUntil.isAfter(Instant.now());
	}

	/**
	 * Check if the perun principal contains acr_mfa. It means that user has been authenticated by MFA.
	 * @param sess session
	 * @return true if principal contains acr_mfa
	 */
	private static boolean sessionHasMfa(PerunSession sess) {
		return sess.getPerunPrincipal().getAdditionalInformations().containsKey(ACR_MFA);
	}

	/**
	 * Return id of the role by its name.
	 *
	 * @param name - name of the role
	 * @return - id of the role
	 */
	public static int getRoleIdByName(String name) {
		return authzResolverImpl.getRoleIdByName(name);
	}
}
