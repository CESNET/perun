package cz.metacentrum.perun.core.impl.modules.attributes;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AD Name module
 * <p>
 * Only one group can have this name set for one facility no matter the OU. If group is assigned to more than 1 resource
 * with OU set on the same facility, this state is forbidden.
 *
 * @author Michal Stava &lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_group_attribute_def_def_adName_o365mu extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {

  private static final String A_R_D_AD_OU_NAME = AttributesManager.NS_RESOURCE_ATTR_DEF + ":adOuName";

  private static final Pattern pattern = Pattern.compile("(\\w|-|\\.)*");

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    //Attribute can be null
    if (attribute.getValue() == null) {
      return;
    }

    //Prepare map where keys are facilityIds (to be able to group them together) and values are all resources under
    // facilityId
    Map<Integer, Set<Resource>> mapOfResourcesByFacility =
        sess.getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group).stream()
            .collect(groupingBy(Resource::getFacilityId, toSet()));

    //For every facility id in the Map check all resources where group was set
    for (Set<Resource> setOfResourcesWithSameFacilityId : mapOfResourcesByFacility.values()) {
      boolean resourceWithOuSet = false;
      for (Resource resource : setOfResourcesWithSameFacilityId) {
        try {
          Attribute resourceAdOuName =
              sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_D_AD_OU_NAME);
          if (resourceAdOuName.getValue() != null) {
            if (resourceWithOuSet) {
              throw new WrongReferenceAttributeValueException(attribute, resourceAdOuName, group, null, resource, null,
                  "There is more than one resource where group is assigned and OU is also set there (on the same " +
                  "facility)!");
            } else {
              resourceWithOuSet = true;
            }
          }
        } catch (AttributeNotExistsException ex) {
          throw new ConsistencyErrorException(ex);
        }
      }
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    //Attribute can be null
    if (attribute.getValue() == null) {
      return;
    }

    if (!pattern.matcher(attribute.valueAsString()).matches()) {
      throw new WrongAttributeValueException(attribute,
          "Invalid attribute adName value. It should contain only letters, digits, hyphens, underscores or dots.");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("adName:o365mu");
    attr.setDisplayName("AD Name in o365mu");
    attr.setType(String.class.getName());
    attr.setUnique(true);
    attr.setDescription("Name of AD in o365mu namespace");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(A_R_D_AD_OU_NAME);
    return dependencies;
  }
}
