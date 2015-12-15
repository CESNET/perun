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
public class CreateCompleteService {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service name
	private String serviceName = "";
	private String scriptPath = "";
	private boolean enabled = true;
	private int defaultDelay = 10;

	// URL to call
	final String JSON_URL = "servicesManager/createCompleteService";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public CreateCompleteService() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public CreateCompleteService(final JsonCallbackEvents events) {
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

		if(scriptPath.length() == 0){
			errorMsg += "You must fill in the parameter 'Script path'.</br>";
			result = false;
		}

		if(defaultDelay <= 0){
			errorMsg += "Parameter 'Default delay' must be > 0.</br>";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a new Service including both ExecServices with dependency,
	 * it first tests the values and then submits them.
	 *
	 * @param name service Name
	 * @param scriptPath Path to the service script, default "./service_name"
	 * @param defaultDelay Delay in minutes when service is rescheduled (10 min is default)
	 * @param enabled TRUE if service should be enabled
	 */
	public void createCompleteService(final String name, final String scriptPath, final int defaultDelay, final boolean enabled)  {

		this.serviceName = name;
		this.scriptPath = scriptPath;
		this.defaultDelay = defaultDelay;
		this.enabled = enabled;

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
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("name", new JSONString(serviceName));
		jsonQuery.put("scriptPath", new JSONString(scriptPath));
		jsonQuery.put("defaultDelay", new JSONNumber(defaultDelay));
		jsonQuery.put("enabled", JSONBoolean.getInstance(enabled));
		return jsonQuery;
	}

}
