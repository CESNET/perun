package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to delete selected group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteGroup {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "groupsManager/deleteGroup";

	/**
	 * New instance of callback
	 */
	public DeleteGroup() {}

	/**
	 * New instance of callback with external events
	 *
	 * @param events external events
	 */
	public DeleteGroup(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Delete a group from VO in DB
	 *
	 * @param groupId ID of group to be deleted
	 */
	public void deleteGroup(final int groupId) {

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("group", new JSONNumber(groupId));
		jsonQuery.put("force", new JSONNumber(1));

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting group "+ groupId +" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group "+ groupId +" successfully deleted!");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, jsonQuery);

	}

}
