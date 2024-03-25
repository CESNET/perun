package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.InvalidHtmlInputException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.HTMLParser;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

/**
 * Attribute for checking HTML content of mail footer (in a Group).
 *
 * @author Matej Hakoš <492968@mail.muni.cz>
 */
public class urn_perun_group_attribute_def_def_htmlMailFooter extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {
  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    // Sanitize HTML (remove unsafe tags, attributes, styles
    String input = (String) attribute.getValue();
    if (input == null) {
      return;
    }
    HTMLParser parser = new HTMLParser().sanitizeHTML(input).checkEscapedHTML();
    if (!parser.isInputValid()) {
      throw new InvalidHtmlInputException(
          "HTML content contains unsafe HTML tags or styles. Remove them and try again.", parser.getEscaped());
    }
    attribute.setValue(parser.getEscapedHTML());
  }

  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setDisplayName("HTML Mail Footer");
    attrDef.setFriendlyName("htmlMailFooter");
    attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attrDef.setDescription(
        "HTML email footer used in HTML mail notifications by tag {htmlMailFooter}. To edit text without loss of " +
        "formatting, please use notification's GUI!!");
    attrDef.setType(String.class.getName());
    return attrDef;
  }

}
