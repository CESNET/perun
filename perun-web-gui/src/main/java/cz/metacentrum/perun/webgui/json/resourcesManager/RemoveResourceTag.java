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
 * Ajax query for removing resource tag from Resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveResourceTag {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/removeResourceTagFromResource";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private ResourceTag resourceTag = null;
	private int resourceId = 0;

	/**
	 * Creates a new request
	 *
	 * @param resourceId ID of resource to remove tag for
	 */
	public RemoveResourceTag(int resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param resourceId ID of resource to remove tag for
	 * @param events Custom events
	 */
	public RemoveResourceTag(int resourceId, JsonCallbackEvents events) {
		this.resourceId = resourceId;
		this.events = events;
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
	 * Attempts to remove resource tag from Resource, it first tests the values and then submits them
	 *
	 * @param tag ResourceTag to remove
	 */
	public void removeResourceTag(ResourceTag tag) {

		this.resourceTag = tag;

		// test arguments
		if(!this.testRemoving()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Removing resource tag from resource failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Resource tag removed.");
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
