package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns if member is INDIRECT for specified member and group
 *
 * @author Matej Hakoš <492968@muni.cz>
 */
public class urn_perun_member_group_attribute_def_virt_groupStatusIndirect
    extends MemberGroupVirtualAttributesModuleAbstract implements MemberGroupVirtualAttributesModuleImplApi {

  static final Logger LOG =
      LoggerFactory.getLogger(urn_perun_member_group_attribute_def_virt_groupStatusIndirect.class);

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
    attr.setFriendlyName("groupStatusIndirect");
    attr.setDisplayName("Member group indirect status");
    attr.setType(Boolean.class.getName());
    attr.setDescription("Whether member is INDIRECT in a group.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) {
    Attribute newAttribute = new Attribute(attribute);
    try {
      Member retrievedMember = sess.getPerunBl().getGroupsManagerBl().getGroupMemberById(sess, group, member.getId());
      boolean indirect = retrievedMember.getMembershipType() == MembershipType.INDIRECT;
      newAttribute.setValue(indirect);
      return newAttribute;
    } catch (NotGroupMemberException e) {
      LOG.warn("{} is not member of a {} when retrieving member_group:virt:groupStatusIndirect attribute.", member,
          group);
    }
    return newAttribute;

  }

}
