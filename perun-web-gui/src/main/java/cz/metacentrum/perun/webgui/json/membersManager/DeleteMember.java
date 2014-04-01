package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to delete member in VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class DeleteMember {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "membersManager/deleteMember";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int memberId = 0;

	/**
	 * Creates a new request
	 */
	public DeleteMember() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public DeleteMember(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to delete member in VO
	 *
	 * @param memberId ID of member which should be deleted
	 */
	public void deleteMember(final int memberId)
	{

		this.memberId = memberId;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting member: " + memberId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member: " + memberId + " deleted!");
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

		if(memberId == 0){
			errorMsg += "Wrong 'member ID' parametr.\n";
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

		JSONNumber member = new JSONNumber(memberId);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("member", member);
		return jsonQuery;
	}

}
