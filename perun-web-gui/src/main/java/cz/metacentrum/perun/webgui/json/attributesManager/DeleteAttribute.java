package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.Label;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which deletes attribute definition
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class DeleteAttribute {

  // URL to call
  final String JSON_URL = "attributesManager/deleteAttribute";
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  // external events
  private JsonCallbackEvents events = new JsonCallbackEvents();

  private int attrDefId = 0;

  /**
   * Creates a new request
   */
  public DeleteAttribute() {
  }

  /**
   * Creates a new request with custom events passed from tab or page
   *
   * @param events external events
   */
  public DeleteAttribute(final JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Deletes attribute definition from DB - make RPC call
   *
   * @param attrDefId - ID of attribute definition which should be deleted
   */
  public void deleteAttributeDefinition(final int attrDefId) {

    this.attrDefId = attrDefId;

    // test arguments
    if (!this.testDeleting()) {
      return;
    }

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Deleting attribute definition: " + attrDefId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Attribute definition: " + attrDefId + " successfully deleted.");
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
  private boolean testDeleting() {

    boolean result = true;
    String errorMsg = "";

    if (attrDefId == 0) {
      errorMsg += "Wrong Attribute definition ID parameter.\n";
      result = false;
    }

    if (errorMsg.length() > 0) {
      Confirm c = new Confirm("Error while deleting attribute", new Label(errorMsg), true);
      c.show();
    }

    return result;

  }

  /**
   * Prepares a JSON object.
   *
   * @return JSONObject - the whole query
   */
  private JSONObject prepareJSONObject() {

    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("attribute", new JSONNumber(attrDefId));
    return jsonQuery;

  }

}
