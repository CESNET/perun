package cz.metacentrum.perun.webgui.json.usersManager;

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
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get list of users for service user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetUsersBySpecificUser implements JsonCallback, JsonCallbackTable<User> {

  // json url
  static private final String JSON_URL = "usersManager/getUsersBySpecificUser";
  // Selection model
  final MultiSelectionModel<User> selectionModel = new MultiSelectionModel<User>(new GeneralKeyProvider<User>());
  // session
  private PerunWebSession session = PerunWebSession.getInstance();
  private int userId;
  // Data provider
  private ListDataProvider<User> dataProvider = new ListDataProvider<User>();
  // table
  private PerunTable<User> table;
  // table data
  private ArrayList<User> list = new ArrayList<User>();
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  // Table field updater
  private FieldUpdater<User, String> tableFieldUpdater;
  // loader image
  private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
  private boolean checkable = true;

  /**
   * Creates a new request
   *
   * @param userId
   */
  public GetUsersBySpecificUser(int userId) {
    this.userId = userId;
  }

  /**
   * Creates a new request with custom events
   *
   * @param userId
   * @param events
   */
  public GetUsersBySpecificUser(int userId, JsonCallbackEvents events) {
    this.userId = userId;
    this.events = events;
  }

  /**
   * Returns table of users
   *
   * @param
   */
  public CellTable<User> getTable(FieldUpdater<User, String> fu) {
    this.tableFieldUpdater = fu;
    return this.getTable();
  }

  /**
   * Returns table of users.
   *
   * @return
   */
  public CellTable<User> getTable() {

    // retrieve data
    retrieveData();

    // Table data provider.
    dataProvider = new ListDataProvider<User>(list);

    // Cell table
    table = new PerunTable<User>(list);

    // Connect the table to the data provider.
    dataProvider.addDataDisplay(table);

    // Sorting
    ListHandler<User> columnSortHandler = new ListHandler<User>(dataProvider.getList());
    table.addColumnSortHandler(columnSortHandler);

    // table selection
    table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<User>createCheckboxManager());

    // set empty content & loader
    table.setEmptyTableWidget(loaderImage);
    loaderImage.setEmptyResultMessage("Service identity has no owners (users) assigned.");

    // columns
    if (checkable) {
      table.addCheckBoxColumn();
    }
    table.addIdColumn("User ID", tableFieldUpdater);

    // NAME COLUMN
    Column<User, String> nameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<User, String>() {
      public String getValue(User user) {
        return user.getFullName();
      }
    }, tableFieldUpdater);

    nameColumn.setSortable(true);
    columnSortHandler.setComparator(nameColumn, new Comparator<User>() {
      public int compare(User o1, User o2) {
        return o1.getLastName().compareToIgnoreCase(o2.getLastName());
      }
    });

    table.addColumn(nameColumn, "Name");

    // SERVICE COLUMN
    Column<User, String> serviceColumn = JsonUtils.addColumn(new JsonUtils.GetValue<User, String>() {
      public String getValue(User user) {
        if (user.isServiceUser()) {
          return "Service";
        } else if (user.isSponsoredUser()) {
          return "Sponsored";
        } else {
          return "Person";
        }
      }
    }, tableFieldUpdater);

    serviceColumn.setSortable(true);
    columnSortHandler.setComparator(serviceColumn, new Comparator<User>() {
      public int compare(User o1, User o2) {

        String type1 = "Person";
        if (o1.isServiceUser()) {
          type1 = "Service";
        } else if (o1.isSponsoredUser()) {
          type1 = "Sponsored";
        }

        String type2 = "Person";
        if (o2.isServiceUser()) {
          type2 = "Service";
        } else if (o2.isSponsoredUser()) {
          type2 = "Sponsored";
        }

        return type1.compareTo(type2);
      }
    });

    table.addColumn(serviceColumn, "User type");

    return table;

  }

  /**
   * Retrieves data from RPC
   */
  public void retrieveData() {
    JsonClient js = new JsonClient();
    js.retrieveData(JSON_URL, "specificUser=" + userId, this);
  }

  /**
   * Sorts table by objects Name
   */
  public void sortTable() {
    list = new TableSorter<User>().sortByName(getList());
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Add object as new row to table
   *
   * @param object user to be added as new row
   */
  public void addToTable(User object) {
    list.add(object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Removes object as row from table
   *
   * @param object user to be removed as row
   */
  public void removeFromTable(User object) {
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
  public ArrayList<User> getTableSelectedList() {
    return JsonUtils.setToList(selectionModel.getSelectedSet());
  }

  /**
   * Called, when an error occurs
   */
  public void onError(PerunError error) {
    session.getUiElements().setLogErrorText("Error while loading service users.");
    loaderImage.loadingError(error);
    events.onError(error);
  }

  /**
   * Called, when loading starts
   */
  public void onLoadingStart() {
    session.getUiElements().setLogText("Loading service users started.");
    events.onLoadingStart();
  }

  /**
   * Called, when operation finishes successfully.
   */
  public void onFinished(JavaScriptObject jso) {
    setList(JsonUtils.<User>jsoAsList(jso));
    sortTable();
    session.getUiElements().setLogText("Users loaded: " + list.size());
    events.onFinished(jso);
    loaderImage.loadingFinished();
  }

  public void insertToTable(int index, User object) {
    list.add(index, object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  public void setEditable(boolean editable) {
    // TODO Auto-generated method stub
  }

  public void setCheckable(boolean checkable) {
    this.checkable = checkable;
  }

  public ArrayList<User> getList() {
    return this.list;
  }

  public void setList(ArrayList<User> list) {
    clearTable();
    this.list.addAll(list);
    dataProvider.flush();
    dataProvider.refresh();
  }

}
