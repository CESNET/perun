package cz.metacentrum.perun.webgui.json.tasksManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to delete TaskResults
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class DeleteTaskResults {

  // Json URL
  static private final String JSON_URL = "tasksManager/deleteTaskResultById";
  static private final String JSON_URL_MULTIPLE = "tasksManager/deleteTaskResults";
  // Session
  private PerunWebSession session = PerunWebSession.getInstance();
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();

  /**
   * New instance of DeleteTask
   */
  public DeleteTaskResults() {
  }

  /**
   * New instance of DeleteTask with external events
   *
   * @param events external events
   */
  public DeleteTaskResults(JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Deletes Task from DB
   *
   * @param taskId id of Task to be deleted
   */
  public void deleteTaskResults(final int taskId, final int destinationId) {

    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("taskId", new JSONNumber(taskId));
    jsonQuery.put("destinationId", new JSONNumber(destinationId));

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Deleting of TaskResults for Task: " + taskId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("TaskResults of Task: " + taskId + " deleted successfully.");
        events.onFinished(jso);
      }

      ;

      public void onLoadingStart() {
        events.onLoadingStart();
      }

      ;
    };

    // sending data
    JsonPostClient jspc = new JsonPostClient(newEvents);
    jspc.sendData(JSON_URL_MULTIPLE, jsonQuery);


  }

  /**
   * Deletes TaskResult from DB
   *
   * @param taskResultId ID of TaskResult to be deleted
   */
  public void deleteTaskResult(final int taskResultId) {

    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("taskResultId", new JSONNumber(taskResultId));

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Deleting of TaskResult: " + taskResultId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("TaskResult: " + taskResultId + " deleted successfully.");
        events.onFinished(jso);
      }

      ;

      public void onLoadingStart() {
        events.onLoadingStart();
      }

      ;
    };

    // sending data
    JsonPostClient jspc = new JsonPostClient(newEvents);
    jspc.sendData(JSON_URL, jsonQuery);

  }

  public void setEvents(JsonCallbackEvents events) {
    this.events = events;
  }

}
