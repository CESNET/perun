package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
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
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.ServicesPackage;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all services packages
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetServicesPackages implements JsonCallback, JsonCallbackTable<ServicesPackage>, JsonCallbackOracle<ServicesPackage> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url for ServicePackages
	private final String JSON_URL = "servicesManager/getServicesPackages";
	// Data provider and tables
	private ListDataProvider<ServicesPackage> dataProvider = new ListDataProvider<ServicesPackage>();
	private PerunTable<ServicesPackage> table;
	private ArrayList<ServicesPackage> list = new ArrayList<ServicesPackage>();
	// Selection model
	final MultiSelectionModel<ServicesPackage> selectionModel = new MultiSelectionModel<ServicesPackage>(new GeneralKeyProvider<ServicesPackage>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<ServicesPackage, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private ArrayList<ServicesPackage> backupList = new ArrayList<ServicesPackage>();
	private boolean checkable = true;
	private boolean editable = false;

	/**
	 * Creates a new ajax query
	 */
	public GetServicesPackages() {}

	/**
	 * Creates a new ajax query with custom events
	 *
	 * @param events external events for this query
	 */
	public GetServicesPackages(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table of services with custom onClick
	 *
	 * @param fu field updater
	 * @return table widget
	 */
	public CellTable<ServicesPackage> getTable(FieldUpdater<ServicesPackage, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns empty table of ServicePackages with custom onClick
	 *
	 * @param fu field updater
	 * @return empty table widget
	 */
	public CellTable<ServicesPackage> getEmptyTable(FieldUpdater<ServicesPackage, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	/**
	 * Returns table of ServicePackages
	 *
	 * @return table widget
	 */
	public CellTable<ServicesPackage> getTable(){

		retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns empty table of ServicePackages
	 *
	 * @return empty table widget
	 */
	public CellTable<ServicesPackage> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<ServicesPackage>(list);

		// Cell table
		table = new PerunTable<ServicesPackage>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<ServicesPackage> columnSortHandler = new ListHandler<ServicesPackage>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<ServicesPackage> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("There are no services packages.");

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}

		table.addIdColumn("ServicesPackage Id", tableFieldUpdater, 110);

		if (!editable) {

			table.addNameColumn(tableFieldUpdater);

			table.addDescriptionColumn(tableFieldUpdater);

		} else {

			// NAME COLUMN
			final Column<ServicesPackage, String> nameColumn = new Column<ServicesPackage, String>(new TextInputCell()) {
				public String getValue(ServicesPackage object) {
					return object.getName();
				}
			};

			nameColumn.setFieldUpdater(new FieldUpdater<ServicesPackage, String>() {
				@Override
				public void update(int i, final ServicesPackage object, final String s) {
					object.setName(s.trim());
					selectionModel.setSelected(object, true);
				}
			});

			nameColumn.setSortable(true);

			columnSortHandler.setComparator(nameColumn, new Comparator<ServicesPackage>() {
				public int compare(ServicesPackage o1, ServicesPackage o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			// DESCRIPTION COLUMN
			final Column<ServicesPackage, String> descriptionColumn = new Column<ServicesPackage, String>(new TextInputCell()) {
				public String getValue(ServicesPackage object) {
					return object.getDescription();
				}
			};

			descriptionColumn.setFieldUpdater(new FieldUpdater<ServicesPackage, String>() {
				@Override
				public void update(int i, final ServicesPackage object, final String s) {
					object.setDescription(s.trim());
					selectionModel.setSelected(object, true);
				}
			});

			descriptionColumn.setSortable(true);

			columnSortHandler.setComparator(descriptionColumn, new Comparator<ServicesPackage>() {
				public int compare(ServicesPackage o1, ServicesPackage o2) {
					return o1.getDescription().compareTo(o2.getDescription());
				}
			});

			// Link COLUMN
			final Column<ServicesPackage, String> linkColumn = new Column<ServicesPackage, String>(new CustomClickableTextCell()) {
				public String getValue(ServicesPackage object) {
					return "View services in package";
				}
			};
			linkColumn.setFieldUpdater(tableFieldUpdater);

			table.addColumn(nameColumn, "Name");
			table.setColumnWidth(nameColumn, "250px");
			table.addColumn(descriptionColumn, "Description");
			table.setColumnWidth(descriptionColumn, "250px");
			table.addColumn(linkColumn, "Manage services");

		}

		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData(){
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<ServicesPackage>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object ServicesPackage to be added as new row
	 */
	public void addToTable(ServicesPackage object) {
		oracle.add(object.getName());
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object ServicesPackage to be removed as row
	 */
	public void removeFromTable(ServicesPackage object) {
		list.remove(object);
		selectionModel.getSelectedSet().remove(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Clear all table content
	 */
	public void clearTable(){
		oracle.clear();
		backupList.clear();
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
	public ArrayList<ServicesPackage> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading ServicePackages.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading ServicePackages started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<ServicesPackage>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("ServicePackages loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, ServicesPackage object) {
		list.add(index, object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<ServicesPackage> list) {
		clearTable();
		this.list.addAll(list);
		for (ServicesPackage s : list) {
			oracle.add(s.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<ServicesPackage> getList() {
		return this.list;
	}

	/**
	 * Sets external events for callback after it's creation
	 *
	 * @param externalEvent external events
	 */
	public void setEvents(JsonCallbackEvents externalEvent) {
		events = externalEvent;
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
			for (ServicesPackage s : backupList){
				// store facility by filter
				if (s.getName().toLowerCase().startsWith(filter.toLowerCase())) {
					list.add(s);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No services package matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("There are no services packages.");
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

	public MultiSelectionModel<ServicesPackage> getSelectionModel() {
		return this.selectionModel;
	}

}
