package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Changing user's password
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class ChangePassword {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "usersManager/changePassword";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// local variables for entity to send
	private User user = null;
	private String loginNamespace = "";
	private String oldPassword = "";
	private String newPassword = "";
	private boolean checkOldPassword = true;

	/**
	 * Creates a new request
	 */
	public ChangePassword() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 */
	public ChangePassword(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events custom events
	 * @param checkOldPassword whether to check old password
	 */
	public ChangePassword(final JsonCallbackEvents events, boolean checkOldPassword) {
		this.events = events;
		this.checkOldPassword = checkOldPassword;
	}


	/**
	 * Changes password for the user
	 *
	 * @param user User to change password for
	 * @param loginNamespace Login namespace
	 * @param oldPassword Old password for comparing
	 * @param newPassword New password
	 */
	public void changePassword(User user, String loginNamespace, String oldPassword, String newPassword)
	{

		this.user = user;
		this.loginNamespace = loginNamespace;
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Changing password failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Password changed successfully.");
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

		if(user == null){
			errorMsg += "User null.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while changing password.", new HTML(errorMsg), true);
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
		int checkPasswordNum = (checkOldPassword) ? 1 : 0;

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("user", new JSONNumber(user.getId()));
		jsonQuery.put("loginNamespace", new JSONString(loginNamespace));
		jsonQuery.put("oldPassword", new JSONString(oldPassword));
		jsonQuery.put("newPassword", new JSONString(newPassword));
		jsonQuery.put("checkOldPassword", new JSONNumber(checkPasswordNum));
		return jsonQuery;
	}

}
