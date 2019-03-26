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
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_resource_attribute_def_def_mailingListManagerEmail extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException{
		if (attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, resource, "Attribute value is null.");
		}

		perunSession.getPerunBl().getModulesUtilsBl().isNameOfEmailValid(perunSession, (String)attribute.getValue());


	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("mailingListManagerEmail");
		attr.setDisplayName("Mailing list manager email.");
		attr.setType(String.class.getName());
		attr.setDescription("Email of owner of mailing list");
		return attr;
	}
}
