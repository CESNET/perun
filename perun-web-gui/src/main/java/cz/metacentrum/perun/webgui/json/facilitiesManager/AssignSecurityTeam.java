package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which adds security team to facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignSecurityTeam {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// IDS
	private int secTeam = 0;
	private int facility = 0;
	// URL to call
	final String JSON_URL = "facilitiesManager/assignSecurityTeam";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public AssignSecurityTeam() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public AssignSecurityTeam(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false if process can/can't continue
	 */
	private boolean testAdding()
	{
		boolean result = true;
		String errorMsg = "";

		if(facility == 0){
			errorMsg += "Wrong parameter 'Facility'.\n";
			result = false;
		}

		if(secTeam == 0){
			errorMsg += "Wrong parameter 'Security team'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to add security team to facility, it first tests the values and then submits them.
	 *
	 * @param facility ID of facility which should have security team added
	 * @param secTeam ID of SecurityTeam to be added to facility
	 */
	public void assignSecurityTeam(final int facility,final int secTeam)
	{
		this.facility = facility;
		this.secTeam = secTeam;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding SecurityTeam " + secTeam + " to facility "+facility+" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("SecurityTeam " + secTeam + " added to facility "+ facility);
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		// create request
		JsonPostClient request = new JsonPostClient(newEvents);
		request.sendData(JSON_URL, jsonQuery);

	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {
		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", new JSONNumber(facility));
		jsonQuery.put("securityTeam", new JSONNumber(secTeam));
		return jsonQuery;
	}

}
