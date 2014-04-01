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
import cz.metacentrum.perun.webgui.model.RichResource;

import java.util.ArrayList;

/**
 * Ajax query which Assigns group to resources
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveGroupFromResources {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/removeGroupFromResources";
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private Group group;
	private ArrayList<RichResource> resources;

	/**
	 * Creates a new request
	 */
	public RemoveGroupFromResources() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public RemoveGroupFromResources(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to remove group from resources
	 *
	 * @param group group which should be removed
	 * @param resources resources where group should be removed
	 */
	public void removeGroupFromResources(final Group group, final ArrayList<RichResource> resources) {

		this.group = group;
		this.resources = resources;

		// test arguments
		if(!this.testAssigning()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Assigning group: " + group.getShortName() + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Group: "+group.getShortName()+" was successfully assigned to resources.");
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
	private boolean testAssigning() {

		boolean result = true;
		String errorMsg = "";

		if(resources == null || resources.isEmpty()){
			errorMsg += "Wrong parameter <strong>Resources</strong>.<br />";
			result = false;
		}

		if(group == null){
			errorMsg += "Wrong parameter <strong>group</strong>.";
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
		for (int i=0; i< resources.size(); i++) {
			array.set(i, new JSONNumber(resources.get(i).getId()));
		}
		jsonQuery.put("resources", array);
		jsonQuery.put("group", new JSONNumber(group.getId()));

		return jsonQuery;
	}

}
