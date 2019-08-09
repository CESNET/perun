package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for service 'rt' used for setting output file name
 * The path is always '/tmp/{name}'
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 9.8.2019
 */
public class urn_perun_facility_attribute_def_def_rtOutputFileName extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	Pattern fileNamePattern = Pattern.compile("^[-_a-zA-Z0-9]+$");

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws WrongAttributeValueException {

		//attribute can be empty
		if (attribute.getValue() == null) {
			return;
		}

		String value = (String) attribute.getValue();

		Matcher matcher = fileNamePattern.matcher(value);
		if (!matcher.matches()) {
			throw new WrongAttributeValueException(attribute, facility, "Name of the file can contain only alphabet, numeric, dash and underscore characters.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("rtOutputFileName");
		attr.setDisplayName("RT output file name");
		attr.setType(String.class.getName());
		attr.setDescription("Name of the file to which will Perun save the output file from the service 'rt' in the end device. It is always '/tmp/{nameOfTheFile}'.");
		return attr;
	}
}
