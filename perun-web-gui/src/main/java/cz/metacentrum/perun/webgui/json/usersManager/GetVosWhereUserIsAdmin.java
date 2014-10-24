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
import cz.metacentrum.perun.webgui.json.columnProviders.IsClickableCell;
import cz.metacentrum.perun.webgui.json.columnProviders.VoColumnProvider;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * The usersManager/getVosWhereUserIsAdmin method.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetVosWhereUserIsAdmin implements JsonCallback, JsonCallbackTable<VirtualOrganization> {

	// JSON URL
	private final String JSON_URL = "usersManager/getVosWhereUserIsAdmin";
	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Data provider.
	private ListDataProvider<VirtualOrganization> dataProvider = new ListDataProvider<VirtualOrganization>();
	// Table itself
	private PerunTable<VirtualOrganization> table;
	// Table list
	private ArrayList<VirtualOrganization> list = new ArrayList<VirtualOrganization>();
	// Selection model
	final MultiSelectionModel<VirtualOrganization> selectionModel = new MultiSelectionModel<VirtualOrganization>(new GeneralKeyProvider<VirtualOrganization>());
	// Custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// User id
	private int userId = 0;
	// Table field updater
	private FieldUpdater<VirtualOrganization, VirtualOrganization> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * Creates a new instance of the request
	 * @param userId User id
	 */
	public GetVosWhereUserIsAdmin(int userId) {
		this.userId = userId;
	}

	/**
	 * Creates a new instance of the request with custom events
	 * @param userId User id
	 * @param events Custom events
	 */
	public GetVosWhereUserIsAdmin(int userId, JsonCallbackEvents events) {
		this.events = events;
		this.userId = userId;
	}


	/**
	 * When called, the VOs are reloaded.
	 */
	public void retrieveData(){
		String param = "user=" + userId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Returns the table widget with VOs and custom onclick.
	 * @param fu Field Updater instance
	 * @return The table Widget
	 */
	public CellTable<VirtualOrganization> getTable(FieldUpdater<VirtualOrganization, VirtualOrganization> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}


	/**
	 * Returns the table widget with VOs.
	 * @return The table Widget
	 */
	public CellTable<VirtualOrganization> getTable(){

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<VirtualOrganization>(list);

		// Cell table
		table = new PerunTable<VirtualOrganization>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<VirtualOrganization> columnSortHandler = new ListHandler<VirtualOrganization>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<VirtualOrganization> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}

		VoColumnProvider columnProvider = new VoColumnProvider(table, tableFieldUpdater);
		IsClickableCell<GeneralObject> authz = VoColumnProvider.getDefaultClickableAuthz();
		columnProvider.addIdColumn(authz, 100);
		columnProvider.addShortNameColumn(authz, 200);
		columnProvider.addNameColumn(authz, 0);

		return table;
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<VirtualOrganization>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Return selected Vos from Table
	 *
	 * @return ArrayList<Vo> selected items
	 */
	public ArrayList<VirtualOrganization> getTableSelectedList() {
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Clears list of selected items
	 */
	public void clearTableSelectedSet(){
		selectionModel.clear();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object VO to be added as new row
	 */
	public void addToTable(VirtualOrganization object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object VO to be removed as row
	 */
	public void removeFromTable(VirtualOrganization object) {
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
	 * Called when the query successfully finishes.
	 * @param jso The JavaScript object returned by the query.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<VirtualOrganization>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Virtual organizations loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading virtual organizations.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading virtual organizations started.");
		events.onLoadingStart();
	}

	public void insertToTable(int index, VirtualOrganization object) {
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

	public void setList(ArrayList<VirtualOrganization> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<VirtualOrganization> getList() {
		return this.list;
	}

}


