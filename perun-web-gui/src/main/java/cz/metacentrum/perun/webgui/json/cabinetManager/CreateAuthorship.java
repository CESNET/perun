package cz.metacentrum.perun.webgui.json.cabinetManager;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which creates a new Authorship (adds user as author of publication in perun).
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateAuthorship {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// publication / user
	private int pubId = 0;
	private int userId = 0;
	// URL to call
	final String JSON_URL = "cabinetManager/createAuthorship";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public CreateAuthorship() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public CreateAuthorship(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false when process can/can't continue
	 */
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

		if(pubId == 0){
			errorMsg += "Wrong parameter 'Publication ID'.\n";
			result = false;
		}
		if(userId == 0){
			errorMsg += "Wrong parameter 'User ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a new Authorship, it first tests the values and then submits them.
	 *
	 * @param pubId ID of publication
	 * @param userId ID of user
	 */
	public void createAuthorship(final int pubId, final int userId)
	{
		this.userId = userId;
		this.pubId = pubId;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding Author: "+ userId+" for publication: "+ pubId +" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Author: " + userId + " successfully added for publication: " + pubId);
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
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		// category
		JSONObject authorship = new JSONObject();
		authorship.put("id", null);
		authorship.put("publicationId", new JSONNumber(pubId));
		authorship.put("userId", new JSONNumber(userId));
		authorship.put("createdBy", new JSONString(session.getPerunPrincipal().getActor()));
		authorship.put("createdDate", null);
		authorship.put("createdByUid", new JSONNumber(session.getActiveUser().getId()));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("authorship", authorship);
		return jsonQuery;

	}

}
