package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Ajax query which gets list of attributes
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class GetListOfAttributes implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "attributesManager/getAttributes";

	// IDs
	private Map<String, Integer> ids = new HashMap<String, Integer>();
	private ArrayList<String> attributes = new ArrayList<String>();
	private String attributesString;

	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Creates a new request
	 */
	public GetListOfAttributes() {}

	/**
	 * Creates a new request with custom events passed from tab or page
	 *
	 * @param events externalEvents
	 */
	public GetListOfAttributes(final JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Attempts to get list of attributes
	 *
	 * @param ids defines which type of attribute will be set (member, user, member_resource, etc.)
	 * @param attributes list of attributes with a new value
	 */
	public void getListOfAttributes(final Map<String, Integer> ids, final ArrayList<String> attributes)
	{

		this.ids = ids;
		this.attributes = attributes;
		retrieveData();

	}

	/**
	 * Attempts to get list of attributes
	 *
	 * @param ids defines which type of attribute will be set (member, user, member_resource, etc.)
	 * @param attributes list of attributes with a new value
	 */
	public void getListOfAttributes(final Map<String, Integer> ids, final String attributes)
	{

		this.ids = ids;
		this.attributesString = attributes;
		retrieveData();

	}

	/**
	 * Retrieves data from the RPC
	 */
	public void retrieveData() {

		String params = "";
		// serialize parameters
		for (Map.Entry<String, Integer> attr : this.ids.entrySet()) {
			params += attr.getKey() + "=" + attr.getValue() + "&";
		}

		if (attributes != null && !attributes.isEmpty()) {
			// parse lists
			for (int i=0; i<attributes.size(); i++) {
				if (i != attributes.size()-1) {
					params += "attrNames[]=" + attributes.get(i) + "&";
				} else {
					params += "attrNames[]=" + attributes.get(i);
				}
			}


		} else if (attributesString != null && attributesString.length() != 0) {

			String[] splitted = attributesString.split(",");
			for (int i=0; i<splitted.length; i++) {
				params += "attrNames[]="+splitted[i].trim()+"&";
			}

		}

		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, params, this);

		// clear values after call because they can be switched
		this.attributes = null;
		this.attributesString = null;

	}

	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Loading of attributes finished.");
		events.onFinished(jso);
	}

	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Loading of attributes failed.");
		events.onError(error);
	}

	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading of attributes started.");
		events.onLoadingStart();
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}
