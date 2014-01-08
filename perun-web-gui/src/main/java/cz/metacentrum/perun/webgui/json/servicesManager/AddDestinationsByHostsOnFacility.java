package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;

import java.util.ArrayList;

/**
 * Ajax query which creates a new destinations for service and facility based on facility hosts (their names) 
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class AddDestinationsByHostsOnFacility {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "servicesManager/addDestinationsDefinedByHostsOnFacility";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int facilityId = 0;
	private Service service;

	/**
	 * Creates a new request
	 *
	 * @param facilityId ID of facility to have destination added
	 */
	public AddDestinationsByHostsOnFacility(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param facilityId ID of facility to have destination added
	 * @param events custom events
	 */
	public AddDestinationsByHostsOnFacility(int facilityId, final JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 * 
	 * @return true/false for continue/stop
	 */
	private boolean testCreating() {

		boolean result = true;
		String errorMsg = "";

		if(facilityId == 0){
			errorMsg += "You must pick parameter 'Facility'.</br>";
			result = false;
		}

		if(service == null){
			errorMsg += "You must pick parameter 'Service'.";
			result = false;
		}

		if(errorMsg.length()>0){
            UiElements.generateAlert("Wrong parameter", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to add new Destination to service and facility, it first tests the values and then 
	 * submits them.
	 * 
	 * @param services list of services to have destinations by hosts added
	 */
	public void addDestinationByHosts(ArrayList<Service> services) {
		for (Service serv : services){
			// add auto destinations for all services
			addDestinationByHosts(serv);
		}
	}
	
	/**
	 * Attempts to add new Destination to service and facility, it first tests the values and then 
	 * submits them.
	 * 
	 * @param service to have destinations by hosts addded
	 */
	public void addDestinationByHosts(Service service) {
		
		this.service = service;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding destination for facility: " + facilityId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Destination for facility: " + facilityId + " added.");
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
		jsonQuery.put("service", new JSONNumber(service.getId()));
		jsonQuery.put("facility", new JSONNumber(facilityId));

		return jsonQuery;
	}

}