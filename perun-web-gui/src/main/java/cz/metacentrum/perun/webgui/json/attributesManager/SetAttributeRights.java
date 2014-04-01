package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.AttributeRights;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;

/**
 * Ajax query which sets attribute rights
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SetAttributeRights {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "attributesManager/setAttributeRights";

	// IDs
	private ArrayList<AttributeRights> rights = new ArrayList<AttributeRights>();
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new request
	 */
	public SetAttributeRights() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events externalEvents
	 */
	public SetAttributeRights(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to set new value for some attributes
	 *
	 * @param rights List of attribute rights to set
	 */
	public void setAttributeRights(final ArrayList<AttributeRights> rights) {

		this.rights = rights;

		// test arguments
		if(!this.testSetting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Setting attribute rights failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Attribute rights are successfully updated.");
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

		if (rights == null || rights.isEmpty()) {
			// TODO
			return false;
		}
		return true;

	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject(){

		JSONObject query = new JSONObject();
		JSONArray array = new JSONArray();

		for (int i=0; i<rights.size(); i++) {
			JSONObject obj = new JSONObject(rights.get(i));
			array.set(i, obj);
		}

		query.put("rights", array);

		return query;

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
