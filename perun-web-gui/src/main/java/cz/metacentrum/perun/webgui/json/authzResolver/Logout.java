package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to logout from RPC
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Logout implements JsonCallback {

	private PerunWebSession session = PerunWebSession.getInstance();
	static private final String JSON_URL = "utils/logout";
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new callback
	 */
	public Logout() {}

	/**
	 * Creates a new callback
	 *
	 * @param events external events
	 */
	public Logout(JsonCallbackEvents events) {
		this.events = events;
	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Logout from Perun successfully finished.");
		events.onFinished(jso);
	}

	@Override
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Logging out from Perun failed.");
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		session.getUiElements().setLogText("Triggering logout.");
		events.onLoadingStart();
	}

	@Override
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

}
