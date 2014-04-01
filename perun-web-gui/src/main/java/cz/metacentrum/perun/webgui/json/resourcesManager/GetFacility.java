package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;

/**
 * Ajax query to get facility object for specified resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class GetFacility implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// resource
	private int resourceId;
	// JSON URL
	static private final String JSON_URL = "resourcesManager/getFacility";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// The Resource itself
	private Facility facility;
	// The facility link wrapper
	private SimplePanel facilityLinkWrapper = new SimplePanel();

	/**
	 * New instance of callback
	 *
	 * @param id ID of resource to get facility for
	 */
	public GetFacility(int id) {
		this.resourceId = id;
	}

	/**
	 * New instance of callback with custom events
	 *
	 * @param id ID of resource to get facility for
	 * @param events custom events
	 */
	public GetFacility(int id, JsonCallbackEvents events) {
		this(id);
		this.events = events;
	}

	/**
	 * Returns label with link
	 */
	public Widget getLabelWithLink()
	{
		return facilityLinkWrapper;
	}

	/**
	 * Returns facility object associated with resource
	 *
	 * @return facility object
	 */
	public Facility getFacility() {
		return this.facility;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		final String param = "resource=" + this.resourceId;
		// retrieve data
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * When successfully finishes
	 *
	 * @param jso javascript objects (array) returned from RPC
	 */
	public void onFinished(JavaScriptObject jso) {
		this.facility = (Facility) jso;
		session.getUiElements().setLogText("Loading facility for resource : " + this.resourceId + " finished.");

		// creates the link
		Hyperlink link = new Hyperlink(facility.getName(),session.getTabManager().getLinkForTab(new FacilityDetailTabItem(facility)));
		facilityLinkWrapper.setWidget(link);

		// custom on finished
		events.onFinished(facility);
	}

	/**
	 * When error
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading facility for resource: " + this.resourceId);
		events.onError(error);
	}

	/**
	 * When loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading facility for resource started.");
		events.onLoadingStart();
	}

}
