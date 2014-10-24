package cz.metacentrum.perun.webgui.json.usersManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
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

import java.util.ArrayList;

/**
 * GroupsManager/getGroups method
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetGroupsWhereUserIsAdmin implements JsonCallback, JsonCallbackTable<Group> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// USER id
	private int userId;
	// JSON URL
	static private final String JSON_URL = "usersManager/getGroupsWhereUserIsAdmin";
	// Selection model
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<Group>(new GeneralKeyProvider<Group>());
	// Groups table data provider
	private ListDataProvider<Group> dataProvider = new ListDataProvider<Group>();
	// Groups table
	private PerunTable<Group> table;
	// Groups table list
	private ArrayList<Group> list = new ArrayList<Group>();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Group, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * All groups in VO request.
	 * @param id
	 */
	public GetGroupsWhereUserIsAdmin(int id) {
		this.userId = id;
	}

	/**
	 * All groups in VO request with custom events.
	 * @param id
	 * @param events
	 */
	public GetGroupsWhereUserIsAdmin(int id, JsonCallbackEvents events) {
		this.userId = id;
		this.events = events;
	}

	/**
	 * Groups table with custom field updater
	 * @param fu Custom field updater
	 * @return
	 */
	public CellTable<Group> getTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns groups table
	 * @return
	 */
	public CellTable<Group> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Group>(list);

		// Cell table
		table = new PerunTable<Group>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Group> columnSortHandler = new ListHandler<Group>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Group> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Group ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		final String param = "user=" + this.userId;
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
		session.getUiElements().setLogErrorText("Error while loading users groups.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading users groups started.");
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
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Group> getList() {
		return this.list;
	}

}
