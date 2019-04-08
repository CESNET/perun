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
 * Attribute module for apacheAuthzFile attribute. Module checks that
 * attribute is not empty and it also contains unix-like file path.
 *
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 */
public class urn_perun_resource_attribute_def_def_apacheAuthzFile extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^(/[-_a-zA-Z0-9.?*+$%]+)+$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		if (attribute.getValue() == null) {
			throw new WrongAttributeValueException(attribute, resource, null, "Attribute value can not be null.");
		}

		//checks for valid unix file path in attribute based on pattern
		Matcher match = pattern.matcher((String) attribute.getValue());

		if(!match.matches()) {
			throw new WrongAttributeValueException(attribute, resource, null, "Wrong file path format in attribute (should be like '/dir1/dir2').");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("apacheAuthzFile");
		attr.setDisplayName("Apache authz file");
		attr.setType(String.class.getName());
		attr.setDescription("File containing authz entries for Apache.");
		return attr;
	}
}
