package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Checks that value of m365AllowedLicensesPriorities is valid:
 * -	does not contain a null key
 * -	when a change is made, there is no m365LicenseGroup attribute under the same facility
 * containing a license that no longer exists in this map
 *
 * @author Michal Berky <michal.berky@cesnet.cz>
 */
public class urn_perun_facility_attribute_def_def_m365AllowedLicensesPriorities extends FacilityAttributesModuleAbstract
    implements FacilityAttributesModuleImplApi {

  private static final String A_FAC_m365AllowedLicensesPriorities =
      AttributesManager.NS_FACILITY_ATTR_DEF + ":m365AllowedLicensesPriorities";
  private static final String A_RES_m365LicenseGroup = AttributesManager.NS_RESOURCE_ATTR_DEF + ":m365LicenseGroup";

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongAttributeValueException {
    LinkedHashMap<String, String> licensesPriorities = attribute.valueAsMap();
    if (licensesPriorities == null) {
      return;
    }

    for (String key : licensesPriorities.keySet()) {
      if (key == null) {
        throw new WrongAttributeValueException("There can't be a null key in: " + attribute);
      }
      String license = licensesPriorities.get(key);
      if (license == null || license.isEmpty()) {
        throw new WrongAttributeValueException("There can't be an empty value in: " + attribute + " for key: " + key);
      }
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute)
      throws WrongReferenceAttributeValueException, InternalErrorException {
    LinkedHashMap<String, String> licensesMap = attribute.valueAsMap();
    List<Resource> resources =
        perunSession.getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility);

    if (licensesMap == null) {
      handleNullLicensesMap(perunSession, resources, attribute);
      return;
    }

    verifyResourcesLicense(perunSession, resources, licensesMap);
  }

  private void handleNullLicensesMap(PerunSessionImpl perunSession, List<Resource> resources, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    if (resources.isEmpty()) {
      return;
    }

    for (Resource res : resources) {
      if (fetchResourceLicense(perunSession, res) != null) {
        throw new WrongReferenceAttributeValueException("The attribute '" + attribute +
            "' is being cleared, but there is still a resource in this facility depending on it: '" + res.getName());
      }
    }
  }

  private String fetchResourceLicense(PerunSessionImpl perunSession, Resource res)
      throws WrongReferenceAttributeValueException {
    try {
      return perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, res, A_RES_m365LicenseGroup)
          .valueAsString();
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
      throw new WrongReferenceAttributeValueException(
          "Couldn't retrieve m365LicenseGroup from resource " + res.getName(), e);
    }
  }

  private void verifyResourcesLicense(PerunSessionImpl perunSession, List<Resource> resources,
                                      LinkedHashMap<String, String> licensesMap)
      throws WrongReferenceAttributeValueException {
    for (Resource res : resources) {
      String resourceLicense = fetchResourceLicense(perunSession, res);

      if (resourceLicense != null && !licensesMap.containsValue(resourceLicense)) {
        throw new WrongReferenceAttributeValueException(
            "The license group: " + resourceLicense + " is still required in this facility by resource: " +
                res.getName());
      }
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
    attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
    attr.setFriendlyName("m365AllowedLicensesPriorities");
    attr.setDisplayName("M365 Allowed Licenses Priorities");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Map of priority number (higher = bigger priority) and license names for M365");
    return attr;
  }
}
