package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonErrorHandler;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which adds service to service package.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddServiceToServicesPackage {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service id
	private int serviceId = 0;
	// services package id
	private int packageId = 0;
	// URL to call
	final String JSON_URL = "servicesManager/addServiceToServicesPackage";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public AddServiceToServicesPackage() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AddServiceToServicesPackage(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding() {
		boolean result = true;
		String errorMsg = "";

		if (serviceId == 0) {
			errorMsg += "Wrong parameter 'Service ID'.</br>";
			result = false;
		}

		if (packageId == 0) {
			errorMsg += "Wrong parameter 'Services package ID'.";
			result = false;
		}

		if (errorMsg.length() > 0) {
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to add new Service to service package, it first tests the values and then submits them.
	 *
	 * @param packageId ID of services package to add service to
	 * @param serviceId ID of service to be added to services package
	 */
	public void addServiceToServicesPackage(int packageId, int serviceId) {

		this.serviceId = serviceId;
		this.packageId = packageId;

		// test arguments
		if (!this.testAdding()) {
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding service to services package failed.");
				if (error != null && error.getName().equals("ServiceAlreadyAssignedException")) {
					UiElements.generateError(error, "Service is already assigned", "Service is already assigned to this services package.");
				} else {
					JsonErrorHandler.alertBox(error);
				}
				events.onError(error); // custom events
			}
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service added to services package.");
				events.onFinished(jso);
			}
			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		// custom error handling
		jspc.setHidden(true);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Prepares a JSON object
	 *
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