package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RoleAssignmentType;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RoleAlreadySetException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSetException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.impl.AuthzRoles;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents AuthzResolver methods.
 *
 * @author Michal Prochazka
 */

public interface AuthzResolverImplApi {

	/**
	 * Returns user's direct roles, can also include roles resulting from being a VALID member of authorized groups
	 *
	 * @param user
	 * @param getAuthorizedGroupBasedRoles
	 * @return AuthzRoles object which contains all roles with perunbeans
	 */
	AuthzRoles getRoles(User user, boolean getAuthorizedGroupBasedRoles);

	/**
	 * Returns user's roles resulting from being a VALID member of authorized groups
	 *
	 * @param user user
	 * @return AuthzRoles object which contains roles with perunbeans
	 */
	AuthzRoles getRolesObtainedFromAuthorizedGroupMemberships(User user);

	/**
	 * Returns map of role name and map of corresponding role complementary objects (perun beans) distinguished by type.
	 * 	 * together with list of authorized groups where user is member:
	 * 	 *     Map< RoleName, Map< BeanName, Map< BeanID, List<AuthzGroup> >>>
	 *
	 * @param user
	 * @return Map<String, Map<String, Map<Integer, List<Group>>>> complementary objects with associated authorized groups
	 */
	Map<String, Map<String, Map<Integer, List<Group>>>> getRoleComplementaryObjectsWithAuthorizedGroups(User user);

	/**
	 * Returns all group's roles.
	 *
	 * @param group
	 * @return AuthzRoles object which contains all roles with perunbeans
	 */
	AuthzRoles getRoles(Group group);

	/**
	 * Removes all authz entries for the sponsoredUser.
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @throws InternalErrorException
	 */
	void removeAllSponsoredUserAuthz(PerunSession sess, User sponsoredUser);

	/**
	 * Removes all authz entries for the user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void removeAllUserAuthz(PerunSession sess, User user);

	/**
	 * Removes all authz entries for the vo
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForVo(PerunSession sess, Vo vo);

	/**
	 * Removes all authz entries for the group
	 *
	 * @param sess
	 * @param group
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForGroup(PerunSession sess, Group group);

	/**
	 * Removes all authz entries for the facility
	 *
	 * @param sess
	 * @param facility
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForFacility(PerunSession sess, Facility facility);

	/**
	 * Removes all authz entries for the resource
	 *
	 * @param sess
	 * @param resource
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForResource(PerunSession sess, Resource resource);

	/**
	 * Removes all authz entries for the service
	 *
	 * @param sess
	 * @param service
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForService(PerunSession sess, Service service);

	/**
	 * Removes all authz entries for the securityTeam
	 *
	 * @param sess
	 * @param securityTeam
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForSecurityTeam(PerunSession sess, SecurityTeam securityTeam);

	/**
	 * Add user role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Facility facility, User user) throws AlreadyAdminException;

	/**
	 * Add group of users role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Facility facility, Group group) throws AlreadyAdminException;

	/**
	 * Remove user role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, Facility facility, User user) throws UserNotAdminException;

	/**
	 * Remove group of users role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, Facility facility, Group group) throws GroupNotAdminException;

	/**
	 * Add user role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Resource resource, User user) throws AlreadyAdminException;

	/**
	 * Add group of users role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Resource resource, Group group) throws AlreadyAdminException;

	/**
	 * Remove user role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, Resource resource, User user) throws UserNotAdminException;

	/**
	 * Remove group of users role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, Resource resource, Group group) throws GroupNotAdminException;

	/**
	 * Add user role admin for the sponsored user
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, User sponsoredUser, User user) throws AlreadyAdminException;

	/**
	 * Add group of users role admin for the sponsored user
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, User sponsoredUser, Group group) throws AlreadyAdminException;

	/**
	 * Remove user role admin for the sponsoredUser
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, User sponsoredUser, User user) throws UserNotAdminException;

	/**
	 * Remove group of users role admin for the sponsoredUser
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, User sponsoredUser, Group group) throws GroupNotAdminException;

	/**
	 * Add user role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Group group, User user) throws AlreadyAdminException;

	/**
	 * Add group of users role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param authorizedGroup
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws AlreadyAdminException;

	/**
	 * Remove user role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, Group group, User user) throws UserNotAdminException;

	/**
	 * Remove group of users role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param authorizedGroup
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws GroupNotAdminException;

	void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException;

	void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws AlreadyAdminException;

	void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException;

	void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws GroupNotAdminException;

	/**
	 * Make user to be perunAdmin
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void makeUserPerunAdmin(PerunSession sess, User user) throws AlreadyAdminException;

	/**
	 * Make user Perun observer
	 *
	 * @param sess the perunSession
	 * @param user user to be promoted to perunObserver
	 * @throws InternalErrorException
	 */
	void makeUserPerunObserver(PerunSession sess, User user) throws AlreadyAdminException;

