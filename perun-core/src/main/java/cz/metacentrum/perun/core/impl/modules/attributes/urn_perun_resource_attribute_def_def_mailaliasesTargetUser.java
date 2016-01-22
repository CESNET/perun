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
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks mail aliases target user
 *
 * @author Michal Stava   <stavamichal@gmail.com>
 */
public class urn_perun_resource_attribute_def_def_mailaliasesTargetUser extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	Pattern userPattern = Pattern.compile("^[-a-zA-Z0-9_]+$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if (attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, resource, null, "Attribute value can't be null.");
		}

		String targetUser = (String) attribute.getValue();
		Matcher userMatcher = userPattern.matcher(targetUser);
		if(!userMatcher.matches()) {
			throw new WrongAttributeValueException(attribute, resource, null, "Matcher must match to ^[-a-zA-Z0-9_]+$ pattern.");
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("mailaliasesTargetUser");
		attr.setDisplayName("Target user for mailaliases");
		attr.setType(String.class.getName());
		attr.setDescription("Target user for mailaliases settings.");
		return attr;
	}
}