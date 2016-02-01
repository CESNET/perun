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
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * UsersManager/getUsers
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetUsers implements JsonCallback, JsonCallbackTable<User> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "usersManager/getUsers";
	// Data provider
	private ListDataProvider<User> dataProvider = new ListDataProvider<User>();
	// table
	private PerunTable<User> table;
	// table data
	private ArrayList<User> list = new ArrayList<User>();
	// Selection model
	final MultiSelectionModel<User> selectionModel = new MultiSelectionModel<User>(new GeneralKeyProvider<User>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<User, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// filter by user type (default show all)
	private boolean hideService = false;
	private boolean hidePerson = false;
	private boolean hideSponsored = false;
	private boolean checkable = true;

	/**
	 * Creates a new request
	 */
	public GetUsers() {}

	/**
	 * Creates a new request with custom events
	 * @param events
	 */
	public GetUsers(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table of users
	 * @param fu
	 */
	public CellTable<User> getTable(FieldUpdater<User, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table of users.
	 * @return table
	 */
	public CellTable<User> getTable(){

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
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<User> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

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
		},tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return o1.getLastName().compareToIgnoreCase(o2.getLastName());
			}
		});

		table.addColumn(nameColumn, "Name");

		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
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
		session.getUiElements().setLogErrorText("Error while loading users.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading users started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		ArrayList<User> list = JsonUtils.jsoAsList(jso);
		for (User u : list) {
			if (hideService && u.isServiceUser())  {
				// if service hidden, skip service users
			} else if (hidePerson && !u.isServiceUser() && !u.isSponsoredUser()) {
				// if person hidden, skip person
			} else if (hideSponsored && !u.isSponsoredUser()) {
				// if sponsored hidden, skip person
			} else {
				addToTable(u);
			}
		}
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

	public void setList(ArrayList<User> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<User> getList() {
		return this.list;
	}

	public void hideService(boolean hide){
		this.hideService = hide;
	}

	public void hidePerson(boolean hide){
		this.hidePerson = hide;
	}

	public boolean isHideSponsored() {
		return hideSponsored;
	}

	public void setHideSponsored(boolean hideSponsored) {
		this.hideSponsored = hideSponsored;
	}
}
