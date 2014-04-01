package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;

/**
 * Ajax query to add member to group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddMember {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "groupsManager/addMember";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int memberId = 0;
	private int groupId = 0;

	private RichMember member = null;
	private Group group = null;

	/**
	 * Creates a new request
	 */
	public AddMember() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AddMember(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to add member to group
	 *
	 * @param groupId ID of group
	 * @param memberId ID of VO member to be member of group
	 */
	public void addMemberToGroup(final int groupId,final int memberId) {

		this.memberId = memberId;
		this.groupId = groupId;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding member: " + memberId + " to group: " + groupId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member: " + memberId + " added to group: " + groupId);
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
	 * Attempts to add member to group
	 *
	 * @param group group
	 * @param member member to be member of group
	 */
	public void addMemberToGroup(final Group group,final RichMember member) {

		this.group = group;
		this.member = member;

		this.memberId = (member != null) ? member.getId() : 0;
		this.groupId = (group != null) ? group.getId() : 0;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding member: " + member.getUser().getFullName() + " to group: " + group.getShortName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Member: " + member.getUser().getFullName()+ " added to group: " + group.getShortName());
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
	private boolean testAdding() {

		boolean result = true;
		String errorMsg = "";

		if(groupId == 0){
			errorMsg += "Wrong parameter <strong>Group</strong>.</br>";
			result = false;
		}

		if(memberId == 0){
			errorMsg += "Wrong parameter <strong>Member</strong>.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		JSONNumber group = new JSONNumber(groupId);
		JSONNumber member = new JSONNumber(memberId);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("group", group);
		jsonQuery.put("member", member);
		return jsonQuery;
	}

}
