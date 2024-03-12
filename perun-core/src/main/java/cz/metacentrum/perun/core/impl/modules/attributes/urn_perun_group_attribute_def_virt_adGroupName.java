package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

public class urn_perun_group_attribute_def_virt_adGroupName extends GroupVirtualAttributesModuleAbstract
    implements GroupVirtualAttributesModuleImplApi {

  private static final String SOURCE_ATTR_NAME = AttributesManager.NS_GROUP_ATTR_DEF + ":adGroupName";
  private static final String DELIMITER = "-";

  /**
   * Recursively fetch adGroupName value from group and its parent groups Result names are sorted from root group name
   * to the leaf group name.
   *
   * @param sess  Perun Session
   * @param group for which we will be fetching values
   * @return List of adGroupName values from group and its parent groups
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   */
  private List<String> fetchAdGroupNamesFromGroupAndItsParentGroups(PerunSessionImpl sess, Group group)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    List<String> resultList = new ArrayList<>();
    AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
    Attribute adGroupName = am.getAttribute(sess, group, SOURCE_ATTR_NAME);
    String value = adGroupName.valueAsString();

    if (group.getParentGroupId() != null) {
      try {
        Group parentGroup = sess.getPerunBl().getGroupsManagerBl().getParentGroup(sess, group);
        resultList.addAll(fetchAdGroupNamesFromGroupAndItsParentGroups(sess, parentGroup));
      } catch (ParentGroupNotExistsException e) {
        throw new ConsistencyErrorException(e);
      }
    }

    resultList.add(value);
    return resultList;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_VIRT);
    attr.setFriendlyName("adGroupName");
    attr.setDisplayName("Composed AD Group Name");
    attr.setType(String.class.getName());
    attr.setDescription("AD group name, which is composed from all def AD group names of this group and its parents.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Group group, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);

    try {
      List<String> adGroupNames = fetchAdGroupNamesFromGroupAndItsParentGroups(sess, group);
      if (adGroupNames.contains(null)) {
        attribute.setValue(null);
      } else {
        attribute.setValue(String.join(DELIMITER, adGroupNames));
      }
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      attribute.setValue(null);
    }
    return attribute;
  }
}
