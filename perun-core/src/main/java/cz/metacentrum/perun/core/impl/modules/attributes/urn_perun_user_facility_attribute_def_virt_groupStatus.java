package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleImplApi;

/**
 * Get value for attribute as unified result of MemberGroupStatus for specified user and facility.
 *
 * If user is VALID in at least one group assigned to at least one resource on facility, result is VALID.
 * If user is not VALID in any of groups assigned to any of resources, result is EXPIRED.
 * If user is not assigned to the facility at all, result is NULL.
 *
 * MemberGroupStatus is never related to the members status in any VO!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_facility_attribute_def_virt_groupStatus extends UserFacilityVirtualAttributesModuleAbstract implements UserFacilityVirtualAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws WrongAttributeValueException {

		String status = attribute.valueAsString();

		if (status == null) return; // NULL is ok

		if (!"VALID".equals(status) && !"EXPIRED".equals(status)) throw new WrongAttributeValueException("Group status of member can be only 'VALID' or 'EXPIRED', not '"+status+"'");

	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);
		MemberGroupStatus result = sess.getPerunBl().getMembersManagerBl().getUnifiedMemberGroupStatus(sess, user, facility);
		attribute.setValue((result != null) ? result.toString() : null);
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setFriendlyName("groupStatus");
		attr.setDisplayName("Group membership status");
		attr.setType(String.class.getName());
		attr.setDescription("Whether user is VALID or EXPIRED in all groups assigned to the facility.");
		return attr;
	}
}

