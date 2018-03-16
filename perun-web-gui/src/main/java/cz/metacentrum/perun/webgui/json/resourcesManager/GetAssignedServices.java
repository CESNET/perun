package cz.metacentrum.perun.webgui.json.resourcesManager;

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
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get assigned services for specified resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAssignedServices implements JsonCallback, JsonCallbackTable<Service>, JsonCallbackOracle<Service> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url for services
	private final String JSON_URL = "resourcesManager/getAssignedServices";
	// Data provider and tables
	private ListDataProvider<Service> dataProvider = new ListDataProvider<Service>();
	private PerunTable<Service> table;
	private ArrayList<Service> list = new ArrayList<Service>();
	// Selection model
	final MultiSelectionModel<Service> selectionModel = new MultiSelectionModel<Service>(new GeneralKeyProvider<Service>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Service, String> tableFieldUpdater;
	// resource ID
	private int resourceId;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;
	// oracle
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private ArrayList<Service> fullBackup = new ArrayList<Service>();

	/**
	 * Creates a new ajax query
	 *
	 * @param resourceId ID of resource to get services for
	 */
	public GetAssignedServices(int resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Creates a new ajax query
	 *
	 * @param resourceId ID of resource to get services for
	 * @param events external events for this query
	 */
	public GetAssignedServices(int resourceId, JsonCallbackEvents events) {
		this.events = events;
		this.resourceId = resourceId;
	}

	/**
	 * Returns table with assigned services on resource with custom onClick
	 *
	 * @param fu custom onClick action
	 * @return table widget
	 */
	public CellTable<Service> getTable(FieldUpdater<Service, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with assigned services on resource
	 *
	 * @return table widget
	 */
	public CellTable<Service> getTable(){

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Service>(list);

		// Cell table
		table = new PerunTable<Service>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Service> columnSortHandler = new ListHandler<Service>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Service> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Resource has no services assigned.");

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}

		Column<Service, String> enabledColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service,String>() {
			public String getValue(Service serv) {
				return serv.isEnabled() ? "ENABLED" : "DISABLED";
			}
		}, tableFieldUpdater);

		enabledColumn.setSortable(true);
		columnSortHandler.setComparator(enabledColumn, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return (o1.isEnabled() ? "ENABLED" : "DISABLED").compareToIgnoreCase((o2.isEnabled() ? "ENABLED" : "DISABLED"));
			}
		});

		Column<Service, String> scriptColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service,String>() {
			public String getValue(Service serv) {
				return serv.getScriptPath();
			}
		}, tableFieldUpdater);

		scriptColumn.setSortable(true);
		columnSortHandler.setComparator(scriptColumn, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return o1.getScriptPath().compareToIgnoreCase(o2.getScriptPath());
			}
		});

		table.addIdColumn("Service Id", tableFieldUpdater, 110);

		table.addNameColumn(tableFieldUpdater);
		table.addColumn(enabledColumn, "Enabled");
		table.addColumn(scriptColumn, "Script");
		table.addDescriptionColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData(){
		final String param = "resource=" + this.resourceId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Service>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Service to be added as new row
	 */
	public void addToTable(Service object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Service to be removed as row
	 */
	public void removeFromTable(Service object) {
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
		fullBackup.clear();
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
	public ArrayList<Service> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading assigned services.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading assigned services started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Service>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Assigned services loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Service object) {
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

	public void setList(ArrayList<Service> list) {
		clearTable();
		this.list.addAll(list);
		for (Service object : list) {
			oracle.add(object.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Service> getList() {
		return this.list;
	}

	public UnaccentMultiWordSuggestOracle getOracle(){
		return this.oracle;
	}

	public void filterTable(String text){

		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			fullBackup.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (text.equalsIgnoreCase("")) {
			list.addAll(fullBackup);
		} else {
			for (Service s : fullBackup){
				// store facility by filter
				if (s.getName().toLowerCase().startsWith(text.toLowerCase())) {
					list.add(s);
				}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No service matching '"+text+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("Resource has no services assigned.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
