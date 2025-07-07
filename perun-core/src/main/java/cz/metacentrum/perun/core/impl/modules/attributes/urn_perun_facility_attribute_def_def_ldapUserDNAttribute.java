package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

public class urn_perun_facility_attribute_def_def_ldapUserDNAttribute extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {
  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "attribute is null");
    }
    if (!attribute.valueAsString().equals("cn") && !attribute.valueAsString().equals("uid")) {
      throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "attribute has to be cn or uid");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("ldapUserDNAttribute");
    attr.setDisplayName("LDAP User DN attribute");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Whether to use `cn` or `uid` for user DN. Default behaviour will be `cn` if this attribute is not set");
    return attr;
  }
}
