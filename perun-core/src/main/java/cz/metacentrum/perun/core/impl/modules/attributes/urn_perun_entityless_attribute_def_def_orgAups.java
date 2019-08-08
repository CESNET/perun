package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This attribute holds all AUPs for each organization/infrastructure managed by Perun and accessed by its ProxyIdP.
 *
 * Keys in a map are infrastructure identifiers
 * Value is JSON array representation of all AUPs
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_entityless_attribute_def_def_orgAups extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongAttributeValueException {

		if(attribute.getValue() == null) return;

		Map<String, String> map = (Map<String, String>) attribute.getValue();

		if(map.isEmpty()) return;

		Set<String> mapKeys = map.keySet();
		for(String mapKey: mapKeys) {

			String value = map.get(mapKey);
			if (value == null || value.isEmpty()) throw new WrongAttributeValueException(attribute, "AUPs for key: '"+mapKey+"' can't be empty.");

			// we expect array or AUPs
			try {
				JSONArray array = new JSONArray(value);
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					if (!object.has("version")) throw new WrongAttributeValueException(attribute, "AUP for key: '"+mapKey+"' is missing key version in JSON.");
					if (!object.has("date")) throw new WrongAttributeValueException(attribute, "AUP for key: '"+mapKey+"' is missing key date in JSON.");
					if (!object.has("link")) throw new WrongAttributeValueException(attribute, "AUP for key: '"+mapKey+"' is missing key link in JSON.");
					if (!object.has("text")) throw new WrongAttributeValueException(attribute, "AUP for key: '"+mapKey+"' is missing key text in JSON.");
				}
			} catch (JSONException ex) {
				throw new WrongAttributeValueException(attribute, "AUP for key: '"+mapKey+"' is not valid JSON.", ex);
			}

		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("orgAups");
		attr.setDisplayName("Organisation AUPs");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Contains all AUPs used at organization/infrastructure level.");
		return attr;
	}

}
