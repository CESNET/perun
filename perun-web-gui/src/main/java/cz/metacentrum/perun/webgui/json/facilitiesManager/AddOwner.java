package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which adds owner to facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddOwner {

  // URL to call
  final String JSON_URL = "facilitiesManager/addOwner";
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  // IDS
  private int owner = 0;
  private int facility = 0;
  // custom events
  private JsonCallbackEvents events = new JsonCallbackEvents();

  /**
   * Creates a new request
   */
  public AddOwner() {
  }

  /**
   * Creates a new request with custom events
   *
   * @param events Custom events
   */
  public AddOwner(JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Tests the values, if the process can continue
   *
   * @return true/false if process can/can't continue
   */
  private boolean testAdding() {
    boolean result = true;
    String errorMsg = "";

    if (facility == 0) {
      errorMsg += "Wrong parameter 'Facility'.\n";
      result = false;
    }

    if (owner == 0) {
      errorMsg += "Wrong parameter 'Owner'.\n";
      result = false;
    }

    if (errorMsg.length() > 0) {
      Window.alert(errorMsg);
    }

    return result;
  }

  /**
   * Attempts to add owner to facility, it first tests the values and then submits them.
   *
   * @param facility ID of facility which should have owner added
   * @param owner    ID of owner to be added to facility
   */
  public void addOwner(final int facility, final int owner) {
    this.facility = facility;
    this.owner = owner;

    // test arguments
    if (!this.testAdding()) {
      return;
    }

    // json object
    JSONObject jsonQuery = prepareJSONObject();

    // local events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {

      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Adding owner " + owner + " to facility " + facility + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Owner " + owner + " added to facility " + facility);
        events.onFinished(jso);
      }

      ;

      public void onLoadingStart() {
        events.onLoadingStart();
      }

      ;

    };

    // create request
    JsonPostClient request = new JsonPostClient(newEvents);
    request.sendData(JSON_URL, jsonQuery);

  }

  /**
   * Prepares a JSON object
   *
   * @return JSONObject the whole query
   */
  private JSONObject prepareJSONObject() {
    // whole JSON query
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("facility", new JSONNumber(facility));
    jsonQuery.put("owner", new JSONNumber(owner));
    return jsonQuery;
  }

}
