package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonStatusSetCallback;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax request to set status for member
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SetStatus implements JsonStatusSetCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	private int memberId = 0;
	private String status = "";
	final String JSON_URL = "membersManager/setStatus";
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 *
	 * @param memberId ID of member to set new status
	 */
	public SetStatus(int memberId) {
		this.memberId = memberId;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param memberId ID of member to set new status
	 * @param events Custom events
	 */
	public SetStatus(int memberId, JsonCallbackEvents events) {
		this.memberId = memberId;
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false
	 */
	private boolean testSetting()
	{
		boolean result = true;
		String errorMsg = "";

		if(status.length() == 0){
			errorMsg += "Wrong parameter 'Status'.\n";
			result = false;
		}

		if(memberId == 0){
			errorMsg += "Wrong parameter 'Member ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to set new status for selected member
	 *
	 * @param status new status (VALID,INVALID,SUSPENDED,EXPIRED,DISABLED)
	 */
	public void setStatus(String status)
	{
		this.status = status;

		// test arguments
		if(!this.testSetting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Setting new status for member: " + memberId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("New status for member: " + memberId + " successfully set.");
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
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("member", new JSONNumber(memberId));
		jsonQuery.put("status", new JSONString(status));
		return jsonQuery;
	}

	/**
	 * Sets the json events
	 */
	public void setEvents(JsonCallbackEvents events)
	{
		this.events = events;
	}
}
