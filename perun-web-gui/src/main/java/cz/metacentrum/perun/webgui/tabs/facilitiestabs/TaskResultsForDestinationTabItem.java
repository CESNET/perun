package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.tasksManager.DeleteTaskResults;
import cz.metacentrum.perun.webgui.json.tasksManager.GetRichTaskResultsByTaskAndDestination;
import cz.metacentrum.perun.webgui.model.Destination;
import cz.metacentrum.perun.webgui.model.Task;
import cz.metacentrum.perun.webgui.model.TaskResult;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class TaskResultsForDestinationTabItem implements TabItem, TabItemWithUrl {

  public static final String URL = "taskresultsdetail";

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
  private Label titleWidget = new Label("Loading Task");

  // data
  private Destination destination;
  private Task task;
  private int destinationId;
  private int taskId;

  public TaskResultsForDestinationTabItem(Task task, Destination destination) {
    this.destination = destination;
    this.task = task;
    this.destinationId = destination.getId();
    this.taskId = task.getId();
  }

  public TaskResultsForDestinationTabItem(int taskId, int destinationId) {
    this.taskId = taskId;
    this.destinationId = destinationId;
    new GetEntityById(PerunEntity.DESTINATION, destinationId, new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        destination = jso.cast();
      }
    }).retrieveData();
    new GetEntityById(PerunEntity.TASK, taskId, new JsonCallbackEvents() {
      @Override
      public void onFinished(JavaScriptObject jso) {
        task = jso.cast();
      }
    }).retrieveData();
  }

  public static TaskResultsForDestinationTabItem load(Task task, Destination destination) {
    return new TaskResultsForDestinationTabItem(task, destination);
  }

  public static TaskResultsForDestinationTabItem load(Map<String, String> parameters) {
    int tid = Integer.parseInt(parameters.get("id"));
    int did = Integer.parseInt(parameters.get("destinationId"));
    return new TaskResultsForDestinationTabItem(tid, did);
  }

  @Override
  public String getUrl() {
    return URL;
  }

  @Override
  public String getUrlWithParameters() {
    return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + task.getId() + "&destinationId=" +
        destinationId;
  }

  @Override
  public Widget draw() {
    this.titleWidget.setText(destination.getDestination() + ": " + task.getService().getName());

    VerticalPanel vp = new VerticalPanel();
    vp.setSize("100%", "100%");

    final GetRichTaskResultsByTaskAndDestination callback =
        new GetRichTaskResultsByTaskAndDestination(task.getId(), destinationId);

    // refresh table events
    final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(callback);

    TabMenu menu = new TabMenu();
    menu.addWidget(UiElements.getRefreshButton(this));

    final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove all TaskResults");
    removeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final ArrayList<TaskResult> tasksResultsToDelete = callback.getTableSelectedList();
        String text = "Following TaskResults will be deleted.";
        UiElements.showDeleteConfirm(tasksResultsToDelete, text, new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
            for (int i = 0; i < tasksResultsToDelete.size(); i++) {
              if (i == tasksResultsToDelete.size() - 1) {
                DeleteTaskResults request =
                    new DeleteTaskResults(JsonCallbackEvents.disableButtonEvents(removeButton, events));
                request.deleteTaskResult(tasksResultsToDelete.get(i).getId());
              } else {
                DeleteTaskResults request = new DeleteTaskResults(JsonCallbackEvents.disableButtonEvents(removeButton));
                request.deleteTaskResult(tasksResultsToDelete.get(i).getId());
              }
            }
          }
        });
      }
    });
    menu.addWidget(removeButton);

    CellTable<TaskResult> table = callback.getTable();

    table.addStyleName("perun-table");
    ScrollPanel sp = new ScrollPanel(table);
    sp.addStyleName("perun-tableScrollPanel");

    vp.add(menu);
    vp.setCellHeight(menu, "30px");
    vp.add(sp);

    session.getUiElements().resizePerunTable(sp, 350, this);

    removeButton.setEnabled(false);
    JsonUtils.addTableManagedButton(callback, table, removeButton);

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
    return SmallIcons.INSTANCE.databaseServerIcon();
  }

  @Override
  public boolean multipleInstancesEnabled() {
    return false;
  }

  @Override
  public void open() {
    session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
    session.getUiElements().getBreadcrumbs().setLocation(task.getFacility(),
        "Propagation results detail: " + destination.getDestination() + " - " + task.getService().getName(),
        getUrlWithParameters());
  }

  @Override
  public boolean isAuthorized() {
    return session.isFacilityAdmin(task.getFacility().getId());
  }

  @Override
  public boolean isPrepared() {
    return task != null && destination != null;
  }

  @Override
  public boolean isRefreshParentOnClose() {
    return false;
  }

  @Override
  public void onClose() {

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskResultsForDestinationTabItem that = (TaskResultsForDestinationTabItem) o;
    return taskId == that.taskId &&
        destinationId == that.destinationId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskId, destinationId);
  }
}
