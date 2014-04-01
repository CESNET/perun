package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which removes a destination from service and facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class RemoveDestination {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "servicesManager/removeDestination";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int facilityId = 0;
	private int serviceId = 0;
	private String destination = "";
	private String type = "";

	/**
	 * Creates a new request
	 *
	 * @param facilityId ID of facility to remove destination from
	 * @param serviceId ID of service to remove destination for
	 */
	public RemoveDestination(int facilityId, int serviceId) {
		this.facilityId = facilityId;
		this.serviceId = serviceId;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param facilityId ID of facility to remove destination from
	 * @param serviceId ID of service to remove destination for
	 * @param events custom events
	 */
	public RemoveDestination(int facilityId, int serviceId, final JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.serviceId = serviceId;
		this.events = events;
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
			errorMsg += "Wrong parameter 'Facility'.\n";
			result = false;
		}

		if(serviceId == 0){
			errorMsg += "Wrong parameter 'Service'.\n";
			result = false;
		}

		if(destination.length() == 0){
			errorMsg += "Wrong parameter 'Destination'.\n";
			result = false;
		}

		if(type.length() == 0){
			errorMsg += "Wrong parameter 'Type'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to remove a Destination from service and facility, it first tests the values and then
	 * submits them.
	 *
	 * @param destination destination string
	 * @param type type of destination
	 */
	public void removeDestination(final String destination, final String type)
	{

		this.destination = destination;
		this.type = type;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing destination" + destination + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Destination " + destination + " removed.");
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
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("destination", new JSONString(destination));
		jsonQuery.put("type", new JSONString(type));
		jsonQuery.put("service", new JSONNumber(serviceId));
		jsonQuery.put("facility", new JSONNumber(facilityId));

		return jsonQuery;
	}

}
