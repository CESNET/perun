package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.rt.LoginNotExistsRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordChangeFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordCreationFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDeletionFailedRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordDoesntMatchRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordOperationTimeoutRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.PasswordStrengthFailedRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic implementation of {@link PasswordManagerModule}. It runs generic password manger script defined as perun
 * config in {@link CoreConfig#getPasswordManagerProgram()} or
 * {@link CoreConfig#getAlternativePasswordManagerProgram()}.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class GenericPasswordManagerModule implements PasswordManagerModule {

  protected static final Pattern DEFAULT_LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_][-A-z0-9_.@/]*$");
  protected static final String PASSWORD_VALIDATE = "validate";
  protected static final String PASSWORD_CREATE = "create";
  protected static final String PASSWORD_RESERVE = "reserve";
  protected static final String PASSWORD_RESERVE_RANDOM = "reserve_random";
  protected static final String PASSWORD_CHANGE = "change";
  protected static final String PASSWORD_CHECK = "check";
  protected static final String PASSWORD_DELETE = "delete";
  protected static final String LOGIN_EXIST = "exist";
  protected static final String WEAKPASS = "weakpass";
  protected static final String BIN_TRUE = "/bin/true";
  private static final Logger LOG = LoggerFactory.getLogger(GenericPasswordManagerModule.class);
  private static final int MINIMUM_PASSWORD_LENGTH = 8;
  protected String actualLoginNamespace = "generic";
  protected String passwordManagerProgram = BeansUtils.getCoreConfig().getPasswordManagerProgram();
  protected String altPasswordManagerProgram = BeansUtils.getCoreConfig().getAlternativePasswordManagerProgram();
  protected int randomPasswordLength = 12;
  protected char[] randomPasswordCharacters =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()-_=+;:,<.>/?".toCharArray();

  @Override
  public void changePassword(PerunSession sess, String userLogin, String newPassword)
      throws InvalidLoginException, PasswordStrengthException {
    checkLoginFormat(sess, userLogin);
    checkPasswordStrength(sess, userLogin, newPassword);
    Process process = createPwdManagerProcess(PASSWORD_CHANGE, actualLoginNamespace, userLogin);
    sendPassword(process, newPassword);
    handleExit(process, actualLoginNamespace, userLogin);
  }

  @Override
  public void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException {

    // check login syntax/format
    ((PerunBl) sess.getPerun()).getModulesUtilsBl()
        .checkLoginNamespaceRegex(actualLoginNamespace, login, DEFAULT_LOGIN_PATTERN);

    // check if login is permitted
    if (!((PerunBl) sess.getPerun()).getModulesUtilsBl().isUserLoginPermitted(actualLoginNamespace, login)) {
      LOG.warn("Login '{}' is not allowed in {} namespace by configuration.", login, actualLoginNamespace);
      throw new InvalidLoginException(
          "Login '" + login + "' is not allowed in '" + actualLoginNamespace + "' namespace by configuration.");
    }

  }

  @Override
  public void checkPassword(PerunSession sess, String userLogin, String password) {
    // use custom check instead of checkPasswordStrength(), since this is only about empty input
    // and we must allow checks for older (weaker) passwords
    if (StringUtils.isBlank(password)) {
      throw new InternalErrorException("Password for " + actualLoginNamespace + ":" + userLogin + " cannot be empty.");
    }
    Process process = createPwdManagerProcess(PASSWORD_CHECK, actualLoginNamespace, userLogin);
    sendPassword(process, password);
    handleExit(process, actualLoginNamespace, userLogin);
  }

  @Override
  public void checkPasswordStrength(PerunSession sess, String login, String password) throws PasswordStrengthException {

    if (StringUtils.length(password) < MINIMUM_PASSWORD_LENGTH) {
      LOG.warn("Password for {}:{} must be at least {} characters long.", actualLoginNamespace, login,
          MINIMUM_PASSWORD_LENGTH);
      throw new PasswordStrengthException(
          "Password for " + actualLoginNamespace + ":" + login + " must be at least " + MINIMUM_PASSWORD_LENGTH +
          " characters long.");
    }

    String weakpassFilename = "";

    try {
      weakpassFilename = Utils.CONFIGURATIONS_LOCATIONS + WEAKPASS + "_" + actualLoginNamespace + ".txt";
      if (Utils.checkWordInSortedFile(weakpassFilename, password)) {
        throw new PasswordStrengthException(
            "Password for " + actualLoginNamespace + ":" + login + " is listed as a weak password.");
      }
    } catch (FileNotFoundException e) {
      weakpassFilename = Utils.CONFIGURATIONS_LOCATIONS + WEAKPASS + ".txt";
      try {
        if (Utils.checkWordInSortedFile(weakpassFilename, password)) {
          throw new PasswordStrengthException(
              "Password for " + actualLoginNamespace + ":" + login + " is listed as a weak password.");
        }
      } catch (FileNotFoundException ex) {
        LOG.warn("No default weak passwords file found in " + Utils.CONFIGURATIONS_LOCATIONS);
      } catch (IOException ex) {
        LOG.error("Error reading weak passwords file " + weakpassFilename + ":" + ex);
      }
    } catch (IOException e) {
      LOG.error("Error reading weak passwords file " + weakpassFilename + ":" + e);
    }

    // TODO - some more generic checks ???

  }

  protected Process createAltPwdManagerProcess(String operation, String loginNamespace, User user, String passwordId) {

    ProcessBuilder pb =
        new ProcessBuilder(altPasswordManagerProgram, operation, loginNamespace, Integer.toString(user.getId()),
            passwordId);

    Process process;
    try {
      process = pb.start();
    } catch (IOException e) {
      throw new InternalErrorException(e);
    }

    return process;

  }

  @Override
  public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password)
      throws PasswordStrengthException {
    checkPasswordStrength(sess, passwordId, password);
    Process process = createAltPwdManagerProcess(PASSWORD_CREATE, actualLoginNamespace, user, passwordId);
    sendPassword(process, password);
    handleAltPwdManagerExit(process, user, actualLoginNamespace, passwordId);
  }

  /**
   * Run password manager script on path defined in perun config.
   *
   * @param operation      Operation to perform (reserve, reserveRandom, validate, check, change, delete)
   * @param loginNamespace Namespace in which operation is performed.
   * @param login          Login to perform operation for
   * @return Started process
   */
  protected Process createPwdManagerProcess(String operation, String loginNamespace, String login) {

    ProcessBuilder pb = new ProcessBuilder(passwordManagerProgram, operation, loginNamespace, login);

    Process process;
    try {
      process = pb.start();
    } catch (IOException e) {
      throw new InternalErrorException(e);
    }

    return process;

  }

  @Override
  public void deleteAlternativePassword(PerunSession sess, User user, String passwordId) {
    Process process = createAltPwdManagerProcess(PASSWORD_DELETE, actualLoginNamespace, user, passwordId);
    handleAltPwdManagerExit(process, user, actualLoginNamespace, passwordId);
  }

  @Override
  public void deletePassword(PerunSession sess, String userLogin) throws InvalidLoginException {
    checkLoginFormat(sess, userLogin);
    Process process = createPwdManagerProcess(PASSWORD_DELETE, actualLoginNamespace, userLogin);
    handleExit(process, actualLoginNamespace, userLogin);
  }

  @Override
  public Map<String, String> generateAccount(PerunSession sess, Map<String, String> parameters) {
    // account generation is not supported
    return null;
  }

  @Override
  public String generateRandomPassword(PerunSession sess, String login) {

    String randomPassword = null;
    boolean strengthOk = false;
    while (!strengthOk) {

      randomPassword =
          RandomStringUtils.random(randomPasswordLength, 0, randomPasswordCharacters.length - 1, false, false,
              randomPasswordCharacters, new SecureRandom());

      try {
        checkPasswordStrength(sess, login, randomPassword);
        strengthOk = true;
      } catch (PasswordStrengthException ex) {
        strengthOk = false;
      }
    }

    return randomPassword;

  }

  public String getActualLoginNamespace() {
    return actualLoginNamespace;
  }

  /**
   * Wait for alternative password manager script to end and handle known return codes.
   *
   * @param process        Running password manager script process.
   * @param user           User for which operation was performed.
   * @param loginNamespace Namespace in which operation was performed.
   * @param passwordId     ID of alt password entry for which it was performed.
   */
  protected void handleAltPwdManagerExit(Process process, User user, String loginNamespace, String passwordId) {

    InputStream es = process.getErrorStream();

    // If non-zero exit code is returned, then try to read error output
    try {
      if (process.waitFor() != 0) {
        if (process.exitValue() == 1) {
          //throw new PasswordDoesntMatchRuntimeException("Old password doesn't match for " + loginNamespace + ":" +
          // userLogin + ".");
          throw new InternalErrorException(
              "Alternative password manager returns unexpected return code: " + process.exitValue());
        } else if (process.exitValue() == 3) {
          //throw new PasswordChangeFailedRuntimeException("Password change failed for " + loginNamespace + ":" +
          // userLogin + ".");
          throw new InternalErrorException(
              "Alternative password manager returns unexpected return code: " + process.exitValue());
        } else if (process.exitValue() == 4) {
          throw new PasswordCreationFailedRuntimeException(
              "Alternative password creation failed for " + user + ". Namespace: " + loginNamespace + ", passwordId: " +
              passwordId + ".");
        } else if (process.exitValue() == 5) {
          throw new PasswordDeletionFailedRuntimeException(
              "Password deletion failed for " + user + ". Namespace: " + loginNamespace + ", passwordId: " +
              passwordId + ".");
        } else if (process.exitValue() == 6) {
          throw new LoginNotExistsRuntimeException(
              "User doesn't exists in underlying system for namespace " + loginNamespace + ", user: " + user + ".");
        } else if (process.exitValue() == 7) {
          throw new InternalErrorException(
              "Problem with creating user entry in underlying system " + loginNamespace + ", user: " + user + ".");
        } else {
          handleGenericErrorCode(es);
        }
      }
    } catch (InterruptedException e) {
      throw new InternalErrorException(e);
    }

  }

  /**
   * Wait for password manager script to end and handle known return codes.
   *
   * @param process        Running password manager script process.
   * @param loginNamespace Namespace in which operation was performed.
   * @param userLogin      Login for which operation was performed.
   */
  protected void handleExit(Process process, String loginNamespace, String userLogin) {

    InputStream es = process.getErrorStream();

    // If non-zero exit code is returned, then try to read error output
    try {
      if (process.waitFor() != 0) {
        if (process.exitValue() == 1) {
          throw new PasswordDoesntMatchRuntimeException(
              "Old password doesn't match for " + loginNamespace + ":" + userLogin + ".");
        } else if (process.exitValue() == 3) {
          throw new PasswordChangeFailedRuntimeException(
              "Password change failed for " + loginNamespace + ":" + userLogin + ".");
        } else if (process.exitValue() == 4) {
          throw new PasswordCreationFailedRuntimeException(
              "Password creation failed for " + loginNamespace + ":" + userLogin + ".");
        } else if (process.exitValue() == 5) {
          throw new PasswordDeletionFailedRuntimeException(
              "Password deletion failed for " + loginNamespace + ":" + userLogin + ".");
        } else if (process.exitValue() == 6) {
          throw new LoginNotExistsRuntimeException(
              "User login doesn't exists in underlying system for " + loginNamespace + ":" + userLogin + ".");
        } else if (process.exitValue() == 11) {
          throw new PasswordStrengthFailedRuntimeException(
              "Password to set doesn't match expected restrictions for " + loginNamespace + ":" + userLogin + ".");
        } else if (process.exitValue() == 12) {
          throw new PasswordOperationTimeoutRuntimeException(
              "Operation with password exceeded expected limit for " + loginNamespace + ":" + userLogin + ".");
        } else {
          handleGenericErrorCode(es);
        }
      }
    } catch (InterruptedException e) {
      throw new InternalErrorException(e);
    }

  }

  /**
   * Handle error stream from password manager script on unexpected return code.
   *
   * @param errorStream Password manager script error stream
   */
  protected void handleGenericErrorCode(InputStream errorStream) {

    BufferedReader inReader = new BufferedReader(new InputStreamReader(errorStream));
    StringBuilder errorMsg = new StringBuilder();
    String line;
    try {
      while ((line = inReader.readLine()) != null) {
        errorMsg.append(line);
      }
    } catch (IOException e) {
      throw new InternalErrorException(e);
    }

    throw new InternalErrorException(errorMsg.toString());

  }

  @Override
  public boolean loginExist(PerunSession sess, String login) {
    Process process = createPwdManagerProcess(LOGIN_EXIST, actualLoginNamespace, login);
    try {
      handleExit(process, actualLoginNamespace, login);
    } catch (LoginNotExistsRuntimeException e) {
      return false;
    }
    return true;
  }

  @Override
  public void reservePassword(PerunSession sess, String userLogin, String password)
      throws InvalidLoginException, PasswordStrengthException {
    checkLoginFormat(sess, userLogin);
    checkPasswordStrength(sess, userLogin, password);
    Process process = createPwdManagerProcess(PASSWORD_RESERVE, actualLoginNamespace, userLogin);
    sendPassword(process, password);
    handleExit(process, actualLoginNamespace, userLogin);
  }

  @Override
  public void reserveRandomPassword(PerunSession sess, String userLogin) throws InvalidLoginException {
    checkLoginFormat(sess, userLogin);
    Process process = createPwdManagerProcess(PASSWORD_RESERVE_RANDOM, actualLoginNamespace, userLogin);
    handleExit(process, actualLoginNamespace, userLogin);
  }

  /**
   * Send password to the STDIN of running password manager script process.
   *
   * @param process  process waiting for password on STDIN
   * @param password password to be set
   */
  protected void sendPassword(Process process, String password) {

    OutputStream os = process.getOutputStream();
    // Write password to the stdin of the program
    PrintWriter pw = new PrintWriter(os, true);
    pw.write(password);
    pw.close();

  }

  public void setActualLoginNamespace(String actualLoginNamespace) {
    this.actualLoginNamespace = actualLoginNamespace;
  }

  @Override
  public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
    checkLoginFormat(sess, userLogin);
    Process process = createPwdManagerProcess(PASSWORD_VALIDATE, actualLoginNamespace, userLogin);
    handleExit(process, actualLoginNamespace, userLogin);
  }

}
