package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import cz.metacentrum.perun.webgui.client.resources.ExceptionLogger;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetGuiConfiguration;
import cz.metacentrum.perun.webgui.json.GetNewGuiAlert;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttribute;
import cz.metacentrum.perun.webgui.json.authzResolver.GetPerunPrincipal;
import cz.metacentrum.perun.webgui.json.authzResolver.KeepAlive;
import cz.metacentrum.perun.webgui.json.usersManager.ValidatePreferredEmailChange;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunPrincipal;
import cz.metacentrum.perun.webgui.tabs.TabManager;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilitiesSelectTabItem;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupsTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.SelfDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VosSelectTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CantLogAsServiceUserWidget;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.NotUserOfPerunWidget;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main web GUI class. It's GWT Entry point.
 * <p>
 * Loads whole GUI, makes login to RPC by calling GetPerunPrincipal
 * Handles all changes in URL by default
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class WebGui implements EntryPoint, ValueChangeHandler<String> {

  // min client height & width
  static final public int MIN_CLIENT_HEIGHT = 700;  // when changed update .css file too !!
  static final public int MIN_CLIENT_WIDTH = 1200; // when changed update .css file too !!
  private static boolean checkPending = false;
  // web session
  private PerunWebSession session = PerunWebSession.getInstance();
  // url mapper
  private UrlMapper urlMapper;
  // perun loaded (is checked when page loads)
  private boolean perunLoaded = false;
  private WebGui webgui = this;
  private RootLayoutPanel body;
  private boolean connected = true;

  private int keepAliveTreshold = 5;
  private int keepAliveCounter = 0;

  /**
   * This is ENTRY POINT method. It's called automatically when web page
   * containing this GUI is loaded in browser.
   */
  public void onModuleLoad() {

    ExceptionLogger exceptionHandler = new ExceptionLogger();
    GWT.setUncaughtExceptionHandler(exceptionHandler);

    try {

      // IF IS MAIL VERIFICATION REQUEST
      if (checkIfMailVerification()) {
        return;
      }


      // LOAD PERUN WEB GUI

      // Get web page's BODY
      body = RootLayoutPanel.get();
      body.setStyleName("mainPanel");

      // check RPC url
      if (session.getRpcUrl().isEmpty()) {
        VerticalPanel bodyContents = new VerticalPanel();
        bodyContents.setSize("100%", "300px");
        bodyContents.add(new HTML(new Image(LargeIcons.INSTANCE.errorIcon()) + "<h2>RPC SERVER NOT FOUND!</h2>"));
        bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(0), HasHorizontalAlignment.ALIGN_CENTER);
        bodyContents.setCellVerticalAlignment(bodyContents.getWidget(0), HasVerticalAlignment.ALIGN_BOTTOM);
        body.add(bodyContents);
        return;
      }

      // load principal
      loadPerunPrincipal();

    } catch (Exception ex) {
      exceptionHandler.onUncaughtException(ex);
    }

  }

  /**
   * Performs a login into the RPC, loads user and his roles into session and enables GUI.
   */
  private void loadPerunPrincipal() {

    // WEB PAGE SPLITTER
    final DockLayoutPanel bodySplitter = new DockLayoutPanel(Unit.PX);
    body.add(bodySplitter);

    // MAIN CONTENT WRAPPER - make content resize-able
    final ResizeLayoutPanel contentWrapper = new ResizeLayoutPanel();
    contentWrapper.setSize("100%", "100%");

    // MAIN CONTENT
    AbsolutePanel contentPanel = new AbsolutePanel();
    contentPanel.setSize("100%", "100%");
    // put content into wrapper
    contentWrapper.setWidget(contentPanel);

    // SETUP SESSION

    // store handler for main contetn's elements (tabs etc.) into session
    session.setUiElements(new UiElements(contentPanel));
    // Store TabManager into session for handling tabs (add/remove/close...)
    session.setTabManager(new TabManager());
    // Store this class into session
    session.setWebGui(webgui);

    // Set this class as browser's History handler
    History.addValueChangeHandler(webgui);

    // show loading box
    final PopupPanel loadingBox = session.getUiElements().perunLoadingBox();
    loadingBox.show();


    // events after getting PerunPrincipal from RPC
    final JsonCallbackEvents events = new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {

        // store perun principal into session for future use
        final PerunPrincipal pp = (PerunPrincipal) jso;
        session.setPerunPrincipal(pp);

        GetNewGuiAlert getNewGuiAlert = new GetNewGuiAlert(new JsonCallbackEvents() {
          @Override
          public void onFinished(JavaScriptObject jso) {
            BasicOverlayType basic = (jso == null) ? null : jso.cast();
            String alert = (basic == null) ? null : basic.getString();
            session.setNewGuiAlert(alert);

            String newGuiAlertContent = session.getNewGuiAlert();
            if (newGuiAlertContent != null && !newGuiAlertContent.isEmpty()) {
              DOM.getElementById("perun-new-gui-alert").setInnerHTML(session.getNewGuiAlert());
              DOM.getElementById("perun-new-gui-alert").setClassName("newGuiAlertActive");
            }
          }
        });
        getNewGuiAlert.retrieveData();

        GetGuiConfiguration getConfig = new GetGuiConfiguration(new JsonCallbackEvents() {
          @Override
          public void onFinished(JavaScriptObject jso) {

            session.setConfiguration((BasicOverlayType) jso.cast());

            // check if user exists
            if (session.getUser() == null && !pp.getRoles().hasAnyRole()) {
              // if not and no role, redraw page body
              RootLayoutPanel body = RootLayoutPanel.get();
              loadingBox.hide();
              body.clear();
              body.add(new NotUserOfPerunWidget());
              return;
            }

            // check if user exists
            if (session.getUser() != null && session.getUser().isServiceUser()) {
              // if not and no role, redraw page body
              RootLayoutPanel body = RootLayoutPanel.get();
              loadingBox.hide();
              body.clear();
              body.add(new CantLogAsServiceUserWidget());
              return;
            }

            // Sets URL mapper for loading proper tabs
            urlMapper = new UrlMapper();

            // MENU WRAPPER
            VerticalPanel menuWrapper = new VerticalPanel();
            menuWrapper.setHeight("100%");
            // add menu
            menuWrapper.add(session.getUiElements().getMenu().getWidget());
            menuWrapper.setCellVerticalAlignment(session.getUiElements().getMenu().getWidget(),
                HasVerticalAlignment.ALIGN_TOP);

            // Put all panels into web page splitter
            //	bodySplitter.addNorth(session.getUiElements().getHeader(), 78);
            bodySplitter.addNorth(session.getUiElements().getHeader(), 64);
            bodySplitter.addSouth(session.getUiElements().getFooter(), 30);
            bodySplitter.addWest(menuWrapper, 202);
            bodySplitter.add(contentWrapper); // content must be added as last !!

            // Append more GUI elements from UiElements class which are not part of splitted design
            if ("true".equalsIgnoreCase(Location.getParameter("log"))) {
              bodySplitter.getElement().appendChild(session.getUiElements().getLog().getElement()); // log
            }
            bodySplitter.getElement().appendChild(session.getUiElements().getStatus().getElement()); // status

            // keep alive
            VerticalPanel vp = new VerticalPanel();
            vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

            vp.add(new HTML(
                "<h2>Connection to Perun has been lost.</h2><strong>Please check your internet connection.</strong>"));

            final FlexTable layout = new FlexTable();
            layout.setVisible(false);

            layout.setHTML(0, 0,
                "<p>You can also try to <strong>refresh the browser window</strong>. However, all <strong>unsaved changes will be lost</strong>.");
            layout.getFlexCellFormatter()
                .setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);

            vp.add(layout);
            vp.setSpacing(10);

            final Confirm c = new Confirm("", vp, true);
            c.setAutoHide(false);
            c.setHideOnButtonClick(false);
            c.setOkIcon(SmallIcons.INSTANCE.arrowRefreshIcon());
            c.setOkButtonText("Re-connect");
            c.setNonScrollable(true);
            c.setOkClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                KeepAlive call =
                    new KeepAlive(JsonCallbackEvents.disableButtonEvents(c.getOkButton(), new JsonCallbackEvents() {
                      @Override
                      public void onLoadingStart() {
                        checkPending = true;
                      }

                      @Override
                      public void onFinished(JavaScriptObject jso) {
                        BasicOverlayType type = jso.cast();
                        checkPending = false;
                        connected = true;
                        keepAliveCounter = 0;
                        if (type.getString().equals("OK")) {
                          if (c.isShowing()) {
                            c.hide();
                          }
                        }
                        // If ok, append new keepalive checker
                        appendKeepAliveChecker(c);
                      }

                      @Override
                      public void onError(PerunError error) {
                        checkPending = false;
                        // connection lost only IF TIMEOUT
                        if (error != null && error.getErrorId().equals("0")) {
                          keepAliveCounter++;
                          if (keepAliveCounter >= keepAliveTreshold) {
                            connected = false;
                            if (!c.isShowing()) {
                              c.show();
                            }
                            layout.setVisible(true);
                            c.getOkButton().setText("Reload");
                            c.getOkButton().addClickHandler(new ClickHandler() {
                              @Override
                              public void onClick(ClickEvent event) {
                                Window.Location.reload();
                              }
                            });

                          } else {
                            // not connected but under treshold
                            appendKeepAliveChecker(c);
                          }

                        }
                      }
                    }));
                call.retrieveData();
              }
            });

            appendKeepAliveChecker(c);

            // store users roles and editable entities into session
            session.setRoles(pp.getRoles());

            // display logged user
            session.getUiElements().setLoggedUserInfo(pp);
            if (pp.getUser() != null) {
              session.getUiElements().setLogText("Welcome " + pp.getUser().getFullNameWithTitles());
            }

            // show extended info ?
            boolean showExtendedInfo = false;

            // is perun admin ?
            if (session.isPerunAdmin()) {
              showExtendedInfo = true;
            }

            // replace by local storage if possible
            Storage localStorage = Storage.getLocalStorageIfSupported();
            if (localStorage != null) {
              String value = localStorage.getItem("urn:perun:gui:preferences:extendedInfo");
              if (value != null) {
                showExtendedInfo = Boolean.parseBoolean(value);
              }
            }

            // finally set it
            JsonUtils.setExtendedInfoVisible(showExtendedInfo);
            session.getUiElements().getExtendedInfoButtonWidget().setDown(showExtendedInfo); // set extended info button

            // Specific GDPR approval is required only from admins
            if (session.isFacilityAdmin() || session.isVoAdmin() || session.isVoObserver() ||
                session.isGroupAdmin() || session.isPerunAdmin()) {
              checkGdprApproval(new JsonCallbackEvents() {
                @Override
                public void onFinished(JavaScriptObject jso) {

                  // FINISH LOADING UI ONCE ITS CHECKED

                  // perun loaded = true
                  perunLoaded = true;

                  // loads the page based on URL or default
                  loadPage();

                  // hides the loading box
                  loadingBox.hide();

                  // load proper parts of menu (based on roles)
                  session.getUiElements().getMenu().prepare();

                }

                @Override
                public void onError(PerunError error) {

                  // hides the loading box
                  loadingBox.hide();

                  // shows error box
                  PopupPanel loadingFailedBox;
                  if (error != null) {
                    loadingFailedBox = session.getUiElements().perunLoadingFailedBox(error.getErrorInfo());
                  } else {
                    String checkFailed = session.getConfiguration().getGDPRproperty("check_failed");
                    loadingFailedBox = session.getUiElements().perunLoadingFailedBox(checkFailed);
                  }
                  loadingFailedBox.show();

                }
              });
            } else {

              // NO GDPR CHECK NEEDED

              // perun loaded = true
              perunLoaded = true;

              // loads the page based on URL or default
              loadPage();

              // hides the loading box
              loadingBox.hide();

              // load proper parts of menu (based on roles)
              session.getUiElements().getMenu().prepare();

            }

          }

          @Override
          public void onError(PerunError error) {

            // hides the loading box
            loadingBox.hide();

            // shows error box
            PopupPanel loadingFailedBox;
            if (error != null) {
              loadingFailedBox = session.getUiElements().perunLoadingFailedBox("Request timeout exceeded.");
            } else {
              if (error.getName().contains("UserNotExistsException")) {
                loadingFailedBox = session.getUiElements().perunLoadingFailedBox(
                    "You are not registered to any Virtual Organization.</br></br>" + error.getErrorInfo());
              } else {
                loadingFailedBox = session.getUiElements().perunLoadingFailedBox(error.getErrorInfo());
              }
            }
            if (!error.getErrorId().equals("0")) {
              loadingFailedBox.show();
            }

          }
        });
        getConfig.retrieveData();
      }

      @Override
      public void onError(PerunError error) {
        // hides the loading box
        loadingBox.hide();

        // shows error box
        PopupPanel loadingFailedBox;
        if (error == null) {
          loadingFailedBox = session.getUiElements().perunLoadingFailedBox("Request timeout exceeded.");
        } else {
          if (error.getName().contains("UserNotExistsException")) {
            loadingFailedBox = session.getUiElements().perunLoadingFailedBox(
                "You are not registered to any Virtual Organization.</br></br>" + error.getErrorInfo());
          } else {
            loadingFailedBox = session.getUiElements().perunLoadingFailedBox(error.getErrorInfo());
          }
        }
        loadingFailedBox.show();
      }
    };
    GetPerunPrincipal loggedUserRequst = new GetPerunPrincipal(events);
    loggedUserRequst.retrieveData();
  }

  /**
   * Called automatically when page changes (tokens after #)
   */
  public void onValueChange(ValueChangeEvent<String> event) {
    changePage(History.getToken());
  }

  /**
   * Called when page is changed. Loads proper tabs base on tokens after #
   *
   * @param token Hash (value after #)
   */
  public void changePage(String token) {

    // if Perun not ready, user not authorized or in process of adding tabs
    if (!perunLoaded || session.getTabManager().isChangePageEventLocked()) {
      return;
    }

    urlMapper.parseUrl(token);

  }

  /**
   * Loads the page. Must be called after authentication process!
   */
  private void loadPage() {

    // when there is no token, default tabs are loaded
    // this is useful if user has bookmarked a site other than the homepage.
    if (History.getToken().isEmpty()) {

      // get whole URL
      String url = Window.Location.getHref();
      String newToken = "";

      int index = -1;

      if (url.contains("?gwt.codesvr=127.0.0.1:9997")) {
        // if local devel build

        if (url.contains("?locale=")) {
          // with locale
          index = url.indexOf("?", url.indexOf("?", url.indexOf("?")) + 1);
        } else {
          // without locale
          index = url.indexOf("?", url.indexOf("?") + 1);
        }
      } else {
        // if production build
        if (url.contains("?locale=")) {
          // with locale
          index = url.indexOf("?", url.indexOf("?") + 1);
        } else {
          // without locale
          index = url.indexOf("?");
        }
      }

      if (index != -1) {
        newToken = url.substring(index + 1);
      }

      // will sort of break URL, but will work without refreshing whole GUI
      if (newToken.isEmpty()) {
        // token was empty anyway - load default
        loadDefaultTabs();
      } else {
        // token is now correct - load it
        History.newItem(newToken);
      }

    } else {
      changePage(History.getToken());
    }

  }

  /**
   * Loads default tabs based on user's roles (called when no other tabs opened).
   * Must be called after authentication process !
   */
  private void loadDefaultTabs() {

    // perun admin
    if (session.isPerunAdmin()) {
      // let perun admin decide what to open
      //session.getTabManager().addTab(new VosTabItem(session), true);
      return;
    }
    // VO admin
    if (session.isVoAdmin()) {
      if (session.getEditableVos().size() > 1) {
        session.getTabManager().addTab(new VosSelectTabItem(), true);
      } else if (session.getEditableVos().size() == 1) {
        session.getTabManager().addTab(new VoDetailTabItem(session.getEditableVos().get(0)), true);
      }
      return;
    }
    // VO observer
    if (session.isVoObserver()) {
      if (session.getViewableVos().size() > 1) {
        session.getTabManager().addTab(new VosSelectTabItem(), true);
      } else if (session.getViewableVos().size() == 1) {
        session.getTabManager().addTab(new VoDetailTabItem(session.getViewableVos().get(0)), true);
      }
      return;
    }
    // facility admin
    if (session.isFacilityAdmin()) {
      if (session.getEditableFacilities().size() > 1) {
        session.getTabManager().addTab(new FacilitiesSelectTabItem(), true);
      } else {
        session.getTabManager().addTab(new FacilityDetailTabItem(session.getEditableFacilities().get(0)), true);
      }
      return;
    }
    // group admin
    if (session.isGroupAdmin()) {
      if (session.getEditableGroups().size() > 1) {
        session.getTabManager().addTab(new GroupsTabItem(null), true);
      } else {
        session.getTabManager().addTab(new GroupDetailTabItem(session.getEditableGroups().get(0)), true);
      }
      return;
    }

    // only user
    if (session.isSelf() && session.getUser() != null) {
      session.getTabManager().addTab(new SelfDetailTabItem(session.getUser()), true);
    }

  }

  /**
   * Check if URL parameters contains data for mail verification and display different GUI instead.
   *
   * @return TRUE if parameters are contained and valid in URL, FALSE otherwise.
   */
  public boolean checkIfMailVerification() {

    // Trigger validation if necessary
    if (Location.getParameterMap().keySet().contains("token") &&
        Location.getParameterMap().keySet().contains("u")) {

      String verifyToken = Location.getParameter("token");
      String verifyU = Location.getParameter("u");

      if (verifyToken != null && !verifyToken.isEmpty() &&
          verifyU != null && !verifyU.isEmpty() && JsonUtils.checkParseInt(verifyU)) {

        ValidatePreferredEmailChange call =
            new ValidatePreferredEmailChange(verifyToken, Integer.parseInt(verifyU), new JsonCallbackEvents() {
              @Override
              public void onFinished(JavaScriptObject jso) {

                BasicOverlayType over = jso.cast();

                RootLayoutPanel body = RootLayoutPanel.get();
                body.clear();
                FlexTable ft = new FlexTable();
                ft.setSize("100%", "300px");
                ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.acceptIcon()) + "<h2>" + "The email address: <i>" +
                    (SafeHtmlUtils.fromString(over.getString())).asString() +
                    "</i></h2><h2>was verified and set as your preferred email.</h2>");
                ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
                ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
                body.add(ft);
              }

              @Override
              public void onLoadingStart() {

                RootLayoutPanel body = RootLayoutPanel.get();
                body.clear();
                FlexTable ft = new FlexTable();
                ft.setSize("100%", "300px");
                ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
                ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
                ft.setWidget(0, 0, new AjaxLoaderImage());
                body.add(ft);

              }

              @Override
              public void onError(PerunError error) {

                RootLayoutPanel body = RootLayoutPanel.get();
                body.clear();
                FlexTable ft = new FlexTable();
                ft.setSize("100%", "300px");
                ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.deleteIcon()) +
                    "<h2>Your new email address couldn't be verified !</h2>");
                ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
                ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);

                if (error == null) {
                  ft.setHTML(1, 0, "Request timeout exceeded.");
                  ft.getFlexCellFormatter().setStyleName(1, 0, "serverResponseLabelError");
                } else {
                  // display raw message
                  ft.setHTML(1, 0, "<strong>" + error.getErrorInfo() + "</strong>");
                }
                ft.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
                ft.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_MIDDLE);

                body.add(ft);

              }
            });
        call.retrieveData();
      }

      return true;

    }

    return false;

  }

  /**
   * Append keep-alive checker.
   *
   * @param c Confirm displayed when connection is lost
   */
  private void appendKeepAliveChecker(final Confirm c) {

    // Check RPC URL every 15 sec if call not pending
    Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
      @Override
      public boolean execute() {

        KeepAlive call = new KeepAlive(new JsonCallbackEvents() {
          @Override
          public void onLoadingStart() {
            checkPending = true;
          }

          @Override
          public void onFinished(JavaScriptObject jso) {
            BasicOverlayType type = jso.cast();
            checkPending = false;
            keepAliveCounter = 0;
            if (type.getString().equals("OK")) {
              if (c.isShowing()) {
                c.hide();
              }
            }
          }

          @Override
          public void onError(PerunError error) {
            checkPending = false;
            if (error != null && error.getErrorId().equals("0")) {
              keepAliveCounter++;
              if (keepAliveCounter >= keepAliveTreshold) {
                // connection lost only IF TIMEOUT and treshold
                if (!c.isShowing()) {
                  c.show();
                }
                connected = false;
              }
            }
          }
        });
        if (!checkPending && perunLoaded && connected) {
          call.retrieveData();
        }
        return connected;
      }
    }, 15000);

  }

  private void checkGdprApproval(JsonCallbackEvents finishEvents) {

    if (session.getConfiguration() != null) {

      String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("gdprAdminEnabled");
      if (value != null && !value.isEmpty() && "true".equalsIgnoreCase(value) && session.getUser() != null) {

        final String currentVersion = PerunWebSession.getInstance().getConfiguration().getGDPRproperty("version");

        if (currentVersion != null && !currentVersion.isEmpty()) {

          Map<String, Integer> ids = new HashMap<>();
          ids.put("user", session.getUser().getId());

          GetListOfAttributes list = new GetListOfAttributes(new JsonCallbackEvents() {
            @Override
            public void onFinished(JavaScriptObject jso) {

              List<Attribute> response = JsonUtils.<Attribute>jsoAsList(jso);
              if (response.isEmpty()) {
                // attribute for storing approved versions doesn't exists or user is not allowed to read it - continue loading UI
                finishEvents.onFinished(null);
                return;
              }
              for (Attribute a : response) {
                if (a != null && "gdprAdminApprovedVersions".equalsIgnoreCase(a.getFriendlyName())) {
                  Map<String, String> approvedAdminGDPR = a.getValueAsMapString();
                  if (approvedAdminGDPR.containsKey(currentVersion)) {
                    // user already approved latest version - continue loading UI
                    finishEvents.onFinished(null);
                    break;
                  } else {
                    // show current GDPR approval
                    showCurrentGDPRApproval(ids, a, approvedAdminGDPR, currentVersion, finishEvents);
                    break;
                  }
                } else {
                  PerunError error = (PerunError) PerunError.createObject();
                  error.setErrorInfo(
                      "In order to use GUI as administrator you must approve latest version of GDPR. But configuration for it is missing in Perun (missing gdprAdminApprovedVersions attribute).");
                  finishEvents.onError(error);
                  return;
                }
              }

            }
          });
          list.getListOfAttributes(ids, "urn:perun:user:attribute-def:def:gdprAdminApprovedVersions");

        }

      } else {
        // load gui - check was disabled
        finishEvents.onFinished(null);
      }
    } else {
      // load gui - check is disabled - no configuration object
      finishEvents.onFinished(null);
    }

  }

  private void showCurrentGDPRApproval(Map<String, Integer> ids, Attribute a, Map<String, String> approvedAdminGDPR,
                                       String currentVersion, JsonCallbackEvents finishEvents) {

    String gdprApprovalText = session.getConfiguration().getGDPRproperty("text");
    String agreeButtonText = session.getConfiguration().getGDPRproperty("agree");
    String disagreeButtonText = session.getConfiguration().getGDPRproperty("disagree");
    String disagreeResult = session.getConfiguration().getGDPRproperty("disagree_result");
    String title = session.getConfiguration().getGDPRproperty("title");
    String backToGdprConfirm = session.getConfiguration().getGDPRproperty("disagree_back");

    HTML html = new HTML("<div height=\"500\" width=\"420\">" + gdprApprovalText + "</div>");

    Confirm gdprConfirm = new Confirm(title, html, new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

        SetAttribute setAttribute = new SetAttribute(new JsonCallbackEvents() {

          @Override
          public void onFinished(JavaScriptObject jso) {
            // approved finish loading UI
            finishEvents.onFinished(null);
          }

          @Override
          public void onError(PerunError error) {
            error.setErrorInfo("Failed to store your GDPR approval for version: " + currentVersion);
            finishEvents.onError(error);
          }
        });

        approvedAdminGDPR.put(currentVersion, JsonUtils.getCurrentDateAsString());
        a.setValueAsMap(approvedAdminGDPR);
        setAttribute.setAttribute(ids, a);

      }
    }, new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

        Confirm disagreeConfirm = new Confirm(title, new HTML(disagreeResult), false);
        disagreeConfirm.setNonScrollable(true);
        disagreeConfirm.setAutoHide(false);
        disagreeConfirm.setOkButtonText(backToGdprConfirm);
        disagreeConfirm.setOkClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            finishEvents.onError(null);
            Window.Location.reload();
          }
        });
        disagreeConfirm.show();
      }
    }, agreeButtonText, disagreeButtonText, true);
    gdprConfirm.setNonScrollable(true);
    gdprConfirm.setAutoHide(false);
    gdprConfirm.show();

  }

}
