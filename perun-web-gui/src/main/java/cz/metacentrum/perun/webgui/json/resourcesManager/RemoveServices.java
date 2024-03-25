package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import java.util.ArrayList;

/**
 * Ajax query which removes service from resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class RemoveServices {

  // URL to call
  final String JSON_URL = "resourcesManager/removeServices";
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  // external events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  // ids
  private int resourceId = 0;
  private int[] serviceIds = new int[1];

  /**
   * Creates a new request
   */
  public RemoveServices() {
    this.session = session;
  }

  /**
   * Creates a new request with custom events passed from tab or page
   *
   * @param events custom events
   */
  public RemoveServices(final JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Attempts to remove service from resource
   *
   * @param serviceId  ID of service which should be removed
   * @param resourceId ID of resource where should be removed
   */
  public void removeService(final int serviceId, final int resourceId) {

    this.resourceId = resourceId;
    this.serviceIds[0] = serviceId;

    // test arguments
    if (!this.testRemoving()) {
      return;
    }

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements()
            .setLogErrorText("Removing service: " + serviceId + " from resource: " + resourceId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements()
            .setLogSuccessText("Service: " + serviceId + " successfully removed from resource: " + resourceId);
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
   * Attempts to remove Services from Resource
   *
   * @param resourceId ID of resource
   * @param services   IDs of services to remove
   */
  public void removeServices(final int resourceId, final ArrayList<Service> services) {

    this.resourceId = resourceId;
    for (int i = 0; i < services.size(); i++) {
      serviceIds[i] = services.get(i).getId();
    }

    // test arguments
    if (!this.testRemoving()) {
      return;
    }

    // new events
    JsonCallbackEvents newEvents = new JsonCallbackEvents() {
      public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Removing services from resource: " + resourceId + " failed.");
        events.onError(error);
      }

      ;

      public void onFinished(JavaScriptObject jso) {
        session.getUiElements().setLogSuccessText("Services successfully removed from resource: " + resourceId);
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

    if (serviceIds.length == 0) {
      errorMsg += "Wrong Service parameter.\n";
      result = false;
    }

    if (resourceId == 0) {
      errorMsg += "Wrong Resource parameter.\n";
      result = false;
    }

    if (errorMsg.length() > 0) {
      Window.alert(errorMsg);
    }

    return result;
  }

  /**
   * Prepares a JSON object
   *
   * @return JSONObject the whole query
   */
  private JSONObject prepareJSONObject() {

    // create whole JSON query
    JSONObject jsonQuery = new JSONObject();

    jsonQuery.put("resource", new JSONNumber(resourceId));
    JSONArray servicesArray = new JSONArray();
    // put names in array
    for (int i = 0; i < serviceIds.length; i++) {
      servicesArray.set(i, new JSONNumber(serviceIds[i]));
    }
    jsonQuery.put("services", servicesArray);
    return jsonQuery;
  }

}
