package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to add hosts to Cluster
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class AddHosts {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "facilitiesManager/addHosts";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int clusterId = 0;
	private String[] hostNames;

	/**
	 * Creates a new request
     *
     * @param clusterId ID of facility
	 */
	public AddHosts(int clusterId) {
		this.clusterId = clusterId;
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
     * @param clusterId ID of facility
     * @param events external events
	 */
	public AddHosts(int clusterId, final JsonCallbackEvents events) {
		this.clusterId = clusterId;
		this.events = events;
	}

	/**
	 * Adds hosts to cluster - makes RPC call
	 * 
	 * @param hosts array of hostnames
	 */
	public void addHosts(String[] hosts) {

		this.hostNames = hosts;	

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding hosts failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Hosts added to cluster: "+ clusterId);
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
	 * 
	 * @return true/false for continue/stop
	 */
	private boolean testAdding() {

		boolean result = true;
		String errorMsg = "";

		if(clusterId == 0){
			errorMsg += "Wrong parametr Facility ID.\n";
			result = false;
		}

		if(hostNames.length == 0){
			errorMsg += "You must enter at least 1 host name.\n";
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
	private JSONObject prepareJSONObject() {

		JSONNumber cluster = new JSONNumber(clusterId);
		JSONArray ids = new JSONArray();
		// put names in array
		for (int i=0; i<hostNames.length; i++) {
			if (hostNames[i] == "") continue; // empty host names are excluded
			ids.set(i,new JSONString(hostNames[i]));	
		}

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();      
		jsonQuery.put("facility", cluster);
		jsonQuery.put("hostnames", ids);
		return jsonQuery;

	}

}