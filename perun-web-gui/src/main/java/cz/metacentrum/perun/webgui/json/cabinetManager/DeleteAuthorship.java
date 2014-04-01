package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which deletes Authorship
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteAuthorship {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// data
	private int publicationId = 0;
	private int userId = 0;
	// URL to call
	final String JSON_URL = "cabinetManager/deleteAuthorship";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public DeleteAuthorship() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public DeleteAuthorship(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false when process can/can't continue
	 */
	private boolean testDeleting()
	{
		boolean result = true;
		String errorMsg = "";

		if(publicationId == 0){
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
	 * Attempts to delete Authorship, it first tests the values and then submits them.
	 *
	 * @param publicationId
	 * @param userId
	 */
	public void deleteAuthorship(final int publicationId, final int userId) {

		this.publicationId = publicationId;
		this.userId = userId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing author " + userId + " from publication: " + publicationId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Author " + userId + " removed from publication: " + publicationId);
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
		JSONObject category = new JSONObject();
		category.put("publicationId", new JSONNumber(publicationId));
		category.put("userId", new JSONNumber(userId));

		return category;

	}

}
