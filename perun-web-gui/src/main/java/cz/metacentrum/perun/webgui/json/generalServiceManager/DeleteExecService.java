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
 * Ajax query to delete exec service
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class DeleteExecService {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "generalServiceManager/deleteExecService";
	// ID
	private int execServiceId = 0;

	/**
	 * New instance of DeleteExecService
	 */
	public DeleteExecService() {}

	/**
	 * New instance of DeleteExecService with external events
	 *
	 * @param events external events
	 */
	public DeleteExecService(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Deletes Exec Service from DB
	 * 
	 * @param serviceId id of exec service to be deleted
	 */
	public void deleteExecService(final int serviceId) {

		this.execServiceId = serviceId;

		// test arguments
		if(!this.testArguments()){
			return;
		}

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("execService", new JSONNumber(serviceId));

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting of ExecService: "+execServiceId+" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("ExecService: "+execServiceId+" deleted successfully.");
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
	 * @return true if correct / false otherwise
	 */
	private boolean testArguments()
	{
		boolean result = true;
		String errorMsg = "";

		if(execServiceId == 0){
			errorMsg += "Wrong paramter 'exec service ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

}