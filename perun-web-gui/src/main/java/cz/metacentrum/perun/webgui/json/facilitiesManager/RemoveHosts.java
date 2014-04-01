package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;

/**
 * Ajax query which deletes hosts from Cluster/Facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class RemoveHosts {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "facilitiesManager/removeHosts";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int clusterId = 0;
	private int hostId = 0;

	/**
	 * Creates a new request
	 *
	 * @param clusterId ID of cluster to remove hosts from
	 */
	public RemoveHosts(int clusterId) {
		this.clusterId = clusterId;
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param clusterId ID of cluster to remove hosts from
	 * @param events external events
	 */
	public RemoveHosts(int clusterId, final JsonCallbackEvents events) {
		this.clusterId = clusterId;
		this.events = events;
	}

	/**
	 * Deletes all hosts from list at once
	 *
	 * @param hosts list of hosts IDS
	 */
	public void deleteHosts(ArrayList<Integer> hosts) {
		for (Integer id : hosts) {
			removeHost(id);
		}
	}

	/**
	 * Removes host from cluster in DB - make RPC call
	 *
	 * @param hostId ID of host to be removed
	 */
	public void removeHost(final int hostId) {

		this.hostId = hostId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing host: " + hostId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Host: " + hostId + " removed!");
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
	private boolean testDeleting() {

		boolean result = true;
		String errorMsg = "";

		if(clusterId == 0){
			errorMsg += "Wrong parameter Facility ID.\n";
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
		JSONNumber host = new JSONNumber(hostId);
		JSONArray ids = new JSONArray();
		ids.set(0, host);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", cluster);
		jsonQuery.put("hosts", ids);
		return jsonQuery;

	}

}
