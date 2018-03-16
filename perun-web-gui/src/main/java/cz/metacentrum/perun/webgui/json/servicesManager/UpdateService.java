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
import cz.metacentrum.perun.webgui.model.Service;

/**
 * Ajax query which creates a new service.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class UpdateService {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service name
	Service service;

	// URL to call
	final String JSON_URL = "servicesManager/updateService";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public UpdateService() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public UpdateService(final JsonCallbackEvents events) {
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

		if(service.getName().length() == 0){
			errorMsg += "You must fill in the parameter 'Name'.</br>";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to update Service, it first tests the values and then submits them.
	 *
	 * @param service service
	 */
	public void updateService(final Service service)
	{
		this.service = service;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating service " + service.getName() + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service " + service.getName()+ " updated.");
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
		JSONObject newService = new JSONObject();
		newService.put("id", new JSONNumber(service.getId()));
		newService.put("name", new JSONString(service.getName()));
		if (service.getDescription() != null) {
			newService.put("description", new JSONString(service.getDescription()));
		} else {
			newService.put("description", null);
		}
		newService.put("delay", new JSONNumber(service.getDelay()));
		newService.put("recurrence", new JSONNumber(service.getRecurrence()));
		newService.put("enabled", JSONBoolean.getInstance(service.isEnabled()));
		newService.put("script", new JSONString(service.getScriptPath()));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("service", newService);           // service object
		return jsonQuery;
	}

}
