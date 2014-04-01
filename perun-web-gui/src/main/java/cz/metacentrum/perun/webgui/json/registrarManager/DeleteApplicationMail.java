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
 * Request, which deletes application email definition
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteApplicationMail {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/deleteApplicationMail";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private int mailId;
	private int id;
	private PerunEntity entity;

	/**
	 * Creates a new request
	 *
	 * @param entity VO or GROUP
	 */
	public DeleteApplicationMail(PerunEntity entity) {
		this.entity = entity;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param entity VO or GROUP
	 * @param events Custom events
	 */
	public DeleteApplicationMail(PerunEntity entity, JsonCallbackEvents events) {
		this.events = events;
		this.entity = entity;
	}

	/**
	 * Deletes ApplicationMail definition for VO.
	 *
	 * @param mailId
	 * @param id
	 */
	public void deleteMail(int mailId, int id) {

		this.mailId = mailId;
		this.id = id;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deletion of email notification failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Email notification deleted.");
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
		request.put("id", new JSONNumber(mailId));
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			request.put("vo", new JSONNumber(id));
		} else if (PerunEntity.GROUP.equals(entity)) {
			request.put("group", new JSONNumber(id));
		}
		return request;

	}

}
