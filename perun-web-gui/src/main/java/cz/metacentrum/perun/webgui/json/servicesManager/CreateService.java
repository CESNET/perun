package cz.metacentrum.perun.webgui.json.servicesManager;

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
 * Ajax query which creates a new service.
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz> 
 * @version $Id$
 */
public class CreateService {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service name
	private String serviceName = "";
	// service owner
	private int ownerId = 0;
	// URL to call
	final String JSON_URL = "servicesManager/createService";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public CreateService() {
	}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events external events
	 */
	public CreateService(final JsonCallbackEvents events) {
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

		if(serviceName.length() == 0){
			errorMsg += "You must fill in the parameter 'Name'.\n";
			result = false;
		}

		if(ownerId == 0){
			errorMsg += "You must pick the parameter 'Owner'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to create a new Service, it first tests the values and then submits them.
	 * 
	 * @param name service Name
	 * @param ownerId ID of owner
	 */
	public void createService(final String name, int ownerId)
	{
		this.serviceName = name;
		this.ownerId = ownerId;

		// test arguments
		if(!this.testCreating()){
			return;
		}
	
		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating service " + serviceName + " failed.");
				events.onError(error); // custom events
			};
			
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Service " + serviceName + " created.");
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
	private JSONObject prepareJSONObject()
	{
		// service
		JSONObject service = new JSONObject();
		service.put("name", new JSONString(serviceName));      // service name as object

		// owner
		JSONNumber owner = new JSONNumber(ownerId); // object json number

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();      
		jsonQuery.put("service", service);           // service object
		jsonQuery.put("owner", owner);               // owner ID
		return jsonQuery;
	}

}