package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import java.text.ParseException;

/**
 * Holds the date of the last extension of the member's VO membership (format YYYY-MM-DD), i.e. the date when the
 * member extended the membership by an approved extension application.
 * This value should only be set automatically by Perun.
 *
 * @author David Flor <davidflor@seznam.cz>
 */
public class urn_perun_member_attribute_def_def_lastExtension extends MemberAttributesModuleAbstract
    implements MemberAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }
    try {
      BeansUtils.getDateFormatterWithoutTime().parse(attribute.valueAsString());
    } catch (ParseException ex) {
      throw new WrongAttributeValueException(attribute, member,
          "Date parsing failed, expected format YYYY-MM-DD: " + attribute.getValue());
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
    attr.setFriendlyName("lastExtension");
    attr.setDisplayName("Last membership extension");
    attr.setType(String.class.getName());
    attr.setDescription("Date of the last VO membership extension, format YYYY-MM-DD. " +
                        "Set automatically by Perun when an extension application is approved.");
    return attr;
  }
}
