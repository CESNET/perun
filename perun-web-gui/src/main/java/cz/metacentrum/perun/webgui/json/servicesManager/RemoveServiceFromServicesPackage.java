package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which adds service to service package.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveServiceFromServicesPackage {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service id
	private int serviceId = 0;
	// services package id
	private int packageId = 0;
	// URL to call
	final String JSON_URL = "servicesManager/removeServiceFromServicesPackage";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public RemoveServiceFromServicesPackage() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public RemoveServiceFromServicesPackage(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding()
	{
		boolean result = true;
		String errorMsg = "";

		if(serviceId == 0){
			errorMsg += "Wrong parameter 'Service ID'.</br>";
			result = false;
		}

		if(packageId == 0){
			errorMsg += "Wrong parameter 'Services package ID'.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to remove Service from services package, it first tests the values and then submits them.
	 *
	 * @param packageId ID of services package to remove service from
	 * @param serviceId ID of service to be removed from services package
	 */
	public void removeServiceFromServicesPackage(int packageId, int serviceId) {

		this.serviceId = serviceId;
		this.packageId = packageId;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing service from services package failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service removed from services package.");
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
		jsonQuery.put("service", new JSONNumber(serviceId));
		jsonQuery.put("servicesPackage", new JSONNumber(packageId));
		return jsonQuery;
	}

}
