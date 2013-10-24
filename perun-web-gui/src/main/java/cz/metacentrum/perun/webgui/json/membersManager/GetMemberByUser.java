package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * MembersManager/GetMemberByUser
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class GetMemberByUser implements JsonCallback {

	// User ID
	private int userId;

	// VO ID
	private int voId;

	// JSON URL
	static private final String JSON_URL = "membersManager/getMemberByUser";

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// User ID label
	private Label idLabel = new Label();

	// The user itself
	private Member member;
	
	private boolean hidden = false;

	/**
	 * New instance of member info
     *
	 * @param voId 
	 * @param userId
	 */
	public GetMemberByUser(int voId, int userId) {
		this.voId = voId;
		this.userId = userId;
	}

	/**
	 * New instance of member info.
     *
	 * @param voId
	 * @param userId
	 * @param events
	 */
	public GetMemberByUser(int voId, int userId, JsonCallbackEvents events) {
		this.userId = userId;
		this.voId = voId;
		this.events = events;
	}


	/**
	 * Returns user info
	 * @return
	 */
	public Widget getDecoratedFlexTable(){
		this.retrieveData();

		// User info table
		FlexTable layout = new FlexTable();
		layout.setCellSpacing(6);

		// Add some standard form options
		layout.setHTML(0, 0, "Member ID:");
		layout.setWidget(0, 1, this.idLabel);

		// wrap the content in a DecoratorPanel
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setWidget(layout);

		return decPanel;
	}

	/**
	 * Retrieves data
	 */
	public void retrieveData(){

		final String param = "user=" + this.userId + "&vo=" + this.voId;

		// retrieve data
		JsonClient js = new JsonClient();
		js.setHidden(hidden);
		js.retrieveData(JSON_URL, param, this);		
	}

	/**
	 * Updates data in the labels.
	 */
	private void updateData() {
		// set id
		this.idLabel.setText(String.valueOf(this.member.getId()));
	}

	public Member getMember() {
		return member;
	}

	/**
	 * When successfully finishes
	 */
	public void onFinished(JavaScriptObject jso) {
		this.member = (Member) jso;
		this.updateData();
		//session.getUiElements().setLogText("Member found.");
		events.onFinished(member);
	}

	/**
	 * When error
	 */
	public void onError(PerunError error) {
		//session.getUiElements().setLogErrorText("Error while loading member for user: " + this.userId);
		events.onError(error);
	}

	/**
	 * When start
	 */
	public void onLoadingStart() {
		//session.getUiElements().setLogText("Loading member for user started.");
		events.onLoadingStart();
	}
	
	/**
	 * Sets events to this callback
	 * 
	 * @param events
	 */
	public void setEvents(JsonCallbackEvents events){
		this.events = events;
	}
	
	/**
	 * Set callback as hidden (do not show error popup)
	 * @param hidden
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
}	