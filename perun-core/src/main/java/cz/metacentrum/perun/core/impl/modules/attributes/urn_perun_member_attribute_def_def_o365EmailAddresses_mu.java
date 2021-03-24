package cz.metacentrum.perun.core.impl.modules.attributes;

import com.google.common.collect.Lists;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.impl.Utils.emailPattern;
import static cz.metacentrum.perun.core.impl.Utils.hasDuplicate;

/**
 * Module for email addresses for Office365 at Masaryk University.
 * Implements checks for attribute urn:perun:member:attribute-def:def:o365EmailAddresses_mu.
 * <p>
 * Requirements:
 * <ul>
 * <li>type is list</li>
 * <li>value can be null</li>
 * <li>if not null, than all values are email addresses</li>
 * <li>no duplicates among the list values</li>
 * <li>no duplicates among all values of this attribute for all members</li>
 * </ul>
 *
 * @author Martin Kuba &lt;makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class urn_perun_member_attribute_def_def_o365EmailAddresses_mu extends MemberAttributesModuleAbstract {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_member_attribute_def_def_o365EmailAddresses_mu.class);

	private static final String NAMESPACE = AttributesManager.NS_MEMBER_ATTR_DEF;

	public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Attribute attribute) throws WrongAttributeValueException {
		Object value = attribute.getValue();
		List<String> emails;

		if (value == null) return;
		else if (!(value instanceof ArrayList)) {
			throw new WrongAttributeValueException(attribute, member, "is of type " + value.getClass() + ", but should be ArrayList");
		} else {
			emails = attribute.valueAsList();
		}

		//check for duplicities
		if (hasDuplicate(emails)) {
			throw new WrongAttributeValueException(attribute, member, "has duplicate values");
		}

		for (String email : emails) {
			Matcher emailMatcher = emailPattern.matcher(email);
			if (!emailMatcher.matches())
				throw new WrongAttributeValueException(attribute, member, "Email " + email + " is not in correct form.");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Member member, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		log.trace("checkAttributeSemantics(member={},attribute={})", member, attribute);

		//get values
		if (attribute.getValue() == null) {
			throw new WrongReferenceAttributeValueException(attribute, "can't be null.");
		}

		//No need to check duplicities, attribute is unique
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(NAMESPACE);
		attr.setFriendlyName("o365EmailAddresses:mu");
		attr.setDisplayName("MU O365 email addresses");
		attr.setType(ArrayList.class.getName());
		attr.setUnique(true);
		attr.setDescription("Email address for Office365 at Masaryk University");
		return attr;
	}
}
