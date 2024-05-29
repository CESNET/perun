package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.util.ArrayList;
import java.util.List;


@SkipValueCheckDuringDependencyCheck
public class urn_perun_member_resource_attribute_def_virt_bucketQuota extends
    MemberResourceVirtualAttributesModuleAbstract {
  public static final String A_R_defaultBucketQuota = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultBucketQuota";
  public static final String A_MR_bucketQuota = AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":bucketQuota";
  public static final String A_MR_bucketQuotaOverride =
      AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":bucketQuotaOverride";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
    attr.setFriendlyName("bucketQuota");
    attr.setDisplayName("Bucket quota");
    attr.setType(String.class.getName());
    attr.setDescription(
        "The quota in format 'SoftQuota:HardQuota' Example: '100:200'. Taken from the quota override and regular " +
            "quota attributes, if none are present, default quota is filled in.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Resource resource,
                                     AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);

    // Get values of all the dependant attributes in the priority order, return of the value is not null
    Pair<Integer, Integer> memberTransferredQuotaOverride = null;
    Attribute memberQuotaOverride;
    try {
      memberQuotaOverride = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, member, resource,
          A_MR_bucketQuotaOverride);
    } catch (AttributeNotExistsException | MemberResourceMismatchException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }

    if (memberQuotaOverride != null && memberQuotaOverride.getValue() != null) {
      try {
        memberTransferredQuotaOverride =
            sess.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(memberQuotaOverride,
            resource, member);
      } catch (WrongAttributeValueException ex) {
        throw new ConsistencyErrorException("Override quotas for member " + member + " are in bad format.", ex);
      }
    }
    if (memberTransferredQuotaOverride != null) {
      attribute.setValue(memberTransferredQuotaOverride.getLeft() + ":" + memberTransferredQuotaOverride.getRight());
      return attribute;
    }

    Pair<Integer, Integer> memberTransferredQuota = null;
    Attribute memberQuota;
    try {
      memberQuota = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, member, resource, A_MR_bucketQuota);
    } catch (AttributeNotExistsException | MemberResourceMismatchException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }

    if (memberQuota != null && memberQuota.getValue() != null) {
      try {
        memberTransferredQuota = sess.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(memberQuota,
            resource, member);
      } catch (WrongAttributeValueException ex) {
        throw new ConsistencyErrorException("Quotas for member " + member + " are in bad format.", ex);
      }
    }

    if (memberTransferredQuota != null) {
      attribute.setValue(memberTransferredQuota.getLeft() + ":" + memberTransferredQuota.getRight());
      return attribute;
    }

    Pair<Integer, Integer> resourceTransferredQuota = null;
    Attribute resourceQuota;
    try {
      resourceQuota = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, A_R_defaultBucketQuota);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }

    if (resourceQuota != null && resourceQuota.getValue() != null) {
      try {
        resourceTransferredQuota = sess.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(resourceQuota,
            resource, null);
      } catch (WrongAttributeValueException ex) {
        throw new ConsistencyErrorException("Quotas on resource " + resource + " are in bad format.", ex);
      }
    }

    if (resourceTransferredQuota != null) {
      attribute.setValue(resourceTransferredQuota.getLeft() + ":" + resourceTransferredQuota.getRight());
      return attribute;
    }

    attribute.setValue(null);
    return attribute;
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> strongDependencies = new ArrayList<>();
    strongDependencies.add(A_R_defaultBucketQuota);
    strongDependencies.add(A_MR_bucketQuota);
    strongDependencies.add(A_MR_bucketQuotaOverride);
    return strongDependencies;
  }
}
