package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which tries to validate user's preferred email address
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ValidatePreferredEmailChange implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "usersManager/validatePreferredEmailChange";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private String i = "";
	private String m = "";
	private int u = 0;
	private boolean hidden = false;

	/**
	 * Creates a new request
	 *
	 * @param i decrypted parameter
	 * @param m encrypted parameter
	 * @param u user ID to request validation for
	 */
	public ValidatePreferredEmailChange(String i, String m, int u) {
		this.i = i;
		this.m = m;
		this.u = u;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param i decrypted parameter
	 * @param m encrypted parameter
	 * @param u user ID to request validation for
	 * @param events Custom events
	 */
	public ValidatePreferredEmailChange(String i, String m, int u, JsonCallbackEvents events) {
		this.events = events;
		this.i = i;
		this.m = m;
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

		params += "i="+i+"&m="+m+"&u="+u;

		JsonClient client = new JsonClient();
		client.setHidden(hidden);
		client.retrieveData(JSON_URL, params, this);

	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
