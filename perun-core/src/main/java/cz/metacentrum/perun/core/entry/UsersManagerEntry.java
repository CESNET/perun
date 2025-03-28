package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.BlockedLogin;
import cz.metacentrum.perun.core.api.BlockedLoginsPageQuery;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.UsersPageQuery;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.DeletionNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
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
import cz.metacentrum.perun.core.api.exceptions.PersonalDataChangeNotEnabledException;
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
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UsersManager entry logic
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @author Sona Mastrakova
 */
public class UsersManagerEntry implements UsersManager {

  private static final Logger LOG = LoggerFactory.getLogger(UsersManagerEntry.class);

  private UsersManagerBl usersManagerBl;
  private PerunBl perunBl;

  public UsersManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.usersManagerBl = perunBl.getUsersManagerBl();
  }

  public UsersManagerEntry() {
  }

  @Override
  public void addSpecificUserOwner(PerunSession sess, User user, User specificUser)
      throws UserNotExistsException, PrivilegeException, SpecificUserExpectedException,
                 NotSpecificUserExpectedException, RelationExistsException {
    Utils.checkPerunSession(sess);
    getUsersManagerBl().checkUserExists(sess, user);
    getUsersManagerBl().checkUserExists(sess, specificUser);
    if (user.isServiceUser()) {
      throw new NotSpecificUserExpectedException(user);
    }
    if (user.isSponsoredUser() && specificUser.isSponsoredUser()) {
      throw new NotSpecificUserExpectedException(specificUser);
    }
    if (!specificUser.isSpecificUser()) {
      throw new SpecificUserExpectedException(specificUser);
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addSpecificUserOwner_User_User_policy", specificUser) &&
            !AuthzResolver.authorizedInternal(sess, "owner-addSpecificUserOwner_User_User_policy", user)) {
      throw new PrivilegeException(sess, "addSpecificUserOwner");
    }
    getUsersManagerBl().addSpecificUserOwner(sess, user, specificUser);
  }

  @Override
  public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource)
      throws UserNotExistsException, PrivilegeException, UserExtSourceExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(userExtSource, "userExtSource");
    Utils.notNull(user, "user");
    Utils.notNull(userExtSource.getExtSource(), "ExtSource in UserExtSource");
    Utils.notNull(userExtSource.getLogin(), "Login in UserExtSource");
    Utils.notNull(userExtSource.getExtSource().getType(), "Type of ExtSource in UserExtSource");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addUserExtSource_User_UserExtSource_policy",
        List.of(user))) {
      throw new PrivilegeException(sess, "addUserExtSource");
    }

    getUsersManagerBl().checkUserExists(sess, user);
    // set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
    userExtSource.setUserId(user.getId());

    try {
      getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);
      throw new UserExtSourceExistsException("UserExtSource " + userExtSource + " already exists");
    } catch (UserExtSourceNotExistsException e) {
      // This is ok
    }

    return getUsersManagerBl().addUserExtSource(sess, user, userExtSource);
  }

  @Override
  public RichUserExtSource addUserExtSourceWithAttributes(PerunSession sess, User user, UserExtSource userExtSource,
                                                          List<Attribute> uesAttributes)
      throws UserNotExistsException, UserExtSourceExistsException, PrivilegeException,
                 WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 UserExtSourceNotExistsException, AttributeNotExistsException, WrongAttributeValueException {
    UserExtSource ues = this.addUserExtSource(sess, user, userExtSource);
    this.perunBl.getAttributesManager().setAttributes(sess, ues, uesAttributes);
    List<Attribute> createdUesAttributes = this.perunBl.getAttributesManager().getAttributes(sess, ues);
    return new RichUserExtSource(ues, createdUesAttributes);
  }

  @Override
  public void anonymizeUser(PerunSession sess, User user, boolean force)
      throws UserNotExistsException, PrivilegeException, RelationExistsException, AnonymizationNotSupportedException {
    Utils.checkPerunSession(sess);

    // Authorization
    for (UserExtSource ues : perunBl.getUsersManager().getUserExtSources(sess, user)) {
      if (!AuthzResolver.authorizedInternal(sess, "anonymizeUser_User_boolean_policy", user, ues)) {
        throw new PrivilegeException(sess, "anonymizeUser");
      }
    }
    if (force) {
      //loop through user's members as well to check MFA
      for (Member member : perunBl.getMembersManager().getMembersByUser(sess, user)) {
        if (!AuthzResolver.authorizedInternal(sess, "anonymizeUser_User_boolean_policy", user, member)) {
          throw new PrivilegeException(sess, "anonymizeUser");
        }
      }
    }

    getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().anonymizeUser(sess, user, force);
  }

  @Override
  public void blockLogins(PerunSession sess, List<String> logins, String namespace)
      throws PrivilegeException, LoginIsAlreadyBlockedException, LoginExistsException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "blockLogins_List<String>_String_policy")) {
      throw new PrivilegeException(sess, "blockLogins");
    }

    getUsersManagerBl().blockLogins(sess, logins, namespace, null);
  }

  @Override
  public void changeNonAuthzPassword(PerunSession sess, UUID token, String password, String lang)
      throws UserNotExistsException, LoginNotExistsException, PasswordChangeFailedException,
                 PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException,
                 PasswordStrengthException, PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
    Utils.checkPerunSession(sess);
    Utils.notNull(token, "token");

    if (lang == null || lang.isEmpty()) {
      lang = "en"; // fallback to english
    }

    getPerunBl().getUsersManagerBl().changeNonAuthzPassword(sess, token, password, lang);
  }

  @Override
  public void changePassword(PerunSession sess, User user, String loginNamespace, String oldPassword,
                             String newPassword, boolean checkOldPassword) throws
      PrivilegeException, UserNotExistsException, LoginNotExistsException, PasswordDoesntMatchException,
          PasswordChangeFailedException, PasswordOperationTimeoutException, PasswordStrengthFailedException,
          InvalidLoginException, PasswordStrengthException {
    Utils.checkPerunSession(sess);

    getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changePassword_User_String_String_String_boolean_policy", user)) {
      throw new PrivilegeException(sess, "changePassword");
    }

    // Check if the login-namespace already exists and the user has a login in the login-namespace
    // Create attribute name
    String attributeName =
        AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace;

    try {
      getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attributeName);
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
      throw new LoginNotExistsException(e);
    }

    getUsersManagerBl().changePassword(sess, user, loginNamespace, oldPassword, newPassword, checkOldPassword);
  }

  @Override
  public void changePassword(PerunSession sess, String login, String loginNamespace, String oldPassword,
                             String newPassword, boolean checkOldPassword) throws
      PrivilegeException, LoginNotExistsException, PasswordDoesntMatchException, PasswordChangeFailedException,
          PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException,
          PasswordStrengthException {
    Utils.checkPerunSession(sess);

    String attributeName =
        AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + loginNamespace;

    List<User> users;
    try {
      users = getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, login);
    } catch (ConsistencyErrorException e) {
      // attr def not exists by implementation in getUsersByAttributeValue
      throw new LoginNotExistsException(e);
    }
    if (users.size() > 1) {
      throw new ConsistencyErrorException("Multiple users found for login: " + login);
    }
    if (users.isEmpty()) {
      throw new LoginNotExistsException("User with login: " + login + " not exists.");
    }
    User user = users.get(0);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changePassword_String_String_String_String_boolean_policy", user)) {
      throw new PrivilegeException(sess, "changePassword");
    }

    getUsersManagerBl().changePassword(sess, user, loginNamespace, oldPassword, newPassword, checkOldPassword);
  }

  @Override
  public String changePasswordRandom(PerunSession sess, User user, String loginNamespace)
      throws PrivilegeException, PasswordOperationTimeoutException, LoginNotExistsException,
                 PasswordChangeFailedException, InvalidLoginException, PasswordStrengthException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changePasswordRandom_User_String_policy", user)) {
      throw new PrivilegeException("changePasswordRandom");
    }

    return usersManagerBl.changePasswordRandom(sess, user, loginNamespace);
  }

  @Override
  public void checkPasswordResetRequestIsValid(PerunSession sess, UUID token)
      throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
    Utils.checkPerunSession(sess);
    Utils.notNull(token, "token");

    getPerunBl().getUsersManagerBl().checkPasswordResetRequestIsValid(sess, token);
  }

  @Override
  public void checkPasswordStrength(PerunSession sess, String password, String namespace, String login)
      throws PasswordStrengthException {
    Utils.checkPerunSession(sess);
    usersManagerBl.checkPasswordStrength(sess, password, namespace, login);
  }

  @Override
  public void createAlternativePassword(PerunSession sess, User user, String description, String loginNamespace,
                                        String password)
      throws PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException,
                 PasswordStrengthException {
    Utils.checkPerunSession(sess);
    Utils.notNull(description, "description");
    Utils.notNull(loginNamespace, "loginNamespace");
    Utils.notNull(password, "password");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "createAlternativePassword_User_String_String_String_policy", user)) {
      throw new PrivilegeException(sess, "createAlternativePassword");
    }

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().createAlternativePassword(sess, user, description, loginNamespace, password);
  }

  /**
   * Create a list of PerunBeans from UserPageQuery. If beanIds are not null it also checks if they exist. It will skip
   * them otherwise.
   *
   * @param sess
   * @param query
   * @return list of PerunBeans created from the given parameters
   * @throws FacilityNotExistsException
   * @throws VoNotExistsException
   * @throws ServiceNotExistsException
   */
  private List<PerunBean> createListOfBeans(PerunSession sess, UsersPageQuery query)
      throws FacilityNotExistsException, VoNotExistsException, ServiceNotExistsException {
    List<PerunBean> beans = new ArrayList<>();
    if (query.getFacilityId() != null) {
      Facility facility = perunBl.getFacilitiesManagerBl().getFacilityById(sess, query.getFacilityId());
      perunBl.getFacilitiesManagerBl().checkFacilityExists(sess, facility);

      beans.add(facility);
      if (query.getVoId() != null) {
        Vo vo = perunBl.getVosManagerBl().getVoById(sess, query.getVoId());
        getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
        beans.add(vo);
      }
      if (query.getServiceId() != null) {
        Service service = perunBl.getServicesManagerBl().getServiceById(sess, query.getServiceId());
        getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
        beans.add(service);
      }
    }
    return beans;
  }

  @Override
  public User createServiceUser(PerunSession sess, Candidate candidate, List<User> owners)
      throws PrivilegeException, WrongAttributeAssignmentException, UserExtSourceExistsException,
                 WrongReferenceAttributeValueException, WrongAttributeValueException, AttributeNotExistsException,
                 UserNotExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(candidate, "candidate");

    for (User owner : owners) {
      getPerunBl().getUsersManagerBl().checkUserExists(sess, owner);
    }

    if (!AuthzResolver.authorizedInternal(sess, "createServiceUser_policy")) {
      throw new PrivilegeException("createServiceUser");
    }

    return perunBl.getUsersManagerBl().createServiceUser(sess, candidate, owners);
  }

  @Override
  @Deprecated
  public User createUser(PerunSession sess, User user) throws PrivilegeException {
    Utils.checkPerunSession(sess);
    Utils.notNull(user, "user");

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      throw new PrivilegeException(sess, "createUser");
    }

    // Create the user
    return getUsersManagerBl().createUser(sess, user);
  }

  @Override
  public void deleteAlternativePassword(PerunSession sess, User user, String loginNamespace, String passwordId)
      throws UserNotExistsException, PasswordDeletionFailedException, PrivilegeException, LoginNotExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(loginNamespace, "loginNamespace");
    Utils.notNull(passwordId, "passwordId");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deleteAlternativePassword_User_String_String_policy", user)) {
      throw new PrivilegeException(sess, "deleteAlternativePassword");
    }

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().deleteAlternativePassword(sess, user, loginNamespace, passwordId);
  }

  @Override
  public void deletePassword(PerunSession sess, String userLogin, String loginNamespace) throws
      PrivilegeException, PasswordDeletionFailedException, LoginNotExistsException, PasswordOperationTimeoutException,
          InvalidLoginException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deletePassword_String_String_policy")) {
      throw new PrivilegeException(sess, "deletePassword");
    }

    if (getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
      // NOT RESERVED BY ATTRIBUTE OR REGISTRAR
      throw new PasswordDeletionFailedException(
          "Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
    } else {
      // RESERVED BY ATTRIBUTE OR REGISTRAR
      try {
        getPerunBl().getUsersManagerBl().checkReservedLogins(sess, loginNamespace, userLogin, false);
        // RESERVED BY ATTRIBUTE ONLY - we don't want to delete logins in use
        throw new PasswordDeletionFailedException("Login " + userLogin + " in namespace " + loginNamespace +
                                                      " can't be delete, because it's already in use.");
      } catch (AlreadyReservedLoginException ex) {
        // RESERVED BY REGISTRAR AND MAYBE BY ATTRIBUTE
        // reservation by both should not occur
        getUsersManagerBl().deletePassword(sess, userLogin, loginNamespace);
      }
    }
  }

  @Override
  public void deletePassword(PerunSession sess, User user, String loginNamespace) throws
      PrivilegeException, UserNotExistsException, LoginNotExistsException, InvalidLoginException,
          PasswordOperationTimeoutException, PasswordDeletionFailedException {
    Utils.checkPerunSession(sess);

    // Authorization
    boolean authorized;
    if (!user.isServiceUser()) {
      authorized = AuthzResolver.authorizedInternal(sess, "deletePassword_User_String_policy", user);
    } else {
      authorized = AuthzResolver.authorizedInternal(sess, "service_user-deletePassword_User_String_policy", user);
    }
    if (!authorized) {
      throw new PrivilegeException(sess, "deletePassword");
    }

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().deletePassword(sess, user, loginNamespace);
  }

  @Override
  public void deleteUser(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException,
                 UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, DeletionNotSupportedException {
    Utils.checkPerunSession(sess);

    // Authorization
    for (UserExtSource ues : perunBl.getUsersManagerBl().getUserExtSources(sess, user)) {
      if (!AuthzResolver.authorizedInternal(sess, "deleteUser_User_policy", user, ues)) {
        throw new PrivilegeException(sess, "deleteUser");
      }
    }

    getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().deleteUser(sess, user);
  }

  @Override
  public void deleteUser(PerunSession sess, User user, boolean forceDelete)
      throws UserNotExistsException, PrivilegeException, RelationExistsException, MemberAlreadyRemovedException,
                 UserAlreadyRemovedException, SpecificUserAlreadyRemovedException, DeletionNotSupportedException {
    Utils.checkPerunSession(sess);

    // Authorization
    for (UserExtSource ues : perunBl.getUsersManager().getUserExtSources(sess, user)) {
      if (!AuthzResolver.authorizedInternal(sess, "deleteUser_User_boolean_policy", user, ues)) {
        throw new PrivilegeException(sess, "deleteUser");
      }
    }
    if (forceDelete) {
      //loop through user's members as well to check MFA
      for (Member member : perunBl.getMembersManager().getMembersByUser(sess, user)) {
        if (!AuthzResolver.authorizedInternal(sess, "deleteUser_User_boolean_policy", user, member)) {
          throw new PrivilegeException(sess, "deleteUser");
        }
      }
    }

    getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().deleteUser(sess, user, forceDelete);
  }

  /**
   * Filters rich users to which the principal has access.
   *
   * @param sess  session
   * @param users list of all rich users
   * @return filtered list of rich users
   */
  private List<RichUser> filterRichUsers(PerunSession sess, List<RichUser> users) {
    List<RichUser> result = new ArrayList<>();
    String filterPolicy = "filter-findRichUsersWithAttributes_policy";

    for (RichUser user : users) {
      List<Vo> vos = perunBl.getUsersManagerBl().getVosWhereUserIsMember(sess, user);
      List<Group> groups = perunBl.getGroupsManagerBl().getUserGroups(sess, user);
      List<Facility> facilities = perunBl.getFacilitiesManagerBl().getAssignedFacilities(sess, user);
      List<Resource> resources = perunBl.getUsersManagerBl().getAllowedResources(sess, user);

      if (AuthzResolver.authorizedInternal(sess, filterPolicy) ||
              vos.stream().anyMatch(vo -> AuthzResolver.authorizedInternal(sess, filterPolicy, vo)) ||
              groups.stream().anyMatch(group -> AuthzResolver.authorizedInternal(sess, filterPolicy, group)) ||
              facilities.stream()
                  .anyMatch(facility -> AuthzResolver.authorizedInternal(sess, filterPolicy, facility)) ||
              resources.stream().anyMatch(resource -> AuthzResolver.authorizedInternal(sess, filterPolicy, resource))) {
        result.add(user);
      }
    }

    return result;
  }

  @Override
  public List<RichUser> findRichUsers(PerunSession sess, String searchString)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "findRichUsers_String_policy")) {
      throw new PrivilegeException(sess, "findUsersByName");
    }

    return getPerunBl().getUsersManagerBl()
               .filterOnlyAllowedAttributes(sess, getUsersManagerBl().findRichUsers(sess, searchString));
  }

  @Override
  public List<RichUser> findRichUsersWithAttributes(PerunSession sess, String searchString, List<String> attrNames)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    searchString = searchString.trim();

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "findRichUsersWithAttributes_String_List<String>_policy")) {

      if (AuthzResolver.authorizedInternal(sess, "user-findRichUsersWithAttributes_String_List<String>_policy")) {
        // necessary when adding new owners to service accounts
        List<User> serviceIdentities =
            getPerunBl().getUsersManagerBl().getSpecificUsersByUser(sess, sess.getPerunPrincipal().getUser());
        if (serviceIdentities.isEmpty()) {
          // user isn't owner of any service identity
          throw new PrivilegeException(sess, "findRichUsersWithAttributes");
        }
      } else {
        throw new PrivilegeException(sess, "findRichUsersWithAttributes");
      }
    }

    List<RichUser> users = getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrNames));
    return filterRichUsers(sess, users);
  }

  @Override
  public List<RichUser> findRichUsersWithAttributesByExactMatch(PerunSession sess, String searchString,
                                                                List<String> attrNames)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess,
        "findRichUsersWithAttributesByExactMatch_String_List<String>_policy")) { // called only internally by registrar
      throw new PrivilegeException(sess, "findRichUsersWithAttributesByExactMatch");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        getUsersManagerBl().findRichUsersWithAttributesByExactMatch(sess, searchString, attrNames));

  }

  @Override
  public List<RichUser> findRichUsersWithoutSpecificVoWithAttributes(PerunSession sess, Vo vo, String searchString,
                                                                     List<String> attrsName)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess,
        "findRichUsersWithoutSpecificVoWithAttributes_Vo_String_List<String>_policy", vo)) {
      throw new PrivilegeException(sess, "findRichUsersWithoutSpecificVoWithAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        getUsersManagerBl().findRichUsersWithoutSpecificVoWithAttributes(sess, vo, searchString, attrsName));
  }

  @Override
  public List<User> findUsers(PerunSession sess, String searchString) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "findUsers_String_policy")) {
      throw new PrivilegeException(sess, "findUsersByName");
    }

    return getUsersManagerBl().findUsers(sess, searchString);
  }

  @Override
  public List<User> findUsersByExactName(PerunSession sess, String searchString) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "findUsersByExactName_String_policy")) {
      throw new PrivilegeException(sess, "findUsersByExactName");
    }

    return getUsersManagerBl().findUsersByExactName(sess, searchString);
  }

  @Override
  public List<User> findUsersByName(PerunSession sess, String searchString) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "findUsersByName_String_policy")) {
      throw new PrivilegeException(sess, "findUsersByName");
    }

    return getUsersManagerBl().findUsersByName(sess, searchString);
  }

  @Override
  public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName,
                                    String lastName, String titleAfter) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "findUsersByName_String_String_String_String_String_policy")) {
      throw new PrivilegeException(sess, "findUsersByName");
    }

    return getUsersManagerBl().findUsersByName(sess, titleBefore, firstName, middleName, lastName, titleAfter);
  }

  @Override
  public Map<String, String> generateAccount(PerunSession sess, String namespace, Map<String, String> parameters)
      throws PrivilegeException, PasswordStrengthException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "generateAccount_String_Map<String_String>_policy")) {
      throw new PrivilegeException(sess, "generateAccount");
    }

    return getUsersManagerBl().generateAccount(sess, namespace, parameters);

  }

  @Override
  public List<BlockedLogin> getAllBlockedLoginsInNamespaces(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getAllBlockedLoginsInNamespaces_policy")) {
      throw new PrivilegeException(sess, "getAllBlockedLoginsInNamespaces");
    }

    return getUsersManagerBl().getAllBlockedLoginsInNamespaces(sess);
  }

  @Override
  public List<RichUser> getAllRichUsers(PerunSession sess, boolean includedSpecificUsers) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAllRichUsers_boolean_policy")) {
      throw new PrivilegeException(sess, "getAllRichUsers");
    }

    return getPerunBl().getUsersManagerBl()
               .filterOnlyAllowedAttributes(sess, getUsersManagerBl().getAllRichUsers(sess, includedSpecificUsers));
  }

  @Override
  public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers)
      throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAllRichUsersWithAttributes_boolean_policy")) {
      throw new PrivilegeException(sess, "getAllRichUsersWithAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        getUsersManagerBl().getAllRichUsersWithAttributes(sess, includedSpecificUsers));
  }

  @Override
  public List<RichUser> getAllRichUsersWithAttributes(PerunSession sess, boolean includedSpecificUsers,
                                                      List<String> attrsNames)
      throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAllRichUsersWithAttributes_boolean_List<String>_policy")) {
      throw new PrivilegeException(sess, "getAllRichUsersWithAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        getUsersManagerBl().getAllRichUsersWithAttributes(sess, includedSpecificUsers, attrsNames));

  }

  @Override
  public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user)
      throws FacilityNotExistsException, UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getAllowedResources_Facility_User_policy",
        Arrays.asList(facility, user))) {
      throw new PrivilegeException(sess, "getAllowedResources");
    }

    getUsersManagerBl().checkUserExists(sess, user);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getUsersManagerBl().getAllowedResources(sess, facility, user);
  }

  @Override
  public List<Resource> getAllowedResources(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getAllowedResources_User_policy", user)) {
      throw new PrivilegeException(sess, "getAllowedResources");
    }

    getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().getAllowedResources(sess, user);
  }


  public Map<Facility, List<Resource>> getUserAssignments(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getUsersManagerBl().checkUserExists(sess, user);


    if (!AuthzResolver.authorizedInternal(sess, "getUserAssignments_User_policy", user)) {
      throw new PrivilegeException(sess, "getUserAssignments");
    }

    List<Facility> facilities = perunBl.getFacilitiesManager().getAssignedFacilities(sess, user);
    Map<Facility, List<Resource>> assignments = new HashMap<>();

    for (Facility fac : facilities) {
      List<Resource> resources;
      try {
        resources = getAssociatedResources(sess, fac, user);
      } catch (FacilityNotExistsException e) {
        throw new ConsistencyErrorException("Facility should exist", e);
      }

      assignments.put(fac, resources);
    }

    return assignments;
  }

  @Override
  public List<RichResource> getAssignedRichResources(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getAssignedRichResources_User_policy", user)) {
      throw new PrivilegeException(sess, "getAssignedRichResources");
    }

    getUsersManagerBl().checkUserExists(sess, user);
    return getUsersManagerBl().getAssignedRichResources(sess, user);
  }

  @Override
  public List<Resource> getAssociatedResources(PerunSession sess, Facility facility, User user)
      throws UserNotExistsException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getAssociatedResources_Facility_User_policy", facility, user)) {
      throw new PrivilegeException(sess, "getAssociatedResources");
    }

    getUsersManagerBl().checkUserExists(sess, user);
    perunBl.getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getUsersManagerBl().getAssociatedResources(sess, facility, user);
  }

  @Override
  public Paginated<BlockedLogin> getBlockedLoginsPage(PerunSession sess, BlockedLoginsPageQuery query)
      throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getBlockedLoginsPage_BlockedLoginsPageQuery_policy")) {
      throw new PrivilegeException(sess, "getBlockedLoginsPage");
    }

    return getUsersManagerBl().getBlockedLoginsPage(sess, query);
  }

  @Override
  public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user)
      throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getGroupsWhereUserIsActive_Resource_User_policy",
        Arrays.asList(resource, user))) {
      throw new PrivilegeException("getGroupsWhereUserIsActive");
    }

    return perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, resource, user);
  }

  @Override
  public List<Group> getGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user)
      throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getGroupsWhereUserIsActive_Facility_User_policy",
        Arrays.asList(facility, user))) {
      throw new PrivilegeException("getGroupsWhereUserIsActive");
    }

    return perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, facility, user);
  }

  @Override
  public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getGroupsWhereUserIsAdmin_User_policy", user)) {
      throw new PrivilegeException(sess, "getGroupsWhereUserIsAdmin");
    }

    getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().getGroupsWhereUserIsAdmin(sess, user);
  }

  @Override
  public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user)
      throws PrivilegeException, UserNotExistsException, VoNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getGroupsWhereUserIsAdmin_Vo_User_policy", Arrays.asList(vo, user))) {
      throw new PrivilegeException(sess, "getGroupsWhereUserIsAdmin");
    }

    return getUsersManagerBl().getGroupsWhereUserIsAdmin(sess, vo, user);
  }

  @Override
  public List<String> getPendingPreferredEmailChanges(PerunSession sess, User user)
      throws PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException,
                 AttributeNotExistsException {

    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getPendingPreferredEmailChanges_User_policy", user)) {
      throw new PrivilegeException(sess, "getPendingPreferredEmailChanges");
    }

    return getPerunBl().getUsersManagerBl().getPendingPreferredEmailChanges(sess, user);

  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public Integer getRelatedUserIdByBlockedLoginInNamespace(PerunSession sess, String login, String namespace)
      throws LoginIsNotBlockedException {
    return getUsersManagerBl().getRelatedUserIdByBlockedLoginInNamespace(sess, login, namespace);
  }

  @Override
  public List<RichGroup> getRichGroupsWhereUserIsActive(PerunSession sess, Resource resource, User user,
                                                        List<String> attrNames) throws PrivilegeException {

    if (!AuthzResolver.authorizedInternal(sess, "getRichGroupsWhereUserIsActive_Resource_User_List<String>_policy",
        Arrays.asList(resource, user))) {
      throw new PrivilegeException("getRichGroupsWhereUserIsActive");
    }

    return perunBl.getGroupsManagerBl().filterOnlyAllowedAttributes(sess,
        perunBl.getGroupsManagerBl().convertGroupsToRichGroupsWithAttributes(sess,
            perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, resource, user), attrNames), null, true);

  }

  @Override
  public List<RichGroup> getRichGroupsWhereUserIsActive(PerunSession sess, Facility facility, User user,
                                                        List<String> attrNames) throws PrivilegeException {

    if (!AuthzResolver.authorizedInternal(sess, "getRichGroupsWhereUserIsActive_Facility_User_List<String>_policy",
        Arrays.asList(facility, user))) {
      throw new PrivilegeException("getRichGroupsWhereUserIsActive");
    }

    return perunBl.getGroupsManagerBl().filterOnlyAllowedAttributes(sess,
        perunBl.getGroupsManagerBl().convertGroupsToRichGroupsWithAttributes(sess,
            perunBl.getUsersManagerBl().getGroupsWhereUserIsActive(sess, facility, user), attrNames), null, true);


  }

  @Override
  public RichUser getRichUser(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getRichUser_User_policy", user)) {
      throw new PrivilegeException(sess, "getRichUser");
    }

    return getPerunBl().getUsersManagerBl()
               .filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUser(sess, user));
  }

  @Override
  public List<RichUserExtSource> getRichUserExtSources(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    return getRichUserExtSources(sess, user, null);
  }

  @Override
  public List<RichUserExtSource> getRichUserExtSources(PerunSession sess, User user, List<String> attrsNames)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getRichUserExtSources_User_List<String>_policy", user)) {
      throw new PrivilegeException(sess, "getRichUserExtSources");
    }

    getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().filterOnlyAllowedAttributesForRichUserExtSources(
        sess, getUsersManagerBl().getRichUserExtSources(sess, user, attrsNames));
  }

  @Override
  public RichUser getRichUserWithAttributes(PerunSession sess, User user)
      throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getRichUserWithAttributes_User_policy", user)) {
      throw new PrivilegeException(sess, "getRichUserWithAttributes");
    }

    return getPerunBl().getUsersManagerBl()
               .filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUserWithAttributes(sess, user));
  }

  @Override
  public List<RichUser> getRichUsersByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getRichUsersByIds_List<Integer>_policy")) {
      throw new PrivilegeException(sess, "getRichUsersByIds");
    }
    List<RichUser> richUsers = getUsersManagerBl().getRichUsersByIds(sess, ids);
    richUsers.removeIf(
        richUser -> !AuthzResolver.authorizedInternal(sess, "filter-getRichUsersByIds_List<Integer>_policy", richUser));

    return richUsers;
  }

  @Override
  public List<RichUser> getRichUsersWithAttributesByIds(PerunSession sess, List<Integer> ids)
      throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getRichUsersWithAttributesByIds_List<Integer>_policy")) {
      throw new PrivilegeException(sess, "getRichUsersWithAttributesByIds");
    }
    List<RichUser> richUsers = getUsersManagerBl().getRichUsersWithAttributesByIds(sess, ids);
    richUsers.removeIf(richUser -> !AuthzResolver.authorizedInternal(sess,
        "filter-getRichUsersWithAttributesByIds_List<Integer>_policy", richUser));

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, richUsers);
  }

  @Override
  public List<RichUser> getRichUsersWithoutVoAssigned(PerunSession sess)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getRichUsersWithoutVoAssigned_policy")) {
      throw new PrivilegeException(sess, "getRichUsersWithoutVoAssigned");
    }

    return getPerunBl().getUsersManagerBl()
               .filterOnlyAllowedAttributes(sess, getUsersManagerBl().getRichUsersWithoutVoAssigned(sess));
  }

  @Override
  public List<RichUser> getRichUsersWithoutVoWithAttributes(PerunSession sess, List<String> attrsName)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getRichUsersWithoutVoWithAttributes_List<String>_policy")) {
      throw new PrivilegeException(sess, "getRichUsersWithoutVOWithAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess,
        getUsersManagerBl().getRichUsersWithoutVoWithAttributes(sess, attrsName));
  }

  @Override
  public List<User> getSpecificUsers(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);
    if (!AuthzResolver.authorizedInternal(sess, "getSpecificUsers_policy")) {
      throw new PrivilegeException(sess, "getSpecificUsers");
    }
    return getUsersManagerBl().getSpecificUsers(sess);
  }

  @Override
  public List<User> getSpecificUsersByUser(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException, NotSpecificUserExpectedException {
    Utils.checkPerunSession(sess);
    getUsersManagerBl().checkUserExists(sess, user);
    if (user.isServiceUser()) {
      throw new NotSpecificUserExpectedException(user);
    }

    List<Vo> vos = getUsersManagerBl().getVosWhereUserIsMember(sess, user);
    for (Vo vo : vos) {
      if (!AuthzResolver.authorizedInternal(sess, "getSpecificUsersByUser_User_policy", Arrays.asList(user, vo))) {
        throw new PrivilegeException(sess, "getSpecificUsersByUser");
      }
    }
    return getUsersManagerBl().getSpecificUsersByUser(sess, user);
  }

  @Override
  @Deprecated
  public List<RichUser> getSponsors(PerunSession sess, Member member, List<String> attrNames)
      throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(member, "member");
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getSponsors_Member_List<String>_policy", member)) {
      throw new PrivilegeException(sess, "getSponsors");
    }
    List<User> sponsors = usersManagerBl.getSponsors(sess, member);
    if (attrNames == null || attrNames.isEmpty()) {
      //adds all existing atributes
      return usersManagerBl.convertRichUsersToRichUsersWithAttributes(sess,
          usersManagerBl.convertUsersToRichUsers(sess, sponsors));
    } else {
      //adds only selected atributes (if the list would be empty, it will return no attributes)
      return usersManagerBl.convertUsersToRichUsersWithAttributesByNames(sess, sponsors, attrNames);
    }
  }

  @Override
  public List<Sponsor> getSponsorsForMember(PerunSession sess, Member member, List<String> attrNames)
      throws PrivilegeException {
    Utils.checkPerunSession(sess);
    Utils.notNull(member, "member");
    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getSponsorsForMember_Member_List<String>_policy", member)) {
      throw new PrivilegeException(sess, "getSponsorsForMember");
    }
    List<User> sponsors = usersManagerBl.getSponsors(sess, member);
    List<RichUser> richUsers;
    if (attrNames == null || attrNames.isEmpty()) {
      //adds all existing atributes
      try {
        richUsers = usersManagerBl.convertRichUsersToRichUsersWithAttributes(sess,
            usersManagerBl.convertUsersToRichUsers(sess, sponsors));
      } catch (UserNotExistsException e) {
        throw new InternalErrorException(e);
      }
    } else {
      //adds only selected atributes (if the list would be empty, it will return no attributes)
      richUsers = usersManagerBl.convertUsersToRichUsersWithAttributesByNames(sess, sponsors, attrNames);
    }

    return richUsers.stream()
               .map(richUser -> perunBl.getMembersManagerBl().convertUserToSponsor(sess, richUser, member))
               .collect(Collectors.toList());
  }

  @Override
  public User getUserByExtSourceNameAndExtLogin(PerunSession sess, String extSourceName, String extLogin)
      throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    User user = getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, extSourceName, extLogin);

    if (!AuthzResolver.authorizedInternal(sess, "getUserByExtSourceNameAndExtLogin_String_String_policy", user)) {
      throw new PrivilegeException(sess, "getUserByExtSourceNameAndExtLogin");
    }

    return user;
  }

  @Override
  public User getUserById(PerunSession sess, int id) throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    User user = getUsersManagerBl().getUserById(sess, id);

    if (!AuthzResolver.authorizedInternal(sess, "getUserById_int_policy", user)) {
      throw new PrivilegeException(sess, "getUserById");
    }

    return user;

  }

  @Override
  public User getUserByMember(PerunSession sess, Member member) throws MemberNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getUserByMember_Member_policy", member)) {
      throw new PrivilegeException(sess, "getUserByMember");
    }

    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    return getUsersManagerBl().getUserByMember(sess, member);
  }

  @Override
  public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

    User user = getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);

    if (!AuthzResolver.authorizedInternal(sess, "getUserByUserExtSource_UserExtSource_policy",
        Arrays.asList(userExtSource, user))) {
      throw new PrivilegeException(sess, "getUserByUserExtSource");
    }

    return user;
  }

  @Override
  public User getUserByUserExtSources(PerunSession sess, List<UserExtSource> userExtSources)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    User user = getUsersManagerBl().getUserByUserExtSources(sess, userExtSources);

    // Authorization
    for (UserExtSource ues : userExtSources) {
      if (!AuthzResolver.authorizedInternal(sess, "getUserByUserExtSources_List<UserExtSource>_policy", ues, user)) {
        throw new PrivilegeException(sess, "getUserByUserExtSources");
      }
    }

    return user;
  }

  @Override
  public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin)
      throws PrivilegeException, ExtSourceNotExistsException, UserExtSourceNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUserExtSourceByExtLogin_ExtSource_String_policy", source)) {
      throw new PrivilegeException(sess, "findUserExtSourceByExtLogin");
    }

    Utils.notNull(extLogin, "extLogin");
    getPerunBl().getExtSourcesManagerBl().checkExtSourceExists(sess, source);

    return getUsersManagerBl().getUserExtSourceByExtLogin(sess, source, extLogin);
  }

  @Override
  public UserExtSource getUserExtSourceById(PerunSession sess, int id)
      throws UserExtSourceNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUserExtSourceById_int_policy")) {
      throw new PrivilegeException(sess, "getUserExtSourceById");
    }

    return getUsersManagerBl().getUserExtSourceById(sess, id);
  }

  @Override
  public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue)
      throws AttributeNotExistsException, UserExtSourceNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUserExtSourceByUniqueAttributeValue_int_String_policy")) {
      throw new PrivilegeException(sess, "getUserExtSourceByUniqueAttributeValue");
    }

    return getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attrId, uniqueValue);
  }

  @Override
  public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, String attrName, String uniqueValue)
      throws AttributeNotExistsException, UserExtSourceNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUserExtSourceByUniqueAttributeValue_String_String_policy")) {
      throw new PrivilegeException(sess, "getUserExtSourceByUniqueAttributeValue");
    }

    return getUsersManagerBl().getUserExtSourceByUniqueAttributeValue(sess, attrName, uniqueValue);
  }

  @Override
  public List<UserExtSource> getUserExtSources(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUserExtSources_User_policy", user)) {
      throw new PrivilegeException(sess, "getUserExtSources");
    }

    getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().getUserExtSources(sess, user);
  }

  @Override
  public List<UserExtSource> getUserExtSourcesByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUserExtSourcesByIds_List<Integer>_policy")) {
      throw new PrivilegeException(sess, "getUserExtSourcesByIds");
    }

    return getUsersManagerBl().getUserExtSourcesByIds(sess, ids);
  }

  @Override
  public List<User> getUsers(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUsers_policy")) {
      throw new PrivilegeException(sess, "getUsers");
    }

    return getUsersManagerBl().getUsers(sess);
  }

  @Override
  public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUsersByAttribute_Attribute_policy")) {
      throw new PrivilegeException(sess, "getUsersByAttribute");
    }

    List<User> users = getUsersManagerBl().getUsersByAttribute(sess, attribute);
    users.removeIf(user -> !AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.READ, attribute, user, true) ||
                               !AuthzResolver.authorizedInternal(sess, "filter-getUsersByAttribute_Attribute_policy",
                                   user));

    return users;
  }

  @Override
  public List<User> getUsersByAttribute(PerunSession sess, String attributeName, String attributeValue)
      throws PrivilegeException, AttributeNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUsersByAttribute_String_String_policy")) {
      throw new PrivilegeException(sess, "getUsersByAttribute");
    }

    AttributeDefinition attribute = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
    List<User> users = getUsersManagerBl().getUsersByAttribute(sess, attributeName, attributeValue);
    users.removeIf(user -> !AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.READ, attribute, user, true) ||
                               !AuthzResolver.authorizedInternal(sess,
                                   "filter-getUsersByAttribute_String_String_policy", user));

    return users;
  }

  @Override
  public List<User> getUsersByAttributeValue(PerunSession sess, String attributeName, String attributeValue)
      throws PrivilegeException, AttributeNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUsersByAttributeValue_String_String_policy")) {
      throw new PrivilegeException(sess, "getUsersByAttributeValue");
    }

    AttributeDefinition attribute = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
    List<User> users = getUsersManagerBl().getUsersByAttributeValue(sess, attributeName, attributeValue);
    users.removeIf(user -> !AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.READ, attribute, user, true) ||
                               !AuthzResolver.authorizedInternal(sess,
                                   "filter-getUsersByAttributeValue_String_String_policy", user));

    return users;
  }

  @Override
  public List<User> getUsersByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUsersByIds_List<Integer>_policy")) {
      throw new PrivilegeException(sess, "getUsersByIds");
    }
    List<User> users = getUsersManagerBl().getUsersByIds(sess, ids);
    users.removeIf(user -> !AuthzResolver.authorizedInternal(sess, "filter-getUsersByIds_List<Integer>_policy", user));

    return users;
  }

  @Override
  public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser)
      throws UserNotExistsException, PrivilegeException, SpecificUserExpectedException {
    Utils.checkPerunSession(sess);
    getUsersManagerBl().checkUserExists(sess, specificUser);
    if (!specificUser.isSpecificUser()) {
      throw new SpecificUserExpectedException(specificUser);
    }

    List<Vo> vos = getUsersManagerBl().getVosWhereUserIsMember(sess, specificUser);
    for (Vo vo : vos) {
      if (!AuthzResolver.authorizedInternal(sess, "getUsersBySpecificUser_User_policy",
          Arrays.asList(specificUser, vo))) {
        throw new PrivilegeException(sess, "getUsersBySpecificUser");
      }
    }
    return getUsersManagerBl().getUsersBySpecificUser(sess, specificUser);
  }

  @Override
  public List<User> getUnanonymizedUsersBySpecificUser(PerunSession sess, User specificUser)
      throws UserNotExistsException, PrivilegeException, SpecificUserExpectedException {
    Utils.checkPerunSession(sess);
    getUsersManagerBl().checkUserExists(sess, specificUser);
    if (!specificUser.isSpecificUser()) {
      throw new SpecificUserExpectedException(specificUser);
    }

    List<Vo> vos = getUsersManagerBl().getVosWhereUserIsMember(sess, specificUser);
    for (Vo vo : vos) {
      if (!AuthzResolver.authorizedInternal(sess, "getUsersBySpecificUser_User_policy",
          Arrays.asList(specificUser, vo))) {
        throw new PrivilegeException(sess, "getUsersBySpecificUser");
      }
    }
    return getUsersManagerBl().getUnanonymizedUsersBySpecificUser(sess, specificUser);
  }

  @Override
  public int getUsersCount(PerunSession sess) {
    Utils.checkPerunSession(sess);

    return getUsersManagerBl().getUsersCount(sess);
  }

  /**
   * Gets the usersManagerBl for this instance.
   *
   * @return The usersManagerBl.
   */
  public UsersManagerBl getUsersManagerBl() {
    return this.usersManagerBl;
  }

  /*FIXME delete this method */
  public UsersManagerImplApi getUsersManagerImpl() {
    throw new InternalErrorException("Unsupported method!");
  }

  @Override
  public Paginated<RichUser> getUsersPage(PerunSession sess, UsersPageQuery query, List<String> attrNames)
      throws PrivilegeException, ResourceNotExistsException, VoNotExistsException, FacilityNotExistsException,
                 ServiceNotExistsException {
    Utils.checkPerunSession(sess);

    List<PerunBean> beans = createListOfBeans(sess, query);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUsersPage_UsersPageQuery_List<String>_policy") &&
            ((query.getResourceId() == null) ||
                 !AuthzResolver.authorizedInternal(sess, "resource-getUsersPage_UsersPageQuery_List<String>_policy",
                     perunBl.getResourcesManagerBl().getResourceById(sess, query.getResourceId()))) &&
            ((beans.isEmpty()) ||
                 !AuthzResolver.authorizedInternal(sess, "facility-getUsersPage_UsersPageQuery_List<String>_policy",
                     beans))) {
      throw new PrivilegeException(sess, "getUsersPage");
    }

    Paginated<RichUser> result = usersManagerBl.getUsersPage(sess, query, attrNames);
    result.setData(getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, result.getData()));

    return result;
  }

  @Override
  public List<User> getUsersWithoutSpecificVo(PerunSession sess, Vo vo, String searchString)
      throws VoNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getUsersWithoutSpecificVo_Vo_String_policy", vo)) {
      throw new PrivilegeException(sess, "findUsersByName");
    }

    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
    return getUsersManagerBl().getUsersWithoutSpecificVo(sess, vo, searchString);
  }

  @Override
  public List<User> getUsersWithoutVoAssigned(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getUsersWithoutVoAssigned_policy")) {
      throw new PrivilegeException(sess, "getUsersWithoutVoAssigned");
    }

    return getUsersManagerBl().getUsersWithoutVoAssigned(sess);
  }

  @Override
  public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getVosWhereUserIsAdmin_User_policy", user)) {
      throw new PrivilegeException(sess, "getVosWhereUserIsAdmin");
    }

    getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().getVosWhereUserIsAdmin(sess, user);
  }

  @Override
  public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getVosWhereUserIsMember_User_policy", user)) {
      throw new PrivilegeException(sess, "getVosWhereUserIsMember");
    }

    getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().getVosWhereUserIsMember(sess, user);
  }

  @Override
  public boolean isLoginAvailable(PerunSession sess, String loginNamespace, String login) throws InvalidLoginException {
    Utils.checkPerunSession(sess);

    // Authorization - must be public since it's used to check anonymous users input on registration form

    return getUsersManagerBl().isLoginAvailable(sess, loginNamespace, login);
  }

  @Override
  public boolean isLoginBlocked(PerunSession sess, String login, boolean ignoreCase) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "isLoginBlocked_String_policy")) {
      throw new PrivilegeException(sess, "isLoginBlocked");
    }

    return getUsersManagerBl().isLoginBlocked(sess, login, ignoreCase);
  }

  @Override
  public boolean isLoginBlockedForNamespace(PerunSession sess, String login, String namespace, boolean ignoreCase)
      throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "isLoginBlockedForNamespace_String_String_policy")) {
      throw new PrivilegeException(sess, "isLoginBlockedForNamespace");
    }

    return getUsersManagerBl().isLoginBlockedForNamespace(sess, login, namespace, ignoreCase);
  }

  @Override
  public boolean isLoginBlockedGlobally(PerunSession sess, String login) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "isLoginBlockedGlobally_String_policy")) {
      throw new PrivilegeException(sess, "isLoginBlockedGlobally");
    }

    return getUsersManagerBl().isLoginBlockedGlobally(sess, login);

  }

  @Override
  @Deprecated
  public boolean isUserPerunAdmin(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);

    getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "isUserPerunAdmin_User_policy", user)) {
      throw new PrivilegeException(sess, "isUserPerunAdmin");
    }

    return getUsersManagerBl().isUserPerunAdmin(sess, user);
  }

  @Override
  public boolean loginExist(PerunSession sess, User user, String loginNamespace)
      throws PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    boolean authorized;
    if (!user.isServiceUser()) {
      authorized = AuthzResolver.authorizedInternal(sess, "loginExist_User_String_policy", user);
    } else {
      authorized = AuthzResolver.authorizedInternal(sess, "service_user-loginExist_User_String_policy", user);
    }
    if (!authorized) {
      throw new PrivilegeException(sess, "loginExist");
    }

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().loginExist(sess, user, loginNamespace);
  }

  @Override
  public void moveUserExtSource(PerunSession sess, User sourceUser, User targetUser, UserExtSource userExtSource)
      throws UserExtSourceNotExistsException, UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getUsersManagerBl().checkUserExists(sess, targetUser);
    getUsersManagerBl().checkUserExists(sess, sourceUser);
    // set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
    userExtSource.setUserId(sourceUser.getId());
    getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "moveUserExtSource_User_User_UserExtSource_policy", sourceUser,
        userExtSource) ||
            !AuthzResolver.authorizedInternal(sess, "moveUserExtSource_User_User_UserExtSource_policy", targetUser)) {
      throw new PrivilegeException(sess, "moveUserExtSource");
    }

    if (userExtSource.isPersistent()) {
      throw new InternalErrorException("Given UserExtSource: " + userExtSource + " is marked as persistent. " +
                                           "It means it can not be removed.");
    }

    getUsersManagerBl().moveUserExtSource(sess, sourceUser, targetUser, userExtSource);
  }

  @Override
  public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser)
      throws UserNotExistsException, PrivilegeException, SpecificUserExpectedException,
                 NotSpecificUserExpectedException, RelationNotExistsException,
                 SpecificUserOwnerAlreadyRemovedException {
    Utils.checkPerunSession(sess);
    getUsersManagerBl().checkUserExists(sess, user);
    getUsersManagerBl().checkUserExists(sess, specificUser);
    if (user.isServiceUser()) {
      throw new NotSpecificUserExpectedException(user);
    }
    if (user.isSponsoredUser() && specificUser.isSponsoredUser()) {
      throw new NotSpecificUserExpectedException(specificUser);
    }
    if (!specificUser.isSpecificUser()) {
      throw new SpecificUserExpectedException(specificUser);
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeSpecificUserOwner_User_User_policy", specificUser) &&
            !AuthzResolver.authorizedInternal(sess, "owner-removeSpecificUserOwner_User_User_policy", user)) {
      throw new PrivilegeException(sess, "removeSpecificUserOwner");
    }
    getUsersManagerBl().removeSpecificUserOwner(sess, user, specificUser);
  }

  @Override
  public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException,
                 UserExtSourceAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeUserExtSource_User_UserExtSource_policy",
        Arrays.asList(user, userExtSource))) {
      throw new PrivilegeException(sess, "removeUserExtSource");
    }

    getUsersManagerBl().checkUserExists(sess, user);
    // set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
    userExtSource.setUserId(user.getId());
    getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

    if (userExtSource.isPersistent()) {
      throw new InternalErrorException("Given UserExtSource: " + userExtSource + " is marked as persistent. " +
                                           "It means it can not be removed.");
    }

    List<Attribute> attrs = perunBl.getAttributesManagerBl().getAttributes(sess, userExtSource);

    getUsersManagerBl().removeUserExtSource(sess, user, userExtSource);

    if (BeansUtils.getCoreConfig().isSendIdentityAlerts() &&
            user.getId() == sess.getPerunPrincipal().getUserId()) {
      try {
        Utils.sendIdentityRemovedAlerts(sess, userExtSource, attrs);
      } catch (Exception e) {
        LOG.error("Failed to send identity removed alerts.", e);
      }
    }
  }

  @Override
  public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource, boolean forceDelete)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException,
                 UserExtSourceAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    if (forceDelete) {

      // Authorization
      if (!AuthzResolver.authorizedInternal(sess, "removeUserExtSource_User_UserExtSource_boolean_policy",
          Arrays.asList(user, userExtSource))) {
        throw new PrivilegeException(sess, "removeUserExtSource");
      }

      getUsersManagerBl().checkUserExists(sess, user);
      // set userId, so checkUserExtSourceExists can check the userExtSource for the particular user
      userExtSource.setUserId(user.getId());
      getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

      getUsersManagerBl().removeUserExtSource(sess, user, userExtSource);
    } else {
      removeUserExtSource(sess, user, userExtSource);
    }
  }

  @Override
  public void removeUserExtSources(PerunSession sess, User user, List<UserExtSource> userExtSources,
                                   boolean forceDelete)
      throws UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException,
                 UserExtSourceAlreadyRemovedException {
    for (UserExtSource src : userExtSources) {
      removeUserExtSource(sess, user, src, forceDelete);
    }
  }

  @Override
  public void requestPreferredEmailChange(PerunSession sess, String url, User user, String email, String lang,
                                          String path, String idp) throws PrivilegeException, UserNotExistsException {

    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "requestPreferredEmailChange_String_User_String_String_policy", user)) {
      throw new PrivilegeException(sess, "requestPreferredEmailChange");
    }

    getPerunBl().getUsersManagerBl().requestPreferredEmailChange(sess, url, user, email, lang, path, idp);

  }

  @Override
  public void reservePassword(PerunSession sess, String userLogin, String loginNamespace, String password) throws
      PrivilegeException, PasswordCreationFailedException, PasswordOperationTimeoutException,
          PasswordStrengthFailedException, InvalidLoginException, PasswordStrengthException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "reservePassword_String_String_String_policy")) {
      throw new PrivilegeException(sess, "reservePassword");
    }

    // Check if the login is already occupied == reserved, if not throw an exception.
    // We cannot set password for the users who have not reserved login in perun DB and in registrar DB as well.
    if (!getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
      getUsersManagerBl().reservePassword(sess, userLogin, loginNamespace, password);
    } else {
      throw new PasswordCreationFailedException(
          "Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
    }
  }

  @Override
  public void reservePassword(PerunSession sess, User user, String loginNamespace, String password) throws
      PrivilegeException, PasswordCreationFailedException, UserNotExistsException, LoginNotExistsException,
          PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException,
          PasswordStrengthException {
    Utils.checkPerunSession(sess);

    // Authorization
    boolean authorized;
    if (!user.isServiceUser()) {
      authorized = AuthzResolver.authorizedInternal(sess, "reservePassword_User_String_String_policy", user);
    } else {
      authorized =
          AuthzResolver.authorizedInternal(sess, "service_user-reservePassword_User_String_String_policy", user);
    }
    if (!authorized) {
      throw new PrivilegeException(sess, "reservePassword");
    }

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().reservePassword(sess, user, loginNamespace, password);
  }

  @Override
  public void reserveRandomPassword(PerunSession sess, User user, String loginNamespace)
      throws PasswordCreationFailedException, PrivilegeException, UserNotExistsException, LoginNotExistsException,
                 PasswordOperationTimeoutException, PasswordStrengthFailedException, InvalidLoginException {
    Utils.checkPerunSession(sess);

    // Authorization
    boolean authorized;
    if (!user.isServiceUser()) {
      authorized = AuthzResolver.authorizedInternal(sess, "reserveRandomPassword_User_String_policy", user);
    } else {
      authorized =
          AuthzResolver.authorizedInternal(sess, "service_user-reserveRandomPassword_User_String_policy", user);
    }
    if (!authorized) {
      throw new PrivilegeException(sess, "reserveRandomPassword");
    }

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
    getUsersManagerBl().reserveRandomPassword(sess, user, loginNamespace);
  }

  @Override
  public void setLogin(PerunSession sess, User user, String loginNamespace, String login)
      throws PrivilegeException, UserNotExistsException, LoginExistsException, InvalidLoginException {

    // checks
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "setLogin_User_String_String_policy", user) && !user.isSpecificUser()) {
      throw new PrivilegeException(sess, "setLogin");
    }

    if (getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, login)) {

      getPerunBl().getUsersManagerBl().setLogin(sess, user, loginNamespace, login);

    } else {
      throw new LoginExistsException("Login: " + login + " in namespace: " + loginNamespace + " is already in use.");
    }

  }

  /**
   * Sets the perunBl for this instance.
   *
   * @param perunBl The perunBl.
   */
  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  @Override
  public User setSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType, User owner)
      throws RelationExistsException, UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    Utils.notNull(specificUserType, "specificUserType");
    getPerunBl().getUsersManagerBl().checkUserExists(sess, owner);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, specificUser);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "setSpecificUser_User_SpecificUserType_User_policy", specificUser) &&
            !AuthzResolver.authorizedInternal(sess, "owner-setSpecificUser_User_SpecificUserType_User_policy", owner)) {
      throw new PrivilegeException(sess, "setSpecificUser");
    }

    //set specific user
    return getUsersManagerBl().setSpecificUser(sess, specificUser, specificUserType, owner);
  }

  /**
   * Sets the usersManagerBl for this instance.
   *
   * @param usersManagerBl The usersManagerBl.
   */
  public void setUsersManagerBl(UsersManagerBl usersManagerBl) {
    this.usersManagerBl = usersManagerBl;
  }

  @Override
  public void unblockLogins(PerunSession sess, List<String> logins, String namespace)
      throws PrivilegeException, LoginIsNotBlockedException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "unblockLogins_List<String>_String_policy")) {
      throw new PrivilegeException(sess, "unblockLogins");
    }

    getUsersManagerBl().unblockLogins(sess, logins, namespace);
  }

  @Override
  public void unblockLoginsById(PerunSession sess, List<Integer> loginIds)
      throws PrivilegeException, LoginIsNotBlockedException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "unblockLoginsById_List<Integer>_policy")) {
      throw new PrivilegeException(sess, "unblockLoginsById");
    }

    getUsersManagerBl().unblockLoginsById(sess, loginIds);
  }

  @Override
  public User unsetSpecificUser(PerunSession sess, User specificUser, SpecificUserType specificUserType)
      throws UserNotExistsException, PrivilegeException, ServiceOnlyRoleAssignedException {
    Utils.checkPerunSession(sess);
    Utils.notNull(specificUserType, "specificUserType");
    getPerunBl().getUsersManagerBl().checkUserExists(sess, specificUser);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "unsetSpecificUser_User_SpecificUserType_policy", specificUser)) {
      throw new PrivilegeException(sess, "unsetSpecificUser");
    }

    //set specific user
    return getUsersManagerBl().unsetSpecificUser(sess, specificUser, specificUserType);
  }

  @Override
  public User updateNameTitles(PerunSession sess, User user) throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    Utils.checkMaxLength("TitleBefore", user.getTitleBefore(), 40);
    Utils.checkMaxLength("TitleAfter", user.getTitleAfter(), 40);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateNameTitles_User_policy", user)) {
      throw new PrivilegeException(sess, "updateNameTitles");
    }

    getUsersManagerBl().checkUserExists(sess, user);

    return getUsersManagerBl().updateNameTitles(sess, user);
  }

  @Override
  public User updateUser(PerunSession sess, User user) throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateUser_User_policy", user)) {
      throw new PrivilegeException(sess, "updateUser");
    }

    getUsersManagerBl().checkUserExists(sess, user);
    if (user.getLastName() == null || user.getLastName().isEmpty()) {
      throw new cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException(
          "User lastName can't be null. It's required attribute.");
    }

    return getUsersManagerBl().updateUser(sess, user);
  }

  @Override
  public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource)
      throws UserExtSourceNotExistsException, UserExtSourceExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateUserExtSource_UserExtSource_policy", userExtSource)) {
      throw new PrivilegeException(sess, "updateUserExtSource");
    }

    getUsersManagerBl().checkUserExtSourceExistsById(sess, userExtSource.getId());

    try {
      getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);
    } catch (UserExtSourceNotExistsException ex) {
      // silently skip, since it's expected, that new value is not taken already
    }

    return getUsersManagerBl().updateUserExtSource(sess, userExtSource);

  }

  @Override
  public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource)
      throws PrivilegeException, UserExtSourceNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExtSourceExists(sess, userExtSource);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateUserExtSourceLastAccess_UserExtSource_policy", userExtSource)) {
      throw new PrivilegeException(sess, "updateUserExtSourceLastAccess");
    }

    getUsersManagerBl().updateUserExtSourceLastAccess(sess, userExtSource);
  }

  @Override
  public void validatePassword(PerunSession sess, String userLogin, String loginNamespace) throws
      PrivilegeException, PasswordCreationFailedException, InvalidLoginException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "validatePassword_String_String_policy")) {
      throw new PrivilegeException(sess, "validatePassword");
    }

    // Check if the login is already occupied == reserved, if not throw an exception.
    // We cannot set password for the users who have not reserved login in perun DB and in registrar DB as well.
    if (!getPerunBl().getUsersManagerBl().isLoginAvailable(sess, loginNamespace, userLogin)) {
      getUsersManagerBl().validatePassword(sess, userLogin, loginNamespace);
    } else {
      throw new PasswordCreationFailedException(
          "Login " + userLogin + " in namespace " + loginNamespace + " is not reserved.");
    }
  }

  @Override
  public void validatePassword(PerunSession sess, User user, String loginNamespace) throws
      PrivilegeException, PasswordCreationFailedException, UserNotExistsException, LoginNotExistsException,
          InvalidLoginException {
    Utils.checkPerunSession(sess);

    // Authorization
    boolean authorized;
    if (!user.isServiceUser()) {
      authorized = AuthzResolver.authorizedInternal(sess, "validatePassword_User_String_policy", user);
    } else {
      authorized = AuthzResolver.authorizedInternal(sess, "service_user-validatePassword_User_String_policy", user);
    }
    if (!authorized) {
      throw new PrivilegeException(sess, "validatePassword");
    }

    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    getUsersManagerBl().validatePassword(sess, user, loginNamespace);
  }

  @Override
  public String validatePreferredEmailChange(PerunSession sess, User user, UUID token)
      throws PrivilegeException, UserNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException,
                 WrongReferenceAttributeValueException, WrongAttributeValueException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "validatePreferredEmailChange_User_String_policy", user)) {
      throw new PrivilegeException(sess, "validatePreferredEmailChange");
    }

    return getPerunBl().getUsersManagerBl().validatePreferredEmailChange(sess, user, token);
  }

  @Override
  public void validateSSHKey(PerunSession sess, String sshKey) throws SSHKeyNotValidException {
    Utils.checkPerunSession(sess);

    getPerunBl().getUsersManagerBl().validateSSHKey(sess, sshKey);
  }

  @Override
  public Map<String, List<PerunBean>> getUserRelations(PerunSession sess, User user) throws UserNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    Map<String, List<PerunBean>> result = new HashMap<>();

    List<Vo> vos = new ArrayList<>();
    List<Group> groups = new ArrayList<>();

    for (Vo vo : getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user)) {
      try {
        Member member = perunBl.getMembersManagerBl().getMemberByUser(sess, vo, user);
        if (AuthzResolver.authorizedInternal(sess, "filter-vo-getUserRelations_User_policy", member)) {
          vos.add(vo);
          List<Group> memberGroups = perunBl.getGroupsManagerBl().getMemberGroups(sess, member);
          memberGroups.removeIf(
                  group -> !AuthzResolver.authorizedInternal(sess, "filter-group-getUserRelations_User_policy",
                          Arrays.asList(group, member)));
          groups.addAll(memberGroups);
        }
      } catch (MemberNotExistsException ex) {
        throw new InternalErrorException(ex);
      }
    }

    result.put("vos", vos.stream().map(PerunBean.class::cast).toList());
    result.put("groups", groups.stream().map(PerunBean.class::cast).toList());

    return result;
  }

  @Override
  public void changeOrganization(PerunSession sess, User user, String newOrganizationName)
      throws UserNotExistsException, PrivilegeException, UserExtSourceNotExistsException,
                 PersonalDataChangeNotEnabledException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changeOrganization_User_String_policy", user)) {
      throw new PrivilegeException(sess, "changeOrganization");
    }

    getUsersManagerBl().changeOrganization(sess, user, newOrganizationName);
  }

  @Override
  public void changeOrganizationCustom(PerunSession sess, User user, String newOrganizationName)
      throws UserNotExistsException, PrivilegeException,
                 PersonalDataChangeNotEnabledException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    newOrganizationName = Utils.trimInput(newOrganizationName);

    if (newOrganizationName == null) {
      throw new IllegalArgumentException("Organization is required");
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changeOrganizationCustom_User_String_policy", user)) {
      throw new PrivilegeException(sess, "changeOrganizationCustom");
    }

    getUsersManagerBl().changeOrganizationCustom(sess, user, newOrganizationName);
  }

  @Override
  public void changeName(PerunSession sess, User user, String newUserName)
      throws UserNotExistsException, PrivilegeException, UserExtSourceNotExistsException,
                 PersonalDataChangeNotEnabledException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changeName_User_String_policy", user)) {
      throw new PrivilegeException(sess, "changeName");
    }

    getUsersManagerBl().changeName(sess, user, newUserName);
  }

  @Override
  public void changeNameCustom(PerunSession sess, User user, String titleBefore, String firstName, String middleName,
                               String lastName, String titleAfter)
      throws UserNotExistsException, PrivilegeException, PersonalDataChangeNotEnabledException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    titleBefore = Utils.trimInput(titleBefore);
    firstName = Utils.trimInput(firstName);
    middleName = Utils.trimInput(middleName);
    lastName = Utils.trimInput(lastName);
    titleAfter = Utils.trimInput(titleAfter);

    if (firstName == null || lastName == null) {
      throw new IllegalArgumentException("First name and last name are required");
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess,
        "changeNameCustom_User_String_String_String_String_String_policy", user)) {
      throw new PrivilegeException(sess, "changeNameCustom");
    }

    getUsersManagerBl().changeNameCustom(sess, user, titleBefore, firstName, middleName, lastName, titleAfter);

  }

  @Override
  public void changeEmail(PerunSession sess, User user, String newEmail)
      throws UserNotExistsException, PrivilegeException, UserExtSourceNotExistsException,
                 PersonalDataChangeNotEnabledException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changeEmail_User_String_policy", user)) {
      throw new PrivilegeException(sess, "changeEmail");
    }

    getUsersManagerBl().changeEmail(sess, user, newEmail);
  }

  @Override
  public void changeEmailCustom(PerunSession sess, User user, String newEmail, String url, String lang, String path,
                                String idp)
      throws UserNotExistsException, PrivilegeException, PersonalDataChangeNotEnabledException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    newEmail = Utils.trimInput(newEmail);

    if (newEmail == null) {
      throw new IllegalArgumentException("Email is required");
    }

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changeEmailCustom_User_String_String_String_String_String_policy",
        user)) {
      throw new PrivilegeException(sess, "changeEmailCustom");
    }

    getUsersManagerBl().changeEmailCustom(sess, user, newEmail, url, lang, path, idp);
  }
}
