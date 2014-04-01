package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.ResourceTag;

/**
 * Ajax query for updating resource tag of VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateResourceTag {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/updateResourceTag";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private ResourceTag tag;

	/**
	 * Creates a new request
	 */
	public UpdateResourceTag() {
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public UpdateResourceTag(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testDeleting()
	{
		boolean result = true;
		String errorMsg = "";

		if(tag == null){
			errorMsg += "Wrong parameter 'resource tag'.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to update resource tag, it first tests the values and then submits them
	 *
	 * @param tag Resource tag to be updated
	 */
	public void updateResourceTag(final ResourceTag tag) {

		this.tag = tag;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating resource tag failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Resource tag updated.");
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
		JSONObject jsonTag = new JSONObject(tag);

		JSONObject newJsonTag = new JSONObject();
		newJsonTag.put("id", jsonTag.get("id"));
		newJsonTag.put("tagName", jsonTag.get("tagName"));
		newJsonTag.put("voId", jsonTag.get("voId"));

		jsonQuery.put("resourceTag", newJsonTag);

		return jsonQuery;
	}

}
