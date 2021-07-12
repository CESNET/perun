package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.regex.Pattern;

public class urn_perun_group_attribute_def_def_adGroupName extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("[A-Za-z0-9 _-]+");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongAttributeValueException {
		//Attribute can be null
		if (attribute.getValue() == null) return;

		if (!pattern.matcher(attribute.valueAsString()).matches()) {
			throw new WrongAttributeValueException(attribute, "Invalid attribute adGroupName value. It should contain only letters, digits, underscores, dashes or spaces.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("adGroupName");
		attr.setDisplayName("AD Group Name");
		attr.setType(String.class.getName());
		attr.setDescription("AD group name which is used to compose full name of the group in AD.");
		return attr;
	}
}
