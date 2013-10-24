package cz.metacentrum.perun.webgui.json.generalServiceManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to free denial of selected exec service on facility
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class FreeDenialOfExecServiceOnFacility {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "generalServiceManager/freeDenialOfExecServiceOnFacility";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int facilityId = 0;
	private int execServiceId = 0;

	/**
	 * Creates a new request
	 * 
	 * @param facilityId facility ID
	 */
	public FreeDenialOfExecServiceOnFacility(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * Creates a new request with custom events
	 * 
	 * @param facilityId facility ID
	 * @param events Custom events
	 */
	public FreeDenialOfExecServiceOnFacility(int facilityId, JsonCallbackEvents events) {
		this.facilityId = facilityId;
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

		if(facilityId == 0){
			errorMsg += "Wrong parameter 'Facility ID'.\n";
			result = false;
		}

		if(execServiceId == 0){
			errorMsg += "Wrong parameter 'Exec service ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to free denial of selected exec service for specified facility
	 * 
	 * @param execServiceId
	 * @param facilityId
	 */
	public void freeDenialOfExecService(int execServiceId, int facilityId){
		this.facilityId = facilityId;
		freeDenialOfExecService(execServiceId);
	};

	/**
	 * Attempts to free denial of selected exec service on facility
	 * 
	 * @param execServiceId
	 */
	public void freeDenialOfExecService(final int execServiceId){

		this.execServiceId = execServiceId;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Allowing of exec service "+ execServiceId +" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Exec service " + execServiceId + " allowed on facility.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	};

	/**
	 * Prepares a JSON object
	 * 
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", new JSONNumber(facilityId));
		jsonQuery.put("execService", new JSONNumber(execServiceId));
		return jsonQuery;

	}

	/**
	 * Sets external events after callback creation
	 * 
	 * @param events
	 */
	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}
	
}