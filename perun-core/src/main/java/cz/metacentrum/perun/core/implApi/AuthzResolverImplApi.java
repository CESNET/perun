package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.impl.AuthzRoles;

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

	/**
	 * Add user role admin for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add group of users role admin for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException;

	void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException, InternalErrorException;

	void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws AlreadyAdminException, InternalErrorException;

	/**
	 * Remove user role admin for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove group of users role admin for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException;

	void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException, InternalErrorException;

	void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Add user role vo observer for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addObserver(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add group of users role vo observer for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addObserver(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add user role vo  topGroupCreator for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addTopGroupCreator(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Add group of users role topGroupCreator for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param group
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addTopGroupCreator(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Remove user role vo observer for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeObserver(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove group of users role vo observer for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeObserver(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Remove user role top group creator for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeTopGroupCreator(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Remove group of users role top group creator for the vo
	 *
	 * @param sess
	 * @param vo
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeTopGroupCreator(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException;


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

}
