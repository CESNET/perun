package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Ajax query which sets attributes with a new value
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class SetAttributes {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "attributesManager/setAttributes";

	// IDs
	private Map<String, Integer> ids = new HashMap<String, Integer>();
	private ArrayList<Attribute> attributes = new ArrayList<Attribute>();

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new request
	 */
	public SetAttributes() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events externalEvents
	 */
	public SetAttributes(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to set new value for some attributes
	 *
	 * @param ids defines which type of attribute will be set (member, user, member_resource, etc.)
	 * @param attributes list of attributes with a new value
	 */
	public void setAttributes(final Map<String, Integer> ids, final ArrayList<Attribute> attributes)
	{

		this.ids = ids;
		this.attributes = attributes;

		// test arguments
		if(!this.testSetting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Setting new values for attributes failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("New values for attributes successfully updated in DB !");
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
	private boolean testSetting()
	{
		boolean result = true;
		String errorMsg = "";

		if(this.ids.isEmpty()){
			errorMsg += "Wrong attribute type value.\n";
			result = false;
		}

		// silent skip - used by save changes buttons on attributes pages
		if (attributes == null || attributes.isEmpty()) {
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while setting attributes", new Label(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject(){

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();

		// create attrs field
		JSONArray array = new JSONArray();

		// create attributes
		for (int i=0; i<attributes.size(); i++) {

			// skip attribute with empty or null value
			if (attributes.get(i).getValue() == null || attributes.get(i).getValue().equalsIgnoreCase("")){
				continue;
			}

			// create Json object from attribute
			JSONObject attr = new JSONObject(attributes.get(i));

			// get only interested in properties
			JSONValue id = attr.get("id");
			JSONValue friendlyName = attr.get("friendlyName");
			JSONValue namespace = attr.get("namespace");
			JSONValue type = attr.get("type");
			JSONValue description = attr.get("description");
			JSONValue value = attr.get("value");
			JSONValue displayName = attr.get("displayName");

			// create new Attribute jsonObject
			JSONObject newAttr = new JSONObject();
			newAttr.put("value", value);
			newAttr.put("id", id);
			newAttr.put("type", type);
			newAttr.put("description", description);
			newAttr.put("namespace", namespace);
			newAttr.put("friendlyName", friendlyName);
			newAttr.put("displayName", displayName);
			newAttr.put("unique", attr.get("unique"));

			// put attribute into array
			array.set(array.size(), newAttr);

		}

		for (Map.Entry<String, Integer> attrIds : this.ids.entrySet()) {
			jsonQuery.put(attrIds.getKey(),new JSONNumber(attrIds.getValue()));
		}

		jsonQuery.put("attributes",array);

		return jsonQuery;

	}

	/**
	 * Sets external events after callback creation
	 *
	 * @param events
	 */
	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}
