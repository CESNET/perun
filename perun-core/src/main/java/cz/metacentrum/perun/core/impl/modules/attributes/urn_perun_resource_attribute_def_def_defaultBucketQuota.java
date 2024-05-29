package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

public class urn_perun_resource_attribute_def_def_defaultBucketQuota extends ResourceAttributesModuleAbstract implements
    ResourceAttributesModuleImplApi {
  public static final String A_R_maxUserBucketQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":maxUserBucketQuota";


  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    //attribute can be null, it means there are no default settings on resource
    if (attribute.getValue() == null) {
      return;
    }

    Pair<Integer, Integer> defaultBucketQuotasForResource;
    try {
      defaultBucketQuotasForResource =
          perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(attribute, resource, null);
    } catch (WrongAttributeValueException ex) {
      throw new ConsistencyErrorException("Final counted quotas on " + resource + " are in bad format.", ex);
    }

    Attribute maxUserBucketQuotaAttribute;
    try {
      maxUserBucketQuotaAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession,
          resource, A_R_maxUserBucketQuota);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }

    Pair<Integer, Integer> maxUserBucketQuotasForResource;
    try {
      maxUserBucketQuotasForResource =
          perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(maxUserBucketQuotaAttribute,
              resource, null);
    } catch (WrongAttributeValueException ex) {
      throw new WrongReferenceAttributeValueException(attribute, maxUserBucketQuotaAttribute, resource, null, resource,
          null,
          "Can't set defaultBucketQuota for resource, because maxUserBucketQuota is not in correct format. Please " +
              "fix it first!", ex);
    }
    // individually check whether soft/hard quotas of the resource are within maximum quota
    if (maxUserBucketQuotasForResource.getLeft().compareTo(0) != 0) {
      if (defaultBucketQuotasForResource.getLeft().compareTo(0) == 0 ||
          defaultBucketQuotasForResource.getLeft().compareTo(maxUserBucketQuotasForResource.getLeft()) > 0) {
        throw new WrongReferenceAttributeValueException(attribute, maxUserBucketQuotaAttribute, resource, null,
          resource, null, "Default bucket soft quota is not in the range of maxUserBucketQuota.");
      }
    }

    if (maxUserBucketQuotasForResource.getRight().compareTo(0) != 0) {
      if (defaultBucketQuotasForResource.getRight().compareTo(0) == 0 ||
          defaultBucketQuotasForResource.getRight().compareTo(maxUserBucketQuotasForResource.getRight()) > 0) {
        throw new WrongReferenceAttributeValueException(attribute, maxUserBucketQuotaAttribute, resource, null,
          resource, null, "Default bucket hard quota is not in the range of maxUserBucketQuota.");
      }
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }
    perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(attribute, resource, null);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("defaultBucketQuota");
    attr.setDisplayName("Bucket quota (default)");
    attr.setType(String.class.getName());
    attr.setDescription("Default bucket quota for each user on this resource. Is used whenever other bucket " +
                            "quota attributes aren't set. The quota is in format 'SoftQuota:HardQuota'." +
                            " Example: '1000:2000'.");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(A_R_maxUserBucketQuota);
    return dependencies;
  }
}


