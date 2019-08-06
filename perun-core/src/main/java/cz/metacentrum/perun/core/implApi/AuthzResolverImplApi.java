package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.impl.AuthzRoles;

import java.util.List;

/**
 * This interface represents AuthzResolver methods.
 *
 * @author Michal Prochazka
 */

public interface AuthzResolverImplApi {

	/**
	 * Returns all user's roles.
	 *
	 * @param user
	 * @return AuthzRoles object which contains all roles with perunbeans
	 */
	AuthzRoles getRoles(User user) throws InternalErrorException;

	/**
	 * Returns all group's roles.
	 *
	 * @param group
	 * @return AuthzRoles object which contains all roles with perunbeans
	 */
	AuthzRoles getRoles(Group group) throws InternalErrorException;

	/**
	 * Removes all authz entries for the sponsoredUser.
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @throws InternalErrorException
	 */
	void removeAllSponsoredUserAuthz(PerunSession sess, User sponsoredUser) throws InternalErrorException;

	/**
	 * Removes all authz entries for the user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void removeAllUserAuthz(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Removes all authz entries for the vo
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForVo(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Removes all authz entries for the group
	 *
	 * @param sess
	 * @param group
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForGroup(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Removes all authz entries for the facility
	 *
	 * @param sess
	 * @param facility
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForFacility(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Removes all authz entries for the resource
	 *
	 * @param sess
	 * @param resource
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForResource(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Removes all authz entries for the service
	 *
	 * @param sess
	 * @param service
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForService(PerunSession sess, Service service) throws InternalErrorException;

	/**
	 * Removes all authz entries for the securityTeam
	 *
	 * @param sess
	 * @param securityTeam
	 * @throws InternalErrorException
	 */
	void removeAllAuthzForSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException;

	/**
	 * Add user role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add group of users role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Remove user role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove group of users role admin for the facility
	 *
	 * @param sess
	 * @param facility
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Add user role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add group of users role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Remove user role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove group of users role admin for the resource
	 *
	 * @param sess
	 * @param resource
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Add user role admin for the sponsored user
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, User sponsoredUser, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add group of users role admin for the sponsored user
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, User sponsoredUser, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Remove user role admin for the sponsoredUser
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, User sponsoredUser, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove group of users role admin for the sponsoredUser
	 *
	 * @param sess
	 * @param sponsoredUser
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, User sponsoredUser, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Add user role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add group of users role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param authorizedGroup
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Remove user role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove group of users role admin for the group
	 *
	 * @param sess
	 * @param group
	 * @param authorizedGroup
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException;

	void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException, InternalErrorException;

	void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws AlreadyAdminException, InternalErrorException;

	void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException, InternalErrorException;

	void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Make user to be perunAdmin
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Make user Perun observer
	 *
	 * @param sess the perunSession
	 * @param user user to be promoted to perunObserver
	 * @throws InternalErrorException
	 */
	void makeUserPerunObserver(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Make group Perun observer
	 *
	 * @param sess the perunSession
	 * @param authorizedGroup authorizedGroup to be promoted to perunObserver
	 * @throws InternalErrorException
	 */
	void makeAuthorizedGroupPerunObserver(PerunSession sess, Group authorizedGroup) throws InternalErrorException;

	/**
	 * Remove role perunAdmin for user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void removePerunAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove role Perun observer from user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void removePerunObserver(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove role Perun observer from authorizedGroup.
	 *
	 * @param sess
	 * @param authorizedGroup
	 * @throws InternalErrorException
	 */
	void removePerunObserverFromAuthorizedGroup(PerunSession sess, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Make user Cabinet manager.
	 *
	 * @param sess PerunSession
	 * @param user User to add Cabinet manager role.
	 * @throws InternalErrorException When implementation fails
	 */
	void makeUserCabinetAdmin(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Remove role Cabinet manager from user.
	 *
	 * @param sess PerunSession
	 * @param user User to have cabinet manager role removed
	 * @throws InternalErrorException If implementation fails
	 * @throws UserNotAdminException If user was not cabinet admin
	 */
	void removeCabinetAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Adds role for user in VO.
	 * @param sess perun session
	 * @param role role of user in VO
	 * @param vo virtual organization
	 * @param user user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addVoRole(PerunSession sess, Role role, Vo vo, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Adds role for group in a VO.
	 * @param sess perun session
	 * @param role role of group in VO
	 * @param vo virtual organization
	 * @param group group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addVoRole(PerunSession sess, Role role, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Removes role from user in a VO.
	 * @param sess perun session
	 * @param role role of user in a VO
	 * @param vo virtual organization
	 * @param user user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeVoRole(PerunSession sess, Role role, Vo vo, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Removes role from group in a VO.
	 * @param sess perun session
	 * @param role role of group in a VO
	 * @param vo virtual organization
	 * @param group group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeVoRole(PerunSession sess, Role role, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Checks whether the user is in role for Vo.
	 * @param session perun session
	 * @param user user
	 * @param role role of user
	 * @param vo virtual organisation
	 * @return true if user is in role for VO, otherwise false.
	 */
	boolean isUserInRoleForVo(PerunSession session, User user, Role role, Vo vo);

	/**
	 * Checks whether the gruop is in role for Vo.
	 * @param session perun session
	 * @param group group
	 * @param role role of group
	 * @param vo virtual organization
	 * @return true if group is in role for VO, otherwise false.
	 */
	boolean isGroupInRoleForVo(PerunSession session, Group group, Role role, Vo vo);

	/**
	 * Gets list of VOs for which the group has the role.
	 * @param sess perun session
	 * @param group group
	 * @param role role of group
	 * @return list of VOs from which the group has the role
	 * @throws InternalErrorException
	 */
	List<Integer> getVoIdsForGroupInRole(PerunSession sess, Group group, Role role) throws InternalErrorException;

	/**
	 * Gets list of VOs for which the user has the role.
	 * @param sess perun session
	 * @param user user
	 * @param role role of user
	 * @return list of VOs for which the user has the role.
	 * @throws InternalErrorException
	 */
	List<Integer> getVoIdsForUserInRole(PerunSession sess, User user, Role role) throws InternalErrorException;

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
	void addResourceRole(PerunSession sess, User user, Role role, Resource resource) throws InternalErrorException, AlreadyAdminException;

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
	void addResourceRole(PerunSession sess, Group group, Role role, Resource resource) throws InternalErrorException, AlreadyAdminException;

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
	void removeResourceRole(PerunSession sess, Role role, Resource resource, User user) throws InternalErrorException, UserNotAdminException;

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
	void removeResourceRole(PerunSession sess, Role role, Resource resource, Group group) throws InternalErrorException, GroupNotAdminException;
}
