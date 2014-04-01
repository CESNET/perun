package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoResourcesTabItem;

/**
 * Returns the count of the resources in VO.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetResourcesCount implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO ID
	private int voId;
	// JSON URL
	private static final String JSON_URL = "resourcesManager/getResourcesCount";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Label with number
	private Label resourcesCountLabel = new Label();
	private Hyperlink hyp = new Hyperlink();

	/**
	 * Creates a new instance of the method
	 * @param id VO id.
	 */
	public GetResourcesCount(int id) {
		this.voId = id;
	}

	/**
	 * Creates a new instance of the method
	 * @param id VO id.
	 * @param events events
	 */
	public GetResourcesCount(int id, JsonCallbackEvents events) {
		this.voId = id;
		this.events = events;
	}


	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData()
	{
		final String param = "vo=" + this.voId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}


	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		events.onError(error);
		session.getUiElements().setLogErrorText("Error while loading resources count.");
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		events.onLoadingStart();
		session.getUiElements().setLogText("Loading resources count.");
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		BasicOverlayType count = (BasicOverlayType) jso;
		session.getUiElements().setLogText("Loading resources count finished: " + count.getInt());
		resourcesCountLabel.setText(String.valueOf(count.getInt()));
		hyp.setText(String.valueOf(count.getInt()));
		events.onFinished(jso);
	}

	/**
	 * Returns the members count label
	 * @return label
	 */
	public Label getResourcesCountLabel()
	{
		return this.resourcesCountLabel;
	}

	/**
	 * Returns the resources count as hyperlink
	 * to VO-Resources
	 *
	 * @return hyperlink
	 */
	public Hyperlink getResourcesCountHyperlink()
	{
		hyp.setTargetHistoryToken(session.getTabManager().getLinkForTab(new VoResourcesTabItem(voId)));
		return this.hyp;
	}

}
