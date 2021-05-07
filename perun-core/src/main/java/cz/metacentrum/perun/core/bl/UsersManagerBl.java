package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordChangeFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDoesntMatchException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkExpiredException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkNotValidException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthFailedException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;

import java.util.List;
import java.util.Map;

/**
 * UsersManager manages users.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Zora Sebestianova
 * @author Sona Mastrakova
 */
public interface UsersManagerBl {

	String ORIGIN_IDENTITY_PROVIDER_KEY = "originIdentityProvider";
	String MULTIVALUE_ATTRIBUTE_SEPARATOR_REGEX = ";";
	String ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME = "additionalIdentifiers";
	String ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME = AttributesManager.NS_UES_ATTR_DEF + ":" + ADDITIONAL_IDENTIFIERS_ATTRIBUTE_NAME;

	/**
	 * Returns user by his login in external source and external source.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return selected user or throws UserNotExistsException in case the user doesn't exists
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	User getUserByUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws UserNotExistsException;

	/**
	 * Get the user based on one of the userExtSource.
	 *
	 * @param sess
	 * @param userExtSources
	 * @return user
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	User getUserByUserExtSources(PerunSession sess, List<UserExtSource> userExtSources) throws UserNotExistsException;

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
	 * Returns user by his/her id.
	 *
	 * @param perunSession
	 * @param id
	 * @return user
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
	 * Return all specificUsers who are owned by the user and their ownership is not in status disabled
	 *
	 * @param sess
	 * @param user the user
	 * @return list of specific users who are owned by the user
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
	 * @param user        the user
	 * @param specificUser the specificUser
	 * @throws InternalErrorException
	 * @throws RelationNotExistsException if there is no such user (the user) to remove
	 * @throws SpecificUserOwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void removeSpecificUserOwner(PerunSession sess, User user, User specificUser) throws RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException;

	/**
	 * Remove specificUser owner (the user).
	 * If forceDelete false, only disable ownership of user and specificUser.
	 * If forceDelete true, delete this ownership from DB.
	 *
	 * @param sess
	 * @param user        the user
	 * @param specificUser the specificUser
	 * @param forceDelete if true, remove from database, if false, only disable this ownership
	 * @throws InternalErrorException
	 * @throws RelationNotExistsException if there is no such user (the user) to remove
	 * @throws SpecificUserOwnerAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void removeSpecificUserOwner(PerunSession sess, User user, User specificUser, boolean forceDelete) throws RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException;

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
	void addSpecificUserOwner(PerunSession sess, User user, User specificUser) throws RelationExistsException;

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
	boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser);

	/**
	 * Return all specific Users (only specific users)
	 *
	 * @param sess
	 * @return list of all specific users in perun
	 * @throws InternalErrorException
	 */
	List<User> getSpecificUsers(PerunSession sess);

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
	User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException;

	/**
	 * Returns all users (included specific users)
	 *
	 * @param sess
	 * @return list of all users
	 * @throws InternalErrorException
	 */
	List<User> getUsers(PerunSession sess);

	/**
	 * Get User to RichUser without attributes.
	 *
	 * @param sess
	 * @param user
	 * @return
	 * @throws InternalErrorException
	 */
	RichUser getRichUser(PerunSession sess, User user);

