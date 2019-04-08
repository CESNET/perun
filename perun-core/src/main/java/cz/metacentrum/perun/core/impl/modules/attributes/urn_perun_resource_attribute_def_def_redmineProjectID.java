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
 * Module for redmine project ID
 *
 * @author Peter Balcirak <peter.balcirak@gmail.com>
 * @date 29.3.2016
 */
public class urn_perun_resource_attribute_def_def_redmineProjectID extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[a-z][-_a-z0-9]+$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		String id = (String) attribute.getValue();
		if (id == null) {
			throw new WrongAttributeValueException(attribute, resource, "Attribute can't be empty. It can start with a-z and then a-z, 0-9, _ or -");
		}

		Matcher match = pattern.matcher(id);

		if (!match.matches()) {
			throw new WrongAttributeValueException(attribute, resource, "Bad format of attribute redmineProjectID. It can start with a-z and then a-z, 0-9, _ or -");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("redmineProjectID");
		attr.setDisplayName("Redmine project ID");
		attr.setType(String.class.getName());
		attr.setDescription("ID for redmine project.");
		return attr;
	}
}
