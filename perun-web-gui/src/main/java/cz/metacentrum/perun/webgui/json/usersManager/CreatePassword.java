package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Create user's password (reserve in external system + validate)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class CreatePassword {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/createPassword";
	final String JSON_URL_RESERVE = "usersManager/reservePassword";
	final String JSON_URL_RESERVE_RANDOM = "usersManager/reserveRandomPassword";
	final String JSON_URL_VALIDATE = "usersManager/validatePassword";
	final String JSON_URL_VALIDATE_AND_SET_USER_EXT_SOURCE = "usersManager/validatePasswordAndSetExtSources";

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// local variables for entity to send
	private int userId;
	private String namespace = "";
	private String pass = "";
	private String login = "";

	/**
	 * Creates a new request
	 */
	public CreatePassword() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 * @param events custom events
	 */
	public CreatePassword(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Changes password for the user
	 *
	 * @param userId user to set password for
	 * @param login used for validation only
	 * @param namespace defined login in namespace
	 * @param pass password to set
	 */
	public void createPassword(int userId, String login, String namespace, String pass) {

		this.userId = userId;
		this.namespace = namespace;
		this.pass = pass;
		this.login = login;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// final events
		final JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating password failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Password created successfully.");
				events.onFinished(jso);
			};
		};

		// validate event
		JsonCallbackEvents validateEvent = new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating password failed.");
				events.onError(error); // custom events
			};
			@Override
			public void onFinished(JavaScriptObject jso) {
				JsonPostClient jspc = new JsonPostClient(newEvents);
				jspc.sendData(JSON_URL_VALIDATE_AND_SET_USER_EXT_SOURCE, validateCallJSON());
			};
			@Override
			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(validateEvent);
		jspc.sendData(JSON_URL_RESERVE, prepareJSONObject());

	}

	/**
	 * Create empty password for the user - random password is generated on KDC side
	 *
	 * @param userId user to set password for
	 * @param login used for validation only
	 * @param namespace defined login in namespace
	 */
	public void createRandomPassword(int userId, String login, String namespace) {

		this.userId = userId;
		this.namespace = namespace;
		this.login = login;

		// test arguments
		String errorMsg = "";

		if(userId == 0){
			errorMsg += "<p>User ID can't be 0.";
		}

		if(namespace.isEmpty()){
			errorMsg += "<p>Namespace can't be empty.";
		}

		if(login.isEmpty()){
			errorMsg += "<p>Login to create password for can't be empty.";
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating password.", new HTML(errorMsg), true);
			c.show();
			return;
		}

		// final events
		final JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating password failed.");
				events.onError(error); // custom events
			};
			@Override
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Password created successfully.");
				events.onFinished(jso);
			};
		};

		// validate event
		JsonCallbackEvents validateEvent = new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating password failed.");
				events.onError(error); // custom events
			};
			@Override
			public void onFinished(JavaScriptObject jso) {
				JsonPostClient jspc = new JsonPostClient(newEvents);
				jspc.sendData(JSON_URL_VALIDATE_AND_SET_USER_EXT_SOURCE, validateCallJSON());
			};
			@Override
			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(validateEvent);
		jspc.sendData(JSON_URL_RESERVE_RANDOM, prepareJSONObject());

	}


	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding() {

		boolean result = true;
		String errorMsg = "";

		if(userId == 0){
			errorMsg += "<p>User ID can't be 0.";
			result = false;
		}

		if(namespace.isEmpty()){
			errorMsg += "<p>Namespace can't be empty.";
			result = false;
		}

		if(pass.isEmpty()){
			errorMsg += "<p>Password to create can't be empty.";
			result = false;
		}

		if(login.isEmpty()){
			errorMsg += "<p>Login to create password for can't be empty.";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating password.", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Prepares a JSON object for password reservation.
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("user", new JSONNumber(userId));
		jsonQuery.put("namespace", new JSONString(namespace));
		jsonQuery.put("password", new JSONString(pass));
		jsonQuery.put("login", new JSONString(login));
		return jsonQuery;
	}

	/**
	 * Prepares a JSON object for validation request.
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject validateCallJSON() {
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("user", new JSONNumber(userId));
		jsonQuery.put("namespace", new JSONString(namespace));
		jsonQuery.put("login", new JSONString(login));
		return jsonQuery;
	}

	/**
	 * Validates password and sets user ext sources
	 *
	 * @param userId user to set password for
	 * @param login used for validation only
	 * @param namespace defined login in namespace
	 */
	public void validateAndSetUserExtSources(int userId, String login, String namespace) {

		this.userId = userId;
		this.namespace = namespace;
		this.login = login;

		// test arguments
		String errorMsg = "";

		if(userId == 0){
			errorMsg += "<p>User ID can't be 0.";
		}

		if(namespace.isEmpty()){
			errorMsg += "<p>Namespace can't be empty.";
		}

		if(login.isEmpty()){
			errorMsg += "<p>Login to create password for can't be empty.";
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating password.", new HTML(errorMsg), true);
			c.show();
			return;
		}

		JsonPostClient jspc = new JsonPostClient(events);
		jspc.sendData(JSON_URL_VALIDATE_AND_SET_USER_EXT_SOURCE, validateCallJSON());

	}

}
