package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.impl.Utils.emailPattern;
import static cz.metacentrum.perun.core.impl.Utils.hasDuplicate;

/**
 * Module for email addresses for Office365 at Masaryk University.
 * Implements checks for attribute urn:perun:member:attribute-def:def:o365EmailAddresses_mu.
 * <p>
 * Requirements:
 * <ul>
 * <li>type is list</li>
 * <li>all values are email addresses</li>
 * <li>must contain value uco@muni.cz</li>
 * <li>no duplicates among the list values</li>
 * <li>no duplicates among all values of this attribute for all members and all values of
 * attribute urn:perun:group_resource:attribute-def:def:o365EmailAddresses:mu</li>
 * </ul>
 *
 * @author Martin Kuba &lt;makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class urn_perun_member_attribute_def_def_o365EmailAddresses_mu extends MemberAttributesModuleAbstract {

	private static final String NAMESPACE = AttributesManager.NS_MEMBER_ATTR_DEF;
	static final String UCO_ATTRIBUTE = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";
	static final Pattern MU_EMAIL_SYNTAX = Pattern.compile("\\d+@muni.cz");

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		List<String> emails;

		//get values
		Object value = attribute.getValue();
		if (value == null) {
			throw new WrongAttributeValueException(attribute, member, "can't be null.");
		} else if (!(value instanceof List)) {
			throw new WrongAttributeValueException(attribute, member, "is of type " + value.getClass() + ", but should be ArrayList");
		} else {
			//noinspection unchecked
			emails = (List<String>) value;
		}
		//check syntax of all values
		for (String email : emails) {
			Matcher emailMatcher = emailPattern.matcher(email);
			if (!emailMatcher.matches())
				throw new WrongAttributeValueException(attribute, member, "Email " + email + " is not in correct form.");
		}

		//check for duplicities
		if (hasDuplicate(emails)) {
			throw new WrongAttributeValueException(attribute, member, "duplicate values");
		}

		//check for presence of uco@muni.cz
		if (!emails.contains(getUserUcoEmail(sess, member))) {
			throw new WrongAttributeValueException(attribute, member, "does not contain uco@muni.cz");
		}

		//check for duplicities among all members and groups (urn_perun_group_resource_attribute_def_def_o365EmailAddresses_mu)
		//TODO not implemented yet
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<String>();
		dependencies.add(UCO_ATTRIBUTE);
		return dependencies;
	}

	/**
	 * Prefills value uco@muni.cz
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Member member, AttributeDefinition attrDef)
			throws InternalErrorException, WrongAttributeAssignmentException {
		if (!NAMESPACE.equals(attrDef.getNamespace())) throw new WrongAttributeAssignmentException(attrDef);
		Attribute result = new Attribute(attrDef);
		List<String> newValue = new ArrayList<>();
		newValue.add(getUserUcoEmail(sess, member));
		result.setValue(newValue);
		return result;
	}

	/**
	 * Gets user uco from attribute urn:perun:user:attribute-def:def:login-namespace:mu with appended @muni.cz.
	 */
	private String getUserUcoEmail(PerunSessionImpl sess, Member member) throws InternalErrorException, WrongAttributeAssignmentException {
		try {
			User user = sess.getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
			Object value = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, UCO_ATTRIBUTE).getValue();
			if (value != null && value instanceof String) {
				return value + "@muni.cz";
			}
			throw new InternalErrorException("user " + user.getId() + " does not have string attribute " + UCO_ATTRIBUTE);
		} catch (UserNotExistsException | AttributeNotExistsException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NAMESPACE);
		attr.setFriendlyName("o365EmailAddresses:mu");
		attr.setDisplayName("MU O365 email addresses");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Email address for Office365 at Masaryk University");
		return attr;
	}
}
