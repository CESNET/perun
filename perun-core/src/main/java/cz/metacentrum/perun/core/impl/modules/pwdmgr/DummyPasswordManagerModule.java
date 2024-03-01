package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SponsoredUserData;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy password module for debugging.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class DummyPasswordManagerModule implements PasswordManagerModule {

  private final static Logger log = LoggerFactory.getLogger(DummyPasswordManagerModule.class);

  private static final Random RANDOM = new Random();

  @Override
  public String handleSponsorship(PerunSession sess, SponsoredUserData userData) throws PasswordStrengthException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PasswordManagerModule.TITLE_BEFORE_KEY, userData.getTitleBefore());
    parameters.put(PasswordManagerModule.FIRST_NAME_KEY, userData.getFirstName());
    parameters.put(PasswordManagerModule.LAST_NAME_KEY, userData.getLastName());
    parameters.put(PasswordManagerModule.TITLE_AFTER_KEY, userData.getTitleAfter());
    if (userData.getPassword() != null) {
      parameters.put(PasswordManagerModule.PASSWORD_KEY, userData.getPassword());
    }
    return generateAccount(sess, parameters).get(PasswordManagerModule.LOGIN_PREFIX + "dummy");
  }

  @Override
  public Map<String, String> generateAccount(PerunSession sess, Map<String, String> parameters) {
    log.debug("generateAccount(parameters={})", parameters);
    Map<String, String> result = new HashMap<>();
    result.put(LOGIN_PREFIX + "dummy", Integer.toString(9000000 + RANDOM.nextInt(1000000)));
    return result;
  }

  @Override
  public void reservePassword(PerunSession sess, String userLogin, String password) {
    log.debug("reservePassword(userLogin={})", userLogin);
  }

  @Override
  public void reserveRandomPassword(PerunSession sess, String userLogin) {
    log.debug("reserveRandomPassword(userLogin={})", userLogin);
  }

  @Override
  public void checkPassword(PerunSession sess, String userLogin, String password) {
    log.debug("checkPassword(userLogin={})", userLogin);
  }

  @Override
  public void changePassword(PerunSession sess, String userLogin, String newPassword) {
    log.debug("changePassword(userLogin={})", userLogin);
  }

  @Override
  public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
    log.debug("validatePassword(userLogin={})", userLogin);

    if (user == null) {
      user = ((PerunBl) sess.getPerun()).getModulesUtilsBl().getUserByLoginInNamespace(sess, userLogin, "dummy");
    }

    if (user == null) {
      log.warn("No user was found by login '{}' in {} namespace.", userLogin, "dummy");
    } else {
      // set extSources and extSource related attributes
      ExtSource extSource;
      try {
        extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, "https://dummy");
      } catch (ExtSourceNotExistsException e) {
        extSource = new ExtSource("https://dummy", ExtSourcesManager.EXTSOURCE_IDP);
        try {
          extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().createExtSource(sess, extSource, null);
        } catch (ExtSourceExistsException e1) {
          log.warn("impossible or race condition", e1);
        }
      }
      UserExtSource ues = new UserExtSource(extSource, userLogin + "@dummy");
      ues.setLoa(2);
      try {
        ((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
      } catch (UserExtSourceExistsException ex) {
        //this is OK
      }
    }
  }

  @Override
  public void deletePassword(PerunSession sess, String userLogin) {
    log.debug("deletePassword(userLogin={})", userLogin);
  }

  @Override
  public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password) {
    log.debug("createAlternativePassword(user={},passwordId={})", user, passwordId);
  }

  @Override
  public void deleteAlternativePassword(PerunSession sess, User user, String passwordId) {
    log.debug("deleteAlternativePassword(user={},passwordId={})", user, passwordId);
  }

  @Override
  public void checkLoginFormat(PerunSession sess, String login) {
    log.debug("checkLoginFormat(userLogin={})", login);
  }

  @Override
  public void checkPasswordStrength(PerunSession sess, String login, String password) {
    log.debug("checkPasswordStrength(userLogin={})", login);
  }

  @Override
  public String generateRandomPassword(PerunSession sess, String login) {
    log.debug("generateRandomPassword(userLogin={})", login);
    return "randomPassword" + RANDOM.nextInt(10);
  }

}
