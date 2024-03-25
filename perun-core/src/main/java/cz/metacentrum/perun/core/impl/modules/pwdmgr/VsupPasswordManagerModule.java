package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VsupPasswordManagerModule extends GenericPasswordManagerModule {

  private static final Logger LOG = LoggerFactory.getLogger(EinfraPasswordManagerModule.class);

  public VsupPasswordManagerModule() {

    // set proper namespace
    this.actualLoginNamespace = "vsup";

    // override random password generating params
    this.randomPasswordLength = 14;

    // omit chars that can be mistaken by users: yY, zZ, O, l, I and all spec chars.
    this.randomPasswordCharacters = "ABCDEFGHJKLMNPQRSTUVWXabcdefghijkmnopqrstuvwx0123456789".toCharArray();

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
      // set extSources and extSource related attributes
      try {
        // Add UES in their ActiveDirectory to access Perun by it
        ExtSource extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, "AD");
        UserExtSource ues = new UserExtSource(extSource, userLogin);
        ues.setLoa(0);

        try {
          ((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
        } catch (UserExtSourceExistsException ex) {
          //this is OK
        }
      } catch (ExtSourceNotExistsException ex) {
        throw new InternalErrorException(ex);
      }
    }

    // validate password
    super.validatePassword(sess, userLogin, user);
  }
}
