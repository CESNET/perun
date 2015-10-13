package cz.metacentrum.perun.webgui.json.securityTeamsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which deletes SecurityTeam from Perun
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class DeleteSecurityTeam {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "securityTeamsManager/deleteSecurityTeam";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int securityTeamId = 0;
	private boolean force = false;

	/**
	 * Creates a new request
	 *
	 */
	public DeleteSecurityTeam() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public DeleteSecurityTeam(JsonCallbackEvents events) {
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
			Confirm c = new Confirm("Error while deleting Security Team", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to delete Security Team, it first tests the values and then submits them.
	 *
	 * @param securityTeamId ID of SecurityTeam to be deleted
	 * @param force true for force delete
	 */
	public void deleteSecurityTeam(final int securityTeamId,final boolean force) {

		this.securityTeamId = securityTeamId;
		this.force = force;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting Security Team: " + securityTeamId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Security Team " + securityTeamId + " deleted.");
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
		if (force) {
			jsonQuery.put("force", null);
		}
		return jsonQuery;
	}

}
