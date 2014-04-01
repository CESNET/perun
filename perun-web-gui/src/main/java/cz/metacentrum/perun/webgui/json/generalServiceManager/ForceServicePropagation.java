package cz.metacentrum.perun.webgui.json.generalServiceManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to Force service propagation on selected Facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ForceServicePropagation {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "generalServiceManager/forceServicePropagation";
	// IDS
	private int serviceId = 0;
	private int facilityId = 0;

	/**
	 * New instance of ForceServicePropagation
	 */
	public ForceServicePropagation() {}

	/**
	 * New instance of ForceServicePropagation with external events
	 *
	 * @param events ecternal events
	 */
	public ForceServicePropagation(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Starts Force service propagation for specified service and facility
	 *
	 * @param facilityId ID of facility to propagate service to
	 * @param serviceId ID of service to be propagated
	 */
	public void forcePropagation(final int facilityId,final int serviceId) {

		this.facilityId = facilityId;
		this.serviceId = serviceId;

		// test arguments
		if(!this.testArguments()){
			return;
		}

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", new JSONNumber(facilityId));
		jsonQuery.put("service", new JSONNumber(serviceId));

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Propagation initialization of service: "+serviceId+" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Propagation of service: "+serviceId+" initiated.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, jsonQuery);

	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return boolean true if correct / false otherwise
	 */
	private boolean testArguments()
	{
		boolean result = true;
		String errorMsg = "";

		if(facilityId == 0){
			errorMsg += "Wrong parameter 'facility ID'.\n";
			result = false;
		}
		if(serviceId == 0){
			errorMsg += "Wrong parameter 'service ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

}
