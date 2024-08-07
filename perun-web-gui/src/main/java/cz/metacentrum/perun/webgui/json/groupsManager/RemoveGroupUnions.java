package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import java.util.List;

/**
 * @author Michal Krajcovic <mkrajcovic@mail.muni.cz>
 */
public class RemoveGroupUnions {

  // Json URL
  static private final String JSON_URL = "groupsManager/removeGroupUnion";
  static private final String JSON_URL2 = "groupsManager/removeGroupUnions";
  // Session
  private PerunWebSession session = PerunWebSession.getInstance();
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();

  public RemoveGroupUnions(JsonCallbackEvents events) {
    this.events = events;
  }

  public <T extends Group> void deleteGroupUnions(final Group result, final List<T> operands) {

    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("resultGroup", new JSONNumber(result.getId()));

    JSONArray operandGroups = new JSONArray();

    for (int index = 0; index < operands.size(); index++) {
      operandGroups.set(index, new JSONNumber(operands.get(index).getId()));
    }

    jsonQuery.put("operandGroups", operandGroups);

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Deleting group unions with result group " + result.getId()
                + " failed.");
        events.onError(error);
      }

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Group unions with result group " + result.getId()
                + " successfully deleted!");
        events.onFinished(jso);
      }

      public void onLoadingStart() {
        events.onLoadingStart();
      }
    };

    // sending data
    JsonPostClient jspc = new JsonPostClient(newEvents);
    jspc.sendData(JSON_URL2, jsonQuery);

  }

  public <T extends Group> void deleteGroupUnions(final List<T> results, final Group operand) {
    for (Group result : results) {
      deleteGroupUnion(result, operand);
    }
  }

  private void deleteGroupUnion(final Group result, final Group operand) {
    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("resultGroup", new JSONNumber(result.getId()));
    jsonQuery.put("operandGroup", new JSONNumber(operand.getId()));

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Deleting group union with result group " + result.getId()
                + " and operand group " + operand.getId() + " failed.");
        events.onError(error);
      }

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Group union with result group " + result.getId()
                + " and operand group " + operand.getId() + " successfully deleted!");
        events.onFinished(jso);
      }

      public void onLoadingStart() {
        events.onLoadingStart();
      }
    };

    // sending data
    JsonPostClient jspc = new JsonPostClient(newEvents);
    jspc.sendData(JSON_URL, jsonQuery);
  }
}
