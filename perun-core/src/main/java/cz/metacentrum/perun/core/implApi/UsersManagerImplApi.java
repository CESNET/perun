package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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
	 * Get all the users who have given type of the ExtSource and login.
	 *
	 * @param perunSession perun session
	 * @param extSourceType type of the user extSource
	 * @param login login of the user
	 * @return all users with given parameters
	 * @throws InternalErrorException
	 */
	List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login);

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
	 * Return all specificUsers who are owned by the user and their ownership is not in status disabled
	 *
	 * @param sess
	 * @param user the user
	 * @return list of specificUsers who are owned by the user
	 * @throws InternalErrorException
	 */
	List<User> getSpecificUsersByUser(PerunSession sess, User user);

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
	 * Remove specificUser owner (the user)
	 * Only disable ownership of user and specificUser
	 *
	 * @param sess
	 * @param user the user
	 * @param specificUser the specificUser
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws SpecificUserOwnerAlreadyRemovedException;

	/**
	 * Add specificUser owner (the user).
	 * If not exists, create new ownership.
	 * If exists, only enable ownership for user and specificUser
	 *
	 * @param sess
	 * @param user the user
	 * @param specificUser the specificUser
	 * @throws InternalErrorException
	 */
	void addSpecificUserOwner(PerunSession sess, User user, User specificUser);

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
	 * Set ownership for user and specificUser to DISABLE (1).
	 *
	 * @param sess
	 * @param user
	 * @param specificUser
	 * @throws InternalErrorException
	 */
	void disableOwnership(PerunSession sess, User user, User specificUser);

	/**
	 * Return true if ownership between user and specificUser already exists.
	 * Return false if not.
	 *
	 * @param sess
	 * @param user
	 * @param specificUser
	 * @return true if ownership exists, false if not
	 * @throws InternalErrorException
	 */
	boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser);

	/**
	 * Return all specific Users (only specific users)
	 * Return also users who has no owners.
	 *
	 * @param sess
	 * @return list of all specific users in perun
	 * @throws InternalErrorException
	 */
	List<User> getSpecificUsers(PerunSession sess);

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
	 * Return users which have member in VO.
	 *
	 * @param sess
	 * @param vo
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByVo(PerunSession sess, Vo vo);

	/**
	 * Returns all users (included specific users).
	 *
	 * @param sess
	 * @return list of all users
	 * @throws InternalErrorException
	 */
	List<User> getUsers(PerunSession sess);

	/**
	 *  Creates the user, stores it in the DB. This method will fill id and uuid to
	 *  the given user object, but returns a new user object loaded from the DB.
	 *
	 * @param perunSession
	 * @param user user bean with filled properties
	 * @return user with user.id filled
	 * @throws InternalErrorException
	 */
	User createUser(PerunSession perunSession, User user);


	/**
	 * Set flag for specific user type for the user.
	 *
	 * @param sess
	 * @param user the user
	 * @param specificUserType specific type of user
	 * @return
	 * @throws InternalErrorException
	 */
	User setSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType);

	/**
	 * Unset flag for specific user type for the user.
	 *
	 * @param sess
	 * @param user the user
	 * @param specificUserType specific type of user
	 * @return
	 * @throws InternalErrorException
	 */
	User unsetSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType);

	/**
	 *  Deletes user (normal or specific) including all relations to other users (normal,specific,sponsor)
	 *
	 * @param perunSession Session for authz
	 * @param user User to delete
	 * @throws InternalErrorException
	 * @throws UserAlreadyRemovedException  When user is already deleted
	 * @throws SpecificUserAlreadyRemovedException When specific user is already deleted
	 */
	void deleteUser(PerunSession perunSession, User user) throws UserAlreadyRemovedException, SpecificUserAlreadyRemovedException;

	/**
	 *  Updates users data in DB.
	 *
	 * @param perunSession
	 * @param user
	 * @return updated user
	 * @throws InternalErrorException
	 */
	User updateUser(PerunSession perunSession, User user);

	/**
	 *  Updates titles before/after users name.
	 *  New titles must be set inside User object.
	 *  Setting any title to null will remove title from name.
	 *  Other user's properties are ignored.
	 *
	 *
	 * @param perunSession
	 * @param user
	 * @return updated user with new titles before/after name
	 * @throws InternalErrorException
	 */
	User updateNameTitles(PerunSession perunSession, User user);

	/**
	 *  Updates user;s userExtSource in DB.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated user
	 * @throws InternalErrorException
	 * @throws UserExtSourceExistsException When UES with same login/extSource already exists.
	 */
	UserExtSource updateUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws UserExtSourceExistsException;

	/**
	 *  Updates user's userExtSource last access time in DB.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated userExtSource
	 * @throws InternalErrorException
	 */
	void updateUserExtSourceLastAccess(PerunSession perunSession, UserExtSource userExtSource);

	/**
	 * Gets list of all users external sources by specific type and extLogin.
	 *
	 * @param sess
	 * @param extType - type of extSource (ex. 'IDP')
	 * @param extLogin - extLogin of userExtSource
	 *
	 * @return list of userExtSources with type and login, empty list if no such userExtSource exists
	 *
	 * @throws InternalErrorException
	 */
	List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin);

	/**
	 * Get all users userExtSources with last_access not older than (now - m),
	 * where 'm' is number of months defined in CONSTANT in UsersManagerImpl.
	 *
	 * @param sess
	 * @param user user to get extSources for
	 *
	 * @return list of active user extSources (not older than now - m)
	 * @throws InternalErrorException
	 */
	List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user);

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
	 * Return userExtSource for specific attribute id and unique value.
	 * If not found, throw and exception.
	 *
	 * It looks for exactly one value of the specific attribute type:
	 * - Integer -> exactly match
	 * - String -> exactly match
	 * - Map -> exactly match of "key=value"
	 * - ArrayList -> exactly match of one of the value
	 *
	 * @param sess
	 * @param attrId attribute id we are looking for
	 * @param uniqueValue value used for searching
	 *
	 * @return userExtSource found by attribute id and it's unique value
	 *
	 * @throws InternalErrorException if Runtime exception has been thrown
	 * @throws UserExtSourceNotExistsException if userExtSource can't be found
	 */
	UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue) throws UserExtSourceNotExistsException;

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
	 * Adds user's external sources.
	 *
	 * @param perunSession
	 * @param user
	 * @param userExtSource
	 * @return	user external source with userExtSource.id filled
	 * @throws InternalErrorException
	 */
	UserExtSource addUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource);

	/**
	 * Removes user's external sources.
	 *
	 * @param perunSession
	 * @param user
	 * @param userExtSource
	 * @throws InternalErrorException
	 * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void removeUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws UserExtSourceAlreadyRemovedException;

	/**
	 *  Removes all user's external sources.
	 *
	 * @param perunSession
	 * @param user
	 * @throws InternalErrorException
	 */
	void removeAllUserExtSources(PerunSession perunSession, User user);

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
	UserExtSource getUserExtSourceByExtLogin(PerunSession perunSession, ExtSource source, String extLogin) throws UserExtSourceNotExistsException;

	/**
	 * Return true if login in specified namespace is already reserved, false if not.
	 *
	 * @param sess
	 * @param namespace namespace for login
	 * @param login login to check
	 * @return true if login exist, false if not exist
	 * @throws InternalErrorException
	 */
	boolean isLoginReserved(PerunSession sess, String namespace, String login);

	/**
	 * Check if login in specified namespace exists.
	 *
	 * @param sess
	 * @param namespace namespace for login
	 * @param login login to check
	 * @throws InternalErrorException
	 * @throws AlreadyReservedLoginException throw this exception if login already exist in table of reserved logins
	 */
	void checkReservedLogins(PerunSession sess, String namespace, String login) throws AlreadyReservedLoginException;

	/**
	 * Check if user exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param user user to check
	 * @return true if user exists in underlaying data source, false otherwise
	 * @throws InternalErrorException
	 */
	boolean userExists(PerunSession perunSession, User user);

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
	 * Check if userExtSource exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param userExtSource userExtSource to check
	 * @return true if userExtSource exists in underlaying data source, false otherwise
	 * @throws InternalErrorException
	 */
	boolean userExtSourceExists(PerunSession perunSession, UserExtSource userExtSource);

	/**
	 * Check if userExtSource exists in underlaying data source by identity (login/extSource combination)
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @throws InternalErrorException
	 * @throws UserExtSourceNotExistsException
	 */
	void checkUserExtSourceExists(PerunSession perunSession, UserExtSource userExtSource) throws UserExtSourceNotExistsException;

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
	 * Returns list of VOs, where the user is an Administrator.
	 * Including VOs, where the user is a member of authorized group.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of VOs, where the user is an Administrator.
	 * @throws InternalErrorException
	 */
	List<Vo> getVosWhereUserIsAdmin(PerunSession perunSession, User user);

	/**
	 * Returns list of Groups in Perun, where the User is a direct Administrator
	 * or he is a member of any group which is Administrator of some of these Groups.
	 *
	 * @param perunSession
	 * @param user
	 *
	 * @return list of Groups, where user or some of his groups is an Administrator
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereUserIsAdmin(PerunSession perunSession, User user);

	/**
	 * Returns list of Groups in selected Vo, where the User is a direct Administrator
	 * or he is a member of any group which is Administrator of some of these Groups.
	 *
	 * @param sess
	 * @param vo selected Vo under which we are looking for groups
	 * @param user manager of groups we are looking for
	 *
	 * @return list of Groups, where user or some of his groups (in the Vo) is an Administrator
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user);

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
	 * Returns list of users who matches the searchString, searching name, id, member attributes, user attributes
	 * and userExtSource attributes (listed in perun.properties).
	 *
	 * @param sess perun session
	 * @param searchString it will be looking for this search string in the specific parameters in DB
	 * @return list of users
	 */
	List<User> findUsers(PerunSession sess, String searchString);

	/**
	 * Returns list of users who matches the searchString, searching name (exact match), id, member attributes, user attributes
	 * and userExtSource attributes (listed in perun.properties).
	 *
	 * @param sess perun session
	 * @param searchString it will be looking for this search string in the specific parameters in DB
	 * @return list of users
	 */
	List<User> findUsersByExactMatch(PerunSession sess, String searchString);

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
	List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter);

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
	 * Returns all users who have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttribute(PerunSession sess, Attribute attribute);

	/**
	 * Returns all users who have the attribute with the value. attributeValue is not converted to the attribute type, it is always type of String.
	 *
	 * @param sess
	 * @param attributeDefintion
	 * @param attributeValue
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttributeValue(PerunSession sess, AttributeDefinition attributeDefintion, String attributeValue);

	/**
	 * Batch method which returns users by theirs ids.
	 *
	 * @param sess
	 * @param usersIds
	 * @return
	 * @throws InternalErrorException
	 */
	List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds);

	/**
	 * Returns all users who are not member of any VO.
	 *
	 * @param sess
	 * @return list of users who are not member of any VO
	 * @throws InternalErrorException
	 */
	List<User> getUsersWithoutVoAssigned(PerunSession sess);

	/**
	 * Returns true if the user is PERUNADMIN.
	 *
	 * @param sess
	 * @param user
	 * @return true if the user is PERUNADMIN, false otherwise.
	 * @throws InternalErrorException
	 */
	boolean isUserPerunAdmin(PerunSession sess, User user);

	/**
	 * Removes all authorships of user when user is deleted from DB
	 * (author records on all his publications).
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException thrown when runtime exception
	 */
	void removeAllAuthorships(PerunSession sess, User user);

	/**
	 * Return list of all reserved logins for specific user
	 * (pair is namespace and login)
	 *
	 * @param user for which get reserved logins
	 * @return list of pairs namespace and login
	 * @throws InternalErrorException
	 */
	List<Pair<String, String>> getUsersReservedLogins(User user);

	/**
	 * Delete all reserved logins for specific user
	 * (pair is namespace and login)
	 *
	 * @param user for which get delete reserved logins
	 * @throws InternalErrorException
	 */
	void deleteUsersReservedLogins(User user);

	/**
	 * Store request of change of user's preferred email address.
	 * Change in attribute value is not done, until email
	 * address is verified by link in email notice.
	 * (urn:perun:user:attribute-def:def:preferredEmail)
	 *
	 * @param sess
	 * @param user
	 * @param email
	 * @throws InternalErrorException
	 * @return ID of change request
	 */
	int requestPreferredEmailChange(PerunSession sess, User user, String email);

	/**
	 * Get new preferred email value from user's original request
	 *
	 * @param sess PerunSession
	 * @param user User to get new email address for
	 * @param i decrypted parameter
	 * @param m encrypted parameter
	 * @throws InternalErrorException
	 * @return String return new preferred email
	 */
	String getPreferredEmailChangeRequest(PerunSession sess, User user, String i, String m);

	/**
	 * Removes all mail change requests related to user.
	 *
	 * @param sess PerunSession
	 * @param user User to remove preferred email change requests for
	 *
	 * @throws InternalErrorException if any exception in DB occur
	 */
	void removeAllPreferredEmailChangeRequests(PerunSession sess, User user);

	/**
	 * Return list of email addresses of user, which are
	 * awaiting validation and are inside time window
	 * for validation.
	 *
	 * If there is no preferred email change request pending
	 * or requests are outside time window for validation,
	 * returns empty list.
	 *
	 * @param sess PerunSession
	 * @param user User to check pending request for
	 *
	 * @return List<String> user's email addresses pending validation
	 */
	List<String> getPendingPreferredEmailChanges(PerunSession sess, User user);

	/**
	 * Checks if the password reset request link is valid. The request is valid, if it
	 * was created, never used and hasn't expired yet.
	 *
	 * @param sess PerunSession
	 * @param user user to check request for
	 * @param requestId request id to check
	 * @throws PasswordResetLinkExpiredException when the password reset request expired
	 * @throws PasswordResetLinkNotValidException when the password reset request was already used or has never existed
	 */
	void checkPasswordResetRequestIsValid(PerunSession sess, User user, int requestId) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

	/**
	 * Return only valid password reset requests for selected user and request ID.
	 * Validity is determined by time since request creation and actual usage (only once).
	 *
	 * If no valid entry is found, exception is thrown. Entry is invalidated once loaded.
	 *
	 * @param sess PerunSession
	 * @param user user to get requests for
	 * @param request request ID to get
	 * @return Pair with "left" = namespace user wants to reset password, "right" = mail used for notification
	 * @throws PasswordResetLinkExpiredException when the password reset request expired
	 * @throws PasswordResetLinkNotValidException when the password reset request was already used or has never existed
	 */
	Pair<String,String> loadPasswordResetRequest(PerunSession sess, User user, int request) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

	/**
	 * Removes all password reset requests associated with user.
	 * This is used when deleting user from Perun.
	 *
	 * @param sess PerunSession
	 * @param user User to remove all pwdreset requests
	 * @throws InternalErrorException
	 */
	void removeAllPasswordResetRequests(PerunSession sess, User user);

	/**
	 * Get count of all users.
	 *
	 * @param perunSession
	 *
	 * @return count of all users
	 *
	 * @throws InternalErrorException
	 */
	int getUsersCount(PerunSession perunSession);

	/**
	 * Return instance of PasswordManagerModule for specified namespace or NULL if class for module is not found.
	 * Throws exception if class can't be instantiated.
	 *
	 * @param session Session with authz
	 * @param namespace Namespace to get PWDMGR module.
	 * @return Instance of password manager module or NULL if not exists for passed namespace.
	 * @throws InternalErrorException When module can't be instantiated.
	 */
	PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace);

	/**
	 * Gets list of user that sponsored a member.
	 * @param sess perun session
	 * @param sponsoredMember member which is sponsored
	 * @return list of users that sponsored a member.
	 * @throws InternalErrorException
	 */
	List<User> getSponsors(PerunSession sess, Member sponsoredMember);

	/**
	 * Deletes all links to sponsors, even those marked as inactive.
	 * @param sess perun session
	 * @param sponsor sponsor
	 * @throws InternalErrorException
	 */
	void deleteSponsorLinks(PerunSession sess, User sponsor);

	/**
	 * Implements search for #UsersManagerBl.findUsersWithExtSourceAttributeValueEnding().
	 */
	List<User> findUsersWithExtSourceAttributeValueEnding(PerunSessionImpl sess, String attributeName, String valueEnd, List<String> excludeValueEnds);

	/**
	 * Return all resources, where user is assigned through all his members.
	 *
	 * @param sess
	 * @param user
	 * @return All resources where user is assigned
	 */
	List<Resource> getAssignedResources(PerunSession sess, User user);

	/**
	 * Return all resources, where user is allowed by all his members.
	 *
	 * @param sess
	 * @param user
	 * @return All resources where user is allowed
	 */
	List<Resource> getAllowedResources(PerunSession sess, User user);

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

}
