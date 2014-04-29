package cz.metacentrum.perun.webgui.json.generalServiceManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.ExecService;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query for update of exec service in DB
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateExecService {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "generalServiceManager/updateExecService";

	/**
	 * New instance of InsertExecService
	 */
	public UpdateExecService() {}

	/**
	 * New instance of InsertExecService with external events
	 *
	 * @param events external events
	 */
	public UpdateExecService(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Creates exec service in DB and associate it with service
	 *
	 * @param execService ExecService to update to
	 */
	public void updateExecService(ExecService execService) {

		JSONObject execOrig = new JSONObject(execService);

		// reconstruct exec service
		JSONObject exec = new JSONObject();
		exec.put("id", execOrig.get("id"));
		exec.put("execServiceType", execOrig.get("execServiceType"));
		exec.put("enabled", execOrig.get("enabled"));
		exec.put("defaultDelay", execOrig.get("defaultDelay"));
		exec.put("defaultRecurrence", execOrig.get("defaultRecurrence"));
		exec.put("service", execOrig.get("service"));  // insert service into exec service
		exec.put("script", execOrig.get("script"));
		exec.put("beanName", execOrig.get("beanName"));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("execService", exec);

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Update of exec service failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Exec service successfully updated!");
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