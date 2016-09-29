package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which adds login in selected namespace if login is available and user doesn't have login in that namespace.
 * ! For service users only !
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SetLogin {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/setLogin";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// local variables for entity to send
	private User user;
	private int userId;
	private String login;
	private String namespace;

	/**
	 * Creates a new request
	 */
	public SetLogin() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public SetLogin(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Set new login for user
	 *
	 * @param user
	 * @param namespace
	 * @param login
	 */
	public void setLogin(final User user, final String namespace, final String login) {

		this.user = user;
		this.userId = user.getId();
		this.login = login;
		this.namespace = namespace;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding login to user: " + user.getFullName() + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Login successfully added to user: "+user.getFullName());
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
	 * Set new login for user
	 *
	 * @param userId
	 * @param namespace
	 * @param login
	 */
	public void setLogin(final int userId, final String namespace, final String login) {

		this.userId = userId;
		this.login = login;
		this.namespace = namespace;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding login to user: " + userId + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Login successfully added to user: "+userId);
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
	private boolean testAdding() {

		boolean result = true;
		String errorMsg = "";

		if(user != null && !user.isSpecificUser()){
			errorMsg += "Only service users can have login(s) added this way.</br>";
			result = false;
		}
		if(userId <= 0){
			errorMsg += "ID of User can't be <= 0.</br>";
			result = false;
		}
		if(login == null || login.isEmpty()){
			errorMsg += "Login can't be empty.</br>";
			result = false;
		}
		if(namespace == null || namespace.isEmpty()){
			errorMsg += "Namespace can't be empty.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
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
		jsonQuery.put("user", new JSONNumber(userId));
		jsonQuery.put("login", new JSONString(login));
		jsonQuery.put("namespace", new JSONString(namespace));

		return jsonQuery;

	}

}
