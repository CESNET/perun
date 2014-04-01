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
 * Ajax query to create dependency between two exec services
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateDependency {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "generalServiceManager/createDependency";
	// values
	private int execService = 0;
	private int dependsOn = 0;

	/**
	 * New instance of CreateDependency
	 */
	public CreateDependency() {}

	/**
	 * New instance of CreateDependency with external events
	 *
	 * @param events events
	 */
	public CreateDependency(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Add dependency for exec service on another exec service
	 *
	 * @param execService add dependency for
	 * @param dependsOn add dependency on
	 */
	public void createDependancy(final int execService, final int dependsOn) {

		this.execService= execService;
		this.dependsOn = dependsOn;

		if(!this.testArguments()){
			return;
		}

		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("execService", new JSONNumber(execService));
		jsonQuery.put("dependantExecService", new JSONNumber(dependsOn));


		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding dependency failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Dependency successfully added!");
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
	 * Test input
	 *
	 * @return true on correct input, false otherwise
	 */
	private boolean testArguments(){

		boolean result = true;
		String errorMsg = "";

		if(execService == 0){
			errorMsg += "Wrong ExecService parameter.\n";
			result = false;
		}

		if(dependsOn == 0){
			errorMsg += "Wrong dependant ExecService parameter.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;

	}

}