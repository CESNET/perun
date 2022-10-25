package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Class to get new GUI alert
 */
public class GetNewGuiAlert implements JsonCallback {

	// SESSION
	private PerunWebSession session = PerunWebSession.getInstance();

	// PARAMS
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// URLs
	static private final String URL = "utils/getNewGuiAlert";

	/**
	 * New callback instance
	 */
	public GetNewGuiAlert() {}

	/**
	 * New callback instance
	 */
	public GetNewGuiAlert(JsonCallbackEvents events) {
		this.events = events;
	}

	@Override
	public void retrieveData() {

		JsonClient js = new JsonClient();
		js.retrieveData(URL, this);

	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Loading new GUI alert finished.");
		events.onFinished(jso);
	}

	@Override
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading new GUI alert.");
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		events.onLoadingStart();
	}

}
