package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which creates a new facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateFacility {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// facility
	private String name = "";
	private String description;
	// URL to call
	final String JSON_URL = "facilitiesManager/createFacility";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public CreateFacility() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public CreateFacility(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false when process can/can't continue
	 */
	private boolean testCreating() {

		boolean result = true;
		String errorMsg = "";

		if(name.length() == 0){
			errorMsg += "Facility parameter <strong>Name</strong> can't be empty.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a new Facility, it first tests the values and then submits them.
	 *
	 * @param name Facility name
	 */
	public void createFacility(final String name, final String description) {

		this.name = name;
		this.description = description;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating facility " + name + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Facility " + name + " created.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		// create request
		JsonPostClient request = new JsonPostClient(newEvents);
		request.sendData(JSON_URL, jsonQuery);

	}

	/**
	 * Prepares a JSON object
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {
		// facility
		JSONObject facility = new JSONObject();
		facility.put("name", new JSONString(name));
		facility.put("description", new JSONString(description));
		facility.put("id", new JSONNumber(0));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", facility);
		return jsonQuery;
	}

}
