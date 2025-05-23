package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all services on facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetFacilityAssignedServices implements JsonCallback, JsonCallbackTable<Service> {

  // JSON URL
  static private final String JSON_URL = "servicesManager/getAssignedServices";
  // Selection model
  final MultiSelectionModel<Service> selectionModel =
      new MultiSelectionModel<Service>(new GeneralKeyProvider<Service>());
  // Session
  private PerunWebSession session = PerunWebSession.getInstance();
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  // Table field updater
  private FieldUpdater<Service, String> tableFieldUpdater;
  // data providers
  private ListDataProvider<Service> dataProvider = new ListDataProvider<Service>();
  private ArrayList<Service> list = new ArrayList<Service>();
  private PerunTable<Service> table;
  private int facilityId = 0;
  private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

  /**
   * New instance of callback
   *
   * @param facilityId ID of facility to get services for
   */
  public GetFacilityAssignedServices(int facilityId) {
    this.facilityId = facilityId;
  }

  /**
   * New instance of callback with custom events
   *
   * @param facilityId ID of facility to get services for
   * @param events     custom events
   */
  public GetFacilityAssignedServices(int facilityId, JsonCallbackEvents events) {
    this.events = events;
    this.facilityId = facilityId;
  }

  /**
   * Returns table of assigned services on facility with custom onClick
   *
   * @param fu custom onClick (field updater)
   */
  public CellTable<Service> getTable(FieldUpdater<Service, String> fu) {
    this.tableFieldUpdater = fu;
    return this.getTable();
  }

  /**
   * Return table with assigned services on facility
   *
   * @return table widget
   */
  public CellTable<Service> getTable() {

    retrieveData();

    // Table data provider.
    dataProvider = new ListDataProvider<Service>(list);

    // Cell table
    table = new PerunTable<Service>(list);

    // Connect the table to the data provider.
    dataProvider.addDataDisplay(table);

    // Sorting
    ListHandler<Service> columnSortHandler = new ListHandler<Service>(dataProvider.getList());
    table.addColumnSortHandler(columnSortHandler);

    // table selection
    table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Service>createCheckboxManager());

    // set empty content & loader
    table.setEmptyTableWidget(loaderImage);

    // checkbox column column
    table.addCheckBoxColumn();

    Column<Service, String> enabledColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
      public String getValue(Service serv) {
        return serv.isEnabled() ? "ENABLED" : "DISABLED";
      }
    }, tableFieldUpdater);

    enabledColumn.setSortable(true);
    columnSortHandler.setComparator(enabledColumn, new Comparator<Service>() {
      public int compare(Service o1, Service o2) {
        return (o1.isEnabled() ? "ENABLED" : "DISABLED").compareToIgnoreCase((o2.isEnabled() ? "ENABLED" : "DISABLED"));
      }
    });

    Column<Service, String> expiredVoMembersColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
      public String getValue(Service serv) {
        return serv.getUseExpiredVoMembers() ? "YES" : "NO";
      }
    }, tableFieldUpdater);

    expiredVoMembersColumn.setSortable(true);
    columnSortHandler.setComparator(expiredVoMembersColumn, new Comparator<Service>() {
      public int compare(Service o1, Service o2) {
        return (o1.getUseExpiredVoMembers() ? "YES" : "NO").compareToIgnoreCase((o2.getUseExpiredVoMembers() ? "YES" : "NO"));
      }
    });

    Column<Service, String> expiredGroupMembersColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
      public String getValue(Service serv) {
        return serv.getUseExpiredMembers() ? "YES" : "NO";
      }
    }, tableFieldUpdater);

    expiredGroupMembersColumn.setSortable(true);
    columnSortHandler.setComparator(expiredGroupMembersColumn, new Comparator<Service>() {
      public int compare(Service o1, Service o2) {
        return (o1.getUseExpiredMembers() ? "YES" : "NO").compareToIgnoreCase((o2.getUseExpiredMembers() ? "YES" : "NO"));
      }
    });

    Column<Service, String> scriptColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
      public String getValue(Service serv) {
        return serv.getScriptPath();
      }
    }, tableFieldUpdater);

    scriptColumn.setSortable(true);
    columnSortHandler.setComparator(scriptColumn, new Comparator<Service>() {
      public int compare(Service o1, Service o2) {
        return o1.getScriptPath().compareToIgnoreCase(o2.getScriptPath());
      }
    });

    table.addIdColumn("Service Id", tableFieldUpdater, 110);

    table.addNameColumn(tableFieldUpdater);
    table.addColumn(enabledColumn, "Enabled");
    table.addColumn(scriptColumn, "Script");
    table.addColumn(expiredVoMembersColumn, "Provision expired VO members");
    table.addColumn(expiredGroupMembersColumn, "Provision expired group members");
    table.addDescriptionColumn(tableFieldUpdater);
    return table;

  }

  /**
   * Retrieve data from RPC
   */
  public void retrieveData() {
    JsonClient js = new JsonClient();
    js.retrieveData(JSON_URL, "facility=" + facilityId, this);
  }

  /**
   * Sorts table by objects Name
   */
  public void sortTable() {
    list = new TableSorter<Service>().sortByName(getList());
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Add object as new row to table
   *
   * @param object Service to be added as new row
   */
  public void addToTable(Service object) {
    list.add(object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Removes object as row from table
   *
   * @param object Service to be removed as row
   */
  public void removeFromTable(Service object) {
    list.remove(object);
    selectionModel.getSelectedSet().remove(object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Clear all table content
   */
  public void clearTable() {
    loaderImage.loadingStart();
    list.clear();
    selectionModel.clear();
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Clears list of selected items
   */
  public void clearTableSelectedSet() {
    selectionModel.clear();
  }

  /**
   * Return selected items from list
   *
   * @return return list of checked items
   */
  public ArrayList<Service> getTableSelectedList() {
    return JsonUtils.setToList(selectionModel.getSelectedSet());
  }

  /**
   * Called, when an error occurs
   */
  public void onError(PerunError error) {
    session.getUiElements().setLogErrorText("Error while loading facility services.");
    loaderImage.loadingError(error);
    events.onError(error);
  }

  /**
   * Called, when loading starts
   */
  public void onLoadingStart() {
    session.getUiElements().setLogText("Loading facility services started.");
    events.onLoadingStart();
  }

  /**
   * Called, when operation finishes successfully.
   */
  public void onFinished(JavaScriptObject jso) {
    setList(JsonUtils.<Service>jsoAsList(jso));
    sortTable();
    session.getUiElements().setLogText("Facility services loaded: " + list.size());
    events.onFinished(jso);
    loaderImage.loadingFinished();
  }

  public void insertToTable(int index, Service object) {
    list.add(index, object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  public void setEditable(boolean editable) {
    // TODO Auto-generated method stub
  }

  public void setCheckable(boolean checkable) {
    // TODO Auto-generated method stub
  }

  public ArrayList<Service> getList() {
    return this.list;
  }

  public void setList(ArrayList<Service> list) {
    clearTable();
    this.list.addAll(list);
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Sets different facility ID for callback after creation
   *
   * @param facilityId new facility ID
   */
  public void setFacility(int facilityId) {
    this.facilityId = facilityId;
  }

  /**
   * Sets events after callback creation
   *
   * @param events external events
   */
  public void setEvents(JsonCallbackEvents events) {
    this.events = events;
  }

}
