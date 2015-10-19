package cz.metacentrum.perun.webgui.json.securityTeamsManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.PairKeyProvider;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * The list of blacklisted users for security team or facility
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class GetBlacklistWithDescription implements JsonCallback, JsonCallbackTable<Pair<User,String>>, JsonCallbackOracle<Pair<User,String>> {

	// JSON URL
	private final String JSON_URL = "securityTeamsManager/getBlacklistWithDescription";
	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Data provider.
	private ListDataProvider<Pair<User,String>> dataProvider = new ListDataProvider<Pair<User,String>>();
	// Table itself
	private PerunTable<Pair<User,String>> table;
	// Table list
	private ArrayList<Pair<User,String>> list = new ArrayList<Pair<User,String>>();
	// Selection model
	final MultiSelectionModel<Pair<User,String>> selectionModel = new MultiSelectionModel<Pair<User,String>>(new PairKeyProvider<User,String>());
	// Custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Pair<User,String>, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private ArrayList<Pair<User,String>> fullBackup = new ArrayList<Pair<User,String>>();
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
	public GetBlacklistWithDescription(PerunEntity entity, int id) {
		this.entity = entity;
		this.entityId = id;
	}

	/**
	 * Creates a new instance of the request with custom events
	 * @param events Custom events
	 * @param entity
	 * @param id
	 */
	public GetBlacklistWithDescription(PerunEntity entity, int id, JsonCallbackEvents events) {
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
	public CellTable<Pair<User,String>> getTable(FieldUpdater<Pair<User,String>, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}


	/**
	 * Returns the table widget with VOs.
	 * @return The table Widget
	 */
	public CellTable<Pair<User,String>> getTable(){

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Pair<User,String>>(list);

		// Cell table
		table = new PerunTable<Pair<User,String>>(list);
		table.setHyperlinksAllowed(false); // prevent double loading when clicked on vo name

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Pair<User,String>> columnSortHandler = new ListHandler<Pair<User,String>>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Pair<User,String>> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		loaderImage.setEmptyResultMessage("Blacklist is empty.");

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}

		Column<Pair<User,String>, String> idColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Pair<User,String>, String>() {
					public String getValue(Pair<User,String> object) {
						return String.valueOf(object.getLeft().getId());
					}
				}, tableFieldUpdater);

		idColumn.setSortable(true);
		// comparator
		columnSortHandler.setComparator(idColumn, new Comparator<Pair<User, String>>() {
			@Override
			public int compare(Pair<User, String> o1, Pair<User, String> o2) {
				return o1.getLeft().getId() - o2.getLeft().getId();
			}
		});

		// adding columns
		table.addColumn(idColumn, "User ID");
		table.setColumnWidth(idColumn, "150px");

		Column<Pair<User,String>,String> nameColumn = JsonUtils.addColumn(new CustomClickableTextCell(), new JsonUtils.GetValue<Pair<User,String>, String>() {
			@Override
			public String getValue(Pair<User, String> object) {
				return object.getLeft().getFullNameWithTitles();
			}
		}, tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<Pair<User,String>>() {
			public int compare(Pair<User,String> o1, Pair<User,String> o2) {
				return o1.getLeft().getFullName().compareToIgnoreCase(o2.getLeft().getFullName());
			}
		});
		table.addColumn(nameColumn, "Name");
		table.setColumnWidth(nameColumn, "40%");

		// Type column
		TextColumn<Pair<User,String>> reasonColumn = new TextColumn<Pair<User,String>>() {
			@Override
			public String getValue(Pair<User,String> pair) {
				return pair.getRight();
			}
		};

		// sort type column
		reasonColumn.setSortable(true);
		columnSortHandler.setComparator(reasonColumn, new Comparator<Pair<User,String>>() {
			public int compare(Pair<User,String> o1, Pair<User,String> o2) {
				return o1.getRight().compareToIgnoreCase(o2.getRight());
			}
		});

		// Add columns to table
		table.addColumn(reasonColumn, "Reason");
		table.setColumnWidth(reasonColumn, "50%");

		return table;

	}

	/**
	 * Return selected Vos from Table
	 *
	 * @return ArrayList<Vo> selected items
	 */
	public ArrayList<Pair<User,String>> getTableSelectedList() {
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
		// TODO
		list = new TableSorter<Pair<User,String>>().sortByLeft(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object VO to be added as new row
	 */
	public void addToTable(Pair<User,String> object) {
		list.add(object);
		oracle.add(object.getLeft().getFullName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object VO to be removed as row
	 */
	public void removeFromTable(Pair<User,String> object) {
		list.remove(object.getLeft());
		selectionModel.getSelectedSet().remove(object.getLeft());
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
		setList(JsonUtils.<Pair<User,String>>jsoAsList(jso));
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

	public void insertToTable(int index, Pair<User,String> object) {
		list.add(index, object);
		oracle.add(object.getLeft().getFullName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<Pair<User,String>> list) {
		clearTable();
		this.list.addAll(list);
		for (Pair<User,String> user : list) {
			oracle.add(user.getLeft().getFullName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Pair<User,String>> getList() {
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
			for (Pair<User,String> pair : fullBackup){
				// store facility by filter
				if (pair.getLeft().getFullName().toLowerCase().contains(filter.toLowerCase())) {
					list.add(pair);
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
