package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Class with a client for JSON calls. For each call a new instance must be created.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetPendingRequests implements JsonCallback{

	// JSON URL
	public static final String JSON_URL = "getPendingRequests";

	private JsonCallbackEvents events = new JsonCallbackEvents();
	private double callbackName;

	public GetPendingRequests(double callbackName, JsonCallbackEvents events){
		this.callbackName = callbackName;
		this.events = events;
	}

	public GetPendingRequests(double callbackName){
		this.callbackName = callbackName;
	}

	public void retrieveData() {
		JsonClient client = new JsonClient();
		client.setHidden(true);
		client.retrieveData(JSON_URL, "callbackId="+callbackName, this);
	}

	public void onFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	public void onError(PerunError error) {
		events.onError(error);
	}

	public void onLoadingStart() {
		events.onLoadingStart();
	}

}





