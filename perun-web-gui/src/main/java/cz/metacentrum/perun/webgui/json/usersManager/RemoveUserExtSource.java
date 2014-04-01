package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which removes external identity from specified user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveUserExtSource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/removeUserExtSource";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// local variables for entity to send
	private int uesId = 0;
	private int userId = 0;

	/**
	 * Creates a new request
	 */
	public RemoveUserExtSource() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public RemoveUserExtSource(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to remove user ext source from specified user
	 *
	 * @param userId ID of user, which should have his ext source reomved
	 * @param uesId ID of user ext source, which should be removed
	 */
	public void removeUserExtSource(final int userId,final int uesId)
	{

		this.userId = userId;
		this.uesId = uesId;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing UES: "+ uesId +" from user: " + userId + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User Ext Source removed from user: " + userId );
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
	private boolean testRemoving()
	{
		boolean result = true;
		String errorMsg = "";

		if(userId == 0){
			errorMsg += "Wrong parameter <strong>User ID</strong>. ";
			result = false;
		}

		if(uesId == 0){
			errorMsg += "Wrong parameter <strong>User Ext Source ID</strong>. ";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while adding user external source", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		JSONNumber user = new JSONNumber(userId);
		JSONNumber ues = new JSONNumber(uesId);

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("user", user);
		jsonQuery.put("userExtSource", ues);
		return jsonQuery;
	}

}
