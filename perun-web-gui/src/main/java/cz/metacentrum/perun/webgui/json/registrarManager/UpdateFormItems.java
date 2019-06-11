package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.ApplicationFormItem;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;

/**
 * Request, which updates form items
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UpdateFormItems {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/updateFormItems";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// data
	private ArrayList<ApplicationFormItem> formItems = new ArrayList<ApplicationFormItem>();

	// vo or group
	private int id;
	private PerunEntity entity;

	/**
	 * Creates a new request
	 *
	 * @param entity VO or Group
	 * @param id ID of entity
	 */
	public UpdateFormItems(PerunEntity entity, int id) {
		this.entity = entity;
		this.id = id;
	}

	/**
	 * Creates a new request with custom events
	 *
	 * @param entity VO or Group
	 * @param id ID of entity
	 * @param events Custom events
	 */
	public UpdateFormItems(PerunEntity entity, int id, JsonCallbackEvents events) {
		this.entity = entity;
		this.id = id;
		this.events = events;
	}

	/**
	 * Updates form items in DB by passed list of them
	 *
	 * @param formItems
	 */
	public void updateFormItems(ArrayList<ApplicationFormItem> formItems) {

		this.formItems = formItems;;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Updating form items failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Form items updated.");
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
	private JSONObject prepareJSONObject()
	{

		// data to JSON array
		JSONArray data = new JSONArray();
		for(int i = 0; i<formItems.size(); i++){

			// get
			JSONObject obj = new JSONObject(formItems.get(i));

			// reconstruct
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

			// recreate i18n
			JSONObject i18n = new JSONObject();
			i18n.put("en", new JSONObject(formItems.get(i).getItemTexts("en")));
			if (!Utils.getNativeLanguage().isEmpty()) {
				i18n.put(Utils.getNativeLanguage().get(0), new JSONObject(formItems.get(i).getItemTexts(Utils.getNativeLanguage().get(0))));
			}
			newItem.put("i18n", i18n);

			data.set(i, newItem);

		}

		// query
		JSONObject query = new JSONObject();

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			query.put("vo", new JSONNumber(id));
		} else if (PerunEntity.GROUP.equals(entity)) {
			query.put("group", new JSONNumber(id));
		}
		query.put("items", data);

		return query;

	}

}
