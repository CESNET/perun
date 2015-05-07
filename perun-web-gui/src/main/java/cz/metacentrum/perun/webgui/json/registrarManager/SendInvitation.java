package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Candidate;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

/**
 * Send invitation email to user or unknown person.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SendInvitation {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/sendInvitation";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private int voId = 0;
	private int groupId = 0;

	/**
	 * Creates a new request
	 *
	 * @param voId
	 * @param groupId
	 */
	public SendInvitation(int voId, int groupId) {
		this.voId = voId;
		this.groupId = groupId;
	}

	/**
	 * Creates a new request
	 *
	 * @param voId
	 * @param groupId
	 * @param events Custom events
	 */
	public SendInvitation(int voId, int groupId, JsonCallbackEvents events) {
		this.voId = voId;
		this.groupId = groupId;
		this.events = events;
	}

	/**
	 * Send request to invite user
	 */
	public void inviteUser(User user) {

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Inviting user failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User invited.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// query
		JSONObject query = new JSONObject();

		query.put("voId", new JSONNumber(voId));
		if (groupId != 0) {
			query.put("groupId", new JSONNumber(groupId));
		}
		query.put("userId", new JSONNumber(user.getId()));

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, query);

	}

	/**
	 * Send request to invite user
	 */
	public void inviteUser(Candidate candidate) {
		inviteUser(candidate.getEmail(), candidate.getDisplayName(), "");
	}

	/**
	 * Send request to invite user
	 */
	public void inviteUser(String email, String name, String language) {

		if (email == null || email.isEmpty())  {
			UiElements.generateAlert("Input error", "Email address to send invitation to is empty.");
			return;
		}
		if (!JsonUtils.isValidEmail(email)) {
			UiElements.generateAlert("Input error", "Email address format is not valid.");
			return;
		}
		/*
		if (name == null || name.isEmpty()) {
			UiElements.generateAlert("Input error", "Name of user to invite can't be empty.");
			return;
		}
		*/

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Inviting user failed.");
				events.onError(error);
			}

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User invited.");
				events.onFinished(jso);
			}

			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		// query
		JSONObject query = new JSONObject();

		query.put("voId", new JSONNumber(voId));
		if (groupId != 0) {
			query.put("groupId", new JSONNumber(groupId));
		}
		if (name != null && !name.isEmpty()) query.put("name", new JSONString(name));
		query.put("email", new JSONString(email));

		if (language != null && !language.isEmpty()) {
			query.put("language", new JSONString(language));
		} else {
			query.put("language", new JSONObject(null));
		}

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, query);

	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}