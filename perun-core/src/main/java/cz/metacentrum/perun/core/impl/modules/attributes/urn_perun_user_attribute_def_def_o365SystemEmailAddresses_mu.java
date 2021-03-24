package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static cz.metacentrum.perun.core.impl.Utils.emailPattern;
import static cz.metacentrum.perun.core.impl.Utils.hasDuplicate;

public class urn_perun_user_attribute_def_def_o365SystemEmailAddresses_mu extends UserAttributesModuleAbstract {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_o365SystemEmailAddresses_mu.class);

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
		}

		//check for duplicities
		if (hasDuplicate(emails)) {
			throw new WrongAttributeValueException(attribute, user, "duplicate values");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		log.trace("checkAttributeSemantics(user={},attribute={})", user, attribute);

		//empty value is valid
		if(attribute.getValue() == null) return;
		ArrayList<String> emails = attribute.valueAsList();

		//No need to check duplicities, attribute is unique
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("o365SystemEmailAddresses:mu");
		attr.setDisplayName("System managed email addresses for MU o365");
		attr.setType(ArrayList.class.getName());
		attr.setUnique(true);
		attr.setDescription("System managed email addresses for MU o365");
		return attr;
	}
}
