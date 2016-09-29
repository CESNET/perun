package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to get date to which membership will be extended if application will be approved right now.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetNewExtendMembership implements JsonCallback {

	static private final String JSON_URL = "membersManager/getNewExtendMembership";
	private int userId = 0;
	private int voId = 0;
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private Label widget = new Label();

	/**
	 * New instance of member info
	 *
	 * @param voId
	 * @param userId
	 */
	public GetNewExtendMembership(int voId, int userId) {
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
	public GetNewExtendMembership(int voId, int userId, JsonCallbackEvents events) {
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
		js.retrieveData(JSON_URL, param, this);
	}

	public Label getWidget() {
		return this.widget;
	}

	/**
	 * When successfully finishes
	 */
	public void onFinished(JavaScriptObject jso) {
		BasicOverlayType basic = jso.cast();
		if (jso != null) {
			widget.setText(basic.getString());
		}
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

}
