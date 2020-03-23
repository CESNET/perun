package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.ApplicationFormItemData;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which updates form item data
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateFormItemData {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/updateFormItemData";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private int appId = 0;

	/**
	 * Creates a new request
	 *
	 */
	public UpdateFormItemData() {
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public UpdateFormItemData(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Updates form item value in DB
	 *
	 * @param data
	 */
	public void updateFormItemData(int appId, ApplicationFormItemData data) {

		this.appId = appId;

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating form item failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Form item updated.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL, prepareJSONObject(data));

	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject(ApplicationFormItemData data) {

		JSONObject query = new JSONObject();

		JSONObject obj = new JSONObject(data.getFormItem());
		JSONObject newItem = new JSONObject();
		newItem.put("id", obj.get("id"));
		newItem.put("shortname", obj.get("shortname"));
		newItem.put("required", obj.get("required"));
		newItem.put("type", obj.get("type"));
		newItem.put("federationAttribute", obj.get("federationAttribute"));
		newItem.put("perunSourceAttribute", obj.get("perunSourceAttribute"));
		newItem.put("perunDestinationAttribute", obj.get("perunDestinationAttribute"));
		newItem.put("regex", obj.get("regex"));
		newItem.put("appTypes", obj.get("appTypes"));
		newItem.put("ordnum", obj.get("ordnum"));
		newItem.put("forDelete", obj.get("forDelete"));
		newItem.put("applicationTypes", obj.get("applicationTypes"));

		JSONObject obj2 = new JSONObject(data);
		JSONObject newItem2 = new JSONObject();
		newItem2.put("id", obj2.get("id"));
		newItem2.put("shortName", obj2.get("shortName"));
		newItem2.put("value", obj2.get("value"));
		newItem2.put("prefilledValue", obj2.get("prefilledValue"));
		newItem2.put("assuranceLevel", obj2.get("assuranceLevel"));
		newItem2.put("formItem", newItem);

		query.put("appId", new JSONNumber(appId));
		query.put("data", newItem2);

		return query;

	}

}
