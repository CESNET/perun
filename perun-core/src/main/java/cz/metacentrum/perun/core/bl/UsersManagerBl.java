package cz.metacentrum.perun.core.bl;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;

/**
 * UsersManager manages users.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Zora Sebestianova
 * @author Sona Mastrakova
 */
public interface UsersManagerBl {

	/**
	 * Returns user by his login in external source and external source.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return selected user or throws  in case the user doesn't exists
	 * @throws InternalErrorException
	 */
	User getUserByUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get the user based on one of the userExtSource.
	 *
	 * @param sess
	 * @param userExtSources
	 * @return user
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	User getUserByUserExtSources(PerunSession sess, List<UserExtSource> userExtSources) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get all the users who have given type of the ExtSource and login.
	 *
	 * @param perunSession  perun session
	 * @param extSourceType type of the user extSource
	 * @param login         login of the user
	 * @return all users with given parameters
	 * @throws InternalErrorException
	 */
	List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) throws InternalErrorException;

	/**
	 * Returns user by his/her id.
	 *
	 * @param perunSession
	 * @param id
	 * @return user
	 * @throws InternalErrorException
	 */
	User getUserById(PerunSession perunSession, int id) throws InternalErrorException, UserNotExistsException;

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
	 * Return all specificUsers who are owned by the user and their ownership is not in status disabled
	 *
	 * @param sess
	 * @param user the user
	 * @return list of specific users who are owned by the user
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
	 * @param user        the user
	 * @param specificUser the specificUser
	 * @throws InternalErrorException
	 * @throws RelationNotExistsException if there is no such user (the user) to remove
	 * @throws SpecificUserMustHaveOwnerException if there is the last user to remove
	 * @throws SpecificUserOwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, RelationNotExistsException, SpecificUserMustHaveOwnerException, SpecificUserOwnerAlreadyRemovedException;

	/**
	 * Add specificUser owner (the user)
	 * If not exists, create new ownership.
	 * If exists, only enable ownership for user and specificUser
	 *
	 * @param sess
	 * @param user the user
	 * @param specificUser the specificUser
	 * @throws InternalErrorException
	 * @throws RelationExistsException If there is such user (the user) who try to add
	 */
	void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws InternalErrorException, RelationExistsException;

	/**
	 * Return true if ownership of user and specificUser already exists.
	 * Return false if not.
	 * <p/>
	 * Looking for enabled and also for disabled ownership.
	 *
	 * @param sess
	 * @param user
	 * @param specificUser
	 * @return
	 * @throws InternalErrorException
	 */
	boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) throws InternalErrorException;

	/**
	 * Return all specific Users (only specific users)
	 *
	 * @param sess
	 * @return list of all specific users in perun
	 * @throws InternalErrorException
	 */
	List<User> getSpecificUsers(PerunSession sess) throws InternalErrorException;

	/**
	 * Get user by extSourceName and extSourceLogin
	 *
	 * @param sess
	 * @param extSourceName
	 * @param extLogin
	 * @return user
	 * @throws ExtSourceNotExistsException
	 * @throws UserExtSourceNotExistsException
	 * @throws UserNotExistsException
	 * @throws InternalErrorException
	 */
	User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, InternalErrorException;

	/**
	 * Returns all users (included specific users)
	 *
	 * @param sess
	 * @return list of all users
	 * @throws InternalErrorException
	 */
	List<User> getUsers(PerunSession sess) throws InternalErrorException;

	/**
	 * Get User to RichUser without attributes.
	 *
	 * @param sess
	 * @param user
	 * @return
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	RichUser getRichUser(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get User to RichUser with attributes.
	 *
	 * @param sess
	 * @param user
	 * @return
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	RichUser getRichUserWithAttributes(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get All richUsers with or without specificUsers.
	 * If includedSpecificUsers is true, you got all Users included specificUsers
	 * If includedSpecificUsers is false, you get all Users without specificUsers
	 *
	 * @param sess
	 * @param includedSpecificUsers true or false if you want or dont want get specificUsers too
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get All richUsers with or without specificUsers.
	 * If includedSpecificUsers is true, you got all Users included specificUsers
	 * If includedSpecificUsers is false, you get all Users without specificUsers
	 * <p/>
	 * This method get all RichUsers included Attributes.
	 *
	 * @param sess
	 * @param includedSpecificUsers true or false if you want or dont want get specificUsers too
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers) throws InternalErrorException, UserNotExistsException;

	/**
	 * From Users makes RichUsers without attributes.
	 *
	 * @param sess
	 * @param users users to convert
	 * @return list of richUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, UserNotExistsException;

	/**
	 * From Users makes RichUsers with attributes.
	 *
	 * @param sess
	 * @param users users to convert
	 * @return list of richUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersWithAttributesFromListOfUsers(PerunSession sess, List<User> users) throws InternalErrorException, UserNotExistsException;

	/**
	 * Convert RichUsers without attribute to RichUsers with specific attributes.
	 * Specific by list of Attributes.
	 * If in list of Attributes is some notUser attribute, it is skipped.
	 *
	 * @param sess
	 * @param richUsers
	 * @param attrsDef
	 * @return list of RichUsers with specific attributes
	 * @throws InternalErrorException
	 */
	List<RichUser> convertUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers, List<AttributeDefinition> attrsDef) throws InternalErrorException;

	/**
	 * Inserts user into DB.
	 *
	 * @param perunSession
	 * @param user
	 * @throws InternalErrorException
	 */
	User createUser(PerunSession perunSession, User user) throws InternalErrorException;

	/**
	 * Deletes user.
	 *
	 * @param perunSession
	 * @param user
	 * @throws InternalErrorException
	 * @throws RelationExistsException             if user has some members assigned
	 * @throws MemberAlreadyRemovedException       if there is at least 1 member deleted but not affected by deleting from DB
	 * @throws UserAlreadyRemovedException         if there are no rows affected by deleting user in DB
	 * @throws SpecificUserAlreadyRemovedException if there are no rows affected by deleting specific user in DB
	 * @throws GroupOperationsException	           if something went wrong while processing relations
	 */
	void deleteUser(PerunSession perunSession, User user) throws InternalErrorException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, GroupOperationsException;

	/**
	 * Deletes user. If forceDelete is true, then removes also associated members.
	 *
	 * @param perunSession
	 * @param user
	 * @param forceDelete  if true, deletes also all members if they are assigned to the user
	 * @throws InternalErrorException
	 * @throws RelationExistsException             if forceDelete is false and the user has some members assigned
	 * @throws MemberAlreadyRemovedException       if there is at least 1 member deleted but not affected by deleting from DB
	 * @throws UserAlreadyRemovedException         if there are no rows affected by deleting user in DB
	 * @throws SpecificUserAlreadyRemovedException if there are no rows affected by deleting specific user in DBn
	 * @throws GroupOperationsException	           if something went wrong while processing relations
	 */
	void deleteUser(PerunSession perunSession, User user, boolean forceDelete) throws InternalErrorException, RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, GroupOperationsException;

	/**
	 * Updates users data in DB.
	 *
	 * @param perunSession
	 * @param user
	 * @return updated user
	 * @throws InternalErrorException
	 * @throws UserNotExistsException if user not exists when method trying to update him
	 */
	User updateUser(PerunSession perunSession, User user) throws InternalErrorException, UserNotExistsException;

	/**
	 * Updates titles before/after users name.
	 * <p/>
	 * New titles must be set inside User object.
	 * Setting any title to null will remove title from name.
	 * Other user's properties are ignored.
	 *
	 * @param perunSession
	 * @param user
	 * @return updated user with new titles before/after name
	 * @throws InternalErrorException
	 * @throws UserNotExistsException if user not exists when method trying to update him
	 */
	User updateNameTitles(PerunSession perunSession, User user) throws InternalErrorException, UserNotExistsException;

	/**
	 * Updates user's userExtSource in DB.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated userExtSource
	 * @throws InternalErrorException
	 */
	UserExtSource updateUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;

	/**
	 * Updates user's userExtSource last access time in DB. We can get information which userExtSource has been used as a last one.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated userExtSource
	 * @throws InternalErrorException
	 */
	void updateUserExtSourceLastAccess(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;

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
	 * Gets list of all user's external sources of the user.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of user's external sources
	 * @throws InternalErrorException
	 */
	List<UserExtSource> getUserExtSources(PerunSession perunSession, User user) throws InternalErrorException;

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
	 * @return user external auth object with newly generated ID
	 * @throws InternalErrorException
	 * @throws UserExtSourceExistsException
	 */
	UserExtSource addUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceExistsException;

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
	 * Gets user's external source by the user's external login and external source.
	 *
	 * @param perunSession
	 * @param source
	 * @param extLogin
	 * @return user external source object
	 * @throws InternalErrorException
	 */
	UserExtSource getUserExtSourceByExtLogin(PerunSession perunSession, ExtSource source, String extLogin) throws InternalErrorException, UserExtSourceNotExistsException;

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
	 * Returns list of VOs, where the user is a member.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of VOs, where the user is a member.
	 * @throws InternalErrorException
	 */
	List<Vo> getVosWhereUserIsMember(PerunSession perunSession, User user) throws InternalErrorException;

	/**
	 * Get all resources from the facility which have the user access on.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @return list of resources which have the user access on
	 * @throws InternalErrorException
	 */
	List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get all resources from the facility where the user is assigned.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @return list of resources which have the user access on
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get all resources which have the user access on.
	 *
	 * @param sess
	 * @param user
	 * @return list of resources which have the user access on
	 * @throws InternalErrorException
	 */
	List<Resource> getAllowedResources(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get all resources where the user is assigned.
	 *
	 * @param sess
	 * @param user
	 * @return list of resources which have the user access on
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get all rich resources where the user is assigned.
	 *
	 * @param sess
	 * @param user
	 * @return list of rich resources which have the user access on
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, User user) throws InternalErrorException;

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
	 * Returns all users who have set the attribute with the value. Searching by attributeName. Searching only def and opt attributes.
	 * Can find only attributes with String Value by this way! (not Integer, Map or List)
	 *
	 * @param sess
	 * @param attributeName
	 * @param attributeValue
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttribute(PerunSession sess, String attributeName, String attributeValue) throws InternalErrorException;

	/**
	 * Returns all users who have the attribute with the value. attributeValue is not converted to the attribute type, it is always type of String.
	 *
	 * @param sess
	 * @param attributeName
	 * @param attributeValue
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttributeValue(PerunSession sess, String attributeName, String attributeValue) throws InternalErrorException;

	/**
	 * Returns list of users' who matches the searchString, searching name, email and logins.
	 *
	 * @param sess
	 * @param searchString
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException;

	/**
	 * Returns list of richusers with attributes who matches the searchString, searching name, email, logins.
	 *
	 * @param sess
	 * @param searchString
	 * @return list of richusers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> findRichUsers(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException;

	/**
	 * Returns list of richusers with attributes who matches the searchString, searching name, email, logins.
	 * Name part is searched for exact match.
	 *
	 * @param sess
	 * @param searchString
	 * @return list of richusers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> findRichUsersByExactMatch(PerunSession sess, String searchString) throws InternalErrorException, UserNotExistsException;

	/**
	 * Return list of users who matches the searchString, searching name, email and logins
	 * and are not member in specific VO.
	 *
	 * @param sess
	 * @param vo
	 * @param searchString
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException;

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
	 * Checks if the login is available in the namespace.
	 *
	 * @param sess
	 * @param loginNamespace in which the login will be checked (provide only the name of the namespace, not the whole attribute name)
	 * @param login          to be checked
	 * @return true if login available, false otherwise
	 * @throws InternalErrorException
	 */
	boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InternalErrorException;

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
	 * Returns all RichUsers with attributes who are not member of any VO.
	 *
	 * @param sess
	 * @return list of richUsers who are not member of any VO
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException, UserNotExistsException;


	/**
	 * Adds PERUNADMIN role to the user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException;

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
	 * !!! Not Complete yet, need to implement all perunBeans !!!
	 * <p/>
	 * Get perunBean and try to find all connected Users
	 *
	 * @param sess
	 * @param perunBean
	 * @return list of users connected with perunBeans
	 * @throws InternalErrorException
	 */
	List<User> getUsersByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException;

	/**
	 * Changes user password in defined login-namespace. If checkOldPassword is true, then ask authentication system if old password is correct. user must exists.
	 *
	 * @param sess
	 * @param user             user object which is used to get userLogin from the loginNamespace
	 * @param oldPassword
	 * @param newPassword
	 * @param checkOldPassword
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws LoginNotExistsException
	 * @throws PasswordDoesntMatchException
	 * @throws PasswordChangeFailedException
	 */
	void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword)
			throws InternalErrorException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException;

	/**
	 * Creates the password in external system. User must not exists.
	 *
	 * @param sess
	 * @param userLogin      string representation of the userLogin
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 */
	@Deprecated
	void createPassword(PerunSession sess, String userLogin, String loginNamespace, String password)
			throws InternalErrorException, PasswordCreationFailedException;

	/**
	 * Creates the password in external system. User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException
	 */
	@Deprecated
	void createPassword(PerunSession sess, User user, String loginNamespace, String password)
			throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException;

	/**
	 * Reserves random password in external system. User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException
	 */
	void reserveRandomPassword(PerunSession sess, User user, String loginNamespace) throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException;

	/**
	 * Reserves the password in external system. User must not exists.
	 *
	 * @param sess
	 * @param userLogin      string representation of the userLogin
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 */
	void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password)
			throws InternalErrorException, PasswordCreationFailedException;

	/**
	 * Reserves the password in external system. User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException
	 */
	void reservePassword(PerunSession sess, User user, String loginNamespace, String password)
			throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException;

	/**
	 * Validates the password in external system. User must not exists.
	 *
	 * @param sess
	 * @param userLogin      string representation of the userLogin
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 */
	void validatePassword(PerunSession sess, String userLogin, String loginNamespace)
			throws InternalErrorException, PasswordCreationFailedException;

	/**
	 * Validates the password in external system. User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException
	 */
	void validatePassword(PerunSession sess, User user, String loginNamespace)
			throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException;

	/**
	 * Validates the password in external system and set user extSources and extSource related attributes. User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param userLogin
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException
	 * @throws ExtSourceNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	public void validatePasswordAndSetExtSources(PerunSession sess, User user, String userLogin, String loginNamespace) throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Deletes password in external system. User must not exists.
	 *
	 * @param sess
	 * @param userLogin
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordDeletionFailedException
	 * @throws LoginNotExistsException
	 */
	void deletePassword(PerunSession sess, String userLogin, String loginNamespace)
			throws InternalErrorException, PasswordDeletionFailedException, LoginNotExistsException;

	/**
	 * Creates alternative password in external system.
	 *
	 * @param sess
	 * @param user
	 * @param description - description of a password (e.g. 'mobile phone', 'tablet', ...)
	 * @param loginNamespace
	 * @param password string representation of password
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException
	 */
	void createAlternativePassword(PerunSession sess, User user, String description, String loginNamespace, String password) throws InternalErrorException, PasswordCreationFailedException, LoginNotExistsException;

	/**
	 * Deletes alternative password in external system.
	 *
	 * @param sess
	 * @param loginNamespace
	 * @param passwordId passwords ID
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws PasswordDeletionFailedException
	 * @throws LoginNotExistsException
	 */
	void deleteAlternativePassword(PerunSession sess, User user, String loginNamespace, String passwordId) throws InternalErrorException, PasswordDeletionFailedException, LoginNotExistsException;


	/**
	 * Check if login in specified namespace exists.
	 *
	 * @param sess
	 * @param namespace namespace for login
	 * @param login     login to check
	 * @throws InternalErrorException
	 * @throws AlreadyReservedLoginException throw this exception if login already exist in table of reserved logins
	 */
	void checkReservedLogins(PerunSession sess, String namespace, String login) throws InternalErrorException, AlreadyReservedLoginException;

	void checkUserExists(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException;

	void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException;

	boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws InternalErrorException;

	/**
	 * From List of Users make list of RichUsers (without attributes)
	 *
	 * @param sess
	 * @param users
	 * @return list of RIch Users without attributes
	 * @throws InternalErrorException
	 */
	List<RichUser> convertUsersToRichUsers(PerunSession sess, List<User> users) throws InternalErrorException;

	/**
	 * From List of Rich Users without attribute make list of Rich Users with attributes
	 *
	 * @param sess
	 * @param richUsers
	 * @return list of Rich Users with attributes
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> convertRichUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers) throws InternalErrorException, UserNotExistsException;

	/**
	 * From List of Users make list of RichUsers (with attributes by names)
	 *
	 * @param sess
	 * @param users
	 * @return list of RIch Users without attributes
	 * @throws InternalErrorException
	 */
	List<RichUser> convertUsersToRichUsersWithAttributesByNames(PerunSession sess, List<User> users, List<String> attrNames) throws InternalErrorException;

	/**
	 * For richUser filter all his user attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richUser
	 * @return richUser with only allowed attributes
	 * @throws InternalErrorException
	 */
	RichUser filterOnlyAllowedAttributes(PerunSession sess, RichUser richUser) throws InternalErrorException;

	/**
	 * For list of richUser filter all their user attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richUsers
	 * @return list of RichUsers with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichUser> filterOnlyAllowedAttributes(PerunSession sess, List<RichUser> richUsers) throws InternalErrorException;

	/**
	 * Return list of RichUsers who matches the searchString, searching name, email and logins
	 * and are not member in specific VO and contain selected attributes.
	 *
	 * @param sess
	 * @param vo
	 * @param searchString
	 * @param attrsName
	 * @return list of RichUser
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws VoNotExistsException
	 */
	List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws InternalErrorException, UserNotExistsException, VoNotExistsException;

	/**
	 * Return list of RichUsers which are not members of any VO and contain selected attributes.
	 *
	 * @param sess
	 * @param attrsName
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws InternalErrorException, VoNotExistsException, UserNotExistsException;

	/**
	 * Returns list of RichUsers with selected attributes who matches the searchString, searching name, email, logins.
	 *
	 * @param sess
	 * @param searchString
	 * @param attrNames
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrNames) throws InternalErrorException, UserNotExistsException;

	/**
	 * Returns list of RichUsers with selected attributes who matches the searchString, searching name, email, logins.
	 * Name part is searched for exact match.
	 *
	 * @param sess
	 * @param searchString
	 * @param attrNames
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrNames) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get User to RichUser with attributes.
	 *
	 * @param sess
	 * @param includedSpecificUsers
	 * @param attrsNames
	 * @return
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers, List<String> attrsNames) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get All RichUsers without UserExtSources and without virtual attributes.
	 *
	 * @param sess
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 */
	List<RichUser> getAllRichUsersWithAllNonVirutalAttributes(PerunSession sess) throws InternalErrorException;

	/**
	 * Allow users to manually add login in supported namespace if same login is not reserved
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @param login
	 * @throws InternalErrorException
	 */
	void setLogin(PerunSession sess, User user, String loginNamespace, String login) throws InternalErrorException;

	/**
	 * Request change of user's preferred email address.
	 * Change in attribute value is not done, until email
	 * address is verified by link in email notice.
	 * (urn:perun:user:attribute-def:def:preferredEmail)
	 *
	 * @param sess  PerunSession
	 * @param url   base URL of running perun instance passed from RPC.
	 * @param user  User to request preferred email change for
	 * @param email new email address
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	void requestPreferredEmailChange(PerunSession sess, String url, User user, String email) throws InternalErrorException, UserNotExistsException;

	/**
	 * * Validate change of user's preferred email address.
	 * New email address is set as value of
	 * urn:perun:user:attribute-def:def:preferredEmail attribute.
	 *
	 * @param sess PerunSession
	 * @param user User to validate email address for
	 * @param i    decrypted parameter
	 * @param m    encrypted parameter
	 * @return String return new preferred email
	 * @throws InternalErrorException
	 * @throws UserNotExistsException                When user from session is null
	 * @throws WrongAttributeValueException          If new email address is in wrong format
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException           If user:preferredEmail attribute doesn't exists.
	 * @throws WrongReferenceAttributeValueException
	 */
	String validatePreferredEmailChange(PerunSession sess, User user, String i, String m) throws InternalErrorException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Return list of email addresses of user, which are
	 * awaiting validation and are inside time window
	 * for validation.
	 * <p/>
	 * If there is no preferred email change request pending
	 * or requests are outside time window for validation,
	 * returns empty list.
	 *
	 * @param sess PerunSession
	 * @param user User to check pending request for
	 * @return List<String> user's email addresses pending validation
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 */
	List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException;

	/**
	 * Get user and convert values of his object attributes:
	 * - firstName
	 * - lastName
	 * - middleName
	 * - titleBefore
	 * - titleAfter
	 * from emptyString (like "") to null.
	 * <p/>
	 * If these values are not empty strings, do not change them.
	 * If user is null, return null.
	 *
	 * @param user user to converting
	 * @return converted user
	 */
	User convertUserEmptyStringsInObjectAttributesIntoNull(User user);

	/**
	 * Changes user password in defined login-namespace using encrypted parameters.
	 *
	 * @param sess     PerunSession
	 * @param user     user to change password for
	 * @param m        encrypted parameter
	 * @param password password to set
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws LoginNotExistsException
	 * @throws PasswordChangeFailedException
	 */
	void changeNonAuthzPassword(PerunSession sess, User user, String m, String password)
			throws InternalErrorException, UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException;

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

}
