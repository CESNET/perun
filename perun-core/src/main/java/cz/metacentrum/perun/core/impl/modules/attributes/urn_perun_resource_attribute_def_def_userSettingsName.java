package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

/**
 * @author Vojtech Sassmann &lt;vojtech.sassmann@gmail.com&gt;
 */
public class urn_perun_resource_attribute_def_def_userSettingsName extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {
	@Override
	public Attribute fillAttribute(PerunSessionImpl session, Resource resource, AttributeDefinition attribute) {
		Attribute toReturn = new Attribute(attribute);
		toReturn.setValue(resource.getName());
		return toReturn;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("userSettingsName");
		attr.setDisplayName("User settings name");
		attr.setType(String.class.getName());
		attr.setDescription("Name displayed in user profile resource settings. To display certain resource in user profile settings this attribute value needs to be set.");
		return attr;
	}
}
