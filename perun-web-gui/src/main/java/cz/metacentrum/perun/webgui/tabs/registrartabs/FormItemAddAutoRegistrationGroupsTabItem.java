package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllSubGroups;
import cz.metacentrum.perun.webgui.json.registrarManager.AddGroupsToAutoRegistration;
import cz.metacentrum.perun.webgui.json.registrarManager.GetGroupsToAutoRegistration;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class FormItemAddAutoRegistrationGroupsTabItem implements TabItem {

  //data
  private final PerunEntity entity;
  private final int id;
  private final int formItem;
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
  private Label titleWidget = new Label("Add groups for auto registration - form item");

  public FormItemAddAutoRegistrationGroupsTabItem(PerunEntity entity, int id, int formItem) {
    this.id = id;
    this.entity = entity;
    this.formItem = formItem;
  }

  @Override
  public Widget draw() {
    titleWidget.setText("Add groups for auto registration - form item");

    VerticalPanel vp = new VerticalPanel();
    vp.setSize("100%", "100%");

    // menu
    TabMenu menu = new TabMenu();
    menu.addWidget(new HTML(""));

    if (PerunEntity.GROUP.equals(entity)) {
      return setUpForGroup(vp, menu);
    } else {
      return setUpForVo(vp, menu);
    }
  }

  private Widget setUpForGroup(VerticalPanel vp, TabMenu menu) {
    final GetAllSubGroups groups = new GetAllSubGroups(id);
    // remove already added union groups from offering
    JsonCallbackEvents localEvents = new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        // second callback
        final GetGroupsToAutoRegistration alreadySet =
            new GetGroupsToAutoRegistration(entity, id, formItem, new JsonCallbackEvents() {
              public void onFinished(JavaScriptObject jso) {
                JsArray<Group> esToRemove = JsonUtils.jsoAsArray(jso);
                for (int i = 0; i < esToRemove.length(); i++) {
                  groups.removeFromTable(esToRemove.get(i));
                }
              }
            });
        alreadySet.retrieveData();
      }
    };
    groups.setEvents(localEvents);

    final ExtendedSuggestBox box = new ExtendedSuggestBox(groups.getOracle());

    // button
    final CustomButton assignButton =
        TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedExtSource());
    final TabItem tab = this;

    assignButton.addClickHandler(event -> {
      final ArrayList<Group> availableGroups = groups.getTableSelectedList();
      if (UiElements.cantSaveEmptyListDialogBox(availableGroups)) {
        AddGroupsToAutoRegistration request = new AddGroupsToAutoRegistration(
            JsonCallbackEvents.disableButtonEvents(assignButton, new JsonCallbackEvents() {
              @Override
              public void onFinished(JavaScriptObject jso) {
                // clear search
                box.getSuggestBox().setText("");
                groups.retrieveData();
              }
            }));
        request.setAutoRegGroups(availableGroups, PerunEntity.GROUP.equals(entity) ? id : null, formItem);
      }
    });

    menu.addFilterWidget(box, new PerunSearchEvent() {
      @Override
      public void searchFor(String text) {
        groups.filterTable(text);
      }
    }, "Filter by name");

    menu.addWidget(assignButton);

    menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "",
        clickEvent -> session.getTabManager().closeTab(tab, isRefreshParentOnClose())));

    vp.add(menu);
    vp.setCellHeight(menu, "30px");

    CellTable<Group> table = groups.getTable();

    assignButton.setEnabled(false);
    JsonUtils.addTableManagedButton(groups, table, assignButton);

    table.addStyleName("perun-table");
    ScrollPanel sp = new ScrollPanel(table);
    sp.addStyleName("perun-tableScrollPanel");
    vp.add(sp);

    // do not use resizePerunTable() when tab is in overlay - wrong width is calculated
    session.getUiElements().resizePerunTable(sp, 350, this);

    this.contentWidget.setWidget(vp);

    return getWidget();
  }

  private Widget setUpForVo(VerticalPanel vp, TabMenu menu) {
    final GetAllGroups groups = new GetAllGroups(id);
    groups.setCoreGroupsCheckable(false);

    // remove already added union groups from offering
    JsonCallbackEvents localEvents = new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        // second callback
        final GetGroupsToAutoRegistration alreadySet =
            new GetGroupsToAutoRegistration(entity, id, formItem, new JsonCallbackEvents() {
              public void onFinished(JavaScriptObject jso) {
                JsArray<Group> esToRemove = JsonUtils.jsoAsArray(jso);
                for (int i = 0; i < esToRemove.length(); i++) {
                  groups.removeFromTable(esToRemove.get(i));
                }
              }
            });
        alreadySet.retrieveData();
      }
    };
    groups.setEvents(localEvents);

    final ExtendedSuggestBox box = new ExtendedSuggestBox(groups.getOracle());

    // button
    final CustomButton assignButton =
        TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedExtSource());
    final TabItem tab = this;

    assignButton.addClickHandler(event -> {
      final ArrayList<Group> availableGroups = groups.getTableSelectedList();
      if (UiElements.cantSaveEmptyListDialogBox(availableGroups)) {
        AddGroupsToAutoRegistration request = new AddGroupsToAutoRegistration(
            JsonCallbackEvents.disableButtonEvents(assignButton, new JsonCallbackEvents() {
              @Override
              public void onFinished(JavaScriptObject jso) {
                // clear search
                box.getSuggestBox().setText("");
                groups.retrieveData();
              }
            }));
        request.setAutoRegGroups(availableGroups, PerunEntity.GROUP.equals(entity) ? id : null, formItem);
      }
    });

    menu.addFilterWidget(box, new PerunSearchEvent() {
      @Override
      public void searchFor(String text) {
        groups.filterTable(text);
      }
    }, "Filter by name");

    menu.addWidget(assignButton);

    menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "",
        clickEvent -> session.getTabManager().closeTab(tab, isRefreshParentOnClose())));

    vp.add(menu);
    vp.setCellHeight(menu, "30px");

    CellTable<Group> table = groups.getTable();

    assignButton.setEnabled(false);
    JsonUtils.addTableManagedButton(groups, table, assignButton);

    table.addStyleName("perun-table");
    ScrollPanel sp = new ScrollPanel(table);
    sp.addStyleName("perun-tableScrollPanel");
    vp.add(sp);

    // do not use resizePerunTable() when tab is in overlay - wrong width is calculated
    session.getUiElements().resizePerunTable(sp, 350, this);

    this.contentWidget.setWidget(vp);

    return getWidget();
  }

  @Override
  public Widget getWidget() {
    return this.contentWidget;
  }

  @Override
  public Widget getTitle() {
    return this.titleWidget;
  }

  @Override
  public ImageResource getIcon() {
    return SmallIcons.INSTANCE.addIcon();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FormItemAddAutoRegistrationGroupsTabItem that = (FormItemAddAutoRegistrationGroupsTabItem) o;
    return id == that.id && entity == that.entity && formItem == that.formItem;
  }

  @Override
  public int hashCode() {
    return Objects.hash(entity, id, formItem);
  }

  @Override
  public boolean multipleInstancesEnabled() {
    return false;
  }

  @Override
  public void open() {
    if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
      session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
      session.setActiveVoId(id);
    }
    if (PerunEntity.GROUP.equals(entity)) {
      session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
      session.setActiveGroupId(id);
    }
  }

  @Override
  public boolean isAuthorized() {
    if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
      return session.isVoAdmin(id);
    }
    if (PerunEntity.GROUP.equals(entity)) {
      return session.isGroupAdmin(id);
    }
    return false;
  }

  @Override
  public boolean isPrepared() {
    return true;
  }

  @Override
  public boolean isRefreshParentOnClose() {
    return true;
  }

  @Override
  public void onClose() {

  }
}

