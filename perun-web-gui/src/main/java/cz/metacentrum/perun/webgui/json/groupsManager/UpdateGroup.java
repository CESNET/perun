package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Update group details
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateGroup {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "groupsManager/updateGroup";

	/**
	 * New instance of update group
	 */
	public UpdateGroup() {}

	/**
	 * New instance of update group
	 *
	 * @param events
	 */
	public UpdateGroup(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Updates group details
	 * @param group Group with updated details
	 */
	public void updateGroup(Group group) {

		if (group == null) {
			Window.alert("Group can't be null");
			return;
		}

		// GROUP OBJECT
		JSONObject oldGroup = new JSONObject(group);
		// RECONSTRUCT OBJECT
		JSONObject newGroup = new JSONObject();
		newGroup.put("id", oldGroup.get("id"));
		// fake new group short name as name in order to update
		newGroup.put("name", oldGroup.get("shortName"));
		newGroup.put("description", oldGroup.get("description"));
		newGroup.put("voId", oldGroup.get("voId"));
		newGroup.put("parentGroupId", oldGroup.get("parentGroupId"));
		newGroup.put("beanName", oldGroup.get("beanName"));
		newGroup.put("createdAt", oldGroup.get("createdAt"));
		newGroup.put("createdBy", oldGroup.get("createdBy"));
		newGroup.put("modifiedAt", oldGroup.get("modifiedAt"));
		newGroup.put("modifiedBy", oldGroup.get("modifiedBy"));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("group", newGroup);

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating group failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Group gp = jso.cast();
				session.getUiElements().setLogSuccessText("Group "+ gp.getName() +" successfully updated!");
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
