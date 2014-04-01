package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Author;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Publication;

/**
 * Ajax query which creates a new Publication
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreatePublication {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// category
	private Publication publication;
	// URL to call
	final String JSON_URL = "cabinetManager/createPublication";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 *
	 */
	public CreatePublication() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public CreatePublication(JsonCallbackEvents events) {
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
			errorMsg += "External publication cannot be null.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a new Publication from EXTERNAL SOURCE, it first tests the values and then submits them.
	 *
	 * @param publication Publication
	 */
	public void createPublication(final Publication publication) {

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
				session.getUiElements().setLogErrorText("Creating publicaton ext.ID: " + publication.getExternalId() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Publication pub = jso.cast();
				session.getUiElements().setLogSuccessText("Publication with ID: " + pub.getId() + " created.");
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
	 * Attempts to create a new Publication from scratch.
	 *
	 * @param title
	 * @param category
	 * @param year
	 * @param ISBN
	 * @param doi
	 * @param main
	 */
	public void createPublication(final String title, final int category, final int year, final String ISBN, final String doi, final String main) {

		publication = new JSONObject().getJavaScriptObject().cast();
		publication.setTitle(title);
		publication.setCategoryId(category);
		publication.setYear(year);
		publication.setIsbn(ISBN);
		publication.setMain(main);
		publication.setCreatedBy(session.getPerunPrincipal().getActor());
		// set to zeros to be processed as internal publication
		publication.setPublicationSystemId(0);
		publication.setExternalId(0);
		publication.setId(0);
		publication.setRank(0);
		publication.setDoi(doi);
		publication.setLocked(false);
		publication.setCreatedByUid(session.getActiveUser().getId());

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating publication failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Publication pub = jso.cast();
				session.getUiElements().setLogSuccessText("Publication with ID: " + pub.getId() + " created.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		JSONObject jquery = new JSONObject(publication);
		jquery.put("createdDate", null);
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("publication", jquery);

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
		newPub.put("createdBy", new JSONString(session.getPerunPrincipal().getActor()));
		//newPub.put("createdDate", oldPub.get("createdDate"));
		newPub.put("publicationSystemId", oldPub.get("publicationSystemId"));
		newPub.put("title", oldPub.get("title"));
		// set rank 0
		newPub.put("rank", new JSONNumber(0));
		newPub.put("doi", oldPub.get("doi"));
		newPub.put("locked", oldPub.get("locked"));
		newPub.put("createdByUid", oldPub.get("createdByUid"));

		// dig-in authors
		JsArray<Author> authors = publication.getAuthors();
		JSONArray jsonAuthors = new JSONArray();
		for (int i=0; i<authors.length(); i++){
			JSONObject oldAuthor = new JSONObject(authors.get(i));
			JSONObject jsonAuthor = new JSONObject();
			jsonAuthor.put("firstName", oldAuthor.get("firstName"));
			jsonAuthor.put("lastName", oldAuthor.get("lastName"));
			jsonAuthor.put("namespace", oldAuthor.get("namespace"));
			jsonAuthor.put("namespaceLogin", oldAuthor.get("namespaceLogin"));
			jsonAuthor.put("userId", oldAuthor.get("userId"));
			jsonAuthors.set(i, jsonAuthor);
		}
		newPub.put("authors", jsonAuthors);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("publication", newPub);
		return jsonQuery;

	}

}
