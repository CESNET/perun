package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;

import java.util.List;


/**
 * @author Kristyna Kysela
 */
public class urn_perun_member_group_attribute_def_virt_isGroupAdmin extends MemberGroupVirtualAttributesModuleAbstract implements MemberGroupVirtualAttributesModuleImplApi {

    public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException {

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

    public boolean setAttributeValue(PerunSessionImpl sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException{
        return false;
    }

    public boolean removeAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException{
        return false;
    }

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
