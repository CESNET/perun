package cz.metacentrum.perun.webgui.json.facilitiesManager;

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
import cz.metacentrum.perun.webgui.model.Host;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get hosts from facility (cluster)
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: b2703d8a44931c4844f237ae91048ffb55fe901a $
 */
public class GetHosts implements JsonCallback, JsonCallbackTable<Host> {
	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "facilitiesManager/getHosts";
	// Data provider.
	private ListDataProvider<Host> dataProvider = new ListDataProvider<Host>();
	// The table itself
	private PerunTable<Host> table;
	// Facilities list
	private ArrayList<Host> list = new ArrayList<Host>();
	// Table field updater
	private FieldUpdater<Host, String> tableFieldUpdater;
	
	// Selection model
	final MultiSelectionModel<Host> selectionModel = new MultiSelectionModel<Host>(
			new GeneralKeyProvider<Host>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int facilityId = 0;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	/**
	 * Creates a new instance of the request
	 * 
	 * @param facilityId ID of facility to get hosts for
	 */
	public GetHosts(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * Creates a new instance of the request with custom events
	 *
	 * @param facilityId ID of facility to get hosts for
	 * @param events external events
	 */
	public GetHosts(int facilityId, JsonCallbackEvents events) {
		this.events = events;
		this.facilityId = facilityId;
	}

	/**
	 * Get the cell table with Hosts and custom onclick method
	 * 
	 * @param fu custom field updater
	 * @return CellTable with all host on facility
	 */
	public CellTable<Host> getTable(FieldUpdater<Host, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Gets table with all hosts on facility
	 * 
	 * @return CellTable with all hosts on facility
	 */
	public CellTable<Host> getTable() {

		// retrieve data
		retrieveData();
		
		// Table data provider.
		dataProvider = new ListDataProvider<Host>(list);

		// Cell table
		table = new PerunTable<Host>(list);
		
		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

        loaderImage.setEmptyResultMessage("Facility has no hosts.");

		// Sorting
		ListHandler<Host> columnSortHandler = new ListHandler<Host>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);
		
		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Host> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		
		// columns
		table.addCheckBoxColumn();
		table.addIdColumn("Host ID", tableFieldUpdater);

        // NAME COLUMN
        Column<Host, String> nameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Host, String>() {
            public String getValue(Host host) {
                return host.getName();
            }
        },tableFieldUpdater);

        nameColumn.setSortable(true);
        columnSortHandler.setComparator(nameColumn, new Comparator<Host>() {
            public int compare(Host o1, Host o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        table.addColumn(nameColumn, "Hostname");
		
		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "facility="+facilityId, this);
	}

    /**
     * Sorts table by objects date
     */
    public void sortTable() {
        list = new TableSorter<Host>().sortByHostname(getList());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Add object as new row to table
     *
     * @param object Resource to be added as new row
     */
    public void addToTable(Host object) {
        list.add(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object Resource to be removed as row
     */
    public void removeFromTable(Host object) {
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
    public ArrayList<Host> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading Hosts.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading Hosts started.");
        events.onLoadingStart();
    }

    /**
     * Called when loading successfully finishes.
     */
    public void onFinished(JavaScriptObject jso) {
        setList(JsonUtils.<Host>jsoAsList(jso));
        sortTable();
        loaderImage.loadingFinished();
        session.getUiElements().setLogText("Hosts loaded: " + list.size());
        events.onFinished(jso);

    }

    public void insertToTable(int index, Host object) {
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

    public void setList(ArrayList<Host> list) {
        clearTable();
        this.list.addAll(list);
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<Host> getList() {
        return this.list;
    }

}