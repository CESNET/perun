package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuadmPasswordManagerModule extends GenericPasswordManagerModule {

  private static final Logger LOG = LoggerFactory.getLogger(MuadmPasswordManagerModule.class);

  private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
  private static final Pattern LOWER_CASE_PATTERN = Pattern.compile(".*[a-z].*");
  private static final Pattern UPPER_CASE_PATTERN = Pattern.compile(".*[A-Z].*");
  private static final Pattern SPECIAL_CHAR_PATTERN =
      Pattern.compile(".*[\\x20-\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\x7E].*");

  public MuadmPasswordManagerModule() {
    this.actualLoginNamespace = "mu-adm";

    if (!BIN_TRUE.equals(this.passwordManagerProgram)) {
      passwordManagerProgram += "." + actualLoginNamespace;
    }
  }

  @Override
  public void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException {

    ((PerunBl) sess.getPerun()).getModulesUtilsBl()
        .checkLoginNamespaceRegex(actualLoginNamespace, login, GenericPasswordManagerModule.DEFAULT_LOGIN_PATTERN);

    if (!((PerunBl) sess.getPerun()).getModulesUtilsBl().isUserLoginPermitted(actualLoginNamespace, login)) {
      LOG.warn("Login '{}' is not allowed in {} namespace by configuration.", login, actualLoginNamespace);
      throw new InvalidLoginException("Login '" + login + "' is not allowed in 'mu' namespace by configuration.");
    }

  }

  @Override
  public void checkPasswordStrength(PerunSession sess, String login, String password) throws PasswordStrengthException {
    if (StringUtils.isBlank(password)) {
      LOG.warn("Password for {}:{} cannot be empty.", actualLoginNamespace, login);
      throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login + " cannot be empty.");
    }

    if (password.length() < 14) {
      LOG.warn("Password for {}:{} is too short. At least 14 characters are required.", actualLoginNamespace, login);
      throw new PasswordStrengthException(
          "Password for " + actualLoginNamespace + ":" + login + " is too short. At least 14 characters is required.");
    }

    if (login != null && login.length() > 2) {
      String backwardsLogin = StringUtils.reverse(login);
      if (password.toLowerCase().contains(login.toLowerCase()) ||
          password.toLowerCase().contains(backwardsLogin.toLowerCase())) {
        LOG.warn("Password for {}:{} must not match/contain login or backwards login.", actualLoginNamespace, login);
        throw new PasswordStrengthException(
            "Password for " + actualLoginNamespace + ":" + login + " must not match/contain login or backwards login.");
      }
    }

    int groupsCounter = 0;
    if (DIGIT_PATTERN.matcher(password).matches()) {
      groupsCounter++;
    }
    if (UPPER_CASE_PATTERN.matcher(password).matches()) {
      groupsCounter++;
    }
    if (LOWER_CASE_PATTERN.matcher(password).matches()) {
      groupsCounter++;
    }
    if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
      groupsCounter++;
    }

    if (groupsCounter < 3) {
      LOG.warn("Password for {}:{} is too weak. It has to contain character from at least 3 of these categories: " +
               "lower-case letter, upper-case letter, digit, spec. character.", actualLoginNamespace, login);
      throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + login +
                                          " is too weak. It has to contain character from at least 3 of these " +
                                          "categories: lower-case letter, " +
                                          "upper-case letter, digit, spec. character.");
    }

    super.checkPasswordStrength(sess, login, password);
  }
}
