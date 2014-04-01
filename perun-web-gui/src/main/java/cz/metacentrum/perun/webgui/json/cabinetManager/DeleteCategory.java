package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which deletes a Category
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DeleteCategory {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// category
	private int categoryId = 0;
	// URL to call
	final String JSON_URL = "cabinetManager/deleteCategory";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 *
	 */
	public DeleteCategory() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public DeleteCategory(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false when process can/can't continue
	 */
	private boolean testDeleting()
	{
		boolean result = true;
		String errorMsg = "";

		if(categoryId == 0){
			errorMsg += "Wrong parameter 'Category ID'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to delete a Category, it first tests the values and then submits them.
	 *
	 * @param categoryId ID of category to be deleted
	 */
	public void deleteCategory(final int categoryId) {

		this.categoryId = categoryId;

		// test arguments
		if(!this.testDeleting()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting category " + categoryId + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Category " + categoryId + " deleted.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};

		};

		// create request
		JsonPostClient request = new JsonPostClient(newEvents);
		request.sendData(JSON_URL, jsonQuery);

	}

	/**
	 * Prepares a JSON object
	 * @return JSONObject the whole query
	 */
	private JSONObject prepareJSONObject()
	{
		// category
		JSONObject category = new JSONObject();
		category.put("id", new JSONNumber(categoryId));

		return category;

	}

}
