package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.DeleteFacility;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetFacilities;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Page with Facilities management for Perun Admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilitiesSelectTabItem implements TabItem, TabItemWithUrl {

  public static final String URL = "list";
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
  private Label titleWidget = new Label("Facilities");

  /**
   * Creates a tab instance
   */
  public FacilitiesSelectTabItem() {
  }

  static public FacilitiesSelectTabItem load(Map<String, String> parameters) {
    return new FacilitiesSelectTabItem();
  }

  public boolean isPrepared() {
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

    // MAIN PANEL
    VerticalPanel firstTabPanel = new VerticalPanel();
    firstTabPanel.setSize("100%", "100%");

    // TAB MENU
    TabMenu tabMenu = new TabMenu();

    // get RICH facilities request
    final GetFacilities facilities = new GetFacilities(true);
    final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(facilities);

    // retrieve data (table)
    final CellTable<Facility> table = facilities.getTable(new FieldUpdater<Facility, String>() {
      public void update(int index, Facility object, String value) {
        session.getTabManager().addTab(new FacilityDetailTabItem(object));
      }
    });

    tabMenu.addWidget(UiElements.getRefreshButton(this));

    // add new facility button
    tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createFacility(),
        new ClickHandler() {
          public void onClick(ClickEvent event) {
            session.getTabManager().addTab(new CreateFacilityTabItem(facilities.getFullBackupList(), events));
          }
        }));

    // add delete facilities button
    final CustomButton deleteButton =
        TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteFacilities());
    if (session.isPerunAdmin()) {
      tabMenu.addWidget(deleteButton);
    }

    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final ArrayList<Facility> list = facilities.getTableSelectedList();
        String text = "Following facilities will be deleted.";
        UiElements.showDeleteConfirm(list, text, new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
            for (int i = 0; i < list.size(); i++) {
              if (i == list.size() - 1) {
                DeleteFacility request =
                    new DeleteFacility(JsonCallbackEvents.disableButtonEvents(deleteButton, events));
                request.deleteFacility(list.get(i).getId());
              } else {
                DeleteFacility request = new DeleteFacility(JsonCallbackEvents.disableButtonEvents(deleteButton));
                request.deleteFacility(list.get(i).getId());
              }
            }
          }
        });
      }
    });

    // filter box
    tabMenu.addFilterWidget(new ExtendedSuggestBox(facilities.getOracle()), new PerunSearchEvent() {
      public void searchFor(String text) {
        facilities.filterTable(text);
      }
    }, ButtonTranslation.INSTANCE.filterFacilities());

    tabMenu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
    tabMenu.addWidget(new HTML("<strong>Please select Facility you want to manage.</strong>"));

    deleteButton.setEnabled(false);
    JsonUtils.addTableManagedButton(facilities, table, deleteButton);

    // add a class to the table and wrap it into scroll panel
    table.addStyleName("perun-table");
    ScrollPanel sp = new ScrollPanel(table);
    sp.addStyleName("perun-tableScrollPanel");

    // add menu and the table to the main panel
    firstTabPanel.add(tabMenu);
    firstTabPanel.setCellHeight(tabMenu, "30px");
    firstTabPanel.add(sp);
    firstTabPanel.setCellHeight(sp, "100%");

    session.getUiElements().resizePerunTable(sp, 350, this);

    this.contentWidget.setWidget(firstTabPanel);

    return getWidget();
  }

  public Widget getWidget() {
    return this.contentWidget;
  }

  public Widget getTitle() {
    return this.titleWidget;
  }

  public ImageResource getIcon() {
    return SmallIcons.INSTANCE.databaseServerIcon();
  }

  @Override
  public int hashCode() {
    final int prime = 709;
    int result = 1;
    result = prime * result + 12341;
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
    return true;
  }

  public boolean multipleInstancesEnabled() {
    return false;
  }

  public void open() {
    session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN, true);
    session.getUiElements().getBreadcrumbs()
        .setLocation(MainMenu.FACILITY_ADMIN, "Select facility", getUrlWithParameters());
  }

  public boolean isAuthorized() {

    if (session.isFacilityAdmin()) {
      return true;
    } else {
      return false;
    }

  }

  public String getUrl() {
    return URL;
  }

  public String getUrlWithParameters() {
    return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
  }

}
