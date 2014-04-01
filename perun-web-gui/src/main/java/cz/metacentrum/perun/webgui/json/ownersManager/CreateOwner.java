package cz.metacentrum.perun.webgui.json.ownersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query to create owner in Perun DB
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class CreateOwner {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "ownersManager/createOwner";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// params
	private String ownerName = "";
	private String ownerContact = "";
	private String ownerType = "";

	/**
	 * Creates a new request
	 */
	public CreateOwner() {
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public CreateOwner(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

		if(ownerName.length() == 0){
			errorMsg += "You must enter owners <strong>Name</strong>.</ br>";
			result = false;
		}
		if(ownerContact.length() == 0){
			errorMsg += "You must enter owners <strong>Contact</strong>.</ br>";
			result = false;
		}
		if(ownerType.length() == 0){
			errorMsg += "You must select owners <strong>Type</strong>.</ br>";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating Owner", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to create owner, it first tests the values and then submits them.
	 *
	 * @param ownerName name of future owner
	 * @param ownerContact owners contact (mail, phone, vo etc.)
	 * @param ownerType type of owner (administrative, technical)
	 */
	public void createOwner(final String ownerName, final String ownerContact, final String ownerType)
	{

		this.ownerName = ownerName;
		this.ownerContact = ownerContact;
		this.ownerType = ownerType;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating owner: " + ownerName + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Owner " + ownerName + " created.");
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
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{

		JSONObject owner = new JSONObject();
		owner.put("name", new JSONString(ownerName));
		owner.put("contact", new JSONString(ownerContact));
		owner.put("type", new JSONString(ownerType));

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("owner",owner);
		return jsonQuery;
	}

}
