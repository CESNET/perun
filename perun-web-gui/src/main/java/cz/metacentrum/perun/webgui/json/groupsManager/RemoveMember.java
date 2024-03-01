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
import java.util.List;

/**
 * Ajax query to remove member from group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveMember {

  // URL to call
  final String JSON_URL = "groupsManager/removeMember";
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  // external events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  // ids
  private int memberId = 0;
  private int groupId = 0;
  private RichMember member = null;
  private Group group = null;
  private List<Group> groups = null;

  /**
   * Creates a new request
   */
  public RemoveMember() {
  }

  /**
   * Creates a new request with custom events passed from tab or page
   *
   * @param events JsonCallbackaEvents
   */
  public RemoveMember(final JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Attempts to remove member from group
   *
   * @param groupId  id of group
   * @param memberId ID of member to be removed from group
   */
  public void removeMemberFromGroup(final int groupId, final int memberId) {

    this.memberId = memberId;
    this.groupId = groupId;

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
            .setLogErrorText("Removing member: " + memberId + " from group: " + groupId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Member: " + memberId + " removed from group: " + groupId);
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
   * @param group  group
   * @param member member to be removed from group
   */
  public void removeMemberFromGroup(final Group group, final RichMember member) {

    this.group = group;
    this.member = member;

    this.memberId = (member != null) ? member.getId() : 0;
    this.groupId = (group != null) ? group.getId() : 0;

    // test arguments
    if (!this.testRemoving()) {
      return;
    }

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText(
            "Removing member: " + member.getUser().getFullName() + " from group: " + group.getShortName() + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText(
            "Member: " + member.getUser().getFullName() + " removed from group: " + group.getShortName());
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
   * Attempts to remove member from multiple groups
   *
   * @param groups groups
   * @param member member to be removed from groups
   */
  public void removeMemberFromGroups(final RichMember member, List<Group> groups) {

    this.groups = groups;
    this.member = member;

    String errorMsg = "";

    if (groups.size() == 0) {
      errorMsg += "Wrong parameter <strong>Groups</strong>.</br>";
    }

    if (member == null) {
      errorMsg += "Wrong parameter <strong>Member</strong>.";
    }

    if (errorMsg.length() > 0) {
      UiElements.generateAlert("Parameter error", errorMsg);
      return;
    }

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText(
            "Removing member: " + member.getUser().getFullName() + " from " + groups.size() + " groups failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText(
            "Member: " + member.getUser().getFullName() + " removed from " + groups.size() + " groups.");
        events.onFinished(jso);
      }

      ;

      public void onLoadingStart() {
        events.onLoadingStart();
      }

      ;
    };

    JSONNumber mem = new JSONNumber(member.getId());
    JSONArray grps = new JSONArray();

    for (int index = 0; index < groups.size(); index++) {
      grps.set(index, new JSONNumber(groups.get(index).getId()));
    }

    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("groups", grps);
    jsonQuery.put("member", mem);

    // sending data
    JsonPostClient jspc = new JsonPostClient(newEvents);
    jspc.sendData(JSON_URL, jsonQuery);

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

    if (memberId == 0) {
      errorMsg += "Wrong parameter <strong>Member</strong>.";
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
    JSONNumber member = new JSONNumber(memberId);

    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("group", group);
    jsonQuery.put("member", member);
    return jsonQuery;
  }

}
