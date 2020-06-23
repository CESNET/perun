package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

/**
 * Checks and fills at specified facility uid namespace.
 *
 * @date 22.4.2011 10:43:48
 * @author Lukáš Pravda   <luky.pravda@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_uid_namespace extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	/**
	 * Checks if the corresponding attribute u:uid-namespace:[namespace] exists.
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl session, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException {
		if(attribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "Missing uid namespace for facility.");

		String userFacilityUidNamespaceAttributeName =
			AttributesManager.NS_USER_ATTR_DEF + ":" + attribute.getFriendlyName() + ":" + attribute.getValue();

		try {
			session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, userFacilityUidNamespaceAttributeName);
		} catch (AttributeNotExistsException e) {
			throw new WrongReferenceAttributeValueException("Attribute " + userFacilityUidNamespaceAttributeName + " doesn't exists");
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
		attr.setFriendlyName("uid-namespace");
		attr.setDisplayName("UID namespace");
		attr.setType(String.class.getName());
		attr.setDescription("Namespace for UIDs which can be used at specific facility.");
		return attr;
	}

}
