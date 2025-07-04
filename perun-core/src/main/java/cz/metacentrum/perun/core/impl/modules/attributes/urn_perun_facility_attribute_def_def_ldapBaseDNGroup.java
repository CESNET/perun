package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;


public class urn_perun_facility_attribute_def_def_ldapBaseDNGroup extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "attribute is null");
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongAttributeValueException {

    if (attribute.getValue() == null) {
      return;
    }

    String value = attribute.valueAsString();
    if (value.length() < 3) {
      throw new WrongAttributeValueException(attribute, facility, "attribute has to start with \"ou=\" or \"dc=\"");
    }

    String sub = value.substring(0, 3);

    if (!(sub.equalsIgnoreCase("ou=") || sub.equalsIgnoreCase("dc="))) {
      throw new WrongAttributeValueException(attribute, facility, "attribute has to start with \"ou=\" or \"dc=\"");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("ldapBaseDNGroup");
    attr.setDisplayName("LDAP base DN for groups");
    attr.setType(String.class.getName());
    attr.setDescription(
        "Base part of DN, which will be used for all groups propagated to facility. Should be like \"ou=sth," +
        "dc=example,dc=domain\" (without quotes)");
    return attr;
  }
}
