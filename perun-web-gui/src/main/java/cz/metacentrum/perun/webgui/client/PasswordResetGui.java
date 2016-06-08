package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.*;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.passwordresetresources.PasswordResetFormPage;
import cz.metacentrum.perun.webgui.client.passwordresetresources.PasswordResetLeftMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetGuiConfiguration;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.authzResolver.GetPerunPrincipal;
import cz.metacentrum.perun.webgui.json.registrarManager.VerifyCaptcha;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunPrincipal;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.recaptcha.RecaptchaWidget;

/**
 * The main Password Reset GUI class. It's GWT Entry point.
 *
 * Loads whole GUI, makes login to RPC by calling GetPerunPrincipal
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PasswordResetGui implements EntryPoint {

	// web session
	private PerunWebSession session = PerunWebSession.getInstance();
	private PasswordResetLeftMenu leftMenu;

	/**
	 * Main content panel
	 */
	private ScrollPanel contentPanel = new ScrollPanel();

	/**
	 * This is ENTRY POINT method. It's called automatically when web page
	 * containing this GUI is loaded in browser.
	 */
	public void onModuleLoad() {

		ScriptInjector.fromUrl("https://www.google.com/recaptcha/api/js/recaptcha_ajax.js").setCallback(
				new Callback<Void, Exception>() {
					public void onFailure(Exception reason) {
						Window.alert("Script load failed.");
					}

					public void onSuccess(Void result) {
						loadModule();
					}
				}).setWindow(ScriptInjector.TOP_WINDOW).inject();
	}

	private void loadModule() {

		// perun web session
		session = PerunWebSession.getInstance();

		// basic settings
		session.setUiElements(new UiElements(null));

		// Get web page's BODY
		RootLayoutPanel body = RootLayoutPanel.get();
		body.setStyleName("mainPanel");

		// check RPC url
		if(session.getRpcUrl().isEmpty()){
			VerticalPanel bodyContents = new VerticalPanel();
			bodyContents.setSize("100%", "300px");
			bodyContents.add(new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>RPC SERVER NOT FOUND !</h2>"));
			bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(0), HasHorizontalAlignment.ALIGN_CENTER);
			bodyContents.setCellVerticalAlignment(bodyContents.getWidget(0), HasVerticalAlignment.ALIGN_BOTTOM);
			body.add(bodyContents);
			return;
		}

		// WEB PAGE SPLITTER
		DockLayoutPanel bodySplitter = new DockLayoutPanel(Unit.PX);
		body.add(bodySplitter);

		// left menu
		//leftMenu = new PasswordResetLeftMenu();
		// bodySplitter.addWest(leftMenu, 240);

		// MAIN CONTENT
		contentPanel.setSize("100%", "100%");
		//contentPanel.add(leftMenu.getContent());
		bodySplitter.add(contentPanel);

		loadPerunPrincipal();

	}

	/**
	 * Performs a login into the RPC, loads user and his roles into session and enables GUI.
	 */
	private void loadPerunPrincipal() {

		// show loading box
		final PopupPanel loadingBox = session.getUiElements().perunLoadingBox();
		loadingBox.show();

		// events after getting PerunPrincipal from RPC
		final JsonCallbackEvents events = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {

				// store perun principal into session for future use
				PerunPrincipal pp = (PerunPrincipal)jso;
				session.setPerunPrincipal(pp);

				// check if user exists
				if (session.getUser() != null && !pp.getRoles().hasAnyRole() && !session.getRpcUrl().startsWith("/non/rpc")) {
					// if not and no role, redraw page body
					RootLayoutPanel body = RootLayoutPanel.get();
					loadingBox.hide();
					body.clear();
					body.add(new NotUserOfPerunWidget());
					return;
				}

				if (session.getUser() != null && !pp.getRoles().hasAnyRole()) {

					// store users roles and editable entities into session
					session.setRoles(pp.getRoles());

					// display logged user
					session.getUiElements().setLoggedUserInfo(pp);

				}

				GetGuiConfiguration getConf = new GetGuiConfiguration(new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {

						session.setConfiguration((BasicOverlayType)jso.cast());

						// hides the loading box
						loadingBox.hide();

						if (session.getRpcUrl().startsWith("/non/rpc")) {

							// CHALLENGE WITH CAPTCHA

							FlexTable ft = new FlexTable();
							ft.setSize("100%", "500px");

							// captcha with public key
							String key = Utils.getReCaptchaPublicKey();
							if (key == null) {

								PerunError error = new JSONObject().getJavaScriptObject().cast();
								error.setErrorId("0");
								error.setName("Missing public key");
								error.setErrorInfo("Public key for Re-Captcha service is missing. Please add public key to GUIs configuration file.");
								error.setRequestURL("");
								UiElements.generateError(error, "Missing public key", "Public key for Re-Captcha service is missing.<br />Accessing password reset without authorization is not possible.");
								loadingBox.hide();
								return;
							}

							final RecaptchaWidget captcha = new RecaptchaWidget(key, LocaleInfo.getCurrentLocale().getLocaleName(), "clean");

							final CustomButton cb = new CustomButton();
							cb.setIcon(SmallIcons.INSTANCE.arrowRightIcon());
							cb.setText(ApplicationMessages.INSTANCE.captchaSendButton());
							cb.setImageAlign(true);

							final TextBox response = new TextBox();
							captcha.setOwnTextBox(response);

							Scheduler.get().scheduleDeferred(new Command() {
								@Override
								public void execute() {
									response.setFocus(true);
								}
							});

							response.addKeyDownHandler(new KeyDownHandler() {
								@Override
								public void onKeyDown(KeyDownEvent event) {
									if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
										cb.click();
									}
								}
							});

							cb.addClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent clickEvent) {
									VerifyCaptcha req = new VerifyCaptcha(captcha.getChallenge(), captcha.getResponse(), JsonCallbackEvents.disableButtonEvents(cb, new JsonCallbackEvents() {
										public void onFinished(JavaScriptObject jso) {

											BasicOverlayType bt = jso.cast();
											if (bt.getBoolean()) {

												// OK captcha answer - load GUI

												// add menu item and load content
												contentPanel.setWidget(new PasswordResetFormPage().getContent());
												//Anchor a = leftMenu.addMenuContents("Password reset", SmallIcons.INSTANCE.keyIcon(), new PasswordResetFormPage().getContent());
												//a.fireEvent(new ClickEvent(){});

											} else {
												// wrong captcha answer
												UiElements.generateAlert(ApplicationMessages.INSTANCE.captchaErrorHeader(), ApplicationMessages.INSTANCE.captchaErrorMessage());
											}
										}
									}));
									req.retrieveData();
								}
							});

							// set layout

							int row = 0;

							ft.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
							ft.setHTML(row, 0, "<h2>In order to continue to the password reset page, please, use CAPTCHA below.</h2>");
							ft.setWidget(row + 1, 0, captcha);
							ft.getFlexCellFormatter().setHorizontalAlignment(row + 1, 0, HasHorizontalAlignment.ALIGN_CENTER);
							ft.getFlexCellFormatter().setVerticalAlignment(row + 1, 0, HasVerticalAlignment.ALIGN_BOTTOM);

							FlexTable sendFt = new FlexTable();
							sendFt.setStyleName("inputFormFlexTable");

							sendFt.setWidget(0, 0, response);
							sendFt.setWidget(0, 1, cb);

							ft.setWidget(row + 2, 0, sendFt);
							ft.getFlexCellFormatter().setHorizontalAlignment(row + 2, 0, HasHorizontalAlignment.ALIGN_CENTER);
							ft.getFlexCellFormatter().setVerticalAlignment(row + 2, 0, HasVerticalAlignment.ALIGN_TOP);

							ft.setHeight("100%");
							ft.getFlexCellFormatter().setHeight(row, 0, "50%");
							ft.getFlexCellFormatter().setHeight(row + 2, 0, "50%");

							// finish loading GUI
							loadingBox.hide();
							contentPanel.setWidget(ft);

						} else {

							// add menu item and load content
							contentPanel.setWidget(new PasswordResetFormPage().getContent());
							//Anchor a = leftMenu.addMenuContents("Password reset", SmallIcons.INSTANCE.keyIcon(), new PasswordResetFormPage().getContent());
							//a.fireEvent(new ClickEvent(){});

						}

					}
				@Override
				public void onError(PerunError error){
					// hides the loading box
					loadingBox.hide();

					// shows error box
					PopupPanel loadingFailedBox;
					if (error == null) {
						loadingFailedBox = session.getUiElements().perunLoadingFailedBox("Request timeout exceeded.");
					} else {
						if (error.getName().contains("UserNotExistsException")) {
							loadingFailedBox = session.getUiElements().perunLoadingFailedBox("You are not registered to any Virtual Organization.</br></br>" + error.getErrorInfo());
						} else {
							loadingFailedBox = session.getUiElements().perunLoadingFailedBox(error.getErrorInfo());
						}
					}
					loadingFailedBox.show();

					leftMenu.addItem("Password reset", SmallIcons.INSTANCE.keyIcon(), null);

				}
				});
				getConf.retrieveData();

			}
			@Override
			public void onError(PerunError error){
				// hides the loading box
				loadingBox.hide();

				// shows error box
				PopupPanel loadingFailedBox;
				if (error == null) {
					loadingFailedBox = session.getUiElements().perunLoadingFailedBox("Request timeout exceeded.");
				} else {
					if (error.getName().contains("UserNotExistsException")) {
						loadingFailedBox = session.getUiElements().perunLoadingFailedBox("You are not registered to any Virtual Organization.</br></br>" + error.getErrorInfo());
					} else {
						loadingFailedBox = session.getUiElements().perunLoadingFailedBox(error.getErrorInfo());
					}
				}
				loadingFailedBox.show();

				leftMenu.addItem("Password reset", SmallIcons.INSTANCE.keyIcon(), null);

			}
		};
		GetPerunPrincipal loggedUserRequest = new GetPerunPrincipal(events);
		loggedUserRequest.retrieveData();
	}

}
