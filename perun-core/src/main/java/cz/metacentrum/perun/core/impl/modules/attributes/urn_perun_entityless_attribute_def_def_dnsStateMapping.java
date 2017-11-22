package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Entityless attribute for mapping values of DNS address to specific state (or empty string).
 *
 * Examples:
 * <BR>.cz = “Czech Republic”
 * <BR>.uk = “United Kingdom”
 * <BR>.ebi.ac.uk = “”
 *
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 */
@SuppressWarnings("unchecked")
public class urn_perun_entityless_attribute_def_def_dnsStateMapping extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	static String KEY = "config";

	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		if(!KEY.equals(key)) {
			throw new WrongAttributeValueException(attribute, key, "entityless key for this attribute must be " + KEY);
		}

		if(attribute.getValue() == null) return;
		LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) attribute.getValue();
		if(map.isEmpty()) return;

		//check keys and values
		Set<String> mapKeys = map.keySet();
		for(String mapKey : mapKeys) {
			//check keys
			if(mapKey == null) throw new WrongAttributeValueException(attribute, key, "Key in dnsStateMapping map can not be null.");

			//check values
			String value = map.get(mapKey);
			if(value == null) throw new WrongAttributeValueException(attribute, key, "Value in dnsStateMapping map can not be null.");
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("dnsStateMapping");
		attr.setDisplayName("mapping of DNS address to specific state");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Maps together pairs of DNS address and state.");
		return attr;
	}
}
