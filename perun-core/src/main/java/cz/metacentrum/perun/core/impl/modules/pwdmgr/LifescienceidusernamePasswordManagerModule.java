package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import com.google.common.collect.Lists;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Password manager for lifescienceid-username.
 */
public class LifescienceidusernamePasswordManagerModule extends GenericPasswordManagerModule {

  private static final Logger LOG = LoggerFactory.getLogger(LifescienceidusernamePasswordManagerModule.class);

  private static final String VO_NAME = "lifescience_hostel";
  private static final String LS_DOMAIN = "@hostel.aai.lifescience-ri.eu";
  private static final String EXT_SOURCE_NAME = "https://hostel.aai.lifescience-ri.eu/lshostel/";
  private static final String REGISTRAR = "perunRegistrar";
  private final Pattern passRegex = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#\\^@$!%\\*?&\\.\\" +
                                           "(\\)\\[\\]{}:]())[A-Za-z\\d#\\^@$!%\\*?&\\.\\(\\)\\[\\]{}:]{8,}$");

  public LifescienceidusernamePasswordManagerModule() {
    // set proper namespace
    this.actualLoginNamespace = "lifescienceid-username";
  }

  private boolean addUserToVo(PerunSession sess, String userLogin, User user) {
    if (user == null) {
      LOG.warn("No user was found by login '{}' in {} namespace.", userLogin, actualLoginNamespace);
      return false;
    }

    try {
      ExtSource extSource =
          ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
      UserExtSource ues;
      try {
        // get userExtSource
        ues = ((PerunBl) sess.getPerun()).getUsersManagerBl()
            .getUserExtSourceByExtLogin(sess, extSource, userLogin + LS_DOMAIN);
      } catch (UserExtSourceNotExistsException ex) {
        // ues do not exist yet, so we need to create it
        ues = new UserExtSource(extSource, userLogin + LS_DOMAIN);
        ues.setLoa(0);
        ((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
      }

      // set additional identifiers
      Attribute additionalIdentifiers = ((PerunBl) sess.getPerun()).getAttributesManagerBl()
          .getAttribute(sess, ues, UsersManagerBl.ADDITIONAL_IDENTIFIERS_PERUN_ATTRIBUTE_NAME);
      String newIdentifier = userLogin + LS_DOMAIN;
      if (additionalIdentifiers.valueAsList() == null || additionalIdentifiers.valueAsList().isEmpty()) {
        additionalIdentifiers.setValue(Lists.newArrayList(newIdentifier));
      } else if (!additionalIdentifiers.valueContains(newIdentifier)) {
        additionalIdentifiers.setValue(additionalIdentifiers.valueAsList().add(newIdentifier));
      }
      ((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttribute(sess, ues, additionalIdentifiers);

      // add user to specific vo
      Vo targetVo = ((PerunBl) sess.getPerun()).getVosManagerBl().getVoByShortName(sess, VO_NAME);
      Member member = ((PerunBl) sess.getPerun()).getMembersManagerBl().createMember(sess, targetVo, user);
      ((PerunBl) sess.getPerun()).getMembersManagerBl().validateMemberAsync(sess, member);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException | ExtSourceNotExistsException |
             WrongAttributeValueException | WrongReferenceAttributeValueException | VoNotExistsException |
             ExtendMembershipException | UserExtSourceExistsException ex) {
      throw new InternalErrorException(ex);
    } catch (AlreadyMemberException ignored) {
      // ignore
    }
    return true;
  }

  @Override
  public void changePassword(PerunSession sess, String userLogin, String newPassword)
      throws InvalidLoginException, PasswordStrengthException {
    User user = ((PerunBl) sess.getPerun()).getModulesUtilsBl()
        .getUserByLoginInNamespace(sess, userLogin, actualLoginNamespace);

    if (!addUserToVo(sess, userLogin, user)) {
      return;
    }

    // change password
    super.changePassword(sess, userLogin, newPassword);
  }

  @Override
  public void checkPasswordStrength(PerunSession sess, String userLogin, String newPassword)
      throws PasswordStrengthException {
    if (!passRegex.matcher(newPassword).matches()) {
      LOG.warn("Password for {}:{} is too weak. Your password needs to be at least eight characters long." +
                   " It has to contain at least one uppercase and lowercase letter," +
                   " one digit, and one special character like #^@$!%*?&.()[]\\{}:.", actualLoginNamespace, userLogin);
      throw new PasswordStrengthException("Password for " + actualLoginNamespace + ":" + userLogin + " is too weak. " +
                                              "Your password needs to be at least eight characters long." +
                                              " It has to contain at least one uppercase and lowercase letter," +
                                              " one digit, and one special character like #^@$!%*?&.()[]{}:.");
    }

    super.checkPasswordStrength(sess, userLogin, newPassword);
  }

  @Override
  public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
    // This block of code is intended for manual setup of local accounts. Not for registrations.
    if (!sess.getPerunPrincipal().getActor().equals(REGISTRAR)) {
      if (user == null) {
        user = ((PerunBl) sess.getPerun()).getModulesUtilsBl()
            .getUserByLoginInNamespace(sess, userLogin, actualLoginNamespace);
      }

      if (!addUserToVo(sess, userLogin, user)) {
        return;
      }
    }

    // validate password
    super.validatePassword(sess, userLogin, user);
  }

}