	/**
	 * Make group Perun observer
	 *
	 * @param sess the perunSession
	 * @param authorizedGroup authorizedGroup to be promoted to perunObserver
	 * @throws InternalErrorException
	 */
	void makeAuthorizedGroupPerunObserver(PerunSession sess, Group authorizedGroup) throws AlreadyAdminException;

	/**
	 * Remove role perunAdmin for user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void removePerunAdmin(PerunSession sess, User user) throws UserNotAdminException;

	/**
	 * Remove role Perun observer from user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void removePerunObserver(PerunSession sess, User user) throws UserNotAdminException;

	/**
	 * Remove role Perun observer from authorizedGroup.
	 *
	 * @param sess
	 * @param authorizedGroup
	 * @throws InternalErrorException
	 */
	void removePerunObserverFromAuthorizedGroup(PerunSession sess, Group authorizedGroup) throws GroupNotAdminException;

	/**
	 * Make user Cabinet manager.
	 *
	 * @param sess PerunSession
	 * @param user User to add Cabinet manager role.
	 * @throws InternalErrorException When implementation fails
	 */
	void makeUserCabinetAdmin(PerunSession sess, User user);

	/**
	 * Remove role Cabinet manager from user.
	 *
	 * @param sess PerunSession
	 * @param user User to have cabinet manager role removed
	 * @throws InternalErrorException If implementation fails
	 * @throws UserNotAdminException If user was not cabinet admin
	 */
	void removeCabinetAdmin(PerunSession sess, User user) throws UserNotAdminException;

	/**
	 * Adds role for user in VO.
	 * @param sess perun session
	 * @param role role of user in VO
	 * @param vo virtual organization
	 * @param user user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addVoRole(PerunSession sess, String role, Vo vo, User user) throws AlreadyAdminException;

	/**
	 * Adds role for group in a VO.
	 * @param sess perun session
	 * @param role role of group in VO
	 * @param vo virtual organization
	 * @param group group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addVoRole(PerunSession sess, String role, Vo vo, Group group) throws AlreadyAdminException;

	/**
	 * Removes role from user in a VO.
	 * @param sess perun session
	 * @param role role of user in a VO
	 * @param vo virtual organization
	 * @param user user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeVoRole(PerunSession sess, String role, Vo vo, User user) throws UserNotAdminException;

	/**
	 * Removes role from group in a VO.
	 * @param sess perun session
	 * @param role role of group in a VO
	 * @param vo virtual organization
	 * @param group group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeVoRole(PerunSession sess, String role, Vo vo, Group group) throws GroupNotAdminException;

	/**
	 * Checks whether the user is in role for Vo.
	 * @param session perun session
	 * @param user user
	 * @param role role of user
	 * @param vo virtual organisation
	 * @return true if user is in role for VO, otherwise false.
	 */
	boolean isUserInRoleForVo(PerunSession session, User user, String role, Vo vo);

	/**
	 * Checks whether the gruop is in role for Vo.
	 * @param session perun session
	 * @param group group
	 * @param role role of group
	 * @param vo virtual organization
	 * @return true if group is in role for VO, otherwise false.
	 */
	boolean isGroupInRoleForVo(PerunSession session, Group group, String role, Vo vo);

	/**
	 * Gets list of VOs for which the group has the role.
	 * @param sess perun session
	 * @param group group
	 * @param role role of group
	 * @return list of VOs from which the group has the role
	 * @throws InternalErrorException
	 */
	List<Integer> getVoIdsForGroupInRole(PerunSession sess, Group group, String role);

	/**
	 * Gets list of VOs for which the user has the role.
	 * @param sess perun session
	 * @param user user
	 * @param role role of user
	 * @return list of VOs for which the user has the role.
	 * @throws InternalErrorException
	 */
	List<Integer> getVoIdsForUserInRole(PerunSession sess, User user, String role);

	/**
	 * Sets role to given user for given resource.
	 *
	 * @param sess session
	 * @param user user
	 * @param role role
	 * @param resource resource
	 * @throws InternalErrorException internal error
	 * @throws AlreadyAdminException when already in role
	 */
	void addResourceRole(PerunSession sess, User user, String role, Resource resource) throws AlreadyAdminException;

	/**
	 * Sets role to given group for given resource.
	 *
	 * @param sess session
	 * @param group group
	 * @param role role
	 * @param resource resource
	 * @throws InternalErrorException internal error
	 * @throws AlreadyAdminException when already in role
	 */
	void addResourceRole(PerunSession sess, Group group, String role, Resource resource) throws AlreadyAdminException;

	/**
	 * Remove role to user for resource.
	 *
	 * @param sess session
	 * @param role role
	 * @param resource resource
	 * @param user user
	 * @throws InternalErrorException internal error
	 * @throws UserNotAdminException user was not admin
	 */
	void removeResourceRole(PerunSession sess, String role, Resource resource, User user) throws UserNotAdminException;

