package cz.metacentrum.perun.webgui.json.generalServiceManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to get info about denial of service on facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class IsServiceBlockedOnFacility implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service ID
	private int serviceId = 0;
	private int facilityId = 0;
	// returned object
	private BasicOverlayType bot;
	// JSON URL
	private static final String JSON_URL = "generalServiceManager/isServiceBlockedOnFacility";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of facility
	 * @param serviceId ID of service
	 */
	public IsServiceBlockedOnFacility(int facilityId, int serviceId) {
		this.facilityId = facilityId;
		this.serviceId = serviceId;
	}

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of facility
	 * @param serviceId ID of service
	 * @param events external events
	 */
	public IsServiceBlockedOnFacility(int facilityId, int serviceId, JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.serviceId = serviceId;
		this.events = events;
	}

	/**
	 * Retrieves data
	 */
	public void retrieveData()
	{
		final String param = "service=" + this.serviceId + "&facility="+this.facilityId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Updates services in table from RPC request
	 *
	 * @param bot services array of js objects returned from RPC
	 */
	private void update(BasicOverlayType bot) {
		this.bot = bot;
	}

	/**
	 * Return bot object returned from RPC
	 *
	 * @return bot object
	 */
	public BasicOverlayType getBot() {
		return bot;
	}

	/**
	 * Called when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading denied info about service");
		this.events.onError(error);
	}

	/**
	 * Called when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading denied info about service started.");
		this.events.onLoadingStart();

	}

	/**
	 * Called when loading successfully finishes
	 *
	 * @param jso javascript object (array) returned from RPC
	 */
	public void onFinished(JavaScriptObject jso) {
		BasicOverlayType bot = jso.cast();
		this.update(bot);
		session.getUiElements().setLogText("Loading denied info about service finished.");
		events.onFinished(bot);
	}

}
