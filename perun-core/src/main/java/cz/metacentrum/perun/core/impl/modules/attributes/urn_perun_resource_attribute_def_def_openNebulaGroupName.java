package cz.metacentrum.perun.core.impl.modules.attributes;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

/**
 * Module for OpenNebula group name
 * Checks if the group is not null.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_resource_attribute_def_def_openNebulaGroupName extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongReferenceAttributeValueException {
		if (attribute.valueAsString() == null) {
			throw new WrongReferenceAttributeValueException(attribute, null, resource, null, "Attribute value is invalid. Group name can't be empty.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("openNebulaGroupName");
		attr.setDisplayName("OpenNebula Group Name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of group in OpenNebula defined by resource in Perun");
		return attr;
	}
}