package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Initialize Registrar GUI - retrieves all VO attributes
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Initialize implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/initialize";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private String vo = "";
	private String group = "";
	private boolean hidden = false;

	/**
	 * Creates a new request
	 *
	 * @param voShortName vo name
	 * @param group group name
	 */
	public Initialize(String voShortName, String group) {
		this.vo = voShortName;
		this.group = group;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param voShortName vo name
	 * @param group group name
	 * @param events Custom events
	 */
	public Initialize(String voShortName, String group, JsonCallbackEvents events) {
		this.events = events;
		this.vo = voShortName;
		this.group = group;
	}

	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("VO/Group information retrieved");
		events.onFinished(jso);
	}

	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Retrieving VO/Group information failed.");
		events.onError(error);
	}

	public void onLoadingStart() {
		events.onLoadingStart();
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void retrieveData() {

		String params = "";

		if (vo != null && !vo.equals("")) {
			params += "vo="+vo;
		}
		if (group != null && !group.equals("")) {
			params += "&group="+group;
		}

		JsonClient client = new JsonClient();
		client.setHidden(hidden);
		client.retrieveData(JSON_URL, params, this);

	}

}
