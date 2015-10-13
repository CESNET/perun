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
import cz.metacentrum.perun.webgui.model.SecurityTeam;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Request, which updates SecurityTeam
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class UpdateSecurityTeam {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// vo
	private SecurityTeam team;

	// URL to call
	final String JSON_URL = "securityTeamsManager/updateSecurityTeam";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public UpdateSecurityTeam() {}

	/**
	 * Creates a new request with custom events
	 * @param events Custom events
	 */
	public UpdateSecurityTeam(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return
	 */
	private boolean testCreating() {

		boolean result = true;
		String errorMsg = "";

		if(team == null){
			errorMsg += "Can't update NULL SecurityTeam.<br />";
			result = false;
		}

		if(team.getName().length() == 0){
			errorMsg += "Security Team must have parameter <strong>Name</strong>.<br />";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while updating SecurityTeam", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to update SecurityTeam, it first tests the values and then submits them.
	 *
	 * @param securityTeam SecurityTeam to update
	 */
	public void updateSecurityTeam(final SecurityTeam securityTeam) {

		this.team = securityTeam;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating Security Team " + securityTeam.getName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Updating Security Team " + securityTeam.getName() + " successful.");
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
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject() {
		// vo
		JSONObject newVo = new JSONObject();
		newVo.put("id", new JSONNumber(team.getId()));
		newVo.put("name", new JSONString(team.getName()));
		newVo.put("description", new JSONString(team.getDescription()));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("securityTeam", newVo);
		return jsonQuery;
	}

}
