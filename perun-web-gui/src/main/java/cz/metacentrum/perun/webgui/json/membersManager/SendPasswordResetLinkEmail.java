package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;

/**
 * Ajax query to send password reset link to user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class SendPasswordResetLinkEmail {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "membersManager/sendPasswordResetLinkEmail";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// params
	private RichMember member;
	private String namespace = "";

	/**
	 * Creates a new request
	 */
	public SendPasswordResetLinkEmail() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public SendPasswordResetLinkEmail(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Send password reset gui link to member's email
	 *
	 * @param member
	 */
	public void sendEmail(final RichMember member, String namespace) {

		this.member = member;
		this.namespace = namespace;

		// test arguments
		if (!this.testAdding()) {
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents() {
			public void onError(PerunError error) {
				events.onError(error);
			}

			public void onFinished(JavaScriptObject jso) {
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
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding() {

		boolean result = true;
		String errorMsg = "";

		if (member == null) {
			errorMsg += "Wrong <strong>Member parameter</strong>. Can't be null.\n";
			result = false;
		}

		if (errorMsg.length() > 0) {
			UiElements.generateAlert("Wrong parameter", errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("member", new JSONNumber(member.getId()));
		jsonQuery.put("namespace", new JSONString(namespace));
		return jsonQuery;

	}

}
