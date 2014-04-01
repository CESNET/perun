package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.ServicesPackage;

/**
 * Ajax query which updates an existing service package
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateServicePackage {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service name
	private ServicesPackage servicesPackage;
	// URL to call
	final String JSON_URL = "servicesManager/updateServicesPackage";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public UpdateServicePackage() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public UpdateServicePackage(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testUpdating()
	{
		boolean result = true;
		String errorMsg = "";

		if(servicesPackage == null){
			errorMsg += "Wrong parameter 'Services package'.<br>";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to update ServicesPackage, it first tests the values and then submits them.
	 *
	 * @param servicesPackage service package to update
	 */
	public void updateServicePackage(final ServicesPackage servicesPackage) {

		this.servicesPackage = servicesPackage;

		// test arguments
		if(!this.testUpdating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating service package " + servicesPackage.getName() + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service package " + servicesPackage.getName()+ " updated.");
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
		// service
		JSONObject pack = new JSONObject(servicesPackage);

		JSONObject newPack = new JSONObject();
		newPack.put("name", pack.get("name"));
		newPack.put("description", pack.get("description"));
		newPack.put("id", pack.get("id"));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("servicesPackage", newPack);
		return jsonQuery;

	}

}
