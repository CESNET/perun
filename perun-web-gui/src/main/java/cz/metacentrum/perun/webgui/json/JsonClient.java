package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for GET requests to RPC.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class JsonClient {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Custom events - onFinished, onError
	 */
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Hidden callback do not show error pop-up
	 */
	private boolean hidden = false;

	private boolean isCacheEnabled = false;

	private Map<String, JavaScriptObject> cache = new HashMap<String, JavaScriptObject>();
	private Map<String, PerunRequest> runningRequests = new HashMap<>();

	/**
	 * New JsonPostClient
	 */
	public JsonClient() {
	}

	/**
	 * New JsonPostClient
	 *
	 * @param events
	 */
	public JsonClient(JsonCallbackEvents events) {
		this.events = events;
	}


	public boolean isCacheEnabled() {
		return isCacheEnabled;
	}

	public void setCacheEnabled(boolean isCacheEnabled) {
		this.isCacheEnabled = isCacheEnabled;
	}

	/**
	 * Sends the data
	 */
	public void retrieveData(String url, JsonCallback callback) {
		retrieveData(url, "", callback);
	}

	/**
	 * Sends the data
	 */
	public void retrieveData(String url, String params, final JsonCallback callback) {

		// create events
		if (callback != null) {
			this.events = new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					callback.onFinished(jso);
				}

				@Override
				public void onError(PerunError error) {
					callback.onError(error);
				}

				@Override
				public void onLoadingStart() {
					callback.onLoadingStart();
				}
			};
		}

		final PerunRequest perunRequest = new JSONObject().getJavaScriptObject().cast();
		perunRequest.setStartTime();

		// url to call
		final String requestUrl = URL.encode(PerunWebSession.getInstance().getRpcUrl() + url + "?callback=" + perunRequest.getStartTime() + "&" + params);

		perunRequest.setManager(url.split("\\/")[0]);
		perunRequest.setMethod(url.split("\\/")[1]);
		perunRequest.setParamString("?callback=" + perunRequest.getStartTime() + "&" + params);

		// request building
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, requestUrl);
		try {

			// sends the request
			onRequestLoadingStart();

			if (isCacheEnabled) {
				if (cache.get(requestUrl) != null) {

					// jso
					JavaScriptObject jso = cache.get(requestUrl);

					// Return DATA if not error, otherwise start new call
					PerunError error = (PerunError) jso;
					if ("".equalsIgnoreCase(error.getErrorId()) && "".equalsIgnoreCase(error.getErrorInfo())) {
						// not error, OK
						if (session.getUiElements() != null) {
							session.getUiElements().setLogText("Response not NULL, not ERROR.");
						}
						onRequestFinished(jso);
						return;
					}

				}
			}

			final Request request = builder.sendRequest("", new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {

					// if response = OK
					if (resp.getStatusCode() == 200) {

						// jso
						JavaScriptObject jso = parseResponse(perunRequest.getStartTime()+"", resp.getText());

						// if null - finished
						if (jso == null) {
							if (session.getUiElements() != null) {
								session.getUiElements().setLogText("Response NULL.");
							}
							runningRequests.remove(requestUrl);
							onRequestFinished(null);
							return;
						}

						// if error?
						PerunError error = (PerunError) jso;
						if ("".equalsIgnoreCase(error.getErrorId()) && "".equalsIgnoreCase(error.getErrorInfo())) {

							// not error, OK
							if (session.getUiElements() != null) {
								session.getUiElements().setLogText("Response not NULL, not ERROR.");
							}
							runningRequests.remove(requestUrl);
							onRequestFinished(jso);

							if (isCacheEnabled) {
								cache.put(requestUrl, jso);
							}

							return;
						}

						// triggers onError
						if (session.getUiElements() != null) {
							session.getUiElements().setLogText("Response ERROR.");
						}
						error.setRequest(perunRequest);
						error.setRequestURL(requestUrl);
						error.setPostData("");
						onRequestError(error);
						runningRequests.remove(requestUrl);
						return;

					} else {

						// if response not OK
						PerunError error = new JSONObject().getJavaScriptObject().cast();
						error.setErrorId(""+resp.getStatusCode());
						error.setName(resp.getStatusText());
						error.setErrorInfo("Server responded with HTTP error: " + resp.getStatusCode() + " - " + resp.getStatusText());
						error.setObjectType("PerunError");
						error.setPostData("");
						error.setRequestURL(requestUrl);
						error.setRequest(perunRequest);

						if (resp.getStatusCode() == 401 || resp.getStatusCode() == 403) {

							error.setErrorInfo("You are not authorized to server. Your session might have expired. Please refresh the browser window to re-login.");

						} else if (resp.getStatusCode() == 500) {

							if (runningRequests.get(requestUrl) != null) {

								if ((runningRequests.get(requestUrl).getDuration() / (1000 * 60)) >= 5) {
									// 5 minute timeout

									error.setName("Request Timeout");
									error.setErrorInfo("Your request couldn't be handled within 5 minutes. Please refresh your view (table) to try it again.");

								} else {

									error.setName("Server Internal Error");
									error.setErrorInfo("Server encounter internal error while processing your request. Please report this error and retry.");

								}

							}

						} else if (resp.getStatusCode() == 503) {

							error.setName("Server Temporarily Unavailable");
							error.setErrorInfo("Server is temporarily unavailable. Please try again later.");

						} else if (resp.getStatusCode() == 404) {

							error.setName("Not found");
							error.setErrorInfo("Server is probably being restarted at the moment. Please try again later.");

						} else if (resp.getStatusCode() == 0) {

							error.setName("Can't contact remote server.");
							error.setErrorInfo("You are either without network connection or your session has expired. Click \"OK\" to reload the page (all unsaved changes will be lost).");

							// force reload page if it's first GUI call, otherwise keep it to alert box
							if (!hidden && runningRequests.get(requestUrl).getManager().equals("authzResolver") &&
									runningRequests.get(requestUrl).getMethod().equals("getPerunPrincipal")) {
								Window.Location.reload();
							}

						}

						runningRequests.remove(requestUrl);
						onRequestError(error);
						return;

					}

				}

				@Override
				public void onError(Request req, Throwable exc) {
					// request not sent
					runningRequests.remove(requestUrl);
					onRequestError(parseResponse(perunRequest.getStartTime()+"", exc.toString()));
				}

			});

			runningRequests.put(requestUrl, perunRequest);

		} catch (RequestException exc) {
			// usually couldn't connect to server
			onRequestError(parseResponse(perunRequest.getStartTime()+"", exc.toString()));
		}

	}

	/**
	 * Parses the response to an object.
	 *
	 * @param resp JSON string
	 * @return
	 */
	protected JavaScriptObject parseResponse(String callbackName, String resp) {

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
	 * Called when error occur.
	 *
	 * @param jso
	 */
	protected void onRequestError(JavaScriptObject jso) {

		if (jso != null) {
			PerunError e = (PerunError) jso;
			if (session.getUiElements() != null) {
				session.getUiElements().setLogErrorText("Error while sending request: " + e.getName());
			}
			if (!hidden) {
				// creates a alert box
				JsonErrorHandler.alertBox(e);
			}
			events.onError(e);
		} else {
			PerunError e = (PerunError) JsonUtils.parseJson("{\"errorId\":\"0\",\"name\":\"Cross-site request\",\"type\":\"" + WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteType() + "\",\"message\":\"" + WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteText() + "\"}").cast();
			if (session.getUiElements() != null) {
				session.getUiElements().setLogErrorText("Error while sending request: The response was null or cross-site request.");
			}
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