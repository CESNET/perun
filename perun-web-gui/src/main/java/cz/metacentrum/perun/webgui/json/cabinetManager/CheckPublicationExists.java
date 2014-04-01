package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which checks if publication exists in DB
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CheckPublicationExists implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "cabinetManager/checkPublicationExists";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private int pubSysId = 0;
	private int externalId = 0;
	private String isbn = "";

	/**
	 * Creates a new request
	 *
     */
	public CheckPublicationExists(int pubSysId, int externalId, String isbn) {
		this.pubSysId = pubSysId;
		this.externalId = externalId;
		this.isbn = isbn;
	}

	/**
	 * Creates a new request with custom events
	 *
     * @param events external events
     */
	public CheckPublicationExists(int pubSysId, int externalId, String isbn, JsonCallbackEvents events) {
		this.events = events;
		this.pubSysId = pubSysId;
		this.externalId = externalId;
		this.isbn = isbn;
	}

	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogSuccessText("Publication checked.");
		events.onFinished(jso);
	}

	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Checking if publication exists failed.");
		events.onError(error);
	}

	public void onLoadingStart() {
		events.onLoadingStart();
	}

	public void retrieveData() {

		String parms = "";

		if (pubSysId != 0 && externalId != 0) {
			parms += "pubSysId="+pubSysId+"&externalId="+externalId+"&";
		}
		if (isbn != "") {
			parms += "isbn="+isbn;
		}

		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, parms, this);

	}

}