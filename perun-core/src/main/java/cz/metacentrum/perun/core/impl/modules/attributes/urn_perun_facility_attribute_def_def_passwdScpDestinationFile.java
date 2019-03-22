package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks if the value is absolute unix way to file.
 *
 * @date 19.4.2011 16:50:00
 * @author Michal Šťava   <stava.michal@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_passwdScpDestinationFile extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^(/[-_a-zA-Z0-9]+)+$");

	/**
	 * Method for checking path of the file.
	 * Try to check if the path is equal to pattern ^(/[-_a-zA-Z0-9]+)+$
	 */
	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		String path = (String) attribute.getValue();
		if (path == null) {
			throw new WrongAttributeValueException(attribute, "Attribute was not filled, therefore there is nothing to be checked.");
		}
		Matcher matcher = pattern.matcher(path);
		if (!matcher.matches()) throw new WrongAttributeValueException(attribute, "Bad path to destination of file in attribute format " + path);
	}

	/**
	 * Method for filling path of the file.
	 * Return attribute with value equal null.
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {

		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("passwd_scp_destination_file");
		attr.setDisplayName("Destination file for passwd_scp");
		attr.setType(String.class.getName());
		attr.setDescription("Path where passwd file (for passwd_scp service) is stored. (Typicaly: /etc/passwd)");
		return attr;
	}
}