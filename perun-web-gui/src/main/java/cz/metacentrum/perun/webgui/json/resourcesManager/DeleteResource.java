package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query for deleting resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class DeleteResource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/deleteResource";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int resourceId = 0;

	/**
	 * Creates a new request
	 */
	public DeleteResource() {
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public DeleteResource(JsonCallbackEvents events) {
		this.events = events;
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

		if(resourceId == 0){
			errorMsg += "Wrong parameter 'resource ID'";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to delete resource, it first tests the values and then submits them
	 *
	 * @param resourceId ID of resource to be deleted
	 */
	public void deleteResource(final int resourceId) {

		this.resourceId = resourceId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting resource: " + resourceId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Resource " + resourceId + " deleted.");
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
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("resource", new JSONNumber(resourceId));
		return jsonQuery;
	}

}
