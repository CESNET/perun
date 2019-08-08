package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberGroupAttributesModuleImplApi;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_member_group_attribute_def_def_groupMembershipExpiration extends MemberGroupAttributesModuleAbstract implements MemberGroupAttributesModuleImplApi {
	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Group group, Attribute attribute) throws WrongAttributeValueException {
		String membershipExpTime = (String) attribute.getValue();

		if(membershipExpTime == null) return; // NULL is ok

		Date testDate;

		try {
			testDate = BeansUtils.getDateFormatterWithoutTime().parse(membershipExpTime);

		} catch (ParseException ex) {

			throw new WrongAttributeValueException(attribute, "Date parsing failed", ex);
		}

		if (!BeansUtils.getDateFormatterWithoutTime().format(testDate).equals(membershipExpTime)) {

			throw new WrongAttributeValueException(attribute, "Wrong format yyyy-MM-dd expected.");
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Group group, Attribute attribute) throws InternalErrorException {
		String value = null;
		if(attribute.getValue() != null) value = (String) attribute.getValue();
		//If there is some value and member is in status expired or disabled

		// find out members status in given group
		MemberGroupStatus status = session.getPerunBl().getGroupsManagerBl().getDirectMemberGroupStatus(session, member, group);
		// status is null if member is not direct at the time -> changing value doesn't affect indirect membership
		if(value != null && MemberGroupStatus.EXPIRED.equals(status)) {
			Date expirationDate;
			try {
				expirationDate = BeansUtils.getDateFormatterWithoutTime().parse(value);
			} catch (ParseException ex) {
				throw new InternalErrorException("Date parsing failed in setHook, even if parsing in checkMethod was correct.", ex);
			}
			Date date = new Date();
			if(expirationDate.compareTo(date) > 0) {
				session.getPerunBl().getGroupsManagerBl().validateMemberInGroup(session, member, group);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
		attr.setFriendlyName("groupMembershipExpiration");
		attr.setDisplayName("Group membership expiration");
		attr.setType(String.class.getName());
		attr.setDescription("When the member expires in group, format YYYY-MM-DD.");
		return attr;
	}
}
