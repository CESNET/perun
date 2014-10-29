package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunRequest;

import java.util.HashMap;
import java.util.Map;

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
	private Map<String, PerunRequest> runningRequests = new HashMap<>();


	/**
	 * New JsonPostClient
	 */
	public JsonPostClient() {
	}


	/**
	 * New JsonPostClient
	 *
	 * @param events
	 */
	public JsonPostClient(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Put value to output data
	 *
	 * @param key
	 * @param json
	 */
	public void put(String key, JSONValue json) {
		if (jsonObject == null) {
			jsonObject = new JSONObject();
		}
		jsonObject.put(key, json);
	}

	/**
	 * Sends data.
	 * Returns true, if the data was send.
	 * After the callback is complete, it calls the events.
	 *
	 * @param json
	 * @return
	 */
	public boolean sendData(String url, JSONObject json) {
		if (json == null) {
			return false;
		}
		this.sendNative = false;
		this.url = url;
		this.jsonObject = json;
		this.send();
		return true;
	}

	/**
	 * Sends data.
	 * Returns true, if the data was send.
	 * After the callback is complete, it calls the events.
	 *
	 * @return
	 */
	public boolean sendData(String url) {
		if (jsonObject == null) {
			return false;
		}
		this.sendNative = false;
		this.url = url;
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
	public boolean sendNativeData(String url, String json) {
		if (json == null) {
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
		final String requestUrl = URL.encode(PerunWebSession.getInstance().getRpcUrl() + url + "?callback=" + callbackName);


		String data = "";
		if (sendNative) {
			data = json;
		} else {
			data = jsonObject.toString();
		}

		PerunRequest perunRequest = new JSONObject().getJavaScriptObject().cast();
		perunRequest.setStartTime();
		perunRequest.setManager(url.split("\\/")[0]);
		perunRequest.setMethod(url.split("\\/")[1]);
		perunRequest.setParamString("?callback=" + callbackName + "&" + data);

		// request building
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, requestUrl);
		try {
			// sends the request
			onRequestLoadingStart();

			final String payload = data;
			builder.sendRequest(payload, new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {
					// if response = OK
					if (resp.getStatusCode() == 200) {
						// jso
						JavaScriptObject jso = parseResponse(resp.getText());

						// if null - finished
						if (jso == null) {
							session.getUiElements().setLogText("Response NULL.");
							runningRequests.remove(requestUrl);
							onRequestFinished(null);
							return;
						}

						// if error?
						PerunError error = (PerunError) jso;
						if ("".equalsIgnoreCase(error.getErrorId()) && "".equalsIgnoreCase(error.getErrorInfo())) {
							// not error, OK
							session.getUiElements().setLogText("Response not NULL, not ERROR.");
							runningRequests.remove(requestUrl);
							onRequestFinished(jso);
							return;
						}

						// triggers onError
						session.getUiElements().setLogText("Response ERROR.");
						error.setRequestURL(requestUrl);
						error.setPostData(payload);
						runningRequests.remove(requestUrl);
						onRequestError(error);
						return;

					} else if (resp.getStatusCode() == 401) {

						PerunError error = new JSONObject().getJavaScriptObject().cast();
						error.setErrorId("401");
						error.setName("NotAuthorized");
						error.setErrorInfo("You are not authorized to server. Your session might have expired. Please refresh the browser window to re-login.");
						error.setObjectType("PerunError");
						error.setRequestURL(requestUrl);
						error.setPostData(payload);
						runningRequests.remove(requestUrl);
						onRequestError(error);
						return;

					} else if (resp.getStatusCode() == 500) {

						if (runningRequests.get(requestUrl) != null) {

							if ((runningRequests.get(requestUrl).getDuration() / (1000 * 60)) >= 5) {
								// 5 minute timeout

								PerunError error = new JSONObject().getJavaScriptObject().cast();
								error.setErrorId("408");
								error.setName("RequestTimeout");
								error.setErrorInfo("Your operation is still processing on server. Please refresh your view (table) to see, if it ended up successfully before trying again.");
								error.setObjectType("PerunError");
								error.setRequestURL(requestUrl);
								error.setPostData("");
								runningRequests.remove(requestUrl);
								onRequestError(error);
								return;

							} else {

								PerunError error = new JSONObject().getJavaScriptObject().cast();
								error.setErrorId("500");
								error.setName("ServerInternalError");
								error.setErrorInfo("Server encounter internal error while processing your request. Please report this error and retry.");
								error.setObjectType("PerunError");
								error.setRequestURL(requestUrl);
								error.setPostData(payload);
								runningRequests.remove(requestUrl);
								onRequestError(error);
								return;

							}

						}

					} else if (resp.getStatusCode() == 503) {

						PerunError error = new JSONObject().getJavaScriptObject().cast();
						error.setErrorId("503");
						error.setName("Server Temporarily Unavailable");
						error.setErrorInfo("Server is temporarily unavailable. Please try again later.");
						error.setObjectType("PerunError");
						error.setRequestURL(requestUrl);
						error.setPostData(payload);
						runningRequests.remove(requestUrl);
						onRequestError(error);
						return;

					} else if (resp.getStatusCode() == 404) {

						PerunError error = new JSONObject().getJavaScriptObject().cast();
						error.setErrorId("404");
						error.setName("Not found");
						error.setErrorInfo("Server is probably being restarted at the moment. Please try again later.");
						error.setObjectType("PerunError");
						error.setRequestURL(requestUrl);
						error.setPostData(payload);
						runningRequests.remove(requestUrl);
						onRequestError(error);
						return;

					} else if (resp.getStatusCode() == 0) {

						// request aborted
						PerunError error = new JSONObject().getJavaScriptObject().cast();
						error.setErrorId("0");
						error.setName("Aborted");
						error.setErrorInfo("Can't contact remote server, connection was lost.");
						error.setObjectType("PerunError");
						error.setRequestURL(requestUrl);
						error.setPostData("");
						runningRequests.remove(requestUrl);
						onRequestError(error);
						return;

					}

					// triggers onError
					runningRequests.remove(requestUrl);
					onRequestError(parseResponse(resp.getText()));
				}

				@Override
				public void onError(Request req, Throwable exc) {
					// request not sent
					runningRequests.remove(requestUrl);
					onRequestError(parseResponse(exc.toString()));
				}
			});

			runningRequests.put(requestUrl, perunRequest);

		} catch (RequestException exc) {
			// usually couldn't connect to server
			onRequestError(parseResponse(exc.toString()));
		}
	}

	/**
	 * Parses the responce to an object.
	 *
	 * @param resp JSON string
	 * @return
	 */
	protected JavaScriptObject parseResponse(String resp) {
		// trims the whitespace
		resp = resp.trim();

		// short comparing
		if ((callbackName + "(null);").equalsIgnoreCase(resp)) {
			return null;
		}

		// if starts with callbackName( and ends with ) or ); - wrapped, must be unwrapped
		RegExp re = RegExp.compile("^" + callbackName + "\\((.*)\\)|\\);$");
		MatchResult result = re.exec(resp);
		if (result != null) {
			resp = result.getGroup(1);
		}

		// if response = null - return null
		if (resp.equals("null")) {
			return null;
		}

		// normal object
		JavaScriptObject jso = JsonUtils.parseJson(resp);
		return jso;

	}

	/**
	 * Called when loading starts
	 */
	protected void onRequestLoadingStart() {
		events.onLoadingStart();
	}

	/**
	 * Called when loading finishes
	 *
	 * @param jso
	 */
	protected void onRequestFinished(JavaScriptObject jso) {
		events.onFinished(jso);
	}

	/**
	 * Called when error occured.
	 *
	 * @param jso
	 */
	protected void onRequestError(JavaScriptObject jso) {

		if (jso != null) {
			PerunError e = (PerunError) jso;
			session.getUiElements().setLogErrorText("Error while sending request: " + e.getName());
			if (!hidden) {
				// creates a alert box
				JsonErrorHandler.alertBox(e);
			}
			events.onError(e);
		} else {
			PerunError e = (PerunError) JsonUtils.parseJson("{\"errorId\":\"0\",\"name\":\"Cross-site request\",\"type\":\"" + WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteType() + "\",\"message\":\"" + WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteText() + "\"}").cast();
			session.getUiElements().setLogErrorText("Error while sending request: The response was null or cross-site request.");
			if (!hidden) {
				// creates a alert box
				JsonErrorHandler.alertBox(e);
			}
			events.onError(null);
		}

	}

	/**
	 * Set callback hidden (do not show error pop-up)
	 *
	 * @param hidden (true=hidden/false=show - default)
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}