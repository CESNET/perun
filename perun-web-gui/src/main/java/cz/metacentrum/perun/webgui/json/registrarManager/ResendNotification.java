package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
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
public class ResendNotification {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/sendMessage";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private int appId = 0;
	private String mailType = "";
	private String reason = "";

	/**
	 * Creates a new request
	 *
	 * @param appId
	 */
	public ResendNotification(int appId) {
		this.appId = appId;
	}

	/**
	 * Creates a new request
	 *
	 * @param appId
	 * @param events
	 */
	public ResendNotification(int appId, JsonCallbackEvents events) {
		this.appId = appId;
		this.events = events;
	}

	/**
	 * Send request to RPC to sendMessage() == re-send notification
	 *
	 * @param mailType type of mail notification to send.
	 * @param reason Optional reason for "APP_REJECTED_USER" notification.
	 */
	public void resendNotification(String mailType, String reason) {

		this.mailType = mailType;
		this.reason = reason;

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Notification not re-sent.");
				events.onError(error);
			}

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Notification was sent.");
				events.onFinished(jso);
			}

			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject() {

		// query
		JSONObject query = new JSONObject();
		query.put("appId", new JSONNumber(appId));
		query.put("mailType", new JSONString(mailType));
		if (mailType.equals("APP_REJECTED_USER")) {
			query.put("reason", new JSONString(reason));
		}
		return query;

	}

}