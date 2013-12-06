package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;

/**
 * Update resource details
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 6f992a4341026e85889ee2b9164ce2b873f64bb0 $
 */
public class UpdateResource {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// Json URL
	static private final String JSON_URL = "resourcesManager/updateResource";

	/**
	 * New instance of CreateGroup
	 */
	public UpdateResource() {
	}

	/**
	 * New instance of CreateGroup
	 *
	 * @param events
	 */
	public UpdateResource(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Updates resource details
	 * @param res Resource with updated details
	 */
	public void updateResource(Resource res) {
		
		if (res == null) {
			Window.alert("Resource can't be null");
			return;
		}
		
		// GROUP OBJECT
		JSONObject oldResource = new JSONObject(res);
		// RECONSTRUCT OBJECT
		JSONObject newResource = new JSONObject();
		newResource.put("id", oldResource.get("id"));
		newResource.put("name", oldResource.get("name"));
		newResource.put("description", oldResource.get("description"));
		newResource.put("voId", oldResource.get("voId"));
		newResource.put("facilityId", oldResource.get("facilityId"));
		newResource.put("beanName", oldResource.get("beanName"));
		newResource.put("createdAt", oldResource.get("createdAt"));
		newResource.put("createdBy", oldResource.get("createdBy"));
		newResource.put("modifiedAt", oldResource.get("modifiedAt"));
		newResource.put("modifiedBy", oldResource.get("modifiedBy"));
		
		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("resource", newResource);

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating resource failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				Resource res = jso.cast();
				session.getUiElements().setLogSuccessText("Resource "+ res.getName() +" successfully updated!");
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