package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query for creating resource tag in VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateResourceTag {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/createResourceTag";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private String tagName;
	private int voId;

	/**
	 * Creates a new request
	 */
	public CreateResourceTag() {
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public CreateResourceTag(JsonCallbackEvents events) {
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

		if(tagName == null || tagName.isEmpty()){
			errorMsg += "Wrong parameter 'resource tag name'.</br>";
			result = false;
		}

		if(voId == 0){
			errorMsg += "Wrong parameter 'VO ID'.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to delete resource tag, it first tests the values and then submits them
	 *
	 * @param tagName Resource tag name
	 * @param voId ID of VO to create resource tag for
	 */
	public void createResourceTag(final String tagName, final int voId) {

		this.tagName = tagName;
		this.voId = voId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating resource tag failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Resource tag created.");
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
		JSONObject jsonTag = new JSONObject();
		jsonTag.put("id", null);
		jsonTag.put("tagName", new JSONString(tagName));
		jsonTag.put("voId", new JSONNumber(voId));

		jsonQuery.put("resourceTag", jsonTag);
		jsonQuery.put("vo", new JSONNumber(voId));

		return jsonQuery;
	}

}
