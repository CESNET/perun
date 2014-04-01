package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which tries to validate user's email address
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ValidateEmail implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/validateEmail";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private String i = "";
	private String m = "";

	/**
	 * Creates a new request
	 *
	 * @param i
	 * @param m
	 */
	public ValidateEmail(String i, String m) {
		this.i = i;
		this.m = m;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param i
	 * @param m
	 * @param events Custom events
	 */
	public ValidateEmail(String i, String m, JsonCallbackEvents events) {
		this.events = events;
		this.i = i;
		this.m = m;
	}

	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogSuccessText("Email validated.");
		events.onFinished(jso);
	}

	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Validating email failed.");
		events.onError(error);
	}

	public void onLoadingStart() {
		events.onLoadingStart();
	}

	public void retrieveData() {

		String params = "";

		params += "i="+i+"&m="+m;

		JsonClient client = new JsonClient();
		client.retrieveData(JSON_URL, params, this);

	}

}
