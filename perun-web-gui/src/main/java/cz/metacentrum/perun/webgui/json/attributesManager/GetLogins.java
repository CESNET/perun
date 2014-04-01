package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Retrieves logins for a user
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetLogins implements JsonCallback{

	// Perun session
	private PerunWebSession session = PerunWebSession.getInstance();

	// JSON URL
	private static final String JSON_URL = "attributesManager/getLogins";

	// user id
	private int userId;

	// events
	private JsonCallbackEvents events;



	/**
	 * Creates new instance of callback
	 *
	 * @param userId User ID
	 */
	public GetLogins(int userId) {
		this(userId, new JsonCallbackEvents());
	}


	/**
	 * Creates new instance of callback
	 *
	 * @param userId User ID
	 * @param events external events
	 */
	public GetLogins(int userId, JsonCallbackEvents events) {
		this.events = events;
		this.userId = userId;
	}

	/**
	 * Retrieves data from the RPC
	 */
	public void retrieveData() {

		JsonClient js = new JsonClient();
		js.retrieveData(GetLogins.JSON_URL, "user=" + userId, this);
	}

	/**
	 * When an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading logins.");
		this.events.onError(error);
	}

	/**
	 * When loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading logins started.");
		this.events.onLoadingStart();

	}

	/**
	 * When callback finishes
	 *
	 * @param jso returned object = array of js elements to be processed
	 */
	public void onFinished(JavaScriptObject jso) {
		JsArray<Attribute> attributes = JsonUtils.jsoAsArray(jso);
		session.getUiElements().setLogText("Loading logins finished: " + attributes.length());
		events.onFinished(attributes);
	}

}
