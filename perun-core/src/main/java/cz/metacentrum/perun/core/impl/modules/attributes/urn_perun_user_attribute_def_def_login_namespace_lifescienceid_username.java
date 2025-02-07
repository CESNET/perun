package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsAlreadyBlockedException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class urn_perun_user_attribute_def_def_login_namespace_lifescienceid_username
    extends urn_perun_user_attribute_def_def_login_namespace {
  private static final String elixirUsername = "urn:perun:user:attribute-def:def:login-namespace:elixir";
  private static final String bbmriUsername = "urn:perun:user:attribute-def:def:login-namespace:bbmri";

  private static final Pattern startWithLetterPattern = Pattern.compile("^[A-Za-z].*$");
  private static final Pattern onlyNumbersPattern = Pattern.compile("^[0-9]+$");


  @Override
  public void changedAttributeHook(PerunSessionImpl sess, User user, Attribute attribute) {
    trySetAttribute(sess, user, attribute, elixirUsername);
    trySetAttribute(sess, user, attribute, bbmriUsername);
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    String userLogin = attribute.valueAsString();
    if (userLogin == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");
    }

    Attribute namespaceAttribute = attribute;
    for (String namespaceUsername : List.of(attribute.getName(), elixirUsername, bbmriUsername)) {
      try {
        AttributeDefinition attrDefinition = sess.getPerunBl().getAttributesManagerBl()
                                                     .getAttributeDefinition(sess, namespaceUsername);
        namespaceAttribute = new Attribute(attrDefinition, userLogin);
        String namespace = namespaceAttribute.getFriendlyNameParameter();

        // Check duplicity
        List<User> usersWithSameLogin = sess.getPerunBl().getUsersManagerBl()
                                                           .getUsersByAttribute(sess, namespaceAttribute, true);
        usersWithSameLogin.remove(user);
        if (!usersWithSameLogin.isEmpty()) {
          if (usersWithSameLogin.size() > 1) {
            throw new ConsistencyErrorException(
                    "FATAL ERROR: Duplicated Login detected." + namespaceAttribute + " " + usersWithSameLogin);
          }
          throw new WrongReferenceAttributeValueException(
                  namespaceAttribute, namespaceAttribute, user, null, usersWithSameLogin.get(0), null,
                  "This login " + namespaceAttribute.getValue() + " is already occupied.");
        }

        sess.getPerunBl().getUsersManagerBl().checkReservedLogins(sess, namespace, userLogin, true);

        sess.getPerunBl().getUsersManagerBl().checkBlockedLogins(sess, namespace, userLogin, true);

      } catch (AlreadyReservedLoginException ex) {
        throw new WrongReferenceAttributeValueException(attribute, null, user, null, null, null,
                "Login in specific namespace already reserved.", ex);
      } catch (LoginIsAlreadyBlockedException ex) {
        throw new WrongReferenceAttributeValueException(attribute, null, "Login is blocked.", ex);
      } catch (AttributeNotExistsException ex) {
        // Ignore
      }
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
    super.checkAttributeSyntax(sess, user, attribute);

    if (attribute.getValue() == null) {
      return;
    }

    String value = attribute.valueAsString();

    Matcher onlyNumbersMatcher = onlyNumbersPattern.matcher(value);
    if (onlyNumbersMatcher.matches()) {
      throw new WrongAttributeValueException(attribute, user, "Login can not consist of only numbers.");
    }

    Matcher startWithLetterMatcher = startWithLetterPattern.matcher(value);
    if (!startWithLetterMatcher.matches()) {
      throw new WrongAttributeValueException(attribute, user, "Login must start with a letter.");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("login-namespace:lifescienceid-username");
    attr.setDisplayName("Lifescience username (login)");
    attr.setType(String.class.getName());
    attr.setDescription("Login in namespaceid: lifescience");
    return attr;
  }

  /**
   * Set attribute if it is not filled yet
   */
  private void trySetAttribute(PerunSessionImpl sess, User user, Attribute lsAttribute, String attributeName) {
    Attribute newAttribute;
    try {
      newAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attributeName);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      return;
    }

    if (newAttribute.getValue() != null && !newAttribute.valueAsString().isBlank()) {
      return;
    }

    newAttribute.setValue(lsAttribute.getValue());

    try {
      sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, newAttribute);
    } catch (WrongAttributeValueException | WrongAttributeAssignmentException |
             WrongReferenceAttributeValueException e) {
      throw new InternalErrorException(e);
    }
  }
}
