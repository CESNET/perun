package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

public class urn_perun_user_attribute_def_def_login_namespace_erasmus_username
    extends urn_perun_user_attribute_def_def_login_namespace {

  /**
   * Filling implemented for: - namespaces configured in /etc/perun/perun.properties as property:
   * "perun.loginNamespace.generated"
   * <p>
   * Resulting format/rules: - "firstName-lastName[number]" where number is opt and start with 1 when same login is
   * already present.
   *
   * @param perunSession PerunSession
   * @param user         User to fill attribute for
   * @param attribute    Attribute to fill value to
   * @return Filled attribute
   * @throws WrongAttributeAssignmentException
   */
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {

    Attribute filledAttribute = new Attribute(attribute);

    if (generatedNamespaces.contains(attribute.getFriendlyNameParameter())) {

      String login = generateLoginBase(user);

      if (login == null) {
        return filledAttribute;
      }

      // fill value
      int iterator = 0;
      while (iterator >= 0) {
        if (iterator > 0) {
          int iteratorLength = String.valueOf(iterator).length();
          if (login.length() + iteratorLength > 16) {
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
    }
    return filledAttribute;
  }

  /**
   * Generates login base from users' first and last name. When the result is longer then 16 chars, it is cut to 16
   * chars. Only first part of "firstName" and last part of "lastName" is taken. All accented chars are unaccented and
   * all non (a-z,A-Z) chars are removed from name and value is lowered.
   *
   * @param user for which the login base is generated
   * @return String in form of firstname_lastname or erasmus-user if some info is missing
   */
  private String generateLoginBase(User user) {
    ModulesUtilsBlImpl.LoginGenerator generator = new ModulesUtilsBlImpl.LoginGenerator();
    return generator.generateLogin(user, (firstName, lastName) -> {

      //default login
      String login = "erasmus-user";

      boolean firstNameFilled = firstName != null && !firstName.isEmpty();
      boolean lastNameFilled = lastName != null && !lastName.isEmpty();

      if (firstNameFilled && lastNameFilled) {
        login = firstName + "-" + lastName;
      } else if (firstNameFilled) {
        login = firstName;
      } else if (lastNameFilled) {
        login = lastName;
      }

      if (login.length() > 16) {
        login = login.substring(0, 16);
      }
      return login;
    });
  }

  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("login-namespace:erasmus-username");
    attr.setDisplayName("ERASMUS username");
    attr.setType(String.class.getName());
    attr.setDescription("ERASMUS username");
    return attr;
  }
}
