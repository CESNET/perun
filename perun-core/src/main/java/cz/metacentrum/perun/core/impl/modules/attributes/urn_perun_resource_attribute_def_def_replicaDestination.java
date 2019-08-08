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
 * @author Simona Kruppova, Oliver Mrazik
 */
public class urn_perun_resource_attribute_def_def_replicaDestination extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {

		if(attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, resource, "Destination for FS replica can't be empty.");
		}

		if (!perunSession.getPerunBl().getModulesUtilsBl().isFQDNValid(perunSession, (String) attribute.getValue())) {
			throw new WrongAttributeValueException(attribute, resource, "Bad replicaDestination attribute format " + attribute.getValue() + ". It should be " +
					"fully qualified domain name.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("replicaDestination");
		attr.setDisplayName("Replica destination");
		attr.setType(String.class.getName());
		attr.setDescription("Fully qualified domain name (FQDN) of the target storage.");
		return attr;
	}

}
