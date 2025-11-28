package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Password manager for admin-meta login-namespace.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AdminmetaPasswordManagerModule extends GenericPasswordManagerModule {

  private static final Logger LOG = LoggerFactory.getLogger(AdminmetaPasswordManagerModule.class);

  protected final Pattern adminMetaLoginPattern = Pattern.compile("^[a-z][a-z0-9_-]{1,14}$");

  protected final Pattern adminMetaPasswordContainsDigit = Pattern.compile(".*[0-9].*");
  protected final Pattern adminMetaPasswordContainsLower = Pattern.compile(".*[a-z].*");
  protected final Pattern adminMetaPasswordContainsUpper = Pattern.compile(".*[A-Z].*");
  protected final Pattern adminMetaPasswordContainsSpec =
      Pattern.compile(".*[\\x20-\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\x7E].*");
  protected final int adminMetaPasswordMinLength = 12;

  public AdminmetaPasswordManagerModule() {

    // set proper namespace
    this.actualLoginNamespace = "admin-meta";

    this.randomPasswordLength = 12;
  }

  @Override
  public void changePassword(PerunSession sess, String userLogin, String newPassword)
      throws InvalidLoginException, PasswordStrengthException {
    throw new InternalErrorException("Changing password in login namespace 'admin-meta' is not supported.");
  }

  @Override
  public void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException {
    // check login syntax/format
    ((PerunBl) sess.getPerun()).getModulesUtilsBl()
        .checkLoginNamespaceRegex(actualLoginNamespace, login, adminMetaLoginPattern);

    // check if login is permitted
    if (!((PerunBl) sess.getPerun()).getModulesUtilsBl().isUserLoginPermitted(actualLoginNamespace, login)) {
      LOG.warn("Login '{}' is not allowed in {} namespace by configuration.", login, actualLoginNamespace);
      throw new InvalidLoginException(
          "Login '" + login + "' is not allowed in '" + actualLoginNamespace + "' namespace by configuration.");
    }
  }

  @Override
  public void checkPasswordStrength(PerunSession sess, String login, String password) throws PasswordStrengthException {

    if (StringUtils.isBlank(password)) {
      LOG.warn("Password for {}:{} cannot be empty.", actualLoginNamespace, login);
      throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " cannot be empty.");
    }

    if (password.length() < adminMetaPasswordMinLength) {
      LOG.warn("Password for {}:{} is too short. At least {} characters are required.", actualLoginNamespace, login,
          adminMetaPasswordMinLength);
      throw new PasswordStrengthException(
          "Password for " + actualLoginNamespace + ":" + login + " is too short. At least " +
          adminMetaPasswordMinLength + " characters are required.");
    }

    // if login is at least 3 chars, test if its not contained in password
    if (login != null && login.length() > 2) {
      String backwardsLogin = StringUtils.reverse(login);
      if (password.toLowerCase().contains(login.toLowerCase()) ||
          password.toLowerCase().contains(backwardsLogin.toLowerCase())) {
        LOG.warn("Password for {}:{} must not match/contain login or backwards login.", actualLoginNamespace, login);
        throw new PasswordStrengthException(
            "Password for " + actualLoginNamespace + ":" + login + " must not match/contain login or backwards login.");
      }
    }

    // TODO - fetch user and get names to make sure they are not part of password

    if (!StringUtils.isAsciiPrintable(password)) {
      LOG.warn("Password for {}:{} must contain only printable characters.", actualLoginNamespace, login);
      throw new PasswordStrengthException(
          "Password for " + actualLoginNamespace + ":" + login + " must contain only printable characters.");
    }

    // check that it contains at least 3 groups of 4
    int groupsCounter = 0;
    if (adminMetaPasswordContainsDigit.matcher(password).matches()) {
      groupsCounter++;
    }
    if (adminMetaPasswordContainsUpper.matcher(password).matches()) {
      groupsCounter++;
    }
    if (adminMetaPasswordContainsLower.matcher(password).matches()) {
      groupsCounter++;
    }
    if (adminMetaPasswordContainsSpec.matcher(password).matches()) {
      groupsCounter++;
    }

    if (groupsCounter < 3) {
      LOG.warn(
          "Password for {}:{} is too weak. It has to contain at least 3 kinds of characters from: lower-case letter," +
          " upper-case letter, digit, spec. character.", actualLoginNamespace, login);
      throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login +
                                          " is too weak. It has to contain at least 3 kinds of characters from: " +
                                          "lower-case letter, upper-case letter," +
                                          " digit, spec. character.");
    }

    super.checkPasswordStrength(sess, login, password);

  }

  @Override
  public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password)
      throws PasswordStrengthException {
    throw new InternalErrorException("Creating alternative password in login namespace 'admin-meta' is not supported.");
  }

  @Override
  public void deleteAlternativePassword(PerunSession sess, User user, String passwordId) {
    throw new InternalErrorException("Deleting alternative password in login namespace 'admin-meta' is not supported.");
  }

  @Override
  public Map<String, String> generateAccount(PerunSession session, Map<String, String> parameters) {
    throw new InternalErrorException("Generating account in login namespace 'admin-meta' meta not supported.");
  }

  @Override
  public void reserveRandomPassword(PerunSession session, String userLogin) {
    throw new InternalErrorException("Reserving random password in login namespace 'admin-meta' is not supported.");
  }

  @Override
  public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
    if (user == null) {
      user = ((PerunBl) sess.getPerun()).getModulesUtilsBl()
          .getUserByLoginInNamespace(sess, userLogin, actualLoginNamespace);
    }

    if (user == null) {
      LOG.warn("No user was found by login '{}' in {} namespace.", userLogin, actualLoginNamespace);
    } else {

      PerunBl perunBl = ((PerunBl) sess.getPerun());

      try {
        Attribute kerberosAdminAttr = perunBl.getAttributesManagerBl()
            .getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":kerberosAdminPrincipal");
        if (kerberosAdminAttr.getValue() != null) {
          LOG.error("User {} has attribute kerberosAdminPrincipal already filled while validating " +
                    "login '{}' in {} namespace", user, userLogin, actualLoginNamespace);
          throw new InternalErrorException("Attribute kerberosAdminPrincipal is already filled.");
        }

        kerberosAdminAttr.setValue(userLogin + "@ADMIN.META");
        perunBl.getAttributesManagerBl().setAttribute(sess, user, kerberosAdminAttr);
      } catch (WrongAttributeAssignmentException | AttributeNotExistsException | WrongAttributeValueException |
               WrongReferenceAttributeValueException ex) {
        throw new InternalErrorException(ex);
      }
    }

    // validate password
    super.validatePassword(sess, userLogin, user);
  }

}
