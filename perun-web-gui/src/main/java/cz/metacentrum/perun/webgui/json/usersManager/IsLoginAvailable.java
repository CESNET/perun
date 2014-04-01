package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;

/**
 * Whether is login available.
 * The result is in BasicOverlayType in format {value: true} or {value: false}
 * You can specify the namespace or the request uses ALL the namespaces
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class IsLoginAvailable implements JsonCallback {

	// URL to call
	final String JSON_URL = "usersManager/isLoginAvailable";

	// external events
	private JsonCallbackEvents finalEvents = new JsonCallbackEvents();

	// if namespace empty, all namespaces are checked
	private String namespace = "";

	// how many checked namespaces
	private int checkedNamespaces = 0;

	// whether is available
	private boolean isAvailable = true;

	// namespaces to check
	private ArrayList<String> namespacesToCheck = new ArrayList<String>();

	// login
	private String login = "";

	/**
	 * tested login namespaces
	 */
	static public final String[] LOGIN_NAMESPACES = {"cesnet", "egi-ui", "einfra", "meta", "mu"};



	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param login LOGIN
	 * @param events custom events
	 */
	public IsLoginAvailable(String login, JsonCallbackEvents events) {
		this("", login, events);
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param namespace Login namespace
	 * @param login LOGIN
	 * @param events custom events
	 */
	public IsLoginAvailable(String namespace, String login, final JsonCallbackEvents events) {
		this.namespace = namespace;
		this.login = login;
		if(events != null) {
			this.finalEvents = events;
		}
	}

	/**
	 * Inner request whether is login in namespace available
	 *
	 * @param namespace
	 */
	private void isLoginAvailable(String namespace){
		// sending data
		JsonClient req = new JsonClient();
		req.retrieveData(JSON_URL, "login=" + login + "&loginNamespace=" + namespace, this);
	}

	/**
	 * Generates the final response in format {value: true} or {value: false}
	 * @return
	 */
	protected JavaScriptObject generateFinalResponse(){
		JSONBoolean response = JSONBoolean.getInstance(isAvailable);
		JSONObject json = new JSONObject();
		json.put("value", response);
		return json.getJavaScriptObject();
	}


	/**
	 * Whether is login available in selected namespaces
	 */
	protected void isLoginAvailable() {
		checkedNamespaces = 0;
		namespacesToCheck.clear();
		if (namespace.equals("")) {
			for(int i = 0; i < LOGIN_NAMESPACES.length; i++){
				namespacesToCheck.add(LOGIN_NAMESPACES[i]);
			}
		} else {
			namespacesToCheck.add(namespace);
		}
		for(String namespace : namespacesToCheck) {
			isLoginAvailable(namespace);
		}
	}


	/**
	 * If last request OR NOT AVAILABLE, triggers the final events
	 */
	public void onFinished(JavaScriptObject jso){

		checkedNamespaces++;

		BasicOverlayType ot = jso.cast();
		int available = ot.getInt();

		if(available == 0){
			isAvailable = false;
			finalEvents.onFinished(generateFinalResponse());
			return;
		}

		// is available, is the last
		if(checkedNamespaces == namespacesToCheck.size()){
			finalEvents.onFinished(generateFinalResponse());
			return;
		}
	}

	public void onError(PerunError error) {
		finalEvents.onError(error);
	}

	public void onLoadingStart() {
		finalEvents.onLoadingStart();
	}

	/**
	 * Calls the request for checking logins
	 */
	public void retrieveData() {
		isLoginAvailable();
	}

}
