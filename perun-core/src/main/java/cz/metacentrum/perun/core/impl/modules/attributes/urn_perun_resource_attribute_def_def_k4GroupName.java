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
 * Check max length of group name in K4.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_resource_attribute_def_def_k4GroupName extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {

		if (attribute.getValue() != null &&
				((String)attribute.getValue()).length() > 40) throw new WrongAttributeValueException("Name of Group in K4 musn`t exceed 40 characters.");

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("k4GroupName");
		attr.setDisplayName("K4 Group Name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of Group in K4.");
		return attr;
	}
}
