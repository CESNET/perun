package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

/**
 * Module for redmine role
 * Checks if the role has correct value of either Manager, Reporter or Developer
 * @author Marek Hrasna marekhrasna@outlook.com
 * @date 04.03.16.
 */
public class urn_perun_resource_attribute_def_def_redmineRole extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		String role = (String)attribute.getValue();

		if (role == null) {
			throw new WrongAttributeValueException(attribute, resource, "Attribute value is invalid. The role can be either Manager, Reporter or Developer");
		}
		else if (!role.equals("Manager") && !role.equals("Reporter") && !role.equals("Developer")) {
			throw new WrongAttributeValueException(attribute, resource, "Attribute value is invalid. The role can be either Manager, Reporter or Developer");
		}


	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("redmineRole");
		attr.setDisplayName("Redmine role");
		attr.setType(String.class.getName());
		attr.setDescription("The role can be either Manager, Reporter or Developer");
		return attr;
	}
}