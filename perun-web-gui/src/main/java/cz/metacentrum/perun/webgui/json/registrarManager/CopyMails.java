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
 * Request, which copy application mails from VO to another VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CopyMails {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/copyMails";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private int fromId;
	private int toId;
	private PerunEntity entityFrom;
	private PerunEntity entityTo;

	/**
	 * Creates a new request
	 *
	 * @param fromId
	 * @param toId
	 */
	public CopyMails(PerunEntity entityFrom, int fromId, PerunEntity entityTo, int toId) {
		this.entityFrom = entityFrom;
		this.entityTo = entityTo;
		this.fromId = fromId;
		this.toId = toId;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param fromId
	 * @param toId
	 * @param events Custom events
	 */
	public CopyMails(PerunEntity entityFrom, int fromId, PerunEntity entityTo, int toId, JsonCallbackEvents events) {
		this.entityFrom = entityFrom;
		this.fromId = fromId;
		this.entityTo = entityTo;
		this.toId = toId;
		this.events = events;
	}

	/**
	 * Send request to copy form
	 */
	public void copyMails() {

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Copying form failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Form successfully copied.");
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

		// query
		JSONObject query = new JSONObject();

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entityFrom)) {

			query.put("fromVo", new JSONNumber(fromId));

		} else if (PerunEntity.GROUP.equals(entityFrom)) {

			query.put("fromGroup", new JSONNumber(fromId));

		}

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entityTo)) {

			query.put("toVo", new JSONNumber(toId));

		} else if (PerunEntity.GROUP.equals(entityTo)) {

			query.put("toGroup", new JSONNumber(toId));

		}

		return query;

	}

}