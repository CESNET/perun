package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Returns the count of the facilities of a specified type
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class GetFacilitiesCountByType implements JsonCallback
{
	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO ID
	private String type;
	// JSON URL
	private static final String JSON_URL = "facilitiesManager/getFacilitiesCountByType";

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	
	// Label with number
	private Label facilitiesCountLabel = new Label();
	
	
	/**
     * Creates a new instance of the method
	 *
     * @param type Facility type
	 */
	public GetFacilitiesCountByType(String type) {
		this.type = type;
	}
	
	/**
	 * Creates a new instance of the method
	 *
     * @param type Facility type
	 * @param events Custom callback events
	 */
	public GetFacilitiesCountByType(String type, JsonCallbackEvents events) {
		this.type = type;
		this.events = events;
	}

		
	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData()
	{
		final String param = "type=" + type;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	
	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		this.events.onError(error);
		session.getUiElements().setLogErrorText("Error while loading facilities of type " + type + " count.");
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		this.events.onLoadingStart();
		session.getUiElements().setLogText("Loading facilities count.");
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		BasicOverlayType count = (BasicOverlayType) jso;
		session.getUiElements().setLogText("Loading facilities count of type " + type + " finished: " + count.getInt());
		this.facilitiesCountLabel.setText(String.valueOf(count.getInt()));
		this.events.onFinished(jso);
	}	
	
	/**
	 * Returns the count label
	 * @return
	 */
	public Label getFacilitiesCountLabel()
	{
		return this.facilitiesCountLabel;
	}
	
}
