package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;

import java.util.List;


/**
 * @author Kristyna Kysela
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_member_group_attribute_def_virt_isGroupAdmin extends MemberGroupVirtualAttributesModuleAbstract implements MemberGroupVirtualAttributesModuleImplApi {

    @Override
    public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attributeDefinition) {

        List<User> userAdminList;
        User newUser;
        Attribute attribute = new Attribute(attributeDefinition);

        userAdminList = sess.getPerunBl().getGroupsManagerBl().getAdmins(sess, group, false);
        newUser = sess.getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

        if (userAdminList.contains(newUser)) {
            attribute.setValue(true);
            return attribute;
        }

        attribute.setValue(false);
        return attribute;

    }

    @Override
    public boolean setAttributeValue(PerunSessionImpl sess, Member member, Group group, Attribute attribute) {
        return false;
    }

    @Override
    public boolean removeAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) {
        return false;
    }

    @Override
    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
        attr.setFriendlyName("isGroupAdmin");
        attr.setDisplayName("Is group admin");
        attr.setType(Boolean.class.getName());
        attr.setDescription("Is group admin.");
        return attr;
    }



}
