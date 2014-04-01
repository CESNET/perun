package cz.metacentrum.perun.webgui.json.groupsManager;

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
 * GroupsManager/getMemberGroups Method
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetMemberGroups implements JsonCallback, JsonCallbackTable<Group>, JsonCallbackOracle<Group> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Member  id
	private int memberId;
	// Selection model
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<Group>(new GeneralKeyProvider<Group>());
	// JSON URL
	static final private String JSON_URL = "groupsManager/getMemberGroups";
	// Table data provider
	private ListDataProvider<Group> dataProvider = new ListDataProvider<Group>();
	// The table itself
	private PerunTable<Group> table;
	// List in the table
	private ArrayList<Group> list = new ArrayList<Group>();
	// FIELD UPDATER - when user clicks on a row
	private FieldUpdater<Group, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;
	private boolean coreGroupsCheckable = false;
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(":");
	private ArrayList<Group> fullBackup = new ArrayList<Group>();

	/**
	 * Creates a new instance of GroupsManager/getMemberGroups method
	 *
	 * @param id Member id
	 */
	public GetMemberGroups(int id) {
		this.memberId = id;
	}

	/**
	 * Creates a new instance of GroupsManager/getMemberGroups method with custom field updater
	 *
	 * @param id Member id
	 * @param events Custom events
	 */
	public GetMemberGroups(int id, JsonCallbackEvents events) {
		this.memberId = id;
		this.events = events;
	}

	/**
	 * Retrieves data via RPC
	 */
	public void retrieveData()
	{
		final String param = "member=" + this.memberId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Returns the table with subgroups and custom field updater
	 * @return
	 */
	public CellTable<Group> getTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with subgroups.
	 * @return
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
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Group> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("User is not member of any group.");

		// checkbox column column
		Column<Group, Group> checkBoxColumn = new Column<Group, Group>(
				new PerunCheckboxCell<Group>(true, false, coreGroupsCheckable)) {
			@Override
			public Group getValue(Group object) {
				// Get the value from the selection model.
				object.setChecked(selectionModel.isSelected(object));
				return object;
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

		if (checkable) {
			table.addColumn(checkBoxColumn, checkBoxHeader);
		}

		table.addIdColumn("Group ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;

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
		fullBackup.clear();
		oracle.clear();
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
	public ArrayList<Group> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading members groups.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading members groups started.");
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
		for (Group g : list) {
			oracle.add(g.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Group> getList() {
		return this.list;
	}

	public void setCoreGroupsCheckable(boolean checkable) {
		this.coreGroupsCheckable = checkable;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
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
				// store facility by filter
				if (grp.getName().toLowerCase().startsWith(text.toLowerCase()) ||
						grp.getName().toLowerCase().contains(":"+text.toLowerCase())) {
					list.add(grp);
						}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No group matching '"+text+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("User is not member of any group.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
