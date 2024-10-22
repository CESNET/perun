package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

public class urn_perun_resource_attribute_def_def_blockBucketCreation extends
    ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("blockBucketCreation");
    attr.setDisplayName("Block bucket creation");
    attr.setType(Boolean.class.getName());
    attr.setDescription("Disables creation of new buckets on the resource.");
    return attr;
  }
}


