package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Checks and fills at specified membership expiration
 *
 * @date 12.4.2012 13:05:00
 * @author Michal Šťava   <stava.michal@gmail.com>
 */
public class urn_perun_member_attribute_def_def_membershipExpiration extends MemberAttributesModuleAbstract implements MemberAttributesModuleImplApi {

	@Override
	/**
	 * Checks if the corresponding attribute um:membershipExpiration is null or
	 * matches with regular expression yyyy-MM-dd
	 */
	public void checkAttributeValue(PerunSessionImpl perunSession, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {

		String membershipExpTime = (String) attribute.getValue();

		if(membershipExpTime == null) return; // NULL is ok

		Date testDate = null;

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
	/**
	 * Fill membership expiration time.
	 * If membership starts from Janury to September, time will be the last day of starting year,
	 * if membership start from October, to December, time will be the last day of next year.
	 */
	public Attribute fillAttribute(PerunSessionImpl perunSession, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		/*Attribute ret = new Attribute(attribute);
			Calendar now = Calendar.getInstance();
			int currentMonth = now.get(Calendar.MONTH);
			int currentYear = now.get(Calendar.YEAR);

			if(currentMonth>8) currentYear++;
			ret.setValue(currentYear + "-12-31");*/
		return new Attribute(attribute);
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		String value = null;
		if(attribute.getValue() != null) value = (String) attribute.getValue();
		//If there is some value and member is in status expired or disabled
		if(value != null && (member.getStatus().equals(Status.EXPIRED) || member.getStatus().equals(Status.DISABLED))) {
			Date expirationDate = null;
			try {
				expirationDate = BeansUtils.getDateFormatterWithoutTime().parse(value);
			} catch (ParseException ex) {
				throw new InternalErrorException("Date parsing failed in setHook, even if parsing in checkMethod was correct.", ex);
			}
			Date date = new Date();
			if(expirationDate.compareTo(date) > 0) session.getPerunBl().getMembersManagerBl().validateMemberAsync(session, member);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setFriendlyName("membershipExpiration");
		attr.setDisplayName("Membership expiration");
		attr.setType(String.class.getName());
		attr.setDescription("When the membership expires, format YYYY-MM-DD.");
		return attr;
	}

}
