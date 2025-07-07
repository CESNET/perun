package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.text.ParseException;

/**
 * Very simple VO attribute containing expiration date of purpose, module just for the date format check
 *
 * @author David Flor
 */
public class urn_perun_vo_attribute_def_def_expirationDate extends VoAttributesModuleAbstract
    implements VoAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Vo vo, Attribute attribute)
      throws WrongAttributeValueException {
    String voExpTime = attribute.valueAsString();

    if (voExpTime == null) {
      return; // NULL is ok
    }

    try {
      BeansUtils.getDateFormatterWithoutTime().parse(voExpTime);
    } catch (ParseException ex) {
      throw new WrongAttributeValueException(attribute, "Wrong format, YYYY-MM-DD expected.", ex);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
    attr.setType(String.class.getName());
    attr.setFriendlyName("expirationDate");
    attr.setDisplayName("VO expiration date");
    attr.setDescription("Date in YYYY-MM-DD format on which purpose of the VO expires and" +
                            " can be checked for deletion.");
    return attr;
  }
}
