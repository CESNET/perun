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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.emailPattern;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsKeyVsup;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.usedMailsUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupExchangeMailUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupMailUrn;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_def_vsupMail.vsupPreferredMailUrn;

/**
 * Storage for all spare mail aliases of a person at VŠUP.
 * Primary mail stored in O365 Exchange server is stored in user:def:vsupExchangeMail attribute !!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_vsupExchangeMailAliases extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {

		List<String> mails = (attribute.getValue() != null) ? (attribute.valueAsList()) : (new ArrayList<>());

		for (String mail : mails) {
			Matcher emailMatcher = emailPattern.matcher(mail);
			if(!emailMatcher.find()) throw new WrongAttributeValueException(attribute, user, "Following value of primary mail alias is not in a correct form: '"+mail+"'.");
		}

		// TODO - check uniqueness within list of values ??

	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws WrongReferenceAttributeValueException {

		// map of reserved vsup mails
		Attribute reservedMailsAttribute;
		Map<String,String> reservedMailsAttributeValue;

		// other vsup mail attributes to get values from
		Attribute vsupMailAttribute;
		Attribute vsupPreferredMailAttribute;
		Attribute vsupExchangeMailAttribute;

		// output sets used for comparison
		Set<String> reservedMailsOfUser = new HashSet<>();
		Set<String> actualMailsOfUser = new HashSet<>();

		// get related attributes

		try {
			reservedMailsAttribute = session.getPerunBl().getAttributesManagerBl().getEntitylessAttributeForUpdate(session, usedMailsKeyVsup, usedMailsUrn);
			vsupMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupMailUrn);
			vsupPreferredMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupPreferredMailUrn);
			vsupExchangeMailAttribute = session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, vsupExchangeMailUrn);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Attribute doesn't exists.", ex);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// if REMOVE action and reserved map is empty -> consistency error

		if (attribute.getValue() == null && reservedMailsAttribute.getValue() == null) {
			throw new ConsistencyErrorException("Entityless attribute 'urn:perun:entityless:attribute-def:def:usedMails' is empty, but we are removing 'vsupExchangeMailAliases' value, so there should have been entry in entityless attribute.");
		}

		// get value from reserved mails attribute

		if (reservedMailsAttribute.getValue() == null) {
			reservedMailsAttributeValue = new LinkedHashMap<>();
		} else {
			reservedMailsAttributeValue = reservedMailsAttribute.valueAsMap();
		}

		// if SET action and mail is already reserved by other user
		if (attribute.getValue() != null) {

			List<String> mails = attribute.valueAsList();
			for (String mail : mails) {
				String ownersUserId = reservedMailsAttributeValue.get(mail);
				if (ownersUserId != null && !Objects.equals(ownersUserId, String.valueOf(user.getId()))) {
					// TODO - maybe get actual owners attribute and throw WrongReferenceAttributeException to be nice in a GUI ?
					throw new InternalErrorException("One of VŠUP mail aliases: '"+mail+"' is already in use by User ID: " + ownersUserId + ".");
				}
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
		if (vsupExchangeMailAttribute.getValue() != null) {
			actualMailsOfUser.add(vsupExchangeMailAttribute.valueAsString());
		}

		// Find which is in the map (reserved) but not in attributes anymore and remove it from the map
		// handles remove and change action on attribute

		for (String mail : reservedMailsOfUser) {
			if (!actualMailsOfUser.contains(mail)) {
				// Remove mail, which is not in attributes anymore
				reservedMailsAttributeValue.remove(mail);
			}
		}

		// Put in which is in attribute but not in a map
		if (attribute.getValue() != null) {
			List<String> mails = attribute.valueAsList();
			for (String mail : mails) {
				reservedMailsAttributeValue.putIfAbsent(mail, String.valueOf(user.getId()));
			}
		}

		// save changes in entityless attribute
		try {
			// always set value to attribute, since we might start with null in attribute and empty map in variable !!
			reservedMailsAttribute.setValue(reservedMailsAttributeValue);
			session.getPerunBl().getAttributesManagerBl().setAttribute(session, usedMailsKeyVsup, reservedMailsAttribute);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("vsupExchangeMailAliases");
		attr.setDisplayName("School mail aliases");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Spare school mail aliases of a user. It contains all former addresses of user from @vsup.cz namespace. Values are filled manually.");
		return attr;
	}

}
