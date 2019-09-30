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

import static cz.metacentrum.perun.core.impl.Utils.emailPattern;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsKeyVsup;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailAliasUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailAliasesUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailUrn;

/**
 * Attribute module for storing preferred mail of a user at VŠUP.
 * It's used for other services than a Zimbra mail server itself.
 * Is by default filled from vsupMailAlias or vsupMail, but can be set manually to any value.
 * If empty, value might be set by setting u:d:vsupMail or u:d:vsupMailAlias attributes.
 *
 * On value change, map of usedMails in entityless attributes is checked and updated.
 * Also, value is copied to u:d:preferredMail so admin can see preferred mail in GUI.
 *
 * Since filled value by this module might be NULL at the time of processing, we must allow NULL value in checkAttributeSemantics(),
 * because when all mail attributes are required and set at once, we can't ensure correct processing order of related attributes
 * and it might perform check on old value, because of setRequiredAttributes() implementation uses in memory value instead of refreshing from DB.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupPreferredMail extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {

		// we must allow null, since when setting required attributes all at once, value might not be filled yet
		// if vsupMail or vsupMailAlias is empty, but it's checked by method impl.

		if (attribute.getValue() == null) return; // throw new WrongAttributeValueException(attribute, user, "Preferred mail can't be null.");

		// standard email pattern !!
		Matcher emailMatcher = emailPattern.matcher((String)attribute.getValue());
		if(!emailMatcher.find()) throw new WrongAttributeValueException(attribute, user, "School Preferred Mail is not in a correct form.");

		// We check uniqueness on all related attributes change, so we don't need to do it here.

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {

		Attribute resultAttribute = new Attribute(attribute);
		try {
			Attribute vsupMailAliasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailAliasUrn);
			if (vsupMailAliasAttribute.getValue() != null) {
				resultAttribute.setValue(vsupMailAliasAttribute.getValue());
			} else {
				Attribute vsupMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailUrn);
				if (vsupMailAttribute.getValue() != null) {
					resultAttribute.setValue(vsupMailAttribute.getValue());
				}
			}

		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Related VŠUP mail attributes not exists.", e);
		}
		return resultAttribute;
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {

		// map of reserved vsup mails
		Attribute reservedMailsAttribute;
		Map<String,String> reservedMailsAttributeValue;

		// other vsup mail attributes to get values from
		Attribute vsupMailAttribute;
		Attribute vsupMailAliasAttribute;
		Attribute mailAliasesAttribute;

		// output sets used for comparison
		Set<String> reservedMailsOfUser = new HashSet<>();
		Set<String> actualMailsOfUser = new HashSet<>();

		// get related attributes

		try {
			reservedMailsAttribute = session.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(session, usedMailsKeyVsup, usedMailsUrn);
			vsupMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailUrn);
			mailAliasesAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailAliasesUrn);
			vsupMailAliasAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailAliasUrn);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// if REMOVE action and reserved map is empty -> consistency error

		if (attribute.getValue() == null && reservedMailsAttribute.getValue() == null) {
			throw new ConsistencyErrorException("Entityless attribute 'urn:perun:entityless:attribute-def:def:usedMails' is empty, but we are removing 'vsupPreferredMail' value, so there should have been entry in entityless attribute.");
		}

		// get value from reserved mails attribute

		if (reservedMailsAttribute.getValue() == null) {
			reservedMailsAttributeValue = new LinkedHashMap<>();
		} else {
			reservedMailsAttributeValue = (Map<String,String>)reservedMailsAttribute.getValue();
		}

		// if SET action and mail is already reserved by other user
		if (attribute.getValue() != null) {
			String ownersUserId = reservedMailsAttributeValue.get(attribute.valueAsString());
			if (ownersUserId != null && !Objects.equals(ownersUserId, String.valueOf(user.getId()))) {
				// TODO - maybe get actual owners attribute and throw WrongReferenceAttributeException to be nice in a GUI ?
				throw new InternalErrorException("VŠUP preferred mail: '"+attribute.getValue()+"' is already in use by User ID: " + ownersUserId + ".");
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
			actualMailsOfUser.add((String)vsupMailAttribute.getValue());
		}
		if (vsupMailAliasAttribute.getValue() != null) {
			actualMailsOfUser.add((String)vsupMailAliasAttribute.getValue());
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
			// always set value to attribute, since we might start with null in attribute and empty map in variable !!
			reservedMailsAttribute.setValue(reservedMailsAttributeValue);
			session.getPerunBl().getAttributesManagerBl().setAttribute(session, usedMailsKeyVsup, reservedMailsAttribute);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		// update user:preferredMail so admin can see users preferred mail in GUI.
		try {
			if (attribute.getValue() != null) {

				Attribute userPreferredMail = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, "urn:perun:user:attribute-def:def:preferredMail");
				if (!Objects.equals(userPreferredMail.getValue(), attribute.getValue())) {
					// if preferred mail is different, update user:preferredMail
					userPreferredMail.setValue(attribute.getValue());
					session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userPreferredMail);
				}
			}
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException | AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("vsupPreferredMail");
		attr.setDisplayName("School preferred mail");
		attr.setType(String.class.getName());
		attr.setDescription("Preferred mail used for VŠUP internal services. Pre-filled from vsupMailAlias or vsupMail, but can be custom value.");
		return attr;
	}

}
