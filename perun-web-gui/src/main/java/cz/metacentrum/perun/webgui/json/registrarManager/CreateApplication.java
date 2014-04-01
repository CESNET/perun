package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.ApplicationFormItemData;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;

/**
 * Request, which creates a new application
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateApplication {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/createApplication";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// application form
	private Application application;

	// data
	private ArrayList<ApplicationFormItemData> formData = new ArrayList<ApplicationFormItemData>();

	/**
	 * Creates a new request
	 */
	public CreateApplication() {}

	/**
	 * Creates a new request with custom events
	 * @param events Custom events
	 */
	public CreateApplication(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Creating application
	 *
	 * @param application
	 * @param formData
	 */
	public void createApplication(Application application, ArrayList<ApplicationFormItemData> formData) {
		this.application = application;
		this.formData = formData;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating application failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application created.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject());

	}

	private boolean testCreating() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject() {
		// data to JSON array
		JSONArray data = new JSONArray();
		for(int i = 0; i<formData.size(); i++){
			data.set(i, new JSONObject(formData.get(i)));
		}

		// query
		JSONObject query = new JSONObject();
		query.put("app", new JSONObject(application));
		query.put("data", data);

		return query;
	}

}
