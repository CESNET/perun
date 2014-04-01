package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Unified callback class to get any PerunEntity by it's ID with optional cache support
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetGuiConfiguration implements JsonCallback {

	// SESSION
	private PerunWebSession session = PerunWebSession.getInstance();

	// PARAMS
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// URLs
	static private final String URL = "utils/getGuiConfiguration";

	/**
	 * New callback instance
	 */
	public GetGuiConfiguration() {}

	/**
	 * New callback instance
	 */
	public GetGuiConfiguration(JsonCallbackEvents events) {
		this.events = events;
	}

	@Override
	public void retrieveData() {

		JsonClient js = new JsonClient();
		js.retrieveData(URL, this);

	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Loading configuration finished.");
		events.onFinished(jso);
	}

	@Override
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading configuration.");
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		events.onLoadingStart();
	}

}
