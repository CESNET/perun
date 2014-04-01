package cz.metacentrum.perun.webgui.json.ownersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which deletes owner
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteOwner {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "ownersManager/deleteOwner";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// id
	private int ownerId = 0;

	/**
	 * Creates a new request
	 */
	public DeleteOwner() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public DeleteOwner(JsonCallbackEvents events) {
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

		if(ownerId == 0){
			errorMsg += "Wrong parametr Owner ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to delete owner, it first tests the values and then submits them.
	 *
	 * @param ownerId ID of owner to be deleted
	 */
	public void deleteOwner(final int ownerId)
	{

		this.ownerId = ownerId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting Owner: " + ownerId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Owner " + ownerId + " deleted.");
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
	private JSONObject prepareJSONObject() {

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("owner", new JSONNumber(ownerId));
		return jsonQuery;

	}

}
