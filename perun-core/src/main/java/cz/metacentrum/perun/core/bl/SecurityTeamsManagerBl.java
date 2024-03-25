package cz.metacentrum.perun.core.bl;


import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyBlacklistedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import java.util.List;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public interface SecurityTeamsManagerBl {

  /**
   * Blacklist user by given security team with description.
   * <p>
   * Description can be null.
   *
   * @param sess
   * @param securityTeam
   * @param user
   * @param description
   * @throws InternalErrorException
   */
  void addUserToBlacklist(PerunSession sess, SecurityTeam securityTeam, User user, String description);

  /**
   * check if group is not security admin of given security team throw exception if it is
   *
   * @param sess
   * @param securityTeam
   * @param group
   * @throws AlreadyAdminException
   * @throws InternalErrorException
   */
  void checkGroupIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group)
      throws AlreadyAdminException;

  /**
   * check if group is security admin of given security team throw exception if is not
   *
   * @param sess
   * @param securityTeam
   * @param group
   * @throws GroupNotAdminException
   * @throws InternalErrorException
   */
  void checkGroupIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group)
      throws GroupNotAdminException;

  /**
   * check if security team exists throw exception if doesn't
   *
   * @param sess
   * @param securityTeam
   * @throws SecurityTeamNotExistsException
   * @throws InternalErrorException
   */
  void checkSecurityTeamExists(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamNotExistsException;

  /**
   * check if security team does <b>not</b> exist throw exception if do
   *
   * @param sess
   * @param securityTeam
   * @throws SecurityTeamExistsException
   * @throws InternalErrorException
   */
  void checkSecurityTeamNotExists(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamExistsException;

  /**
   * check if name is unique throw exception if it is not
   *
   * @param sess
   * @param securityTeam
   * @throws InternalErrorException
   */
  void checkSecurityTeamUniqueName(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamExistsException;

  /**
   * check if user is blacklisted by given security team throw exception if is not
   *
   * @param sess
   * @param securityTeam
   * @param user
   * @throws UserAlreadyRemovedException
   * @throws InternalErrorException
   */
  void checkUserIsInBlacklist(PerunSession sess, SecurityTeam securityTeam, User user)
      throws UserAlreadyRemovedException;

  /**
   * check if user is not blacklisted by given security team throw exception if is
   *
   * @param sess
   * @param securityTeam
   * @param user
   * @throws UserAlreadyBlacklistedException
   * @throws InternalErrorException
   */
  void checkUserIsNotInBlacklist(PerunSession sess, SecurityTeam securityTeam, User user)
      throws UserAlreadyBlacklistedException;

  /**
   * check if user is not security admin of given security team throw exception if it is
   *
   * @param sess
   * @param securityTeam
   * @param user
   * @throws AlreadyAdminException
   * @throws InternalErrorException
   */
  void checkUserIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user)
      throws AlreadyAdminException;

  /**
   * check if user is security admin of given security team throw exception if is not
   *
   * @param sess
   * @param securityTeam
   * @param user
   * @throws UserNotAdminException
   * @throws InternalErrorException
   */
  void checkUserIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException;

  /**
   * Create security team
   *
   * @param sess
   * @param securityTeam
   * @return Newly created Security team with new id
   * @throws InternalErrorException
   */
  SecurityTeam createSecurityTeam(PerunSession sess, SecurityTeam securityTeam);

  /**
   * Delete security team
   *
   * @param sess
   * @param securityTeam
   * @param forceDelete  TRUE if Team should be forcefully deleted.
   * @throws InternalErrorException
   * @throws SecurityTeamNotExistsException
   * @throws RelationExistsException        if forceDelete == FALSE and team is assigned to any facility or has
   *                                        blacklisted users.
   */
  void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam, boolean forceDelete)
      throws SecurityTeamNotExistsException, RelationExistsException;

  /**
   * Gets list of all group administrators of the SecurityTeam.
   *
   * @param sess
   * @param securityTeam
   * @return list of Groups that are admins in the security team
   * @throws InternalErrorException
   */
  List<Group> getAdminGroups(PerunSession sess, SecurityTeam securityTeam);

  /**
   * get all security admins of given security team
   *
   * @param sess
   * @param securityTeam
   * @return list of users which are security admins in security team
   * @throws InternalErrorException
   */
  List<User> getAdmins(PerunSession sess, SecurityTeam securityTeam, boolean onlyDirectAdmins);

  /**
   * get all security teams in perun system
   *
   * @param sess
   * @return list of all security teams
   * @throws InternalErrorException
   */
  List<SecurityTeam> getAllSecurityTeams(PerunSession sess);

  /**
   * get blacklist of security team
   *
   * @param sess
   * @param securityTeam
   * @return list of blacklisted users by security team
   * @throws InternalErrorException
   */
  List<User> getBlacklist(PerunSession sess, SecurityTeam securityTeam);

  /**
   * get union of blacklists of all security teams assigned to facility
   *
   * @param sess
   * @param facility
   * @return list of blacklisted users for facility
   * @throws InternalErrorException
   */
  List<User> getBlacklist(PerunSession sess, Facility facility);

  /**
   * get blacklist of security team containing also description
   *
   * @param sess
   * @param securityTeam
   * @return List of pairs of blacklisted users by security team
   * @throws InternalErrorException
   */
  List<Pair<User, String>> getBlacklistWithDescription(PerunSession sess, SecurityTeam securityTeam);

  /**
   * get union of blacklists of all security teams assigned to facility containing also description
   *
   * @param sess
   * @param facility
   * @return List of pairs of blacklisted users for facility
   * @throws InternalErrorException
   */
  List<Pair<User, String>> getBlacklistWithDescription(PerunSession sess, Facility facility);

  /**
   * get security team by its id
   *
   * @param sess
   * @param id
   * @return security team with given id
   * @throws InternalErrorException
   * @throws SecurityTeamNotExistsException
   */
  SecurityTeam getSecurityTeamById(PerunSession sess, int id) throws SecurityTeamNotExistsException;

  /**
   * get security team by its name
   *
   * @param sess
   * @param name
   * @return security team with given name
   * @throws InternalErrorException
   * @throws SecurityTeamNotExistsException
   */
  SecurityTeam getSecurityTeamByName(PerunSession sess, String name) throws SecurityTeamNotExistsException;

  /**
   * Get list of SecurityTeams by access rights - PERUNADMIN : all teams - SECURITYADMIN : teams where user is admin
   *
   * @param sess
   * @return list of security teams by access rights
   * @throws InternalErrorException
   */
  List<SecurityTeam> getSecurityTeams(PerunSession sess);

  /**
   * control if user is blacklisted by given security team
   *
   * @param sess
   * @param st
   * @param user
   * @return true if given user is blacklisted
   * @throws InternalErrorException
   */
  boolean isUserBlacklisted(PerunSession sess, SecurityTeam st, User user);

  /**
   * Check if user is blacklisted by any security team
   *
   * @param sess
   * @param user
   * @return true if given user is blacklisted by any security team
   * @throws InternalErrorException
   */
  boolean isUserBlacklisted(PerunSession sess, User user);

  /**
   * Remove user from all blacklists
   *
   * @param sess
   * @param user
   * @throws InternalErrorException
   */
  void removeUserFromAllBlacklists(PerunSession sess, User user);

  /**
   * remove user from blacklist of given security team
   *
   * @param sess
   * @param securityTeam
   * @param user
   * @throws InternalErrorException
   */
  void removeUserFromBlacklist(PerunSession sess, SecurityTeam securityTeam, User user);

  /**
   * Update security team
   *
   * @param sess
   * @param securityTeam
   * @return updated security team
   * @throws InternalErrorException
   * @throws SecurityTeamNotExistsException
   */
  SecurityTeam updateSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamNotExistsException;

}
