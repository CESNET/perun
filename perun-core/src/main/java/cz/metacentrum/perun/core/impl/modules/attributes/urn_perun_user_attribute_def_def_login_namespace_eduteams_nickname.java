package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling it
 */
public class urn_perun_user_attribute_def_def_login_namespace_eduteams_nickname
    extends urn_perun_user_attribute_def_def_login_namespace {

  private static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_eduteams_nickname.class);

  /**
   * Check if the user's login is in the correct format and if it is permitted to use. Check if maximum length is 20
   * chars
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

    if (generatedNamespaces.contains(attribute.getFriendlyNameParameter())) {

      ModulesUtilsBlImpl.LoginGenerator generator = new ModulesUtilsBlImpl.LoginGenerator();
      String login = generator.generateLogin(user, (firstName, lastName) -> {

        // unable to fill login for users without name or with partial name
        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
          return "eduteams-user";
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

  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("login-namespace:eduteams-nickname");
    attr.setDisplayName("Login in namespace: eduteams");
    attr.setType(String.class.getName());
    attr.setDescription("Logname in namespace 'eduteams'.");
    return attr;
  }


}
