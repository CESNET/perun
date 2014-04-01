package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Publication;

/**
 * Ajax query which updates a Publication
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdatePublication {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// category
	private Publication publication;
	// URL to call
	final String JSON_URL = "cabinetManager/updatePublication";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public UpdatePublication() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public UpdatePublication(JsonCallbackEvents events) {
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

		if(publication == null){
			errorMsg += "Publication cannot be null.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to update a Publication, it first tests the values and then submits them.
	 *
	 * @param publication Publication
	 */
	public void updatePublication(final Publication publication) {

		this.publication = publication;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating publication: " + publication.getId() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Publication pub = jso.cast();
				session.getUiElements().setLogSuccessText("Publication with ID: " + pub.getId() + " updated.");
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

		// publication
		JSONObject oldPub = new JSONObject(publication);
		// reconstruct object
		JSONObject newPub = new JSONObject();
		newPub.put("id", oldPub.get("id"));
		newPub.put("year", oldPub.get("year"));
		newPub.put("categoryId", oldPub.get("categoryId"));
		newPub.put("externalId", oldPub.get("externalId"));
		newPub.put("isbn", oldPub.get("isbn"));
		newPub.put("main", oldPub.get("main"));
		newPub.put("publicationSystemId", oldPub.get("publicationSystemId"));
		newPub.put("title", oldPub.get("title"));
		newPub.put("createdBy", oldPub.get("createdBy"));
		newPub.put("createdDate", oldPub.get("createdDate"));
		newPub.put("rank", oldPub.get("rank"));
		newPub.put("doi", oldPub.get("doi"));
		newPub.put("locked", oldPub.get("locked"));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("publication", newPub);
		return jsonQuery;

	}

}
