package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which creates a new service.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateService {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service name
	private String serviceName = "";
	private String description = "";
	private String script = "";
	private boolean enabled = true;
	private int delay = 10;
	private int recurrence = 2;

	// URL to call
	final String JSON_URL = "servicesManager/createService";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public CreateService() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public CreateService(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testCreating() {

		boolean result = true;
		String errorMsg = "";

		if(serviceName.length() == 0){
			errorMsg += "You must fill in the parameter 'Name'.</br>";
			result = false;
		}

		if(script.length() == 0){
			errorMsg += "You must fill in the parameter 'Script path'.</br>";
			result = false;
		}

		if(delay <= 0){
			errorMsg += "Parameter 'Delay' must be > 0.</br>";
			result = false;
		}

		if(recurrence <= 0){
			errorMsg += "Parameter 'Recurrence' must be > 0.</br>";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a new Service,
	 * it first tests the values and then submits them.
	 */
	public void createService(final String name, final String description, final int delay, final int recurrence, final boolean enabled, final String script) {

		this.serviceName = name;
		this.description = description;
		this.delay = delay;
		this.recurrence = recurrence;
		this.enabled = enabled;
		this.script = script;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating service " + serviceName + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service " + serviceName + " created.");
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
	 * Prepares a JSON object
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		// whole JSON query
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", new JSONString(serviceName));
		jsonObject.put("description", new JSONString(description));
		jsonObject.put("recurrence", new JSONNumber(recurrence));
		jsonObject.put("script", new JSONString(script));
		jsonObject.put("delay", new JSONNumber(delay));
		jsonObject.put("enabled", JSONBoolean.getInstance(enabled));

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("service", jsonObject);
		return jsonQuery;
	}

}
