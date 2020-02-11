package cz.metacentrum.perun.webgui.json.tasksManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to get All resources propagation state for some VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class GetAllResourcesState implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "tasksManager/getAllResourcesState";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int voId = 0;

	/**
	 * New instance of get all resources state
	 *
	 * @param voId - ID of VO to get resources state for
	 */
	public GetAllResourcesState(int voId) {
		this.voId = voId;
	}

	/**
	 * New instance of get all resources state with external events
	 *
	 * @param voId - ID of VO to get resources state for
	 * @param events external events
	 */
	public GetAllResourcesState(int voId, JsonCallbackEvents events) {
		this.voId = voId;
		this.events = events;
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading ResourceState");
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading ResourceState started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "voId="+voId, this);
	}

}