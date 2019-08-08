package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resource vomsRoles attribute.
 *
 * @author Vojtech Sassmann &lt;vojtech.sassmann@gmail.com&gt;
 */
public class urn_perun_resource_attribute_def_def_vomsRoles extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[^<>&]*$");

	@Override
	@SuppressWarnings("unchecked")
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) {
			return;
		}
		try {
			List<String> vomRoles = (List<String>) attribute.getValue();
			for (String vomRole : vomRoles) {
				Matcher matcher = pattern.matcher(vomRole);
				if(!matcher.matches()) {
					throw new WrongAttributeValueException(attribute, "Bad resource vomsRoles value. It should not contain '<>&' characters.");
				}
			}
		} catch (ClassCastException e) {
			throw new WrongAttributeValueException(attribute, "Value should be a list of Strings.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setFriendlyName("vomsRoles");
		attr.setDisplayName("Voms roles");
		attr.setDescription("Default roles of all people assigned to this resource.");
		attr.setType(ArrayList.class.getName());
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		return attr;
	}
}
