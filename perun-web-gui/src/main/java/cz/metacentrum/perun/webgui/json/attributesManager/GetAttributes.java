package cz.metacentrum.perun.webgui.json.attributesManager;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Ajax query to get attributes with values for all sort of entities in Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetAttributes implements JsonCallback, JsonCallbackTable<Attribute> {

	// Perun session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	private static final String JSON_URL = "attributesManager/getAttributes";
	// IDs
	private Map<String, Integer> ids = new HashMap<String, Integer>();
	// Selection model
	final MultiSelectionModel<Attribute> selectionModel = new MultiSelectionModel<Attribute>(new GeneralKeyProvider<Attribute>());
	// Table data provider
	private ListDataProvider<Attribute> dataProvider = new ListDataProvider<Attribute>();
	// Table
	private PerunTable<Attribute> table;
	// List of attributes
	private ArrayList<Attribute> list = new ArrayList<Attribute>();
	// FIELD UPDATER - when user clicks on a row
	private FieldUpdater<Attribute, String> tableFieldUpdater;
	// Json callback events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	// friendly table
	private FlexTable friendlyTable = new FlexTable();

	private boolean editable = true;
	private boolean checkable = true;

	/**
	 * Creates new instance of callback
	 */
	public GetAttributes() {}


	/**
	 * Creates new instance of callback
	 *
	 * @param events external events
	 */
	public GetAttributes(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns attributes of a facility
	 *
	 * @param facilityId facility ID
	 */
	public void getFacilityAttributes(int facilityId) {
		ids.clear();
		this.ids.put("facility", facilityId);
	}

	/**
	 * Returns attributes of a user on facility
	 *
	 * @param facilityId facility ID
	 * @param userId user ID
	 */
	public void getUserFacilityAttributes(int facilityId, int userId) {
		ids.clear();
		this.ids.put("facility", facilityId);
		this.ids.put("user", userId);
	}

	/**
	 * Returns attributes of a VO
	 *
	 * @param voId VO ID
	 */
	public void getVoAttributes(int voId) {
		ids.clear();
		this.ids.put("vo", voId);
	}

	/**
	 * Returns member-resource attributes
	 *
	 * @param memberId Member ID
	 * @param resourceId Resource ID
	 */
	public void getMemberResourceAttributes(int memberId, int resourceId) {
		ids.clear();
		this.ids.put("resource", resourceId);
		this.ids.put("member", memberId);
	}

	/**
	 * Returns attributes of a member
	 *
	 * @param memberId Member ID
	 */
	public void getMemberAttributes(int memberId) {
		ids.clear();
		this.ids.put("member", memberId);
	}

	/**
	 * Returns attributes of a member_group
	 *
	 * @param memberId Member ID
	 * @param groupId Group ID
	 */
	public void getMemberGroupAttributes(int memberId, int groupId) {
		ids.clear();
		this.ids.put("member", memberId);
		this.ids.put("group", groupId);
	}

	/**
	 * Returns attributes of a member
	 *
	 * @param memberId Member ID
	 */
	public void getMemberAttributes(int memberId, int workWithUser) {
		ids.clear();
		this.ids.put("member", memberId);
		this.ids.put("workWithUserAttributes", workWithUser);
	}

	/**
	 * Returns attributes of a resource
	 *
	 * @param resourceId Resource ID
	 */
	public void getResourceAttributes(int resourceId) {
		ids.clear();
		this.ids.put("resource", resourceId);
	}

	/**
	 * Returns attributes of a user
	 *
	 * @param userId user ID
	 */
	public void getUserAttributes(int userId) {
		ids.clear();
		this.ids.put("user", userId);
	}

	/**
	 * Returns attributes of a UserExtSource
	 *
	 * @param uesId UserExtSource ID
	 */
	public void getUserExtSourceAttributes(int uesId) {
		ids.clear();
		this.ids.put("userExtSource", uesId);
	}

	/**
	 * Returns attributes of a group
	 *
	 * @param groupId Group ID
	 */
	public void getGroupAttributes(int groupId) {
		ids.clear();
		this.ids.put("group", groupId);
	}


	/**
	 * Returns member-resource attributes
	 *
	 * @param groupId Group ID
	 * @param resourceId Resource ID
	 */
	public void getGroupResourceAttributes(int groupId, int resourceId) {
		ids.clear();
		this.ids.put("resource", resourceId);
		this.ids.put("group", groupId);
	}

	/**
	 * Returns attributes of a host
	 *
	 * @param hostId Host ID
	 */
	public void getHostAttributes(int hostId) {
		ids.clear();
		this.ids.put("host", hostId);
	}

	/**
	 * Retrieves data from the RPC
	 */
	public void retrieveData() {
		String params = "";
		// serialize parameters
		for (Map.Entry<String, Integer> attr : this.ids.entrySet()) {
			params += attr.getKey() + "=" + attr.getValue() + "&";
		}

		JsonClient js = new JsonClient();
		js.retrieveData(GetAttributes.JSON_URL, params, this);
	}

	/**
	 * Returns the table widget with attributes and custom field updater
	 *
	 * @param fu Field updater
	 * @return table widget
	 */
	public CellTable<Attribute> getTable(FieldUpdater<Attribute, String> fu) {
		this.tableFieldUpdater = fu;
		return getTable();
	}

	/**
	 * Returns table widget with attributes
	 *
	 * @return table widget
	 */
	public CellTable<Attribute> getTable(){

		CellTable<Attribute> table = getEmptyTable();
		// retrieve data
		retrieveData();
		return table;

	}

	/**
	 * Returns table widget with attributes
	 * @return table widget
	 */
	public Widget getDecoratedFlexTable(){
		DecoratorPanel dp = new DecoratorPanel();
		dp.setWidget(this.friendlyTable);
		return dp;
	}

	/**
	 * Returns empty table widget with attributes
	 *
	 * @return table widget
	 */
	public CellTable<Attribute> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Attribute>(list);

		// Cell table
		table = new PerunTable<Attribute>(list);
		table.removeRowCountChangeHandler(); // remove row count change handler

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Attribute> columnSortHandler = new ListHandler<Attribute>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Attribute> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// because of tabindex
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

		if(checkable) {

			// checkbox column column
			Column<Attribute, Attribute> checkBoxColumn = new Column<Attribute, Attribute>(
					new PerunCheckboxCell<Attribute>(true, false, false)) {
				@Override
				public Attribute getValue(Attribute object) {
					// Get the value from the selection model.
					GeneralObject go = object.cast();
					go.setChecked(selectionModel.isSelected(object));
					return go.cast();
				}
			};

			// updates the columns size
			table.setColumnWidth(checkBoxColumn, 40.0, Unit.PX);

			// Add the columns

			// Checkbox column header
			CheckboxCell cb = new CheckboxCell();
			Header<Boolean> checkBoxHeader = new Header<Boolean>(cb) {
				public Boolean getValue() {
					return false;//return true to see a checked checkbox.
				}
			};
			checkBoxHeader.setUpdater(new ValueUpdater<Boolean>() {
				public void update(Boolean value) {
					// sets selected to all, if value = true, unselect otherwise
					for(Attribute obj : list){
						if (obj.isWritable()) {
							selectionModel.setSelected(obj, value);
						}
					}
				}
			});

			table.addColumn(checkBoxColumn, checkBoxHeader);

		}

		// Create ID column.
		table.addIdColumn("Attribute ID", this.tableFieldUpdater);

		// Name column
		Column<Attribute, String> nameColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Attribute, String>() {
					public String getValue(Attribute attribute) {
						return attribute.getFriendlyName();
					}
				}, this.tableFieldUpdater);

		// Create ENTITY column
		Column<Attribute, String> entityColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Attribute, String>() {
					public String getValue(Attribute a) {
						return a.getEntity();
					}
				}, this.tableFieldUpdater);

		// Create def type column
		Column<Attribute, String> defTypeColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Attribute, String>() {
					public String getValue(Attribute a) {
						return a.getDefinition();
					}
				}, this.tableFieldUpdater);


		// Create type column.
		Column<Attribute, String> typeColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Attribute, String>() {
					public String getValue(Attribute attribute) {
						return renameContent(attribute.getType());
					}
				}, this.tableFieldUpdater);

		Column<Attribute, String> valueColumn;
		if(editable)
		{
			valueColumn = JsonUtils.addColumn(
					new JsonUtils.GetValue<Attribute, String>() {
						public String getValue(Attribute attribute) {
							return attribute.getValue();
						}
					}, new FieldUpdater<Attribute, String>() {
						public void update(int index, Attribute object, String newText) {
							if (object.setValue(newText)) {
								selectionModel.setSelected(object, true);
							} else {
								selectionModel.setSelected(object, false);
								UiElements.cantSaveAttributeValueDialogBox(object);
							}
						}
					});
		}else{
			valueColumn = JsonUtils.addColumn(
					new JsonUtils.GetValue<Attribute, String>() {
						public String getValue(Attribute attribute) {
							return attribute.getValue();
						}
					}, this.tableFieldUpdater);
		}

		// Create description column.
		Column<Attribute, String> descriptionColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Attribute, String>() {
					public String getValue(Attribute attribute) {
						return attribute.getDescription();
					}
				}, this.tableFieldUpdater);


		// Sorting name column
		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn,
				new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getFriendlyName().compareToIgnoreCase(
							o2.getFriendlyName());
					}
				});


		// Sorting type column
		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn,
				new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getType().compareToIgnoreCase(o2.getType());
					}
				});

		// Sorting description column
		descriptionColumn.setSortable(true);
		columnSortHandler.setComparator(descriptionColumn,
				new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getDescription().compareToIgnoreCase(
							o2.getDescription());
					}
				});

		// Sorting value column
		valueColumn.setSortable(true);
		columnSortHandler.setComparator(valueColumn,
				new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getValue().compareToIgnoreCase(o2.getValue());
					}
				});

		// Sorting value column
		entityColumn.setSortable(true);
		columnSortHandler.setComparator(entityColumn,
				new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getEntity().compareToIgnoreCase(o2.getEntity());
					}
				});

		// Sorting value column
		defTypeColumn.setSortable(true);
		columnSortHandler.setComparator(defTypeColumn,
				new Comparator<Attribute>() {
					public int compare(Attribute o1, Attribute o2) {
						return o1.getDefinition().compareToIgnoreCase(o2.getDefinition());
					}
				});

		// column size
		this.table.setColumnWidth(typeColumn, 120.0, Unit.PX);
		this.table.setColumnWidth(entityColumn, 100.0, Unit.PX);
		this.table.setColumnWidth(defTypeColumn, 110.0, Unit.PX);
		this.table.setColumnWidth(nameColumn, 200.0, Unit.PX);
		this.table.setColumnWidth(valueColumn, 200.0, Unit.PX);

		// Add the columns.
		this.table.addColumn(nameColumn, "Name");
		this.table.addColumn(entityColumn,"Entity");
		this.table.addColumn(defTypeColumn,"Definition");
		this.table.addColumn(typeColumn, "Value type");
		this.table.addColumn(valueColumn, "Value");
		this.table.addColumn(descriptionColumn, "Description");

		return table;

	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Attribute>().sortByFriendlyName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Attribute to be added as new row
	 */
	public void addToTable(Attribute object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Attribute to be removed as row
	 */
	public void removeFromTable(Attribute object) {
		list.remove(object);
		selectionModel.getSelectedSet().remove(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Clear all table content
	 */
	public void clearTable(){
		loaderImage.loadingStart();
		list.clear();
		selectionModel.clear();
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Clears list of selected items
	 */
	public void clearTableSelectedSet(){
		selectionModel.clear();
	}

	/**
	 * Return selected items from list
	 *
	 * @return return list of checked items
	 */
	public ArrayList<Attribute> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading attributes.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading attributes started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		clearTable();
		friendlyTable.clear();
		int counter = 0;
		for (Attribute a : JsonUtils.<Attribute>jsoAsList(jso)) {
			if (!a.getDefinition().equals("core")) {
				addToTable(a);
			}
			friendlyTable.setWidget(counter, 0, new HTML("<strong>" + a.getFriendlyName() + "</strong>"));
			friendlyTable.setText(counter, 1, a.getValue());
			counter++;
		}
		sortTable();
		session.getUiElements().setLogText("Attributes loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Attribute object) {
		list.add(index, object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<Attribute> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Attribute> getList() {
		return this.list;
	}

	/**
	 * Return substring of input text - start on index 10
	 *
	 * @param oldString old string
	 * @return new string
	 */
	private String renameContent(String oldString) {
		String newString = "";
		newString = oldString.substring(10);

		return newString;
	}

	/**
	 * Sets IDS for this callback
	 *
	 * @param ids to be set
	 */
	public void setIds(Map<String, Integer> ids) {
		this.ids = ids;
	}

	/**
	 * Set external events after callback is created.
	 *
	 * @param extEvents external events
	 */
	public void setEvents(JsonCallbackEvents extEvents) {
		events = extEvents;
	}

	/**
	 * Return current IDS associated with callback
	 *
	 * @return IDS
	 */
	public Map<String, Integer> getIds() {
		return this.ids;
	}

}
