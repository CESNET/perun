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
import cz.metacentrum.perun.webgui.json.JsonCallbackOracle;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get services which have some attribute definition set as "required".
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetServicesByAttrDefinition
    implements JsonCallback, JsonCallbackTable<Service>, JsonCallbackOracle<Service> {

  // Selection model
  final MultiSelectionModel<Service> selectionModel =
      new MultiSelectionModel<Service>(new GeneralKeyProvider<Service>());
  // json url for services
  private final String JSON_URL = "servicesManager/getServicesByAttributeDefinition";
  // session
  private PerunWebSession session = PerunWebSession.getInstance();
  // Data provider and tables
  private ListDataProvider<Service> dataProvider = new ListDataProvider<Service>();
  private PerunTable<Service> table;
  private ArrayList<Service> list = new ArrayList<Service>();
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  // Table field updater
  private FieldUpdater<Service, String> tableFieldUpdater;
  private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
  private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
  private ArrayList<Service> backupList = new ArrayList<Service>();
  private boolean checkable = true;
  private int id = 0;

  /**
   * Creates a new ajax query
   */
  public GetServicesByAttrDefinition(int attrDefId) {
    this.id = attrDefId;
  }

  /**
   * Creates a new ajax query with custom events
   *
   * @param events external events for this query
   */
  public GetServicesByAttrDefinition(int attrDefId, JsonCallbackEvents events) {
    this.id = attrDefId;
    this.events = events;
  }

  /**
   * Returns table of services with custom onClick
   *
   * @param fu field updater
   * @return table widget
   */
  public CellTable<Service> getTable(FieldUpdater<Service, String> fu) {
    this.tableFieldUpdater = fu;
    return this.getTable();
  }

  /**
   * Returns empty table of services with custom onClick
   *
   * @param fu field updater
   * @return empty table widget
   */
  public CellTable<Service> getEmptyTable(FieldUpdater<Service, String> fu) {
    this.tableFieldUpdater = fu;
    return this.getEmptyTable();
  }

  /**
   * Returns table of services
   *
   * @return table widget
   */
  public CellTable<Service> getTable() {

    retrieveData();
    return this.getEmptyTable();
  }

  /**
   * Returns empty table of services
   *
   * @return empty table widget
   */
  public CellTable<Service> getEmptyTable() {

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
    loaderImage.setEmptyResultMessage("Attribute is not required by any service.");

    // checkbox column column
    if (checkable) {
      table.addCheckBoxColumn();
    }

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
    String params = "attributeDefinition=" + id;
    js.retrieveData(JSON_URL, params, this);
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
    oracle.add(object.getName());
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
    oracle.clear();
    backupList.clear();
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
    session.getUiElements().setLogErrorText("Error while loading services.");
    loaderImage.loadingError(error);
    events.onError(error);
  }

  /**
   * Called, when loading starts
   */
  public void onLoadingStart() {
    session.getUiElements().setLogText("Loading services started.");
    events.onLoadingStart();
  }

  /**
   * Called, when operation finishes successfully.
   */
  public void onFinished(JavaScriptObject jso) {
    setList(JsonUtils.<Service>jsoAsList(jso));
    sortTable();
    session.getUiElements().setLogText("Services loaded: " + list.size());
    events.onFinished(jso);
    loaderImage.loadingFinished();
  }

  public void insertToTable(int index, Service object) {
    list.add(index, object);
    oracle.add(object.getName());
    dataProvider.flush();
    dataProvider.refresh();
  }

  public void setEditable(boolean editable) {
    // TODO Auto-generated method stub
  }

  public void setCheckable(boolean checkable) {
    this.checkable = checkable;
  }

  public ArrayList<Service> getList() {
    return this.list;
  }

  public void setList(ArrayList<Service> list) {
    clearTable();
    this.list.addAll(list);
    for (Service s : list) {
      oracle.add(s.getName());
    }
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Sets external events for callback after it's creation
   *
   * @param externalEvent external events
   */
  public void setEvents(JsonCallbackEvents externalEvent) {
    events = externalEvent;
  }

  @Override
  public void filterTable(String filter) {

    // store list only for first time
    if (backupList.isEmpty() || backupList == null) {
      backupList.addAll(list);
    }

    // always clear selected items
    selectionModel.clear();
    list.clear();

    if (filter.equalsIgnoreCase("")) {
      list.addAll(backupList);
    } else {
      for (Service s : backupList) {
        // store facility by filter
        if (s.getName().toLowerCase().startsWith(filter.toLowerCase())) {
          list.add(s);
        }
      }
    }

    dataProvider.flush();
    dataProvider.refresh();
    loaderImage.loadingFinished();

  }

  @Override
  public UnaccentMultiWordSuggestOracle getOracle() {
    return this.oracle;
  }

  @Override
  public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
    this.oracle = oracle;
  }
}
