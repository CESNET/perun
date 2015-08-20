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
 * Ajax query which removes ext source from VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class RemoveExtSource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "extSourcesManager/removeExtSource";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// IDS
	private int voId = 0;
	private int extSourceId = 0;

	/**
	 * Creates a new request
	 */
	public RemoveExtSource() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 * @param events external events
	 */
	public RemoveExtSource(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to remove external source from VO
	 *
	 * @param voId ID of VO, where we should remove ext source
	 * @param extSourceId ID of external source to be removed
	 */
	public void removeVoExtSource(final int voId,final int extSourceId){

		this.voId = voId;
		this.extSourceId = extSourceId;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing external source: " + extSourceId + " from VO: " + voId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("External source: "+ extSourceId +" successfully removed from VO: "+ voId);
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
	 * Attempts to remove external source from Group
	 *
	 * @param groupId ID of Group, where we should remove ext source
	 * @param extSourceId ID of external source to be removed
	 */
	public void removeGroupExtSource(final int groupId,final int extSourceId){

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("source", new JSONNumber(extSourceId));
		jsonQuery.put("group", new JSONNumber(groupId));

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing external source: " + extSourceId + " from Group: " + groupId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("External source: "+ extSourceId +" successfully removed from Group: "+ groupId);
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
	private boolean testRemoving() {

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
