package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which tries to validate user's preferred email address
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ValidatePreferredEmailChange implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "usersManager/validatePreferredEmailChange";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private String i = "";
	private String m = "";
	private String token = "";
	private int u = 0;
	private boolean hidden = false;

	/**
	 * Creates a new request
	 *
	 * @param i decrypted parameter
	 * @param m encrypted parameter
	 * @param u user ID to request validation for
	 */
	public ValidatePreferredEmailChange(String i, String m, int u) {
		this.i = i;
		this.m = m;
		this.u = u;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param i decrypted parameter
	 * @param m encrypted parameter
	 * @param u user ID to request validation for
	 * @param events Custom events
	 */
	public ValidatePreferredEmailChange(String i, String m, int u, JsonCallbackEvents events) {
		this.events = events;
		this.i = i;
		this.m = m;
		this.u = u;
	}

	/**
	 * Creates a new request
	 *
	 * @param token parameter for request validation
	 * @param u user ID to request validation for
	 */
	public ValidatePreferredEmailChange(String token, int u) {
		this.token = token;
		this.u = u;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param token parameter for request validation
	 * @param u user ID to request validation for
	 * @param events Custom events
	 */
	public ValidatePreferredEmailChange(String token, int u, JsonCallbackEvents events) {
		this.events = events;
		this.token = token;
		this.u = u;
	}

	public void onFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	public void onError(PerunError error) {
		events.onError(error);
	}

	public void onLoadingStart() {
		events.onLoadingStart();
	}

	public void retrieveData() {

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("u", new JSONNumber(u));
		if (!token.isEmpty()) {
			jsonQuery.put("token", new JSONString(token));
		} else {
			jsonQuery.put("i", new JSONString(i));
			jsonQuery.put("m", new JSONString(m));
		}

		JsonPostClient client = new JsonPostClient(events);
		client.setHidden(hidden);
		client.sendData(JSON_URL, jsonQuery);

	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
