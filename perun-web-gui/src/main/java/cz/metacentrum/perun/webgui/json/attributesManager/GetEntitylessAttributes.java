package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
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
import cz.metacentrum.perun.webgui.json.keyproviders.EntitylessAttributeKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAttributeValueCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;
import java.util.ArrayList;
import java.util.Map;

/**
 * Ajax query to get entityless attributes with values
 *
 * @author Daniel Fecko <dano9500@gmail.com>
 */
public class GetEntitylessAttributes implements JsonCallback, JsonCallbackTable<Attribute> {

  final MultiSelectionModel<Attribute> selectionModel = new MultiSelectionModel<>(new EntitylessAttributeKeyProvider());
  // Perun session
  private PerunWebSession session = PerunWebSession.getInstance();
  // Json callback events
  private JsonCallbackEvents events = new JsonCallbackEvents();
  // Table data provider
  private ListDataProvider<Attribute> dataProvider = new ListDataProvider<>();
  // Table
  private PerunTable<Attribute> table;
  // List of attributes
  private ArrayList<Attribute> list = new ArrayList<>();

  private AttributeDefinition attributeDefinition;

  // FIELD UPDATER - when user clicks on a row
  private FieldUpdater<Attribute, String> tableFieldUpdater;
  // loader image
  private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

  private boolean editable = true;
  private boolean checkable = true;
  private boolean ownClear = false;

  public GetEntitylessAttributes(AttributeDefinition attributeDefinition) {
    this.attributeDefinition = attributeDefinition;
  }

  public GetEntitylessAttributes(JsonCallbackEvents events) {
    this.events = events;
  }

  @Override
  public void clearTable() {
    loaderImage.loadingStart();
    list.clear();
    selectionModel.clear();
    dataProvider.flush();
    dataProvider.refresh();
  }

  @Override
  public void insertToTable(int index, Attribute object) {
    list.add(index, object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  @Override
  public void addToTable(Attribute object) {
    list.add(object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  @Override
  public void removeFromTable(Attribute object) {
    list.remove(object);
    selectionModel.getSelectedSet().remove(object);
    dataProvider.flush();
    dataProvider.refresh();
  }

  @Override
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  @Override
  public void setCheckable(boolean checkable) {
    this.checkable = checkable;
  }

  @Override
  public void clearTableSelectedSet() {
    selectionModel.clear();
  }

  @Override
  public ArrayList<Attribute> getTableSelectedList() {
    return JsonUtils.setToList(selectionModel.getSelectedSet());
  }

  @Override
  public ArrayList<Attribute> getList() {
    return this.list;
  }

  @Override
  public void setList(ArrayList<Attribute> list) {
    if (!ownClear) {
      clearTable();
    }
    this.list.addAll(list);
    dataProvider.flush();
    dataProvider.refresh();
  }

  @Override
  public CellTable<Attribute> getTable() {
    CellTable<Attribute> table = getEmptyTable();
    // retrieve data
    retrieveData();
    return table;
  }

  public CellTable<Attribute> getEmptyTable() {

    // Table data provider.
    dataProvider = new ListDataProvider<>(list);

    // Cell table
    table = new PerunTable<>(list);
    table.removeRowCountChangeHandler(); // remove row count change handler

    // Connect the table to the data provider.
    dataProvider.addDataDisplay(table);

    // Sorting
    ColumnSortEvent.ListHandler<Attribute> columnSortHandler =
        new ColumnSortEvent.ListHandler<>(dataProvider.getList());
    table.addColumnSortHandler(columnSortHandler);

    // set empty content & loader
    table.setEmptyTableWidget(loaderImage);

    loaderImage.setEmptyResultMessage("No attributes found. Use 'Add' button to add new key=value.");

    // because of tab index
    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);

    // checkbox column
    if (checkable) {

      // checkbox column column
      Column<Attribute, Attribute> checkBoxColumn = new Column<Attribute, Attribute>(
          new PerunCheckboxCell<>(true, false, false)) {
        @Override
        public Attribute getValue(Attribute object) {
          // Get the value from the selection model.
          GeneralObject go = object.cast();
          go.setChecked(selectionModel.isSelected(object));
          return go.cast();
        }
      };

      // updates the columns size
      table.setColumnWidth(checkBoxColumn, 40.0, Style.Unit.PX);

      // Add the columns

      // Checkbox column header
      CheckboxCell cb = new CheckboxCell();
      Header<Boolean> checkBoxHeader = new Header<Boolean>(cb) {
        public Boolean getValue() {
          return false;//return true to see a checked checkbox.
        }
      };
      checkBoxHeader.setUpdater(value -> {
        // sets selected to all, if value = true, unselect otherwise
        for (Attribute obj : list) {
          if (obj.isWritable()) {
            selectionModel.setSelected(obj, value);
          }
        }
      });

      // table selection
      table.setSelectionModel(selectionModel, DefaultSelectionEventManager.createCheckboxManager(0));

      table.addColumn(checkBoxColumn, checkBoxHeader);

    }

    //Key column
    TextColumn<Attribute> keyColumn = new TextColumn<Attribute>() {
      @Override
      public String getValue(Attribute attribute) {
        return attribute.getKey();
      }
    };
    keyColumn.setSortable(true);
    table.addColumn(keyColumn, "Key");
    this.table.setColumnWidth(keyColumn, 200.0, Style.Unit.PX);

    // Value column
    Column<Attribute, Attribute> valueColumn = JsonUtils.addColumn(new PerunAttributeValueCell());
    valueColumn.setFieldUpdater((index, object, value) -> {
      object = value;
      selectionModel.setSelected(object, object.isAttributeValid());
    });

    // Add sorting
    this.table.addColumnSortHandler(columnSortHandler);

    // Add the columns.
    this.table.addColumn(valueColumn, "Value");

    return this.table;
  }

  @Override
  public void onFinished(JavaScriptObject jso) {
    if (!ownClear) {
      clearTable();
    }
    ArrayList<Attribute> attrList = new ArrayList<>();
    Map<String, JSONValue> bot = JsonUtils.parseJsonToMap(jso);
    for (String key : bot.keySet()) {
      Attribute a = bot.get(key).isObject().getJavaScriptObject().cast();
      a.setKey(key);
      attrList.add(a);
    }
    setList(attrList);
    sortTable();
    loaderImage.loadingFinished();
    events.onFinished(jso);
  }

  @Override
  public void onError(PerunError error) {
    session.getUiElements().setLogErrorText("Error while loading attributes.");
    loaderImage.loadingError(error);
    events.onError(error);
  }

  @Override
  public void onLoadingStart() {
    loaderImage.loadingStart();
    session.getUiElements().setLogText("Loading attributes started.");
    events.onLoadingStart();
  }

  @Override
  public void retrieveData() {
    String params = "attrName=" + attributeDefinition.getName();
    String JSON_URL_ATRIBUTES = "attributesManager/getEntitylessAttributesWithKeys";
    JsonClient js = new JsonClient();
    js.retrieveData(JSON_URL_ATRIBUTES, params, this);
  }

  public void sortTable() {
    list = new TableSorter<Attribute>().sortById(getList());
    dataProvider.flush();
    dataProvider.refresh();
  }

}
