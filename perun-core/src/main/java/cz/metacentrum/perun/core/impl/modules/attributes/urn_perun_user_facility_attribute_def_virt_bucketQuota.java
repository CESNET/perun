package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;
import java.util.ArrayList;
import java.util.List;

@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_facility_attribute_def_virt_bucketQuota extends
    UserFacilityVirtualAttributesModuleAbstract {
  public static final String A_MR_V_bucketQuota = AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT + ":bucketQuota";

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    attr.setFriendlyName("bucketQuota");
    attr.setDisplayName("Computed bucket quota for a user on a facility");
    attr.setType(String.class.getName());
    attr.setDescription(
        "The quota in format 'SoftQuota:HardQuota' Example: '100:200'. Is counted from all member-resource and " +
            "resource settings of the user on " +
        "the facility.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility,
                                     AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);
    List<Resource> allowedResources =
        sess.getPerunBl().getResourcesManagerBl().getAllowedResources(sess, facility, user);

    Integer softQuotaSum = null;
    Integer hardQuotaSum = null;
    for (Resource resource : allowedResources) {
      //get allowed member of this user on this resource (using his VO)
      Vo membersVo;
      try {
        membersVo = sess.getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId());
      } catch (VoNotExistsException ex) {
        throw new ConsistencyErrorException("Vo should exists, because resource with this id exists " + resource);
      }
      Member memberOnResource;
      try {
        memberOnResource = sess.getPerunBl().getMembersManagerBl().getMemberByUser(sess, membersVo, user);
      } catch (MemberNotExistsException ex) {
        throw new ConsistencyErrorException(
            "User should have member in this VO, because he was listed in allowed assigned resources " + user + ", " +
                membersVo + " , " + resource);
      }

      // get the member resource attribute and its value for the user
      Pair<Integer, Integer> memberResourceFinalBucketQuota = null;
      Attribute memberResourceFinalBucketQuotaAttribute;
      try {
        memberResourceFinalBucketQuotaAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess,
            memberOnResource, resource, A_MR_V_bucketQuota);
      } catch (MemberResourceMismatchException | WrongAttributeAssignmentException ex) {
        throw new InternalErrorException(ex);
      } catch (AttributeNotExistsException ex) {
        throw new ConsistencyErrorException(ex);
      }
      if (memberResourceFinalBucketQuotaAttribute.getValue() != null) {
        try {
          memberResourceFinalBucketQuota =
            sess.getPerunBl().getModulesUtilsBl().checkAndTransferBucketQuota(memberResourceFinalBucketQuotaAttribute,
                resource, memberOnResource);
        } catch (WrongAttributeValueException ex) {
          throw new ConsistencyErrorException(
              "Final counted quotas on " + resource + " for member " + memberOnResource + " are in bad format.", ex);
        }
      }
      if (memberResourceFinalBucketQuota != null) {
        // add the member resource value to the sum
        if (softQuotaSum == null || memberResourceFinalBucketQuota.getLeft().compareTo(0) == 0) {
          // initialize sum for the first time and always set to 0 if some quota is 0 (unlimited)
          softQuotaSum = memberResourceFinalBucketQuota.getLeft();
        } else if (softQuotaSum.compareTo(0) != 0) {
          // add only if any other quota hasn't been set to 0 (unlimited)
          softQuotaSum += memberResourceFinalBucketQuota.getLeft();
        }

        if (hardQuotaSum == null || memberResourceFinalBucketQuota.getRight().compareTo(0) == 0) {
          // initialize sum for the first time and always set to 0 if some quota is 0 (unlimited)
          hardQuotaSum = memberResourceFinalBucketQuota.getRight();
        }  else if (hardQuotaSum.compareTo(0) != 0) {
          // add only if any other quota hasn't been set to 0 (unlimited)
          hardQuotaSum += memberResourceFinalBucketQuota.getRight();
        }
      }
    }
    // if no member resource attribute was filled, sums are still null, return null
    if (softQuotaSum == null) {
      attribute.setValue(null);
      return attribute;
    }
    attribute.setValue(softQuotaSum + ":" + hardQuotaSum);
    return attribute;
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> strongDependencies = new ArrayList<>();
    strongDependencies.add(A_MR_V_bucketQuota);
    return strongDependencies;
  }
}
