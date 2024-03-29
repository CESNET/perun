package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.util.regex.Matcher;

/**
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 */
public class urn_perun_user_attribute_def_def_rootMailAliasesMail extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
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
    attr.setFriendlyName("rootMailAliasesMail");
    attr.setDisplayName("Email for root mail aliases. If it's not set, preferred mail will be used instead.");
    attr.setType(String.class.getName());
    attr.setDescription("Email for root mail aliases. If it's not set, preferred mail will be used instead.");
    return attr;
  }
}
