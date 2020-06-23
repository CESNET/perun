package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns member-group status (VALID/EXPIRED) for specified member and group
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_member_group_attribute_def_virt_groupStatus extends MemberGroupVirtualAttributesModuleAbstract implements MemberGroupVirtualAttributesModuleImplApi {

	final static Logger log = LoggerFactory.getLogger(urn_perun_member_group_attribute_def_virt_groupStatus.class);

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Group group, Attribute attribute) throws WrongAttributeValueException {

		String status = attribute.valueAsString();

		if (status == null) return; // NULL is ok

		if (!"VALID".equals(status) && !"EXPIRED".equals(status)) throw new WrongAttributeValueException("Group status of member can be only 'VALID' or 'EXPIRED', not '"+status+"'");

	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) {

		// Source member object can't be trusted to hold correct group membership status, since we don't know in which context
		// it was originally retrieved. Hence we get member of a group once more.
		Attribute newAttribute = new Attribute(attribute);
		try {
			Member retrievedMember = sess.getPerunBl().getGroupsManagerBl().getGroupMemberById(sess, group, member.getId());
			MemberGroupStatus result  = retrievedMember.getGroupStatus();
			newAttribute.setValue((result != null) ? result.toString() : null);
			return newAttribute;

		} catch (NotGroupMemberException e) {
			log.warn("{} is not member of a {} when retrieving member_group:virt:groupStatus attribute.", member, group);
		}
		return newAttribute;

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		attr.setFriendlyName("groupStatus");
		attr.setDisplayName("Group membership status");
		attr.setType(String.class.getName());
		attr.setDescription("Whether member is VALID or EXPIRED in a group.");
		return attr;
	}

}
