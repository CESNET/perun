package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Move Group under new parent group or to top-level.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MoveGroup {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "groupsManager/moveGroup";
	private Group movingGroup;
	private Group destinationGroup;

	/**
	 * New instance of CreateGroup
	 */
	public MoveGroup() {}

	/**
	 * New instance of CreateGroup
	 *
	 * @param events
	 */
	public MoveGroup(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Move group to top-level
	 *
	 * @param movingGroup group to be moved
	 */
	public void moveGroup(Group movingGroup) {
		moveGroup(movingGroup, null);
	}

	/**
	 * Move group under new parent (under different group or as top-level)
	 *
	 * @param movingGroup Group to be moved
	 * @param destinationGroup New parent group or NULL if should be top-level
	 */
	public void moveGroup(final Group movingGroup, final Group destinationGroup) {

		this.movingGroup = movingGroup;
		this.destinationGroup = destinationGroup;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// GROUP OBJECT
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("movingGroup", new JSONNumber(this.movingGroup.getId()));
		if (this.destinationGroup != null) {
			jsonQuery.put("destinationGroup", new JSONNumber(this.destinationGroup.getId()));
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			private Group movedGroup = movingGroup;
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Moving group failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group "+ movedGroup.getName() +" was successfully moved!");
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

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return
	 */
	private boolean testCreating() {

		boolean result = true;
		String errorMsg = "";

		if(movingGroup == null){
			errorMsg += "Moving group can't be NULL.<br />";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while moving Group", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}
}
