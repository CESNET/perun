package cz.metacentrum.perun.webgui.json.facilitiesManager;

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
 * Ajax query to get allowed vos on facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAllowedVos implements JsonCallback, JsonCallbackTable<VirtualOrganization> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "facilitiesManager/getAllowedVos";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<VirtualOrganization> dataProvider = new ListDataProvider<VirtualOrganization>();
	private ArrayList<VirtualOrganization> list = new ArrayList<VirtualOrganization>();
	private PerunTable<VirtualOrganization> table;
	// Selection model
	final MultiSelectionModel<VirtualOrganization> selectionModel = new MultiSelectionModel<VirtualOrganization>(new GeneralKeyProvider<VirtualOrganization>());
	private int facilityId = 0;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of facility to get allowed VOs for
	 */
	public GetAllowedVos(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of facility to get allowed VOs for
	 * @param events external events
	 */
	public GetAllowedVos(int facilityId, JsonCallbackEvents events) {
		this.events = events;
		this.facilityId = facilityId;
	}

	/**
	 * Returns table widget with allowed Vos
	 *
	 * @return table widget
	 */
	public CellTable<VirtualOrganization> getTable() {

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
		if (checkable == true){
			table.addCheckBoxColumn();
		}

		VoColumnProvider columnProvider = new VoColumnProvider(table, null);
		// FIXME we need field updater
		IsClickableCell<GeneralObject> authz = VoColumnProvider.getDefaultClickableAuthz();
		columnProvider.addIdColumn(authz, 100);
		columnProvider.addShortNameColumn(authz, 200);
		columnProvider.addNameColumn(authz, 0);

		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "facility="+facilityId, this);
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
	 * Add object as new row to table
	 *
	 * @param object VirtualOrganization to be added as new row
	 */
	public void addToTable(VirtualOrganization object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object VirtualOrganization to be removed as row
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
	public ArrayList<VirtualOrganization> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading allowed VOs.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading allowed VOs started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<VirtualOrganization>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Loading allowed VOs finished: "+ list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
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
		// TODO Auto-generated method stub
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
