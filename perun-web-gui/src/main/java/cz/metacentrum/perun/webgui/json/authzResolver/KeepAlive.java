package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to keep connection
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class KeepAlive implements JsonCallback {

	private PerunWebSession session = PerunWebSession.getInstance();
	static private final String JSON_URL = "authzResolver/keepAlive";
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new callback
	 */
	public KeepAlive() {}

	/**
	 * Creates a new callback
	 *
	 * @param events external events
	 */
	public KeepAlive(JsonCallbackEvents events) {
		this.events = events;
	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	@Override
	public void onError(PerunError error) {
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		events.onLoadingStart();
	}

	@Override
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.setHidden(true);
		js.retrieveData(JSON_URL, this);
	}

}
