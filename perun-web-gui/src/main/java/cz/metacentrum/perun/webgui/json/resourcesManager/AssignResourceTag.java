package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.ResourceTag;

/**
 * Ajax query for assigning resource tag to Resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AssignResourceTag {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/assignResourceTagToResource";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private ResourceTag resourceTag = null;
	private int resourceId = 0;

	/**
	 * Creates a new request
	 *
	 * @param resourceId ID of resource to assing tag for
	 */
	public AssignResourceTag(int resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param resourceId ID of resource to assign tag for
	 * @param events Custom events
	 */
	public AssignResourceTag(int resourceId, JsonCallbackEvents events) {
		this.resourceId = resourceId;
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

		if(resourceTag == null){
			errorMsg += "Wrong parameter 'resource tag'.</br>";
			result = false;
		}

		if(resourceId == 0){
			errorMsg += "Wrong parameter 'Resource ID'.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to assign resource tag to Resource, it first tests the values and then submits them
	 *
	 * @param tag ResourceTag to assign
	 */
	public void assignResourceTag(ResourceTag tag) {

		this.resourceTag = tag;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Assigning tag to resource failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Tag assigned to resource.");
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
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		JSONObject jsonQuery = new JSONObject();

		JSONObject oldTag = new JSONObject(resourceTag);

		JSONObject jsonTag = new JSONObject();
		jsonTag.put("id", oldTag.get("id"));
		jsonTag.put("tagName", oldTag.get("tagName"));
		jsonTag.put("voId", oldTag.get("voId"));

		jsonQuery.put("resourceTag", jsonTag);
		jsonQuery.put("resource", new JSONNumber(resourceId));

		return jsonQuery;
	}

}
