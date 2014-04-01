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
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 */
public class urn_perun_facility_attribute_def_def_smb_write_conf extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {


	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		/*Integer value = (Integer) attribute.getValue();
			if(value == null) {
			throw new WrongAttributeValueException(attribute, "Attribute was not filled, therefore there is nothing to be checked.");
			}
			if(!(value == 1 || value == 0)) {
			throw new WrongAttributeValueException("0 and 1 are only allowed values");
			}*/
	}


	public Attribute fillAttribute(PerunSessionImpl perunSession, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		/*Attribute attr = new Attribute(attribute);
			attr.setValue(1);
			return attr;*/
		return new Attribute();
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("smb_write_conf");
		attr.setType(String.class.getName());
		attr.setDescription("Automatically update Samba config files.");
		return attr;
	}
}
