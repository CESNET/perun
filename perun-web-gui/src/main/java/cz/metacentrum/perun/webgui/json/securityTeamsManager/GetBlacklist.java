package cz.metacentrum.perun.webgui.json.securityTeamsManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;

/**
 * The list of blacklisted users for security team or facility
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class GetBlacklist implements JsonCallback, JsonCallbackTable<User>, JsonCallbackOracle<User> {

	// JSON URL
	private final String JSON_URL = "securityTeamsManager/getBlacklist";
	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Data provider.
	private ListDataProvider<User> dataProvider = new ListDataProvider<User>();
	// Table itself
	private PerunTable<User> table;
	// Table list
	private ArrayList<User> list = new ArrayList<User>();
	// Selection model
	final MultiSelectionModel<User> selectionModel = new MultiSelectionModel<User>(new GeneralKeyProvider<User>());
	// Custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<User, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private ArrayList<User> fullBackup = new ArrayList<User>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" ");
	private boolean checkable = true;

	private PerunEntity entity;
	private int entityId;

	/**
	 * Creates a new instance of the request
	 *
	 * @param entity
	 * @param id
	 */
	public GetBlacklist(PerunEntity entity, int id) {
		this.entity = entity;
		this.entityId = id;
	}

	/**
	 * Creates a new instance of the request with custom events
	 * @param events Custom events
	 * @param entity
	 * @param id
	 */
	public GetBlacklist(PerunEntity entity, int id, JsonCallbackEvents events) {
		this.entity = entity;
		this.entityId = id;
		this.events = events;
	}

	public void retrieveData(){

		JsonClient js = new JsonClient();

		if (entity != null && entity.equals(PerunEntity.SECURITY_TEAM)) {
			js.retrieveData(JSON_URL, "securityTeam="+entityId, this);
		} else if (entity != null && entity.equals(PerunEntity.FACILITY)) {
			js.retrieveData(JSON_URL, "facility="+entityId, this);
		}

	}

	/**
	 * Returns the table widget with VOs and custom onclick.
	 * @param fu Field Updater instance
	 * @return The table Widget
	 */
	public CellTable<User> getTable(FieldUpdater<User, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}


	/**
	 * Returns the table widget with VOs.
	 * @return The table Widget
	 */
	public CellTable<User> getTable(){

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<User>(list);

		// Cell table
		table = new PerunTable<User>(list);
		table.setHyperlinksAllowed(false); // prevent double loading when clicked on vo name

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<User> columnSortHandler = new ListHandler<User>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<User> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		loaderImage.setEmptyResultMessage("Blacklist is empty.");

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}

		table.addIdColumn("User ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Return selected Vos from Table
	 *
	 * @return ArrayList<Vo> selected items
	 */
	public ArrayList<User> getTableSelectedList() {
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
		list = new TableSorter<User>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object VO to be added as new row
	 */
	public void addToTable(User object) {
		list.add(object);
		oracle.add(object.getFullName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object VO to be removed as row
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
		setList(JsonUtils.<User>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Blacklisted users loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Blacklist.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Blacklist started.");
		events.onLoadingStart();
	}

	public void insertToTable(int index, User object) {
		list.add(index, object);
		oracle.add(object.getFullName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<User> list) {
		clearTable();
		this.list.addAll(list);
		for (User user : list) {
			oracle.add(user.getFullName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<User> getList() {
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
			for (User vo : fullBackup){
				// store facility by filter
				if (vo.getFullName().toLowerCase().contains(filter.toLowerCase())) {
					list.add(vo);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No Blacklisted user matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("Blacklist is empty.");
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
