package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Password manager for EGI-UI login-namespace.
 */
public class EgiuiPasswordManagerModule extends GenericPasswordManagerModule {

  private final static Logger log = LoggerFactory.getLogger(EinfraPasswordManagerModule.class);

  public EgiuiPasswordManagerModule() {
    // set proper namespace
    this.actualLoginNamespace = "egi-ui";
  }

  @Override
  public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
    if (user == null) {
      user = ((PerunBl) sess.getPerun()).getModulesUtilsBl()
          .getUserByLoginInNamespace(sess, userLogin, actualLoginNamespace);
    }

    if (user == null) {
      log.warn("No user was found by login '{}' in {} namespace.", userLogin, actualLoginNamespace);
    } else {
      // set extSources and extSource related attributes
      try {
        List<String> kerberosLogins = new ArrayList<>();

        ExtSource extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, "EGI");
        UserExtSource ues = new UserExtSource(extSource, userLogin + "@EGI");
        ues.setLoa(0);

        try {
          ((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
        } catch (UserExtSourceExistsException ex) {
          //this is OK
        }

        // Store also Kerberos logins
        Attribute kerberosLoginsAttr = ((PerunBl) sess.getPerun()).getAttributesManagerBl()
            .getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":" + "kerberosLogins");
        if (kerberosLoginsAttr != null && kerberosLoginsAttr.getValue() != null) {
          kerberosLogins.addAll((List<String>) kerberosLoginsAttr.getValue());
        }

        if (!kerberosLogins.contains(userLogin + "@EGI") && kerberosLoginsAttr != null) {
          kerberosLogins.add(userLogin + "@EGI");
          kerberosLoginsAttr.setValue(kerberosLogins);
          ((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttribute(sess, user, kerberosLoginsAttr);
        }
      } catch (WrongAttributeAssignmentException | AttributeNotExistsException | ExtSourceNotExistsException |
               WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
        throw new InternalErrorException(ex);
      }
    }

    // validate password
    super.validatePassword(sess, userLogin, user);
  }
}
