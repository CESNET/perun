package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;

import java.util.ArrayList;

/**
 * Ajax query to get assigned groups for specified resource
 * or specified resource and member
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAssignedGroups implements JsonCallback, JsonCallbackTable<Group>, JsonCallbackOracle<Group> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// resource id
	private int resourceId;
	private int memberId = 0;
	// JSON URL
	static private final String JSON_URL = "resourcesManager/getAssignedGroups";
	// Selection model
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<Group>(new GeneralKeyProvider<Group>());
	final SingleSelectionModel<Group> singleSelectionModel = new SingleSelectionModel<Group>(new GeneralKeyProvider<Group>());
	// is table single selection like ?
	boolean singleSelection = false;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<Group> dataProvider = new ListDataProvider<Group>();
	// Table itself
	private PerunTable<Group> table;
	// Table list
	private ArrayList<Group> list = new ArrayList<Group>();
	// Table field updater
	private FieldUpdater<Group, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;
	private boolean coreGroupsCheckable = true;
	// oracle
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(":");
	private ArrayList<Group> fullBackup = new ArrayList<Group>();

	/**
	 * Creates a new callback instance
	 *
	 * @param id resource ID
	 */
	public GetAssignedGroups(int id) {
		this.resourceId = id;
	}

	/**
	 * Creates a new callback instance
	 *
	 * @param resourceId resource ID
	 * @param memberId member ID
	 */
	public GetAssignedGroups(int resourceId, int memberId) {
		this.resourceId = resourceId;
		this.memberId = memberId;
	}

	/**
	 * Creates a new callback instance
	 *
	 * @param id resource ID
	 * @param events Custom events
	 */
	public GetAssignedGroups(int id, JsonCallbackEvents events) {
		this.resourceId = id;
		this.events = events;
	}

	/**
	 * Creates a new callback instance
	 *
	 * @param id resource ID
	 * @param memberId member ID
	 * @param events Custom events
	 */
	public GetAssignedGroups(int id, int memberId, JsonCallbackEvents events) {
		this.resourceId = id;
		this.memberId = memberId;
		this.events = events;
	}

	/**
	 * Returns table with assigned groups with custom onClick
	 *
	 * @param fu Field updater
	 * @return table widget
	 */
	public CellTable<Group> getTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with assigned groups
	 *
	 * @return table widget
	 */
	public CellTable<Group> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Group>(list);

		// Cell table
		table = new PerunTable<Group>(list);
		table.setHyperlinksAllowed(false);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Group> columnSortHandler = new ListHandler<Group>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		if (singleSelection) {
			table.setSelectionModel(singleSelectionModel, DefaultSelectionEventManager.<Group> createCheckboxManager());
		} else {
			table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Group> createCheckboxManager());
		}

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Resource has no groups assigned.");

		// checkbox column column
		if (checkable){

			Column<Group, Group> checkBoxColumn = new Column<Group, Group>(
					new PerunCheckboxCell<Group>(true, false, coreGroupsCheckable)) {
				@Override
				public Group getValue(Group object) {
					// Get the value from the selection model.
					if (singleSelection) {
						object.setChecked(singleSelectionModel.isSelected(object));
					} else {
						object.setChecked(selectionModel.isSelected(object));
					}

					return object;
				}
			};

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
					for(Group obj : list){
						if (!obj.isCoreGroup()) {
							selectionModel.setSelected(obj, value);
						}
					}
				}
			});

			// updates the columns size
			table.setColumnWidth(checkBoxColumn, 60.0, Style.Unit.PX);

			if (singleSelection) {
				// single selection withou "check all"
				table.addColumn(checkBoxColumn);
			} else {
				// multi selection with header
				table.addColumn(checkBoxColumn,checkBoxHeader);
			}

		}


		table.addIdColumn("Group ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;
	}


	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		String param = "resource=" + this.resourceId;
		if (memberId != 0) {
			param = param+"&member=" + this.memberId;
		}
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Group>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Group to be added as new row
	 */
	public void addToTable(Group object) {
		list.add(object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Group to be removed as row
	 */
	public void removeFromTable(Group object) {
		list.remove(object);
		if (singleSelection) {
			singleSelectionModel.setSelected(object, false);
		} else {
			selectionModel.getSelectedSet().remove(object);
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Clear all table content
	 */
	public void clearTable(){
		loaderImage.loadingStart();
		list.clear();
		oracle.clear();
		fullBackup.clear();
		selectionModel.clear();
		singleSelectionModel.clear();
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Clears list of selected items
	 */
	public void clearTableSelectedSet(){
		selectionModel.clear();
		singleSelectionModel.clear();
	}

	/**
	 * Return selected items from list
	 *
	 * @return return list of checked items
	 */
	public ArrayList<Group> getTableSelectedList(){
		if (singleSelection) {
			return JsonUtils.setToList(singleSelectionModel.getSelectedSet());
		} else {
			return JsonUtils.setToList(selectionModel.getSelectedSet());
		}
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading assigned groups.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading assigned groups started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Group>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Groups loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Group object) {
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

	public void setList(ArrayList<Group> list) {
		clearTable();
		this.list.addAll(list);
		for (Group object : list) {
			oracle.add(object.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Group> getList() {
		return this.list;
	}

	/**
	 * Set single or multiple selection model for table
	 *
	 * @param single TRUE = single selection / FALSE = multiple selection
	 */
	public void setSingleSelection(boolean single){
		singleSelection = single;
	}

	/**
	 * Enable or disable checkboxes for core groups
	 * Default = enabled
	 *
	 * @param checkable true if core groups (members, administrators), should be checkable or not
	 */
	public void setCoreGroupsCheckable(boolean checkable) {
		this.coreGroupsCheckable = checkable;
	}

	public UnaccentMultiWordSuggestOracle getOracle(){
		return this.oracle;
	}

	public void filterTable(String text){

		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			fullBackup.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (text.equalsIgnoreCase("")) {
			list.addAll(fullBackup);
		} else {
			for (Group grp : fullBackup){
				if (grp.getName().toLowerCase().startsWith(text.toLowerCase())) {
					list.add(grp);
				}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No group matching '"+text+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("Resource has no groups assigned.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

}
