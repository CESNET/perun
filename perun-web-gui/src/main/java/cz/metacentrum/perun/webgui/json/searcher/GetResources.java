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
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Searching for Resources
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetResources implements JsonCallback, JsonCallbackTable<Resource> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "searcher/getResources";
	// Data provider
	private ListDataProvider<Resource> dataProvider = new ListDataProvider<Resource>();
	// table
	private PerunTable<Resource> table;
	// table data
	private ArrayList<Resource> list = new ArrayList<Resource>();
	// Selection model
	final MultiSelectionModel<Resource> selectionModel = new MultiSelectionModel<Resource>(new GeneralKeyProvider<Resource>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Resource, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, "Enter keywords to search.");

	// checkable
	private boolean checkable = true;

	// private hashmap attributes to search by
	private Map<String, String> attributesToSearchBy = new HashMap<String, String>();



	/**
	 * Creates a new request
	 */
	public GetResources() {
	}

	/**
	 * Creates a new request with custom events
	 * @param events
	 */
	public GetResources(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table of users
	 * @param
	 */
	public CellTable<Resource> getTable(FieldUpdater<Resource, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table of users.
	 * @return
	 */
	public CellTable<Resource> getTable(){
		// retrieve data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<Resource> getEmptyTable(FieldUpdater<Resource, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	};

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<Resource> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Resource>(list);

		// Cell table
		table = new PerunTable<Resource>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Resource> columnSortHandler = new ListHandler<Resource>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Resource> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Resource ID", tableFieldUpdater);

		// NAME COLUMN
		Column<Resource, String> nameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Resource, String>() {
			public String getValue(Resource resource) {
				return resource.getName();
			}
		},tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<Resource>() {
			public int compare(Resource o1, Resource o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		// Description
		Column<Resource, String> descriptionColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Resource, String>() {
			public String getValue(Resource resource) {
				return resource.getDescription();
			}
		},tableFieldUpdater);

		descriptionColumn.setSortable(true);
		columnSortHandler.setComparator(descriptionColumn, new Comparator<Resource>() {
			public int compare(Resource o1, Resource o2) {
				return o1.getDescription().compareToIgnoreCase(o2.getDescription());
			}
		});

		Column<Resource, String> voIdColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Resource, String>() {
			public String getValue(Resource resource) {
				return ""+resource.getVoId();
			}
		},tableFieldUpdater);
		voIdColumn.setSortable(true);
		columnSortHandler.setComparator(voIdColumn, new Comparator<Resource>() {
			public int compare(Resource o1, Resource o2) {
				return (o1.getVoId()+"").compareTo(o2.getVoId()+"");
			}
		});

		Column<Resource, String> facilityIdColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Resource, String>() {
			public String getValue(Resource resource) {
				return ""+resource.getFacilityId();
			}
		},tableFieldUpdater);
		facilityIdColumn.setSortable(true);
		columnSortHandler.setComparator(facilityIdColumn, new Comparator<Resource>() {
			public int compare(Resource o1, Resource o2) {
				return (o1.getFacilityId()+"").compareTo(o2.getFacilityId()+"");
			}
		});

		table.addColumn(nameColumn, "Name");
		table.addColumn(descriptionColumn, "Description");
		table.addColumn(voIdColumn, "VO Id");
		table.addColumn(facilityIdColumn, "Facility Id");

		return table;

	}

	/**
	 * Do search
	 */
	public void search(){

		loaderImage.setEmptyResultMessage("No resurces found.");

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
				session.getUiElements().setLogErrorText("Error while loading resources.");
				loaderImage.loadingError(error);
				events.onError(error);
			}
			public void onLoadingStart() {
				loaderImage.loadingStart();
				session.getUiElements().setLogText("Loading resources started.");
				events.onLoadingStart();
			}
			public void onFinished(JavaScriptObject jso) {
				loaderImage.loadingFinished();
				setList(JsonUtils.<Resource>jsoAsList(jso));
				sortTable();
				session.getUiElements().setLogText("Resources loaded: " + list.size());
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
		list = new TableSorter<Resource>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(Resource object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(Resource object) {
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
	public ArrayList<Resource> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading resources.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading resources started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		loaderImage.loadingFinished();
		setList(JsonUtils.<Resource>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Resources loaded: " + list.size());
		events.onFinished(jso);
	}

	public void insertToTable(int index, Resource object) {
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

	public void setList(ArrayList<Resource> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Resource> getList() {
		return this.list;
	}


	public void setSelected(Resource resource) {
		selectionModel.setSelected(resource, true);
	}

	public void setEvents(JsonCallbackEvents event) {
		this.events = event;
	}

	public void clearParameters() {
		this.attributesToSearchBy.clear();
	}


}
