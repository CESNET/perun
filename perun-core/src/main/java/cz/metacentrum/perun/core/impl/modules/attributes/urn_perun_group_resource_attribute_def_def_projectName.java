package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupResourceAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for project name
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 25.2.2014
 */
public class urn_perun_group_resource_attribute_def_def_projectName extends GroupResourceAttributesModuleAbstract
    implements GroupResourceAttributesModuleImplApi {

  private static final String A_R_projectsBasePath = AttributesManager.NS_RESOURCE_ATTR_DEF + ":projectsBasePath";
  private static final String A_GR_projectName = AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":projectName";
  private static final Pattern pattern = Pattern.compile("^[-_a-zA-Z0-9]+$");

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    String name = attribute.valueAsString();
    if (name == null) {
      return;
    }

    //Prepare this resource projectsBasePath
    Attribute thisResourceProjectsBasePath;
    try {
      thisResourceProjectsBasePath =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_projectsBasePath);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException("Attribute projectBasePath not exists!", ex);
    }

    //Prepare value of this resource projectsBasePath
    String thisResourceProjectBasePathValue;
    if (thisResourceProjectsBasePath.getValue() != null) {
      thisResourceProjectBasePathValue = thisResourceProjectsBasePath.valueAsString();
    } else {
      throw new WrongReferenceAttributeValueException(attribute, thisResourceProjectsBasePath, group, resource,
          resource, null,
          "Resource must have set projectsBasePath if attribute projectName for it's group need to be set.");
    }

    //Get All Resources with the same project_base_path
    Facility facility = sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
    List<Resource> resources = sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
    resources.remove(resource);

    //Remove all resources which has other
    Iterator<Resource> iterator = resources.iterator();
    while (iterator.hasNext()) {
      Resource r = iterator.next();
      Attribute otherResourceProjectsBasePath;
      try {
        otherResourceProjectsBasePath =
            sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, r, A_R_projectsBasePath);
      } catch (AttributeNotExistsException ex) {
        throw new ConsistencyErrorException("Attribute projectBasePath not exists!", ex);
      }

      if (otherResourceProjectsBasePath.getValue() != null) {
        String otherResourceProjectsBasePathValue = otherResourceProjectsBasePath.valueAsString();
        if (!thisResourceProjectBasePathValue.equals(otherResourceProjectsBasePathValue)) {
          iterator.remove();
        }
      } else {
        //If projectsBasePath is null, also remove resource
        iterator.remove();
      }
    }

    //For all resources with the same project_base_path look for groups with the same projectName
    for (Resource r : resources) {
      List<Group> groups = sess.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, r);
      //Our group may also be part of assigned Group, so it needs to be removed
      groups.remove(group);
      for (Group g : groups) {
        Attribute groupProjectName;
        try {
          groupProjectName = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, r, g, A_GR_projectName);
        } catch (AttributeNotExistsException ex) {
          throw new ConsistencyErrorException("Attribute projectName not exists!", ex);
        } catch (GroupResourceMismatchException ex) {
          throw new InternalErrorException(ex);
        }

        String groupProjectNameValue = null;
        if (groupProjectName.getValue() != null) {
          groupProjectNameValue = groupProjectName.valueAsString();
        }

        //If the name is somewhere same, exception must be thrown
        if (name.equals(groupProjectNameValue)) {
          throw new WrongReferenceAttributeValueException(attribute, groupProjectName, group, resource, g, r,
              "Group " + group + " and " + g + " have the same projectName in the same projectsBasePath.");
        }
      }
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    String name = attribute.valueAsString();
    if (name == null) {
      return;
    }

    Matcher match = pattern.matcher(name);

    if (!match.matches()) {
      throw new WrongAttributeValueException(attribute, group, resource,
          "Bad format of attribute projectName (expected something like 'project_name-24').");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("projectName");
    attr.setDisplayName("Project name");
    attr.setType(String.class.getName());
    attr.setDescription("Name of project, directory where the project exists.");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(A_GR_projectName);
    dependencies.add(A_R_projectsBasePath);
    return dependencies;
  }
}
