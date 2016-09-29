package cz.metacentrum.perun.core.implApi;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;

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
	User getUserByUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get all the users who have given type of the ExtSource and login.
	 *
	 * @param perunSession perun session
	 * @param extSourceType type of the user extSource
	 * @param login login of the user
	 * @return all users with given parameters
	 * @throws InternalErrorException
	 */
	List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) throws InternalErrorException;

	/**
	 * Returns user by its id.
	 *
	 * @param perunSession
	 * @param id
	 * @return user
	 * @throws UserNotExistsException
	 * @throws InternalErrorException
	 */
	User getUserById(PerunSession perunSession, int id) throws InternalErrorException, UserNotExistsException;

	/**
	 * Return all specificUsers who are owned by the user and their ownership is not in status disabled
	 *
	 * @param sess
	 * @param user the user
	 * @return list of specificUsers who are owned by the user
	 * @throws InternalErrorException
	 */
	List<User> getSpecificUsersByUser(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Return all users who owns the specificUser and their ownership is not in status disabled
	 *
	 * @param sess
	 * @param specificUser the specific User
	 * @return list of user who owns the specificUser
	 * @throws InternalErrorException
	 */
	List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) throws InternalErrorException;

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
	void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, SpecificUserOwnerAlreadyRemovedException;

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
	void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException;

	/**
	 * Set ownership for user and specificUser to ENABLE (0).
	 *
	 * @param sess
	 * @param user
	 * @param specificUser
	 * @throws InternalErrorException
	 */
	void enableOwnership(PerunSession sess, User user, User specificUser) throws InternalErrorException;

	/**
	 * Set ownership for user and specificUser to DISABLE (1).
	 *
	 * @param sess
	 * @param user
	 * @param specificUser
	 * @throws InternalErrorException
	 */
	void disableOwnership(PerunSession sess, User user, User specificUser) throws InternalErrorException;

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
	boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) throws InternalErrorException;

	/**
	 * Return all specific Users (only specific users)
	 * Return also users who has no owners.
	 *
	 * @param sess
	 * @return list of all specific users in perun
	 * @throws InternalErrorException
	 */
	List<User> getSpecificUsers(PerunSession sess) throws InternalErrorException;

	/**
	 * Returns user by VO member.
	 *
	 * @param perunSession
	 * @param member
	 * @return user
	 * @throws InternalErrorException
	 */
	User getUserByMember(PerunSession perunSession, Member member) throws InternalErrorException;

	/**
	 * Return users which have member in VO.
	 *
	 * @param sess
	 * @param vo
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByVo(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Returns all users (included specific users).
	 *
	 * @param sess
	 * @return list of all users
	 * @throws InternalErrorException
	 */
	List<User> getUsers(PerunSession sess) throws InternalErrorException;

	/**
	 *  Creates the user, stores it in the DB. This method will fill user.id property.
	 *
	 * @param perunSession
	 * @param user user bean with filled properties
	 * @return user with user.id filled
	 * @throws InternalErrorException
	 */
	User createUser(PerunSession perunSession, User user) throws InternalErrorException;


	/**
	 *  Deletes user (normal or specific) including all relations to other users (normal,specific,sponsor)
	 *
	 * @param perunSession Session for authz
	 * @param user User to delete
	 * @throws InternalErrorException
	 * @throws UserAlreadyRemovedException  When user is already deleted
	 * @throws SpecificUserAlreadyRemovedException When specific user is already deleted
	 */
	void deleteUser(PerunSession perunSession, User user) throws InternalErrorException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException;

	/**
	 *  Updates users data in DB.
	 *
	 * @param perunSession
	 * @param user
	 * @return updated user
	 * @throws InternalErrorException
	 */
	User updateUser(PerunSession perunSession, User user) throws InternalErrorException;

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
	User updateNameTitles(PerunSession perunSession, User user) throws InternalErrorException;

	/**
	 *  Updates user;s userExtSource in DB.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated user
	 * @throws InternalErrorException
	 */
	UserExtSource updateUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;

	/**
	 *  Updates user's userExtSource last access time in DB.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated userExtSource
	 * @throws InternalErrorException
	 */
	void updateUserExtSourceLastAccess(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;

	/**
	 * Gets list of all user external sources ids of the user.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of user's external sources ids
	 * @throws InternalErrorException
	 */
	List<Integer> getUserExtSourcesIds(PerunSession perunSession, User user) throws InternalErrorException;

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
	List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin) throws InternalErrorException;

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
	List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get the user ext source by its id.
	 *
	 * @param sess
	 * @param id
	 * @return user external source for the id
	 * @throws InternalErrorException
	 * @throws UserExtSourceNotExistsException
	 */
	UserExtSource getUserExtSourceById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException;

	/**
	 * Get list of user ext sources be their ids.
	 *
	 * @param sess
	 * @param ids
	 * @return list of user external sources for ids
	 * @throws InternalErrorException
	 */
	List<UserExtSource> getUserExtsourcesByIds(PerunSession sess, List<Integer> ids) throws InternalErrorException;

	/**
	 * Adds user's external sources.
	 *
	 * @param perunSession
	 * @param user
	 * @param userExtSource
	 * @return	user external source with userExtSource.id filled
	 * @throws InternalErrorException
	 */
	UserExtSource addUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws InternalErrorException;

	/**
	 * Removes user's external sources.
	 *
	 * @param perunSession
	 * @param user
	 * @param userExtSource
	 * @throws InternalErrorException
	 * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void removeUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceAlreadyRemovedException;

	/**
	 *  Removes all user's external sources.
	 *
	 * @param perunSession
	 * @param user
	 * @throws InternalErrorException
	 */
	void removeAllUserExtSources(PerunSession perunSession, User user) throws InternalErrorException;

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
	UserExtSource getUserExtSourceByExtLogin(PerunSession perunSession, ExtSource source, String extLogin) throws InternalErrorException, UserExtSourceNotExistsException;

	/**
	 * Return true if login in specified namespace is already reserved, false if not.
	 *
	 * @param sess
	 * @param namespace namespace for login
	 * @param login login to check
	 * @return true if login exist, false if not exist
	 * @throws InternalErrorException
	 */
	boolean isLoginReserved(PerunSession sess, String namespace, String login) throws InternalErrorException;

	/**
	 * Check if login in specified namespace exists.
	 *
	 * @param sess
	 * @param namespace namespace for login
	 * @param login login to check
	 * @throws InternalErrorException
	 * @throws AlreadyReservedLoginException throw this exception if login already exist in table of reserved logins
	 */
	void checkReservedLogins(PerunSession sess, String namespace, String login) throws InternalErrorException, AlreadyReservedLoginException;

	/**
	 * Check if user exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param user user to check
	 * @return true if user exists in underlaying data source, false otherwise
	 * @throws InternalErrorException
	 */
	boolean userExists(PerunSession perunSession, User user) throws InternalErrorException;

	/**
	 * Check if user exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param user
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	void checkUserExists(PerunSession perunSession, User user) throws InternalErrorException, UserNotExistsException;

	/**
	 * Check if userExtSource exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param userExtSource userExtSource to check
	 * @return true if userExtSource exists in underlaying data source, false otherwise
	 * @throws InternalErrorException
	 */
	boolean userExtSourceExists(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;

	/**
	 * Check if userExtSource exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @throws InternalErrorException
	 * @throws UserExtSourceNotExistsException
	 */
	void checkUserExtSourceExists(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException;

	/**
	 * Returns list of VOs, where the user is an Administrator.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of VOs, where the user is an Administrator.
	 * @throws InternalErrorException
	 */
	List<Vo> getVosWhereUserIsAdmin(PerunSession perunSession, User user) throws InternalErrorException;

	/**
	 * Returns list of Groups, where the user is an Administrator.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of Groups, where the user is an Administrator.
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereUserIsAdmin(PerunSession perunSession, User user) throws InternalErrorException;

	/**
	 * Returns list of Vos' ids, where the user is member.
	 *
	 * @param sess
	 * @param user
	 * @return list of Vos, where the user is member
	 * @throws InternalErrorException
	 */
	List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Returns list of users who matches the searchString, searching name, email and logins.
	 *
	 * @param sess
	 * @param searchString
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException;

	/**
	 * Returns list of users who matches the searchString, searching name, email and logins.
	 *
	 * @param sess
	 * @param searchString
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> findUsersByExactMatch(PerunSession sess, String searchString) throws InternalErrorException;

	/**
	 * Returns list of users who matches the searchString
	 *
	 * @param sess
	 * @param searchString
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> findUsersByName(PerunSession sess, String searchString) throws InternalErrorException;

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
	List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) throws InternalErrorException;
        
        /**
	 * Returns list of users who exactly matches the searchString
	 *
	 * @param sess
	 * @param searchString
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> findUsersByExactName(PerunSession sess, String searchString) throws InternalErrorException;

	/**
	 * Returns all users who have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException;

	/**
	 * Returns all users who have the attribute with the value. attributeValue is not converted to the attribute type, it is always type of String.
	 *
	 * @param sess
	 * @param attributeDefintion
	 * @param attributeValue
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttributeValue(PerunSession sess, AttributeDefinition attributeDefintion, String attributeValue) throws InternalErrorException;

	/**
	 * Batch method which returns users by theirs ids.
	 *
	 * @param sess
	 * @param usersIds
	 * @return
	 * @throws InternalErrorException
	 */
	List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) throws InternalErrorException;

	/**
	 * Returns all users who are not member of any VO.
	 *
	 * @param sess
	 * @return list of users who are not member of any VO
	 * @throws InternalErrorException
	 */
	List<User> getUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException;

	/**
	 * Returns true if the user is PERUNADMIN.
	 *
	 * @param sess
	 * @param user
	 * @return true if the user is PERUNADMIN, false otherwise.
	 * @throws InternalErrorException
	 */
	boolean isUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Removes all authorships of user when user is deleted from DB
	 * (author records on all his publications).
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException thrown when runtime exception
	 */
	void removeAllAuthorships(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Return list of all reserved logins for specific user
	 * (pair is namespace and login)
	 *
	 * @param user for which get reserved logins
	 * @return list of pairs namespace and login
	 * @throws InternalErrorException
	 */
	public List<Pair<String, String>> getUsersReservedLogins(User user) throws InternalErrorException;

	/**
	 * Delete all reserved logins for specific user
	 * (pair is namespace and login)
	 *
	 * @param user for which get delete reserved logins
	 * @throws InternalErrorException
	 */
	public void deleteUsersReservedLogins(User user) throws InternalErrorException;

	/**
	 * Get All RichUsers without UserExtSources and without virtual attributes.
	 *
	 * @param sess
	 * @return list of richUsers
	 * @throws InternalErrorException
	 */
	List<Pair<User, Attribute>> getAllRichUsersWithAllNonVirutalAttributes(PerunSession sess) throws InternalErrorException;

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
	int requestPreferredEmailChange(PerunSession sess, User user, String email) throws InternalErrorException;

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
	String getPreferredEmailChangeRequest(PerunSession sess, User user, String i, String m) throws InternalErrorException;

	/**
	 * Removes all mail change requests related to user.
	 *
	 * @param sess PerunSession
	 * @param user User to remove preferred email change requests for
	 *
	 * @throws InternalErrorException if any exception in DB occur
	 */
	void removeAllPreferredEmailChangeRequests(PerunSession sess, User user) throws InternalErrorException;

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
	 * @throws InternalErrorException
	 *
	 * @return List<String> user's email addresses pending validation
	 */
	List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Return only valid password reset requests for selected user and request ID.
	 * Validity is determined by time since request creation and actual usage (only once).
	 *
	 * If no valid entry is found, then empty string is returned. Entry is invalidated once loaded.
	 *
	 * @param user user to get requests for
	 * @param request request ID to get
	 * @return namespace where user wants to reset password in or empty string
	 * @throws InternalErrorException
	 */
	String loadPasswordResetRequest(User user, int request) throws InternalErrorException;

	/**
	 * Removes all password reset requests associated with user.
	 * This is used when deleting user from Perun.
	 *
	 * @param sess PerunSession
	 * @param user User to remove all pwdreset requests
	 * @throws InternalErrorException
	 */
	void removeAllPasswordResetRequests(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get count of all users.
	 *
	 * @param perunSession
	 *
	 * @return count of all users
	 *
	 * @throws InternalErrorException
	 */
	int getUsersCount(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Generate user account in a backend system associated with login-namespace in Perun.
	 *
	 * This method consumes optional parameters map. Requirements are implementation-dependant
	 * for each login-namespace.
	 *
	 * Returns map with
	 * 1: key=login-namespace attribute urn, value=generated login
	 * 2: rest of opt response attributes...
	 *
	 * @param session
	 * @param namespace Namespace to generate account in
	 * @param parameters Optional parameters
	 * @return Map of data from backed response
	 * @throws InternalErrorException
	 */
	Map<String,String> generateAccount(PerunSession session, String namespace, Map<String, String> parameters) throws InternalErrorException;

	/**
	 * Return instance of PasswordManagerModule for specified namespace or throw exception.
	 *
	 * @param session
	 * @param namespace Namespace to get PWDMGR module.
	 * @return
	 * @throws InternalErrorException
	 */
	public PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace) throws InternalErrorException;

}
