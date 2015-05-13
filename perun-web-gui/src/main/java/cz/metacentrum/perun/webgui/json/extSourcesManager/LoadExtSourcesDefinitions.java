package cz.metacentrum.perun.webgui.json.extSourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to load external sources to Perun from local file definition.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class LoadExtSourcesDefinitions implements JsonCallback {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// jsonCallback string
	private final String JSON_URL = "extSourcesManager/loadExtSourcesDefinitions";

	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new callback
	 */
	public LoadExtSourcesDefinitions() {}

	/**
	 * Creates a new callback
	 *
	 * @param events external events
	 */
	public LoadExtSourcesDefinitions(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading external sources.");
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading external sources started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Loading external sources finished.");
		events.onFinished(jso);
	}

}
