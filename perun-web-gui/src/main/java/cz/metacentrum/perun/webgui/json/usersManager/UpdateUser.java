package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

/**
 * Update user in DB
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateUser {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "usersManager/updateUser";

	/**
	 * New instance of CreateGroup
	 */
	public UpdateUser() {}

	/**
	 * New instance of CreateGroup
	 *
	 * @param events
	 */
	public UpdateUser(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Updates user details
	 * @param user User with updated details
	 */
	public void updateUser(User user) {

		if (user == null) {
			UiElements.generateAlert("Parameter error", "User to update can't be null");
			return;
		}

		// OBJECT
		JSONObject oldUser = new JSONObject(user);
		// RECONSTRUCT OBJECT
		JSONObject newUser = new JSONObject();
		newUser.put("id", oldUser.get("id"));
		newUser.put("firstName", oldUser.get("firstName"));
		newUser.put("middleName", oldUser.get("middleName"));
		newUser.put("lastName", oldUser.get("lastName"));
		newUser.put("titleBefore", oldUser.get("titleBefore"));
		newUser.put("titleAfter", oldUser.get("titleAfter"));
		newUser.put("serviceUser", oldUser.get("serviceUser"));
		newUser.put("sponsoredUser", oldUser.get("sponsoredUser"));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("user", newUser);

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating user failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				User u = jso.cast();
				session.getUiElements().setLogSuccessText("User "+ u.getFullNameWithTitles() +" successfully updated!");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, jsonQuery);

	}

}
