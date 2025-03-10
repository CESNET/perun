package cz.metacentrum.perun.webgui.json.configManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * configManager/getPersonalDataChangeConfig
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class GetPersonalDataChangeConfig implements JsonCallback {

  // JSON URL
  static private final String JSON_URL = "configManager/getPersonalDataChangeConfig";
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();

  private boolean hidden = false;

  /**
   * New instance of personal data change config
   */
  public GetPersonalDataChangeConfig() {
  }

  /**
   * New instance of personal data change config
   *
   * @param events
   */
  public GetPersonalDataChangeConfig(JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Retrieves data
   */
  public void retrieveData() {
    // retrieve data
    JsonClient js = new JsonClient();
    js.setHidden(hidden);
    js.retrieveData(JSON_URL, "", this);
  }

  /**
   * When successfully finishes
   */
  public void onFinished(JavaScriptObject jso) {
    events.onFinished(jso);
  }

  /**
   * When error
   */
  public void onError(PerunError error) {
    events.onError(error);
  }

  /**
   * When start
   */
  public void onLoadingStart() {
    events.onLoadingStart();
  }

  /**
   * Sets events to this callback
   *
   * @param events
   */
  public void setEvents(JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Set callback as hidden (do not show error popup)
   *
   * @param hidden
   */
  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

}
