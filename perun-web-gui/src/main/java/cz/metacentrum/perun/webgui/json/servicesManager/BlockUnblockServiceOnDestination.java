package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to ban selected service on destination
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class BlockUnblockServiceOnDestination {

  // URL to call
  final String JSON_URL = "servicesManager/blockServiceOnDestination";
  final String JSON_URL_UNBLOCK = "servicesManager/unblockServiceOnDestination";
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  // custom events
  private JsonCallbackEvents events = new JsonCallbackEvents();

  /**
   * Creates a new request
   */
  public BlockUnblockServiceOnDestination() {
  }

  /**
   * Creates a new request with custom events
   *
   * @param events Custom events
   */
  public BlockUnblockServiceOnDestination(JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Attempts to ban selected service for specified facility
   *
   * @param serviceId
   * @param destinationId
   */
  public void blockServiceOnDestination(int serviceId, int destinationId) {

    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("destination", new JSONNumber(destinationId));
    jsonQuery.put("service", new JSONNumber(serviceId));

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Blocking service " + serviceId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Service " + serviceId + " blocked on destination.");
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
   * Attempts to ban selected service for specified facility
   *
   * @param serviceId
   * @param destinationId
   */
  public void unblockServiceOnDestination(int serviceId, int destinationId) {

    JSONObject jsonQuery = new JSONObject();
    jsonQuery.put("destination", new JSONNumber(destinationId));
    jsonQuery.put("service", new JSONNumber(serviceId));

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Allowing service " + serviceId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Service " + serviceId + " allowed on destination.");
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
    jspc.sendData(JSON_URL_UNBLOCK, jsonQuery);

  }

  /**
   * Sets external events after callback creation
   *
   * @param events
   */
  public void setEvents(JsonCallbackEvents events) {
    this.events = events;
  }

}
