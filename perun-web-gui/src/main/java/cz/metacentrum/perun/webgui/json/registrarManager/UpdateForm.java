package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.ApplicationForm;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which updates vo application form - to switch automatic and manual approval
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateForm {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/updateForm";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private ApplicationForm form;

	/**
	 * Creates a new request
	 */
	public UpdateForm(){}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public UpdateForm(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Updates form
	 *
	 * @param form
	 */
	public void updateForm(ApplicationForm form) {

		this.form = form;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating approval style failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Approval style updated.");
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

		// query
		JSONObject query = new JSONObject(form);
		JSONObject result = new JSONObject();
		result.put("form", query);

		return result;

	}

}
