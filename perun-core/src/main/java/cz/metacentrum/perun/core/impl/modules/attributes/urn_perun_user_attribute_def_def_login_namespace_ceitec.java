package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.CeitecCrmConnector;
import cz.metacentrum.perun.core.impl.modules.ModulesYamlConfigLoader;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling ceitec id.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_ceitec
        extends urn_perun_user_attribute_def_def_login_namespace {

  private static final Logger LOG =
          LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_ceitec.class);

  private static final String CEITEC_PROXY_ENTITY_ID = "https://login.ceitec.cz/idp/";
  private static final String CEITEC_PROXY_SCOPE = "ceitec.cz";

  private static final String CEITEC_PROXY_ENTITY_ID_AD = "https://login.ceitec.cz/idp-ad/";
  private static final String CEITEC_PROXY_SCOPE_AD = "ceitec.local";

  private static final CeitecCrmConnector ceitecCrmConnector = new CeitecCrmConnector();
  private static final ModulesYamlConfigLoader loader = new ModulesYamlConfigLoader();

  @Override
  public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute)
          throws WrongReferenceAttributeValueException {
    // generate identities only on old Perun instance
    if (!loader.moduleFileExists("Ceitec")) {
      handleChangedAttributeHook(session, user, attribute, CEITEC_PROXY_ENTITY_ID, CEITEC_PROXY_SCOPE);
      handleChangedAttributeHook(session, user, attribute, CEITEC_PROXY_ENTITY_ID_AD, CEITEC_PROXY_SCOPE_AD);
    }
  }

  /**
   * Check if the user's login is in the correct format and if it is permitted to use. Check if maximum length is 20
   * chars, because of MSAD limitations.
   *
   * @param sess      PerunSession
   * @param user      User to check attribute for
   * @param attribute Attribute to check value to
   * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
   * @throws cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException
   */
  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
          throws WrongAttributeValueException {

    // check syntax of attribute
    super.checkAttributeSyntax(sess, user, attribute);

    // plus check, that login is max 20 chars.
    if (attribute.getValue() != null) {
      if ((attribute.valueAsString()).length() > 20) {
        throw new WrongAttributeValueException(attribute, user,
                "Login '" + attribute.getValue() + "' exceeds 20 chars limit.");
      }
    }

  }

  /**
   * Filling implemented for: - namespaces configured in /etc/perun/perun.properties as property:
   * "perun.loginNamespace.generated"
   * <p>
   * Resulting format/rules: - "firstName.lastName[number]" where number is opt and start with 1 when same login is
   * already present. - Only first part of "firstName" and last part of "lastName" is taken. - All accented chars are
   * unaccented and all non (a-z,A-Z) chars are removed from name and value is lowered.
   *
   * @param perunSession PerunSession
   * @param user         User to fill attribute for
   * @param attribute    Attribute to fill value to
   * @return Filled attribute
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute)
          throws WrongAttributeAssignmentException {

    Attribute filledAttribute = new Attribute(attribute);
    if (loader.moduleFileExists("Ceitec")) {
      return fillAttributeUsingCRM(perunSession, user, filledAttribute);
    } else {
      return fillAttributeLocally(perunSession, user, filledAttribute);
    }

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("login-namespace:ceitec");
    attr.setDisplayName("Login in namespace: ceitec");
    attr.setType(String.class.getName());
    attr.setDescription("Logname in namespace 'ceitec'.");
    return attr;
  }

  /**
   * LOCAL FILL ATTRIBUTE LOGIN for old CEITEC (MU instance).
   *
   * Filling implemented for: - namespaces configured in /etc/perun/perun.properties as property:
   * "perun.loginNamespace.generated"
   * <p>
   * Resulting format/rules: - "firstName.lastName[number]" where number is opt and start with 1 when same login is
   * already present. - Only first part of "firstName" and last part of "lastName" is taken. - All accented chars are
   * unaccented and all non (a-z,A-Z) chars are removed from name and value is lowered.
   */
  private Attribute fillAttributeLocally(PerunSessionImpl perunSession, User user, Attribute filledAttribute)
          throws WrongAttributeAssignmentException {

    if (generatedNamespaces.contains(filledAttribute.getFriendlyNameParameter())) {

      ModulesUtilsBlImpl.LoginGenerator generator = new ModulesUtilsBlImpl.LoginGenerator();
      String login = generator.generateLogin(user, (firstName, lastName) -> {

        // unable to fill login for users without name or with partial name - use "External.customer1" like logins
        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
          firstName = "External";
          lastName = "customer";
        }

        String login1 = firstName + "." + lastName;
        if (login1.length() > 20) {
          login1 = login1.substring(0, 20);
        }
        return login1;
      });

      if (login == null) {
        return filledAttribute;
      }

      // fill value
      int iterator = 0;
      while (iterator >= 0) {
        if (iterator > 0) {
          int iteratorLength = String.valueOf(iterator).length();
          if (login.length() + iteratorLength > 20) {
            // if login+iterator > 20 => crop login & reset iterator
            login = login.substring(0, login.length() - 1);
            iterator = 0;
            filledAttribute.setValue(login);
          } else {
            filledAttribute.setValue(login + iterator);
          }

        } else {
          filledAttribute.setValue(login);
        }
        try {
          checkAttributeSemantics(perunSession, user, filledAttribute);
          return filledAttribute;
        } catch (WrongReferenceAttributeValueException ex) {
          // continue in a WHILE cycle
          iterator++;
        }
      }

      return filledAttribute;

    } else {
      // without value
      return filledAttribute;
    }

  }

  /**
   * Fill attribute using same rules, but data is generated on CRM side (new CEITEC on e-INFRA CZ instance)
   * We call CRM passing users ceitec_id, eppns, name and get back
   * either existing login for matching user (by ceitec_id, or eppns)
   * or new login if there is no matching user.
   * If eppns are split between multiple user accounts in ceitec CRM, this throws error since user must "choose"
   * one of the accounts.
   *
   * @param perunSession
   * @param user
   * @param filledAttribute
   * @return
   */
  private Attribute fillAttributeUsingCRM(PerunSessionImpl perunSession, User user, Attribute filledAttribute) {

    AttributesManagerBl attributesManagerBl = perunSession.getPerunBl().getAttributesManagerBl();
    try {
      // Exclusive lock for transaction
      Attribute lock = attributesManagerBl.getEntitylessAttributeForUpdate(perunSession, "CRM",
              AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":ceitecCrmLock");
      String login = getLoginFromCRM(perunSession, user);
      filledAttribute.setValue(login);
      return filledAttribute;
    } catch (AttributeNotExistsException e) {
      throw new InternalErrorException("Locking attribute ceitecCrmLock not found.");
    }

  }

  private String getLoginFromCRM(PerunSessionImpl perunSession, User user) {

    PerunBl perunBl = perunSession.getPerunBl();
    Attribute eppns = null;
    Attribute ceitecId = null;

    // TODO - normalize string or convert?
    // get only first part of first name and remove spec. chars
    // get only last part of last name and remove spec. chars
    // like ModulesUtilsBlImpl.LoginGenerator().generateLogin();

    try {
      eppns = perunBl.getAttributesManagerBl().getAttribute(perunSession, user,
              AttributesManager.NS_USER_ATTR_VIRT + ":eduPersonPrincipalNames");
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }

    try {
      ceitecId = perunBl.getAttributesManagerBl().getAttribute(perunSession, user,
              AttributesManager.NS_USER_ATTR_DEF + ":ceitecId");
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      throw new InternalErrorException(e);
    }

    try {
      return ceitecCrmConnector.getLogin(ceitecId.valueAsString(), eppns, user);
    } catch (Exception ex) {
      return null;
    }

  }

  private UserExtSource getCeitecProxyUserExtSource(PerunSessionImpl session, User user, ExtSource ceitecExtSource,
                                                    String scope) {
    UserExtSource ceitecProxyUserExtSource = null;

    List<UserExtSource> uess = session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user);

    for (UserExtSource ues : uess) {
      if (ues.getExtSource().equals(ceitecExtSource) && ues.getLogin().endsWith("@" + scope)) {
        if (ceitecProxyUserExtSource != null) {
          throw new InternalErrorException(
                  "Multiple UserExtSourceLogins with Ceitec proxy IdP scope '" + scope + "' founded for user: " + user);
        }
        ceitecProxyUserExtSource = ues;
      }
    }
    return ceitecProxyUserExtSource;

  }

  private void handleChangedAttributeHook(PerunSessionImpl session, User user, Attribute attribute, String entityId,
                                          String scope) {

    /*
     * "Synchornize" this attribute to user extSource. Means it creates, updates and removes userExtSource
     * whenever this attribute is added, edited or removed.
     *
     * Ceitec proxy UserExtSourceLogin has form: {login-namespace:ceitec}@ceitec.cz
     */
    String newLogin = "";

    try {
      ExtSource ceitecProxyIdp = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, entityId);

      UserExtSource ceitecUes = getCeitecProxyUserExtSource(session, user, ceitecProxyIdp, scope);
      LOG.debug("changedAttributeHook UserExtSourceLogin to be synchronized: " + ceitecUes);

      if (attribute.getValue() == null) {
        // Deleting attribute
        if (ceitecUes == null) {
          LOG.debug("Deleting ceitec login but proxy UES does not exist. Probably ceitec login was not set before.");
        } else {
          session.getPerunBl().getUsersManagerBl().removeUserExtSource(session, user, ceitecUes);
        }

      } else {
        newLogin = attribute.getValue() + "@" + scope;
        if (ceitecUes == null) {
          // Creating UES
          ceitecUes = new UserExtSource(ceitecProxyIdp, 0, newLogin);
          session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, ceitecUes);
        } else {
          // Updating UES
          ceitecUes.setLogin(newLogin);
          session.getPerunBl().getUsersManagerBl().updateUserExtSource(session, ceitecUes);
        }

      }

    } catch (ExtSourceNotExistsException e) {
      throw new InternalErrorException("Attribute module 'urn_perun_user_attribute_def_def_login_namespace_ceitec' " +
              " require extSource with name (entityId): " + entityId + ". User: " + user, e);
    } catch (UserExtSourceAlreadyRemovedException e) {
      throw new InternalErrorException(
              "Inconsistency. Attribute module 'urn_perun_user_attribute_def_def_login_namespace_ceitec' " +
                      " tries to delete extSource but it does not exists. " + "extSource with name (entityId): " +
                      entityId + ". User: " + user, e);
    } catch (UserExtSourceExistsException e) {
      throw new InternalErrorException("Login: '" + newLogin + "' of user: " + user +
              " is already taken by another user in namespace ceitec. Cannot add " +
              "UserExtSource to the user. Different " +
              "login must be used", e);
    }

  }

}
