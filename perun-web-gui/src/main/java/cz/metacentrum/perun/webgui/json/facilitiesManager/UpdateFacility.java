package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Update facility details
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: $
 */
public class UpdateFacility {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "facilitiesManager/updateFacility";

	/**
	 * New instance of CreateGroup
	 */
	public UpdateFacility() {
	}

	/**
	 * New instance of CreateGroup
	 *
	 * @param events
	 */
	public UpdateFacility(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Updates facility details
     *
	 * @param fac Facility with updated details
	 */
	public void updateFacility(Facility fac) {
		
		if (fac == null) {
            UiElements.generateAlert("Error updating facility", "Facility to update can't be null.");
            return;
		}
		
		// GROUP OBJECT
		JSONObject oldFacility = new JSONObject(fac);
		// RECONSTRUCT OBJECT
		JSONObject newFacility = new JSONObject();
		newFacility.put("id", oldFacility.get("id"));
		newFacility.put("name", oldFacility.get("name"));
		newFacility.put("type", oldFacility.get("type"));
		newFacility.put("beanName", oldFacility.get("beanName"));
		newFacility.put("createdAt", oldFacility.get("createdAt"));
		newFacility.put("createdBy", oldFacility.get("createdBy"));
		newFacility.put("modifiedAt", oldFacility.get("modifiedAt"));
		newFacility.put("modifiedBy", oldFacility.get("modifiedBy"));
		
		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("facility", newFacility);

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating facility failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Facility fac = jso.cast();
				session.getUiElements().setLogSuccessText("Facility "+ fac.getName() +" successfully updated!");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, jsonQuery);
		
	}

}