package cz.metacentrum.perun.webgui.json.generalServiceManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;

/**
 * Ajax query for creation of exec service in DB
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class InsertExecService {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "generalServiceManager/insertExecService";

	/**
	 * New instance of InsertExecService
	 */
	public InsertExecService() {}

	/**
	 * New instance of InsertExecService with external events
	 *
	 * @param events external events
	 */
	public InsertExecService(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Creates exec service in DB and associate it with service
	 * 
	 * @param service service to associate with
	 * @param owner owner of exec service
	 * @param type type of exec service (SEND, GENERATE)
	 * @param enabled true if exec service is enabled
	 * @param delayNum default delay
	 * @param scriptPath path to propagation scripts
	 */
	public void addExecService(Service service, Owner owner, String type, Boolean enabled, int delayNum, String scriptPath) {

		// TODO - test input
		// test arguments
		// if(!this.testArguments()){
		//	return;
		//}

		// reconstruct service
		JSONObject serv = new JSONObject();
		serv.put("id", new JSONNumber(service.getId()));
		serv.put("name", new JSONString(service.getName()));

		// reconstruct exec service
		JSONObject exec = new JSONObject();
		exec.put("execServiceType", new JSONString(type));
		exec.put("enabled", JSONBoolean.getInstance(enabled));
		exec.put("defaultDelay", new JSONNumber(delayNum));
		exec.put("service", serv);  // insert service into exec service
		exec.put("script", new JSONString(scriptPath));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("execService", exec);
		jsonQuery.put("owner", new JSONNumber(owner.getId()));

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Adding of exec service failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Exec service successfully added!");
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

}