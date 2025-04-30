package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class urn_perun_facility_attribute_def_def_ldapUserAttrMap extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  private static final String USER_DN_ATTR = AttributesManager.NS_FACILITY_ATTR_DEF + ":ldapUserDNAttribute";

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "attribute is null");
    }

    String dnAttr;
    try {
      Attribute userDnAttr = perunSession.getPerunBl().getAttributesManagerBl()
                                 .getAttribute(perunSession, facility, USER_DN_ATTR);
      dnAttr = userDnAttr.getValue() != null ? userDnAttr.valueAsString() : "cn";
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
      dnAttr = "cn";
    }

    if (!attribute.valueAsMap().containsValue(dnAttr)) {
      throw new WrongReferenceAttributeValueException(attribute, null, facility, null,
          "DN attribute assignment '" + dnAttr + "' is missing.");
    }


    LinkedHashMap<String, String> map = attribute.valueAsMap();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      AttributeDefinition attrDef;
      try {
        attrDef = perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession,
            entry.getKey());
      } catch (AttributeNotExistsException e) {
        throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "Attribute " +
                                                 entry.getKey() + " doesn't exist for ldap attribute " +
                                                                                             entry.getValue() + ".");
      }

      if (!perunSession.getPerunBl().getAttributesManagerBl().isFromNamespace(perunSession, attrDef,
          AttributesManager.NS_USER_ATTR) && !perunSession.getPerunBl().getAttributesManagerBl()
                                                  .isFromNamespace(perunSession, attrDef,
          AttributesManager.NS_USER_FACILITY_ATTR) && !perunSession.getPerunBl().getAttributesManagerBl()
                                                           .isFromNamespace(perunSession, attrDef,
          AttributesManager.NS_MEMBER_ATTR)) {
        throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "Attribute for" +
                                                                                             " ldap attribute " +
                                       entry.getValue() + " - " + entry.getKey() +
                                           "has to be either a user, user-facility or member attribute.");
      }
    }
  }

  @Override
  public List<String> getDependencies() {
    return List.of(USER_DN_ATTR);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("ldapUserAttrMap");
    attr.setDisplayName("Map of Perun user attributes to LDAP attributes");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription(
        "Map of Perun user attributes to LDAP attributes. LDAP attributes are the values, Perun attributes the keys." +
            "The full attribute name has to be listed, e.g 'urn:perun:user:attribute-def:def:login-namespace:mu'");
    return attr;
  }
}
