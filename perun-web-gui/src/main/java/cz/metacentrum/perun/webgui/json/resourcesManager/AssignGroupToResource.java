package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which Assigns group to resource
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class AssignGroupToResource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/assignGroupToResource";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private int resourceId = 0;
	private int groupId = 0;

	/**
	 * Creates a new request
	 */
	public AssignGroupToResource() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AssignGroupToResource(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to assign group to resource
	 * 
	 * @param groupId ID of group which should be assigned
	 * @param resourceId ID of resource where should be assigned
	 * 
	 */
	public void assignGroup(final int groupId, final int resourceId)
	{

		this.resourceId = resourceId;
		this.groupId = groupId;

		// test arguments
		if(!this.testAssigning()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Assigning group: " + groupId + " to resource: " + resourceId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group: "+ groupId +" sucessfully assigned to resource: "+ resourceId);
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
	private boolean testAssigning()
	{
		boolean result = true;
		String errorMsg = "";

		if(groupId == 0){
			errorMsg += "Wrong Group parametr.\n";
			result = false;
		}

		if(resourceId == 0){
			errorMsg += "Wrong Resource parametr.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
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
		jsonQuery.put("group", new JSONNumber(groupId));    
		jsonQuery.put("resource", new JSONNumber(resourceId));
		return jsonQuery;
	}

}