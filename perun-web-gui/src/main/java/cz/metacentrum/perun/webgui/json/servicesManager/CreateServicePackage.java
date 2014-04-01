package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which creates a new service package
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateServicePackage {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// services package name
	private String packageName = "";
	// services package description
	private String packageDescription = "";
	// URL to call
	final String JSON_URL = "servicesManager/createServicesPackage";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public CreateServicePackage() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public CreateServicePackage(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

		if(packageName.length() == 0){
			errorMsg += "You must fill in the parameter 'Name'.<br>";
			result = false;
		}

		if(packageDescription.length() == 0){
			errorMsg += "You must fill in the parameter 'Description'.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a new ServicesPackage, it first tests the values and then submits them.
	 *
	 * @param name service package name
	 * @param description service package description
	 */
	public void createServicePackage(final String name, final String description)
	{
		this.packageName = name;
		this.packageDescription = description;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating service package " + packageName + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service package " + packageName + " created.");
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
	private JSONObject prepareJSONObject()
	{
		// service
		JSONObject service = new JSONObject();
		service.put("name", new JSONString(packageName));
		service.put("description", new JSONString(packageDescription));
		service.put("id", null);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("servicesPackage", service);
		return jsonQuery;
	}

}
