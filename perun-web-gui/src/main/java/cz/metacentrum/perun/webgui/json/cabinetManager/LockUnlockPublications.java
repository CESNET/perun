package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Publication;

import java.util.ArrayList;

/**
 * Ajax query which lock or unlock a Publications
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class LockUnlockPublications {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// pubs
	private ArrayList<Publication> publications;
	private Boolean lock;
	// URL to call
	final String JSON_URL = "cabinetManager/lockPublications";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public LockUnlockPublications() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public LockUnlockPublications(JsonCallbackEvents events) {
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

		if(publications == null || publications.isEmpty()){
			errorMsg += "Publications cannot be null or empty.\n";
			result = false;
		}

		if(lock == null){
			errorMsg += "Lock state can't be empty.\n";
			result = false;
		}


		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to lock / unlock a Publication, it first tests the values and then submits them.
	 *
	 * @param lock true = lock / false = unlock
	 * @param publication publication to update
	 */
	public void lockUnlockPublication(final boolean lock, final Publication publication) {
		ArrayList<Publication> publications = new ArrayList<Publication>();
		publications.add(publication);
		lockUnlockPublications(lock, publications);
	}

	/**
	 * Attempts to lock / unlock a Publication, it first tests the values and then submits them.
	 *
	 * @param lock true = lock / false = unlock
	 * @param publications list of pubs to update
	 */
	public void lockUnlockPublications(final boolean lock, final ArrayList<Publication> publications) {

		this.publications = publications;
		this.lock = lock;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Lock/Unlock publication failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Publications locked / unlocked.");
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

		JSONArray array = new JSONArray();

		for (int i=0; i<publications.size(); i++) {

			// publication
			JSONObject oldPub = new JSONObject(publications.get(i));
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

			// set in list
			array.set(i, newPub);

		}

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("publications", array);
		jsonQuery.put("lock", lock == true ? new JSONNumber(1) : new JSONNumber(0));

		return jsonQuery;

	}

}
