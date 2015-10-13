package cz.metacentrum.perun.webgui.json.securityTeamsManager;

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
import cz.metacentrum.perun.webgui.model.SecurityTeam;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;

/**
 * The list of SecurityTeams.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class GetSecurityTeams implements JsonCallback, JsonCallbackTable<SecurityTeam>, JsonCallbackOracle<SecurityTeam> {

	// JSON URL
	private final String JSON_URL = "securityTeamsManager/getSecurityTeams";
	private final String JSON_URL_ALL_VOS = "securityTeamsManager/getAllSecurityTeams";
	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Data provider.
	private ListDataProvider<SecurityTeam> dataProvider = new ListDataProvider<SecurityTeam>();
	// Table itself
	private PerunTable<SecurityTeam> table;
	// Table list
	private ArrayList<SecurityTeam> list = new ArrayList<SecurityTeam>();
	// Selection model
	final MultiSelectionModel<SecurityTeam> selectionModel = new MultiSelectionModel<SecurityTeam>(new GeneralKeyProvider<SecurityTeam>());
	// Custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<SecurityTeam, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private ArrayList<SecurityTeam> fullBackup = new ArrayList<SecurityTeam>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" .-");
	private boolean forceAll = false;
	private boolean checkable = true;

	/**
	 * Creates a new instance of the request
	 */
	public GetSecurityTeams() {}

	/**
	 * Creates a new instance of the request with custom events
	 * @param events Custom events
	 */
	public GetSecurityTeams(JsonCallbackEvents events) {
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
	public CellTable<SecurityTeam> getTable(FieldUpdater<SecurityTeam, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}


	/**
	 * Returns the table widget with VOs.
	 * @return The table Widget
	 */
	public CellTable<SecurityTeam> getTable(){

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<SecurityTeam>(list);

		// Cell table
		table = new PerunTable<SecurityTeam>(list);
		table.setHyperlinksAllowed(false); // prevent double loading when clicked on vo name

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<SecurityTeam> columnSortHandler = new ListHandler<SecurityTeam>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<SecurityTeam> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		if (forceAll) {
			loaderImage.setEmptyResultMessage("No Security Teams found.");
		} else {
			loaderImage.setEmptyResultMessage("You are not member of any Security team.");
		}

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}

		table.addIdColumn("SecTeam ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Return selected Vos from Table
	 *
	 * @return ArrayList<Vo> selected items
	 */
	public ArrayList<SecurityTeam> getTableSelectedList() {
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
		list = new TableSorter<SecurityTeam>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object VO to be added as new row
	 */
	public void addToTable(SecurityTeam object) {
		list.add(object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object VO to be removed as row
	 */
	public void removeFromTable(SecurityTeam object) {
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
		setList(JsonUtils.<SecurityTeam>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Security Teams loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading security teams.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Security teams started.");
		events.onLoadingStart();
	}

	public void insertToTable(int index, SecurityTeam object) {
		list.add(index, object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<SecurityTeam> list) {
		clearTable();
		this.list.addAll(list);
		for (SecurityTeam vo : list) {
			oracle.add(vo.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<SecurityTeam> getList() {
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
			for (SecurityTeam vo : fullBackup){
				// store facility by filter
				if (vo.getName().toLowerCase().contains(filter.toLowerCase())) {
					list.add(vo);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No Security Team matching '"+filter+"' found.");
		} else {
			if (forceAll) {
				loaderImage.setEmptyResultMessage("No Security Teams found.");
			} else {
				loaderImage.setEmptyResultMessage("You are not member of any Security Team.");
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

	public MultiSelectionModel<SecurityTeam> getSelectionModel() {
		return this.selectionModel;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}
