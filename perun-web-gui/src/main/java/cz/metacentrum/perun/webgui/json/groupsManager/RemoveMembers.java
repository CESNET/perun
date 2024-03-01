package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import java.util.ArrayList;
import java.util.List;

/**
 * Ajax query to remove members from group.
 * Only direct members are removed, others are silently skipped on server side
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveMembers {

  // URL to call
  final String JSON_URL = "groupsManager/removeMembers";
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  // external events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  // ids
  private List<Integer> membersIds;
  private int groupId = 0;
  private List<RichMember> members = null;
  private Group group = null;

  /**
   * Creates a new request
   */
  public RemoveMembers() {
  }

  /**
   * Creates a new request with custom events passed from tab or page
   *
   * @param events JsonCallbackaEvents
   */
  public RemoveMembers(final JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Attempts to remove member from group
   *
   * @param groupId    id of group
   * @param membersIds IDs of members to be removed from group
   */
  public void removeMembersFromGroup(final int groupId, final List<Integer> membersIds) {

    this.groupId = groupId;
    this.membersIds = membersIds;

    // test arguments
    if (!this.testRemoving()) {
      return;
    }

    // prepare json object
    JSONObject jsonQuery = prepareJSONObject();

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements()
            .setLogErrorText("Removing " + membersIds.size() + " members from group: " + groupId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText(membersIds.size() + " members removed from group: " + groupId);
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

  /**
   * Attempts to remove member from group
   *
   * @param group   group
   * @param members member to be removed from group
   */
  public void removeMembersFromGroup(final Group group, final List<RichMember> members) {

    this.group = group;
    this.members = members;

    ArrayList<Integer> ids = new ArrayList<>();
    for (RichMember rm : members) {
      ids.add(rm.getId());
    }
    membersIds = ids;

    this.groupId = (group != null) ? group.getId() : 0;

    // test arguments
    if (!this.testRemoving()) {
      return;
    }

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText(
            "Removing " + membersIds.size() + " members from group: " + group.getShortName() + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements()
            .setLogSuccessText(membersIds.size() + " members removed from group: " + group.getShortName());
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
   * Tests the values, if the process can continue
   *
   * @return true/false for continue/stop
   */
  private boolean testRemoving() {

    boolean result = true;
    String errorMsg = "";

    if (groupId == 0) {
      errorMsg += "Wrong parameter <strong>Group</strong>.</br>";
      result = false;
    }

    if (membersIds.size() == 0) {
      errorMsg += "Wrong parameter <strong>Members</strong>.";
      result = false;
    }

    if (errorMsg.length() > 0) {
      UiElements.generateAlert("Parameter error", errorMsg);
    }

    return result;
  }

  /**
   * Prepares a JSON object
   *
   * @return JSONObject the whole query
   */
  private JSONObject prepareJSONObject() {

    JSONNumber group = new JSONNumber(groupId);
    JSONArray members = new JSONArray();

    for (int index = 0; index < membersIds.size(); index++) {
      members.set(index, new JSONNumber(membersIds.get(index)));
    }

    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("group", group);
    jsonQuery.put("members", members);
    return jsonQuery;
  }

}
