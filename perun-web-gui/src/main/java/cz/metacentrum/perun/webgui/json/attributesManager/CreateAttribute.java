package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.HTML;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

/**
 * Ajax query which creates attribute definition
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class CreateAttribute {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "attributesManager/createAttribute";

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private String friendlyName = "";
	private String description = "";
	private String type = "";
	private String namespace = "";
	private String displayName = "";
	private boolean unique = false;

	/**
	 * Creates a new request
	 */
	public CreateAttribute() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 * @param events external events
	 */
	public CreateAttribute(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Creates attribute definition in DB - make RPC call
	 *
	 * @param friendlyName name of new attribute
	 * @param description description of new attribute
	 * @param namespace namespace of new attribute
	 * @param type type of new attribute (core,def,opt,virt)
	 */
	public void createAttributeDefinition(final String displayName, final String friendlyName, final String description, final String namespace, final String type, final boolean unique) {

		this.displayName = displayName;
		this.friendlyName = friendlyName;
		this.description = description;
		this.namespace = namespace;
		this.type = type;
		this.unique = unique;

		// test arguments
		if(!this.testCreating()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Creating attribute definition: " + friendlyName + " failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Attribute definition: "+ friendlyName +" successfully created.");
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

	/**
	 * Tests the values, if the process can continue
	 *
	 * @return true/false for continue/stop
	 */
	private boolean testCreating()
	{
		boolean result = true;
		String errorMsg = "";

		if(friendlyName.length() == 0){
			errorMsg += "You must enter attribute <strong>Friendly name</strong>.</ br>";
			result = false;
		}

		if(friendlyName.length() == 0){
			errorMsg += "You must enter attribute <strong>Display name</strong>.</ br>";
			result = false;
		}


		if(namespace.length() == 0){
			errorMsg += "Wrong parameter <strong>EntityType+DefinitionType</strong>.</ br>";
			result = false;
		}

		if(type.length() == 0){
			errorMsg += "Wrong parameter <strong>Values type</strong>.</ br>";
			result = false;
		}

		if(errorMsg.length()>0){
			Confirm c = new Confirm("Error while creating attribute", new HTML(errorMsg), true);
			c.show();
		}

		return result;
	}

	/**
	 * Prepares a JSON object.
	 *
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject() {

		JSONObject attributeDef = new JSONObject();
		attributeDef.put("id", new JSONNumber(0));
		attributeDef.put("displayName", new JSONString(displayName));
		attributeDef.put("friendlyName", new JSONString(friendlyName));
		attributeDef.put("description", new JSONString(description));
		attributeDef.put("namespace", new JSONString(namespace));
		attributeDef.put("type", new JSONString(type));
		attributeDef.put("unique", JSONBoolean.getInstance(unique));

		// create whole JSON query
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("attribute", attributeDef);
		return jsonQuery;

	}

}
