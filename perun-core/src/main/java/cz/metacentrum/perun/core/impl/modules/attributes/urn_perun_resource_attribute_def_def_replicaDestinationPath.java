package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simona Kruppova, Oliver Mrazik
 */
public class urn_perun_resource_attribute_def_def_replicaDestinationPath extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {

		if(attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, resource, "This attribute can't be empty");
		}

		Pattern pattern = Pattern.compile("^/[-a-zA-Z.0-9_/]*$");
		Matcher match = pattern.matcher((String) attribute.getValue());
		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, resource, "Bad replicaDestinationPath attribute format " + attribute.getValue());
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("replicaDestinationPath");
		attr.setDisplayName("Replica destination path");
		attr.setType(String.class.getName());
		attr.setDescription("Absolute path in the target storage to copy to");
		return attr;
	}
}
