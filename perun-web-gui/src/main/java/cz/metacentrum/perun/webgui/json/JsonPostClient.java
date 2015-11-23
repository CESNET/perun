package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunRequest;
import cz.metacentrum.perun.webgui.widgets.Confirm;

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
	private static Confirm c = null;
	private static FlexTable layout = new FlexTable();
	private static int counter = 0;

	/**
	 * New JsonPostClient
	 */
	public JsonPostClient() {

		if (c == null) {

			layout.setWidget(0, 0, new HTML("<p>" + new Image(LargeIcons.INSTANCE.informationIcon())));
			layout.setHTML(0, 1, "<p>" + "Processing of your request(s) is taking longer than usual, but it's actively processed by the server.<p>Please do not close opened window/tab nor repeat your action. You will be notified once operation completes.<p>Remaining requests: " + counter);

			layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
			layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
			layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

			c = new Confirm("Pending request", layout, true);
			c.setNonScrollable(true);
			c.setAutoHide(false);

		}

	}


	/**
	 * New JsonPostClient
	 *
	 * @param events
	 */
	public JsonPostClient(JsonCallbackEvents events) {
		this();
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

		final PerunRequest perunRequest = new JSONObject().getJavaScriptObject().cast();
		perunRequest.setStartTime();

		// url to call
		final String requestUrl = URL.encode(PerunWebSession.getInstance().getRpcUrl() + url + "?callback=" + perunRequest.getStartTime());

		String data = "";
		if (sendNative) {
			data = json;
		} else {
			data = jsonObject.toString();
		}

		perunRequest.setManager(url.split("\\/")[0]);
		perunRequest.setMethod(url.split("\\/")[1]);
		perunRequest.setParamString("?callback=" + perunRequest.getStartTime() + "&" + data);

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
						JavaScriptObject jso = parseResponse(perunRequest.getStartTime() + "", resp.getText());

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
							return;
						}

						// triggers onError
						if (session.getUiElements() != null) {
							session.getUiElements().setLogText("Response ERROR.");
						}
						error.setRequestURL(requestUrl);
						error.setRequest(perunRequest);
						error.setPostData(payload);
						runningRequests.remove(requestUrl);
						onRequestError(error);
						return;

					} else {

						// if response not OK
						PerunError error = new JSONObject().getJavaScriptObject().cast();
						error.setErrorId("" + resp.getStatusCode());
						error.setName(resp.getStatusText());
						error.setErrorInfo("Server responded with HTTP error: " + resp.getStatusCode() + " - " + resp.getStatusText());
						error.setObjectType("PerunError");
						error.setPostData(payload);
						error.setRequest(perunRequest);
						error.setRequestURL(requestUrl);

						if (resp.getStatusCode() == 401 || resp.getStatusCode() == 403) {

							error.setName("Not Authorized");
							error.setErrorInfo("You are not authorized to server. Your session might have expired. Please refresh the browser window to re-login.");

						} else if (resp.getStatusCode() == 500) {

							if (runningRequests.get(requestUrl) != null) {

								// 5 minute timeout
								if ((runningRequests.get(requestUrl).getDuration() / (1000 * 60)) >= 5) {

									counter++;
									layout.setHTML(0, 1, "<p>" + "Processing of your request(s) is taking longer than usual, but it's actively processed by the server.<p>Please do not close opened window/tab nor repeat your action. You will be notified once operation completes.<p>Remaining requests: "+counter);

									if (!c.isShowing() && counter > 0) c.show();

									Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
										boolean again = true;

										@Override
										public boolean execute() {
											GetPendingRequests req = new GetPendingRequests(perunRequest.getStartTime(), new JsonCallbackEvents() {
												@Override
												public void onFinished(JavaScriptObject jso) {
													final PerunRequest req = jso.cast();
													if ((req.getCallbackId().equals(perunRequest.getStartTime() + "")) && req.getEndTime() > 0) {

														if (again) {

															again = false;
															counter--;
															layout.setHTML(0, 1, "<p>" + "Processing of your request(s) is taking longer than usual, but it's actively processed by the server.<p>Please do not close opened window/tab nor repeat your action. You will be notified once operation completes.<p>Remaining requests: "+counter);

															// hide notification
															if (c.isShowing() && counter <= 0) c.hide();

															JavaScriptObject result = req.getResult();

															// if null - finished
															if (result == null) {
																if (session.getUiElements() != null) {
																	session.getUiElements().setLogText("Response NULL.");
																}
																runningRequests.remove(requestUrl);
																onRequestFinished(null);
																return;
															}

															// if error?
															PerunError error = (PerunError) result;
															if ("".equalsIgnoreCase(error.getErrorId()) && "".equalsIgnoreCase(error.getErrorInfo())) {
																// not error, OK
																if (session.getUiElements() != null) {
																	session.getUiElements().setLogText("Response not NULL, not ERROR.");
																}
																runningRequests.remove(requestUrl);
																onRequestFinished(result);
																return;
															}

															// triggers onError
															if (session.getUiElements() != null) {
																session.getUiElements().setLogText("Response ERROR.");
															}
															error.setRequestURL(requestUrl);
															error.setRequest(perunRequest);
															error.setPostData(payload);
															runningRequests.remove(requestUrl);
															onRequestError(error);
															return;

														}
													}
												}
											});
											req.retrieveData();
											return again;
										}
									}, ((PerunWebConstants) GWT.create(PerunWebConstants.class)).pendingRequestsRefreshInterval());

									return;

								} else {

									error.setName("ServerInternalError");
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

							error.setName("Aborted");
							error.setErrorInfo("Can't contact remote server, connection was lost.");

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
	 * Parses the responce to an object.
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
	 * Called when error occurred.
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