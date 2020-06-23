package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Module for capabilities according to AARC specification. i.e. specification of resource and optional actions.
 * Syntax:
 * value=“res:RESOURCE[:CHILD-RESOURCE1][:CHILD-RESOURCE2]...[:act:ACTION[,ACTION]...]”
 * Example1: value=“res:RESOURCE1:CHILD-RESOURCE1:CHILD-RESOURCE2:act:ACTION1,ACTION2,ACTION3”
 * Example2: value=“res:RESOURCE1:CHILD-RESOURCE1:CHILD-RESOURCE2”
 *
 * @see urn_perun_facility_attribute_def_def_capabilities
 *
 * @author Ondrej Ernst
 */
public class urn_perun_resource_attribute_def_def_capabilities extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final String RESOURCE_ALLOWED_CHARACTERS = "[\\-a-zA-Z0-9()+,.=@;$_!*']";
	private static final String ACTION_ALLOWED_CHARACTERS = "[\\-a-zA-Z0-9()+.:=@;$_!*']";
	private static final String NOT_ALLOWED_PARTS = "?!(:act$|:act:|.*:,|.*,:|.*::|.*:$)";
	protected static final Pattern PATTERN = Pattern.compile("res((" + NOT_ALLOWED_PARTS + ")(:" +
			RESOURCE_ALLOWED_CHARACTERS + "+))+(:act:((" + ACTION_ALLOWED_CHARACTERS +
			"+,)*(" + ACTION_ALLOWED_CHARACTERS + "+)))?");


	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		if (attribute.getValue() == null) return;
		List<String> values = attribute.valueAsList();
		for (String value : values) {
			if (!PATTERN.matcher(value).matches()) {
				throw new WrongAttributeValueException(attribute, resource.getName() + " has attribute whose value is not valid. Example of valid value: res:RESOURCE[:CHILD-RESOURCE1][:CHILD-RESOURCE2]...[:act:ACTION[,ACTION]...]");
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("capabilities");
		attr.setDisplayName("Capabilities");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Capabilities according to AARC specification. i.e. specification of resource and optional actions.");
		return attr;
	}
}
