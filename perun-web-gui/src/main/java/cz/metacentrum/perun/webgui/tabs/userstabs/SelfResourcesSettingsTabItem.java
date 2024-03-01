package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.GetRequiredAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.membersManager.GetMemberByUser;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAllowedResources;
import cz.metacentrum.perun.webgui.json.usersManager.GetVosWhereUserIsMember;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithHelp;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.tabs.userstabs.RequestQuotaChangeTabItem.QuotaType;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's settings for User
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfResourcesSettingsTabItem implements TabItem, TabItemWithUrl, TabItemWithHelp {

  public final static String URL = "settings";
  /**
   * Perun web session
   */
  private PerunWebSession session = PerunWebSession.getInstance();
  /**
   * Content widget - should be simple panel
   */
  private SimplePanel contentWidget = new SimplePanel();
  /**
   * Title widget
   */
  private Label titleWidget = new Label("Loading user");
  /**
   * Search string
   */
  private User user;
  private int userId;
  // virtual organization which display settings for
  private VirtualOrganization vo;
  private int voId = 0;

  /**
   * Creates a tab instance for logged user
   */
  public SelfResourcesSettingsTabItem() {
    this.user = session.getActiveUser();
    this.userId = user.getId();
  }

  /**
   * Creates a tab instance for specific user
   *
   * @param user
   */
  public SelfResourcesSettingsTabItem(User user) {
    this.user = user;
    this.userId = user.getId();
  }

  /**
   * Creates a tab instance for specific user
   *
   * @param userId
   */
  public SelfResourcesSettingsTabItem(int userId) {
    this.userId = userId;
    if (userId != 0) {
      new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents() {
        public void onFinished(JavaScriptObject jso) {
          user = jso.cast();
        }
      }).retrieveData();
    }
  }

  /**
   * Creates a tab instance for specific user and VO
   *
   * @param user
   * @param vo
   */
  public SelfResourcesSettingsTabItem(User user, VirtualOrganization vo) {
    this(user);
    this.vo = vo;
    this.voId = vo.getId();
  }

  /**
   * Creates a tab instance for logged user with specific VO
   *
   * @param vo
   */
  public SelfResourcesSettingsTabItem(VirtualOrganization vo) {
    this();
    this.vo = vo;
    this.voId = vo.getId();
  }

  /**
   * Creates a tab instance for specific user with specific VO
   * <p>
   * Have fallback if userId == 0.
   *
   * @param userId
   * @param voId
   */
  public SelfResourcesSettingsTabItem(int userId, int voId) {
    if (userId == 0) {
      this.user = session.getActiveUser();
      this.userId = user.getId();
    } else {
      this.userId = userId;
      new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents() {
        public void onFinished(JavaScriptObject jso) {
          user = jso.cast();
        }
      }).retrieveData();
    }
    this.voId = voId;
    if (voId != 0) {
      new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, new JsonCallbackEvents() {
        public void onFinished(JavaScriptObject jso) {
          vo = jso.cast();
        }
      }).retrieveData();
    }
  }

  static public SelfResourcesSettingsTabItem load(Map<String, String> parameters) {
    if (parameters.containsKey("id")) {
      int uid = Integer.parseInt(parameters.get("id"));
      int voId = Integer.parseInt(parameters.get("vo"));
      return new SelfResourcesSettingsTabItem(uid, voId);
    } else if (parameters.containsKey("vo")) {
      int voId = Integer.parseInt(parameters.get("vo"));
      return new SelfResourcesSettingsTabItem(0, voId);
    }
    return new SelfResourcesSettingsTabItem();
  }

  public boolean isPrepared() {

    if (user == null) {
      return false;
    }
    if (voId == 0) {
      return true;
    }
    if (vo == null) {
      return false;
    }

    return true;

  }

  @Override
  public boolean isRefreshParentOnClose() {
    return false;
  }

  @Override
  public void onClose() {

  }

  public Widget draw() {

    this.titleWidget.setText(
        Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()) + ": Resources settings");

    final VerticalPanel vp = new VerticalPanel();
    vp.setSize("100%", "100%");

    final TabMenu menu = new TabMenu();
    vp.add(menu);
    vp.setCellHeight(menu, "30px");

    menu.addWidget(UiElements.getRefreshButton(this));

    final ScrollPanel scroll = new ScrollPanel();
    scroll.setWidget(vp);
    scroll.setStyleName("perun-tableScrollPanel");
    session.getUiElements().resizeSmallTabPanel(scroll, 350, this);
    scroll.setWidth("100%");

    final AjaxLoaderImage loader = new AjaxLoaderImage();

    // RETRIEVES ALL VOS WHERE USER IS A MEMBER
    GetVosWhereUserIsMember vosRequest = new GetVosWhereUserIsMember(userId, new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        vp.getWidget(1).removeFromParent();
        ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
        vos = new TableSorter<VirtualOrganization>().sortByName(vos);

        // if vos length = 1, load immediately all resources
        final boolean loadImmediately = (vos.size() == 1);

        // FOR EACH VO, FIND THE MEMBER
        for (final VirtualOrganization innerVo : vos) {

          final FlexTable header = new FlexTable();
          header.setWidth("100%");
          header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
          header.getFlexCellFormatter().setWidth(0, 0, "40px");
          Anchor a = new Anchor("<p class=\"now-managing disclosurePanelHeader\">" + innerVo.getName() + "</p>", true);
          header.setWidget(0, 1, a);
          header.setTitle("Click to show resources for " + innerVo.getName());

          // disclosure panel
          final DisclosurePanel settings = new DisclosurePanel();
          settings.setWidth("100%");
          settings.setHeader(header);

          // load content on open
          settings.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            public void onOpen(OpenEvent<DisclosurePanel> event) {
              if (settings.getContent() == null) {
                GetMemberByUser memberRequest = new GetMemberByUser(innerVo.getId(), userId, new JsonCallbackEvents() {
                  @Override
                  public void onFinished(JavaScriptObject jso) {
                    Member member = jso.cast();
                    settings.setContent(userSettingsForVo(innerVo, member)); // set content
                  }
                });
                memberRequest.retrieveData();
              }
            }
          });
          vp.add(settings);
          settings.setOpen((vos.size() == 1) || (vo != null && vo.getId() == innerVo.getId()));
        }
      }

      @Override
      public void onLoadingStart() {
        vp.add(loader);
      }

      @Override
      public void onError(PerunError error) {
        loader.loadingError(error);
      }
    });

    vosRequest.retrieveData();

    this.contentWidget.setWidget(scroll);
    return getWidget();

  }

  /**
   * User settings for the VO
   *
   * @param vo
   * @param member
   * @return
   */
  protected Widget userSettingsForVo(final VirtualOrganization vo, Member member) {

    final VerticalPanel vp = new VerticalPanel();
    vp.setWidth("100%");

    GetAllowedResources resourcesCallback = new GetAllowedResources(member.getId(), new JsonCallbackEvents() {
      @Override
      public void onLoadingStart() {
        vp.clear();
        vp.add(new AjaxLoaderImage());
      }

      @Override
      public void onFinished(JavaScriptObject jso) {
        vp.clear();
        // convert & sort
        ArrayList<Resource> resources = JsonUtils.jsoAsList(jso);
        resources = new TableSorter<Resource>().sortByDescription(resources);
        // if empty
        if (resources.isEmpty() || resources == null) {
          FlexTable ft = new FlexTable();
          ft.setHTML(0, 0,
              "<p><strong>VO " + SafeHtmlUtils.fromString((vo.getName() != null) ? vo.getName() : "").asString() +
                  " doesn't provide any resources to configure.</strong></p>");
          vp.add(ft);
          return;
        }
        // process
        for (Resource r : resources) {
          DisclosurePanel settings = new DisclosurePanel();
          settings.setWidth("100%");

          final FlexTable header = new FlexTable();
          header.setWidth("100%");
          header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.databaseServerIcon()));
          header.getFlexCellFormatter().setWidth(0, 0, "40px");
          Anchor a = new Anchor("<p class=\"now-managing\">" + r.getDescription() + " (" + r.getName() + ")</p>", true);
          header.setWidget(0, 1, a);
          header.setTitle("Click to show setting for resource " + r.getName());
          settings.setHeader(header);
          loadSettings(settings, r);  // load content for each resource
          vp.add(settings);
        }
      }
    });

    resourcesCallback.retrieveData();

    return vp;

  }

  /**
   * Help widget
   *
   * @return
   */
  public Widget getHelpWidget() {

    String help = "";

    help += "<p>" + "The resources are grouped by virtual organizations - click on a VO to see the resources." + "</p>";
    help += "<p><strong>" + "Click on a resource to change its settings:" + "</strong></p>";
    help += "<ul>";
    help += "   <li>Shell</li>";
    help += "</ul>";
    help += "<p><strong>" + "Or you can request quota change:" + "</strong></p>";
    help += "<ul>";
    help += "   <li>Data quota</li>";
    help += "   <li>Files quota</li>";
    help += "</ul>";

    return new HTML(help);

  }

  /**
   * Load settings for each resource (loaded only once onClick on header)
   *
   * @return settings content for each resource
   */
  private void loadSettings(final DisclosurePanel settings, final Resource resource) {

    // create content table
    final FlexTable layoutx = new FlexTable();
    layoutx.setCellSpacing(5);

    GetAttributesV2 resourceAttrsCall = new GetAttributesV2();
    Map<String, Integer> ids = new HashMap<String, Integer>();
    ids.put("resource", resource.getId());
    resourceAttrsCall.setIds(ids);

    resourceAttrsCall.setEvents(new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        boolean disableOptOut = false;
        ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
        for (Attribute a : attrs) {
          if ("urn:perun:resource:attribute-def:def:disableMailingListOptOut".equals(a.getName())) {
            disableOptOut = true;
            break;
          }
        }
        loadContent(layoutx, resource, disableOptOut);
      }

      public void onLoadingStart() {
        AjaxLoaderImage loader = new AjaxLoaderImage(true);
        layoutx.setWidget(0, 0, loader.loadingStart());
      }

      public void onError(PerunError error) {
        AjaxLoaderImage loader = new AjaxLoaderImage(true);
        layoutx.setWidget(0, 0, loader.loadingError(error));
      }
    });

    // load content on open
    settings.addOpenHandler(new OpenHandler<DisclosurePanel>() {
      public void onOpen(OpenEvent<DisclosurePanel> event) {
        if (settings.getContent() == null) {
          resourceAttrsCall.retrieveData();
          settings.setContent(layoutx); // set content
        }
      }
    });

  }

  private void loadContent(final FlexTable layoutx, final Resource resource, final boolean disableOptOut) {

    // get member
    final GetMemberByUser callMember = new GetMemberByUser(resource.getVoId(), userId, new JsonCallbackEvents() {
      public void onFinished(JavaScriptObject jso) {
        // member
        final Member mem = jso.cast();
        // set ids
        Map<String, Integer> ids = new HashMap<String, Integer>();
        ids.put("member", mem.getId());
        ids.put("resource", resource.getId());
        ids.put("resourceToGetServicesFrom", resource.getId()); // to filter empty values
        ids.put("workWithUserAttributes", 1);

        // get req. attrs - to filter eg. if quota is used or not
        final GetRequiredAttributes attributes = new GetRequiredAttributes(ids);
        final JsonCallbackEvents refreshEvent = new JsonCallbackEvents() {
          @Override
          public void onFinished(JavaScriptObject jso) {
            /// reload resource settings data
            attributes.retrieveData();
          }
        };
        attributes.setEvents(new JsonCallbackEvents() {
          public void onLoadingStart() {
            AjaxLoaderImage loader = new AjaxLoaderImage(true);
            layoutx.setWidget(0, 0, loader.loadingStart());
          }

          public void onError(PerunError error) {
            AjaxLoaderImage loader = new AjaxLoaderImage(true);
            layoutx.setWidget(0, 0, loader.loadingError(error));
          }

          public void onFinished(JavaScriptObject jso) {
            // fill layout
            ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
            boolean empty = true; // check if any attributes displayed
            layoutx.getWidget(0, 0).removeFromParent(); // remove loading image

            int row = 0;

            // process shell
            for (final Attribute a : attrs) {
              // user's shell on facility (DEF is exception from default VIRT so display them)
              if (a.getFriendlyName().equalsIgnoreCase("shell") && !a.getDefinition().equalsIgnoreCase("virt")) {
                layoutx.setHTML(row, 0, "<strong>Shell: </strong>");

                empty = false;
                // change button
                CustomButton cb = new CustomButton("Change…", SmallIcons.INSTANCE.cogIcon());
                layoutx.setWidget(row, 2, cb);
                // click handler
                cb.addClickHandler(new ClickHandler() {
                  public void onClick(ClickEvent event) {
                    session.getTabManager()
                        .addTabToCurrentTab(new ShellChangeTabItem(resource, userId, a, refreshEvent));
                  }
                });
                if (!a.getValue().equalsIgnoreCase("null")) {
                  // FIXME - we can't offer what default would be, since virt value is always same as def value
                  layoutx.setHTML(row, 1,
                      SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString());
                  row++;
                  layoutx.setHTML(row, 1,
                      "You are using specific shell for this resource overriding your global preferences.<br />To get back to default use change button.");
                  layoutx.getFlexCellFormatter().setStyleName(row, 1, "inputFormInlineComment");
                  layoutx.getFlexCellFormatter().setColSpan(row, 1, 2);
                }
                for (Attribute ia : attrs) {
                  if (ia.getFriendlyName().equalsIgnoreCase("shell") && ia.getDefinition().equalsIgnoreCase("virt")) {
                    if (a.getValue().equalsIgnoreCase("null") && ia.getValue().equalsIgnoreCase("null")) {
                      layoutx.setHTML(row, 1, "Using default (default: Not set)");
                    } else if (a.getValue().equalsIgnoreCase("null")) {
                      layoutx.setHTML(row, 1, "Using default (default: " +
                          SafeHtmlUtils.fromString((a.getValue() != null) ? a.getValue() : "").asString() + ")");
                      row++;
                      layoutx.setHTML(row, 1,
                          "You are using default shell taken from your global preferences.<br />Use change button to set specific shell for this resource.");
                      layoutx.getFlexCellFormatter().setStyleName(row, 1, "inputFormInlineComment");
                      layoutx.getFlexCellFormatter().setColSpan(row, 1, 2);
                    }
                    break;
                  }
                }
                row++;
                break;
              }
            }
            for (final Attribute a : attrs) {
              if (a.getFriendlyName().equalsIgnoreCase("dataQuotas") && !a.getValueAsMap().isEmpty()) {
                final int rowDataLimit = row;
                final CustomButton quotaChangeButton =
                    new CustomButton("Request change…", SmallIcons.INSTANCE.databaseIcon());

                // display value
                layoutx.setHTML(row, 0, "<strong>Data quota: </strong>");

                // get default
                Map<String, Integer> ids = new HashMap<String, Integer>();
                ids.put("resource", resource.getId());
                GetAttributes defaultAttr = new GetAttributes(new JsonCallbackEvents() {
                  public void onError(PerunError error) {
                    if (a.getValueAsMap().isEmpty()) {
                      layoutx.setHTML(rowDataLimit, 1, "Using default (default: error while loading)");
                    } else {
                      layoutx.setHTML(rowDataLimit, 1,
                          SafeHtmlUtils.fromString(convertMapToNiceString(a.getValueAsMap())).asString() +
                              " (default: error while loading)");
                    }
                    quotaChangeButton.addClickHandler(new ClickHandler() {
                      public void onClick(ClickEvent event) {
                        session.getTabManager().addTabToCurrentTab(
                            new RequestQuotaChangeTabItem(resource, user, QuotaType.DATA,
                                convertMapToNiceString(a.getValueAsMap()),
                                convertMapToNiceString(new HashMap<String, JSONValue>())));
                      }
                    });
                  }

                  public void onFinished(JavaScriptObject jso) {
                    ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
                    boolean empty = true;
                    for (final Attribute resAttr : attrs) {
                      if (resAttr.getFriendlyName().equalsIgnoreCase("defaultDataQuotas")) {
                        if (a.getValueAsMap().isEmpty()) {
                          // null private + default
                          layoutx.setHTML(rowDataLimit, 1, "Using default (default: " +
                              SafeHtmlUtils.fromString(convertMapToNiceString(resAttr.getValueAsMap())).asString() +
                              ")");
                        } else {
                          // private - default
                          layoutx.setHTML(rowDataLimit, 1,
                              SafeHtmlUtils.fromString(convertMapToNiceString(a.getValueAsMap())).asString() +
                                  " (default: " +
                                  SafeHtmlUtils.fromString(convertMapToNiceString(resAttr.getValueAsMap())).asString() +
                                  ")");
                        }
                        empty = false;
                        quotaChangeButton.addClickHandler(new ClickHandler() {
                          public void onClick(ClickEvent event) {
                            session.getTabManager().addTabToCurrentTab(
                                new RequestQuotaChangeTabItem(resource, user, QuotaType.DATA,
                                    convertMapToNiceString(a.getValueAsMap()),
                                    convertMapToNiceString(resAttr.getValueAsMap())));
                          }
                        });
                      }
                    }
                    // if no default found, write down at least private
                    if (empty) {
                      if (a.getValueAsMap().isEmpty()) {
                        layoutx.setHTML(rowDataLimit, 1, "Using default (default: Not set)");
                      } else {
                        layoutx.setHTML(rowDataLimit, 1,
                            SafeHtmlUtils.fromString(convertMapToNiceString(a.getValueAsMap())).asString() +
                                " (default: Not set)");
                      }
                      quotaChangeButton.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                          session.getTabManager().addTabToCurrentTab(
                              new RequestQuotaChangeTabItem(resource, user, QuotaType.DATA,
                                  convertMapToNiceString(a.getValueAsMap()),
                                  convertMapToNiceString(new HashMap<String, JSONValue>())));
                        }
                      });
                    }
                  }
                });
                defaultAttr.setIds(ids);
                defaultAttr.retrieveData();
                empty = false;
                layoutx.setWidget(row, 2, quotaChangeButton);
                row++;


                layoutx.setHTML(row, 1,
                    "Quotas are using \"soft limit : hard limit\" notation and are bound to specific volume.");
                layoutx.getFlexCellFormatter().setStyleName(row, 1, "inputFormInlineComment");
                layoutx.getFlexCellFormatter().setColSpan(row, 1, 2);
                row++;

                break;
              }
            }
            for (final Attribute a : attrs) {
              if (a.getFriendlyName().equalsIgnoreCase("filesQuotas") && !a.getValueAsMap().isEmpty()) {
                layoutx.setHTML(row, 0, "<strong>Files quota: </strong>");
                final int rowFilesQuota = row;
                // get default
                Map<String, Integer> ids = new HashMap<String, Integer>();
                ids.put("resource", resource.getId());

                final CustomButton quotaChangeButton =
                    new CustomButton("Request change…", SmallIcons.INSTANCE.databaseIcon());

                GetAttributes defaultAttr = new GetAttributes(new JsonCallbackEvents() {
                  public void onError(PerunError error) {
                    if (a.getValueAsMap().isEmpty()) {
                      layoutx.setHTML(rowFilesQuota, 1, "Using default (default: error while loading)");
                    } else {
                      layoutx.setHTML(rowFilesQuota, 1,
                          SafeHtmlUtils.fromString(convertMapToNiceString(a.getValueAsMap())).asString() +
                              " (default: error while loading)");
                    }
                    quotaChangeButton.addClickHandler(new ClickHandler() {
                      public void onClick(ClickEvent event) {
                        session.getTabManager().addTabToCurrentTab(
                            new RequestQuotaChangeTabItem(resource, user, QuotaType.FILES,
                                convertMapToNiceString(a.getValueAsMap()),
                                convertMapToNiceString(new HashMap<String, JSONValue>())));
                      }
                    });
                  }

                  public void onFinished(JavaScriptObject jso) {
                    ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
                    boolean empty = true;
                    for (final Attribute resAttr : attrs) {
                      if (resAttr.getFriendlyName().equalsIgnoreCase("defaultFilesQuotas")) {
                        if (a.getValueAsMap().isEmpty()) {
                          // null private + default
                          layoutx.setHTML(rowFilesQuota, 1, "Using default (default: " +
                              SafeHtmlUtils.fromString(convertMapToNiceString(resAttr.getValueAsMap())).asString() +
                              ")");
                        } else {
                          // private + default
                          layoutx.setHTML(rowFilesQuota, 1,
                              SafeHtmlUtils.fromString(convertMapToNiceString(a.getValueAsMap())).asString() +
                                  " (default: " +
                                  SafeHtmlUtils.fromString(convertMapToNiceString(resAttr.getValueAsMap())).asString() +
                                  ")");
                        }
                        empty = false;
                        quotaChangeButton.addClickHandler(new ClickHandler() {
                          public void onClick(ClickEvent event) {
                            session.getTabManager().addTabToCurrentTab(
                                new RequestQuotaChangeTabItem(resource, user, QuotaType.FILES,
                                    convertMapToNiceString(a.getValueAsMap()),
                                    convertMapToNiceString(resAttr.getValueAsMap())));
                          }
                        });
                      }
                    }
                    // if no default found, write down at least private
                    if (empty) {
                      if (a.getValueAsMap().isEmpty()) {
                        layoutx.setHTML(rowFilesQuota, 1, "Using default (default: Not set)");
                      } else {
                        layoutx.setHTML(rowFilesQuota, 1,
                            SafeHtmlUtils.fromString(convertMapToNiceString(a.getValueAsMap())).asString() +
                                " (default: Not set)");
                      }
                      quotaChangeButton.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                          session.getTabManager().addTabToCurrentTab(
                              new RequestQuotaChangeTabItem(resource, user, QuotaType.FILES,
                                  convertMapToNiceString(a.getValueAsMap()),
                                  convertMapToNiceString(new HashMap<String, JSONValue>())));
                        }
                      });
                    }

                    layoutx.setWidget(rowFilesQuota, 2, quotaChangeButton);
                  }
                });
                defaultAttr.setIds(ids);
                defaultAttr.retrieveData();
                empty = false;
                row++;

                layoutx.setHTML(row, 1,
                    "Quotas are using \"soft limit : hard limit\" notation and are bound to specific volume.");
                layoutx.getFlexCellFormatter().setStyleName(row, 1, "inputFormInlineComment");
                layoutx.getFlexCellFormatter().setColSpan(row, 1, 2);
                row++;

                break;
              }
            }
            for (final Attribute a : attrs) {
              if (a.getFriendlyName().equalsIgnoreCase("optOutMailingList")) {
                layoutx.setHTML(row, 0, "<strong>Mailing: </strong>");
                // find attribute with settings
                final CheckBox exclude = new CheckBox("Exclude me from this mailing list");
                exclude.setValue(Boolean.parseBoolean(a.getValue()));
                final int rowMail = row;
                exclude.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                  @Override
                  public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if (exclude.getValue()) {
                      a.setValue("true");
                    } else {
                      a.setValue(null);
                    }
                    Map<String, Integer> ids = new HashMap<String, Integer>();
                    ids.put("resource", resource.getId());
                    ids.put("member", mem.getId());
                    ArrayList<Attribute> ls = new ArrayList<Attribute>();
                    ls.add(a);
                    SetAttributes set = new SetAttributes(new JsonCallbackEvents() {
                      @Override
                      public void onFinished(JavaScriptObject jso) {
                        layoutx.setWidget(rowMail, 1, exclude);
                      }

                      @Override
                      public void onLoadingStart() {
                        layoutx.setWidget(rowMail, 1, new AjaxLoaderImage(true));
                      }

                      @Override
                      public void onError(PerunError error) {
                        layoutx.setWidget(rowMail, 1, exclude);
                        // change back since we were not able to change value in Perun
                        exclude.setValue(!exclude.getValue());
                      }
                    });
                    set.setAttributes(ids, ls);
                  }
                });
                // enable only if opt-out is allowed
                if (disableOptOut) {
                  layoutx.setHTML(row, 1, "Your membership in this mailing list is mandatory.");
                } else {
                  layoutx.setWidget(row, 1, exclude);
                }
                empty = false;
              }
            }
            if (empty) {
              layoutx.setHTML(0, 0, "<strong>No settings available for this resource.</strong>");
              layoutx.getFlexCellFormatter().setColSpan(0, 0, 3);
            }
          }
        });
        attributes.retrieveData();
      }

      public void onLoadingStart() {
        AjaxLoaderImage loader = new AjaxLoaderImage(true);
        layoutx.setWidget(0, 0, loader.loadingStart());
      }

      public void onError(PerunError error) {
        AjaxLoaderImage loader = new AjaxLoaderImage(true);
        layoutx.setWidget(0, 0, loader.loadingError(error));
      }
    });

    callMember.retrieveData();

  }

  private String convertMapToNiceString(Map<String, JSONValue> quotaMap) {

    StringBuilder result = new StringBuilder();
    for (String key : quotaMap.keySet()) {
      String val = quotaMap.get(key).toString();
      val = val.replaceAll("\"", "").replaceAll(":", " : ");
      val = val.replaceAll("K", " KiB").replaceAll("M", " MiB").replaceAll("G", " GiB").replaceAll("T", " TiB")
          .replaceAll("E", " EiB");
      result.append(val).append(" ");
      // FIXME - will we have multiple volumes per facility ??
      //result.append(val).append(" at ").append(key).append(" ");
    }
    return result.toString();

  }

  /**
   * Set selected VO later on
   *
   * @param vo
   */
  public void setVo(VirtualOrganization vo) {
    this.vo = vo;
  }

  public Widget getWidget() {
    return this.contentWidget;
  }

  public Widget getTitle() {
    return this.titleWidget;
  }

  public ImageResource getIcon() {
    return SmallIcons.INSTANCE.settingToolsIcon();
  }

  @Override
  public int hashCode() {
    final int prime = 1249;
    int result = 432;
    result = prime * result;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    if (this.userId != ((SelfResourcesSettingsTabItem) obj).userId) {
      return false;
    }
    if (this.voId != ((SelfResourcesSettingsTabItem) obj).voId) {
      return false;
    }
    return true;
  }

  public boolean multipleInstancesEnabled() {
    return false;
  }

  public void open() {
    session.setActiveUser(user);
    session.getUiElements().getMenu().openMenu(MainMenu.USER);
    session.getUiElements().getBreadcrumbs()
        .setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()),
            UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "Resources settings",
            getUrlWithParameters());
  }

  public boolean isAuthorized() {

    if (session.isSelf(userId)) {
      return true;
    } else {
      return false;
    }

  }

  public String getUrl() {
    return URL;
  }

  public String getUrlWithParameters() {
    return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId + "&vo=" + voId;
  }

}
