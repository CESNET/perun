package cz.metacentrum.perun.webgui.json.ownersManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ajax query to get all owners
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetOwners implements JsonCallback, JsonCallbackTable<Owner>, JsonCallbackOracle<Owner> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "ownersManager/getOwners";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<Owner> dataProvider = new ListDataProvider<Owner>();
	private ArrayList<Owner> list = new ArrayList<Owner>();
	private PerunTable<Owner> table;
	// Selection model
	final MultiSelectionModel<Owner> selectionModel = new MultiSelectionModel<Owner>(new GeneralKeyProvider<Owner>());
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private String type = "";

	private boolean checkable = true;
	private ArrayList<Owner> backupList = new ArrayList<Owner>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" .@");
	private List<String> filterByNames = new ArrayList<>();

	/**
	 * New instance of get owners
	 */
	public GetOwners() {}

	/**
	 * New instance of get owners with external events
	 *
	 * @param events external events
	 */
	public GetOwners(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Return table with owners - starts RPC call
	 *
	 * @return table widget
	 */
	public CellTable<Owner> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Owner>(list);

		// Cell table
		table = new PerunTable<Owner>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Owner> columnSortHandler = new ListHandler<Owner>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Owner> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No owners defined in Perun.");

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}

		table.addIdColumn("Owner Id");

		table.addNameColumn(null);

		// CONTACT COLUMN
		TextColumn<Owner> contactColumn = new TextColumn<Owner>() {
			public String getValue(Owner owner) {
				return String.valueOf(owner.getContact());
			}
		};

		table.addColumn(contactColumn, "Contact");

		contactColumn.setSortable(true);
		columnSortHandler.setComparator(contactColumn, new Comparator<Owner>(){
			public int compare(Owner o1, Owner o2) {
				return o1.getContact().compareToIgnoreCase(o2.getContact());
			}
		});

		// OWNER TYPE COLUMN
		TextColumn<Owner> typeColumn = new TextColumn<Owner>() {
			public String getValue(Owner owner) {
				return Owner.getTranslatedType(owner.getType());
			}
		};

		table.addColumn(typeColumn, "Type");

		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<Owner>(){
			public int compare(Owner o1, Owner o2) {
				return Owner.getTranslatedType(o1.getType()).compareToIgnoreCase(Owner.getTranslatedType(o2.getType()));
			}
		});

		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<Owner>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(Owner object) {
		list.add(object);
		oracle.add(object.getName());
		oracle.add(object.getContact());
		oracle.add(Owner.getTranslatedType(object.getType()));
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(Owner object) {
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
		backupList.clear();
		selectionModel.clear();
		dataProvider.flush();
		dataProvider.refresh();
		oracle.clear();
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
	public ArrayList<Owner> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Owners.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Owners started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		if (type.isEmpty() && filterByNames.isEmpty()) {
			// don't filter
			setList(JsonUtils.<Owner>jsoAsList(jso));
		} else {
			if (!filterByNames.isEmpty() && type.isEmpty()) {
				for (Owner o : JsonUtils.<Owner>jsoAsList(jso)) {
					if (filterByNames.contains(o.getName())) {
						addToTable(o);
					}
				}
			} else if (!filterByNames.isEmpty() && !type.isEmpty()) {
				for (Owner o : JsonUtils.<Owner>jsoAsList(jso)) {
					if (filterByNames.contains(o.getName()) && type.equalsIgnoreCase(o.getType())) {
						addToTable(o);
					}
				}
			} else {
				for (Owner o : JsonUtils.<Owner>jsoAsList(jso)) {
					if (type.equalsIgnoreCase(o.getType())) {
						addToTable(o);
					}
				}
			}
		}
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Owners loaded: " + list.size());
		events.onFinished(jso);

	}

	public void insertToTable(int index, Owner object) {
		list.add(index, object);
		oracle.add(object.getName());
		oracle.add(object.getContact());
		oracle.add(Owner.getTranslatedType(object.getType()));
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<Owner> list) {
		clearTable();
		this.list.addAll(list);
		for (Owner o : list) {
			oracle.add(o.getName());
			oracle.add(o.getContact());
			oracle.add(Owner.getTranslatedType(o.getType()));
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Owner> getList() {
		return this.list;
	}

	/**
	 * Set filtering of received owners by their type
	 *  - administrative
	 *  - technical
	 *  - empty string to disable filter
	 *
	 * @param type
	 */
	public void setFilterByType(String type) {
		this.type = type;
	}

	@Override
	public void filterTable(String filter) {

		// store list only for first time
		if (backupList.isEmpty() || backupList == null) {
			backupList.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (filter.equalsIgnoreCase("")) {
			list.addAll(backupList);
		} else {
			for (Owner o: backupList){
				// store owner by filter
				if (o.getName().toLowerCase().contains(filter.toLowerCase()) ||
						o.getContact().toLowerCase().contains(filter.toLowerCase()) ||
						Owner.getTranslatedType(o.getType()).toLowerCase().startsWith(Owner.getTranslatedType(filter).toLowerCase())) {
					list.add(o);
						}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No owner matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("No owners defined in Perun.");
		}

		loaderImage.loadingFinished();
		dataProvider.flush();
		dataProvider.refresh();

	}

	@Override
	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	@Override
	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	public List<String> getFilterByNames() {
		return filterByNames;
	}

	public void setFilterByNames(List<String> filterByNames) {
		this.filterByNames = filterByNames;
	}

}
