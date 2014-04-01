package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Remove owner from facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class RemoveOwner {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "facilitiesManager/removeOwner";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int ownerId = 0;
	private int facilityId = 0;

	/**
	 * Creates a new request
	 */
	public RemoveOwner() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public RemoveOwner(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to remove admin from facility
	 *
	 * @param facilityId id of facility
	 * @param ownerId ID of user which should be removed as admin
	 */
	public void removeFacilityOwner(final int facilityId, final int ownerId) {

		this.ownerId = ownerId;
		this.facilityId = facilityId;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// prepare json object
		JSONObject jsonQuery = prepareJSONObject();

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing owner (" + ownerId + ") from facility: " + facilityId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Owner (" + ownerId + ") removed from facility: " + facilityId);
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, jsonQuery);

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

		if(facilityId == 0){
			errorMsg += "Wrong 'facilityId' parameter.\n";
			result = false;
		}

		if(ownerId == 0){
			errorMsg += "Wrong 'ownerId' parameter.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject()
	{

		JSONNumber facility = new JSONNumber(facilityId);
		JSONNumber user = new JSONNumber(ownerId);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", facility);
		jsonQuery.put("owner", user);
		return jsonQuery;
	}

}
