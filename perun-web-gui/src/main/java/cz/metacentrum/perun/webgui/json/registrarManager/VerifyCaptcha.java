package cz.metacentrum.perun.webgui.json.registrarManager;


import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Request, which verifies captcha
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VerifyCaptcha implements JsonCallback {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL = "registrarManager/verifyCaptcha";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// application form
	private String challenge;
	private String response;

	/**
	 * Creates a new request
	 */
	public VerifyCaptcha(String challenge, String response) {
		this.challenge = challenge;
		this.response = response;
	}

	/**
	 * Creates a new request with custom events
	 * @param events Custom events
	 */
	public VerifyCaptcha(String challenge, String response, JsonCallbackEvents events) {
		this.challenge = challenge;
		this.response = response;
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
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "challenge="+challenge+"&response="+response, this);

	}
}
