package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import java.util.ArrayList;

public class urn_perun_facility_attribute_def_def_ldapUserObjectClasses extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    // null attribute
    if (attribute.getValue() == null) {
      throw new WrongReferenceAttributeValueException(attribute, "objectClasses list cannot be null.");
    }
  }


  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("ldapUserObjectClasses");
    attr.setDisplayName("LDAP User objectClasses");
    attr.setType(ArrayList.class.getName());
    attr.setDescription(
        "A list of objectClass attribute values for the user entity in LDAP. The initial search of LDAP entities will" +
            " be performed based on the first value of this list, so e.g. `person`");
    return attr;
  }
}
