package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get VO/GROUP/FACILITY admins
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAdminGroups implements JsonCallback, JsonCallbackTable<Group> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	private static final String GROUP_JSON_URL = "groupsManager/getAdminGroups";
	private static final String VO_JSON_URL = "vosManager/getAdminGroups";
	private static final String FACILITY_JSON_URL = "facilitiesManager/getAdminGroups";

	// entity ID
	private int entityId;
	// Selection model for the table
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<Group>(new GeneralKeyProvider<Group>());
	// Table data provider.
	private ListDataProvider<Group> dataProvider = new ListDataProvider<Group>();
	// Cell table
	private PerunTable<Group> table;
	// List of members
	private ArrayList<Group> list = new ArrayList<Group>();
	// Table field updater
	private FieldUpdater<Group, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private PerunEntity entity;

	/**
	 * Creates a new instance of callback
	 *
	 * @param entity entity
	 * @param id entity ID
	 */
	public GetAdminGroups(PerunEntity entity, int id) {
		this.entity = entity;
		this.entityId = id;
	}

	/**
	 * Creates a new instance of callback
	 *
	 * @param entity entity
	 * @param id entity ID
	 * @param events
	 */
	public GetAdminGroups(PerunEntity entity, int id, JsonCallbackEvents events) {
		this.entity = entity;
		this.entityId = id;
		this.events = events;
	}

	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData() {

		JsonClient js = new JsonClient();
		String param;

		if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
			param = "vo="+entityId;
			js.retrieveData(VO_JSON_URL, param, this);
		} else if (entity.equals(PerunEntity.GROUP)) {
			param = "group="+entityId;
			js.retrieveData(GROUP_JSON_URL, param, this);
		} else if (entity.equals(PerunEntity.FACILITY)) {
			param = "facility="+entityId;
			js.retrieveData(FACILITY_JSON_URL, param, this);
		}

	}

	/**
	 * Returns the table with member-Groups
	 *
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<Group> getTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with member-Groups
	 *
	 * @return CellTable widget
	 */
	public CellTable<Group> getTable() {

		// Retrieves data
		this.retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Group>(list);

		// Cell table
		table = new PerunTable<Group>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Group> columnSortHandler = new ListHandler<Group>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// Table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Group> createCheckboxManager());

		// Set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
			loaderImage.setEmptyResultMessage("VO has no groups of managers (try to switch to 'Users' view).");
		} else if (entity.equals(PerunEntity.GROUP)) {
			loaderImage.setEmptyResultMessage("Group has no groups of managers (try to switch to 'Users' view).");
		} else if (entity.equals(PerunEntity.FACILITY)) {
			loaderImage.setEmptyResultMessage("Facility has no groups of managers (try to switch to 'Users' view).");
		}

		// Checkbox column column
		table.addCheckBoxColumn();

		// Create Group ID column.
		Column<Group, String> groupIdColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Group, String>() {
					public String getValue(Group object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);


		groupIdColumn.setSortable(true);
		columnSortHandler.setComparator(groupIdColumn, new GeneralComparator<Group>(GeneralComparator.Column.ID));

		table.setColumnWidth(groupIdColumn, 110.0, Unit.PX);

		if(JsonUtils.isExtendedInfoVisible()){
			table.addColumn(groupIdColumn,  "Group ID");
		}

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
		session.getUiElements().setLogErrorText("Error while loading admin groups.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading admin groups started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Group>jsoAsList(jso));
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Admin groups loaded: " + list.size());
		events.onFinished(jso);
	}

	public void insertToTable(int index, Group object) {
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

	public void setList(ArrayList<Group> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Group> getList() {
		return this.list;
	}

	/**
	 * Sets external events after callback creation
	 *
	 * @param externalEvents external events
	 */
	public void setEvents(JsonCallbackEvents externalEvents) {
		events = externalEvents;
	}

}
