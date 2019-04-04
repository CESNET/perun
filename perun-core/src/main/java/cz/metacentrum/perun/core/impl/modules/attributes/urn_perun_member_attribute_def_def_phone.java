package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simona Kruppova
 */
public class urn_perun_member_attribute_def_def_phone extends MemberAttributesModuleAbstract implements MemberAttributesModuleImplApi {

	private static final String A_U_phone = AttributesManager.NS_USER_ATTR_DEF + ":phone";
	//This regular expression requires international form of phone number starting with '+' without spaces
	//Due to technical reasons we support only numbers longer than 3 characters and shorter than 17 characters [4,16]
	//Example of correct number "+420123456789"
	private static final Pattern pattern = Pattern.compile("^[+][0-9]{4,16}$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Member member, Attribute attribute) throws WrongAttributeValueException {

		// null attribute
		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "User attribute phone cannot be null.");

		// wrong type of the attribute
		if (!(attribute.getValue() instanceof String)) throw new WrongAttributeValueException(attribute, "Wrong type of the attribute. Expected: String");

		String phone = (String) attribute.getValue();

		Matcher matcher = pattern.matcher(phone);
		if (!matcher.matches()) {
			throw new WrongAttributeValueException(attribute, "Phone is not in correct format!");
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		User user = session.getPerunBl().getUsersManagerBl().getUserByMember(session, member);

		if(attribute.getValue() != null) {
			Attribute userPhone = null;
			try {
				userPhone = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_phone);
				if(userPhone.getValue() == null) {
					userPhone.setValue(attribute.getValue());
					session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPhone);
				}
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (WrongAttributeValueException ex) {
				throw new WrongReferenceAttributeValueException(attribute, userPhone, "Mismatch in checking of member phone and user phone (different checking rules)", ex);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setFriendlyName("phone");
		attr.setDisplayName("Phone (for VO)");
		attr.setType(String.class.getName());
		attr.setDescription("Phone number in organization.");
		return attr;
	}
}
