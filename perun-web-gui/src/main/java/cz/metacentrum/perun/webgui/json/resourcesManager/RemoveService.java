package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which removes service from resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class RemoveService {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/removeService";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int resourceId = 0;
	private int serviceId = 0;

	/**
	 * Creates a new request
	 */
	public RemoveService() {
		this.session = session;
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public RemoveService(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to remove service from resource
	 *
	 * @param serviceId ID of service which should be removed
	 * @param resourceId ID of resource where should be removed
	 */
	public void removeService(final int serviceId,final int resourceId)
	{

		this.resourceId = resourceId;
		this.serviceId = serviceId;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing service: " + serviceId + " from resource: " + resourceId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service: "+ serviceId +" successfully removed from resource: "+ resourceId);
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
	private boolean testRemoving()
	{
		boolean result = true;
		String errorMsg = "";

		if(serviceId == 0){
			errorMsg += "Wrong Group parametr.\n";
			result = false;
		}

		if(resourceId == 0){
			errorMsg += "Wrong Resource parametr.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("service", new JSONNumber(serviceId));
		jsonQuery.put("resource", new JSONNumber(resourceId));
		return jsonQuery;
	}

}
