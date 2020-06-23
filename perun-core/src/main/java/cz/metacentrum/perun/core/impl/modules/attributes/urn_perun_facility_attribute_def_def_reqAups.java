package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Checks if all required AUP for facility are correct (available as keys in urn_perun_entityless_attribute_def_def_orgAups)
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_facility_attribute_def_def_reqAups extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi  {

	private final static String availableAups = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":orgAups";

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws WrongReferenceAttributeValueException {

		List<String> aups = attribute.valueAsList();

		if (aups == null || aups.isEmpty()) return;

		List<Attribute> allAUPS = perunSession.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(perunSession, availableAups);
		if (allAUPS.isEmpty()) return;

		Set<String> keys = new HashSet<>();

		// fill available keys
		for (Attribute a : allAUPS) {
			if (a != null && a.getValue() != null && a.getValue() instanceof LinkedHashMap) {
				LinkedHashMap<String,String> map = a.valueAsMap();
				keys.addAll(map.keySet());
			}
		}

		for (String aup : aups) {
			if (!keys.contains(aup)) throw new WrongReferenceAttributeValueException(attribute, null, facility, null, aup+" AUP doesn't exist in available organization AUPs.");
		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("reqAups");
		attr.setDisplayName("Required AUP");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("List of required AUP names. Users must agree with all recent AUPs before accessing the service represented by this facility.");
		return attr;
	}

}
