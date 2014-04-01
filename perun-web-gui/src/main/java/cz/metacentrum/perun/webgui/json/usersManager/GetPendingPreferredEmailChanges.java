package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Get all user's email addresses which are awaiting validation.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetPendingPreferredEmailChanges implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "usersManager/getPendingPreferredEmailChanges";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private int u = 0;
	private boolean hidden = false;

	/**
	 * Creates a new request
	 *
	 * @param u user ID to request validation for
	 */
	public GetPendingPreferredEmailChanges(int u) {
		this.u = u;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param u user ID to request validation for
	 * @param events Custom events
	 */
	public GetPendingPreferredEmailChanges(int u, JsonCallbackEvents events) {
		this.events = events;
		this.u = u;
	}

	public void onFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	public void onError(PerunError error) {
		events.onError(error);
	}

	public void onLoadingStart() {
		events.onLoadingStart();
	}

	public void retrieveData() {

		String params = "";

		params += "user="+u;

		JsonClient client = new JsonClient();
		client.setHidden(hidden);
		client.retrieveData(JSON_URL, params, this);

	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
