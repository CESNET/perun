package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which adds required attribute to service
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class AddRequiredAttribute {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service id
	private int serviceId = 0;
	// attribute id
	private int attributeId = 0;
	// URL to call
	final String JSON_URL = "servicesManager/addRequiredAttribute";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public AddRequiredAttribute() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 * @param events custom events
	 */
	public AddRequiredAttribute(final JsonCallbackEvents events) {
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

		if(serviceId == 0){
			errorMsg += "Wrong SERVICE ID parametr.\n";
			result = false;
		}

		if(attributeId == 0){
			errorMsg += "Wrong ATTRIBUTE ID parametr.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to add required attribute to specified service
	 *
	 * @param serviceId ID of service to get new required attribute
	 * @param attributeId ID of attribute def. which will be added as required
	 */
	public void addRequiredAttribute(final int serviceId,final int attributeId)
	{
		this.serviceId = serviceId;
		this.attributeId = attributeId;

		// test arguments
		if(!this.testAdding()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding required attribute ID: " + attributeId + " to service ID: "+ serviceId +" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Attribute ID: "+ attributeId + " set as required to service ID: " + serviceId);;
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
		// get values
		JSONNumber service = new JSONNumber(serviceId);
		JSONNumber attribute = new JSONNumber(attributeId);

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("service", service);
		jsonQuery.put("attribute", attribute);
		return jsonQuery;
	}

}
