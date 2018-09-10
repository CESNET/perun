package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;
import java.util.regex.Matcher;

/**
 * @author Michal Šťava   <stava.michal@gmail.com>
 */
public class urn_perun_member_attribute_def_def_mail extends MemberAttributesModuleAbstract implements MemberAttributesModuleImplApi {

	private static final String A_U_preferredMail = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";
	private static final String A_M_mail = AttributesManager.NS_MEMBER_ATTR_DEF + ":mail";

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		String attributeValue = null;

		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "Member mail can't be null.");
		else attributeValue = (String) attribute.getValue();

		Matcher emailMatcher = Utils.emailPattern.matcher(attributeValue);
		if(!emailMatcher.find()) throw new WrongAttributeValueException(attribute, "Email is not in correct form.");
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		User user = session.getPerunBl().getUsersManagerBl().getUserByMember(session, member);

		if(attribute.getValue() != null) {
			Attribute userPreferredMail = null;
			try {
				userPreferredMail = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_preferredMail);
				if(userPreferredMail.getValue() == null) {
					userPreferredMail.setValue(attribute.getValue());
					session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredMail);
				}
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeValueException ex) {
				throw new WrongReferenceAttributeValueException(attribute, userPreferredMail, "Mismatch in checking of member mail and user preferredMail (different checking rules).", ex);
			}
		}

		/* This funcionality is not needed now
		//if this mail has been removed, check user preffered mail if the value is still correct, if not set a new one or remove it if no other exists
		User user = session.getPerunBl().getUsersManagerBl().getUserByMember(session, member);
		try {
		Attribute userPreferredMail = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_preferredMail);
		//TODO: if userPreferredMail is null when memberMail has been removed, its error in consistency, but exception is not solution, need to log it

		//If member mail has been removed
		if(attribute.getValue() == null) {
		try {
		session.getPerunBl().getAttributesManagerBl().checkAttributeValue(session, user, userPreferredMail);
		} catch (WrongAttributeValueException ex) {
		List<Member> membersOfUser = session.getPerunBl().getMembersManagerBl().getMembersByUser(session, user);
		for(Member m: membersOfUser) {
		Attribute memberMail = session.getPerunBl().getAttributesManagerBl().getAttribute(session, member, A_M_mail);
		if(memberMail.getValue() != null) {
		userPreferredMail.setValue(memberMail.getValue());
		session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredMail);
		break;
		}
		}
		userPreferredMail.setValue(null);
		session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredMail);

		}
		//if member mail was new set or set to another value
		} else {
		//if userPreferredMail is null, so can save this new value there
		if(userPreferredMail.getValue() == null) {
		userPreferredMail.setValue(attribute.getValue());
		session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredMail);

		//if userPreferredMail is not null, need to try if the old value is still correct, if not, save new value there
		} else {
		try {
		session.getPerunBl().getAttributesManagerBl().checkAttributeValue(session, user, userPreferredMail);
		} catch (WrongAttributeValueException ex) {
		//old value of userPreferredMail is not correct now, save the new value from member mail there
		userPreferredMail.setValue(attribute.getValue());
		session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, attribute);
		}
		}
		}
		} catch(WrongAttributeAssignmentException ex) {
		throw new InternalErrorException(ex);
		} catch(AttributeNotExistsException ex) {
		throw new ConsistencyErrorException(ex);
		} catch(WrongAttributeValueException ex) {
		throw new WrongReferenceAttributeValueException("There is mismatch between possible format of member mail and userPreferredMail", ex);
		}*/
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setFriendlyName("mail");
		attr.setDisplayName("Mail");
		attr.setType(String.class.getName());
		attr.setDescription("Member's trusted mail.");
		return attr;
	}
}
