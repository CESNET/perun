package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.*;
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
	 * Remove role perunAdmin for user.
	 *
	 * @param sess
	 * @param user 
	 * @throws InternalErrorException
	 */
	void removePerunAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException;


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
}
