package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get VO/GROUP/FACILITY admins
 *
 * TODO: support for FACILITY ADMINS is not yet done on perun side
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAdmins implements JsonCallback, JsonCallbackTable<User> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	private static final String GROUP_JSON_URL = "groupsManager/getAdmins";
	private static final String VO_JSON_URL = "vosManager/getAdmins";
	private static final String FACILITY_JSON_URL = "facilitiesManager/getAdmins";
	private static final String SECURITY_JSON_URL = "securityTeamsManager/getAdmins";

	// entity ID
	private int entityId;
	// Selection model for the table
	final MultiSelectionModel<User> selectionModel = new MultiSelectionModel<User>(new GeneralKeyProvider<User>());
	// Table data provider.
	private ListDataProvider<User> dataProvider = new ListDataProvider<User>();
	// Cell table
	private PerunTable<User> table;
	// List of members
	private ArrayList<User> list = new ArrayList<User>();
	// Table field updater
	private FieldUpdater<User, String> tableFieldUpdater;
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
	public GetAdmins(PerunEntity entity, int id) {
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
	public GetAdmins(PerunEntity entity, int id, JsonCallbackEvents events) {
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
			Window.alert("Get admins for facility by new callback is not yet supported on perun side.");
			// param = "facility="+entityId;
			// js.retrieveData(FACILITY_JSON_URL, param, this);
		} else if (entity.equals(PerunEntity.SECURITY_TEAM)) {
			param = "securityTeam="+entityId;
			js.retrieveData(SECURITY_JSON_URL, param, this);
		}

	}

	/**
	 * Returns the table with member-users
	 *
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<User> getTable(FieldUpdater<User, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with member-users
	 *
	 * @return CellTable widget
	 */
	public CellTable<User> getTable() {

		// Retrieves data
		this.retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<User>(list);

		// Cell table
		table = new PerunTable<User>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<User> columnSortHandler = new ListHandler<User>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// Table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<User> createCheckboxManager());

		// Set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// Checkbox column column
		table.addCheckBoxColumn();

		// Create User ID column.
		Column<User, String> userIdColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<User, String>() {
					public String getValue(User object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);


		userIdColumn.setSortable(true);
		columnSortHandler.setComparator(userIdColumn, new GeneralComparator<User>(GeneralComparator.Column.ID));

		table.setColumnWidth(userIdColumn, 110.0, Unit.PX);

		if(JsonUtils.isExtendedInfoVisible()){
			table.addColumn(userIdColumn,  "User ID");
		}

		// Create name column.
		Column<User, String> nameColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<User, String>() {
					public String getValue(User object) {
						return object.getFullName();
					}
				}, this.tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return o1.getFullName().compareTo(o2.getFullName());
			}
		});

		// Add the other columns.
		table.addColumn(nameColumn, "Name");

		return table;

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
	 * @param object User to be added as new row
	 */
	public void addToTable(User object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object User to be removed as row
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
	public ArrayList<User> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading managers.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading managers started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<User>jsoAsList(jso));
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Managers loaded: " + list.size());
		events.onFinished(jso);
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
		// TODO Auto-generated method stub
	}

	public void setList(ArrayList<User> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<User> getList() {
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
