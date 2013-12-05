package cz.metacentrum.perun.webgui.json.facilitiesManager;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which copy owners from one facility to another
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: $
 */
public class CopyOwners {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// facility
    private int sourceFacility = 0;
	private int destinationFacility = 0;
	// URL to call
	final String JSON_URL = "facilitiesManager/copyOwners";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public CopyOwners() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public CopyOwners(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 * 
	 * @return true/false when process can/can't continue
	 */
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

        if(sourceFacility == 0){
            errorMsg += "Source facility can't be 0.\n";
            result = false;
        }

        if(destinationFacility == 0){
            errorMsg += "Destination facility can't be 0..\n";
            result = false;
        }

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to copy owners from one facility to another
	 * 
	 * @param sourceFacility ID of source facility to get owners from
	 * @param destinationFacility ID of destination facility to copy owners to
	 */
	public void copyFacilityOwners(int sourceFacility, int destinationFacility)
	{

        this.sourceFacility = sourceFacility;
		this.destinationFacility = destinationFacility;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Copying facility owners failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Facility owners copied.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		// create request
		JsonPostClient request = new JsonPostClient(newEvents);
		request.sendData(JSON_URL, jsonQuery);

	}

	/**
	 * Prepares a JSON object
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject() {

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("srcFacility", new JSONNumber(sourceFacility));
        jsonQuery.put("destFacility", new JSONNumber(destinationFacility));
		return jsonQuery;

	}

}