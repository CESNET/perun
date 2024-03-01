package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute value of all groups assigned to the Resource and return list of their names. Name of group in the list is
 * like '[VO_SHORT_NAME]:[PARENT_GROUP_NAME]:[GROUP_NAME]'
 * <p>
 * If there is attribute 'resource:def:isAssignedWithSubgroups' set to true on the Resource, it will add all subgroups
 * of assigned groups too.
 *
 * @author Michal Stava &lt;stavamichal@gmail.com&gt;
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_resource_attribute_def_virt_assignedGroups extends ResourceVirtualAttributesModuleAbstract
    implements ResourceVirtualAttributesModuleImplApi {

  private static final Logger LOG = LoggerFactory.getLogger(urn_perun_resource_attribute_def_virt_assignedGroups.class);
  private static final String A_R_isAssignedWithSubgroups =
      AttributesManager.NS_RESOURCE_ATTR_DEF + ":isAssignedWithSubgroups";

  /**
   * Return group name like '[VO_SHORT_NAME]:[GROUP_NAME]'
   *
   * @param vo    to get short name from
   * @param group to get name from
   * @return computed group name
   */
  private String computeGroupName(Vo vo, Group group) {
    return vo.getShortName() + ":" + group.getName();
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_VIRT);
    attr.setFriendlyName("assignedGroups");
    attr.setDisplayName("List of all assigned groups");
    attr.setType(ArrayList.class.getName());
    attr.setDescription(
        "Compute list of all names of assigned groups (optionally also with all their subgroups - if true is set in " +
        "'def:resource:isAssignedWithSubgroups' attribute).");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource,
                                     AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);
    Set<String> allUniqueGroupNames = new HashSet<>();

    Vo vo = sess.getPerunBl().getResourcesManagerBl().getVo(sess, resource);
    List<Group> assignedGroups = sess.getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);

    boolean isAssignedWithSubgroups;
    try {
      Attribute isAssignedWithSubgroupsAttribute =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_isAssignedWithSubgroups);
      if (isAssignedWithSubgroupsAttribute.getValue() == null) {
        isAssignedWithSubgroups = false;
      } else {
        isAssignedWithSubgroups = isAssignedWithSubgroupsAttribute.valueAsBoolean();
      }
    } catch (AttributeNotExistsException ex) {
      LOG.debug("There is missing definition of attribute {}", A_R_isAssignedWithSubgroups);
      isAssignedWithSubgroups = false;
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }

    for (Group assignedGroup : assignedGroups) {
      allUniqueGroupNames.add(computeGroupName(vo, assignedGroup));
      if (isAssignedWithSubgroups) {
        for (Group subgroup : sess.getPerunBl().getGroupsManagerBl().getAllSubGroups(sess, assignedGroup)) {
          allUniqueGroupNames.add(computeGroupName(vo, subgroup));
        }
      }
    }

    List<String> allGroupNames = new ArrayList<>(allUniqueGroupNames);
    Collections.sort(allGroupNames);
    attribute.setValue(allGroupNames);

    return attribute;
  }
}
