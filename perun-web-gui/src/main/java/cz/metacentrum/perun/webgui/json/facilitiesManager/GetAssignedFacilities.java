package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
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
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all facilities where selected service is assigned
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class GetAssignedFacilities implements JsonCallback, JsonCallbackTable<Facility> {

	// params
	static private final String JSON_URL = "facilitiesManager/getAssignedFacilities";
	private int typeId = 0;
	private PerunWebSession session = PerunWebSession.getInstance();
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private ListDataProvider<Facility> dataProvider = new ListDataProvider<Facility>();
	private PerunTable<Facility> table;
	private ArrayList<Facility> list = new ArrayList<Facility>();
	private FieldUpdater<Facility, String> tableFieldUpdater;
	final MultiSelectionModel<Facility> selectionModel = new MultiSelectionModel<Facility>(new GeneralKeyProvider<Facility>());


	// type which get assigned facilites for
	private PerunEntity type;

	/**
	 * Creates a new callback
	 *
	 * @param type One of the types: SERVICE, GROUP, MEMBER, USER
	 * @param id ID of service to get facilities for
	 */
	public GetAssignedFacilities(PerunEntity type, int id) {
		this.type = type;
		this.typeId = id;
	}

	/**
	 * Creates a new callback
	 *
	 * @param type One of the types: SERVICE, GROUP, MEMBER, USER
	 * @param id ID of service to get facilities for
	 * @param events custom events
	 */
	public GetAssignedFacilities(PerunEntity type, int id, JsonCallbackEvents events) {
		this.type = type;
		this.typeId = id;
		this.events = events;
	}

	/**
	 * Returns table with facilities assigned to specified service
	 *
	 * @param fu custom onClick
	 * @return table widget
	 */
	public CellTable<Facility> getTable(FieldUpdater<Facility,String> fu) {
		this.tableFieldUpdater = fu;
		this.retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns table with facilities assigned to specified service
	 *
	 * @return table widget
	 */
	public CellTable<Facility> getTable() {
		this.retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns empty table with facilities assigned to specified service
	 *
	 * @return table widget
	 */
	public CellTable<Facility> getEmptyTable() {

		// Table data provider.
		dataProvider = new ListDataProvider<Facility>(list);

		// Cell table
		table = new PerunTable<Facility>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		table.setHyperlinksAllowed(false); // prevent double-loading when clicked on name of facility in list

		// Sorting
		ListHandler<Facility> columnSortHandler = new ListHandler<Facility>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Facility> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// columns
		table.addCheckBoxColumn();
		table.addIdColumn("Facility ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		JsonClient cl = new JsonClient();

		String request = "=" + typeId;

		switch(type){
			case GROUP:
				request = "group" + request;
				break;
			case MEMBER:
				request = "member" + request;
				break;
			case SERVICE:
				request = "service" + request;
				break;
			case USER:
				request = "user" + request;
				break;
			default:
				session.getUiElements().setLogErrorText("Failed to get the associated facility.");
		}
		cl.retrieveData(JSON_URL, request, this);
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
		session.getUiElements().setLogErrorText("Error while loading assigned facilities.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading assigned facilities for " + type.toString() + " id: " + typeId + " started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Facility>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Loading assigned facilities for " + type.toString() + " id: " + typeId + " finished: "+ list.size());
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
