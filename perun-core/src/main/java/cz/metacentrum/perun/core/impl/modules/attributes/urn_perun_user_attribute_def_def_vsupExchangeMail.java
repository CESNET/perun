package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsKeyVsup;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailAliasUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailAliasesUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupPreferredMailUrn;

/**
 * Attribute module for Primary school mail at VŠUP.
 * Expected format is "firstName.lastName[counter]@umprum.cz"
 * Artistic names have preference over normal: "u:d:artisticFirstName", "u:d:artisticLastName".
 * Value can be empty or manually changed.
 * In case of users name change, attribute value must be changed manually !!
 * Value is filled/generated only for normal users (service users must have value set manually)!!
 *
 * On value change, map of usedMails in entityless attributes is checked and updated.
 * Also u:d:vsupPreferredMail is set to current value, if is empty.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupExchangeMail extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	// VŠUP EXCHANGE MAIL PATTERN !!
	private static final Pattern vsupExchangeMailPattern = Pattern.compile("^[A-Za-z]+\\.[A-Za-z]+([0-9])*@umprum\\.cz$");

	public static final String vsupExchangeMailUrn = "urn:perun:user:attribute-def:def:vsupExchangeMail";
	public static final String vsupExchangeMailAliasesUrn = "urn:perun:user:attribute-def:def:vsupExchangeMailAliases";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		// can be empty
		if (attribute.getValue() == null) return;

		// if set, must match generic format
		Matcher emailMatcher = vsupExchangeMailPattern.matcher(attribute.valueAsString());
		if(!emailMatcher.find()) throw new WrongAttributeValueException(attribute, user, "Primary mail alias is not in a correct form: \"firstName.lastName[counter]@umprum.cz\".");
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws WrongReferenceAttributeValueException {

		// can be empty
		if (attribute.getValue() == null) return;

		// We must check uniqueness since vsupExchangeMail is filled by itself and filling function iterates until value is correct.

		try {
			Attribute reservedMailsAttribute = sess.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(sess, usedMailsKeyVsup, usedMailsUrn);
			if (reservedMailsAttribute.getValue() != null) {
				Map<String,String> reservedMailsAttributeValue = reservedMailsAttribute.valueAsMap();
				String ownersUserId = reservedMailsAttributeValue.get(attribute.valueAsString());
				if (ownersUserId != null && !Objects.equals(ownersUserId, String.valueOf(user.getId()))) {
					User ownersUser = sess.getPerunBl().getUsersManagerBl().getUserById(sess, Integer.parseInt(ownersUserId));
					throw new WrongReferenceAttributeValueException(attribute, reservedMailsAttribute, user, null, ownersUser, null, "VŠUP primary mail: '"+attribute.getValue()+"' is already in use by User ID: " + ownersUserId + ".");
				}
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
		} catch (UserNotExistsException e) {
			throw new ConsistencyErrorException("User doesn't exists.", e);
		}

	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(usedMailsUrn);
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute) throws WrongAttributeAssignmentException {

		return new Attribute(attribute);

		// FIXME - UNCOMMENT DURING MAIL MIGRATION (once we manually migrate old values from vsupMailAlias).

		/*
		if (user.isSpecificUser()) {
			// Do not fill value for service/sponsored users
			return new Attribute(attribute);
		}

		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		Attribute filledAttribute = new Attribute(attribute);

		try {
			Attribute artFirstName = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, "urn:perun:user:attribute-def:def:artisticFirstName");
			Attribute artLastName = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, "urn:perun:user:attribute-def:def:artisticLastName");

			if (artFirstName.getValue() != null) firstName = artFirstName.valueAsString();
			if (artLastName.getValue() != null) lastName = artLastName.valueAsString();

		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Definition for artistic names of user doesn't exists.", e);
		}

		if (lastName == null || firstName == null) {
			return filledAttribute;
		}

		// remove all diacritics marks from name
		String mail = ModulesUtilsBlImpl.normalizeStringForLogin(firstName) + "." + ModulesUtilsBlImpl.normalizeStringForLogin(lastName);

		// fill value - start as mail, mail2, mail3, ....
		int iterator = 1;
		while (iterator >= 1) {
			if (iterator > 1) {
				filledAttribute.setValue(mail + iterator + "@umprum.cz");
			} else {
				filledAttribute.setValue(mail + "@umprum.cz");
			}
			try {
				checkAttributeSemantics(session, user, filledAttribute);
				return filledAttribute;
			} catch (WrongReferenceAttributeValueException ex) {
				// continue in a WHILE cycle
				iterator++;
			}
		}

		return filledAttribute;
		*/

	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws WrongReferenceAttributeValueException {

		// FIXME - REMOVE checks on vsupMailAlias and vsupMailAliases AFTER MIGRATION

		// map of reserved vsup mails
		Attribute reservedMailsAttribute;
		Map<String,String> reservedMailsAttributeValue;

		// other vsup mail attributes to get values from
		Attribute vsupMailAttribute;
		Attribute mailAliasAttribute;
		Attribute mailAliasesAttribute;
		Attribute vsupPreferredMailAttribute;
		Attribute vsupExchangeMailAliasesAttribute;

		// output sets used for comparison
		Set<String> reservedMailsOfUser = new HashSet<>();
		Set<String> actualMailsOfUser = new HashSet<>();

		// get related attributes

		try {
			reservedMailsAttribute = session.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(session, usedMailsKeyVsup, usedMailsUrn);
			vsupMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailUrn);
			mailAliasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailAliasUrn);
			mailAliasesAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailAliasesUrn);
			vsupPreferredMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupPreferredMailUrn);
			vsupExchangeMailAliasesAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupExchangeMailAliasesUrn);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// if REMOVE action and reserved map is empty -> consistency error

		if (attribute.getValue() == null && reservedMailsAttribute.getValue() == null) {
			throw new ConsistencyErrorException("Entityless attribute 'urn:perun:entityless:attribute-def:def:usedMails' is empty, but we are removing 'vsupExchangeMail' value, so there should have been entry in entityless attribute.");
		}

		// get value from reserved mails attribute

		if (reservedMailsAttribute.getValue() == null) {
			reservedMailsAttributeValue = new LinkedHashMap<>();
		} else {
			reservedMailsAttributeValue = reservedMailsAttribute.valueAsMap();
		}

		// if SET action and mail is already reserved by other user
		if (attribute.getValue() != null) {
			String ownersUserId = reservedMailsAttributeValue.get(attribute.valueAsString());
			if (ownersUserId != null && !Objects.equals(ownersUserId, String.valueOf(user.getId()))) {
				// TODO - maybe get actual owners attribute and throw WrongReferenceAttributeException to be nice in a GUI ?
				throw new InternalErrorException("VŠUP primary mail: '"+attribute.getValue()+"' is already in use by User ID: " + ownersUserId + ".");
			}
		}

		// fill output sets for comparison

		for (Map.Entry<String,String> entry : reservedMailsAttributeValue.entrySet()) {
			if (Objects.equals(entry.getValue(), String.valueOf(user.getId()))) {
				// reserved mails of a user
				reservedMailsOfUser.add(entry.getKey());
			}
		}

		if (vsupMailAttribute.getValue() != null) {
			actualMailsOfUser.add(vsupMailAttribute.valueAsString());
		}
		if (vsupPreferredMailAttribute.getValue() != null) {
			actualMailsOfUser.add(vsupPreferredMailAttribute.valueAsString());
		}
		if (mailAliasAttribute.getValue() != null) {
			actualMailsOfUser.add(mailAliasAttribute.valueAsString());
		}
		if (mailAliasesAttribute.getValue() != null) {
			actualMailsOfUser.addAll(mailAliasesAttribute.valueAsList());
		}
		if (vsupExchangeMailAliasesAttribute.getValue() != null) {
			actualMailsOfUser.addAll(vsupExchangeMailAliasesAttribute.valueAsList());
		}


		// Find which is in the map (reserved) but not in attributes anymore and remove it from the map
		// handles remove and change action on attribute

		for (String mail : reservedMailsOfUser) {
			if (!actualMailsOfUser.contains(mail)) {
				// Remove mail, which is not in attributes anymore
				reservedMailsAttributeValue.remove(mail);
				// since this attribute holds single value, we can break the cycle here
				break;
			}
		}

		// Put in which is in attribute but not in a map
		if (attribute.getValue() != null) {
			reservedMailsAttributeValue.putIfAbsent(attribute.valueAsString(), String.valueOf(user.getId()));
		}

		// save changes in entityless attribute
		try {
			// always set value to attribute, since we might start with null in attribute and empty map in variable !!
			reservedMailsAttribute.setValue(reservedMailsAttributeValue);
			session.getPerunBl().getAttributesManagerBl().setAttribute(session, usedMailsKeyVsup, reservedMailsAttribute);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		// if set, check vsupPreferredMail and set it's value if is currently empty or equals vsupMail
		// FIXME - ENABLE SETTING OF PREFERRED MAIL AFTER MIGRATION
		// FIXME - perform comparison with both domains @vsup.cz and @umprum.cz
		/*
		if (attribute.getValue() != null) {
			String preferredMail = vsupPreferredMailAttribute.valueAsString();
			if (preferredMail == null || Objects.equals(preferredMail, vsupMailAttribute.getValue())) {
				vsupPreferredMailAttribute.setValue(attribute.getValue());
				try {
					session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, vsupPreferredMailAttribute);
				} catch (WrongAttributeValueException | WrongAttributeAssignmentException e) {
					throw new InternalErrorException("Unable to store generated vsupExchangeMail to vsupPreferredMail.", e);
				}
			}
		}
		*/

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("vsupExchangeMail");
		attr.setDisplayName("School mail (primary)");
		attr.setType(String.class.getName());
		attr.setDescription("Generated primary school mail in a \"name.surname[counter]@umprum.cz\" form. It is used by o365 Exchange server. Value can be empty. On users name change, attribute value must be fixed manually.");
		return attr;
	}

}
