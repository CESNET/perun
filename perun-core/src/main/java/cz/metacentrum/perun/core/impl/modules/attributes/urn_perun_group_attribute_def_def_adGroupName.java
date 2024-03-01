package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import java.util.List;
import java.util.regex.Pattern;

public class urn_perun_group_attribute_def_def_adGroupName extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {

  private static final Pattern pattern = Pattern.compile("[A-Za-z0-9_-]|([A-Za-z0-9_-][A-Za-z0-9 _-]*[A-Za-z0-9_-])");
  private static final String A_R_D_AD_RESOURCE_REPRESENTATION =
      AttributesManager.NS_RESOURCE_ATTR_DEF + ":adResourceRepresentation";
  private static final String A_G_D_AD_GROUP_NAME = AttributesManager.NS_GROUP_ATTR_DEF + ":adGroupName";

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    //Attribute can be null
    if (attribute.getValue() == null) {
      return;
    }

    if (!pattern.matcher(attribute.valueAsString()).matches()) {
      throw new WrongAttributeValueException(attribute,
          "Invalid attribute adGroupName value. It should contain only letters, digits, underscores, dashes or spaces.");
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
    //Attribute can be null
    if (attribute.getValue() == null) {
      return;
    }

    List<Resource> assignedResources = sess.getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);

    for (Resource assignedResource : assignedResources) {
      try {
        Attribute resourceAdRepresentation = sess.getPerunBl().getAttributesManagerBl()
            .getAttribute(sess, assignedResource, A_R_D_AD_RESOURCE_REPRESENTATION);

        if ("tree".equals(resourceAdRepresentation.valueAsString())) {
          if (!groupHasUniqueName(sess, group, attribute)) {
            throw new WrongReferenceAttributeValueException(attribute,
                "There already exists a group with the same name and same hierarchy!");
          }

          return;
        }
      } catch (AttributeNotExistsException e) {
        //We can skip this case
      }
    }
  }

  /**
   * Checks uniqueness of adGroupName by comparing it to all adGroupNames of subgroups of parent group.
   *
   * @param sess      perun session
   * @param group     group
   * @param attribute attribute
   * @return true if the name is unique, false otherwise
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException
   */
  private boolean groupHasUniqueName(PerunSessionImpl sess, Group group, Attribute attribute)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    if (group.getParentGroupId() != null) {
      try {
        Group parentGroup = sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, group);

        List<Group> subGroups = sess.getPerunBl().getGroupsManagerBl().getSubGroups(sess, parentGroup);
        subGroups.remove(group);

        for (Group subGroup : subGroups) {
          Attribute nameAttribute =
              sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, subGroup, A_G_D_AD_GROUP_NAME);
          String name = nameAttribute.valueAsString();
          if (attribute.valueAsString().equals(name)) {
            return false;
          }
        }
      } catch (ParentGroupNotExistsException e) {
        throw new ConsistencyErrorException(e);
      }
    }

    return true;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("adGroupName");
    attr.setDisplayName("AD Group Name");
    attr.setType(String.class.getName());
    attr.setDescription("AD group name which is used to compose full name of the group in AD.");
    return attr;
  }
}
