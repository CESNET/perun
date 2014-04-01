package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunStatus;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoMembersTabItem;

/**
 * Returns the count of the members in VO.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetMembersCount implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO ID
	private int voId;
	// JSON URL
	private static final String JSON_URL = "membersManager/getMembersCount";

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Label with number
	private Label membersCountLabel = new Label();
	private Hyperlink hyp = new Hyperlink();

	private PerunStatus status;

	/**
	 * Creates a new instance of the method
	 *
	 * @param id VO id.
	 * @param status PerunStatus
	 */
	public GetMembersCount(int id, PerunStatus status) {
		this.voId = id;
		this.status= status;
	}

	/**
	 * Creates a new instance of the method
	 *
	 * @param id VO id.
	 * @param status PerunStatus
	 * @param events externalEvents
	 */
	public GetMembersCount(int id, PerunStatus status, JsonCallbackEvents events) {
		this.voId = id;
		this.status= status;
		this.events = events;
	}


	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData()
	{
		String param = "vo=" + this.voId;
		if (status != null) {
			param = param + "&status=" + status;
		}
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}


	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		this.events.onError(error);
		session.getUiElements().setLogErrorText("Error while loading members count.");
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		this.events.onLoadingStart();
		session.getUiElements().setLogText("Loading members count.");
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		BasicOverlayType count = (BasicOverlayType) jso;
		session.getUiElements().setLogText("Loading members count finished: " + count.getInt());
		this.membersCountLabel.setText(String.valueOf(count.getInt()));
		this.hyp.setText(String.valueOf(count.getInt()));
		this.events.onFinished(jso);
	}

	/**
	 * Returns the members count label
	 * @return label
	 */
	public Label getMembersCountLabel()
	{
		return this.membersCountLabel;
	}

	/**
	 * Returns the Members count as hyperlink
	 * to VO-Members
	 *
	 * @return hyperlink
	 */
	public Hyperlink getMembersCountHyperlink()
	{
		hyp.setTargetHistoryToken(session.getTabManager().getLinkForTab(new VoMembersTabItem(voId)));
		return this.hyp;
	}

}
