package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to force group synchronization
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ForceGroupSynchronization {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "groupsManager/forceGroupSynchronization";

	/**
	 * New instance of callback
	 */
	public ForceGroupSynchronization() {}

	/**
	 * New instance of callback with external events
	 *
	 * @param events external events
	 */
	public ForceGroupSynchronization(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Immediately queues group for synchronization.
	 *
	 * @param groupId ID of group to be synchronized
	 */
	public void synchronizeGroup(final int groupId) {

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("group", new JSONNumber(groupId));

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Forcing synchronization of group "+ groupId +" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group "+ groupId +" queued for synchronization!");
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
