package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static cz.metacentrum.perun.core.impl.Utils.*;

public class urn_perun_user_attribute_def_def_o365UserEmailAddresses_mu extends UserAttributesModuleAbstract {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_o365UserEmailAddresses_mu.class);
	final static String G_D_O365_EMAIL_ADDRESSES_O365_MU_ATTR = AttributesManager.NS_GROUP_ATTR_DEF + ":o365EmailAddresses:o365mu";

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException {
		//empty value is valid
		if(attribute.getValue() == null) return;
		ArrayList<String> emails = attribute.valueAsList();

		//check syntax of all values
		for (String email : emails) {
			Matcher emailMatcher = emailPattern.matcher(email);
			if (!emailMatcher.matches())
				throw new WrongAttributeValueException(attribute, user, "Email " + email + " is not in correct form.");
			Matcher ucoEmailMatcher = ucoEmailPattern.matcher(email);
			if (ucoEmailMatcher.matches())
				throw new WrongAttributeValueException(attribute, user, "Email " + email + " is based on UCO which is not supported.");
		}

		//check for duplicities
		if (hasDuplicate(emails)) {
			throw new WrongAttributeValueException(attribute, user, "duplicate values");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		log.trace("checkAttributeSemantics(member={},attribute={})", user, attribute);

		//empty value is valid
		if(attribute.getValue() == null) return;
		ArrayList<String> emails = attribute.valueAsList();

		//check for duplicities among this attribute and all groups (urn_perun_group_attribute_def_def_o365EmailAddresses_o365mu)
		try {
			Attribute groupO365EmailAddresses = new Attribute(perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, G_D_O365_EMAIL_ADDRESSES_O365_MU_ATTR));
			groupO365EmailAddresses.setValue(emails);
			Set<Pair<Integer, Integer>> groupResourcePairs = perunSession.getPerunBl().getAttributesManagerBl().getPerunBeanIdsForUniqueAttributeValue(perunSession, groupO365EmailAddresses);
			if (!groupResourcePairs.isEmpty()) {
				throw new WrongReferenceAttributeValueException(attribute, groupO365EmailAddresses, user, null, "some of the email addresses are already assigned to the following group_resource pairs: " + groupResourcePairs);
			}
		} catch (AttributeNotExistsException ex) {
			//If attribute not exists, we can log it and skip it, because there are no duplicates in not existing attributes
			log.debug("Attribute {} not exists to check duplicities in it while checkAttributeSemantics for {}.", G_D_O365_EMAIL_ADDRESSES_O365_MU_ATTR, attribute);
		}
	}

	@Override
	public List<String> getDependencies() {
		return Collections.singletonList(G_D_O365_EMAIL_ADDRESSES_O365_MU_ATTR);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("o365UserEmailAddresses:mu");
		attr.setDisplayName("User managed email addresses for MU o365");
		attr.setType(ArrayList.class.getName());
		attr.setUnique(true);
		attr.setDescription("User managed email addresses for MU o365");
		return attr;
	}
}