	/**
	 * Get User to RichUser with attributes.
	 *
	 * @param sess
	 * @param user
	 * @return
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	RichUser getRichUserWithAttributes(PerunSession sess, User user) throws UserNotExistsException;

	/**
	 * Get All richUsers with or without specificUsers.
	 * If includedSpecificUsers is true, you got all Users included specificUsers
	 * If includedSpecificUsers is false, you get all Users without specificUsers
	 *
	 * @param sess
	 * @param includedSpecificUsers true or false if you want or dont want get specificUsers too
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 */
	List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers);

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
	List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers) throws UserNotExistsException;

	/**
	 * Returns rich users without attributes by their ids.
	 *
	 * @param sess
	 * @param ids
	 * @return list of rich users with specified ids
	 * @throws InternalErrorException
	 */
	List<RichUser> getRichUsersByIds(PerunSession sess, List<Integer> ids);

	/**
	 * Returns rich users with attributes by their ids.
	 *
	 * @param sess
	 * @param ids
	 * @return list of rich users with specified ids
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersWithAttributesByIds(PerunSession sess, List<Integer> ids) throws UserNotExistsException;

	/**
	 * From Users makes RichUsers without attributes.
	 *
	 * @param sess
	 * @param users users to convert
	 * @return list of richUsers
	 * @throws InternalErrorException
	 */
	List<RichUser> getRichUsersFromListOfUsers(PerunSession sess, List<User> users);

	/**
	 * From Users makes RichUsers with attributes.
	 *
	 * @param sess
	 * @param users users to convert
	 * @return list of richUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersWithAttributesFromListOfUsers(PerunSession sess, List<User> users) throws UserNotExistsException;

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
	List<RichUser> convertUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers, List<AttributeDefinition> attrsDef);

	/**
	 * Inserts user into DB.
	 *
	 * @param perunSession
	 * @param user
	 * @throws InternalErrorException
	 */
	User createUser(PerunSession perunSession, User user);

	/**
	 * Set specific user type for specific user and set ownership of this user for the owner.
	 *
	 * @param sess perun session
	 * @param specificUser specific user
	 * @param specificUserType specific type of user
	 * @param owner user, who will be owner of the specific user
	 *
	 * @return specific user with specific user type set
	 *
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 */
	User setSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType, User owner) throws RelationExistsException;

	/**
	 * Remove all ownerships of this specific user and unset this specific user type from this specific user.
	 *
	 * @param sess perun session
	 * @param specificUser specific user
	 * @param specificUserType specific type of user
	 *
	 * @return user who is no more specific
	 *
	 * @throws InternalErrorException
	 */
	User unsetSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType);

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
	 */
	void deleteUser(PerunSession perunSession, User user) throws RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException;

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
	 */
	void deleteUser(PerunSession perunSession, User user, boolean forceDelete) throws RelationExistsException, MemberAlreadyRemovedException, UserAlreadyRemovedException, SpecificUserAlreadyRemovedException;

	/**
	 * Anonymizes user - according to configuration, each of user's attributes is either
	 * anonymized, kept untouched or deleted. Also deletes other user's related data, e.g.
	 * authorships of users publications, mail change and password reset requests, bans...
	 *
	 * @param perunSession
	 * @param user
	 * @throws InternalErrorException
	 * @throws RelationExistsException if the user has some members assigned
	 * @throws AnonymizationNotSupportedException if an attribute should be anonymized but its module doesn't specify the anonymization process
	 */
	void anonymizeUser(PerunSession perunSession, User user) throws RelationExistsException, AnonymizationNotSupportedException;

	/**
	 * Updates users data in DB.
	 *
	 * @param perunSession
	 * @param user
	 * @return updated user
	 * @throws InternalErrorException
	 * @throws UserNotExistsException if user not exists when method trying to update him
	 */
	User updateUser(PerunSession perunSession, User user) throws UserNotExistsException;

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
	User updateNameTitles(PerunSession perunSession, User user) throws UserNotExistsException;

	/**
	 * Updates user's userExtSource in DB.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated userExtSource
	 * @throws InternalErrorException
	 * @throws UserExtSourceExistsException When UES with same login/extSource already exists.
	 */
	UserExtSource updateUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws UserExtSourceExistsException;

	/**
	 * Updates user's userExtSource last access time in DB. We can get information which userExtSource has been used as a last one.
	 *
	 * @param perunSession
	 * @param userExtSource
	 * @return updated userExtSource
	 * @throws InternalErrorException
	 */
	void updateUserExtSourceLastAccess(PerunSession perunSession, UserExtSource userExtSource);

	/**
	 * Get user by principal's additional identifiers or extSourceName and extSourceLogin.
	 * Additional identifiers are used in case principal's extSource was send through proxy which has enabled multiple identifiers.
	 * extSourceName and extSourceLogin are used otherwise.
	 *
	 * @param sess
	 * @param principal
	 * @return
	 * @throws UserExtSourceNotExistsException
	 * @throws UserNotExistsException
	 * @throws ExtSourceNotExistsException
	 */
	User getUserByExtSourceInformation(PerunSession sess, PerunPrincipal principal) throws UserExtSourceNotExistsException, UserNotExistsException, ExtSourceNotExistsException;

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
	 * Gets list of all user's external sources of the user.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of user's external sources
	 * @throws InternalErrorException
	 */
	List<UserExtSource> getUserExtSources(PerunSession perunSession, User user);

	/**
	 * Gets list of all user's external sources with attributes. If any of the attribute names is incorrect
	 * then the value is silently skipped. If the attrsNames is null, then this method returns all ues attributes.
	 *
	 * @param sess session
	 * @param user user for who should be the data returned
	 * @param attrsNames list of attribute names that should be found, if null or empty return all
	 * @return list of user's external sources with attributes
	 * @throws InternalErrorException internal error
	 */
	List<RichUserExtSource> getRichUserExtSources(PerunSession sess, User user, List<String> attrsNames);

	/**
	 * From given list of {@link RichUserExtSource} removes the attributes which are not allowed for
	 * the current principal. The attributes are removed from the given list and the list is also returned.
	 *
	 *
	 * @param sess session
	 * @param richUserExtSources richUserExtSources to be filtered
	 * @return list of filtered richUserExtSources
	 */
	List<RichUserExtSource> filterOnlyAllowedAttributesForRichUserExtSources(PerunSession sess, List<RichUserExtSource> richUserExtSources);

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
	 * Return userExtSource for specific attribute definition (specified by id) and unique value.
	 * If not found, throw and exception.
	 *
	 * It looks for exactly one value of the specific attribute type:
	 * - Integer -> exactly match
	 * - String -> exactly match
	 * - Map -> exactly match of "key=value"
	 * - ArrayList -> exactly match of one of the value
	 *
	 * @param sess
	 * @param attrId attribute id used for founding attribute definition which has to exists, be unique and in userExtSource namespace
	 * @param uniqueValue value used for searching
	 *
	 * @return userExtSource found by attribute id and it's unique value
	 *
	 * @throws InternalErrorException if attrId or uniqueValue is in incorrect format
	 * @throws UserExtSourceNotExistsException if userExtSource can't be found
	 * @throws AttributeNotExistsException if attribute can't be found by it's id
	 */
	UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue) throws AttributeNotExistsException, UserExtSourceNotExistsException;

	/**
	 * Return userExtSource for specific attribute definition (specified by id) and unique value.
	 * If not found, throw and exception.
	 *
	 * It looks for exactly one value of the specific attribute type:
	 * - Integer -> exactly match
	 * - String -> exactly match
	 * - Map -> exactly match of "key=value"
	 * - ArrayList -> exactly match of one of the value
	 *
	 * @param sess
	 * @param attrName attribute name used for founding attribute definition which has to exists, be unique and in userExtSource namespace
	 * @param uniqueValue value used for searching
	 *
	 * @return userExtSource found by attribute name and it's unique value
	 *
	 * @throws InternalErrorException if attrName or uniqueValue is in incorrect format
	 * @throws UserExtSourceNotExistsException if userExtSource can't be found
	 * @throws AttributeNotExistsException if attribute can't be found by it's name
	 */
	UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, String attrName, String uniqueValue) throws AttributeNotExistsException, UserExtSourceNotExistsException;

	/**
	 * Iteratively searches through additional identifiers trying to find userExtSource with the same identifier.
	 * Returns first found userExtSource or throw an exception when no matching userExtSource is found.
	 *
	 * @param sess PerunSession to retrieve UserExtSource
	 * @param principal PerunPrincipal which contains additionalIdentifiers
	 *
	 * @return UserExtSource found using additionalIdentifiers
	 *
	 * @throws UserExtSourceNotExistsException When no matching userExtSource is found
	 */
	UserExtSource getUserExtSourceFromMultipleIdentifiers(PerunSession sess, PerunPrincipal principal) throws UserExtSourceNotExistsException;

	/**
	 * Get user ext sources by their ids.
	 *
	 * @param sess
	 * @param ids
	 * @return list of user external sources with specified ids
	 * @throws InternalErrorException
	 */
	List<UserExtSource> getUserExtSourcesByIds(PerunSession sess, List<Integer> ids);

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
	 * Adds user's external sources.
	 *
	 * @param perunSession
	 * @param user
	 * @param userExtSource
	 * @return user external auth object with newly generated ID
	 * @throws InternalErrorException
	 * @throws UserExtSourceExistsException
	 */
	UserExtSource addUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws UserExtSourceExistsException;

	/**
	 * Removes user's external sources.
	 * It also means removing all it's attributes.
	 *
	 * @param perunSession
	 * @param user
	 * @param userExtSource
	 * @throws InternalErrorException
	 * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void removeUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws UserExtSourceAlreadyRemovedException;

	/**
	 * Take UserExtSource from sourceUser and move it to the targetUser.
	 *
	 * It removes old UserExtSource with all it's attributes from sourceUser and creates and assigns the new one with
	 * the same settings to target user.
	 *
	 * @param perunSession
	 * @param sourceUser user with UserExtSource to move
	 * @param targetUser user for who will be UserExtSource moved
	 * @param userExtSource the UserExtSource which will be moved from sourceUser to targetUser
	 *
	 * @throws InternalErrorException
	 */
	void moveUserExtSource(PerunSession perunSession, User sourceUser, User targetUser, UserExtSource userExtSource);

	/**
	 * Gets user's external source by the user's external login and external source.
	 *
	 * @param perunSession
	 * @param source
	 * @param extLogin
	 * @return user external source object
	 * @throws InternalErrorException
	 */
	UserExtSource getUserExtSourceByExtLogin(PerunSession perunSession, ExtSource source, String extLogin) throws UserExtSourceNotExistsException;

	/**
	 * Returns list of VOs, where the user is an Administrator.
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
	 * Returns list of VOs, where the user is a member.
	 *
	 * @param perunSession
	 * @param user
	 * @return list of VOs, where the user is a member.
	 * @throws InternalErrorException
	 */
	List<Vo> getVosWhereUserIsMember(PerunSession perunSession, User user);

	/**
	 * Get all resources from the facility which have the user access on.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @return list of resources which have the user access on
	 * @throws InternalErrorException
	 */
	List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user);

	/**
	 * Get all resources from the facility where the user is assigned.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 * @return list of resources which have the user access on
	 */
	List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user);

	/**
	 * Get all resources which have the user access on.
	 *
	 * @param sess
	 * @param user
	 * @return list of resources which have the user access on
	 */
	List<Resource> getAllowedResources(PerunSession sess, User user);

	/**
	 * Get all resources where the user is assigned.
	 *
	 * @param sess
	 * @param user
	 * @return list of resources which have the user access on
	 */
	List<Resource> getAssignedResources(PerunSession sess, User user);

	/**
	 * Get all rich resources where the user is assigned.
	 *
	 * @param sess
	 * @param user
	 * @return list of rich resources which have the user access on
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, User user);

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
	 * Returns all users who have set the attribute with the value. Searching by attributeName. Searching only def and opt attributes.
	 * Can find only attributes with String Value by this way! (not Integer, Map or List)
	 *
	 * @param sess
	 * @param attributeName
	 * @param attributeValue
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttribute(PerunSession sess, String attributeName, String attributeValue);

	/**
	 * Returns all users who have the attribute with the value. attributeValue is not converted to the attribute type, it is always type of String.
	 *
	 * @param sess
	 * @param attributeName
	 * @param attributeValue
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getUsersByAttributeValue(PerunSession sess, String attributeName, String attributeValue);

	/**
	 * Returns list of users' who matches the searchString, searching name, email and logins.
	 *
	 * @param sess
	 * @param searchString
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> findUsers(PerunSession sess, String searchString);

	/**
	 * Returns list of richusers with attributes who matches the searchString, searching name, email, logins.
	 *
	 * @param sess
	 * @param searchString
	 * @return list of richusers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> findRichUsers(PerunSession sess, String searchString) throws UserNotExistsException;

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
	List<RichUser> findRichUsersByExactMatch(PerunSession sess, String searchString) throws UserNotExistsException;

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
	List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString);

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
	 * Checks if the login is available in the namespace. Returns FALSE is is already occupied,
	 * throws exception if value is not allowed.
	 *
	 * @param sess
	 * @param loginNamespace in which the login will be checked (provide only the name of the namespace, not the whole attribute name)
	 * @param login to be checked
	 * @return true if login is available, false otherwise
	 * @throws InvalidLoginException When login to check has invalid syntax or is not allowed.
	 */
	boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InvalidLoginException;

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
	 * Returns all RichUsers with attributes who are not member of any VO.
	 *
	 * @param sess
	 * @return list of richUsers who are not member of any VO
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws UserNotExistsException;

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
	 * Returns list of users connected with a group
	 *
	 * @param sess
	 * @param group
	 * @return list of users connected with group
	 * @throws InternalErrorException
	 */
	List<User> getUsersByPerunBean(PerunSession sess, Group group);

	/**
	 * Returns list of users connected with a member
	 *
	 * @param sess
	 * @param member
	 * @return list of users connected with member
	 * @throws InternalErrorException
	 */
	List<User> getUsersByPerunBean(PerunSession sess, Member member);

	/**
	 * Returns list of users connected with a resource
	 *
	 * @param sess
	 * @param resource
	 * @return list of users connected with resource
	 * @throws InternalErrorException
	 */
	List<User> getUsersByPerunBean(PerunSession sess, Resource resource);

	/**
	 * Returns list of users connected with a host
	 *
	 * @param sess
	 * @param host
	 * @return list of users connected with host
	 * @throws InternalErrorException
	 */
	List<User> getUsersByPerunBean(PerunSession sess, Host host);

	/**
	 * Returns list of users connected with a facility
	 *
	 * @param sess
	 * @param facility
	 * @return list of users connected with facility
	 * @throws InternalErrorException
	 */
	List<User> getUsersByPerunBean(PerunSession sess, Facility facility);

	/**
	 * Returns list of users connected with a vo
	 *
	 * @param sess
	 * @param vo
	 * @return list of users connected with vo
	 * @throws InternalErrorException
	 */
	List<User> getUsersByPerunBean(PerunSession sess, Vo vo);

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
	 * @throws PasswordDoesntMatchException
	 * @throws PasswordChangeFailedException
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 */
	void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword)
			throws LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException;

	/**
	 * Reserves random password in external system. User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 */
	void reserveRandomPassword(PerunSession sess, User user, String loginNamespace) throws PasswordCreationFailedException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException;

	/**
	 * Reserves the password in external system. User must not exists.
	 *
	 * @param sess
	 * @param userLogin      string representation of the userLogin
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 */
	void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password)
			throws PasswordCreationFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException;

	/**
	 * Reserves the password in external system. User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @param password
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 */
	void reservePassword(PerunSession sess, User user, String loginNamespace, String password)
			throws PasswordCreationFailedException, LoginNotExistsException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException;

	/**
	 * Validates the password in external system and sets user extSources and extSource related attributes.
	 * User must not exists.
	 *
	 * @param sess
	 * @param userLogin      string representation of the userLogin
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 */
	void validatePassword(PerunSession sess, String userLogin, String loginNamespace)
			throws PasswordCreationFailedException, InvalidLoginException;

	/**
	 * Validates the password in external system and sets user extSources and extSource related attributes.
	 * User must exists.
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 */
	void validatePassword(PerunSession sess, User user, String loginNamespace)
			throws PasswordCreationFailedException, LoginNotExistsException, InvalidLoginException;

	/**
	 * Deletes password in external system. User must not exists.
	 *
	 * @param sess
	 * @param userLogin
	 * @param loginNamespace
	 * @throws InternalErrorException
	 * @throws PasswordDeletionFailedException
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 */
	void deletePassword(PerunSession sess, String userLogin, String loginNamespace)
			throws PasswordDeletionFailedException, LoginNotExistsException, PasswordOperationTimeoutException, InvalidLoginException;

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
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 */
	void createAlternativePassword(PerunSession sess, User user, String description, String loginNamespace, String password) throws PasswordCreationFailedException, LoginNotExistsException, PasswordStrengthException;

	/**
	 * Deletes alternative password in external system.
	 *
	 * @param sess
	 * @param loginNamespace
	 * @param passwordId passwords ID
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws PasswordDeletionFailedException
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 */
	void deleteAlternativePassword(PerunSession sess, User user, String loginNamespace, String passwordId) throws PasswordDeletionFailedException, LoginNotExistsException;


	/**
	 * Check if login in specified namespace exists.
	 *
	 * @param sess
	 * @param namespace namespace for login
	 * @param login     login to check
	 * @throws InternalErrorException
	 * @throws AlreadyReservedLoginException throw this exception if login already exist in table of reserved logins
	 */
	void checkReservedLogins(PerunSession sess, String namespace, String login) throws AlreadyReservedLoginException;

	void checkUserExists(PerunSession sess, User user) throws UserNotExistsException;

	void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource) throws UserExtSourceNotExistsException;

	void checkUserExtSourceExistsById(PerunSession sess, int id) throws UserExtSourceNotExistsException;

	boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource);

	/**
	 * From List of Users make list of RichUsers (without attributes)
	 *
	 * @param sess
	 * @param users
	 * @return list of RIch Users without attributes
	 * @throws InternalErrorException
	 */
	List<RichUser> convertUsersToRichUsers(PerunSession sess, List<User> users);

	/**
	 * From List of Rich Users without attribute make list of Rich Users with attributes
	 *
	 * @param sess
	 * @param richUsers
	 * @return list of Rich Users with attributes
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> convertRichUsersToRichUsersWithAttributes(PerunSession sess, List<RichUser> richUsers) throws UserNotExistsException;

	/**
	 * From List of Users make list of RichUsers (with attributes by names)
	 *
	 * @param sess
	 * @param users
	 * @return list of RIch Users without attributes
	 * @throws InternalErrorException
	 */
	List<RichUser> convertUsersToRichUsersWithAttributesByNames(PerunSession sess, List<User> users, List<String> attrNames);

	/**
	 * From User make Rich user (with attributes by names)
	 *
	 * @param sess session
	 * @param user user to be converted
	 * @param attrNames list of Strings with attribute names
	 * @return RichUser with attributes
	 * @throws InternalErrorException internal error
	 */
	RichUser convertUserToRichUserWithAttributesByNames(PerunSession sess, User user, List<String> attrNames);

	/**
	 * For richUser filter all his user attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richUser
	 * @return richUser with only allowed attributes
	 * @throws InternalErrorException
	 */
	RichUser filterOnlyAllowedAttributes(PerunSession sess, RichUser richUser);

	/**
	 * For list of richUser filter all their user attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richUsers
	 * @return list of RichUsers with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichUser> filterOnlyAllowedAttributes(PerunSession sess, List<RichUser> richUsers);

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
	 */
	List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString, List<String> attrsName) throws UserNotExistsException;

	/**
	 * Return list of RichUsers which are not members of any VO and contain selected attributes.
	 *
	 * @param sess
	 * @param attrsName
	 * @return list of RichUsers
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName) throws UserNotExistsException;

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
	List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrNames) throws UserNotExistsException;

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
	List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrNames) throws UserNotExistsException;

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
	List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers, List<String> attrsNames) throws UserNotExistsException;

	/**
	 * Allow users to manually add login in supported namespace if same login is not reserved
	 *
	 * @param sess
	 * @param user
	 * @param loginNamespace
	 * @param login
	 * @throws InternalErrorException
	 */
	void setLogin(PerunSession sess, User user, String loginNamespace, String login);

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
	 * @param lang language to get confirmation mail in (optional)
	 * @param path path that is appended to the url of the verification link (optional)
	 * @throws InternalErrorException
	 */
	void requestPreferredEmailChange(PerunSession sess, String url, User user, String email, String lang, String path);

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
	 * @throws WrongAttributeValueException          If new email address is in wrong format
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException           If user:preferredEmail attribute doesn't exists.
	 * @throws WrongReferenceAttributeValueException
	 */
	String validatePreferredEmailChange(PerunSession sess, User user, String i, String m) throws WrongAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, WrongReferenceAttributeValueException;

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
	List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) throws WrongAttributeAssignmentException, AttributeNotExistsException;

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
	 * Checks if the password reset request link is valid. The request is valid, if it
	 * was created, never used and hasn't expired yet.
	 *
	 * @param sess PerunSession
	 * @param token token for the request to check
	 * @throws PasswordResetLinkExpiredException when the reset link expired
	 * @throws PasswordResetLinkNotValidException when the reset link was already used or has never existed
	 */
	void checkPasswordResetRequestIsValid(PerunSession sess, String token) throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

	/**
	 * Changes user password in defined login-namespace based on token of the password reset request.
	 *
	 * @param sess PerunSession
	 * @param token token for the password reset request
	 * @param password new password
	 * @param lang Language to get notification in
	 * @throws InternalErrorException
	 * @throws UserNotExistsException When the user who requested the password reset doesn't exist
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws InvalidLoginException When login of user has invalid syntax (is not allowed)
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 * @throws PasswordResetLinkExpiredException When the password reset request expired
	 * @throws PasswordResetLinkNotValidException When the password reset request was already used or has never existed
	 * @throws PasswordChangeFailedException When password change failed
	 * @throws PasswordOperationTimeoutException When password change timed out
	 */
	void changeNonAuthzPassword(PerunSession sess, String token, String password, String lang) throws UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException, PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

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
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 */
	Map<String,String> generateAccount(PerunSession session, String namespace, Map<String, String> parameters) throws PasswordStrengthException;

	/**
	 * Gets list of users that sponsor the member.
	 * @param sess perun session
	 * @param sponsoredMember member which is sponsored
	 * @return list of users that sponsor the member.
	 * @throws InternalErrorException
	 */
	List<User> getSponsors(PerunSession sess, Member sponsoredMember);

	/**
	 * Returns password manager module for specified login-namespace or falls back on generic password manager module.
	 * Throws exception if no module implementation is found or it can't be instantiated.
	 *
	 * @param session session with authz
	 * @param namespace specific namespace
	 * @return Password manager module for namespace or 'generic' module.
	 * @throws InternalErrorException When module instantiation fails or no module implementation is found by class loader.
	 */
	PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace);

	/**
	 * Removes all user's external sources.
	 * It also means removing all it's attributes.
	 *
	 * @param sess session
	 * @param user owner of external sources
	 * @throws InternalErrorException
	 */
	void removeAllUserExtSources(PerunSession sess, User user);


	/**
	 * Finds users with UserExtSource with attribute value that ends with specified string but not with specified exclude strings.
	 * This method is written to find all users with schacHomeOrganization domains ending with valueEnd, but not with exludeValueEnds.
	 *
	 * @param sess session
	 * @param attributeName UserExtSource attribute name
	 * @param valueEnd required attribute value ending
	 * @param excludeValueEnds exclude these attribute value endings
	 * @return list of users
	 */
	List<User> findUsersWithExtSourceAttributeValueEnding(PerunSessionImpl sess, String attributeName, String valueEnd, List<String> excludeValueEnds) throws AttributeNotExistsException;

	/**
	 * Generates new random password for given user and returns String representing HTML
	 * where is the new password.
	 * <p>
	 * The HTML template is taken from entityless attribute randomPwdResetTemplate and the
	 * loginNamespace is used as a key.
	 *
	 * @param session        session
	 * @param user           user
	 * @param loginNamespace login namespace
	 * @return String representing HTML with data about new generated password
	 * @throws PasswordOperationTimeoutException password change timed out
	 * @throws InternalErrorException            internal error
	 * @throws PasswordChangeFailedException     password change failed
	 * @throws LoginNotExistsException When user doesn't have login in specified namespace
	 * @throws InvalidLoginException When When login of user has invalid syntax (is not allowed)
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 */
	String changePasswordRandom(PerunSession session, User user, String loginNamespace) throws PasswordOperationTimeoutException, LoginNotExistsException, PasswordChangeFailedException, InvalidLoginException, PasswordStrengthException;

	/**
	 * Check password strength for the given namespace. If the password is too weak,
	 * the PasswordStrengthException is thrown
	 *
	 * @param password password, that will be checked
	 * @param namespace namespace, that will be used to check the strength of the password
	 *
	 * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
	 */
	void checkPasswordStrength(PerunSession sess, String password, String namespace) throws PasswordStrengthException;
	/**
	 * Return all groups where user is active (has VALID status in VO and Group together)
	 * for specified user and resource
	 *
	 * @param sess PerunSession
	 * @param resource Only groups assigned to this resource might be returned
	 * @param user Only groups where this user is VALID member might be returned
	 * @return List of groups where user is active (is a VALID vo and group member) on specified resource
	 */
	List<Group> getGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user);

	/**
	 * Return all groups where user is active (has VALID status in VO and Group together)
	 * for specified user and facility
	 *
	 * @param sess PerunSession
	 * @param facility Only groups assigned to this facility (all its resources) might be returned
	 * @param user Only groups where this user is VALID member might be returned
	 * @return List of groups where user is active (is a VALID vo and group member) on specified facility
	 */
	List<Group> getGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user);

	/**
	 * From given candidate, creates a user.
	 * This method also checks if some of given userExtSources do exist. If so, this method
	 * throws a UserExtSourceExistsException.
	 * This method can also set only user-def and user-opt attributes for the given candidate.
	 *
	 * @param sess session
	 * @param candidate candidate
	 * @return created user
	 * @throws AttributeNotExistsException if some of the given attributes dont exist
	 * @throws WrongAttributeAssignmentException if some of the given attributes have unsupported namespace
	 * @throws UserExtSourceExistsException if some of the given UES already exist
	 * @throws WrongReferenceAttributeValueException if some of the given attribute value cannot be set because of
	 *                                               some other attribute constraint
	 * @throws WrongAttributeValueException if some of the given attribute value is invalid
	 */
	User createUser(PerunSession sess, Candidate candidate) throws UserExtSourceExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * From given candidate, creates a service user and assign given owners to him.
	 * This method also checks if some of given userExtSources do exist. If so,
	 * this method throws a UserExtSourceExistsException.
	 * This method can also set only user-def and user-opt attributes for the given candidate.
	 *
	 * @param sess session
	 * @param candidate candidate
	 * @param owners owners to be set for the new user
	 * @return created service user
	 * @throws AttributeNotExistsException if some of the given attributes dont exist
	 * @throws WrongAttributeAssignmentException if some of the given attributes have unsupported namespace
	 * @throws UserExtSourceExistsException if some of the given UES already exist
	 * @throws WrongReferenceAttributeValueException if some of the given attribute value cannot be set because of
	 *                                               some other attribute constraint
	 * @throws WrongAttributeValueException if some of the given attribute value is invalid
	 */
	User createServiceUser(PerunSession sess, Candidate candidate, List<User> owners) throws WrongAttributeAssignmentException, UserExtSourceExistsException, WrongReferenceAttributeValueException, WrongAttributeValueException, AttributeNotExistsException;
}
