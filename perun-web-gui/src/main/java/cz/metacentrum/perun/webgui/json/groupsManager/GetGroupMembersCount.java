package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Returns the count of the members in Group.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetGroupMembersCount implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO ID
	private int groupId;
	// JSON URL
	private static final String JSON_URL = "groupsManager/getGroupMembersCount";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();


	/**
	 * Creates a new instance of the method
	 *
	 * @param id VO id.
	 */
	public GetGroupMembersCount(int id) {
		this.groupId = id;
	}

	/**
	 * Creates a new instance of the method
	 *
	 * @param id VO id.
	 * @param events events
	 */
	public GetGroupMembersCount(int id, JsonCallbackEvents events) {
		this.groupId = id;
		this.events = events;
	}


	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData()
	{
		final String param = "group=" + this.groupId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}


	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		this.events.onError(error);
		session.getUiElements().setLogErrorText("Error while loading group count.");
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		this.events.onLoadingStart();
		session.getUiElements().setLogText("Loading group members count.");
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		BasicOverlayType count = (BasicOverlayType) jso;
		session.getUiElements().setLogText("Loading group members count finished: " + count.getInt());
		this.events.onFinished(jso);
	}


}
