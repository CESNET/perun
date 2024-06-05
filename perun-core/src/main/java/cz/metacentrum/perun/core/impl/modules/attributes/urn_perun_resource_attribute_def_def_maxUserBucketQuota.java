package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

public class urn_perun_resource_attribute_def_def_maxUserBucketQuota extends ResourceAttributesModuleAbstract implements
    ResourceAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    //attribute can be null, it means there are no max user settings on resource
    if (attribute.getValue() == null) {
      return;
    }
    perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(attribute, resource, null);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("maxUserBucketQuota");
    attr.setDisplayName("Bucket quota (max)");
    attr.setType(String.class.getName());
    attr.setDescription("Maximum bucket quota for each user on this resource. " +
                        "The quota is in format 'SoftQuota:HardQuota'. Example:" +
                        " '1000:2000'.");
    return attr;
  }
}
