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
 * Ajax query which deletes services packages from Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class DeleteServicePackage {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "servicesManager/deleteServicesPackage";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int packageId = 0;

	/**
	 * Creates a new request
	 */
	public DeleteServicePackage() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public DeleteServicePackage(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to delete a service, tests values first
	 *
	 * @param packageId id of service to be deleted
	 */
	public void deleteServicePackage(final int packageId)
	{

		this.packageId = packageId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting service package: " + packageId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service package: " + packageId + " deleted !");
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
	private boolean testDeleting()
	{
		boolean result = true;
		String errorMsg = "";

		if(packageId == 0){
			errorMsg += "Wrong parameter 'Service package ID'.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter Error", errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		JSONNumber packageId = new JSONNumber(this.packageId);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("servicesPackage", packageId);
		return jsonQuery;
	}

}
