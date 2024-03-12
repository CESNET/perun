package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.ServicesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;
import java.util.Map;

/**
 * Tab with service details and complete management
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ServiceDetailTabItem implements TabItem, TabItemWithUrl {

  public static final String URL = "detail";
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
  private Label titleWidget = new Label("Loading service");
  // data
  private Service service;
  private int serviceId;
  /**
   * Small tab panel
   */
  private TabPanelForTabItems tabPanel;

  /**
   * Tab with service details and complete management
   *
   * @param service service to get details for
   */
  public ServiceDetailTabItem(Service service) {
    this.service = service;
    this.serviceId = service.getId();
    this.tabPanel = new TabPanelForTabItems(this);
  }

  /**
   * Tab with service details and complete management
   *
   * @param serviceId service to get details for
   */
  public ServiceDetailTabItem(int serviceId) {
    this.serviceId = serviceId;
    new GetEntityById(PerunEntity.SERVICE, serviceId, new JsonCallbackEvents() {
      public void onFinished(JavaScriptObject jso) {
        service = jso.cast();
      }
    }).retrieveData();
    this.tabPanel = new TabPanelForTabItems(this);
  }

  static public ServiceDetailTabItem load(Map<String, String> parameters) {
    int id = Integer.parseInt(parameters.get("id"));
    return new ServiceDetailTabItem(id);
  }

  public boolean isPrepared() {
    return !(service == null);
  }

  @Override
  public boolean isRefreshParentOnClose() {
    return false;
  }

  @Override
  public void onClose() {

  }

  public Widget draw() {

    this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(service.getName()));

    // main widget panel
    final VerticalPanel vp = new VerticalPanel();
    vp.setSize("100%", "100%");

    AbsolutePanel dp = new AbsolutePanel();
    final FlexTable menu = new FlexTable();
    menu.setCellSpacing(5);

    // Add service information
    menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.trafficLightsIcon()));
    Label serviceName = new Label();
    serviceName.setText(Utils.getStrippedStringWithEllipsis(service.getName(), 40));
    serviceName.setStyleName("now-managing");
    serviceName.setTitle(service.getName());
    menu.setWidget(0, 1, serviceName);

    menu.setHTML(0, 2, "&nbsp;");
    menu.getFlexCellFormatter().setWidth(0, 2, "25px");

    int column = 3;

    final JsonCallbackEvents events = new JsonCallbackEvents() {
      public void onFinished(JavaScriptObject jso) {
        new GetEntityById(PerunEntity.SERVICE, serviceId, new JsonCallbackEvents() {
          public void onFinished(JavaScriptObject jso) {
            service = jso.cast();
            open();
            draw();
          }
        }).retrieveData();
      }
    };

    CustomButton change = new CustomButton("", ButtonTranslation.INSTANCE.editFacilityDetails(),
        SmallIcons.INSTANCE.applicationFormEditIcon());
    change.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // prepare confirm content
        session.getTabManager().addTabToCurrentTab(new EditServiceDetailsTabItem(service, events));
      }
    });
    menu.setWidget(0, column, change);

    column++;

    menu.setHTML(0, column, "&nbsp;");
    menu.getFlexCellFormatter().setWidth(0, column, "25px");
    column++;

    if (JsonUtils.isExtendedInfoVisible()) {
      menu.setHTML(0, column,
          "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">" + service.getId() + "</span>");
      column++;
      menu.setHTML(0, column, "&nbsp;");
      menu.getFlexCellFormatter().setWidth(0, column, "25px");
      column++;
    }

    menu.setHTML(0, column,
        "<strong>Description:</strong><br/><span class=\"inputFormInlineComment\">" + service.getDescription() +
            "</span>");

    dp.add(menu);
    vp.add(dp);
    vp.setCellHeight(dp, "30px");

    // TAB PANEL

    tabPanel.clear();

    tabPanel.add(new ServiceRequiredAttributesTabItem(service), "Required attributes");
    tabPanel.add(new ServiceDestinationsTabItem(service), "Destinations");

    // Resize must be called after page fully displays
    Scheduler.get().scheduleDeferred(new Command() {
      @Override
      public void execute() {
        tabPanel.finishAdding();
      }
    });

    vp.add(tabPanel);
    vp.setCellHeight(tabPanel, "100%");

    // add tabs to the main panel
    this.contentWidget.setWidget(vp);

    return getWidget();
  }

  public Widget getWidget() {
    return this.contentWidget;
  }

  public Widget getTitle() {
    return this.titleWidget;
  }

  public ImageResource getIcon() {
    return SmallIcons.INSTANCE.trafficLightsIcon();
  }

  @Override
  public int hashCode() {
    final int prime = 1087;
    int result = 1;
    result = prime * result + serviceId;
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
    ServiceDetailTabItem other = (ServiceDetailTabItem) obj;
    if (serviceId != other.serviceId) {
      return false;
    }
    return true;
  }

  public boolean multipleInstancesEnabled() {
    return false;
  }

  public void open() {
    session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
    session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Services",
        ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + ServicesTabItem.URL);
  }

  public boolean isAuthorized() {

    if (session.isPerunAdmin()) {
      return true;
    } else {
      return false;
    }

  }

  public String getUrl() {
    return URL;
  }

  public String getUrlWithParameters() {
    return ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + serviceId;
  }
}
