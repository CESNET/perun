package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;

/**
 * Attribute module for rpEnvironment. Possible values are "TESTING", "STAGING" or "PRODUCTION".
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_rpEnvironment extends FacilityAttributesModuleAbstract {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongAttributeValueException {
    String attrValue = attribute.valueAsString();
    if (attrValue == null) {
      return;
    }

    if (!attrValue.equals("TESTING") && !attrValue.equals("STAGING") && !attrValue.equals("PRODUCTION")) {
      throw new WrongAttributeValueException(attribute, facility,
          "Possible values for this attribute are 'TESTING', 'STAGING' or 'PRODUCTION'");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("rpEnvironment");
    attr.setDisplayName("rpEnvironment");
    attr.setType(String.class.getName());
    attr.setDescription("Environment of relying party - TESTING, STAGING or PRODUCTION");
    return attr;
  }
}
