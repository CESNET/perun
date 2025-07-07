package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;
import java.util.List;

public class urn_perun_facility_attribute_def_def_ldapDeactivate extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {
  private static final String A_F_ldapDeactivateAttributeName = AttributesManager.NS_FACILITY_ATTR_DEF +
                                                                    ":ldapDeactivateAttributeName";

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    if (attribute.getValue() == null) {
      // null is fine
      return;
    }
    if (attribute.valueAsBoolean()) {
      try {
        Attribute attrNameAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession,
            facility, A_F_ldapDeactivateAttributeName);
        if (attrNameAttribute.getValue() == null || attrNameAttribute.valueAsString().isEmpty()) {
          throw new WrongReferenceAttributeValueException(attribute, null, facility, null,
              "Attribute " + A_F_ldapDeactivateAttributeName + " needs to be defined for LDAP deactivation to work.");
        }
      } catch (AttributeNotExistsException e) {
        // should not happen
        throw new InternalErrorException(e);
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setType(Boolean.class.getName());
    attr.setFriendlyName("ldapDeactivate");
    attr.setDisplayName("LDAP Deactivate");
    attr.setDescription("Whether to deactivate or delete entries on the LDAP server. True will result in entries " +
                            "being deactivated, false (or unset) will result in entries being deleted.");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    return List.of(A_F_ldapDeactivateAttributeName);
  }
}
