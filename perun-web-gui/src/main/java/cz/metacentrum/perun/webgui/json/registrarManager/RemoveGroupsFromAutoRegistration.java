package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;

/**
 * Ajax query to delete selected groups
 *
 * @author vojtech sassmann
 */
public class RemoveGroupsFromAutoRegistration {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "registrarManager/deleteGroupsFromAutoRegistration";

	/**
	 * New instance of callback
	 */
	public RemoveGroupsFromAutoRegistration() {}

	/**
	 * New instance of callback with external events
	 *
	 * @param events external events
	 */
	public RemoveGroupsFromAutoRegistration(JsonCallbackEvents events) {
		this.events = events;
	}

	public void deleteGroups(final ArrayList<? extends Group> groups) {

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();

		JSONArray grps = new JSONArray();
		for (int i=0; i<groups.size(); i++) {
			grps.set(i, new JSONNumber(groups.get(i).getId()));
		}

		jsonQuery.put("groups", grps);

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Failed to remove groups from auto registration.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Groups successfully removed from auto registration!");
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