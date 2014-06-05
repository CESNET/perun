package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Changing user's password using non-authz way
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class ChangeNonAuthzPassword {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/changeNonAuthzPassword";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// local variables for entity to send
	private String password = "";
	private String i = "";
	private String m = "";

	/**
	 * Creates a new request
	 */
	public ChangeNonAuthzPassword() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public ChangeNonAuthzPassword(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Changes password for the user
	 *
	 * @param i        encrypted value 1
	 * @param m        encrypted value 2
	 * @param password new password to set
	 */
	public void changeNonAuthzPassword(String i, String m, String password) {

		this.i = i;
		this.m = m;
		this.password = password;

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Changing password failed.");
				events.onError(error); // custom events
			}
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Password changed successfully.");
				events.onFinished(jso);
			}
			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("i", new JSONString(i));
		jsonQuery.put("m", new JSONString(m));
		jsonQuery.put("password", new JSONString(password));
		return jsonQuery;

	}

}
