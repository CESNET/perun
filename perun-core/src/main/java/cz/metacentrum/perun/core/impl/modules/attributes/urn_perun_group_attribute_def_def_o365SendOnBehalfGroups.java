package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.ArrayList;

/**
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_group_attribute_def_def_o365SendOnBehalfGroups extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    for (String groupId : attribute.valueAsList()) {
      try {
        Integer.valueOf(groupId);
      } catch (NumberFormatException ex) {
        throw new WrongAttributeValueException(groupId + " is not a correct subgroup id.");
      }
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    sess.getPerunBl().getModulesUtilsBl().checkAttributeValueIsIncludedOrSubgroupId(sess, group, attribute);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("o365SendOnBehalfGroups");
    attr.setDisplayName("O365 Send on behalf of Groups");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("List of subgroups and included groups with rights to send on behalf of.");
    return attr;
  }
}
