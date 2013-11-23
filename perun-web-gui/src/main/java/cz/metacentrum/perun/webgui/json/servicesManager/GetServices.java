package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
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

import java.util.ArrayList;

/**
 * Ajax query to get all services
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 07555b0435c8dd9899c882fa307417a3edffd6ad $
 */

public class GetServices implements JsonCallback, JsonCallbackTable<Service>, JsonCallbackOracle<Service> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url for services
	private final String JSON_URL = "servicesManager/getServices";
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
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
    private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
    private ArrayList<Service> backupList = new ArrayList<Service>();
    private boolean checkable = true;

	/**
	 * Creates a new ajax query
	 */
	public GetServices() {}

	/**
	 * Creates a new ajax query with custom events
     *
	 * @param events external events for this query
	 */
	public GetServices(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table of services with custom onClick
	 * 
	 * @param fu field updater
	 * @return table widget
	 */
	public CellTable<Service> getTable(FieldUpdater<Service, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns empty table of services with custom onClick
	 * 
	 * @param fu field updater
	 * @return empty table widget
	 */
	public CellTable<Service> getEmptyTable(FieldUpdater<Service, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	/**
	 * Returns table of services
	 *
	 * @return table widget
	 */
	public CellTable<Service> getTable(){

		retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns empty table of services
	 *
	 * @return empty table widget
	 */
	public CellTable<Service> getEmptyTable(){

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

		// checkbox column column
		if (checkable) {
            table.addCheckBoxColumn();
        }

		table.addIdColumn("Service Id", tableFieldUpdater, 110);

		table.addNameColumn(tableFieldUpdater);

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
        oracle.add(object.getName());
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
    public ArrayList<Service> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading services.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading services started.");
        events.onLoadingStart();
    }

    /**
     * Called, when operation finishes successfully.
     */
    public void onFinished(JavaScriptObject jso) {
        setList(JsonUtils.<Service>jsoAsList(jso));
        sortTable();
        session.getUiElements().setLogText("Services loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();
    }

    public void insertToTable(int index, Service object) {
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

    public void setList(ArrayList<Service> list) {
        clearTable();
        this.list.addAll(list);
        for (Service s : list) {
            oracle.add(s.getName());
        }
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<Service> getList() {
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
        // always clear selected items
        selectionModel.clear();

        // store list only for first time
        if (backupList.isEmpty() || backupList == null) {
            for (Service s : getList()){
                backupList.add(s);
            }
        }
        getList().clear();
        if (filter.equalsIgnoreCase("")) {
            for (Service s : backupList) {
                list.add(s);
            }
        } else {
            for (Service s : backupList){
                // store facility by filter
                if (s.getName().toLowerCase().startsWith(filter.toLowerCase())) {
                    list.add(s);
                }
            }
            if (getList().isEmpty()) {
                loaderImage.loadingFinished();
            }
        }
        dataProvider.flush();
        dataProvider.refresh();
    }

    @Override
    public MultiWordSuggestOracle getOracle() {
        return this.oracle;
    }

    @Override
    public void setOracle(MultiWordSuggestOracle oracle) {
        this.oracle = oracle;
    }

    public MultiSelectionModel<Service> getSelectionModel() {
        return this.selectionModel;
    }

}