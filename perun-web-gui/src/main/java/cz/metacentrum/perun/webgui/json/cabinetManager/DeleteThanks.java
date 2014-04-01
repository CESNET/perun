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
 * Ajax query which deletes a Thanks
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteThanks {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// thanks
	private int thanksId = 0;
	// URL to call
	final String JSON_URL = "cabinetManager/deleteThanks";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 *
	 */
	public DeleteThanks() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public DeleteThanks(JsonCallbackEvents events) {
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

		if(thanksId == 0){
			errorMsg += "Wrong parameter 'Acknowledgement ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to delete a Publication, it first tests the values and then submits them.
	 *
	 * @param pubId ID of publication to be deleted
	 */
	public void deleteThanks(final int thanksId) {

		this.thanksId = thanksId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting acknowledgement " + thanksId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Acknowledgement " + thanksId + " deleted.");
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
		JSONObject thanks = new JSONObject();
		thanks.put("id", new JSONNumber(thanksId));

		return thanks;

	}

}
