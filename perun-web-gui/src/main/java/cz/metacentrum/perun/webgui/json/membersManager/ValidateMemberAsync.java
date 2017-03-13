package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to validates member asynchronously (should be called when member is created from GUI)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class ValidateMemberAsync {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "membersManager/validateMemberAsync";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// params
	private Member member;
	// by default we care about result
	private boolean hidden = false;

	/**
	 * Creates a new request
	 */
	public ValidateMemberAsync() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public ValidateMemberAsync(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to create member in VO from candidate
	 *
	 * @param member
	 *
	 */
	public void validateMemberAsync(final Member member)
	{

		this.member = member;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Validating member: " + member.getId() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member "+ member.getId() +" validated !");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.setHidden(hidden);
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

		if(member == null){
			errorMsg += "Wrong 'Member' parameter. Can't be null.\n";
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
		jsonQuery.put("member", new JSONNumber(member.getId()));
		return jsonQuery;

	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
