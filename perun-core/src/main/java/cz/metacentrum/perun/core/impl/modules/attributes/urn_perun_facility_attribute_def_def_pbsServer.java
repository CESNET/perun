package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

import java.util.List;

/**
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_pbsServer extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException {
		String pbsServer = attribute.valueAsString();

		if (pbsServer == null) throw new WrongReferenceAttributeValueException(attribute, "PbsServer cannot be null.");

		//TODO better method for searching Facility by querry in DB
		List<Facility> allFacilities = perunSession.getPerunBl().getFacilitiesManagerBl().getFacilities(perunSession);
		boolean success = false;
		for(Facility f: allFacilities) {
			if(f.getName().equals(pbsServer)) {
				success = true;
				break;
			}
		}
		try {
			Attribute name = new Attribute(perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, AttributesManager.NS_FACILITY_ATTR_CORE + ":name"));
			name.setValue(pbsServer);
			if(!success) throw new WrongReferenceAttributeValueException(attribute, name, facility, null, "There is no facility with attribute name same as the pbsServer attribute.");
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl session, Facility facility, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("pbsServer");
		attr.setDisplayName("PBS server");
		attr.setType(String.class.getName());
		attr.setDescription("PBS server which controls this facility.");
		return attr;
	}

}
