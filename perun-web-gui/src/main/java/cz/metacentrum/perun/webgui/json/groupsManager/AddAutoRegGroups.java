package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gson.JsonArray;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.List;

/**
 * @author vojtech sassmann
 */
public class AddAutoRegGroups {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "groupsManager/addGroupsToAutoRegistration";

	public AddAutoRegGroups(JsonCallbackEvents events) {
		this.events = events;
	}

	public void setAutoRegGroups(final List<Group> groups) {
		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		JSONArray groupIds = new JSONArray();
		for (int i = 0; i < groups.size(); i++) {
			Group group = groups.get(i);
			groupIds.set(i, new JSONNumber(group.getId()));
		}
		jsonQuery.put("groups", groupIds);

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Failed to add groups to auto registration.");
				events.onError(error);
			}

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Groups successfully added to auto registration!");
				events.onFinished(jso);
			}

			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, jsonQuery);
	}

}
