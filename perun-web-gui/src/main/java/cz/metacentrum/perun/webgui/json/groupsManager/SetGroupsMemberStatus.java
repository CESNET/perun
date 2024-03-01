package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonStatusSetCallback;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax request to set status for member in a group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SetGroupsMemberStatus implements JsonStatusSetCallback {

  final String JSON_URL = "groupsManager/setGroupsMemberStatus";
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  private int groupId = 0;
  private int memberId = 0;
  private String status = "";
  private JsonCallbackEvents events = new JsonCallbackEvents();

  /**
   * Creates a new request
   *
   * @param groupId  ID of group to set new status in
   * @param memberId ID of member to set new status
   */
  public SetGroupsMemberStatus(int groupId, int memberId) {
    this.groupId = groupId;
    this.memberId = memberId;
  }

  /**
   * Creates a new request with custom events
   *
   * @param memberId ID of member to set new status
   * @param events   Custom events
   */
  public SetGroupsMemberStatus(int groupId, int memberId, JsonCallbackEvents events) {
    this(groupId, memberId);
    this.events = events;
  }

  /**
   * Tests the values, if the process can continue
   *
   * @return true/false
   */
  private boolean testSetting() {
    boolean result = true;
    String errorMsg = "";

    if (status.length() == 0) {
      errorMsg += "Wrong parameter 'Status'.\n";
      result = false;
    }

    if (memberId == 0) {
      errorMsg += "Wrong parameter 'Member ID'.\n";
      result = false;
    }

    if (groupId == 0) {
      errorMsg += "Wrong parameter 'Group ID'.\n";
      result = false;
    }

    if (errorMsg.length() > 0) {
      Window.alert(errorMsg);
    }

    return result;
  }

  /**
   * Attempts to set new status for selected member
   *
   * @param status new status (VALID,EXPIRED)
   */
  public void setStatus(String status) {
    this.status = status;

    // test arguments
    if (!this.testSetting()) {
      return;
    }

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Setting new status for member: " + memberId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("New status for member: " + memberId + " successfully set.");
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
    jspc.sendData(JSON_URL, prepareJSONObject());
  }

  /**
   * Prepares a JSON object.
   *
   * @return JSONObject - the whole query
   */
  private JSONObject prepareJSONObject() {
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("member", new JSONNumber(memberId));
    jsonQuery.put("group", new JSONNumber(groupId));
    jsonQuery.put("status", new JSONString(status));
    return jsonQuery;
  }

  /**
   * Sets the json events
   */
  public void setEvents(JsonCallbackEvents events) {
    this.events = events;
  }
}
