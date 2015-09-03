package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;

import java.util.List;

/**
 * Created by ondrej on 12.8.15.
 */
public interface SecurityTeamsManagerImplApi {

	/**
	 * get all security teams in perun system
	 *
	 * @param sess
	 * @return list of all security teams
	 * @throws InternalErrorException
	 */
	List<SecurityTeam> getAllSecurityTeams(PerunSession sess) throws InternalErrorException;

	/**
	 * Create security team
	 *
	 * @param sess
	 * @param securityTeam
	 * @return Newly created Security team with new id
	 * @throws InternalErrorException
	 * @throws SecurityTeamExistsException
	 */
	SecurityTeam createSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamExistsException;

	/**
	 * Update security team
	 *
	 * @param sess
	 * @param securityTeam
	 * @return updated security team
	 * @throws InternalErrorException
	 * @throws SecurityTeamNotExistsException
	 */
	SecurityTeam updateSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamNotExistsException;

	/**
	 * Delete security team
	 *
	 * @param sess
	 * @param securityTeam
	 * @throws InternalErrorException
	 * @throws SecurityTeamNotExistsException
	 */
	void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamNotExistsException;

	/**
	 * get security team by its id
	 *
	 * @param sess
	 * @param id
	 * @return security team with given id
	 * @throws InternalErrorException
	 * @throws SecurityTeamNotExistsException
	 */
	SecurityTeam getSecurityTeamById(PerunSession sess, int id) throws SecurityTeamNotExistsException, InternalErrorException;

	/**
	 * get all security admins of given security team
	 *
	 * @param sess
	 * @param securityTeam
	 * @return list of users which are security admins in security team
	 * @throws InternalErrorException
	 */
	List<User> getAdmins(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException;

	/**
	 * Blacklist user by given security team
	 *
	 * @param sess
	 * @param securityTeam
	 * @param user
	 * @throws InternalErrorException
	 */
	void addUserToBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException;

	/**
	 * remove user from blacklist of given security team
	 *
	 * @param sess
	 * @param securityTeam
	 * @param user
	 * @throws InternalErrorException
	 */
	void removeUserFromBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException;

	/**
	 * get union of blacklists of security teams
	 *
	 * @param sess
	 * @param securityTeams
	 * @return list of blacklisted users for list of given security teams
	 * @throws InternalErrorException
	 */
	List<User> getBlacklist(PerunSession sess, List<SecurityTeam> securityTeams) throws InternalErrorException;



	/**
	 * check if security team exists
	 * throw exception if doesn't
	 *
	 * @param sess
	 * @param securityTeam
	 * @throws SecurityTeamNotExistsException
	 * @throws InternalErrorException
	 */
	void checkSecurityTeamExists(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamNotExistsException;

	/**
	 * check if security team does <b>not</b> exist
	 * throw exception if do
	 *
	 * @param sess
	 * @param securityTeam
	 * @throws SecurityTeamExistsException
	 * @throws InternalErrorException
	 */
	void checkSecurityTeamNotExists(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamExistsException;

	/**
	 * check if name is unique
	 * throw exception if it is not
	 *
	 * @param sess
	 * @param securityTeam
	 * @throws InternalErrorException
	 */
	void checkSecurityTeamUniqueName(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamExistsException;

	/**
	 * check if user is not security admin of given security team
	 * throw exception if it is
	 *
	 * @param sess
	 * @param securityTeam
	 * @param user
	 * @throws AlreadyAdminException
	 * @throws InternalErrorException
	 */
	void checkUserIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * check if user is security admin of given security team
	 * throw exception if is not
	 *
	 * @param sess
	 * @param securityTeam
	 * @param user
	 * @throws UserNotAdminException
	 * @throws InternalErrorException
	 */
	void checkUserIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * check if group is not security admin of given security team
	 * throw exception if it is
	 *
	 *
	 * @param sess
	 * @param securityTeam
	 * @param group
	 * @throws AlreadyAdminException
	 * @throws InternalErrorException
	 */
	void checkGroupIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * check if group is security admin of given security team
	 * throw exception if is not
	 *
	 * @param sess
	 * @param securityTeam
	 * @param group
	 * @throws GroupNotAdminException
	 * @throws InternalErrorException
	 */
	void checkGroupIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * check if user is not blacklisted by given security team
	 * throw exception if is
	 *
	 * @param sess
	 * @param securityTeam
	 * @param user
	 * @throws InternalErrorException
	 */
	boolean isUserBlacklisted(PerunSession sess, SecurityTeam securityTeam, User user) throws InternalErrorException;

}
