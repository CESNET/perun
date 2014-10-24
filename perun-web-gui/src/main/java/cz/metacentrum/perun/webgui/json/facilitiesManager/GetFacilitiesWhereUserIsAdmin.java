package cz.metacentrum.perun.webgui.json.facilitiesManager;

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
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get facilties where user is administrator.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetFacilitiesWhereUserIsAdmin implements JsonCallback, JsonCallbackTable<Facility> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// USER id
	private int userId;
	// JSON URL
	static private final String JSON_URL = "facilitiesManager/getFacilitiesWhereUserIsAdmin";
	// Selection model
	final MultiSelectionModel<Facility> selectionModel = new MultiSelectionModel<Facility>(new GeneralKeyProvider<Facility>());
	// Facilitys table data provider
	private ListDataProvider<Facility> dataProvider = new ListDataProvider<Facility>();
	// Facilitys table
	private PerunTable<Facility> table;
	// Facilitys table list
	private ArrayList<Facility> list = new ArrayList<Facility>();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Facility, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	/**
	 * Creates a new callback
	 *
	 * @param id
	 */
	public GetFacilitiesWhereUserIsAdmin(int id) {
		this.userId = id;
	}

	/**
	 * Creates a new callback
	 *
	 * @param id
	 * @param events
	 */
	public GetFacilitiesWhereUserIsAdmin(int id, JsonCallbackEvents events) {
		this.userId = id;
		this.events = events;
	}

	/**
	 * Facilities table with custom field updater
	 * @param fu Custom field updater
	 * @return
	 */
	public CellTable<Facility> getTable(FieldUpdater<Facility, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns facilities table
	 * @return
	 */
	public CellTable<Facility> getTable() {

		// retrieve data
		retrieveData();

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

		// checkbox column column
		table.addCheckBoxColumn();
		table.addIdColumn("Facility ID", tableFieldUpdater);
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
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<Facility>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(Facility object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
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
	public ArrayList<Facility> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Facilities where user is admin.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Facilities where user is admin started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Facility>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Facilities loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
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

}
