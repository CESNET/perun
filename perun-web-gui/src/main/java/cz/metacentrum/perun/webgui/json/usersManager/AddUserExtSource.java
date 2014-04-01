package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which add new external identity to specified user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class AddUserExtSource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/addUserExtSource";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// local variables for entity to send
	private ExtSource extSource = null;
	private String login = "";
	private int userId = 0;
	private int loa = 0;

	/**
	 * Creates a new request
	 */
	public AddUserExtSource() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public AddUserExtSource(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to add new user ext source to specified user
	 *
	 * @param userId ID of user, which should get this external source
	 * @param login login of user in external source
	 * @param extSource object of external source from Perun
	 * @param loa level of assurance
	 */
	public void addUserExtSource(final int userId, final String login, final ExtSource extSource, int loa) {

		this.loa = loa;
		this.userId = userId;
		this.login = login;
		this.extSource = extSource;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding UES to user: " + userId + " failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("User Ext Source added to user: " + userId );
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
	private boolean testAdding()
	{
		boolean result = true;
		String errorMsg = "";

		if(userId == 0){
			errorMsg += "Wrong parameter <strong>User ID</strong>.\n";
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

		// create Json object from webgui extSource
		JSONObject oldExtSource = new JSONObject(extSource);

		// get only interested in items
		JSONValue uesName = oldExtSource.get("name");
		JSONValue uesId = oldExtSource.get("id");
		JSONValue uesType = oldExtSource.get("type");

		// create a new form of ext source
		JSONObject newExtSource = new JSONObject();
		newExtSource.put("name", uesName);
		newExtSource.put("id", uesId);
		newExtSource.put("type", uesType);

		// create new userExtSource
		JSONObject userExtSource = new JSONObject();
		userExtSource.put("id", new JSONNumber(0));
		userExtSource.put("extSource", newExtSource);
		userExtSource.put("login", new JSONString(login));
		userExtSource.put("loa", new JSONNumber(loa));

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("user", user);
		jsonQuery.put("userExtSource", userExtSource);
		return jsonQuery;

	}

}
