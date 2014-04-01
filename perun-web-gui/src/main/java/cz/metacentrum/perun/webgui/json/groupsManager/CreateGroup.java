package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Create a new group query.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateGroup {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "groupsManager/createGroup";
	private String groupName = "";
	private String groupDescription = "";

	/**
	 * New instance of CreateGroup
	 */
	public CreateGroup() {}

	/**
	 * New instance of CreateGroup
	 *
	 * @param events
	 */
	public CreateGroup(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Create a new group in a VO
	 * @param voId VO ID
	 * @param name Group name
	 * @param description Group description
	 */
	public void createGroupInVo(int voId, final String name, String description) {

		this.groupName = name;
		this.groupDescription = description;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// GROUP OBJECT
		JSONObject group = new JSONObject();
		group.put("name", new JSONString(name));
		group.put("description", new JSONString(description));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("vo", new JSONNumber(voId));
		jsonQuery.put("group", group);

		this.createGroup(jsonQuery);
	}

	/**
	 * Creates a new subgroup in group
	 * @param groupId Parent group id
	 * @param name New group name
	 * @param description New group description
	 */
	public void createGroupInGroup(final int groupId, final String name, String description) {

		this.groupName = name;
		this.groupDescription = description;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// GROUP OBJECT
		JSONObject group = new JSONObject();
		group.put("name", new JSONString(name));
		group.put("description", new JSONString(description));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("parentGroup", new JSONNumber(groupId));
		jsonQuery.put("group", group);

		this.createGroup(jsonQuery);
	}

	/**
	 * Creates a new GROUP
	 * @param jsonQuery A JSON object prepared by createGroupInGroup and createGroupInVo methods.
	 */
	private void createGroup(JSONObject jsonQuery) {

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating group failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Group gp = jso.cast();
				// if group admin, grant access in GUI !!
				if (session.isGroupAdmin() && gp != null) {
					session.addEditableGroup(gp.getId());
					session.getUiElements().setLogSuccessText("Group "+ gp.getName() +" successfully created!");
				}
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
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

		if(groupName.length() == 0){
			errorMsg += "You must fill in the parameter <strong>Name</strong>.<br />";
			result = false;
		}

		if(groupDescription.length() == 0){
			errorMsg += "You must fill in the parameter <strong>Description</strong>.<br />";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating Group", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

}
