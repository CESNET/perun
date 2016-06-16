package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

import java.util.HashMap;
import java.util.Map;

/**
 * Generate users account
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class GenerateAccount {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/generateAccount";

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// local variables for entity to send
	private String namespace = "";
	private String pass = "";
	private Map<String, String> params = new HashMap<>();

	/**
	 * Creates a new request
	 */
	public GenerateAccount() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 * @param events custom events
	 */
	public GenerateAccount(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Generates account in namespace with optional password
	 *
	 * @param namespace defined namespace
	 * @param pass password to set
	 * @param params optional params as map of attribute urn and string value
	 */
	public void generateAccount(String namespace, String pass, Map<String, String> params) {

		this.namespace = namespace;
		this.pass = pass;
		this.params = params;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// final events
		final JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating account failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Account created successfully.");
				events.onFinished(jso);
			};
			@Override
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

		if(namespace.isEmpty()){
			errorMsg += "<p>Namespace can't be empty.";
			result = false;
		}

		if (errorMsg.length()>0) {
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
		jsonQuery.put("namespace", new JSONString(namespace));

		JSONObject param = new JSONObject();

		if (pass != null && !pass.isEmpty()) {
			param.put("password", new JSONString(pass));
		}
		for (String key : params.keySet()) {
			param.put(key, new JSONString(params.get(key)));
		}

		jsonQuery.put("parameters", param);

		return jsonQuery;

	}

}
