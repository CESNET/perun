package cz.metacentrum.perun.webgui.json.vosManager;

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
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;

/**
 * The list of VOs query. You can get VOs by "getVos" method.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class GetVos implements JsonCallback, JsonCallbackTable<VirtualOrganization>, JsonCallbackOracle<VirtualOrganization> {

	// JSON URL
	private final String JSON_URL = "vosManager/getVos";
	private final String JSON_URL_ALL_VOS = "vosManager/getAllVos";
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
	// Table field updater
	private FieldUpdater<VirtualOrganization, VirtualOrganization> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private ArrayList<VirtualOrganization> fullBackup = new ArrayList<VirtualOrganization>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" .-");
	private boolean forceAll = false;
	private boolean checkable = true;

	/**
	 * Creates a new instance of the request
	 */
	public GetVos() {}

	/**
	 * Creates a new instance of the request with custom events
	 * @param events Custom events
	 */
	public GetVos(JsonCallbackEvents events) {
		this.events = events;
	}

	public void setForceAll(boolean force) {
		this.forceAll = force;
	}

	public void retrieveData(){

		JsonClient js = new JsonClient();

		if (forceAll) {
			js.retrieveData(JSON_URL_ALL_VOS, this);
		} else {
			js.retrieveData(JSON_URL, this);
		}

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
		table.setHyperlinksAllowed(false); // prevent double loading when clicked on vo name

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<VirtualOrganization> columnSortHandler = new ListHandler<VirtualOrganization>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<VirtualOrganization> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		if (forceAll) {
			loaderImage.setEmptyResultMessage("No VOs found.");
		} else {
			loaderImage.setEmptyResultMessage("You are not manager of any VO.");
		}

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}

		VoColumnProvider columnProvider = new VoColumnProvider(table, tableFieldUpdater);
		IsClickableCell<GeneralObject> authz = VoColumnProvider.getDefaultClickableAuthz();

		columnProvider.addIdColumn(authz, 100);
		columnProvider.addShortNameColumn(authz, 200);
		columnProvider.addNameColumn(authz);

		return table;

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
	 *  clears list of selected items
	 */
	public void clearTableSelectedSet(){
		selectionModel.clear();
	}

	/**
	 * Sorts table by objects ID
	 */
	public void sortTable() {
		list = new TableSorter<VirtualOrganization>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object VO to be added as new row
	 */
	public void addToTable(VirtualOrganization object) {
		list.add(object);
		oracle.add(object.getName());
		oracle.add(object.getShortName());
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
		oracle.clear();
		fullBackup.clear();
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
		oracle.add(object.getName());
		oracle.add(object.getShortName());
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
		for (VirtualOrganization vo : list) {
			oracle.add(vo.getName());
			oracle.add(vo.getShortName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<VirtualOrganization> getList() {
		return this.list;
	}

	@Override
	public void filterTable(String filter) {

		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			fullBackup.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (filter.equalsIgnoreCase("")) {
			list.addAll(fullBackup);
		} else {
			for (VirtualOrganization vo : fullBackup){
				// store facility by filter
				if (vo.getName().toLowerCase().contains(filter.toLowerCase()) || vo.getShortName().toLowerCase().contains(filter.toLowerCase())) {
					list.add(vo);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No VO matching '"+filter+"' found.");
		} else {
			if (forceAll) {
				loaderImage.setEmptyResultMessage("No VOs found.");
			} else {
				loaderImage.setEmptyResultMessage("You are not manager of any VO.");
			}
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	@Override
	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	@Override
	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}
}
