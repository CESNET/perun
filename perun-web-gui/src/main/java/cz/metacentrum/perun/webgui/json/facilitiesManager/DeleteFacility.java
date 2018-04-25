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
 * Ajax query to delete a facility from DB
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class DeleteFacility {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "facilitiesManager/deleteFacility";

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private int facilityId = 0;

	/**
	 * Creates a new request
	 */
	public DeleteFacility() {}

	/**
	 * Creates a new request with custom events passed from tab or page

	 * @param events JsonCallbackaEvents
	 */
	public DeleteFacility(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to delete the facility.
	 * @param facilityId - ID of the facility which should be deleted
	 */
	public void deleteFacility(final int facilityId)
	{

		this.facilityId = facilityId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting facility: " + facilityId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Facility: "+ facilityId +" successfully deleted.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		// create request
		JsonPostClient request = new JsonPostClient(newEvents);
		request.sendData(JSON_URL, jsonQuery);

	}

	/**
	 * Tests the values, if the process can continue
	 * @return true/false for continue/stop
	 */
	private boolean testDeleting() {

		boolean result = true;
		String errorMsg = "";

		if(facilityId == 0){  // facility id not set
			errorMsg += "Facility ID not set.\n";
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
	private JSONObject prepareJSONObject() {
		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", new JSONNumber(facilityId));
		return jsonQuery;
	}

}
