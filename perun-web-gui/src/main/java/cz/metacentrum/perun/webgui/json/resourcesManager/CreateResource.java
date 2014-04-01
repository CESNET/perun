package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query for creating a resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class CreateResource {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// URL to call
	final String JSON_URL = "resourcesManager/createResource";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// ids
	private String resourceName = "";
	private String resourceDescription = "";
	private int facilityId = 0;
	private int voId = 0;

	/**
	 * Creates a new request
	 */
	public CreateResource() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public CreateResource(JsonCallbackEvents events) {
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

		if(resourceName.length() == 0){
			errorMsg += "You must enter resource 'Name'.\n";
			result = false;
		}
		if(resourceDescription.length() == 0){
			errorMsg += "You must enter resource 'Description'.\n";
			result = false;
		}

		if(facilityId == 0){
			errorMsg += "Wrong parameter Facility ID'.\n";
			result = false;
		}

		if(voId == 0){
			errorMsg += "Wrong parameter VO ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create resource, it first tests the values and then submits them.
	 *
	 * @param resourceName name of future resource
	 * @param resourceDescription eg. resource purpose
	 * @param facilityId id of Facility, to which resource is connected
	 */
	public void createResource(final String resourceName, final String resourceDescription, final int facilityId, final int voId)
	{

		this.resourceName = resourceName;
		this.resourceDescription = resourceDescription;
		this.facilityId = facilityId;
		this.voId = voId;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating resource " + resourceName + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Resource " + resourceName + " created.");
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
	private JSONObject prepareJSONObject() {

		JSONObject resource = new JSONObject();
		resource.put("id", new JSONNumber(0));
		resource.put("name", new JSONString(resourceName));
		resource.put("description", new JSONString(resourceDescription));

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("resource",resource);
		jsonQuery.put("facility", new JSONNumber(facilityId));
		jsonQuery.put("vo", new JSONNumber(voId));

		return jsonQuery;

	}

}
