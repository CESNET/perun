package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Class for POST requests to RPC.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class JsonPostClient {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * JSON object to be sent
	 * For object format - check sources in: cz.metacentrum.perun.rpc.methods
	 */
	private JSONObject jsonObject;

	private String json;
	private boolean sendNative = false;

	/**
	 * URL to be called, eg. "vosManager/createVo"
	 */
	private String url;

	/**
	 * Custom events - onFinished, onError
	 */
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Callback name - can be anything, just for internal purpose
	 */
	private String callbackName = "callbackPost";

	/**
	 * Hidden callback do not show error pop-up
	 */
	private boolean hidden = false;


	/**
	 * New JsonPostClient
	 */
	public JsonPostClient() {}


	/**
	 * New JsonPostClient
	 * @param events
	 */
	public JsonPostClient(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Sends data.
	 * Returns true, if the data was send.
	 * After the callback is complete, it calls the events.
	 *
	 * @param json
	 * @return
	 */
	public boolean sendData(String url, JSONObject json)
	{
		if(json == null){
			return false;
		}
		this.sendNative = false;
		this.url = url;
		this.jsonObject = json;
		this.send();
		return true;
	}

	/**
	 * Sends native data.
	 * Returns true, if the data was send.
	 * After the callback is complete, it calls the events.
	 *
	 * @param json
	 * @return
	 */
	public boolean sendNativeData(String url, String json)
	{
		if(json == null){
			return false;
		}
		this.sendNative = true;
		this.url = url;
		this.json = json;
		this.send();
		return true;
	}

	/**
	 * Sends the data
	 */
	private void send() {

		// url to call
		String requestUrl = PerunWebSession.getInstance().getRpcUrl() + url + "?callback=" + callbackName;

		// request building
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, URL.encode(requestUrl));
		try {
			// sends the request
			onRequestLoadingStart();
			String data = "";
			if (sendNative) {
				data = json;
			} else {
				data = jsonObject.toString();
			}
			builder.sendRequest(data, new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {
					// if response = OK
					if(resp.getStatusCode() == 200)
			{
				// jso
				JavaScriptObject jso = parseResponse(resp.getText());

				// if null - finished
				if(jso == null)
			{
				session.getUiElements().setLogText("Response NULL.");
				onRequestFinished(null);
				return;
			}

			// if error?
			PerunError error = (PerunError) jso;
			if("".equalsIgnoreCase(error.getErrorId()) && "".equalsIgnoreCase(error.getErrorInfo())){
				// not error, OK
				session.getUiElements().setLogText("Response not NULL, not ERROR.");
				onRequestFinished(jso);
				return;
			}

			// triggers onError
			session.getUiElements().setLogText("Response ERROR.");
			onRequestError(jso);
			return;
			}

			// triggers onError
			onRequestError(parseResponse(resp.getText()));
				}
			@Override
			public void onError(Request req, Throwable exc) {
				// request not sent
				onRequestError(parseResponse(exc.toString()));
			}
			});
		} catch (RequestException exc) {
			// usually couldn't connect to server
			onRequestError(parseResponse(exc.toString()));
		}
	}

	/**
	 * Parses the responce to an object.
	 * @param resp JSON string
	 * @return
	 */
	protected JavaScriptObject parseResponse(String resp)
	{
		// trims the whitespace
		resp = resp.trim();

		// short comparing
		if((callbackName + "(null);").equalsIgnoreCase(resp)){
			return null;
		}

		// if starts with callbackName( and ends with ) or ); - wrapped, must be unwrapped
		RegExp re = RegExp.compile("^" + callbackName + "\\((.*)\\)|\\);$");
		MatchResult result = re.exec(resp);
		if(result != null){
			resp = result.getGroup(1);
		}

		// if response = null - return null
		if(resp.equals("null")){
			return null;
		}

		// normal object
		JavaScriptObject jso = JsonUtils.parseJson(resp);
		return jso;

	}

	/**
	 * Called when loading starts
	 */
	protected void onRequestLoadingStart(){
		events.onLoadingStart();
	}

	/**
	 * Called when loading finishes
	 * @param jso
	 */
	protected void onRequestFinished(JavaScriptObject jso){
		events.onFinished(jso);
	}

	/**
	 * Called when error occured.
	 * @param jso
	 */
	protected void onRequestError(JavaScriptObject jso){

		if(jso != null){
			PerunError e = (PerunError) jso;
			session.getUiElements().setLogErrorText("Error while sending request: " + e.getName() );
			if (!hidden) {
				// creates a alert box
				JsonErrorHandler.alertBox(e, url, jsonObject);
			}
			events.onError(e);
		}
		else
		{
			PerunError e = (PerunError) JsonUtils.parseJson("{\"errorId\":\"0\",\"name\":\"Cross-site request\",\"type\":\""+ WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteType()+"\",\"message\":\""+ WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteText()+"\"}").cast();
			session.getUiElements().setLogErrorText("Error while sending request: The response was null or cross-site request.");
			JsonErrorHandler.alertBox(e, url, jsonObject);
		}
		events.onError(null);

	}

	/**
	 * Set callback hidden (do not show error pop-up)
	 * @param hidden (true=hidden/false=show - default)
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
