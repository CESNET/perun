package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsAlreadyBlockedException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for EINFRA login namespace improves login checks with case-insensitive search!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.core.impl.modules.pwdmgr.EinfraPasswordManagerModule
 */
public class urn_perun_user_attribute_def_def_login_namespace_einfra
    extends urn_perun_user_attribute_def_def_login_namespace implements UserAttributesModuleImplApi {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_einfra.class);

  /**
   * Temporarily allows case ignore collision for specific logins
   * FIXME
   *
   * @param login user login in EINFRA namespace
   * @return Whether collision is allowed or not
   */
  private static boolean allowCaseIgnoreCollision(String login) {
    return login.equalsIgnoreCase("idu") || login.equalsIgnoreCase("kb");
  }

  /**
   * Checks if the user's login is unique in the namespace organization
   *
   * @param sess      PerunSession
   * @param user      User to check attribute for
   * @param attribute Attribute to check value to
   * @throws InternalErrorException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    String userLogin = attribute.valueAsString();
    if (userLogin == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");
    }

    boolean ignoreCase = !allowCaseIgnoreCollision(attribute.getValue().toString());

    List<User> usersWithSameLogin =
        sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, attribute, ignoreCase);

    usersWithSameLogin.remove(user); //remove self

    if (!usersWithSameLogin.isEmpty()) {
      if (usersWithSameLogin.size() > 1) {
        throw new ConsistencyErrorException(
            "FATAL ERROR: Duplicated Login detected." + attribute + " " + usersWithSameLogin);
      }
      throw new WrongReferenceAttributeValueException(attribute, attribute, user, null, usersWithSameLogin.get(0), null,
          "This login " + attribute.getValue() + " is already occupied.");
    }

    try {
      String namespace = attribute.getFriendlyNameParameter();
      sess.getPerunBl().getUsersManagerBl().checkReservedLogins(sess, namespace, userLogin, ignoreCase);
      sess.getPerunBl().getUsersManagerBl().checkBlockedLogins(sess, namespace, userLogin, ignoreCase);
    } catch (AlreadyReservedLoginException ex) {
      throw new WrongReferenceAttributeValueException(attribute, null, user, null, null, null,
          "Login in specific namespace already reserved.", ex);
    } catch (LoginIsAlreadyBlockedException ex) {
      throw new WrongReferenceAttributeValueException(attribute, null, "Login is blocked.", ex);
    }
  }
}
