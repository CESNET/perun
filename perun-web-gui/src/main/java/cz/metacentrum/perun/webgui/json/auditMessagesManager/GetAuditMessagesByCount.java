package cz.metacentrum.perun.webgui.json.auditMessagesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
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
import cz.metacentrum.perun.webgui.model.AuditMessage;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.CustomTextCell;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to logIn to RPC and get PerunPrincipal
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAuditMessagesByCount implements JsonCallback, JsonCallbackTable<AuditMessage> {

  // JSON URL
  static private final String JSON_URL = "auditMessagesManager/getMessagesByCount";
  // Selection model
  final MultiSelectionModel<AuditMessage> selectionModel =
      new MultiSelectionModel<AuditMessage>(new GeneralKeyProvider<AuditMessage>());
  // Session
  private PerunWebSession session = PerunWebSession.getInstance();
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  /// default: 0 = all messages
  private int count = 0;
  private ListDataProvider<AuditMessage> dataProvider = new ListDataProvider<AuditMessage>();
  // The table itself
  private PerunTable<AuditMessage> table;
  // Facilities list
  private ArrayList<AuditMessage> list = new ArrayList<AuditMessage>();
  private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
  private boolean checkable = false;

  /**
   * Creates a new callback
   */
  public GetAuditMessagesByCount() {
  }

  /**
   * Creates a new callback
   *
   * @param events external events
   */
  public GetAuditMessagesByCount(JsonCallbackEvents events) {
    this.events = events;
  }

  /**
   * Get currently set number for messages
   *
   * @return number of messages to be retrieved
   */
  public int getCount() {
    return this.count;
  }

  /**
   * Sets count of messages to be retrieved from RPC
   *
   * @param count / 0 = all
   */
  public void setCount(int count) {
    this.count = count;
  }

  /**
   * Retrieves data
   */
  public void retrieveData() {
    // retrieve data
    JsonClient js = new JsonClient();
    if (count > 0) {
      js.retrieveData(JSON_URL, "count=" + count, this);
    } else {
      js.retrieveData(JSON_URL, this);
    }
  }

  private String getAuditEventName(AuditMessage auditMessage) {
    return auditMessage.getEventName().substring(1 + auditMessage.getEventName().lastIndexOf("."));
  }

  public CellTable<AuditMessage> getTable() {

    retrieveData();

    // Table data provider.
    dataProvider = new ListDataProvider<AuditMessage>(list);

    // Cell table
    table = new PerunTable<AuditMessage>(list);

    // Connect the table to the data provider.
    dataProvider.addDataDisplay(table);

    // Sorting
    ListHandler<AuditMessage> columnSortHandler = new ListHandler<AuditMessage>(dataProvider.getList());
    table.addColumnSortHandler(columnSortHandler);

    // table selection
    table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<AuditMessage>createCheckboxManager());

    // set empty content & loader
    table.setEmptyTableWidget(loaderImage);
    loaderImage.setEmptyResultMessage("No audit messages found.");

    if (checkable) {
      // checkbox column column
      table.addCheckBoxColumn();
    }

    table.addIdColumn("Message ID", null, 120);

    // Event name column
    Column<AuditMessage, String> eventNameColumn = JsonUtils.addColumn(new CustomTextCell(),
        this::getAuditEventName, null);
    eventNameColumn.setSortable(true);
    columnSortHandler.setComparator(eventNameColumn,
        (o1, o2) -> (getAuditEventName(o1)).compareToIgnoreCase(getAuditEventName(o2)));

    // Event objects column
    Column<AuditMessage, String> objectsColumn = JsonUtils.addColumn(new CustomTextCell(),
        AuditMessage::getAuditEventObjectsMessage, null);
    eventNameColumn.setSortable(true);
    columnSortHandler.setComparator(eventNameColumn,
        (o1, o2) -> (o1.getAuditEventObjectsMessage()).compareToIgnoreCase(o2.getAuditEventObjectsMessage()));

    // Actor column
    Column<AuditMessage, String> actorColumn = JsonUtils.addColumn(new CustomTextCell(),
        AuditMessage::getActor, null);
    eventNameColumn.setSortable(true);
    columnSortHandler.setComparator(eventNameColumn,
        (o1, o2) -> (o1.getActor()).compareToIgnoreCase(o2.getActor()));

    // TIME COLUMN
    TextColumn<AuditMessage> timeColumn = new TextColumn<AuditMessage>() {
      public String getValue(AuditMessage msg) {
        if (msg.getCreatedAt().contains(".")) {
          return msg.getCreatedAt().substring(0, msg.getCreatedAt().indexOf("."));
        } else {
          // oracle timestamp doesn't have ending ".12345" in "2020-01-01 12:00:00.12345"
          return msg.getCreatedAt();
        }
      }
    };

    timeColumn.setSortable(true);
    columnSortHandler.setComparator(timeColumn, new Comparator<AuditMessage>() {
      public int compare(AuditMessage o1, AuditMessage o2) {
        return o1.getCreatedAt().compareToIgnoreCase(o2.getCreatedAt());
      }
    });

    table.addColumn(timeColumn, "Created at");
    table.setColumnWidth(timeColumn, "180px");
    table.addColumn(eventNameColumn, "Event name");
    table.setColumnWidth(eventNameColumn, "250px");
    table.addColumn(actorColumn, "Actor");
    table.setColumnWidth(actorColumn, "150px");
    table.addColumn(objectsColumn, "Related objects");

    return table;

  }

  /**
   * Sorts table by objects Name
   */
  public void sortTable() {
    list = new TableSorter<AuditMessage>().sortById(getList());
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Add object as new row to table
   *
   * @param object AuditMessage to be added as new row
   */
  public void addToTable(AuditMessage object) {
    list.add(object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  /**
   * Removes object as row from table
   *
   * @param object AuditMessage to be removed as row
   */
  public void removeFromTable(AuditMessage object) {
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
  public ArrayList<AuditMessage> getTableSelectedList() {
    return JsonUtils.setToList(selectionModel.getSelectedSet());
  }

  /**
   * Called, when an error occurs
   */
  public void onError(PerunError error) {
    session.getUiElements().setLogErrorText("Error while loading audit messages.");
    loaderImage.loadingError(error);
    events.onError(error);
  }

  /**
   * Called, when loading starts
   */
  public void onLoadingStart() {
    session.getUiElements().setLogText("Loading audit messages started.");
    events.onLoadingStart();
  }

  /**
   * Called, when operation finishes successfully.
   */
  public void onFinished(JavaScriptObject jso) {
    setList(JsonUtils.<AuditMessage>jsoAsList(jso));
    //sortTable(); comes pre-sorted
    loaderImage.loadingFinished();
    session.getUiElements().setLogText("Audit messages loaded: " + list.size());
    events.onFinished(jso);
  }

  public void insertToTable(int index, AuditMessage object) {
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

  public ArrayList<AuditMessage> getList() {
    return this.list;
  }

  public void setList(ArrayList<AuditMessage> list) {
    clearTable();
    this.list.addAll(list);
    dataProvider.flush();
    dataProvider.refresh();
  }

}
