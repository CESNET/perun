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

	private final static Logger log = LoggerFactory.getLogger(urn_perun_member_attribute_def_def_o365EmailAddresses_mu.class);

	private static final String NAMESPACE = AttributesManager.NS_MEMBER_ATTR_DEF;
	static final String UCO_ATTRIBUTE = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:mu";
	static final String G_D_O365_EMAIL_ADDRESSES_O365MU_ATTR = AttributesManager.NS_GROUP_ATTR_DEF + ":o365EmailAddresses:o365mu";

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
		List<String> emails;

		//get values
		if (attribute.getValue() == null) {
			throw new WrongReferenceAttributeValueException(attribute, "can't be null.");
		} else {
			emails = attribute.valueAsList();
		}

		//check for presence of uco@muni.cz
		Attribute attrUCO = getUserUco(sess, member);
		String UCO = attrUCO.valueAsString();
		//Throw an exception if UCO is null (we need to have this value not-null to correctly check value of this attribute)
		if(UCO == null) {
			throw new WrongReferenceAttributeValueException(attribute, attrUCO, member, null, UCO_ATTRIBUTE + " has null value!");
		}
		String ucoEmail = UCO + "@muni.cz";
		if (!emails.contains(ucoEmail)) {
			throw new WrongReferenceAttributeValueException(attribute, attrUCO, member, null, member, null, "does not contain " + ucoEmail);
		}

		//No need to check duplicities between other members, cause attribute is unique
		//Check uniqueness between this attribute and groups
		try {
			Attribute groupO365EmailAddresses = new Attribute(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, G_D_O365_EMAIL_ADDRESSES_O365MU_ATTR));
			groupO365EmailAddresses.setValue(emails);
			Set<Integer> groupsWithSameAttributeValueSet = sess.getPerunBl().getAttributesManagerBl().getPerunBeanIdsForUniqueAttributeValue(sess, groupO365EmailAddresses).stream()
				.map(Pair::getLeft)
				.collect(Collectors.toSet());
			if (!groupsWithSameAttributeValueSet.isEmpty()) {
				try {
					Group groupWithDuplicateEmail = sess.getPerunBl().getGroupsManagerBl().getGroupById(sess, groupsWithSameAttributeValueSet.iterator().next());
					throw new WrongReferenceAttributeValueException(attribute, groupO365EmailAddresses, member, null, groupWithDuplicateEmail, null, "some of the email addresses are already assigned.");
				} catch (GroupNotExistsException e) {
					throw new ConsistencyErrorException(e);
				}
			}
		} catch (AttributeNotExistsException ex) {
			//If attribute not exists, we can log it and skip it, because there are no duplicates in not existing attributes
			log.debug("Attribute {} not exists to check duplicities in it while checkAttributeSemantics for {}.", G_D_O365_EMAIL_ADDRESSES_O365MU_ATTR, attribute);
		}
	}

	@Override
	public List<String> getDependencies() {
		return Arrays.asList(UCO_ATTRIBUTE, G_D_O365_EMAIL_ADDRESSES_O365MU_ATTR);
	}

	/**
	 * Prefills values uco@mail.muni.cz and uco@muni.cz
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Member member, AttributeDefinition attrDef) throws WrongAttributeAssignmentException {
		return new Attribute(attrDef, getUserUcoEmails(sess, member));
	}

	/**
	 * Gets user uco attribute urn:perun:user:attribute-def:def:login-namespace:mu.
	 *
	 * @return Attribute with STRING UCO value if exists, with null value if not exists
	 */
	private Attribute getUserUco(PerunSessionImpl sess, Member member) throws WrongAttributeAssignmentException {
		try {
			User user = sess.getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, UCO_ATTRIBUTE);
		} catch (UserNotExistsException | AttributeNotExistsException e) {
			throw new InternalErrorException(e.getMessage(), e);
		}
	}

	/**
	 * @returns uco@muni.cz in list if UCO exists, null if not exists
	 */
	private ArrayList<String> getUserUcoEmails(PerunSessionImpl sess, Member member) throws WrongAttributeAssignmentException {
		Attribute attributeUCO = getUserUco(sess, member);
		String uco = attributeUCO.valueAsString();
		if(uco == null) return null;
		else return Lists.newArrayList(uco + "@muni.cz");
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
