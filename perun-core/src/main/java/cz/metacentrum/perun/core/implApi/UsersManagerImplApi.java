package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BlockedLogin;
import cz.metacentrum.perun.core.api.BlockedLoginsPageQuery;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Sponsorship;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersPageQuery;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsAlreadyBlockedException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsNotBlockedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkExpiredException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkNotValidException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UsersManager can find users.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Zora Sebestianova
 * @author Sona Mastrakova
 */
public interface UsersManagerImplApi {
  /**
   * Add specificUser owner (the user). If not exists, create new ownership. If exists, only enable ownership for user
   * and specificUser
   *
   * @param sess
   * @param user         the user
   * @param specificUser the specificUser
   * @throws InternalErrorException
   */
  void addSpecificUserOwner(PerunSession sess, User user, User specificUser);

  /**
   * Adds user's external sources.
   *
   * @param perunSession
   * @param user
   * @param userExtSource
   * @return user external source with userExtSource.id filled
   * @throws InternalErrorException
   */
  UserExtSource addUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource);

  /**
   * Anonymizes users data in DB - sets names and titles to NULL and sets anonymized flag to true.
   *
   * @param perunSession
   * @param user
   * @return user
   * @throws InternalErrorException
   */
  User anonymizeUser(PerunSession perunSession, User user);

  /**
   * Block login for given namespace or block login globally (if no namespace is selected)
   *
   * @param sess
   * @param login         login to be blocked
   * @param namespace     namespace where the login should be blocked (null means block the login globally)
   * @param relatedUserId id of the user related to the login or null if the relatedUserId should not be stored
   * @throws LoginIsAlreadyBlockedException
   */
  void blockLogin(PerunSession sess, String login, String namespace, Integer relatedUserId)
      throws LoginIsAlreadyBlockedException;

  /**
   * Checks if the password reset request link is valid. The request is valid, if it was created, never used and hasn't
   * expired yet.
   *
   * @param sess PerunSession
   * @param uuid UUID of the request to check
   * @throws PasswordResetLinkExpiredException  when the password reset request expired
   * @throws PasswordResetLinkNotValidException when the password reset request was already used or has never existed
   */
  void checkPasswordResetRequestIsValid(PerunSession sess, UUID uuid)
      throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

  /**
   * Check if login exists in specified namespace or in any namespace (if namespace is null).
   *
   * @param sess
   * @param namespace  namespace for login, null for all namespace
   * @param login      login to check
   * @param ignoreCase TRUE to perform case-insensitive check
   * @throws InternalErrorException
   * @throws AlreadyReservedLoginException throw this exception if login already exist in table of reserved logins
   */
  void checkReservedLogins(PerunSession sess, String namespace, String login, boolean ignoreCase)
      throws AlreadyReservedLoginException;

  /**
   * Check if user exists in underlaying data source.
   *
   * @param perunSession
   * @param user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   */
  void checkUserExists(PerunSession perunSession, User user) throws UserNotExistsException;

  /**
   * Check if userExtSource exists in underlaying data source by identity (login/extSource combination)
   *
   * @param perunSession
   * @param userExtSource
   * @throws InternalErrorException
   * @throws UserExtSourceNotExistsException
   */
  void checkUserExtSourceExists(PerunSession perunSession, UserExtSource userExtSource)
      throws UserExtSourceNotExistsException;

  /**
   * Check if userExtSource exists in underlaying data source by its ID.
   *
   * @param perunSession
   * @param id
   * @throws InternalErrorException
   * @throws UserExtSourceNotExistsException
   */
  void checkUserExtSourceExistsById(PerunSession perunSession, int id) throws UserExtSourceNotExistsException;

  /**
   * Creates the user, stores it in the DB. This method will fill id and uuid to the given user object, but returns a
   * new user object loaded from the DB.
   *
   * @param perunSession
   * @param user         user bean with filled properties
   * @return user with user.id filled
   * @throws InternalErrorException
   */
  User createUser(PerunSession perunSession, User user);

  /**
   * Deletes given login reservations.
   * <p>
   * Reserved logins must be removed from external systems (e.g. KDC) BEFORE calling this method via deletePassword() in
   * UsersManager.
   *
   * @param sess
   * @param login login (pair namespace and login) to delete
   */
  void deleteReservedLogin(PerunSession sess, Pair<String, String> login);

  /**
   * Deletes all reserved logins in given namespace
   *
   * @param sess      PerunSession
   * @param namespace Namespace
   */
  void deleteReservedLoginsForNamespace(PerunSession sess, String namespace);

  /**
   * Deletes all links to sponsors, even those marked as inactive.
   *
   * @param sess    perun session
   * @param sponsor sponsor
   * @throws InternalErrorException
   */
  void deleteSponsorLinks(PerunSession sess, User sponsor);

  /**
   * Deletes user (normal or specific) including all relations to other users (normal,specific,sponsor)
   *
   * @param perunSession Session for authz
   * @param user         User to delete
   * @throws InternalErrorException
   * @throws UserAlreadyRemovedException         When user is already deleted
   * @throws SpecificUserAlreadyRemovedException When specific user is already deleted
   */
  void deleteUser(PerunSession perunSession, User user)
      throws UserAlreadyRemovedException, SpecificUserAlreadyRemovedException;

  /**
   * Delete all applications and submitted data for specific user.
   *
   * @param user for which delete applications and submitted data
   * @throws InternalErrorException
   */
  void deleteUsersApplications(User user);

  /**
   * Delete all reserved logins for specific user (pair is namespace and login)
   *
   * @param user for which get delete reserved logins
   * @throws InternalErrorException
   */
  void deleteUsersReservedLogins(User user);

  /**
   * Set ownership for user and specificUser to DISABLE (1).
   *
   * @param sess
   * @param user
   * @param specificUser
   * @throws InternalErrorException
   */
  void disableOwnership(PerunSession sess, User user, User specificUser);

  /**
   * Set ownership for user and specificUser to ENABLE (0).
   *
   * @param sess
   * @param user
   * @param specificUser
   * @throws InternalErrorException
   */
  void enableOwnership(PerunSession sess, User user, User specificUser);

  /**
   * Returns list of users who matches the searchString, searching name, id, uuid, member attributes, user attributes
   * and userExtSource attributes (listed in perun.properties).
   *
   * @param sess         perun session
   * @param searchString it will be looking for this search string in the specific parameters in DB
   * @return list of users
   */
  List<User> findUsers(PerunSession sess, String searchString);

  /**
   * Returns list of users who matches the searchString, searching name (exact match), id, uuid, member attributes, user
   * attributes and userExtSource attributes (listed in perun.properties).
   *
   * @param sess         perun session
   * @param searchString it will be looking for this search string in the specific parameters in DB
   * @return list of users
   */
  List<User> findUsersByExactMatch(PerunSession sess, String searchString);

  /**
   * Returns list of users who exactly matches the searchString
   *
   * @param sess
   * @param searchString
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> findUsersByExactName(PerunSession sess, String searchString);

  /**
   * Returns list of users who matches the searchString
   *
   * @param sess
   * @param searchString
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> findUsersByName(PerunSession sess, String searchString);

  /**
   * Returns list of users who matches the fields.
   *
   * @param sess
   * @param titleBefore
   * @param firstName
   * @param middleName
   * @param lastName
   * @param titleAfter
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName,
                             String lastName, String titleAfter);

  /**
   * Implements search for #UsersManagerBl.findUsersWithExtSourceAttributeValueEnding().
   */
  List<User> findUsersWithExtSourceAttributeValueEnding(PerunSessionImpl sess, String attributeName, String valueEnd,
                                                        List<String> excludeValueEnds);

  /**
   * Get all users userExtSources with last_access not older than (now - m), where 'm' is number of months defined in
   * CONSTANT in UsersManagerImpl.
   *
   * @param sess
   * @param user user to get extSources for
   * @return list of active user extSources (not older than now - m)
   * @throws InternalErrorException
   */
  List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user);

  /**
   * Returns all blocked logins in namespaces (if namespace is null, then this login is blocked globally)
   *
   * @param sess
   * @return list of all blocked logins in namespaces
   */
  List<BlockedLogin> getAllBlockedLoginsInNamespaces(PerunSession sess);

  /**
   * Gets list of all users external sources by specific type and extLogin.
   *
   * @param sess
   * @param extType  - type of extSource (ex. 'IDP')
   * @param extLogin - extLogin of userExtSource
   * @return list of userExtSources with type and login, empty list if no such userExtSource exists
   * @throws InternalErrorException
   */
  List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin);

  /**
   * Return all resources, where user is allowed by all his members.
   *
   * @param sess
   * @param user
   * @return All resources where user is allowed
   */
  List<Resource> getAllowedResources(PerunSession sess, User user);

  /**
   * Return all resources, where user is assigned through all his members.
   *
   * @param sess
   * @param user
   * @return All resources where user is assigned
   */
  List<Resource> getAssignedResources(PerunSession sess, User user);

  /**
   * Return all resources of specified facility, where user is assigned through all his members.
   *
   * @param sess
   * @param facility
   * @param user
   * @return All resources where user is assigned
   */
  List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user);

  /**
   * Return all rich resources, where user is assigned through all his members.
   *
   * @param sess
   * @param user
   * @return All resources where user is assigned
   */
  List<RichResource> getAssignedRichResources(PerunSession sess, User user);

  /**
   * Return all resources of specified facility with which user is associated through all his members. Does not require
   * ACTIVE group-resource assignment.
   *
   * @param sess
   * @param facility
   * @param user
   * @return All resources with which user is associated
   */
  List<Resource> getAssociatedResources(PerunSession sess, Facility facility, User user);

  /**
   * Get all resources with which user can be associated (similar to assigned resources, but does not require ACTIVE
   * group-resource assignment).
   *
   * @param sess
   * @param user
   * @return list of resources with which user is associated
   */
  List<Resource> getAssociatedResources(PerunSession sess, User user);

  /**
   * Get blocked login by id
   *
   * @param sess session
   * @param id   id of blocked login
   * @return blocked login
   * @throws LoginIsNotBlockedException when login is not blocked
   */
  BlockedLogin getBlockedLoginById(PerunSession sess, int id) throws LoginIsNotBlockedException;

  /**
   * Get page of blocked logins.
   *
   * @param sess  session
   * @param query query with page information
   * @return page of requested blocked logins
   */
  Paginated<BlockedLogin> getBlockedLoginsPage(PerunSession sess, BlockedLoginsPageQuery query);

  /**
   * Returns list of Groups in Perun, where the User is a direct Administrator or he is a VALID member of any group
   * which is Administrator of some of these Groups.
   *
   * @param perunSession
   * @param user
   * @return list of Groups, where user or some of his groups is an Administrator
   * @throws InternalErrorException
   */
  List<Group> getGroupsWhereUserIsAdmin(PerunSession perunSession, User user);

  /**
   * Returns list of Groups in selected Vo, where the User is a direct Administrator or he is a VALID member of any
   * group which is Administrator of some of these Groups.
   *
   * @param sess
   * @param vo   selected Vo under which we are looking for groups
   * @param user manager of groups we are looking for
   * @return list of Groups, where user or some of his groups (in the Vo) is an Administrator
   * @throws InternalErrorException
   */
  List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user);

  /**
   * Return ID of blocked login
   *
   * @param sess      session
   * @param login     login
   * @param namespace namespace
   * @return id of login blocked in specified namespace
   */
  int getIdOfBlockedLogin(PerunSession sess, String login, String namespace);

  /**
   * Return instance of PasswordManagerModule for specified namespace or NULL if class for module is not found. Throws
   * exception if class can't be instantiated.
   *
   * @param session   Session with authz
   * @param namespace Namespace to get PWDMGR module.
   * @return Instance of password manager module or NULL if not exists for passed namespace.
   * @throws InternalErrorException When module can't be instantiated.
   */
  PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace);

  /**
   * Return list of email addresses of user, which are awaiting validation and are inside time window for validation.
   * <p>
   * If there is no preferred email change request pending or requests are outside time window for validation, returns
   * empty list.
   *
   * @param sess PerunSession
   * @param user User to check pending request for
   * @return List<String> user's email addresses pending validation
   */
  List<String> getPendingPreferredEmailChanges(PerunSession sess, User user);

  /**
   * Get new preferred email value from user's original request
   *
   * @param sess PerunSession
   * @param user User to get new email address for
   * @param uuid UUID of the email change request
   * @return String return new preferred email
   * @throws InternalErrorException
   */
  String getPreferredEmailChangeRequest(PerunSession sess, User user, UUID uuid);

  /**
   * Get user id of the user who was related to the given login in the past
   *
   * @param sess      session
   * @param login     blocked login
   * @param namespace namespace where the login is blocked
   * @return user id or null if there is no related user id
   */
  Integer getRelatedUserIdByBlockedLoginInNamespace(PerunSession sess, String login, String namespace)
      throws LoginIsNotBlockedException;

  /**
   * Gets reserved logins which used in the given application.
   *
   * @param sess
   * @param appId
   * @return list of logins (Pair: left - namespace, right - login)
   */
  List<Pair<String, String>> getReservedLoginsByApp(PerunSession sess, int appId);

  /**
   * Gets reserved logins which can be deleted - they are used only in the given application.
   *
   * @param sess
   * @param appId
   * @return list of logins (Pair: left - namespace, right - login)
   */
  List<Pair<String, String>> getReservedLoginsOnlyByGivenApp(PerunSession sess, int appId);

  /**
   * Return all specific Users (only specific users) Return also users who has no owners.
   *
   * @param sess
   * @return list of all specific users in perun
   * @throws InternalErrorException
   */
  List<User> getSpecificUsers(PerunSession sess);

  /**
   * Return all specificUsers who are owned by the user and their ownership is not in status disabled
   *
   * @param sess
   * @param user the user
   * @return list of specificUsers who are owned by the user
   * @throws InternalErrorException
   */
  List<User> getSpecificUsersByUser(PerunSession sess, User user);

  /**
   * Gets list of user that sponsored a member.
   *
   * @param sess            perun session
   * @param sponsoredMember member which is sponsored
   * @return list of users that sponsored a member.
   * @throws InternalErrorException
   */
  List<User> getSponsors(PerunSession sess, Member sponsoredMember);

  /**
   * Retrieves a map, that maps the ids of the sponsored members in the given VO to a list of their Sponsors with the
   * corresponding Sponsorship objects.
   *
   * @param sess perun session
   * @param voId id of a vo for whose members to retrieve the sponsors
   * @return Map of memberIds in the Vo with Lists of Pairs of their Sponsor and Sponsorship objects
   */
  Map<Integer, List<Pair<User, Sponsorship>>> getSponsorsForSponsoredMembersInVo(PerunSession sess, int voId);

  /**
   * Returns user by its id.
   *
   * @param perunSession
   * @param id
   * @return user
   * @throws UserNotExistsException
   * @throws InternalErrorException
   */
  User getUserById(PerunSession perunSession, int id) throws UserNotExistsException;

  /**
   * Returns user by VO member.
   *
   * @param perunSession
   * @param member
   * @return user
   * @throws InternalErrorException
   */
  User getUserByMember(PerunSession perunSession, Member member);

  /**
   * Returns user by his login in external source.
   *
   * @param perunSession
   * @param userExtSource
   * @return user by its userExtSource or throws UserNotExistsException
   * @throws InternalErrorException
   * @throws UserNotExistsException
   */
  User getUserByUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws UserNotExistsException;

  /**
   * Gets user's external source by the user's external login and external source.
   *
   * @param perunSession
   * @param source
   * @param extLogin
   * @return user external source object
   * @throws InternalErrorException
   * @throws UserExtSourceNotExistsException
   */
  UserExtSource getUserExtSourceByExtLogin(PerunSession perunSession, ExtSource source, String extLogin)
      throws UserExtSourceNotExistsException;

  /**
   * Get the user ext source by its id.
   *
   * @param sess
   * @param id
   * @return user external source for the id
   * @throws InternalErrorException
   * @throws UserExtSourceNotExistsException
   */
  UserExtSource getUserExtSourceById(PerunSession sess, int id) throws UserExtSourceNotExistsException;

  /**
   * Return userExtSource for specific attribute id and unique value. If not found, throw and exception.
   * <p>
   * It looks for exactly one value of the specific attribute type: - Integer -> exactly match - String -> exactly match
   * - Map -> exactly match of "key=value" - ArrayList -> exactly match of one of the value
   *
   * @param sess
   * @param attrId      attribute id we are looking for
   * @param uniqueValue value used for searching
   * @return userExtSource found by attribute id and it's unique value
   * @throws InternalErrorException          if Runtime exception has been thrown
   * @throws UserExtSourceNotExistsException if userExtSource can't be found
   */
  UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue)
      throws UserExtSourceNotExistsException;

  /**
   * Get List of user ext sources by user
   *
   * @param sess session
   * @param user owner of extSources
   * @return List of user's UserExtSources
   * @throws InternalErrorException
   */
  List<UserExtSource> getUserExtSources(PerunSession sess, User user);

  /**
   * Gets user ext sources by their ids. Silently skips non-existing user ext sources.
   *
   * @param sess
   * @param ids
   * @return List of UserExtSources with specified ids
   * @throws InternalErrorException
   */
  List<UserExtSource> getUserExtSourcesByIds(PerunSession sess, List<Integer> ids);

  /**
   * Returns all users (included specific users).
   *
   * @param sess
   * @return list of all users
   * @throws InternalErrorException
   */
  List<User> getUsers(PerunSession sess);

  /**
   * Returns all users who have set the attribute with the value. Searching only def and opt attributes.
   *
   * @param sess
   * @param attribute
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> getUsersByAttribute(PerunSession sess, Attribute attribute);

  /**
   * Returns all users who have set the attribute with the value IGNORING CASE in the comparison. Searching only def and
   * opt attributes.
   *
   * @param sess
   * @param attribute
   * @param ignoreCase TRUE to perform case-insensitive check
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> getUsersByAttribute(PerunSession sess, Attribute attribute, boolean ignoreCase);

  /**
   * Returns all users who have the attribute with the value. attributeValue is not converted to the attribute type, it
   * is always type of String.
   *
   * @param sess
   * @param attributeDefintion
   * @param attributeValue
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> getUsersByAttributeValue(PerunSession sess, AttributeDefinition attributeDefintion, String attributeValue);

  /**
   * Get all the users who have given type of the ExtSource and login.
   *
   * @param perunSession  perun session
   * @param extSourceType type of the user extSource
   * @param login         login of the user
   * @return all users with given parameters
   * @throws InternalErrorException
   */
  List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login);

  /**
   * Gets users by their ids. Silently skips non-existing users.
   *
   * @param sess
   * @param usersIds
   * @return List of users with specified ids
   * @throws InternalErrorException
   */
  List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds);

  /**
   * Return all users who owns the specificUser and their ownership is not in status disabled
   *
   * @param sess
   * @param specificUser the specific User
   * @return list of user who owns the specificUser
   * @throws InternalErrorException
   */
  List<User> getUsersBySpecificUser(PerunSession sess, User specificUser);

  /**
   * Return users which have member in VO.
   *
   * @param sess
   * @param vo
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> getUsersByVo(PerunSession sess, Vo vo);

  /**
   * Get count of all users.
   *
   * @param perunSession
   * @return count of all users
   * @throws InternalErrorException
   */
  int getUsersCount(PerunSession perunSession);

  /**
   * Get page of users.
   *
   * @param sess  session
   * @param query query with page information
   * @return page of requested users
   */
  Paginated<User> getUsersPage(PerunSession sess, UsersPageQuery query);

  /**
   * Return list of all reserved logins for specific user (pair is namespace and login)
   *
   * @param user for which get reserved logins
   * @return list of pairs namespace and login
   * @throws InternalErrorException
   */
  List<Pair<String, String>> getUsersReservedLogins(User user);

  /**
   * Returns all users who are not member of any VO.
   *
   * @param sess
   * @return list of users who are not member of any VO
   * @throws InternalErrorException
   */
  List<User> getUsersWithoutVoAssigned(PerunSession sess);

  /**
   * Returns list of VOs, where the user is an Administrator. Including VOs, where the user is a VALID member of
   * authorized group.
   *
   * @param perunSession
   * @param user
   * @return list of VOs, where the user is an Administrator.
   * @throws InternalErrorException
   */
  List<Vo> getVosWhereUserIsAdmin(PerunSession perunSession, User user);

  /**
   * Returns list of Vos' ids, where the user is member.
   *
   * @param sess
   * @param user
   * @return list of Vos, where the user is member
   * @throws InternalErrorException
   */
  List<Vo> getVosWhereUserIsMember(PerunSession sess, User user);

  /**
   * Return true if login is blocked (globally - for all namespaces per instance OR for some namespace), false if not.
   * Globally banned logins are ALWAYS case-insensitive (ignoreCase value is not taken into account for them).
   *
   * @param sess
   * @param login      login to check
   * @param ignoreCase
   * @return true if login is blocked
   */
  boolean isLoginBlocked(PerunSession sess, String login, boolean ignoreCase);

  /**
   * Return true if login is blocked for given namespace, false if not When the namespace is null, then the method
   * behaves like isLoginBlockedGlobally(), so it checks if the login is blocked globally. Globally banned logins are
   * ALWAYS case-insensitive.
   *
   * @param sess
   * @param login      login to check
   * @param namespace  namespace for login
   * @param ignoreCase
   * @return true if login is blocked for given namespace (or globally for null namespace)
   */
  boolean isLoginBlockedForNamespace(PerunSession sess, String login, String namespace, boolean ignoreCase);

  /**
   * Return true if login is blocked globally (for all namespaces per instance - represented by namespace = null), false
   * if not. Globally banned logins are ALWAYS case-insensitive.
   *
   * @param sess
   * @param login login to check
   * @return true if login is blocked globally
   */
  boolean isLoginBlockedGlobally(PerunSession sess, String login);

  /**
   * Return true if login is already reserved in specified namespace or in any namespace (if namespace is null), false
   * if not.
   *
   * @param sess
   * @param namespace  namespace for login, null for all namespace
   * @param login      login to check
   * @param ignoreCase TRUE to perform case-insensitive check
   * @return true if login exist, false if not exist
   * @throws InternalErrorException
   */
  boolean isLoginReserved(PerunSession sess, String namespace, String login, boolean ignoreCase);

  /**
   * Checks whether user has been anonymized or not.
   *
   * @param sess
   * @param user
   * @return true if user has been anonymized, false otherwise.
   */
  boolean isUserAnonymized(PerunSession sess, User user);

  /**
   * Returns true if the user is PERUNADMIN.
   *
   * @param sess
   * @param user
   * @return true if the user is PERUNADMIN, false otherwise.
   * @throws InternalErrorException
   */
  @Deprecated
  boolean isUserPerunAdmin(PerunSession sess, User user);

  /**
   * Returns only valid password reset request with specified UUID. Validity is determined by time since request
   * creation and actual usage (only once).
   * <p>
   * If no valid entry is found, exception is thrown. Entry is invalidated once loaded.
   *
   * @param sess PerunSession
   * @param uuid UUID of the request to get
   * @return Map with 3 keys: - "user_id" = ID of the user who requested this password reset, value is Integer -
   * "namespace" = namespace user wants to reset password, value is String - "mail" = mail used for notification, value
   * is String
   * @throws PasswordResetLinkExpiredException  when the password reset request expired
   * @throws PasswordResetLinkNotValidException when the password reset request was already used or has never existed
   */
  Map<String, Object> loadPasswordResetRequest(PerunSession sess, UUID uuid)
      throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

  /**
   * Removes all authorships of user when user is deleted from DB (author records on all his publications).
   *
   * @param sess
   * @param user
   * @throws InternalErrorException thrown when runtime exception
   */
  void removeAllAuthorships(PerunSession sess, User user);

  /**
   * Removes all password reset requests associated with user. This is used when deleting user from Perun.
   *
   * @param sess PerunSession
   * @param user User to remove all pwdreset requests
   * @throws InternalErrorException
   */
  void removeAllPasswordResetRequests(PerunSession sess, User user);

  /**
   * Removes all mail change requests related to user.
   *
   * @param sess PerunSession
   * @param user User to remove preferred email change requests for
   * @throws InternalErrorException if any exception in DB occur
   */
  void removeAllPreferredEmailChangeRequests(PerunSession sess, User user);

  /**
   * Removes all user's external sources.
   *
   * @param perunSession
   * @param user
   * @throws InternalErrorException
   */
  void removeAllUserExtSources(PerunSession perunSession, User user);

  /**
   * Remove specificUser owner (the user) Only disable ownership of user and specificUser
   *
   * @param sess
   * @param user         the user
   * @param specificUser the specificUser
   * @throws InternalErrorException
   * @throws cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException if there are 0 rows
   *                                                                                           affected by deleting from
   *                                                                                           DB
   */
  void removeSpecificUserOwner(PerunSession sess, User user, User specificUser)
      throws SpecificUserOwnerAlreadyRemovedException;

  /**
   * Removes user's external sources.
   *
   * @param perunSession
   * @param user
   * @param userExtSource
   * @throws InternalErrorException
   * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void removeUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource)
      throws UserExtSourceAlreadyRemovedException;

  /**
   * Store request of change of user's preferred email address. Change in attribute value is not done, until email
   * address is verified by link in email notice. (urn:perun:user:attribute-def:def:preferredEmail)
   *
   * @param sess
   * @param user
   * @param email
   * @return UUID of change request
   * @throws InternalErrorException
   */
  UUID requestPreferredEmailChange(PerunSession sess, User user, String email);

  /**
   * Set flag for specific user type for the user.
   *
   * @param sess
   * @param user             the user
   * @param specificUserType specific type of user
   * @return
   * @throws InternalErrorException
   */
  User setSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType);

  /**
   * Return true if ownership between user and specificUser already exists. Return false if not.
   *
   * @param sess
   * @param user
   * @param specificUser
   * @return true if ownership exists, false if not
   * @throws InternalErrorException
   */
  boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser);

  /**
   * Unblock login for given namespace or unblock login globally (if no namespace is selected)
   *
   * @param sess
   * @param login     login to be unblocked
   * @param namespace namespace where the login should be unblocked (null means unblock the login globally)
   * @throws LoginIsNotBlockedException
   */
  void unblockLogin(PerunSession sess, String login, String namespace) throws LoginIsNotBlockedException;

  /**
   * Unblock logins by id globally, or in the namespace they were initially blocked.
   *
   * @param sess     session
   * @param loginIds list of login ids
   */
  void unblockLoginsById(PerunSession sess, List<Integer> loginIds);

  /**
   * Unblock all logins for given namespace
   *
   * @param sess      PerunSession
   * @param namespace Namespace or null for globally blocked
   */
  void unblockLoginsForNamespace(PerunSession sess, String namespace);

  /**
   * Unset flag for specific user type for the user.
   *
   * @param sess
   * @param user             the user
   * @param specificUserType specific type of user
   * @return
   * @throws InternalErrorException
   */
  User unsetSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType);

  /**
   * Updates titles before/after users name. New titles must be set inside User object. Setting any title to null will
   * remove title from name. Other user's properties are ignored.
   *
   * @param perunSession
   * @param user
   * @return updated user with new titles before/after name
   * @throws InternalErrorException
   */
  User updateNameTitles(PerunSession perunSession, User user);

  /**
   * Updates users data in DB.
   *
   * @param perunSession
   * @param user
   * @return updated user
   * @throws InternalErrorException
   */
  User updateUser(PerunSession perunSession, User user);

  /**
   * Updates user;s userExtSource in DB.
   *
   * @param perunSession
   * @param userExtSource
   * @return updated user
   * @throws InternalErrorException
   * @throws UserExtSourceExistsException When UES with same login/extSource already exists.
   */
  UserExtSource updateUserExtSource(PerunSession perunSession, UserExtSource userExtSource)
      throws UserExtSourceExistsException;

  /**
   * Updates user's userExtSource last access time in DB.
   *
   * @param perunSession
   * @param userExtSource
   * @return updated userExtSource
   * @throws InternalErrorException
   */
  void updateUserExtSourceLastAccess(PerunSession perunSession, UserExtSource userExtSource);

  /**
   * Check if user exists in underlaying data source.
   *
   * @param perunSession
   * @param user         user to check
   * @return true if user exists in underlaying data source, false otherwise
   * @throws InternalErrorException
   */
  boolean userExists(PerunSession perunSession, User user);

  /**
   * Check if userExtSource exists in underlaying data source.
   *
   * @param perunSession
   * @param userExtSource userExtSource to check
   * @return true if userExtSource exists in underlaying data source, false otherwise
   * @throws InternalErrorException
   */
  boolean userExtSourceExists(PerunSession perunSession, UserExtSource userExtSource);
}
