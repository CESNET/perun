package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

import java.util.HashMap;
import java.util.Map;

/**
 * Ajax query which sets attribute with a new value
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class SetAttribute {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "attributesManager/setAttribute";

	// IDs
	private Map<String, Integer> ids = new HashMap<String, Integer>();
	private Attribute attribute = null;

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new request
	 *
	 */
	public SetAttribute() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public SetAttribute(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to set new value for some attribute
	 *
	 * @param ids defines which type of attribute will be set (member, user, member_resource, etc.)
	 * @param attribute attribute object with a new value
	 */
	public void setAttribute(final Map<String, Integer> ids, final Attribute attribute){

		this.ids = ids;
		this.attribute = attribute;

		// test arguments
		if(!this.testSetting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Setting new value for attribute: "+ attribute.getId() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("New value for attribute: " + attribute.getId() + " successfully updated in DB !");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testSetting() {

		boolean result = true;
		String errorMsg = "";

		if(this.ids.isEmpty()){
			errorMsg += "Wrong attribute type value.\n";
			result = false;
		}

		// skip attribute with empty or null value
		if (attribute.getValue() == null || attribute.getValue().equalsIgnoreCase("")){
			errorMsg += "Can't save attribute with null or empty value.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while setting attribute", new Label(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		// create Json object from attribute
		JSONObject attr = new JSONObject(attribute);

		// get only interested in properties
		JSONValue id = attr.get("id");
		JSONValue friendlyName = attr.get("friendlyName");
		JSONValue namespace = attr.get("namespace");
		JSONValue type = attr.get("type");
		JSONValue description = attr.get("description");
		JSONValue value = attr.get("value");

		// create new Attribute jsonObject
		JSONObject newAttr = new JSONObject();
		newAttr.put("value", value);
		newAttr.put("id", id);
		newAttr.put("type", type);
		newAttr.put("description", description);
		newAttr.put("namespace", namespace);
		newAttr.put("friendlyName", friendlyName);
		newAttr.put("displayName", attr.get("displayName"));
		newAttr.put("unique", attr.get("unique"));

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();

		for (Map.Entry<String, Integer> attrIds : this.ids.entrySet()) {
			jsonQuery.put(attrIds.getKey(),new JSONNumber(attrIds.getValue()));
		}

		jsonQuery.put("attribute",newAttr);

		return jsonQuery;
	}

}
