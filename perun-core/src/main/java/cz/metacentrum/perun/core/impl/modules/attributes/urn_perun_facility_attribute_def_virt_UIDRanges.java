package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class urn_perun_facility_attribute_def_virt_UIDRanges extends FacilityVirtualAttributesModuleAbstract
    implements FacilityVirtualAttributesModuleImplApi {

  private static final String A_E_namespaceUIDRanges =
      AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-UIDRanges";
  private static final String A_FAC_uidNamespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace";

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    try {
      Attribute uidNamespaceAttribute = getUidNamespaceAttribute(sess, facility);
      if (uidNamespaceAttribute.getValue() == null) {
        throw new WrongReferenceAttributeValueException(attribute, uidNamespaceAttribute,
            "There is missing UID namespace on the facility.");
      }
      Attribute namespaceUIDRangesAttribute =
          getNamespaceUIDRangesAttribute(sess, uidNamespaceAttribute.valueAsString());
      sess.getPerunBl().getAttributesManagerBl()
          .checkAttributeSemantics(sess, uidNamespaceAttribute.valueAsString(), namespaceUIDRangesAttribute);
    } catch (WrongReferenceAttributeValueException ex) {
      throw new WrongReferenceAttributeValueException(attribute, ex.getReferenceAttribute());
    }

  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility,
                                     AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);
    Attribute uidNamespaceAttribute = getUidNamespaceAttribute(sess, facility);
    if (uidNamespaceAttribute.getValue() == null) {
      return attribute;
    }
    Attribute namespaceUIDRangesAttribute = getNamespaceUIDRangesAttribute(sess, uidNamespaceAttribute.valueAsString());
    return Utils.copyAttributeToVirtualAttributeWithValue(namespaceUIDRangesAttribute, attribute);
  }

  @Override
  public void removeAttributeValue(PerunSessionImpl sess, Facility facility, AttributeDefinition attributeDefinition) {
    //Not suported yet.
    throw new InternalErrorException("Can't remove value of this virtual attribute this way. " + attributeDefinition);
  }

  private Attribute getNamespaceUIDRangesAttribute(PerunSessionImpl sess, String uidNamespace) {
    try {
      return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, uidNamespace, A_E_namespaceUIDRanges);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  private Attribute getUidNamespaceAttribute(PerunSessionImpl sess, Facility facility) {
    try {
      return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, A_FAC_uidNamespace);
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(A_FAC_uidNamespace);
    dependencies.add(A_E_namespaceUIDRanges);
    return dependencies;
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> strongDependencies = new ArrayList<>();
    strongDependencies.add(A_FAC_uidNamespace);
    strongDependencies.add(A_E_namespaceUIDRanges);
    return strongDependencies;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_VIRT);
    attr.setFriendlyName("UIDRanges");
    attr.setDisplayName("UID ranges in set namespace for the Facility");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Computed UID ranges in set namespace for the facility");
    return attr;
  }

}
