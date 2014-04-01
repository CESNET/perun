package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.ApplicationMail;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which adds application email
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AddApplicationMail {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/addApplicationMail";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private ApplicationMail appMail;
	private int id;
	private PerunEntity entity;

	/**
	 * Creates a new request
	 *
	 * @param entity VO or Group
	 */
	public AddApplicationMail(PerunEntity entity) {
		this.entity = entity;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param entity VO or Group
	 * @param events Custom events
	 */
	public AddApplicationMail(PerunEntity entity, JsonCallbackEvents events) {
		this.events = events;
		this.entity = entity;
	}

	/**
	 * Adds new ApplicationMail
	 *
	 * @param appMail
	 * @param id
	 */
	public void addMail(ApplicationMail appMail, int id) {

		this.appMail = appMail;
		this.id = id;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding email failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Email added.");
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
	private JSONObject prepareJSONObject() {

		JSONObject request = new JSONObject();
		request.put("mail", new JSONObject(appMail));
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			request.put("vo", new JSONNumber(id));
		} else if (PerunEntity.GROUP.equals(entity)) {
			request.put("group", new JSONNumber(id));
		}
		return request;

	}

}
