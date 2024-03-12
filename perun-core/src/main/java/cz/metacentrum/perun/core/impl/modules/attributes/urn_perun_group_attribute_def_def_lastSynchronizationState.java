package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

/**
 * Last synchronization state module
 * <p>
 * If group is synchronized, there will be information about state of last synchronization. If everything is ok,
 * information will be 'OK'. If there is some error, there will be text of an error.
 * <p>
 * If group has never been synchronized, this attribute will be empty.
 *
 * @author Michal Stava  stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_def_lastSynchronizationState extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("lastSynchronizationState");
    attr.setDisplayName("Last synchronization State");
    attr.setType(String.class.getName());
    attr.setDescription("If group is synchronized, there will be information about state of last synchronization.");
    return attr;
  }
}
