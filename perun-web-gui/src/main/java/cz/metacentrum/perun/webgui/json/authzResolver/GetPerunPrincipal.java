package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunPrincipal;

/**
 * Ajax qeury to logIn to RPC and get PerunPrincipal
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class GetPerunPrincipal implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "authzResolver/getPerunPrincipal";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new callback
	 */
	public GetPerunPrincipal() {}

	/**
	 * Creates a new callback
	 *
	 * @param events external events
	 */
	public GetPerunPrincipal(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Retrieves data
	 */
	public void retrieveData(){
		// retrieve data
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

	/**
	 * When successfully finishes
	 */
	public void onFinished(JavaScriptObject jso) {
		PerunPrincipal pp = (PerunPrincipal) jso;
		pp.setObjectType("PerunPrincipal");
		session.getUiElements().setLogText("Logged as: " + pp.getActor() + " with Ext source: " + pp.getExtSource());
		events.onFinished(jso);
	}

	/**
	 * When error
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Unable to LogIn (retrieve Perun Principal)");
		events.onError(error);
	}

	/**
	 * When loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Authentication process started.");
		events.onLoadingStart();
	}

}
