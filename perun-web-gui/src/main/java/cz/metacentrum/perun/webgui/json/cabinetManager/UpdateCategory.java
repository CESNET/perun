package cz.metacentrum.perun.webgui.json.cabinetManager;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Category;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query which updates a Category
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateCategory {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	// category
	private int id = 0;
	private String name = "";
	private double rank = 0.0;
	// URL to call
	final String JSON_URL = "cabinetManager/updateCategory";
	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public UpdateCategory() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events external events
	 */
	public UpdateCategory(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false when process can/can't continue
	 */
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

		if(name.length() == 0){
			errorMsg += "You must fill in the parameter 'Name'.\n";
			result = false;
		}

		if(id == 0){
			errorMsg += "Wrong parameter 'Id'.\n";
			result = false;
		}

		if(errorMsg.length()>0){
			Window.alert(errorMsg);
		}

		return result;
	}

	/**
	 * Attempts to update a Category, it first tests the values and then submits them.
	 *
	 * @param category category to update
	 */
	public void updateCategory(Category category)
	{

		this.id = category.getId();
		this.name = category.getName();
		this.rank = category.getRank();

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// json object
		JSONObject jsonQuery = prepareJSONObject();

		// local events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){

			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating category " + name + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Category " + name + " updated.");
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
		category.put("id", new JSONNumber(id));
		category.put("name", new JSONString(name));
		category.put("rank", new JSONNumber(rank));

		// whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("category", category);
		return jsonQuery;

	}

}
