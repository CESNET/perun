package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.DeletionNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginExistsException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsAlreadyBlockedException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsNotBlockedException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotSpecificUserExpectedException;
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
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SSHKeyNotValidException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceOnlyRoleAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserExpectedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UsersManager manages users.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Zora Sebestianova
 * @author Sona Mastrakova
 */
public interface UsersManager {

  // Contains query needed to get users from external source
  String USERS_QUERY = "usersQuery";

  /**
   * Add specificUser owner (the user) If not exists, create new ownership. If exists, only enable ownership for user
   * and specificUser
   *
   * @param sess
   * @param user         the user
   * @param specificUser the specificUser
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws SpecificUserExpectedException    when the specificUser is not really specific user (is it normal user)
   * @throws NotSpecificUserExpectedException when the user is specific User
   * @throws RelationExistsException          If there is such user (the user) who try to add
   */
  void addSpecificUserOwner(PerunSession sess, User user, User specificUser)
      throws UserNotExistsException, PrivilegeException, NotSpecificUserExpectedException,
      SpecificUserExpectedException, RelationExistsException;

  /**
   * Adds user's external sources.
   *
   * @param perunSession
   * @param user
   * @param userExtSource
   * @return user external auth object with newly generated ID
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @throws UserExtSourceExistsException
   */
  UserExtSource addUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource)
      throws UserNotExistsException, PrivilegeException, UserExtSourceExistsException;

  /**
   * Adds userExternalSource to the user along with the ues attributes.
   *
   * @param sess
   * @param user          user to assign the userExtSource to
   * @param userExtSource the UserExtSource to be created
   * @param uesAttributes the Attributes to be set to the userExtSource
   * @return the newly created ues along with its attributes as RichUserExtSource
   * @throws UserNotExistsException
   * @throws UserExtSourceExistsException
   * @throws PrivilegeException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws UserExtSourceNotExistsException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   */
  RichUserExtSource addUserExtSourceWithAttributes(PerunSession sess, User user, UserExtSource userExtSource,
                                                   List<Attribute> uesAttributes)
      throws UserNotExistsException, UserExtSourceExistsException, PrivilegeException,
      WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserExtSourceNotExistsException,
      AttributeNotExistsException, WrongAttributeValueException;

  /**
   * Anonymizes user - according to configuration, each of user's attributes is either anonymized, kept untouched or
   * deleted. Also deletes other user's related data, e.g. authorships of users publications, mail change and password
   * reset requests, bans... If force is true then also removes associated members.
   *
   * @param perunSession
   * @param user
   * @param force
   * @throws InternalErrorException             if an internal error has occurred
   * @throws UserNotExistsException             if the user doesn't exist
   * @throws PrivilegeException                 if the method isn't called by perun admin
   * @throws RelationExistsException            if the user has some members assigned
   * @throws AnonymizationNotSupportedException if an attribute should be anonymized but its module doesn't specify the
   *                                            anonymization process or if the anonymization is not supported at this
   *                                            instance
   */
  void anonymizeUser(PerunSession perunSession, User user, boolean force)
      throws UserNotExistsException, PrivilegeException, RelationExistsException, AnonymizationNotSupportedException;

  /**
   * Block logins for given namespace or block logins globally (if no namespace is selected)
   *
   * @param sess
   * @param logins    list of logins to be blocked
   * @param namespace namespace where the logins should be blocked (null means block the logins globally)
   * @throws LoginIsAlreadyBlockedException
   * @throws LoginExistsException
   */
  void blockLogins(PerunSession sess, List<String> logins, String namespace)
      throws PrivilegeException, LoginIsAlreadyBlockedException, LoginExistsException;

  /**
   * Changes user password in defined login-namespace based on token parameter.
   *
   * @param sess
   * @param token    token for the password reset request
   * @param password new password
   * @param lang     language to get notification in
   * @throws InternalErrorException
   * @throws UserNotExistsException             When the user who requested the password reset doesn't exist
   * @throws LoginNotExistsException            When user doesn't have login in specified namespace
   * @throws InvalidLoginException              When login of user has invalid syntax (is not allowed)
   * @throws PasswordStrengthException          When password doesn't match expected strength by namespace
   *                                            configuration
   * @throws PasswordResetLinkExpiredException  When the password reset request expired
   * @throws PasswordResetLinkNotValidException When the password reset request was already used or has never existed
   * @throws PasswordChangeFailedException      When password change failed
   * @throws PasswordOperationTimeoutException  When password change timed out
   */
  void changeNonAuthzPassword(PerunSession sess, UUID token, String password, String lang)
      throws UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException,
      PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException,
      PasswordStrengthException, PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

  /**
   * Changes user password in defined login-namespace. If checkOldPassword is true, then ask authentication system if
   * old password is correct.
   *
   * @param sess             Perun session
   * @param login            String representation of the userLogin
   * @param loginNamespace   Login-namespace to change password in
   * @param oldPassword      Old password
   * @param newPassword      New password
   * @param checkOldPassword If true, validates old password
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws LoginNotExistsException       When user doesn't have login in specified namespace
   * @throws PasswordDoesntMatchException  When old password does not match
   * @throws PasswordChangeFailedException
   */
  void changePassword(PerunSession sess, String login, String loginNamespace, String oldPassword, String newPassword,
                      boolean checkOldPassword)
      throws PrivilegeException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException,
      PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException,
      PasswordStrengthException;

  /**
   * Changes user password in defined login-namespace. If checkOldPassword is true, then ask authentication system if
   * old password is correct.
   *
   * @param sess             Perun session
   * @param user             User requesting password change
   * @param loginNamespace   Login-namespace to change password in
   * @param oldPassword      Old password
   * @param newPassword      New password
   * @param checkOldPassword If true, validates old password
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException        When the user doesn't exist
   * @throws LoginNotExistsException       When user doesn't have login in specified namespace
   * @throws PasswordDoesntMatchException  When old password does not match
   * @throws PasswordChangeFailedException
   */
  void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword, String newPassword,
                      boolean checkOldPassword)
      throws PrivilegeException, UserNotExistsException, LoginNotExistsException, PasswordDoesntMatchException,
      PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException,
      InvalidLoginException, PasswordStrengthException;

  /**
   * Generates new random password for given user and returns String representing HTML where is the new password.
   * <p>
   * The HTML template is taken from entityless attribute randomPwdResetTemplate and the loginNamespace is used as a
   * key.
   *
   * @param sess           session
   * @param user           user
   * @param loginNamespace login namespace
   * @return String representing HTML with data about new generated password
   */
  String changePasswordRandom(PerunSession sess, User user, String loginNamespace)
      throws PrivilegeException, PasswordOperationTimeoutException, LoginNotExistsException,
      PasswordChangeFailedException, InvalidLoginException, PasswordStrengthException;

  /**
   * Checks if the password reset request is valid. The request is valid, if it was created, never used and hasn't
   * expired yet.
   *
   * @param sess
   * @param token token for the request to check
   * @throws PasswordResetLinkExpiredException  when the reset link expired
   * @throws PasswordResetLinkNotValidException when the reset link was already used or has never existed
   */
  void checkPasswordResetRequestIsValid(PerunSession sess, UUID token)
      throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException;

  /**
   * Check password strength for the given namespace. If the password is too weak, the PasswordStrengthException is
   * thrown
   *
   * @param password  password, that will be checked
   * @param namespace namespace, that will be used to check the strength of the password
   * @param login     login, which may be required for correct password strength check
   * @throws PasswordStrengthException When password doesn't match expected strength by namespace configuration
   */
  void checkPasswordStrength(PerunSession sess, String password, String namespace, String login)
      throws PasswordStrengthException;

  /**
   * Creates alternative password in external system.
   *
   * @param sess
   * @param user
   * @param description    - description of a password (e.g. 'mobile phone', 'tablet', ...)
   * @param loginNamespace
   * @param password       string representation of password
   * @throws InternalErrorException
   * @throws PasswordCreationFailedException
   * @throws UserNotExistsException
   * @throws LoginNotExistsException
   * @throws PrivilegeException
   */
  void createAlternativePassword(PerunSession sess, User user, String description, String loginNamespace,
                                 String password)
      throws PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException,
      PasswordStrengthException;

  /**
   * From given candidate, creates a service user and assign given owners to him. This method also checks if some of
   * given userExtSources do exist. If so, this method throws a UserExtSourceExistsException. This method can also set
   * only user-def and user-opt attributes for the given candidate.
   *
   * @param sess               session
   * @param candidate          candidate
   * @param specificUserOwners owners to be set for the new user
   * @return created service user
   * @throws UserNotExistsException                if some of the given owners does not exist
   * @throws AttributeNotExistsException           if some of the given attributes dont exist
   * @throws WrongAttributeAssignmentException     if some of the given attributes have unsupported namespace
   * @throws UserExtSourceExistsException          if some of the given UES already exist
   * @throws WrongReferenceAttributeValueException if some of the given attribute value cannot be set because of some
   *                                               other attribute constraint
   * @throws WrongAttributeValueException          if some of the given attribute value is invalid
   * @throws PrivilegeException                    insufficient permissions
   */
  User createServiceUser(PerunSession sess, Candidate candidate, List<User> specificUserOwners)
      throws PrivilegeException, WrongAttributeAssignmentException, UserExtSourceExistsException,
      WrongReferenceAttributeValueException, WrongAttributeValueException, AttributeNotExistsException,
      UserNotExistsException;

  /**
   * Inserts user into DB.
   *
   * @param perunSession
   * @param user
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  @Deprecated
  User createUser(PerunSession perunSession, User user) throws PrivilegeException;

  /**
   * Deletes alternative password in external system.
   *
   * @param sess
   * @param user
   * @param loginNamespace
   * @param passwordId     passwords ID
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PasswordDeletionFailedException
   * @throws LoginNotExistsException
   * @throws PrivilegeException
   */
  void deleteAlternativePassword(PerunSession sess, User user, String loginNamespace, String passwordId)
      throws UserNotExistsException, PasswordDeletionFailedException, PrivilegeException, LoginNotExistsException;

  /**
   * Deletes password in external system. User must not exist.
   *
   * @param sess           Perun session
   * @param userLogin      String representation of the userLogin
   * @param loginNamespace Login-namespace to remove password in
   * @throws PrivilegeException
   * @throws PasswordDeletionFailedException
   * @throws LoginNotExistsException           When user doesn't have login in specified namespace
   * @throws InvalidLoginException             When login of user has invalid syntax (is not allowed)
   * @throws PasswordOperationTimeoutException When password change timed out
   */
  void deletePassword(PerunSession sess, String userLogin, String loginNamespace)
      throws PasswordDeletionFailedException, PrivilegeException, LoginNotExistsException,
      PasswordOperationTimeoutException, InvalidLoginException;

  /**
   * Deletes password in external system. User must exist. User's login for specified namespace must exist in Perun.
   *
   * @param sess           Perun session
   * @param user           User whose password is being removed
   * @param loginNamespace Login-namespace to remove password in
   * @throws PrivilegeException
   * @throws UserNotExistsException            When the user doesn't exist
   * @throws LoginNotExistsException           When user doesn't have login in specified namespace
   * @throws InvalidLoginException             When login of user has invalid syntax (is not allowed)
   * @throws PasswordOperationTimeoutException When password change timed out
   * @throws PasswordDeletionFailedException
   */
  void deletePassword(PerunSession sess, User user, String loginNamespace)
      throws PrivilegeException, UserNotExistsException, LoginNotExistsException, InvalidLoginException,
      PasswordOperationTimeoutException, PasswordDeletionFailedException;

  /**
   * Deletes user.
   *
   * @param perunSession
   * @param user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws RelationExistsException             if user has some members assigned
   * @throws MemberAlreadyRemovedException       if there is at least 1 member deleted but not affected by deleting from
   *                                             DB
   * @throws UserAlreadyRemovedException         if there are no rows affected by deleting user in DB
   * @throws SpecificUserAlreadyRemovedException if there are no rows affected by deleting specific user in DB
   * @throws DeletionNotSupportedException       if the deletion of users is not supported
   */
  void deleteUser(PerunSession perunSession, User user)
      throws UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException,
      UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, DeletionNotSupportedException;

  /**
   * Deletes user. If forceDelete is true, then removes also associeted members.
   *
   * @param perunSession
   * @param user
   * @param forceDelete  if true, deletes also all members if they are assigned to the user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws RelationExistsException             if forceDelete is false and the user has some members assigned
   * @throws MemberAlreadyRemovedException       if there is at least 1 member deleted but not affected by deleting from
   *                                             DB
   * @throws UserAlreadyRemovedException         if there are no rows affected by deleting user in DB
   * @throws SpecificUserAlreadyRemovedException if there are no rows affected by deleting specific user in DBn
   * @throws DeletionNotSupportedException       if the deletion of users is not supported
   */
  void deleteUser(PerunSession perunSession, User user, boolean forceDelete)
      throws UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException,
      UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, DeletionNotSupportedException;

  /**
   * Returns list of RichUsers with attributes who matches the searchString, searching name, id, uuid, email, logins.
   *
   * @param sess
   * @param searchString
   * @return list of RichUsers
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<RichUser> findRichUsers(PerunSession sess, String searchString)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Returns list of RichUsers with attributes who matches the searchString, searching name, id, uuid, email, logins.
   *
   * @param sess
   * @param searchString
   * @param attrNames
   * @return list of RichUsers with selected attributes
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrNames)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Returns list of RichUsers with attributes who matches the searchString, searching name, id, uuid, email, logins.
   * Name part is searched for exact match.
   *
   * @param sess
   * @param searchString
   * @param attrNames
   * @return list of RichUsers with selected attributes
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString, List<String> attrNames)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Return list of RichUsers who matches the searchString, searching name, email and logins and are not member in
   * specific VO and contain selected attributes.
   *
   * @param sess
   * @param vo
   * @param searchString
   * @param attrsName
   * @return list of RichUsers
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString,
                                                              List<String> attrsName)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Returns list of users who matches the searchString, searching name, id, uuid, email, logins.
   *
   * @param sess
   * @param searchString
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> findUsers(PerunSession sess, String searchString) throws PrivilegeException;

  /**
   * Returns list of users who exactly matches the searchString
   *
   * @param sess
   * @param searchString
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> findUsersByExactName(PerunSession sess, String searchString) throws PrivilegeException;

  /**
   * Returns list of users who matches the searchString
   *
   * @param sess
   * @param searchString
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> findUsersByName(PerunSession sess, String searchString) throws PrivilegeException;

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
   * @throws PrivilegeException
   */
  List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName,
                             String lastName, String titleAfter) throws PrivilegeException;

  /**
   * Generate user account in a backend system associated with login-namespace in Perun.
   * <p>
   * This method consumes optional parameters map. Requirements are implementation-dependant for each login-namespace.
   * <p>
   * Returns map with 1: key=login-namespace attribute urn, value=generated login 2: rest of opt response attributes...
   *
   * @param session
   * @param namespace  Namespace to generate account in
   * @param parameters Optional parameters
   * @return Map of data from backed response
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  Map<String, String> generateAccount(PerunSession session, String namespace, Map<String, String> parameters)
      throws PrivilegeException, PasswordStrengthException;

  /**
   * Returns all blocked logins in namespaces (if namespace is null, then this login is blocked globally)
   *
   * @param sess
   * @return list of all blocked logins in namespaces
   */
  List<BlockedLogin> getAllBlockedLoginsInNamespaces(PerunSession sess) throws PrivilegeException;

  /**
   * Get All richUsers with or without specificUsers. If includedSpecificUsers is true, you got all Users included
   * specificUsers If includedSpecificUsers is false, you get all Users without specificUsers
   * <p>
   * !!! This method get all RichUsers without Attributes !!!
   *
   * @param sess
   * @param includedSpecificUsers true or false if you want or dont want get specificUsers too
   * @return list of RichUsers
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) throws PrivilegeException;

  /**
   * Get All richUsers with or without specificUsers. If includedSpecificUsers is true, you got all Users included
   * specificUsers If includedSpecificUsers is false, you get all Users without specificUsers
   * <p>
   * This method get all RichUsers included Attributes.
   *
   * @param sess
   * @param includedSpecificUsers true or false if you want or dont want get specificUsers too
   * @return list of RichUsers
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers)
      throws PrivilegeException, UserNotExistsException;

  /**
   * Get All richUsers with or without specificUsers. If includedSpecificUsers is true, you got all Users included
   * specificUsers If includedSpecificUsers is false, you get all Users without specificUsers
   * <p>
   * This method get all RichUsers included selected Attributes.
   *
   * @param sess
   * @param attrsNames
   * @param includedSpecificUsers true or false if you want or dont want get specificUsers too
   * @return list of RichUsers
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers,
                                               List<String> attrsNames)
      throws PrivilegeException, UserNotExistsException;

  /**
   * Get all resources from the facility which have the user access on.
   *
   * @param sess
   * @param facility
   * @param user
   * @return list of resources which have the user acess on
   * @throws InternalErrorException
   * @throws FacilityNotExistsException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user)
      throws FacilityNotExistsException, UserNotExistsException, PrivilegeException;

  /**
   * Get all resources which have the user access on.
   *
   * @param sess
   * @param user
   * @return list of resources which have the user acess on
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<Resource> getAllowedResources(PerunSession sess, User user) throws UserNotExistsException, PrivilegeException;

  /**
   * Get all rich resources which have the user assigned.
   *
   * @param sess
   * @param user
   * @return list of rich resources which have the user assigned
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<RichResource> getAssignedRichResources(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Return all resources of specified facility with which user is associated through all his members. Does not require
   * ACTIVE group-resource assignment.
   *
   * @param sess
   * @param facility
   * @param user
   * @return All resources with which user is associated
   * @throws UserNotExistsException
   * @throws FacilityNotExistsException
   * @throws PrivilegeException
   */
  List<Resource> getAssociatedResources(PerunSession sess, Facility facility, User user)
      throws UserNotExistsException, FacilityNotExistsException, PrivilegeException;

  /**
   * Get page of blocked logins.
   *
   * @param sess  session
   * @param query query with page information
   * @return page of requested blocked logins
   */
  Paginated<BlockedLogin> getBlockedLoginsPage(PerunSession sess, BlockedLoginsPageQuery query)
      throws PrivilegeException;

  /**
   * Return all groups where user is active (has VALID status in VO and Group together) for specified user and resource
   *
   * @param sess     PerunSession
   * @param resource Only groups assigned to this resource might be returned
   * @param user     Only groups where this user is VALID member might be returned
   * @return List of groups where user is active (is a VALID vo and group member) on specified resource
   */
  List<Group> getGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user) throws PrivilegeException;

  /**
   * Return all groups where user is active (has VALID status in VO and Group together) for specified user and facility
   *
   * @param sess     PerunSession
   * @param facility Only groups assigned to this facility (all its resources) might be returned
   * @param user     Only groups where this user is VALID member might be returned
   * @return List of groups where user is active (is a VALID vo and group member) on specified facility
   */
  List<Group> getGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user) throws PrivilegeException;

  /**
   * Returns list of Groups in Perun, where the User is a direct Administrator or he is VALID a member of any group
   * which is Administrator of some of these Groups.
   *
   * @param perunSession
   * @param user
   * @return list of Groups, where user or some of his groups is an Administrator
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<Group> getGroupsWhereUserIsAdmin(PerunSession perunSession, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Returns list of Groups in selected Vo, where the User is a direct Administrator or he is a VALID member of any
   * group which is Administrator of some of these Groups.
   *
   * @param sess
   * @param vo   selected Vo under which we are looking for groups
   * @param user manager of groups we are looking for
   * @return list of Groups, where user or some of his groups (in the Vo) is an Administrator
   * @throws InternalErrorException
   * @throws PrivilegeException     user has no privileges to call this method
   * @throws UserNotExistsException the user not exists in Perun
   * @throws VoNotExistsException   the vo not exists in Perun
   */
  List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user)
      throws PrivilegeException, UserNotExistsException, VoNotExistsException;

  /**
   * Return list of email addresses of user, which are awaiting validation and are inside time window for validation.
   * <p>
   * If there is no preferred email change request pending or requests are outside time window for validation, returns
   * empty list.
   *
   * @param sess PerunSession
   * @param user User to check pending request for
   * @return List<String> user's email addresses pending validation
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   */
  List<String> getPendingPreferredEmailChanges(PerunSession sess, User user)
      throws PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException;

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
   * Return all RichGroups where user is active (has VALID status in VO and Group together) for specified user and
   * resource with specified group attributes by their names (URNs).
   *
   * @param sess      PerunSession
   * @param resource  Only groups assigned to this resource might be returned
   * @param user      Only groups where this user is VALID member might be returned
   * @param attrNames Names (URNs) of group attributes to get with each returned group
   * @return List of groups where user is active (is a VALID vo and group member) on specified resource
   */
  List<RichGroup> getRichGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user,
                                                 List<String> attrNames) throws PrivilegeException;

  /**
   * Return all groups where user is active (has VALID status in VO and Group together) for specified user and resource
   *
   * @param sess      PerunSession
   * @param facility  Only groups assigned to this facility (all its resources) might be returned
   * @param user      Only groups where this user is VALID member might be returned
   * @param attrNames Names (URNs) of group attributes to get with each returned group
   * @return List of groups where user is active (is a VALID vo and group member) on specified facility
   */
  List<RichGroup> getRichGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user,
                                                 List<String> attrNames) throws PrivilegeException;

  /**
   * Get User to RichUser without attributes.
   *
   * @param sess
   * @param user
   * @return RichUser
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  RichUser getRichUser(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException;

  /**
   * Gets list of all user's external sources with attributes.
   *
   * @param perunSession session
   * @param user         user for which should be returned rich ext sources
   * @return list of user's external sources with attributes
   * @throws InternalErrorException internal error
   * @throws UserNotExistsException if given user doesn't exist
   * @throws PrivilegeException     insufficient permissions
   */
  List<RichUserExtSource> getRichUserExtSources(PerunSession perunSession, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Gets list of all user's external sources with attributes. If any of the attribute names is incorrect then the value
   * is silently skipped. If the attrsNames is null, then this method returns all ues attributes.
   *
   * @param perunSession session
   * @param user         user for which should be returned rich ext sources
   * @param attrsNames   list of attribute names that should be found
   * @return list of user's external sources with attributes
   * @throws InternalErrorException internal error
   * @throws UserNotExistsException if given user doesn't exist
   * @throws PrivilegeException     insufficient permissions
   */
  List<RichUserExtSource> getRichUserExtSources(PerunSession perunSession, User user, List<String> attrsNames)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Get User to RichUser with attributes.
   *
   * @param sess
   * @param user
   * @return RichUser
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  RichUser getRichUserWithAttributes(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException;

  /**
   * Returns rich users without attributes by their ids.
   *
   * @param sess
   * @param ids
   * @return list of rich users with specified ids
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<RichUser> getRichUsersByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException;

  /**
   * Returns rich users with attributes by their ids.
   *
   * @param sess
   * @param ids
   * @return list of rich users with specified ids
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<RichUser> getRichUsersWithAttributesByIds(PerunSession sess, List<Integer> ids)
      throws PrivilegeException, UserNotExistsException;

  /**
   * Returns all RichUsers with attributes who are not member of any VO.
   *
   * @param sess
   * @return list of richUsers who are not member of any VO
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess) throws UserNotExistsException, PrivilegeException;

  /**
   * Returns list of RichUsers which are not members of any VO and with selected attributes
   *
   * @param sess
   * @param attrNames
   * @return list of RichUsers with selected attributes
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrNames)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Return all specific Users (only specific users)
   *
   * @param sess
   * @return list of all specific users in perun
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<User> getSpecificUsers(PerunSession sess) throws PrivilegeException;

  /**
   * Return all specificUsers who are owned by the user
   *
   * @param sess
   * @param user the user
   * @return list of specific users who are owned by the user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws NotSpecificUserExpectedException when the user is service User
   */
  List<User> getSpecificUsersByUser(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException, NotSpecificUserExpectedException;

  /**
   * Gets list of users that sponsor the member, with attributes.
   *
   * @param sess      Perun session
   * @param member    member which is sponsored
   * @param attrNames list of attributes. if null or empty, returns all attributes
   * @return list of users which sponsor the member
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @deprecated - use getSponsorsForMember
   */
  @Deprecated
  List<RichUser> getSponsors(PerunSession sess, Member member, List<String> attrNames)
      throws PrivilegeException, UserNotExistsException;

  /**
   * Gets list of users that sponsor the member, with attributes.
   *
   * @param sess      Perun session
   * @param member    member which is sponsored
   * @param attrNames list of attributes. if null or empty, returns all attributes
   * @return list of users which sponsor the member
   * @throws PrivilegeException insufficient permissions
   */
  List<Sponsor> getSponsorsForMember(PerunSession sess, Member member, List<String> attrNames)
      throws PrivilegeException;

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
   * @throws PrivilegeException
   */
  User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin)
      throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, PrivilegeException;

  /**
   * Returns user by his/her id.
   *
   * @param perunSession
   * @param id
   * @return user
   * @throws UserNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  User getUserById(PerunSession perunSession, int id) throws UserNotExistsException, PrivilegeException;

  /**
   * Returns user by VO member.
   *
   * @param perunSession
   * @param member
   * @return user
   * @throws UserNotExistsException
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws PrivilegeException
   */
  User getUserByMember(PerunSession perunSession, Member member) throws MemberNotExistsException, PrivilegeException;

  /**
   * Returns user by his login in external source and external source.
   *
   * @param perunSession
   * @param userExtSource
   * @return selected user or throws UserNotExistsException in case the user doesn't exists
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws UserExtSourceNotExistsException
   * @throws PrivilegeException
   */
  User getUserByUserExtSource(PerunSession perunSession, UserExtSource userExtSource)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException;

  /**
   * Returns user based on one of the userExtSource.
   *
   * @param perunSession
   * @param userExtSources
   * @return user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  User getUserByUserExtSources(PerunSession perunSession, List<UserExtSource> userExtSources)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Gets user's external source by the user's external login and external source.
   *
   * @param perunSession
   * @param source
   * @param extLogin
   * @return user external source object
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ExtSourceNotExistsException
   * @throws UserExtSourceNotExistsException
   */
  UserExtSource getUserExtSourceByExtLogin(PerunSession perunSession, ExtSource source, String extLogin)
      throws PrivilegeException, ExtSourceNotExistsException, UserExtSourceNotExistsException;

  /**
   * Get the user ext source by its id.
   *
   * @param sess
   * @param id
   * @return user external source for the id
   * @throws InternalErrorException
   * @throws UserExtSourceNotExistsException
   * @throws PrivilegeException
   */
  UserExtSource getUserExtSourceById(PerunSession sess, int id)
      throws UserExtSourceNotExistsException, PrivilegeException;

  /**
   * Return userExtSource for specific attribute definition (specified by id) and unique value. If not found, throw and
   * exception.
   * <p>
   * It looks for exactly one value of the specific attribute type: - Integer -> exactly match - String -> exactly match
   * - Map -> exactly match of "key=value" - ArrayList -> exactly match of one of the value
   *
   * @param sess        Perun session
   * @param attrId      attribute id used for founding attribute definition which has to exists, be unique and in
   *                    userExtSource namespace
   * @param uniqueValue value used for searching
   * @return userExtSource found by attribute id and it's unique value
   * @throws UserExtSourceNotExistsException if userExtSource can't be found
   * @throws AttributeNotExistsException     if attribute can't be found by it's id
   * @throws PrivilegeException              if the principal does not have sufficient rights to call this method
   */
  UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue)
      throws AttributeNotExistsException, UserExtSourceNotExistsException, PrivilegeException;

  /**
   * Return userExtSource for specific attribute definition (specified by name) and unique value. If not found, throw
   * and exception.
   * <p>
   * It looks for exactly one value of the specific attribute type: - Integer -> exactly match - String -> exactly match
   * - Map -> exactly match of "key=value" - ArrayList -> exactly match of one of the value
   *
   * @param sess        Perun session
   * @param attrName    attribute name used for founding attribute definition which has to exists, be unique and in
   *                    userExtSource namespace
   * @param uniqueValue value used for searching
   * @return userExtSource found by attribute name and it's unique value
   * @throws UserExtSourceNotExistsException if userExtSource can't be found
   * @throws AttributeNotExistsException     if attribute can't be found by it's name
   * @throws PrivilegeException              if the principal does not have sufficient rights to call this method
   */
  UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, String attrName, String uniqueValue)
      throws AttributeNotExistsException, UserExtSourceNotExistsException, PrivilegeException;

  /**
   * Gets list of all user's external sources of the user.
   *
   * @param perunSession
   * @param user
   * @return list of user's external sources
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<UserExtSource> getUserExtSources(PerunSession perunSession, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Get the user ext sources by their ids.
   *
   * @param sess
   * @param ids
   * @return list of user external sources with specified ids
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<UserExtSource> getUserExtSourcesByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException;

  /**
   * Returns all users (included specific users).
   *
   * @param sess
   * @return list of all users
   * @throws InternalErrorException
   */
  List<User> getUsers(PerunSession sess) throws PrivilegeException;

  /**
   * Returns all users who have set the attribute with the value. Searching only def and opt attributes.
   *
   * @param sess
   * @param attribute
   * @return list of users
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws PrivilegeException;

  /**
   * Returns all users who have set the attribute with the value. Searching by attributeName. Searching only def and opt
   * attributes. Can find only attributes with String Value by this way! (not Integer, Map or List)
   *
   * @param sess
   * @param attributeName
   * @param attributeValue
   * @return list of users
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws AttributeNotExistsException
   */
  List<User> getUsersByAttribute(PerunSession sess, String attributeName, String attributeValue)
      throws PrivilegeException, AttributeNotExistsException;

  /**
   * Returns all users who have the attribute with the value. attributeValue is not converted to the attribute type, it
   * is always type of String.
   *
   * @param sess
   * @param attributeName
   * @param attributeValue
   * @return list of users
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws AttributeNotExistsException
   */
  List<User> getUsersByAttributeValue(PerunSession sess, String attributeName, String attributeValue)
      throws PrivilegeException, AttributeNotExistsException;

  /**
   * Returns existing users by their ids.
   *
   * @param perunSession
   * @param ids
   * @return list of users with specified ids
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<User> getUsersByIds(PerunSession perunSession, List<Integer> ids) throws PrivilegeException;

  /**
   * Return all users who owns the specific one
   *
   * @param sess
   * @param specificUser the specific user
   * @return list of user who owns the specificUser
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws SpecificUserExpectedException when the serviceUser is not really service user (is it normal user)
   */
  List<User> getUsersBySpecificUser(PerunSession sess, User specificUser)
      throws UserNotExistsException, PrivilegeException, SpecificUserExpectedException;

  /**
   * Return all users who owns the specific one and are not anonymized
   *
   * @param sess
   * @param specificUser the specific user
   * @return list of user who owns the specificUser
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws SpecificUserExpectedException when the serviceUser is not really service user (is it normal user)
   */
  List<User> getUnanonymizedUsersBySpecificUser(PerunSession sess, User specificUser)
      throws UserNotExistsException, PrivilegeException, SpecificUserExpectedException;

  /**
   * Get count of all users.
   *
   * @param sess PerunSession
   * @return count of all users
   * @throws InternalErrorException
   */
  int getUsersCount(PerunSession sess);

  /**
   * Get page of users with the given attributes.
   *
   * @param sess      session
   * @param query     query with page information
   * @param attrNames list of attribute names
   * @return page of requested rich users
   * @throws ResourceNotExistsException if resource with id from query does not exist
   * @throws VoNotExistsException       if vo with id from query does not exist
   * @throws FacilityNotExistsException if facility with id from query does not exist
   * @throws ServiceNotExistsException  if service with id from query does not exist
   */
  Paginated<RichUser> getUsersPage(PerunSession sess, UsersPageQuery query, List<String> attrNames)
      throws PrivilegeException, ResourceNotExistsException, VoNotExistsException, FacilityNotExistsException,
      ServiceNotExistsException;

  /**
   * Return list of users who matches the searchString, searching name, email and logins and are not member in specific
   * VO.
   *
   * @param sess
   * @param vo
   * @param searchString
   * @return
   * @throws InternalErrorException
   * @throws VoNotExistsException
   * @throws PrivilegeException
   */
  List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString)
      throws VoNotExistsException, PrivilegeException;

  /**
   * Returns all users who are not member of any VO.
   *
   * @param sess
   * @return list of users who are not member of any VO
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<User> getUsersWithoutVoAssigned(PerunSession sess) throws PrivilegeException;

  /**
   * Returns list of VOs, where the user is an Administrator. Including VOs, where the user is a VALID member of
   * authorized group.
   *
   * @param perunSession
   * @param user
   * @return list of VOs, where the user is an Administrator.
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<Vo> getVosWhereUserIsAdmin(PerunSession perunSession, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Returns list of VOs, where the user is a member.
   *
   * @param perunSession
   * @param user
   * @return list of VOs, where the user is a member.
   * @throws InternalErrorException
   */
  List<Vo> getVosWhereUserIsMember(PerunSession perunSession, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Checks if the login is available in the namespace. Returns FALSE is is already occupied, throws exception if value
   * is not allowed.
   *
   * @param sess
   * @param loginNamespace in which the login will be checked (provide only the name of the namespace, not the whole
   *                       attribute name)
   * @param login          to be checked
   * @return true if login is available, false otherwise
   * @throws InvalidLoginException When login to check has invalid syntax.
   */
  boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InvalidLoginException;

  /**
   * Return true if login is blocked (globally - for all namespaces per instance OR for some namespace), false if not.
   * Globally banned logins are ALWAYS case-insensitive (ignoreCase value is not taken into account for them).
   *
   * @param sess       session
   * @param login      login to check
   * @param ignoreCase ignore case
   * @return true if login is blocked
   */
  boolean isLoginBlocked(PerunSession sess, String login, boolean ignoreCase) throws PrivilegeException;

  /**
   * Return true if login is blocked for given namespace, false if not. When the namespace is null, then the method
   * behaves like isLoginBlockedGlobally(), so it checks if the login is blocked globally. Globally banned logins are
   * ALWAYS case-insensitive.
   *
   * @param sess       session
   * @param login      login to check
   * @param namespace  namespace for login
   * @param ignoreCase ignore case
   * @return true if login is blocked for given namespace (or globally for null namespace)
   */
  boolean isLoginBlockedForNamespace(PerunSession sess, String login, String namespace, boolean ignoreCase)
      throws PrivilegeException;

  /**
   * Return true if login is blocked globally (for all namespaces per instance - represented by namespace = null), false
   * if not. Globally banned logins are ALWAYS case-insensitive.
   *
   * @param sess  session
   * @param login login to check
   * @return true if login is blocked globally
   */
  boolean isLoginBlockedGlobally(PerunSession sess, String login) throws PrivilegeException;

  /**
   * Returns true if the user is PERUNADMIN.
   *
   * @param sess
   * @param user
   * @return true if the user is PERUNADMIN, false otherwise.
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  @Deprecated
  boolean isUserPerunAdmin(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException;

  /**
   * Check, if login exists in given login-namespace. Not implemented for all namespaces.
   *
   * @param sess           Perun session
   * @param user           User to check existence of login for
   * @param loginNamespace Login-namespace to check it for
   * @return True if login exists, false otherwise
   * @throws PrivilegeException
   * @throws UserNotExistsException When the user doesn't exist
   */
  boolean loginExist(PerunSession sess, User user, String loginNamespace)
      throws PrivilegeException, UserNotExistsException;

  /**
   * Take UserExtSource from sourceUser and move it to the targetUser.
   * <p>
   * It removes old UserExtSource with all it's attributes from sourceUser and creates and assigns the new one with the
   * same settings to target user.
   *
   * @param perunSession
   * @param sourceUser    user with UserExtSource to move
   * @param targetUser    user for who will be UserExtSource moved
   * @param userExtSource the UserExtSource which will be moved from sourceUser to targetUser
   * @throws InternalErrorException
   * @throws UserExtSourceNotExistsException UserExtSourceNotExists or is not assigned to sourceUser
   * @throws UserNotExistsException          one of the users not exists
   * @throws PrivilegeException
   */
  void moveUserExtSource(PerunSession perunSession, User sourceUser, User targetUser, UserExtSource userExtSource)
      throws UserExtSourceNotExistsException, UserNotExistsException, PrivilegeException;

  /**
   * Remove specificUser owner (the user) Only disable ownership of user and specificUser
   *
   * @param sess
   * @param user         the user
   * @param specificUser the specificUser
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws SpecificUserExpectedException                                                     when the specific user is
   *                                                                                           not really specific user
   *                                                                                           (is it normal user)
   * @throws NotSpecificUserExpectedException                                                  when the user is specific
   *                                                                                           User
   * @throws RelationNotExistsException                                                        if there is no such user
   *                                                                                           (the user) to remove
   * @throws cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException if there are 0 rows
   *                                                                                           affected by removing from
   *                                                                                           DB
   */
  void removeSpecificUserOwner(PerunSession sess, User user, User specificUser)
      throws UserNotExistsException, PrivilegeException, NotSpecificUserExpectedException,
      SpecificUserExpectedException, RelationNotExistsException, SpecificUserOwnerAlreadyRemovedException;

  /**
   * Removes user's external source.
   *
   * @param perunSession
   * @param user
   * @param userExtSource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserExtSourceNotExistsException
   * @throws UserNotExistsException
   * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void removeUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException,
      UserExtSourceAlreadyRemovedException;

  /**
   * Removes user's external source.
   *
   * @param perunSession
   * @param user
   * @param userExtSource
   * @param forceDelete   if true, persistent ExtSource is deleted too
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserExtSourceNotExistsException
   * @throws UserNotExistsException
   * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void removeUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource, boolean forceDelete)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException,
      UserExtSourceAlreadyRemovedException;

  /**
   * Removes user's external sources.
   *
   * @param perunSession
   * @param user
   * @param userExtSources
   * @param forceDelete    if true, persistent ExtSource is deleted too
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserExtSourceNotExistsException
   * @throws UserNotExistsException
   * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void removeUserExtSources(PerunSession perunSession, User user, List<UserExtSource> userExtSources,
                            boolean forceDelete)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException,
      UserExtSourceAlreadyRemovedException;

  /**
   * Request change of user's preferred email address. Change in attribute value is not done, until email address is
   * verified by link in email notice. (urn:perun:user:attribute-def:def:preferredMail)
   *
   * @param sess  PerunSession
   * @param url   base URL of running perun instance passed from RPC.
   * @param user  User to request preferred email change for
   * @param email new email address
   * @param lang  Language to get confirmation mail in (optional)
   * @param path  path that is appended to the url of the verification link (optional)
   * @param idp   authentication method appended to query parameters of verification link (optional)
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  void requestPreferredEmailChange(PerunSession sess, String url, User user, String email, String lang, String path,
                                   String idp) throws PrivilegeException, UserNotExistsException;

  /**
   * Reserves the password in external system. User must not exist.
   *
   * @param sess           Perun session
   * @param userLogin      String representation of the userLogin
   * @param loginNamespace Login-namespace to reserve password in
   * @param password       Password to be reserved
   * @throws InternalErrorException
   * @throws PasswordCreationFailedException
   * @throws InvalidLoginException           When login of user has invalid syntax (is not allowed)
   */
  void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password)
      throws PasswordCreationFailedException, PrivilegeException, PasswordOperationTimeoutException,
      PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException;

  /**
   * Reserves the password in external system. User must exist. User's login for specified namespace must exist in
   * Perun.
   *
   * @param sess           Perun session
   * @param user           User to reserve password for
   * @param loginNamespace Login-namespace to reserve password in
   * @param password       Password to be reserved
   * @throws InternalErrorException
   * @throws PasswordCreationFailedException
   * @throws UserNotExistsException          When the user doesn't exist
   * @throws LoginNotExistsException         When user doesn't have login in specified namespace
   * @throws PrivilegeException
   */
  void reservePassword(PerunSession sess, User user, String loginNamespace, String password)
      throws PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException,
      PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException,
      PasswordStrengthException;

  /**
   * Reserves random password in external system. User must exist. User's login for specified namespace must exist in
   * Perun.
   *
   * @param sess           Perun session
   * @param user           User to reserve password for
   * @param loginNamespace Login-namespace to reserve password in
   * @throws InternalErrorException
   * @throws PasswordCreationFailedException
   * @throws UserNotExistsException          When the user doesn't exist
   * @throws LoginNotExistsException         When user doesn't have login in specified namespace
   */
  void reserveRandomPassword(PerunSession sess, User user, String loginNamespace)
      throws PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException,
      PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException;

  /**
   * Allow users to manually add login in supported namespace if same login is not reserved. Can be set only to own
   * service or guest users => specific users.
   *
   * @param sess
   * @param user
   * @param loginNamespace
   * @param login
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @throws LoginExistsException
   * @throws InvalidLoginException
   */
  void setLogin(PerunSession sess, User user, String loginNamespace, String login)
      throws PrivilegeException, UserNotExistsException, LoginExistsException, InvalidLoginException;

  /**
   * Set specific user type for specific user and set ownership of this user for the owner.
   *
   * @param sess             perun session
   * @param specificUser     specific user
   * @param specificUserType specific type of user
   * @param owner            user, who will be owner of the specific user
   * @return specific user with specific user type set
   * @throws InternalErrorException
   * @throws RelationExistsException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  User setSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType, User owner)
      throws RelationExistsException, UserNotExistsException, PrivilegeException;

  /**
   * Unblock logins for given namespace or unblock logins globally (if no namespace is selected)
   *
   * @param sess
   * @param logins    list of logins to be unblocked
   * @param namespace namespace where the logins should be unblocked (null means unblock the logins globally)
   * @throws LoginIsNotBlockedException
   */
  void unblockLogins(PerunSession sess, List<String> logins, String namespace)
      throws PrivilegeException, LoginIsNotBlockedException;

  /**
   * Unblock logins by id globally, or in the namespace they were initially blocked.
   *
   * @param sess     session
   * @param loginIds list of login ids
   * @throws PrivilegeException         insufficient permissions
   * @throws LoginIsNotBlockedException when login is not blocked
   */
  void unblockLoginsById(PerunSession sess, List<Integer> loginIds)
      throws PrivilegeException, LoginIsNotBlockedException;

  /**
   * Remove all ownerships of this specific user and unset this specific user type from this specific user.
   *
   * @param sess             perun session
   * @param specificUser     specific user
   * @param specificUserType specific type of user
   * @return user who is no more specific
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws ServiceOnlyRoleAssignedException when trying to unset service flag from a user with service only role
   */
  User unsetSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType)
      throws UserNotExistsException, PrivilegeException, ServiceOnlyRoleAssignedException;

  /**
   * Updates titles before/after name of user.
   * <p>
   * New titles must be set inside User object. Setting any title to null will remove title from name. Other user's
   * properties are ignored.
   *
   * @param perunSession
   * @param user
   * @return updated user with new titles before/after name
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  User updateNameTitles(PerunSession perunSession, User user) throws UserNotExistsException, PrivilegeException;

  /**
   * Updates users data in DB.
   *
   * @param perunSession
   * @param user
   * @return updated user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   */
  User updateUser(PerunSession perunSession, User user) throws UserNotExistsException, PrivilegeException;

  /**
   * Updates user's userExtSource in DB. Login and LoA can be updated this way.
   *
   * @param perunSession
   * @param userExtSource
   * @return updated userExtSource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserExtSourceExistsException    When UES with same login/extSource already exists.
   * @throws UserExtSourceNotExistsException When UES by its ID doesn't exists
   */
  UserExtSource updateUserExtSource(PerunSession perunSession, UserExtSource userExtSource)
      throws UserExtSourceNotExistsException, UserExtSourceExistsException, PrivilegeException;

  /**
   * Updates user's userExtSource last access time in DB. We can get information which userExtSource has been used as a
   * last one.
   *
   * @param perunSession
   * @param userExtSource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserExtSourceNotExistsException
   */
  void updateUserExtSourceLastAccess(PerunSession perunSession, UserExtSource userExtSource)
      throws PrivilegeException, UserExtSourceNotExistsException;

  /**
   * Validates the password in external system and sets user extSources and extSource related attributes. User must not
   * exist.
   *
   * @param sess           Perun session
   * @param userLogin      String representation of the userLogin
   * @param loginNamespace Login-namespace to validate password in
   * @throws InternalErrorException
   * @throws PasswordCreationFailedException
   * @throws InvalidLoginException           When login of user has invalid syntax (is not allowed)
   */
  void validatePassword(PerunSession sess, String userLogin, String loginNamespace)
      throws PasswordCreationFailedException, PrivilegeException, InvalidLoginException;

  /**
   * Validates the password in external system and sets user extSources and extSource related attributes. User must
   * exist. User's login for specified namespace must exist in Perun.
   *
   * @param sess           Perun session
   * @param user           User whose password should being validated
   * @param loginNamespace Login-namespace to validate password in
   * @throws InternalErrorException
   * @throws PasswordCreationFailedException
   * @throws UserNotExistsException          When the user doesn't exist
   * @throws LoginNotExistsException         When user doesn't have login in specified namespace
   * @throws PrivilegeException
   */
  void validatePassword(PerunSession sess, User user, String loginNamespace)
      throws PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException,
      InvalidLoginException;

  /**
   * Validate change of user's preferred email address. New email address is set as value of
   * urn:perun:user:attribute-def:def:preferredMail attribute.
   *
   * @param sess  PerunSession
   * @param user  User to validate email address for
   * @param token token for the email change request to validate
   * @return String return new preferred email
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   */
  String validatePreferredEmailChange(PerunSession sess, User user, UUID token)
      throws PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException,
      WrongReferenceAttributeValueException, WrongAttributeValueException;

  /**
   * Validate ssh public key, throws exception if validation fails
   *
   * @param sess   sess
   * @param sshKey ssh public key to verify
   * @throws SSHKeyNotValidException when validation fails
   */
  void validateSSHKey(PerunSession sess, String sshKey) throws SSHKeyNotValidException;
}
