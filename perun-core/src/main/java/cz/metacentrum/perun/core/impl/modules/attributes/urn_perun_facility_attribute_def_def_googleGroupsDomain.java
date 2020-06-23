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
 * Module of googleGroupsDomain attribute
 *
 * @author Michal Holič  holic.michal@gmail.com
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_facility_attribute_def_def_googleGroupsDomain extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException {
		if(attribute.getValue() == null) throw new WrongReferenceAttributeValueException(attribute, null, facility, null, "Attribute value can't be null");

		// we don't allow dots in attribute friendlyName, so we convert domain dots to dash.
		String namespace = (attribute.valueAsString()).replaceAll("\\.", "-");

		try {
			sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace:" + namespace);
		} catch (AttributeNotExistsException e) {
			throw new WrongReferenceAttributeValueException(attribute, null, e);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("googleGroupsDomain");
		attr.setDisplayName("Google groups domain");
		attr.setType(String.class.getName());
		attr.setDescription("Google groups domain on facility. Namespace attribute for group names is derived from the domain value by replacing all dots by dashes.");
		return attr;
	}
}
