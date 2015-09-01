package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyBlacklistedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;

import java.util.List;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public interface SecurityTeamsManager {

	/**
	 * Get list of SecurityTeams by access rights
	 *  - PERUNADMIN : all teams
	 *  - SECURITYADMIN : teams where user is admin
	 *
	 * @param perunSession
	 * @return List of SecurityTeams or empty ArrayList<SecurityTeam>
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<SecurityTeam> getSecurityTeams(PerunSession perunSession) throws PrivilegeException, InternalErrorException;

	/**
	 * get all security teams in perun system
	 *
	 * @param perunSession
	 * @return List of SecurityTeams or empty List<SecurityTeam>
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<SecurityTeam> getAllSecurityTeams(PerunSession perunSession) throws PrivilegeException, InternalErrorException;

	/**
	 * Create new SecurityTeam.
	 *
	 * @param perunSession
	 * @param securityTeam SecurityTeam object with prefilled name
	 * @return Newly created Security team with new id
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin.
	 * @throws SecurityTeamExistsException
	 */
	SecurityTeam createSecurityTeam(PerunSession perunSession, SecurityTeam securityTeam) throws PrivilegeException, InternalErrorException, SecurityTeamExistsException;

	/**
	 * Updates SecurityTeam.
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @return returns updated SecurityTeam
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws InternalErrorException
	 * @throws SecurityTeamNotExistsException
	 */
	SecurityTeam updateSecurityTeam(PerunSession perunSession, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, SecurityTeamExistsException;

	/**
	 * Delete SecurityTeam.
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws InternalErrorException
	 * @throws SecurityTeamNotExistsException
	 */
	void deleteSecurityTeam(PerunSession perunSession, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException;

	/**
	 * Find existing SecurityTeam by ID.
	 *
	 * @param perunSession
	 * @param id
	 * @return security team with given id
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws InternalErrorException
	 * @throws SecurityTeamNotExistsException
	 */
	SecurityTeam getSecurityTeamById(PerunSession perunSession, int id) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException;

	/**
	 * get all security admins of given security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @return list of users which are admis of given security team
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 */
	List<User> getAdmins(PerunSession perunSession, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException;

	/**
	 * create security admin from given user and add him as security admin of given security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user user who will became a security administrator
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 * @throws UserNotExistsException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession perunSession, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, AlreadyAdminException;

	/**
	 * Create group as security admins group of given security team (all users in group will have security admin rights)
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param group group which members will became a security administrators
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 * @throws GroupNotExistsException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession perunSession, SecurityTeam securityTeam, Group group) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, GroupNotExistsException, AlreadyAdminException;

	/**
	 * Remove security admin role for given security team from user
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 * @throws UserNotExistsException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, UserNotAdminException;

	/**
	 * Remove security admin role for given security team from group
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param group
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 * @throws GroupNotExistsException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, SecurityTeam securityTeam, Group group) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, GroupNotExistsException, GroupNotAdminException;

	/**
	 * Add User to black list of security team to filter him out.
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 * @throws UserNotExistsException
	 * @throws AlreadyMemberException
	 * @throws UserAlreadyBlacklistedException
	 */
	void addUserToBlacklist(PerunSession perunSession, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, AlreadyMemberException, UserAlreadyBlacklistedException;

	/**
	 * remove user from blacklist of given security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @param user user who will became a security administrator
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 */
	void removeUserFromBlacklist(PerunSession perunSession, SecurityTeam securityTeam, User user) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, MemberNotExistsException, UserAlreadyRemovedException;

	/**
	 * get list of blacklisted users by security team
	 *
	 * @param perunSession
	 * @param securityTeam
	 * @return lis of blacklisted users by security team
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 */
	List<User> getBlacklist(PerunSession perunSession, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException;

	/**
	 * get union of blacklists of all security teams assigned to facility
	 *
	 * @param perunSession
	 * @param facility
	 * @return list of blacklisted users for facility
	 * @throws InternalErrorException
	 * @throws PrivilegeException Can do only PerunAdmin or SecurityAdmin of the SecurityTeam
	 * @throws SecurityTeamNotExistsException
	 * @throws FacilityNotExistsException
	 */
	List<User> getBlacklist(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, FacilityNotExistsException;

}
