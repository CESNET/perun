package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;

public class GetGroupAttributeByName implements JsonCallback {

	// PARAMS
	private final int groupId;
	private final String attrName;
	private JsonCallbackEvents events;
	// JSON URL
	private static final String JSON_URL = "attributesManager/getAttribute";
	// DATA
	public Attribute attribute;

	/**
	 * Creates an instance
	 *
	 * @param groupId group ID
	 * @param attrName attribute name
	 */
	public GetGroupAttributeByName(int groupId, String attrName, JsonCallbackEvents events){
		this.groupId = groupId;
		this.attrName = attrName;
		this.events = events;
	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	@Override
	public void onError(PerunError error) {
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		events.onLoadingStart();
	}

	@Override
	public void retrieveData() {
		String params = "group=" + this.groupId + "&";
		params += "attributeName=" + this.attrName;
		JsonClient js = new JsonClient();
		js.retrieveData(GetGroupAttributeByName.JSON_URL, params, this);
	}
}
