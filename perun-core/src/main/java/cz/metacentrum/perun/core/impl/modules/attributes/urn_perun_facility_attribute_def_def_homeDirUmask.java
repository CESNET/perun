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

/**
 * Checks if the value is valid unix permission mask.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_homeDirUmask extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	/**
	 * Method for checking permission mask of home directory.
	 */
	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

		String mask = (String) attribute.getValue();

		if (mask != null) {
			if(!mask.matches("[0-7]{3,4}")) {
				throw new WrongAttributeValueException(attribute, "Bad unix permission mask in attribute format " + mask);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("homeDirUmask");
		attr.setDisplayName("Permissions for home");
		attr.setType(String.class.getName());
		attr.setDescription("Unix permissions, which will be applied when new home folder is created.");
		return attr;
	}
}
