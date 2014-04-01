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
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get all resources assigned to selected facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class GetAssignedResources implements JsonCallback, JsonCallbackTable<Resource> {

	// params
	static private final String JSON_URL = "facilitiesManager/getAssignedResources";
	private int facilityId = 0;
	private PerunWebSession session = PerunWebSession.getInstance();
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private ListDataProvider<Resource> dataProvider = new ListDataProvider<Resource>();
	private PerunTable<Resource> table;
	private ArrayList<Resource> list = new ArrayList<Resource>();
	private FieldUpdater<Resource, String> tableFieldUpdater;
	final MultiSelectionModel<Resource> selectionModel = new MultiSelectionModel<Resource>(new GeneralKeyProvider<Resource>());

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of service to get facilities for
	 */
	public GetAssignedResources(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of service to get facilities for
	 * @param events custom events
	 */
	public GetAssignedResources(int facilityId, JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.events = events;
	}

	/**
	 * Returns table with facilities assigned to specified service
	 *
	 * @param fu custom onClick
	 * @return table widget
	 */
	public CellTable<Resource> getTable(FieldUpdater<Resource,String> fu) {
		this.tableFieldUpdater = fu;
		this.retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns table with facilities assigned to specified service
	 *
	 * @return table widget
	 */
	public CellTable<Resource> getTable() {
		this.retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns empty table with facilities assigned to specified service
	 *
	 * @return table widget
	 */
	public CellTable<Resource> getEmptyTable() {

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

		// checkbox column column
		table.addCheckBoxColumn();

		// TABLE CONTENT

		// Create ID column.
		Column<Resource, String> idColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Resource, String>() {
					public String getValue(Resource object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);

		columnSortHandler.setComparator(idColumn, new GeneralComparator<Resource>(GeneralComparator.Column.ID));
		idColumn.setSortable(true);

		// headers
		if(JsonUtils.isExtendedInfoVisible()){
			table.addColumn(idColumn, "Resource ID");
		}

		table.addNameColumn(tableFieldUpdater);

		table.addDescriptionColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		JsonClient cl = new JsonClient();
		cl.retrieveData(JSON_URL, "facility="+facilityId, this);
	}

	/**
	 * Sorts table by objects date
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
	public ArrayList<Resource> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading assigned resources.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading assigned resources started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Resource>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Assigned resources loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
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

}
