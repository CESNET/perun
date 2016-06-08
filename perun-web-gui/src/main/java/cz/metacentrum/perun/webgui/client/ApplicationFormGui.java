package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.applicationresources.ApplicationFormLeftMenu;
import cz.metacentrum.perun.webgui.client.applicationresources.pages.ApplicationFormPage;
import cz.metacentrum.perun.webgui.client.applicationresources.pages.UsersApplicationsPage;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetGuiConfiguration;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.GetPerunPrincipal;
import cz.metacentrum.perun.webgui.json.groupsManager.GetMemberGroups;
import cz.metacentrum.perun.webgui.json.membersManager.GetMemberByUser;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationsForUser;
import cz.metacentrum.perun.webgui.json.registrarManager.Initialize;
import cz.metacentrum.perun.webgui.json.registrarManager.ValidateEmail;
import cz.metacentrum.perun.webgui.json.registrarManager.VerifyCaptcha;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.testtabs.TestRtReportingTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.recaptcha.RecaptchaWidget;

import java.util.ArrayList;

/**
 * GUI available from ApplicationForm.html
 * It's a GUI for application forms.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class ApplicationFormGui implements EntryPoint {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * VO
	 */
	private static VirtualOrganization vo;
	private static Group group;
	private String voName = null;
	private String groupName = null;
	private HTML voContact = new HTML("");

	/**
	 * Left menu
	 */
	private static ApplicationFormLeftMenu leftMenu;

	/**
	 * Main content panel
	 */
	private static ScrollPanel contentPanel = new ScrollPanel();
	private static DockLayoutPanel bodySplitter = new DockLayoutPanel(Style.Unit.PX);
	private static FlexTable ft = new FlexTable();
	private PopupPanel loadingBox;

	/**
	 * Main class
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

		// basic settings
		session.setUiElements(new UiElements(null));

		// Get web page's BODY
		RootLayoutPanel body = RootLayoutPanel.get();

		// check RPC url
		if(session.getRpcUrl().isEmpty()){
			VerticalPanel bodyContents = new VerticalPanel();
			bodyContents.setSize("100%", "300px");
			bodyContents.add(new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>RPC SERVER NOT FOUND!</h2>"));
			bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(0), HasHorizontalAlignment.ALIGN_CENTER);
			bodyContents.setCellVerticalAlignment(bodyContents.getWidget(0), HasVerticalAlignment.ALIGN_BOTTOM);
			body.add(bodyContents);
			return;
		}

		// WEB PAGE SPLITTER
		bodySplitter.getElement().setId("appFormGUI");
		body.add(bodySplitter);

		// left menu
		leftMenu = new ApplicationFormLeftMenu();

		// show loading box
		loadingBox = session.getUiElements().perunLoadingBox();
		loadingBox.show();

		// switch menu event
		JsonCallbackEvents events = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {

				bodySplitter.clear();
				bodySplitter.addSouth(getFooter(), 23);
				ArrayList<Application> apps = JsonUtils.jsoAsList(jso);
				if (apps != null && !apps.isEmpty()) {
					// show menu
					bodySplitter.addWest(leftMenu, 280);
				}
				// else don't show menu
				// MAIN CONTENT
				contentPanel.setSize("100%", "100%");
				contentPanel.add(leftMenu.getContent());
				bodySplitter.add(contentPanel);

				// Append more GUI elements from UiElements class which are not part of splitted design
				// WE DON'T WANT TO CONFUSE USER WITH STATUS MESSAGES
				//bodySplitter.getElement().appendChild(session.getUiElements().getStatus().getElement()); // status

				// starts loading
				isUserMemberOfVo();

				// hides the loading box
				loadingBox.hide();

			}
			@Override
			public void onError(PerunError error) {
				// MAIN CONTENT

				bodySplitter.clear();
				bodySplitter.addSouth(getFooter(), 23);
				contentPanel.clear();
				contentPanel.setSize("100%", "100%");
				contentPanel.add(leftMenu.getContent());
				bodySplitter.add(contentPanel);

				// Append more GUI elements from UiElements class which are not part of splitted design
				//bodySplitter.getElement().appendChild(session.getUiElements().getStatus().getElement()); // status

				// starts loading
				isUserMemberOfVo();

				// hides the loading box
				loadingBox.hide();

			}
		};

		// load VO to check if exists
		loadVo(events);

	}

	/**
	 * Loads the VO by the parameter
	 */
	public void loadVo(final JsonCallbackEvents events) {

		voName = Location.getParameter("vo");
		groupName = Location.getParameter("group");

		Initialize req = new Initialize(voName, groupName, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso){

				JsArray<Attribute> list = JsonUtils.jsoAsArray(jso);

				// recreate VO and group
				vo = new JSONObject().getJavaScriptObject().cast();

				if (groupName != null && !groupName.isEmpty()) {
					group = new JSONObject().getJavaScriptObject().cast();
				}

				for (int i=0; i<list.length(); i++) {

					Attribute a = list.get(i);

					if (a.getFriendlyName().equalsIgnoreCase("id")) {
						if (a.getNamespace().equalsIgnoreCase("urn:perun:vo:attribute-def:core")) {
							vo.setId(Integer.parseInt(a.getValue()));
							if (group != null) {
								group.setVoId(Integer.parseInt(a.getValue()));
							}
						} else if (a.getNamespace().equalsIgnoreCase("urn:perun:group:attribute-def:core")) {
							group.setId(Integer.parseInt(a.getValue()));
						}
					} else if (a.getFriendlyName().equalsIgnoreCase("name")) {
						if (a.getNamespace().equalsIgnoreCase("urn:perun:vo:attribute-def:core")) {
							vo.setName(a.getValue());
						} else if (a.getNamespace().equalsIgnoreCase("urn:perun:group:attribute-def:core")) {
							group.setName(a.getValue());
						}
					} else if (a.getFriendlyName().equalsIgnoreCase("shortName")) {
						if (a.getNamespace().equalsIgnoreCase("urn:perun:vo:attribute-def:core")) {
							vo.setShortName(a.getValue());
						}
					} else if (a.getFriendlyName().equalsIgnoreCase("description")) {
						if (a.getNamespace().equalsIgnoreCase("urn:perun:group:attribute-def:core")) {
							group.setDescription(a.getValue());
						}
					} else if (a.getFriendlyName().equalsIgnoreCase("contactEmail")) {
						if (a.getNamespace().equalsIgnoreCase("urn:perun:vo:attribute-def:def")) {
							// set contact email
							for (int n=0; n<a.getValueAsJsArray().length(); n++) {
								SafeHtmlBuilder s = new SafeHtmlBuilder();
								if (n>0) {
									//others
									s.appendHtmlConstant(voContact.getHTML().concat(", <a href=\"mailto:" + a.getValueAsJsArray().get(n) + "\">" + a.getValueAsJsArray().get(n) + "</a>"));
								} else {
									// first
									s.appendHtmlConstant(voContact.getHTML().concat("<a href=\"mailto:" + a.getValueAsJsArray().get(n) + "\">" + a.getValueAsJsArray().get(n) + "</a>"));
								}

								voContact.setHTML(s.toSafeHtml());
							}
						}
					}


				}
				// store attrs
				vo.setAttributes(list);

				loadPerunPrincipal(events);

			}
			@Override
			public void onError(PerunError error) {

				// hides the loading box
				loadingBox.hide();

				RootLayoutPanel panel = RootLayoutPanel.get();
				panel.clear();
				panel.add(getErrorWidget(error));

			}
		});

		req.setHidden(true);
		req.retrieveData();

	}

	/**
	 * Performs a login into the RPC, loads user and his roles into session and enables GUI.
	 */
	private void loadPerunPrincipal(final JsonCallbackEvents externalEvents) {

		// events after getting PerunPrincipal from RPC
		GetPerunPrincipal principal = new GetPerunPrincipal(new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {

				// store perun principal into session for future use
				PerunPrincipal pp = (PerunPrincipal)jso;
				session.setPerunPrincipal(pp);

				// store users roles and editable entities into session
				if (pp.getRoles().hasAnyRole()) {
					session.setRoles(pp.getRoles());
				}

				// proceed after GUI configuration is loaded
				GetGuiConfiguration getConf = new GetGuiConfiguration(new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {

						// store configuration
						session.setConfiguration((BasicOverlayType)jso.cast());

						if (Utils.getVosToSkipCaptchaFor().contains(vo.getShortName())) {

							// skip captcha
							final GetApplicationsForUser request;
							if (session.getUser() == null) {
								// if not yet user in perun, search by actor / extSourceName
								request = new GetApplicationsForUser(0, externalEvents);
							} else {
								// if user in perun
								request = new GetApplicationsForUser(session.getUser().getId(), externalEvents);
							}
							request.retrieveData();

							// finish loading GUI
							loadingBox.hide();
							bodySplitter.clear();
							bodySplitter.add(ft);

							// challange captcha only for default URL (non)
						} else if (session.getRpcUrl().startsWith("/non/rpc")) {

							// IF VALIDATION LINK

							if (Location.getParameterMap().keySet().contains("m") &&
									Location.getParameterMap().keySet().contains("i")) {

								// passed params doesn't matter, different UI is loaded.
								final GetApplicationsForUser request;
								if (session.getUser() == null) {
									// if not yet user in perun, search by actor / extSourceName
									request = new GetApplicationsForUser(0, externalEvents);
								} else {
									// if user in perun
									request = new GetApplicationsForUser(session.getUser().getId(), externalEvents);
								}
								request.retrieveData();

								// finish loading GUI
								loadingBox.hide();
								bodySplitter.clear();
								bodySplitter.add(ft);

							} else {

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
									UiElements.generateError(error, "Missing public key", "Public key for Re-Captcha service is missing.<br />Accessing application form without authorization is not possible.");
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

													// Authorized anonymous user
													session.getUiElements().setLogText("Auth OK");

													final GetApplicationsForUser request;
													if (session.getUser() == null) {
														// if not yet user in perun, search by actor / extSourceName
														request = new GetApplicationsForUser(0, externalEvents);
													} else {
														// if user in perun
														request = new GetApplicationsForUser(session.getUser().getId(), externalEvents);
													}
													request.retrieveData();

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

								// display VO logo if present in attribute
								for (int i = 0; i < vo.getAttributes().length(); i++) {
									if (vo.getAttributes().get(i).getFriendlyName().equalsIgnoreCase("voLogoURL")) {
										ft.setWidget(row, 0, new Image(vo.getAttributes().get(i).getValue()));
										ft.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
										row++;
									}
								}

								ft.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
								ft.setHTML(row, 0, ApplicationMessages.INSTANCE.captchaDescription());
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
								bodySplitter.clear();
								bodySplitter.add(ft);

							}

						} else {

							// Authorized known user
							session.getUiElements().setLogText("Auth OK");

							final GetApplicationsForUser req;
							if (session.getUser() == null) {
								// if not yet user in perun, search by actor / extSourceName
								req = new GetApplicationsForUser(0, externalEvents);
							} else {
								// if user in perun
								req = new GetApplicationsForUser(session.getUser().getId(), externalEvents);
							}
							req.retrieveData();

						}

					}
				});
				getConf.retrieveData();

			}
		});
		principal.retrieveData();

	}


	private void isUserMemberOfVo() {

		// CHECK USER IF PRESENT
		if(session.getUser() != null) {

			GetMemberByUser req = new GetMemberByUser(vo.getId(), session.getUser().getId(), new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {

					Member member = jso.cast();
					if (member.getVoId() == vo.getId()) {

						// USER IS MEMBER OF VO
						if (groupName != null && !groupName.isEmpty()) {

							GetMemberGroups call = new GetMemberGroups(member.getId(), new JsonCallbackEvents(){
								@Override
								public void onFinished(JavaScriptObject jso) {

									ArrayList<Group> groups = JsonUtils.jsoAsList(jso);
									for (Group g : groups) {
										if (g.getId() == group.getId()) {
											// USER IS MEMBER OF GROUP
											prepareGui(PerunEntity.GROUP, "EXTENSION");
											return;
										}
									}
									// USER IS NOT MEMBER OF GROUP
									prepareGui(PerunEntity.GROUP, "INITIAL");
								}
								@Override
								public void onError(PerunError error) {

									RootLayoutPanel panel = RootLayoutPanel.get();
									panel.clear();
									panel.add(getErrorWidget(error));

								}
							});
							call.retrieveData();

						} else {
							// only VO application
							prepareGui(PerunEntity.VIRTUAL_ORGANIZATION, "EXTENSION");
						}
					} else {

						// TODO display error ? - retrieved member is not member of VO ??

					}
				}

				public void onError(PerunError error) {

					// not member of VO - load initial
					if (error.getName().equalsIgnoreCase("MemberNotExistsException")) {
						if (groupName != null && !groupName.isEmpty()) {

							// load application to group for NOT vo members
							prepareGui(PerunEntity.GROUP, "INITIAL");

							// Do NOT display application to Group if not member of VO
							//RootLayoutPanel panel = RootLayoutPanel.get();
							//panel.clear();
							//panel.add(getCustomErrorWidget(error, ApplicationMessages.INSTANCE.mustBeVoMemberFirst()));

						} else {
							prepareGui(PerunEntity.VIRTUAL_ORGANIZATION, "INITIAL");
						}
					} else {

						RootLayoutPanel panel = RootLayoutPanel.get();
						panel.clear();
						panel.add(getErrorWidget(error));

					}

				}

			});
			req.setHidden(true);
			req.retrieveData();
			return;

		}

		// UNKNOWN USER - LOAD INITIAL
		if (groupName != null && !groupName.isEmpty()) {
			prepareGui(PerunEntity.GROUP, "INITIAL");
		} else {
			prepareGui(PerunEntity.VIRTUAL_ORGANIZATION, "INITIAL");
		}

		return;

	}

	/**
	 * Prepares the GUI
	 * @param entity PerunEntity GROUP or VO
	 * @param applicationType INITIAL | EXTENSION
	 */
	protected void prepareGui(PerunEntity entity, String applicationType) {

		// trigger email verification as first if present in URL

		if (Location.getParameterMap().keySet().contains("m") &&
				Location.getParameterMap().keySet().contains("i")) {

			String verifyI = Location.getParameter("i");
			String verifyM = Location.getParameter("m");

			if (verifyI != null && !verifyI.isEmpty() &&
					verifyM != null && !verifyM.isEmpty()) {

				final SimplePanel verifContent = new SimplePanel();
				Anchor a = leftMenu.addItem(ApplicationMessages.INSTANCE.emailValidationMenuItem(), SmallIcons.INSTANCE.emailIcon(), verifContent);
				a.fireEvent(new ClickEvent(){});

				ValidateEmail request = new ValidateEmail(verifyI, verifyM, new JsonCallbackEvents(){
					@Override
					public void onLoadingStart() {
						verifContent.clear();
						verifContent.add(new AjaxLoaderImage());
					}
					@Override
					public void onFinished(JavaScriptObject jso) {

						BasicOverlayType obj = jso.cast();
						if (obj.getBoolean()==true) {

							verifContent.clear();
							FlexTable ft = new FlexTable();
							ft.setSize("100%", "300px");
							ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.acceptIcon())+"<h2>"+ApplicationMessages.INSTANCE.emailValidationSuccess()+"</h2>");
							ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
							ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
							verifContent.add(ft);

						} else {

							verifContent.clear();
							FlexTable ft = new FlexTable();
							ft.setSize("100%", "300px");
							ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.deleteIcon())+"<h2>"+ApplicationMessages.INSTANCE.emailValidationFail()+"</h2>");
							ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
							ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
							verifContent.add(ft);

						}
					}
					@Override
					public void onError(PerunError error) {
						((AjaxLoaderImage)verifContent.getWidget()).loadingError(error);
					}
				});
				request.retrieveData();

				leftMenu.addLogoutItem();

				return;

			}

		}

		// group and extension is not allowed
		if (group != null && applicationType.equalsIgnoreCase("EXTENSION")) {

			RootLayoutPanel panel = RootLayoutPanel.get();
			panel.clear();
			FlexTable ft = new FlexTable();
			ft.setSize("100%", "300px");
			ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Error: "+ApplicationMessages.INSTANCE.groupMembershipCantBeExtended(group.getName())+"</h2>");
			ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
			ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
			panel.add(ft);

			// redirect if possible
			if (Location.getParameter("targetexisting") != null) {
				Location.replace(Location.getParameter("targetexisting"));
			}

			return;

		}

		// application form page
		ApplicationFormPage formPage = new ApplicationFormPage(vo, group, applicationType);
		// even user "not yet in perun" can have some applications sent (therefore display by session info)
		UsersApplicationsPage appsPage = new UsersApplicationsPage();

		// if rt test
		if ("true".equals(Location.getParameter("rttest"))) {
			TestRtReportingTabItem tabItem = new TestRtReportingTabItem();
			Widget rtTab = tabItem.draw();
			leftMenu.addItem("RT test", SmallIcons.INSTANCE.settingToolsIcon(), rtTab);
		}

		// proper menu text
		String appMenuText = ApplicationMessages.INSTANCE.applicationFormForVo(vo.getName());
		if (group != null) {
			appMenuText = ApplicationMessages.INSTANCE.applicationFormForGroup(group.getName());
		}
		if (applicationType.equalsIgnoreCase("EXTENSION")) {
			appMenuText = ApplicationMessages.INSTANCE.membershipExtensionForVo(vo.getName());
			if (group != null) {
				appMenuText = ApplicationMessages.INSTANCE.membershipExtensionForGroup(group.getName());
			}
		}

		// load list of applications first if param in session
		if ("apps".equals(Location.getParameter("page"))) {
			Anchor a = leftMenu.addItem(ApplicationMessages.INSTANCE.applications(), SmallIcons.INSTANCE.applicationFromStorageIcon(), appsPage);
			leftMenu.addItem(appMenuText, SmallIcons.INSTANCE.applicationFormIcon(), formPage);
			a.fireEvent(new ClickEvent(){});
			//appsPage.menuClick(); // load list of apps
		} else {
			Anchor a = leftMenu.addItem(appMenuText, SmallIcons.INSTANCE.applicationFormIcon(), formPage);
			leftMenu.addItem(ApplicationMessages.INSTANCE.applications(), SmallIcons.INSTANCE.applicationFromStorageIcon(), appsPage);
			a.fireEvent(new ClickEvent(){});
			//formPage.menuClick(); // load application form
		}

		leftMenu.addLogoutItem();

	}

	private FlexTable getErrorWidget(PerunError error) {

		String text = "Request timeout exceeded.";
		String errorInfo = "";
		if (error != null) {
			if (error.getName().equalsIgnoreCase("VoNotExistsException")){
				text = "Virtual organization with such name doesn't exists. Please check URL and try again.";
			} else if (error.getName().equalsIgnoreCase("GroupNotExistsException")){
				text = "Group with such name doesn't exists. Please check URL and try again.";
			} else {
				text = "Error: "+error.getName();
			}
			errorInfo = error.getErrorInfo();
		}


		FlexTable ft = new FlexTable();
		ft.setSize("100%", "300px");
		ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>" + text + "</h2>");
		ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_BOTTOM);

		ft.setHTML(1, 0, "<p>"+errorInfo);
		ft.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
		ft.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);

		return ft;

	}

	private FlexTable getCustomErrorWidget(PerunError error, String customText) {

		FlexTable ft = new FlexTable();
		ft.setSize("100%", "300px");
		ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Error: </h2>" + customText);
		ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);

		return ft;

	}

	private FlexTable getFooter() {

		ft = new FlexTable();
		ft.addStyleName("perunFooter");

		FlexTable.FlexCellFormatter ftf = ft.getFlexCellFormatter();

		if (!voContact.getHTML().isEmpty()) {
			// show only if any contact is present
			voContact.setHTML("<strong>"+ApplicationMessages.INSTANCE.supportContact()+"</strong> "+voContact.getHTML());
		}

		ft.setWidget(0, 0, voContact);
		ft.setWidget(0, 1, new HTML(PerunWebConstants.INSTANCE.footerPerunCopyright() + " " + JsonUtils.getCurrentYear()));

		ftf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
		ftf.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		ftf.getElement(0, 1).setAttribute("style", "text-wrap: avoid;");
		ftf.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		ftf.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

		return ft;

	}

	/**
	 * Redraws app for GUI (with menu)
	 */
	public static void redrawGuiWithMenu() {

		bodySplitter.clear();
		bodySplitter.addSouth(ft, 23);
		bodySplitter.addWest(leftMenu, 280);
		bodySplitter.add(contentPanel);
		leftMenu.setHeight("100%");
		contentPanel.setHeight("100%");

	}

	public static VirtualOrganization getVo() {
		return vo;
	}

	public static Group getGroup() {
		return group;
	}

}