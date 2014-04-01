package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which creates a new application form in VO or Group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateApplicationForm {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/createApplicationForm";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private PerunEntity entity;
	private int id;

	/**
	 * Creates a new request
	 *
	 * @param entity VO or GROUP
	 * @param id ID of entity
	 */
	public CreateApplicationForm(PerunEntity entity, int id) {
		this.entity = entity;
		this.id = id;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param entity VO or GROUP
	 * @param id ID of entity
	 * @param events Custom events
	 */
	public CreateApplicationForm(PerunEntity entity, int id, JsonCallbackEvents events) {
		this(entity, id);
		this.events = events;
	}

	/**
	 * Creating application form
	 */
	public void createApplicationForm()
	{

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating application form failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application form created.");
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

	private boolean testCreating() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject()
	{

		// query
		JSONObject query = new JSONObject();
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			query.put("vo", new JSONNumber(id));
		} else if (PerunEntity.GROUP.equals(entity)) {
			query.put("group", new JSONNumber(id));
		}
		return query;

	}

}
