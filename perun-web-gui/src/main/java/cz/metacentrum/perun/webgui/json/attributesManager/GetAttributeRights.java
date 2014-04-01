package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Ajax query to get Rights above specific attribute
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAttributeRights implements JsonCallback {

	private final String JSON_URL = "attributesManager/getAttributeRights";

	private PerunWebSession session = PerunWebSession.getInstance();
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int attributeId = 0;

	/**
	 * Creates callback instance
	 *
	 * @param attributeId ID of attribute to get rights for
	 */
	public GetAttributeRights(int attributeId) {
		this.attributeId = attributeId;
	}

	/**
	 * Creates callback instance
	 *
	 * @param attributeId ID of attribute to get rights for
	 * @param events external events
	 */
	public GetAttributeRights(int attributeId, JsonCallbackEvents events) {
		this.attributeId = attributeId;
		this.events = events;
	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Loading attribute rights finished.");
		events.onFinished(jso);
	}

	@Override
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Loading attribute rights failed.");
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading attribute rights started.");
		events.onLoadingStart();
	}

	@Override
	public void retrieveData() {

		JsonClient client = new JsonClient();
		client.retrieveData(JSON_URL, "attributeId="+attributeId, this);

	}

}
