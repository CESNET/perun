package cz.metacentrum.perun.webgui.json.resourcesManager;

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
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Get allowed resources for a Member
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetAllowedResources implements JsonCallback, JsonCallbackTable<Resource> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Member id
	private int memberId;
	// JSON URL
	static private final String JSON_URL = "resourcesManager/getAllowedResources";
	// Selection model
	final MultiSelectionModel<Resource> selectionModel = new MultiSelectionModel<Resource>(
			new GeneralKeyProvider<Resource>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<Resource> dataProvider = new ListDataProvider<Resource>();
	// Table itself
	private PerunTable<Resource> table;
	// Table list
	private ArrayList<Resource> list = new ArrayList<Resource>();
	// Table field updater
	private FieldUpdater<Resource, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable;

	/**
	 * Creates a new getResources method instance
	 *
	 * @param id Member id
	 */
	public GetAllowedResources(int id) {
		this.memberId = id;
	}

	/**
	 * Creates a new getResources method instance
	 *
	 * @param id Member id
	 * @param events Custom events
	 */
	public GetAllowedResources(int id, JsonCallbackEvents events) {
		this.memberId = id;
		this.events = events;
	}

	/**
	 * Returns the celltable with custom onclick
	 * @param fu Field updater
	 * @return
	 */
	public CellTable<Resource> getTable(FieldUpdater<Resource, String> fu)
	{
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns just the celltable
	 * @return
	 */
	public CellTable<Resource> getTable() {

		// retrieve data
		retrieveData();

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
		if(this.checkable)
		{
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Resource ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData()
	{
		final String param = "member=" + this.memberId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
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
		session.getUiElements().setLogErrorText("Error while loading members resources.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading members resources started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Resource>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Resources loaded: " + list.size());
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
		this.checkable = checkable;
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
