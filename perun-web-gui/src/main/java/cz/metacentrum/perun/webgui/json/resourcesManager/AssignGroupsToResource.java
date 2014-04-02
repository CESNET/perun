package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;

import java.util.ArrayList;

/**
 * Ajax query which Assigns group to resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignGroupsToResource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/assignGroupsToResource";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private Resource resource;
	private ArrayList<Group> groups;

	/**
	 * Creates a new request
	 */
	public AssignGroupsToResource() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public AssignGroupsToResource(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to assign groups to resource
	 *
	 * @param groups groups which should be assigned
	 * @param resource resource where should be assigned
	 */
	public void assignGroupsToResource(final ArrayList<Group> groups, final Resource resource) {

		this.resource = resource;
		this.groups = groups;

		// test arguments
		if(!this.testAssigning()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Assigning group(s) to resource: " + resource.getName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group(s) successfully assigned to resource: "+ resource.getName());
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
        // to allow own error handling for attributes errors.
        jspc.setHidden(true);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testAssigning() {

		boolean result = true;
		String errorMsg = "";

		if(groups == null || groups.isEmpty()){
			errorMsg += "Wrong parameter <strong>Groups</strong>.<br />";
			result = false;
		}

		if(resource == null){
			errorMsg += "Wrong parameter <strong>Resource</strong>.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Prepares a JSON object
	 *
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		JSONObject jsonQuery = new JSONObject();

		JSONArray array = new JSONArray();
		for (int i=0; i<groups.size(); i++) {
			array.set(i, new JSONNumber(groups.get(i).getId()));
		}
		jsonQuery.put("groups", array);
		jsonQuery.put("resource", new JSONNumber(resource.getId()));

		return jsonQuery;
	}

}
