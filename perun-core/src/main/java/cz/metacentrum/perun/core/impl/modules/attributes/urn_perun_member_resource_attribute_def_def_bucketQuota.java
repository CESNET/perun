package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;

public class urn_perun_member_resource_attribute_def_def_bucketQuota  extends MemberResourceAttributesModuleAbstract
    implements MemberResourceAttributesModuleImplApi {
  public static final String A_R_maxUserBucketQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":maxUserBucketQuota";

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Resource resource,
                                      Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    //attribute can be null, it means there are no settings for member on resource
    if (attribute.getValue() == null) {
      return;
    }

    Pair<Integer, Integer> bucketQuotaForMember;
    try {
      bucketQuotaForMember =
          perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(attribute, resource, member);
    } catch (WrongAttributeValueException ex) {
      throw new WrongReferenceAttributeValueException(attribute, ex.getMessage());
    }

    //If there are no values after converting quota, we can skip testing against max quotas attribute, because
    // there is nothing to check
    if (bucketQuotaForMember == null) {
      return;
    }

    Attribute maxUserBucketQuotaAttribute;
    try {
      maxUserBucketQuotaAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession,
          resource, A_R_maxUserBucketQuota);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }

    Pair<Integer, Integer> maxUserBucketQuotaForResource;
    try {
      maxUserBucketQuotaForResource =
          perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(maxUserBucketQuotaAttribute,
              resource, null);
    } catch (WrongAttributeValueException ex) {
      throw new WrongReferenceAttributeValueException(attribute, maxUserBucketQuotaAttribute, resource, member,
          resource, null,
          "Can't set bucketQuota for member and resource, because maxUserBucketQuota is not in correct format. Please" +
              " fix it first!", ex);
    }
    // individually check whether soft/hard quota of the member are within maximum quota
    if (maxUserBucketQuotaForResource.getLeft().compareTo(0) != 0) {
      if (bucketQuotaForMember.getLeft().compareTo(0) == 0 ||
          bucketQuotaForMember.getLeft().compareTo(maxUserBucketQuotaForResource.getLeft()) > 0) {
        throw new WrongReferenceAttributeValueException(attribute, maxUserBucketQuotaAttribute, resource, member,
          resource, null, "Bucket soft quota is not in the range of maxUserBucketQuota.");
      }
    }

    if (maxUserBucketQuotaForResource.getRight().compareTo(0) != 0) {
      if (bucketQuotaForMember.getRight().compareTo(0) == 0 ||
          bucketQuotaForMember.getRight().compareTo(maxUserBucketQuotaForResource.getRight()) > 0) {
        throw new WrongReferenceAttributeValueException(attribute, maxUserBucketQuotaAttribute, resource, member,
          resource, null, "Bucket hard quota is not in the range of maxUserBucketQuota.");
      }
    }
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(attribute, resource, member);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("bucketQuota");
    attr.setDisplayName("Bucket quota");
    attr.setType(String.class.getName());
    attr.setDescription(
        "The quota in format 'SoftQuota:HardQuota'. Example: '1000:2000'.");
    return attr;
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(A_R_maxUserBucketQuota);
    return dependencies;
  }
}
