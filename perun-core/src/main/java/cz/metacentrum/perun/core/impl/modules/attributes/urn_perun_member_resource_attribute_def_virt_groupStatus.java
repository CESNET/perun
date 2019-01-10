package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get value for attribute as unified result of MemberGroupStatus for specified member and resource.
 *
 * If member is VALID in at least one group assigned to the resource, result is VALID.
 * If member is not VALID in any of groups assigned to the resource, result is EXPIRED.
 * If member is not assigned to the resource at all, result is NULL.
 *
 * MemberGroupStatus is never related to the members status in a VO as a whole!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_member_resource_attribute_def_virt_groupStatus extends MemberResourceVirtualAttributesModuleAbstract implements MemberResourceVirtualAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_member_resource_attribute_def_virt_groupStatus.class);

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		String status = (String) attribute.getValue();

		if (status == null) return; // NULL is ok

		if (!"VALID".equals(status) && !"EXPIRED".equals(status)) throw new WrongAttributeValueException("Group status of member can be only 'VALID' or 'EXPIRED', not '"+status+"'");

	}

	@Override
    public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException {

		Attribute attribute = new Attribute(attributeDefinition);
		MemberGroupStatus result = sess.getPerunBl().getMembersManagerBl().getUnifiedMemberGroupStatus(sess, member, resource);
		attribute.setValue((result != null) ? result.toString() : null);
        return attribute;

    }

	@Override
    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
        attr.setFriendlyName("groupStatus");
        attr.setDisplayName("Group membership status");
        attr.setType(String.class.getName());
        attr.setDescription("Whether member is ACTIVE or EXPIRED in all groups assigned to the resource.");
        return attr;
    }

}
