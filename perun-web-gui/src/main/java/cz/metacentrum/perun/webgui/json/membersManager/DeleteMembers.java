package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;

import java.util.ArrayList;

/**
 * Ajax query to delete members from VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class DeleteMembers {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "membersManager/deleteMembers";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private ArrayList<RichMember> members = null;

	/**
	 * Creates a new request
	 */
	public DeleteMembers() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public DeleteMembers(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to delete members from VO
	 *
	 * @param members Members which should be deleted
	 */
	public void deleteMembers(final ArrayList<RichMember> members) {

		this.members = members;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting " + members.size() + " members failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText(members.size() + " members deleted!");
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
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding()
	{
		boolean result = true;
		String errorMsg = "";

		if(members == null || members.isEmpty()){
			errorMsg += "Wrong 'members' parameter (null or empty).\n";
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

		JSONArray array = new JSONArray();
		for (int i=0; i<members.size(); i++) {
			array.set(i, new JSONNumber(members.get(i).getId()));
		}

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("members", array);
		return jsonQuery;

	}

}
