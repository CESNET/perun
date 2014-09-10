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
public class GetNewExtendMembershipLoa implements JsonCallback {

	static private final String JSON_URL = "membersManager/getNewExtendMembership";
	private int loa = 0;
	private int voId = 0;
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private Label widget = new Label();

	/**
	 * New instance of member info
	 *
	 * @param voId
	 * @param loa
	 */
	public GetNewExtendMembershipLoa(int voId, int loa) {
		this.voId = voId;
		this.loa = loa;
	}

	/**
	 * New instance of member info.
	 *
	 * @param voId
	 * @param loa
	 * @param events
	 */
	public GetNewExtendMembershipLoa(int voId, int loa, JsonCallbackEvents events) {
		this.loa = loa;
		this.voId = voId;
		this.events = events;
	}

	/**
	 * Retrieves data
	 */
	public void retrieveData(){

		final String param = "loa=" + this.loa + "&vo=" + this.voId;
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
		widget.setText(basic.getString());
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
