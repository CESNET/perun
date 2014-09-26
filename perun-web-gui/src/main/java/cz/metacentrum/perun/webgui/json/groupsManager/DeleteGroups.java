package cz.metacentrum.perun.webgui.json.groupsManager;

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
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteGroups {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "groupsManager/deleteGroups";

	/**
	 * New instance of callback
	 */
	public DeleteGroups() {}

	/**
	 * New instance of callback with external events
	 *
	 * @param events external events
	 */
	public DeleteGroups(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Delete (sub)groups of any group or VO.
	 *
	 * NOTE: force delete is performed (removes all members, remove groups from resources)
	 *
	 * @param groups groups to be deleted
	 */
	public void deleteGroups(final ArrayList<? extends Group> groups) {
		deleteGroups(groups, true);
	}

	/**
	 * Delete (sub)groups of any group or VO.
	 *
	 * @param groups groups to be deleted
	 * @param force TRUE = forced delete (remove all members, remove from resources) / FALSE = not delete if group has members
	 */
	public void deleteGroups(final ArrayList<? extends Group> groups, boolean force) {

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();

		JSONArray grps = new JSONArray();
		for (int i=0; i<groups.size(); i++) {
			grps.set(i, new JSONNumber(groups.get(i).getId()));
		}

		jsonQuery.put("groups", grps);
		jsonQuery.put("forceDelete", new JSONNumber((force) ? 1 : 0));

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting groups failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Groups successfully deleted!");
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