	/**
	 * Remove role to group for resource.
	 *
	 * @param sess session
	 * @param role role
	 * @param resource resource
	 * @param group group
	 * @throws InternalErrorException internal error
	 * @throws GroupNotAdminException group was not admin
	 */
	void removeResourceRole(PerunSession sess, String role, Resource resource, Group group) throws GroupNotAdminException;

	/**
	 * Check if the given role exists in the database.
	 * Check is case insensitive.
	 *
	 * @param role which will be checked
	 * @return true if role exists, false otherwise.
	 */
	boolean roleExists(String role);

	/**
	 * Load perun roles and policies from the configuration file perun-roles.yml.
	 * Roles are loaded to the database and policies are loaded to the PerunPoliciesContainer.
	 */
	void loadAuthorizationComponents();

	/**
	 * Fetch the identification of the role from the table roles in the database;
	 *
	 * @return identification of the role
	 */
	Integer getRoleId(String role);

	/**
	 * Set a role according the mapping of values
	 *
	 * @param sess
	 * @param mappingOfValues from which will be the query created (keys are column names and values are their ids)
	 * @param role which will be set (just information for exception)
	 * @throws InternalErrorException
	 * @throws RoleAlreadySetException
	 */
	void setRole(PerunSession sess, Map<String, Integer> mappingOfValues, String role) throws RoleAlreadySetException;

	/**
	 * Unset a role according the mapping of values
	 *
	 * @param sess
	 * @param mappingOfValues from which will be the query created (keys are column names and values are their ids)
	 * @param role which will be unset (just information for exception)
	 * @throws InternalErrorException
	 * @throws RoleNotSetException
	 */
	void unsetRole(PerunSession sess, Map<String, Integer> mappingOfValues, String role) throws RoleNotSetException;

	/**
	 * Get all valid richUser administrators (for group-based rights, status must be VALID for both Vo and group) for complementary object and role with specified attributes.
	 *
	 * @param mappingOfValues from which will be the query created (keys are column names and values are their ids)
	 * @param onlyDirectAdmins if we do not want to include also members of authorized groups.
	 *
	 * @return list of user administrators for complementary object and role with specified attributes.
	 */
	List<User> getAdmins(Map<String, Integer> mappingOfValues, boolean onlyDirectAdmins);


	/**
	 * Get all authorizedGroups for complementary object and role.
	 *
	 * @param mappingOfValues according to which will be the role selected
	 *
	 * @return list of authorizedGroups
	 */
	List<Group> getAdminGroups(Map<String, Integer> mappingOfValues);

	/**
	 * Get all Vos where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param user for who Vos are retrieved
	 * @param roles for which Vos are retrieved
	 * @return Set of Vos
	 */
	Set<Vo> getVosWhereUserIsInRoles(User user, List<String> roles);

	/**
	 * Get all Facilities where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param user for who Facilities are retrieved
	 * @param roles for which Facilities are retrieved
	 * @return Set of Facilities
	 */

	Set<Facility> getFacilitiesWhereUserIsInRoles(User user, List<String> roles);
	/**
	 * Get all Resources where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param user for who Resources are retrieved
	 * @param roles for which Resources are retrieved
	 * @return Set of Resources
	 */
	Set<Resource> getResourcesWhereUserIsInRoles(User user, List<String> roles);

	/**
	 * Get all Groups where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * Method does not return subgroups of the fetched groups.
	 *
	 * @param user for who Groups are retrieved
	 * @param roles for which Groups are retrieved
	 * @return Set of Groups
	 */
	Set<Group> getGroupsWhereUserIsInRoles(User user, List<String> roles);

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
	boolean groupMatchesUserRolesFilter(PerunSession sess, User user, Group group, List<String> roles, List<RoleAssignmentType> types);

	/**
	 * Get all Members where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param user for who Members are retrieved
	 * @param roles for which Members are retrieved
	 * @return Set of Members
	 */
	Set<Member> getMembersWhereUserIsInRoles(User user, List<String> roles);

	/**
	 * Get all SecurityTeams where the given user has set one of the given roles
	 * or the given user is a member of an authorized group with such roles.
	 *
	 * @param user for who SecurityTeams are retrieved
	 * @param roles for which SecurityTeams are retrieved
	 * @return Set of SecurityTeams
	 */
	Set<SecurityTeam> getSecurityTeamsWhereUserIsInRoles(User user, List<String> roles);

	/**
	 * Get role id by its name, returns -1 if role does not exist.
	 *
	 * @param name - name of the role
	 * @return - role id with the given name
	 */
	int getRoleIdByName(String name);

	/**
	 * Returns true if the user in session is vo admin or vo observer of specific Vo.
	 *
	 * @param sess - session
	 * @param vo - vo
	 * @return bolean
	 */
	boolean isVoAdminOrObserver(PerunSession sess, Vo vo);
}
