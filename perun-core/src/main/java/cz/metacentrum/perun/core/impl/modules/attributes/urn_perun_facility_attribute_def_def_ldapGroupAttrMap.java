package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class urn_perun_facility_attribute_def_def_ldapGroupAttrMap extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "attribute is null");
    }

    if (!attribute.valueAsMap().containsValue("cn")) {
      throw new WrongReferenceAttributeValueException(attribute, null, facility, null,
          "DN attribute assignment 'cn' is missing.");
    }



    LinkedHashMap<String, String> map = attribute.valueAsMap();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      AttributeDefinition attrDef;
      try {
        attrDef = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession,
            entry.getKey());
      } catch (AttributeNotExistsException e) {
        throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "Attribute " +
                                                 entry.getKey() + " doesn't exist for LDAP attr " +
                                                                                             entry.getValue() + ".");
      }

      if (!perunSession.getPerunBl().getAttributesManagerBl().isFromNamespace(perunSession, attrDef,
          AttributesManager.NS_RESOURCE_ATTR)) {
        throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "Attribute for LDAP attr " +
                                       entry.getValue() + " - " + entry.getKey() + " has to be a resource attribute.");
      }
    }
  }


  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("ldapGroupAttrMap");
    attr.setDisplayName("Map of Perun resource attributes to LDAP attributes");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription(
        "Map of Perun resource attributes to LDAP attributes. LDAP attributes are the values, Perun attributes the" +
            " keys. The full attribute name has to be listed, e.g 'urn:perun:resource:attribute-def:core:name'");
    return attr;
  }
}
