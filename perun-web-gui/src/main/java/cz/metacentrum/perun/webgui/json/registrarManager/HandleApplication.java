package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonErrorHandler;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.Identity;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;

import java.util.ArrayList;

/**
 * Common request, which handles applications (VERIFY, REJECT, APPROVE, DELETE)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class HandleApplication {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();

	// URL to call
	final String JSON_URL_VERIFY = "registrarManager/verifyApplication";
	final String JSON_URL_APPROVE = "registrarManager/approveApplication";
	final String JSON_URL_REJECT = "registrarManager/rejectApplication";
	final String JSON_URL_DELETE = "registrarManager/deleteApplication";
	final String JSON_URL_CHECK = "registrarManager/canBeApproved";

	// custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// application form
	private int appId = 0;

	private String reason;

	/**
	 * Creates a new request
	 */
	public HandleApplication() {}

	/**
	 * Creates a new request with custom events
	 *
	 * @param events Custom events
	 */
	public HandleApplication(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Verify application
	 *
	 * @param appId
	 */
	public void verifyApplication(int appId) {

		this.appId = appId;

		// test arguments
		if(!this.testApplication()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Verifying application failed.");
				events.onError(error);
			};
			@Override
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application verified.");
				events.onFinished(jso);
			};
			@Override
			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL_VERIFY, prepareJSONObject());

	}

	/**
	 * Reject application
	 *
	 * @param appId
	 * @param reason
	 */
	public void rejectApplication(int appId, String reason) {

		this.appId = appId;
		this.reason = reason;

		// test arguments
		if(!this.testApplication()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Rejecting application failed.");
				events.onError(error);
			};

			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application rejected.");
				events.onFinished(jso);
			};

			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL_REJECT, prepareJSONObject());

	}

	/**
	 * Approve application
	 *
	 * @param app
	 */
	public void approveApplication(final Application app) {

		this.appId = app.getId();

		// test arguments
		if(!this.testApplication()){
			return;
		}

		// new events
		final JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Approving application failed.");
				events.onError(error);
			}
			@Override
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application approved.");
				events.onFinished(jso);
			}
			@Override
			public void onLoadingStart() {
				events.onLoadingStart();
			}
		};

		final JSONObject appIdObj = new JSONObject();
		appIdObj.put("appId", new JSONNumber(appId));

		final JSONObject idObj = new JSONObject();
		idObj.put("id", new JSONNumber(appId));

		// check if can be approved

		JsonPostClient jspc = new JsonPostClient(new JsonCallbackEvents(){
			@Override
			public void onError(final PerunError error) {

				session.getUiElements().setLogErrorText("Checking approval failed.");
				events.onError(error);

				if (error == null ) {
					PerunError e = (PerunError) JsonUtils.parseJson("{\"errorId\":\"0\",\"name\":\"Cross-site request\",\"type\":\"" + WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteType() + "\",\"message\":\"" + WidgetTranslation.INSTANCE.jsonClientAlertBoxErrorCrossSiteText() + "\"}").cast();
					JsonErrorHandler.alertBox(e);
				} else if (!error.getName().equals("CantBeApprovedException")) {
					JsonErrorHandler.alertBox(error);
				} else {

					FlexTable layout = new FlexTable();

					layout.setWidget(0, 0, new HTML("<p>" + new Image(LargeIcons.INSTANCE.errorIcon())));

					if ("NOT_ACADEMIC".equals(error.getReason())) {
						layout.setHTML(0, 1, "<p>User is not active academia member and application shouldn't be approved.<p><b>LoA:</b> " + SafeHtmlUtils.fromString(app.getExtSourceLoa()+"").asString() +
								"</br><b>IdP category:</b> " + (!(error.getCategory().equals("")) ? SafeHtmlUtils.fromString(error.getCategory()).asString() : "N/A") +
								"</br><b>Affiliation:</b> " + (!(error.getAffiliation().equals("")) ? SafeHtmlUtils.fromString(error.getAffiliation().replace(";", ", ")).asString() : "N/A") +
								((error.isSoft()) ? "<p>You can try to override above restriction by clicking 'Approve anyway' button." : ""));
					} else {
						layout.setHTML(0, 1, "<p>" + SafeHtmlUtils.fromString(error.getErrorInfo()).asString() + ((error.isSoft()) ? "<p>You can try to override above restriction by clicking 'Approve anyway' button." : ""));
					}

					layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
					layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
					layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

					if (error.isSoft()) {

						Confirm c = new Confirm("Application shouldn't be approved", layout, new ClickHandler() {
							@Override
							public void onClick(ClickEvent clickEvent) {
									// ok approve sending data
									JsonPostClient jspc = new JsonPostClient(newEvents);
									jspc.sendData(JSON_URL_APPROVE, prepareJSONObject());
							}
						}, new ClickHandler() {
							@Override
							public void onClick(ClickEvent clickEvent) {
								events.onFinished(null);
							}
						}, true);
						c.setOkButtonText("Approve anyway");
						c.setNonScrollable(true);
						c.show();

					} else {

						Confirm c = new Confirm("Application can't be approved", layout, true);
						c.setNonScrollable(true);
						c.show();

					}

				}

			}
			@Override
			public void onFinished(JavaScriptObject jso) {

				// check for similar users before approving

				if (app.getUser() != null) {

					// ok approve sending data
					JsonPostClient jspc2 = new JsonPostClient(newEvents);
					jspc2.sendData(JSON_URL_APPROVE, prepareJSONObject());

				} else {

					JsonPostClient checkJspc = new JsonPostClient(new JsonCallbackEvents(){
						@Override
						public void onError(PerunError error) {
							session.getUiElements().setLogErrorText("Approving application failed.");
							events.onError(error);
						}
						@Override
						public void onFinished(JavaScriptObject jso) {

							ArrayList<Identity> users = JsonUtils.jsoAsList(jso);
							if (users != null && !users.isEmpty()) {

								FlexTable ft = new FlexTable();
								ft.setWidth("600px");
								ft.setHTML(0, 0, "<p><strong>Following similar user(s) were found in system:");
								ft.getFlexCellFormatter().setColSpan(0, 0, 3);

								ft.setHTML(1, 0, "<strong>" + ApplicationMessages.INSTANCE.name() + "</strong>");
								ft.setHTML(1, 1, "<strong>" + ApplicationMessages.INSTANCE.email() +"</strong>");
								ft.setHTML(1, 2, "<strong>" + ApplicationMessages.INSTANCE.organization() +"</strong>");

								int i = 2;

								for (Identity user : users) {

									ft.setHTML(i, 0, SafeHtmlUtils.fromString(user.getName()).asString());

									if (user.getEmail() != null && !user.getEmail().isEmpty()) {
										ft.setHTML(i, 1, SafeHtmlUtils.fromString(user.getEmail()).asString());
									} else {
										ft.setHTML(i, 1, "N/A");
									}

									if (user.getOrganization() != null && !user.getOrganization().isEmpty()) {
										ft.setHTML(i, 2, SafeHtmlUtils.fromString(user.getOrganization()).asString());
									} else {
										ft.setHTML(i, 2, "N/A");
									}

									i++;

								}

								String type = "";
								if (app.getExtSourceType().equals("cz.metacentrum.perun.core.impl.ExtSourceX509")) {
									type = "cert";
								} else if (app.getExtSourceType().equals("cz.metacentrum.perun.core.impl.ExtSourceIdp")) {
									type = "fed";
								}

								ft.setHTML(i, 0,  "<p>Please contact new applicant with question, if he/she isn't already member of any other VO." +
										"<ul><li>If YES, ask him/her to join identities at <a href=\""+Utils.getIdentityConsolidatorLink(type, false)+"\" target=\"_blank\">identity consolidator</a> before approving this application."+
										"</li><li>If NO, you can approve this application anyway. " +
										"</li><li>If UNSURE, contact <a href=\"mailto:"+ Utils.perunReportEmailAddress()+"\">support</a> to help you.</li></ul>");

								ft.getFlexCellFormatter().setColSpan(i, 0, 3);

								i++;

								ft.setHTML(i, 0, "<strong>Do you wish to approve this application anyway ?</strong>");
								ft.getFlexCellFormatter().setColSpan(i, 0, 3);

								Confirm c = new Confirm("Similar users found!", ft, new ClickHandler() {
									@Override
									public void onClick(ClickEvent clickEvent) {

										// ok approve sending data
										JsonPostClient jspc = new JsonPostClient(newEvents);
										jspc.sendData(JSON_URL_APPROVE, prepareJSONObject());

									}
								}, new ClickHandler() {
									@Override
									public void onClick(ClickEvent clickEvent) {
										events.onFinished(null);
									}
								}, true);

								c.setOkButtonText("Approve");

								c.setNonScrollable(true);
								c.show();

							} else {

								// ok approve sending data
								JsonPostClient jspc = new JsonPostClient(newEvents);
								jspc.sendData(JSON_URL_APPROVE, prepareJSONObject());

							}

						}
						@Override
						public void onLoadingStart() {
							events.onLoadingStart();
						}
					});
					checkJspc.sendData("registrarManager/checkForSimilarUsers", appIdObj);

				}

			}
			@Override
			public void onLoadingStart() {
				events.onLoadingStart();
			}
		});
		// we have own error handling
		jspc.setHidden(true);
		jspc.sendData(JSON_URL_CHECK, idObj);

	}

	/**
	 * Delete application
	 *
	 * @param appId
	 */
	public void deleteApplication(int appId) {

		this.appId = appId;

		// test arguments
		if(!this.testApplication()){
			return;
		}

		// new events
		JsonCallbackEvents newEvents = new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Deleting application failed.");
				events.onError(error);
			};
			@Override
			public void onFinished(JavaScriptObject jso) {
				session.getUiElements().setLogSuccessText("Application deleted.");
				events.onFinished(jso);
			};
			@Override
			public void onLoadingStart() {
				events.onLoadingStart();
			};
		};

		// sending data
		JsonPostClient jspc = new JsonPostClient(newEvents);
		jspc.sendData(JSON_URL_DELETE, prepareJSONObject());

	}


	private boolean testApplication() {

		boolean result = true;
		String errorMsg = "";

		if(appId == 0){
			errorMsg += "Application ID can't be 0.";
			result = false;
		}

		if(errorMsg.length()>0){
			UiElements.generateAlert("Parameter error", errorMsg);
		}

		return result;

	}

	/**
	 * Prepares a JSON object.
	 * @return JSONObject - the whole query
	 */
	private JSONObject prepareJSONObject() {
		// query
		JSONObject query = new JSONObject();
		query.put("id", new JSONNumber(appId));

		if (reason != null) {
			query.put("reason", new JSONString(reason));
		}

		return query;
	}

}
