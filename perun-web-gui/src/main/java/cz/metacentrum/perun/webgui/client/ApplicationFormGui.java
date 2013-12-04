package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.applicationresources.ApplicationFormLeftMenu;
import cz.metacentrum.perun.webgui.client.applicationresources.pages.ApplicationFormPage;
import cz.metacentrum.perun.webgui.client.applicationresources.pages.UsersApplicationsPage;
import cz.metacentrum.perun.webgui.client.applicationresources.pages.VoNotFoundPage;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
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
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.recaptcha.RecaptchaWidget;

import java.util.ArrayList;

/**
 * GUI available from ApplicationForm.html
 * It's a GUI for application forms.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
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
			
			public void onError(PerunError error)
			{
				voNotFound(error);
                // hides the loading box
                loadingBox.hide();
			}
		});
		
		req.setHidden(true);
		req.retrieveData();		
		
	}
	
	/**
	 * When VO not found, disable error
	 */
	public void voNotFound(PerunError error)
	{
		 RootLayoutPanel panel = RootLayoutPanel.get();
		 panel.clear();
		 panel.add(new VoNotFoundPage(error));
	}


    /**
     * Performs a login into the RPC, loads user and his roles into session and enables GUI.
     */
    private void loadPerunPrincipal(final JsonCallbackEvents externalEvents) {

        // events after getting PerunPrincipal from RPC
        final JsonCallbackEvents events = new JsonCallbackEvents() {

            public void onFinished(JavaScriptObject jso) {

                // store perun principal into session for future use
                PerunPrincipal pp = (PerunPrincipal)jso;
                session.setPerunPrincipal(pp);

                // store users roles and editable entities into session
                if (pp.getRoles().hasAnyRole()) {
                    session.setRoles(pp.getRoles());
                }

                // non authz user
                if (session.getRpcUrl().equalsIgnoreCase("/perun-rpc-non/jsonp/")) {

                    // CHALLENGE WITH CAPTCHA

                    FlexTable ft = new FlexTable();
                    ft.setSize("100%", "500px");

                    // captcha with public key
                    final RecaptchaWidget captcha = new RecaptchaWidget("6Lcbdt0SAAAAAGMnlJn57omFv1OCl3O-PbW0NrK7", LocaleInfo.getCurrentLocale().getLocaleName(), "clean");

                    cz.metacentrum.perun.webgui.widgets.CustomButton cb = new CustomButton();
                    cb.setIcon(SmallIcons.INSTANCE.arrowRightIcon());
                    cb.setText(ApplicationMessages.INSTANCE.captchaSendButton());
                    cb.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            VerifyCaptcha req = new VerifyCaptcha(captcha.getChallenge(), captcha.getResponse(), new JsonCallbackEvents(){
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
                                        Confirm c = new Confirm(ApplicationMessages.INSTANCE.captchaErrorHeader(), new HTML(ApplicationMessages.INSTANCE.captchaErrorMessage()),true);
                                        c.show();

                                    }
                                }
                            });
                            req.retrieveData();
                        }
                    });

                    // set layout

                    int row = 0;

                    // display VO logo if present in attribute
                    for (int i=0; i<vo.getAttributes().length(); i++) {
                        if (vo.getAttributes().get(i).getFriendlyName().equalsIgnoreCase("voLogoURL")) {
                            ft.setWidget(row, 0, new Image(vo.getAttributes().get(i).getValue()));
                            ft.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
                            row++;
                        }
                    }

                    ft.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
                    ft.setHTML(row, 0, ApplicationMessages.INSTANCE.captchaDescription());
                    ft.setWidget(row+1, 0, captcha);
                    ft.getFlexCellFormatter().setHorizontalAlignment(row+1, 0, HasHorizontalAlignment.ALIGN_CENTER);
                    ft.setWidget(row+2, 0, cb);
                    ft.getFlexCellFormatter().setHorizontalAlignment(row+2, 0, HasHorizontalAlignment.ALIGN_CENTER);


                    // finish loading GUI
                    loadingBox.hide();
                    bodySplitter.clear();
                    bodySplitter.add(ft);

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
        };
        GetPerunPrincipal loggedUserRequst = new GetPerunPrincipal(events);
        loggedUserRequst.retrieveData();
		
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
		String verifyI = "";
		String verifyM = "";
		
		String query = Location.getQueryString();
		
		String[] array = query.split("&");
		for (String item : array) {
			if (item.startsWith("i=")) {
				verifyI = item.substring(2);
			} else if (item.startsWith("?i=")) {
				verifyI = item.substring(3);				
			} else if (item.startsWith("m=")) {
				verifyM = item.substring(2);
			} else if (item.startsWith("?m=")) {
				verifyM = item.substring(3);
			}
		}
		
		// if both not null and not empty => trigger verification
		if ((verifyI != null && verifyM != null) && (!verifyI.equals("") && !verifyM.equals(""))) {
			
			final SimplePanel verifContent = new SimplePanel();
			leftMenu.addItem(ApplicationMessages.INSTANCE.emailValidationMenuItem(), SmallIcons.INSTANCE.documentSignatureIcon(), verifContent);
			
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
			
			return;
			
		}
		
		// group and extension is not allowed
		if (group != null && applicationType.equalsIgnoreCase("EXTENSION")) {
			
			RootLayoutPanel panel = RootLayoutPanel.get();
			panel.clear();
			FlexTable ft = new FlexTable();
			ft.setSize("100%", "300px");
			ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Error: "+ApplicationMessages.INSTANCE.groupMembershipCantBeExtended(group.getName(), vo.getName())+"</h2>");
			ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
			ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
			panel.add(ft);
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

	}
	
	private FlexTable getErrorWidget(PerunError error) {
		
		String text = "<h2>Request timeout exceeded.</h2>";
		if (error != null) {
			text = error.getErrorInfo();
		}
		FlexTable ft = new FlexTable();
		ft.setSize("100%", "300px");
		ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>Error: </h2>" + text);
		ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		
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
        ft.setWidget(0, 1, new HTML(PerunWebConstants.INSTANCE.footerPerunCopyright()));

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