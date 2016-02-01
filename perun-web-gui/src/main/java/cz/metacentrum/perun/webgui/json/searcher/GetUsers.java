package cz.metacentrum.perun.webgui.json.searcher;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Searching for users.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetUsers implements JsonCallback, JsonCallbackTable<User> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "searcher/getUsers";
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
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, "Enter keywords to search.");

	// checkable
	private boolean checkable = true;

	// private hashmap attributes to search by
	private Map<String, String> attributesToSearchBy = new HashMap<String, String>();



	/**
	 * Creates a new request
	 */
	public GetUsers() {
	}

	/**
	 * Creates a new request with custom events
	 * @param events
	 */
	public GetUsers(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table of users
	 * @param
	 */
	public CellTable<User> getTable(FieldUpdater<User, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table of users.
	 * @return
	 */
	public CellTable<User> getTable(){
		// retrieve data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<User> getEmptyTable(FieldUpdater<User, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	};

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<User> getEmptyTable(){

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
				return user.getFullNameWithTitles(); // display full name with titles
			}
		},tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return o1.getFullName().compareToIgnoreCase(o2.getFullName());  // sort by name without titles
			}
		});

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
		},tableFieldUpdater);

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

		table.addColumn(nameColumn, "Name");
		table.addColumn(serviceColumn, "User type");

		return table;

	}

	/**
	 * Do search
	 */
	public void search(){

		loaderImage.setEmptyResultMessage("No users found.");

		clearTable();
		retrieveData();
	}

	/**
	 * Add parameter
	 */
	public void addSearchParameter(String name, String value){
		attributesToSearchBy.put(name, value);
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		// empty
		if(this.attributesToSearchBy.size() == 0){
			session.getUiElements().setLogText("No keywords.");
			return;
		}

		// ok, start
		loaderImage.loadingStart();

		// build request

		JSONObject attributesWithSearchingValues = new JSONObject();
		for(Map.Entry<String, String> entry : attributesToSearchBy.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();

			attributesWithSearchingValues.put(name, new JSONString(value));
		}

		JSONObject req = new JSONObject();
		req.put("attributesWithSearchingValues", attributesWithSearchingValues);

		// send request
		JsonPostClient js = new JsonPostClient(new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Error while loading users.");
				loaderImage.loadingError(error);
				events.onError(error);
			}
			public void onLoadingStart() {
				loaderImage.loadingStart();
				session.getUiElements().setLogText("Loading users started.");
				events.onLoadingStart();
			}
			public void onFinished(JavaScriptObject jso) {
				loaderImage.loadingFinished();
				setList(JsonUtils.<User>jsoAsList(jso));
				sortTable();
				session.getUiElements().setLogText("Users loaded: " + list.size());
				events.onFinished(jso);
			}


		});
		js.sendData(JSON_URL, req);

		return;
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
		loaderImage.loadingFinished();
		setList(JsonUtils.<User>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Users loaded: " + list.size());
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


	public void setSelected(User user) {
		selectionModel.setSelected(user, true);
	}

	public void setEvents(JsonCallbackEvents event) {
		this.events = event;
	}

	public void clearParameters() {
		this.attributesToSearchBy.clear();
	}


}
