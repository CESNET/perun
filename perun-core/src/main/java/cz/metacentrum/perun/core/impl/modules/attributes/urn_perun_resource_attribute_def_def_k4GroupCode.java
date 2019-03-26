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
 * Check max length and presence of group Code in K4.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_resource_attribute_def_def_k4GroupCode extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {

		if (attribute.getValue() == null) throw new WrongAttributeValueException("Code of Group in K4 can't be empty.");
		if (((String)attribute.getValue()).length() > 20) throw new WrongAttributeValueException("Code of Group in K4 musn`t exceed 20 characters.");

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("k4GroupCode");
		attr.setDisplayName("K4 Group Code");
		attr.setType(String.class.getName());
		attr.setDescription("Code of Group in K4 (visible to users).");
		return attr;
	}
}
