package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attribute module for storing school mail of persons at VŠUP.
 * It has to be login@vsup.cz and is set whenever login-namespace:vsup attribute is set.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupMail extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	// VŠUP MAIL PATTERN !! -> login@vsup.cz
	public static final Pattern emailPattern = Pattern.compile("^[-_A-Za-z0-9+'.]+@vsup\\.cz$");
	public static final String usedMailsUrn = "urn:perun:entityless:attribute-def:def:usedMails";
	public static final String usedMailsKeyVsup = "vsup";

	// all VŠUP mail attributes
	public static final String vsupMailUrn = "urn:perun:user:attribute-def:def:vsupMail";
	public static final String vsupMailAliasUrn = "urn:perun:user:attribute-def:def:vsupMailAlias";
	public static final String vsupMailAliasesUrn = "urn:perun:user:attribute-def:def:vsupMailAliases";
	public static final String vsupPreferredMailUrn = "urn:perun:user:attribute-def:def:vsupPreferredMail";

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {

		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "School mail can't be null.");

		Matcher emailMatcher = emailPattern.matcher((String)attribute.getValue());
		if(!emailMatcher.find()) throw new WrongAttributeValueException(attribute, user, "School mail is not in a correct form: \"login@vsup.cz\".");

		// TODO - check uniqueness - if present in an entityless cache map, must belong to same user
		Map<String,String> usedMails;
		Attribute usedMailsAttribute;
		try {
			usedMailsAttribute = sess.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(sess, usedMailsKeyVsup, usedMailsUrn);
			if (usedMailsAttribute.getValue() != null) {
				usedMails = (Map<String, String>) usedMailsAttribute.getValue();
				if (usedMails.containsKey(attribute.getValue())) {
					if (!Objects.equals(usedMails.get(attribute.getValue()), String.valueOf(user.getId()))) {
						throw new WrongAttributeValueException(attribute, user, "This mail is already in use by User ID: " + usedMails.get(attribute.getValue()) + ".");
					}
				}
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Entityless attribute 'urn:perun:entityless:attribute-def:def:usedMails' doesn't exists.", ex);
		}

	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {

		// map of reserved vsup mails
		Attribute reservedMailsAttribute;
		Map<String,String> reservedMailsAttributeValue;

		// other vsup mail attributes to get values from
		Attribute mailAliasAttribute;
		Attribute mailAliasesAttribute;
		Attribute vsupPreferredMailAttribute;

		// output sets used for comparison
		Set<String> reservedMailsOfUser = new HashSet<>();
		Set<String> actualMailsOfUser = new HashSet<>();

		// get related attributes

		try {
			reservedMailsAttribute = session.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(session, usedMailsKeyVsup, usedMailsUrn);
			mailAliasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailAliasUrn);
			mailAliasesAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailAliasesUrn);
			vsupPreferredMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupPreferredMailUrn);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// if REMOVE action and reserved map is empty -> consistency error

		if (attribute.getValue() == null && reservedMailsAttribute.getValue() == null) {
			throw new ConsistencyErrorException("Entityless attribute 'urn:perun:entityless:attribute-def:def:usedMails' is empty, but we are removing 'vsupMail' value, so there should have been entry in entityless attribute.");
		}

		// get value from reserved mails attribute

		if (reservedMailsAttribute.getValue() == null) {
			reservedMailsAttributeValue = new LinkedHashMap<>();
		} else {
			reservedMailsAttributeValue = (Map<String,String>)reservedMailsAttribute.getValue();
		}

		// if SET action and mail is already reserved
		if (attribute.getValue() != null) {
			String ownersUserId = reservedMailsAttributeValue.get((String)attribute.getValue());
			if (!Objects.equals(ownersUserId, String.valueOf(user.getId()))) {
				// TODO - maybe get actual owners attribute and throw WrongReferenceAttributeException to be nice in a GUI ?
				throw new InternalErrorException("VŠUP mail: '"+attribute.getValue()+"' is already in use by User ID: " + ownersUserId + ".");
			} else {
				// new value was actually already stored in reserved mails and belongs to the correct user
				return;
			}
		}

		// fill output sets for comparison

		for (Map.Entry<String,String> entry : reservedMailsAttributeValue.entrySet()) {
			if (Objects.equals(entry.getValue(), String.valueOf(user.getId()))) {
				// reserved mails of a user
				reservedMailsOfUser.add(entry.getKey());
			}
		}

		if (mailAliasAttribute.getValue() != null) {
			actualMailsOfUser.add((String)mailAliasAttribute.getValue());
		}
		if (vsupPreferredMailAttribute.getValue() != null) {
			actualMailsOfUser.add((String)vsupPreferredMailAttribute.getValue());
		}
		if (mailAliasesAttribute.getValue() != null) {
			actualMailsOfUser.addAll((ArrayList<String>)mailAliasesAttribute.getValue());
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
			reservedMailsAttributeValue.putIfAbsent((String)attribute.getValue(), String.valueOf(user.getId()));
		}

		// save changes in entityless attribute
		try {
			reservedMailsAttribute.setValue(reservedMailsAttributeValue);
			session.getPerunBl().getAttributesManagerBl().setAttribute(session, usedMailsKeyVsup, reservedMailsAttribute);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("vsupMail");
		attr.setDisplayName("School mail");
		attr.setType(Integer.class.getName());
		attr.setDescription("Generated school mail in a \"name.surname@vsup.cz\" form.");
		return attr;
	}

}
