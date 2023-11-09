package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check M365 group name to fulfil the following requirements:
 * 	- Maximum length is 64 characters
 * 	- Only contains character in the ASCII character set 0 - 127 except the following: @ () \ [] " ; : <> , SPACE
 *
 * @author Michal Berky <michal.berky@cesnet.cz>
 */
public class urn_perun_resource_attribute_def_def_m365GroupName extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[0-9A-Za-z!#$%&'*+\\-./=?^_`{|}~]*$");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		String groupName = attribute.valueAsString();
		if(groupName == null) return;

		if (groupName.length() > 64) {
			throw new WrongAttributeValueException(attribute, "Group name in M365 mustn't exceed 64 characters.");
		}

		Matcher matcher = pattern.matcher(groupName);
		if (!matcher.matches()) {
			throw new WrongAttributeValueException(attribute, "Group name contains invalid characters. The following characters are not allowed: @ () \\ [] \" ; : <> , SPACE");
		}
	}

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String groupName = attribute.valueAsString();
		if (groupName == null) {
			throw new WrongReferenceAttributeValueException("Group name can't be empty, as it is required by service assigned to this resource.");
		}
	}

		@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("m365GroupName");
		attr.setDisplayName("M365 Group Name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of Group in M365");
		return attr;
	}
}
