package cz.metacentrum.perun.webgui.json.propagationStatsReader;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to delete Task with it's task results
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteTask {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Json URL
	static private final String JSON_URL = "propagationStatsReader/deleteTask";
	// ID
	private int taskId = 0;

	/**
	 * New instance of DeleteTask
	 */
	public DeleteTask() {}

	/**
	 * New instance of DeleteTask with external events
	 *
	 * @param events external events
	 */
	public DeleteTask(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Deletes Task from DB
	 *
	 * @param taskId id of Task to be deleted
	 */
	public void deleteTask(final int taskId) {

		this.taskId = taskId;

		// test arguments
		if(!this.testArguments()){
			return;
		}

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("task", new JSONNumber(taskId));

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting of Task: "+taskId+" failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Task: "+taskId+" deleted successfully.");
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
	private boolean testArguments() {

		boolean result = true;
		String errorMsg = "";

		if(taskId == 0){
			errorMsg += "Wrong parameter 'Task ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}
