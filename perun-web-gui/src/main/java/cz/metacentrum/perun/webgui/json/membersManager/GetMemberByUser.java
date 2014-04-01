package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * MembersManager/GetMemberByUser
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetMemberByUser implements JsonCallback {

	// User ID
	private int userId;

	// VO ID
	private int voId;

	// JSON URL
	static private final String JSON_URL = "membersManager/getMemberByUser";

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private boolean hidden = false;

	/**
	 * New instance of member info
	 *
	 * @param voId
	 * @param userId
	 */
	public GetMemberByUser(int voId, int userId) {
		this.voId = voId;
		this.userId = userId;
	}

	/**
	 * New instance of member info.
	 *
	 * @param voId
	 * @param userId
	 * @param events
	 */
	public GetMemberByUser(int voId, int userId, JsonCallbackEvents events) {
		this.userId = userId;
		this.voId = voId;
		this.events = events;
	}

	/**
	 * Retrieves data
	 */
	public void retrieveData(){

		final String param = "user=" + this.userId + "&vo=" + this.voId;

		// retrieve data
		JsonClient js = new JsonClient();
		js.setHidden(hidden);
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * When successfully finishes
	 */
	public void onFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	/**
	 * When error
	 */
	public void onError(PerunError error) {
		events.onError(error);
	}

	/**
	 * When start
	 */
	public void onLoadingStart() {
		events.onLoadingStart();
	}

	/**
	 * Sets events to this callback
	 *
	 * @param events
	 */
	public void setEvents(JsonCallbackEvents events){
		this.events = events;
	}

	/**
	 * Set callback as hidden (do not show error popup)
	 * @param hidden
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
