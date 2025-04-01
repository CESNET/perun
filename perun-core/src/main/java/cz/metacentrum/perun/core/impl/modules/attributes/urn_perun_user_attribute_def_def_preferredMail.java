package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.regex.Matcher;

/**
 * @author Michal Šťava   <stava.michal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_preferredMail extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  private static final String A_M_mail = AttributesManager.NS_MEMBER_ATTR_DEF + ":mail";

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, user, null,
          "User preferred mail can't be set to null.");
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }
    String attributeValue = attribute.valueAsString();

    Matcher emailMatcher = Utils.EMAIL_PATTERN.matcher(attributeValue);
    if (!emailMatcher.find()) {
      throw new WrongAttributeValueException(attribute, user, "Email is not in correct form.");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("preferredMail");
    attr.setDisplayName("Preferred mail");
    attr.setType(String.class.getName());
    attr.setDescription("User's preferred mail");
    return attr;
  }
}
