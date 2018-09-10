package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.ArrayList;
import java.util.List;

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
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_facility_attribute_def_def_homeMountPoint_passwd_scp extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		String value = (String) attribute.getValue();

		if(value == null) throw new WrongAttributeValueException(attribute, "Value can't be null");
		if(!value.matches("^(/[-_.a-zA-Z0-9]+)+$")) throw new WrongAttributeValueException(attribute, "Wrong format. ^(/[-_.A-z0-9]+)+$ expected.");
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("homeMountPoint_passwd-scp");
		attr.setDisplayName("Home mount point for passwd_scp");
		attr.setType(String.class.getName());
		attr.setDescription("Home mount point for service passwd_scp.");
		return attr;
	}
}
