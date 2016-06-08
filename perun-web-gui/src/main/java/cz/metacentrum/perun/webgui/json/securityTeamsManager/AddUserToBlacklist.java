package cz.metacentrum.perun.webgui.json.securityTeamsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which adds user to SecurityTeams blacklist
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class AddUserToBlacklist {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "securityTeamsManager/addUserToBlacklist";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int securityTeamId = 0;
	private int userId = 0;
	private String description;

	/**
	 * Creates a new request
	 * @param id
	 */
	public AddUserToBlacklist(int id) {
		this.securityTeamId = id;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param id
	 * @param events Custom events
	 */
	public AddUserToBlacklist(int id, JsonCallbackEvents events) {
		this.securityTeamId = id;
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testDeleting()
	{
		boolean result = true;
		String errorMsg = "";

		if(securityTeamId == 0){
			errorMsg += "Wrong parameter <strong>SECURITY TEAM ID</strong>.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while adding user to blacklist", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to add user to blacklist of Security Team, it first tests the values and then submits them.
	 *
	 * @param userId ID of User to be added to blacklist
	 * @param description Why is user blacklisted
	 */
	public void addUserToBlacklist(final int userId, String description) {

		this.userId = userId;
		this.description = description;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding user " + userId + " to blacklist failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User " + userId + " added to blacklist.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("securityTeam", new JSONNumber(securityTeamId));
		jsonQuery.put("user", new JSONNumber(userId));
		jsonQuery.put("description", new JSONString(description));
		return jsonQuery;
	}

}
