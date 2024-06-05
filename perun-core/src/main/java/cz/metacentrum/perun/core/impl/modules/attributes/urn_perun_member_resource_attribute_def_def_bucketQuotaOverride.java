package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleImplApi;

public class urn_perun_member_resource_attribute_def_def_bucketQuotaOverride
    extends MemberResourceAttributesModuleAbstract implements MemberResourceAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    // attribute can be null, it means there are no override settings
    if (attribute.getValue() == null) {
      return;
    }

    // Check if quota has right settings (softQuota<=hardQuota)
    perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(attribute, resource, member);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("bucketQuotaOverride");
    attr.setDisplayName("Bucket quota (override)");
    attr.setType(String.class.getName());
    attr.setDescription("Override has the highest priority for setting bucket quota of member on resource. " +
                        "The quota in format 'SoftQuota:HardQuota'. Example:" +
                        " '1000:2000'.");
    return attr;
  }
}
