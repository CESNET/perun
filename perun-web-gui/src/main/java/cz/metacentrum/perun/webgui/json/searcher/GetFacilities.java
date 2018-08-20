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
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Searching for Facilities
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetFacilities implements JsonCallback, JsonCallbackTable<Facility> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "searcher/getFacilities";
	// Data provider
	private ListDataProvider<Facility> dataProvider = new ListDataProvider<Facility>();
	// table
	private PerunTable<Facility> table;
	// table data
	private ArrayList<Facility> list = new ArrayList<Facility>();
	// Selection model
	final MultiSelectionModel<Facility> selectionModel = new MultiSelectionModel<Facility>(new GeneralKeyProvider<Facility>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Facility, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, "Enter keywords to search.");

	// checkable
	private boolean checkable = true;

	// private hashmap attributes to search by
	private Map<String, String> attributesToSearchBy = new HashMap<String, String>();



	/**
	 * Creates a new request
	 */
	public GetFacilities() {
	}

	/**
	 * Creates a new request with custom events
	 * @param events
	 */
	public GetFacilities(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table of users
	 * @param
	 */
	public CellTable<Facility> getTable(FieldUpdater<Facility, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table of users.
	 * @return
	 */
	public CellTable<Facility> getTable(){
		// retrieve data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<Facility> getEmptyTable(FieldUpdater<Facility, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	};

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<Facility> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Facility>(list);

		// Cell table
		table = new PerunTable<Facility>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Facility> columnSortHandler = new ListHandler<Facility>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Facility> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Facility ID", tableFieldUpdater);

		// NAME COLUMN
		Column<Facility, String> nameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Facility, String>() {
			public String getValue(Facility facility) {
				return facility.getName(); // display full name with titles
			}
		},tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<Facility>() {
			public int compare(Facility o1, Facility o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());  // sort by name without titles
			}
		});

		// Description
		Column<Facility, String> descriptionColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Facility, String>() {
			public String getValue(Facility facility) {
				return facility.getDescription(); // display full name with titles
			}
		},tableFieldUpdater);

		descriptionColumn.setSortable(true);
		columnSortHandler.setComparator(descriptionColumn, new Comparator<Facility>() {
			public int compare(Facility o1, Facility o2) {
				return o1.getDescription().compareToIgnoreCase(o2.getDescription());
			}
		});

		table.addColumn(nameColumn, "Name");
		table.addColumn(descriptionColumn, "Description");

		return table;

	}

	/**
	 * Do search
	 */
	public void search(){

		loaderImage.setEmptyResultMessage("No facilities found.");

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
				session.getUiElements().setLogErrorText("Error while loading facilities.");
				loaderImage.loadingError(error);
				events.onError(error);
			}
			public void onLoadingStart() {
				loaderImage.loadingStart();
				session.getUiElements().setLogText("Loading facilities started.");
				events.onLoadingStart();
			}
			public void onFinished(JavaScriptObject jso) {
				loaderImage.loadingFinished();
				setList(JsonUtils.<Facility>jsoAsList(jso));
				sortTable();
				session.getUiElements().setLogText("Facilities loaded: " + list.size());
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
		list = new TableSorter<Facility>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Facility to be added as new row
	 */
	public void addToTable(Facility object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Facility to be removed as row
	 */
	public void removeFromTable(Facility object) {
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
	public ArrayList<Facility> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading facilities.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading facilities started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		loaderImage.loadingFinished();
		setList(JsonUtils.<Facility>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Facilities loaded: " + list.size());
		events.onFinished(jso);
	}

	public void insertToTable(int index, Facility object) {
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

	public void setList(ArrayList<Facility> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Facility> getList() {
		return this.list;
	}


	public void setSelected(Facility facility) {
		selectionModel.setSelected(facility, true);
	}

	public void setEvents(JsonCallbackEvents event) {
		this.events = event;
	}

	public void clearParameters() {
		this.attributesToSearchBy.clear();
	}


}
