package cz.metacentrum.perun.webgui.json.extSourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which adds ext source to VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class AddExtSource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "extSourcesManager/addExtSource";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// IDS
	private int voId = 0;
	private int extSourceId = 0;

	/**
	 * Creates a new request
	 */
	public AddExtSource() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AddExtSource(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to add external source to VO in DB - make RPC call
	 *
	 * @param voId ID of VO, where should be ext source added
	 * @param extSourceId ID of external source to be added
	 */
	public void addExtSource(final int voId,final int extSourceId) {

		this.voId = voId;
		this.extSourceId = extSourceId;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding external source: " + extSourceId + " to VO: " + voId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("External source: "+ extSourceId +" successfully added to Vo: "+ voId);
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
	 * Attempts to add external source to Group in DB - make RPC call
	 *
	 * @param groupId ID of Group, where should be ext source added
	 * @param extSourceId ID of external source to be added
	 */
	public void addGroupExtSource(final int groupId,final int extSourceId) {

		this.extSourceId = extSourceId;

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("source", new JSONNumber(extSourceId));
		jsonQuery.put("group", new JSONNumber(groupId));

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding external source: " + extSourceId + " to group: " + groupId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("External source: "+ extSourceId +" successfully added to group: "+ groupId);
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

		if(extSourceId == 0){
			errorMsg += "Wrong ExtSource parameter.\n";
			result = false;
		}

		if(voId == 0){
			errorMsg += "Wrong Vo parameter.\n";
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

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("source", new JSONNumber(extSourceId));
		jsonQuery.put("vo", new JSONNumber(voId));
		return jsonQuery;
	}

}
