package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Thanks;

/**
 * Ajax query which creates a new Thanks
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateThanks {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "cabinetManager/createThanks";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data
	private int ownerId;
	private int publicationId;

	/**
	 * Creates a new request
	 *
	 * @param publicationId ID of Publication
	 */
	public CreateThanks(int publicationId) {
		this.publicationId = publicationId;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param publicationId ID of Publication
	 * @param events external events
	 */
	public CreateThanks(int publicationId, JsonCallbackEvents events) {
		this.publicationId = publicationId;
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false when process can/can't continue
	 */
	private boolean testCreating() {

		boolean result = true;
		String errorMsg = "";

		if(publicationId == 0){
			errorMsg += "Wrong parameter <strong>Publication ID</strong>.<br />";
			result = false;
		}

		if(ownerId == 0){
			errorMsg += "Wrong parameter <strong>Owner</strong>.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a Thanks, it first tests the values and then submits them.
	 *
	 * @param ownerId owner to thank to
	 */
	public void createThanks(final int ownerId) {

		this.ownerId = ownerId;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating acknowledgement failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Thanks thks = jso.cast();
				session.getUiElements().setLogSuccessText("Acknowledgement : " + thks.getId() + " created.");
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
	private JSONObject prepareJSONObject() {

		JSONObject jsonQuery = new JSONObject();

		JSONObject thanks = new JSONObject();
		thanks.put("id", null);
		thanks.put("ownerId", new JSONNumber(ownerId));
		thanks.put("publicationId", new JSONNumber(publicationId));
		thanks.put("createdBy", new JSONString(session.getPerunPrincipal().getActor()));
		thanks.put("createdByUid", new JSONNumber(session.getActiveUser().getId()));

		jsonQuery.put("thanks", thanks);

		return jsonQuery;

	}

}
