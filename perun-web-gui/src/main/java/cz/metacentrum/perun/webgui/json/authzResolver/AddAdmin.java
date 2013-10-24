package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which adds admin to VO or Group
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class AddAdmin {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// ids
	private int userId = 0;
	private int entityId = 0;
    private PerunEntity entity;
	// URL to call
	final String VO_JSON_URL = "vosManager/addAdmin";
	final String GROUP_JSON_URL = "groupsManager/addAdmin";
    final String FACILITY_JSON_URL = "facilitiesManager/addAdmin";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 *
     * @param entity VO/GROUP/FACILITY
     */
	public AddAdmin(PerunEntity entity) {
		this.entity = entity;
	}

	/**
	 * Creates a new request with custom events
	 *
     * @param entity VO/GROUP/FACILITY
     * @param events Custom events
     */
	public AddAdmin(PerunEntity entity, JsonCallbackEvents events) {
		this.entity = entity;
		this.events = events;
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

		if(entityId == 0){
			errorMsg += "Wrong parameter <strong>VO/Group ID</strong>'.\n";
			result = false;
		}

		if(userId == 0){
			errorMsg += "Wrong parameter <strong>User ID</strong>.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while adding admin", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Attempts to add a new admin to VO/Group, it first tests the values and then submits them.
	 * 
	 * @param id ID of Entity, where we want to add admin
	 * @param userId ID of Member to be admin
	 */
	public void addAdmin(final int id,final int userId)
	{

		this.userId = userId;
		this.entityId = id;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding admin (User ID: " + userId + ") failed.");
				events.onError(error); // custom events
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Admin (User ID: " + userId + ") successfully added to "+entity+": "+ entityId);
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            jspc.sendData(VO_JSON_URL, prepareJSONObject());
        } else if (entity.equals(PerunEntity.GROUP)) {
            jspc.sendData(GROUP_JSON_URL, prepareJSONObject());
        } else if (entity.equals(PerunEntity.FACILITY)) {
            jspc.sendData(FACILITY_JSON_URL, prepareJSONObject());
        }

	}

	/**
	 * Prepares a JSON object
	 * 
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		// Whole JSON query
		JSONObject jsonQuery = new JSONObject();

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            jsonQuery.put("vo", new JSONNumber(entityId));
        } else if (entity.equals(PerunEntity.GROUP)) {
            jsonQuery.put("group", new JSONNumber(entityId));
        } else if (entity.equals(PerunEntity.FACILITY)) {
            jsonQuery.put("facility", new JSONNumber(entityId));
        }
		jsonQuery.put("user", new JSONNumber(userId));

		return jsonQuery;
	}

}