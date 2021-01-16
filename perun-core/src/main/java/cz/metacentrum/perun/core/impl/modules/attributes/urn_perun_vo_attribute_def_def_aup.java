package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This attribute holds all AUPs specific for each VO managed by Perun and its services accessed by ProxyIdP.
 *
 * Value is JSON array representation of all custom AUPs
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_vo_attribute_def_def_aup extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Vo vo, Attribute attribute) throws WrongAttributeValueException {

		String value = attribute.valueAsString();

		if (value == null) return;

		// we expect array or AUPs
		try {
			JSONArray array = new JSONArray(value);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				if (!object.has("version")) throw new WrongAttributeValueException(attribute, "AUP is missing key version in JSON.");
				if (!object.has("date")) throw new WrongAttributeValueException(attribute, "AUP is missing key date in JSON.");
				if (!object.has("link")) throw new WrongAttributeValueException(attribute, "AUP is missing key link in JSON.");
				if (!object.has("text")) throw new WrongAttributeValueException(attribute, "AUP is missing key text in JSON.");
			}
		} catch (JSONException ex) {
			throw new WrongAttributeValueException(attribute, "AUP is not valid JSON.", ex);
		}

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("aup");
		attr.setDisplayName("AUP");
		attr.setType(String.class.getName());
		attr.setDescription("Contains all custom AUPs used at VO level.");
		return attr;
	}

}
