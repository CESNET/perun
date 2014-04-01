package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 *
 * Ajax query to get group by it's name
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetGroupByName implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// search attributes
	private String groupName;
	private int voId;
	// Loaded group
	private Group group;
	// JSON URL
	static private final String JSON_URL = "groupsManager/getGroupByName";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// labels
	private Label groupNameLabel = new Label();
	private Label groupDescriptionLabel = new Label();
	private Label groupIdLabel = new Label();

	/**
	 * Creates a new callback
	 *
	 * @param name name
	 * @param voId ID of VO where to search for group
	 */
	public GetGroupByName(String name, int voId) {
		this.groupName = name;
		this.voId = voId;
	}

	/**
	 * Creates a new callback
	 *
	 * @param name name
	 * @param voId ID of VO where to search for group
	 * @param events external events
	 */
	public GetGroupByName(String name, int voId, JsonCallbackEvents events) {
		this.groupName = name;
		this.events = events;
		this.voId = voId;
	}

	/**
	 * Returns decorator panel with group info
	 *
	 * @return widget with group info
	 */
	public DecoratorPanel getDecoratedFlexTable()
	{
		this.retrieveData();

		// The table
		FlexTable layout = new FlexTable();
		layout.setCellSpacing(6);

		// Add group information
		layout.setHTML(0, 0, "ID:");
		layout.setWidget(0, 1, this.groupIdLabel);
		layout.setHTML(1, 0, "Name:");
		layout.setWidget(1, 1, this.groupNameLabel);
		layout.setHTML(2, 0, "Description:");
		layout.setWidget(2, 1, this.groupDescriptionLabel);


		// Wrap the content in a DecoratorPanel
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setWidget(layout);

		return decPanel;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		final String param = "name=" + this.groupName + "&vo=" +voId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Updates the data from the request
	 *
	 * @param obj returned javascript object
	 */
	private void updateData(JavaScriptObject obj){
		this.groupIdLabel.setText(String.valueOf(group.getId()));
		this.groupNameLabel.setText(group.getName());
		this.groupDescriptionLabel.setText(group.getDescription());
	}

	/**
	 * Called when loading successfully finishes
	 */
	public void onFinished(JavaScriptObject jso) {
		group = (Group) jso;
		updateData(group);
		session.getUiElements().setLogText("Loading group details finished.");
		events.onFinished(group);
	}

	/**
	 * Called when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading group details. Group name: " + this.groupName);
		events.onError(error);
	}

	/**
	 * Called when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading group details started.");
		events.onLoadingStart();
	}

}
