package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;

/**
 * Ajax query which connects specific user and user together
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddSpecificUserOwner {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/addSpecificUserOwner";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// local variables for entity to send
	private User user;
	private User specificUser;

	/**
	 * Creates a new request
	 */
	public AddSpecificUserOwner() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public AddSpecificUserOwner(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Create connection between user and service/specific user
	 *
	 * @param user
	 * @param specificUser
	 */
	public void addSpecificUser(final User user, final User specificUser) {

		this.user = user;
		this.specificUser = specificUser;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding "+specificUser.getFullName()+" to user: " + user.getFullName() + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service identity: "+specificUser.getFullName()+" added to user: " + user.getFullName());
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

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAdding() {

		boolean result = true;
		String errorMsg = "";

		if(user.isServiceUser()){
			errorMsg += "Can't connect two specific user identities.</br>";
			result = false;
		}
		if(!specificUser.isServiceUser() && !specificUser.isSponsoredUser()){
			errorMsg += "Can't connect two 'Person' like users.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Error while connecting identities", errorMsg);
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
		jsonQuery.put("user", new JSONNumber(user.getId()));
		jsonQuery.put("specificUser", new JSONNumber(specificUser.getId()));
		return jsonQuery;

	}

}
