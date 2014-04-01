package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

/**
 * Ajax query which request change of preferred email.
 * Address must be verified via email link in core before
 * change happens.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RequestPreferredEmailChange {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/requestPreferredEmailChange";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// local variables for entity to send
	private User user;
	private String email;

	/**
	 * Creates a new request
	 */
	public RequestPreferredEmailChange() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public RequestPreferredEmailChange(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Request change of preferred email
	 *
	 * @param user
	 * @param email
	 */
	public void requestChange(final User user, final String email) {

		this.user = user;
		this.email = email;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Requesting change of preferred email failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Request to change preferred email was submitted.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testRemoving() {

		boolean result = true;
		String errorMsg = "";

		if(user == null){
			errorMsg += "Wrong parameter <strong>User</strong>.</br>";
			result = false;
		}
		if(!JsonUtils.isValidEmail(email)){
			errorMsg += "Wrong parameter <strong>Email</strong.>";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Wrong parameter", errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {
		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("user", new JSONNumber(user.getId()));
		jsonQuery.put("email", new JSONString(email));
		return jsonQuery;
	}

}
