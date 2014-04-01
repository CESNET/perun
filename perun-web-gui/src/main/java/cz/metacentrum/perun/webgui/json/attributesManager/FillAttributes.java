package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
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
 * Ajax query which fills attributes for Resource and Member_Resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class FillAttributes {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "attributesManager/fillAttributes";

	// IDs
	private Map<String, Integer> ids = new HashMap<String, Integer>();
	private ArrayList<Attribute> attributes = new ArrayList<Attribute>();

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new request
	 */
	public FillAttributes() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events externalEvents
	 */
	public FillAttributes(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to fill some attributes on entity
	 *
	 * @param ids defines which type of attribute will be filled (member_resource, resource etc.)
	 * @param attributes list of attributes to fill
	 */
	public void fillAttributes(final Map<String, Integer> ids, final ArrayList<Attribute> attributes)
	{

		this.ids = ids;
		this.attributes = attributes;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Filling attributes failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Attributes successfully filled !");
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
	private boolean testRemoving()
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
			Confirm c = new Confirm("Error while filling attributes", new Label(errorMsg), true);
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

		// create attributes list
		for (int i=0; i<attributes.size(); i++) {

			JSONObject original = new JSONObject(attributes.get(i));

			// reconstruct object
			JSONObject newAttr = new JSONObject();
			newAttr.put("id", original.get("id"));
			newAttr.put("friendlyName", original.get("friendlyName"));
			newAttr.put("namespace", original.get("namespace"));
			newAttr.put("description", original.get("description"));
			newAttr.put("type", original.get("type"));
			newAttr.put("value", original.get("value"));
			newAttr.put("displayName", original.get("displayName"));

			array.set(i, newAttr); // array contains attributes

		}

		// list of entities (facility, resource etc.)
		for (Map.Entry<String, Integer> attrIds : this.ids.entrySet()) {
			jsonQuery.put(attrIds.getKey(),new JSONNumber(attrIds.getValue()));
		}
		// list of attribute's ids
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
