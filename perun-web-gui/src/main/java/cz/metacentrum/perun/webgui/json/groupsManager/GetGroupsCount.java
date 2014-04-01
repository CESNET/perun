package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoGroupsTabItem;

/**
 * Returns the count of the groups in VO.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetGroupsCount implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO ID
	private int voId = 0;
	// JSON URL
	private static final String JSON_URL = "groupsManager/getGroupsCount";

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Label with number
	private Label groupsCountLabel = new Label();
	private Hyperlink hyp = new Hyperlink();

	/**
	 * Creates a new instance of the method
	 *
	 * @param id VO id.
	 */
	public GetGroupsCount(int id) {
		this.voId = id;
	}

	/**
	 * Creates a new instance of the method
	 *
	 * @param id VO id.
	 */
	public GetGroupsCount(int id, JsonCallbackEvents events) {
		this.voId = id;
		this.events = events;
	}


	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData()
	{
		String param = "vo=" + this.voId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		this.events.onError(error);
		session.getUiElements().setLogErrorText("Error while loading groups count.");
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		this.events.onLoadingStart();
		session.getUiElements().setLogText("Loading groups count.");
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		BasicOverlayType count = (BasicOverlayType) jso;
		session.getUiElements().setLogText("Loading groups count finished: " + count.getInt());
		this.groupsCountLabel.setText(String.valueOf(count.getInt()));
		this.hyp.setText(String.valueOf(count.getInt()));
		this.events.onFinished(jso);
	}

	/**
	 * Returns the members count label
	 * @return label
	 */
	public Label getGroupsCountLabel()
	{
		return this.groupsCountLabel;
	}

	/**
	 * Returns the groups count as hyperlink
	 * to VO-Groups
	 *
	 * @return hyperlink
	 */
	public Hyperlink getGroupsCountHyperlink()
	{
		hyp.setTargetHistoryToken(session.getTabManager().getLinkForTab(new VoGroupsTabItem(voId)));
		return this.hyp;
	}

}
