package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Checks that License group is present in m365AllowedLicensesPriorities map
 *
 * @author Michal Berky <michal.berky@cesnet.cz>
 */
public class urn_perun_resource_attribute_def_def_m365LicenseGroup extends ResourceAttributesModuleAbstract
    implements ResourceAttributesModuleImplApi {

  private static final String A_FAC_m365AllowedLicensesPriorities =
      AttributesManager.NS_FACILITY_ATTR_DEF + ":m365AllowedLicensesPriorities";

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    String licenseGroup = attribute.valueAsString();
    if (licenseGroup == null || licenseGroup.isEmpty()) {
      return;
    }

    try {
      Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);

      Attribute allowedLicenses = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(
          perunSession, facility, A_FAC_m365AllowedLicensesPriorities);

      LinkedHashMap<String, String> allowedLicensesMap = allowedLicenses.valueAsMap();
      if (!allowedLicensesMap.containsValue(licenseGroup)) {
        throw new WrongReferenceAttributeValueException(attribute, allowedLicenses,
            "The license group: " + licenseGroup +
                " is not allowed for this Facility. (Not present in m365ALlowedLicensesPriorities facility attribute)");
      }
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
      throw new WrongReferenceAttributeValueException("Error checking attribute semantics.", e);
    }
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(A_FAC_m365AllowedLicensesPriorities);
    return dependencies;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("m365LicenseGroup");
    attr.setDisplayName("M365 License Group");
    attr.setType(String.class.getName());
    attr.setDescription("Specify name of license the group represents");
    return attr;
  }
}
