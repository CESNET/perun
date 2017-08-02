package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for names of target users (to whose accounts are sshkeys stored).
 *
 * @author Zdenek Strmiska <zdenek.strm@gmail.com>
 * @date 27.7.2017
 */


public class urn_perun_resource_attribute_def_def_sshkeysTargetUser {

	Pattern pattern = Pattern.compile("^(?!-)[-_.a-zA-Z0-9]+$");

	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String key = (String) attribute.getValue();
		if (key == null) {
			throw new WrongAttributeValueException(attribute, resource, "Name of the user can't be empty");
		}

		Matcher match = pattern.matcher(key);

		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, resource, "Bad format of attribute sshkeysTargetUser (only letters, numbers and '.' '_' '-' are allowed. Cannot begin with '-').");
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("sshkeysTargetUser");
		attr.setDisplayName("Target user for ssh keys");
		attr.setType(String.class.getName());
		attr.setDescription("Target user for ssh keys");
		return attr;
	}
}