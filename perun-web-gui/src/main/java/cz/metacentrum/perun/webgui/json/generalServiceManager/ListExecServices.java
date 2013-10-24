package cz.metacentrum.perun.webgui.json.generalServiceManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
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
import cz.metacentrum.perun.webgui.model.ExecService;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get exec services for specified or all services
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class ListExecServices implements JsonCallback, JsonCallbackTable<ExecService>
{
	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// service ID
	private int serviceId = 0;
	// JSON URL
	private static final String JSON_URL = "generalServiceManager/listExecServices";
	// Selection model for the table
	final MultiSelectionModel<ExecService> selectionModel = new MultiSelectionModel<ExecService>(new GeneralKeyProvider<ExecService>());
	// Table data provider.
	private ListDataProvider<ExecService> dataProvider = new ListDataProvider<ExecService>();
	// Cell table
	private PerunTable<ExecService> table;
	// List of exec services
	private ArrayList<ExecService> list = new ArrayList<ExecService>();
	// Table field updater
	private FieldUpdater<ExecService, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * Creates a new callback
	 * 
	 * @param id ID of service
	 */
	public ListExecServices(int id) {
		this.serviceId = id;
	}

	/**
	 * Creates a new callback
	 *
	 * @param id ID of service
	 * @param events external events
	 */
	public ListExecServices(int id, JsonCallbackEvents events) {
		this.serviceId = id;
		this.events = events;
	}

	/**
	 * Returns the table with exec services
	 * 
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<ExecService> getTable(FieldUpdater<ExecService, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with exec services
	 * 
	 * @return CellTable widget
	 */
	public CellTable<ExecService> getTable() {	

		// retrieves data
		retrieveData();
				
		// Table data provider.
		dataProvider = new ListDataProvider<ExecService>(list);
		
		// Cell table
		table = new PerunTable<ExecService>(list);
		
		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<ExecService> columnSortHandler = new ListHandler<ExecService>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);
		
		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<ExecService> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		
		if(this.checkable)
		{
			// checkbox column column
			table.addCheckBoxColumn();
		}
		
		//add id column
		table.addIdColumn("ExecService ID", tableFieldUpdater);

		// TODO - some columns will have editable values

		// Create service name column.
		Column<ExecService, String> serviceNameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<ExecService, String>() {
			public String getValue(ExecService object) {
				return object.getService().getName();
			}
		},this.tableFieldUpdater);

		// Create type column
		Column<ExecService, String> typeColumn = JsonUtils.addColumn(new JsonUtils.GetValue<ExecService, String>() {
			public String getValue(ExecService object) {
				return object.getType();
			}
		},this.tableFieldUpdater);

		// Create enabled column
		Column<ExecService, String> enabledColumn = JsonUtils.addColumn(new JsonUtils.GetValue<ExecService, String>() {
			public String getValue(ExecService object) {
				// translate hack
				if (object.isEnabled() == true) { return "Enabled"; }
				else { return "Disabled"; }
			}
		},this.tableFieldUpdater);

		// Create script path column
		Column<ExecService, String> scriptPathColumn = JsonUtils.addColumn(new JsonUtils.GetValue<ExecService, String>() {
			public String getValue(ExecService object) {
				return String.valueOf(object.getScriptPath());
			}
		},this.tableFieldUpdater);

		// Create delay column
		Column<ExecService, String> delayColumn = JsonUtils.addColumn(new JsonUtils.GetValue<ExecService, String>() {
			public String getValue(ExecService object) {
				return String.valueOf(object.getDefaultDelay());
			}
		},this.tableFieldUpdater);

		serviceNameColumn.setSortable(true);
		columnSortHandler.setComparator(serviceNameColumn, new Comparator<ExecService>() {
			public int compare(ExecService o1, ExecService o2) {
				return o1.getService().getName().compareToIgnoreCase(o2.getService().getName());
			}
		});

		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<ExecService>() {
			public int compare(ExecService o1, ExecService o2) {
				return o1.getType().compareToIgnoreCase(o2.getType());
			}
		});

		enabledColumn.setSortable(true);
		columnSortHandler.setComparator(enabledColumn, new Comparator<ExecService>() {
			public int compare(ExecService o1, ExecService o2) {
				return String.valueOf(o1.getStatus()).compareToIgnoreCase(String.valueOf(o2.getStatus()));
			}
		});

		scriptPathColumn.setSortable(true);
		columnSortHandler.setComparator(scriptPathColumn, new Comparator<ExecService>() {
			public int compare(ExecService o1, ExecService o2) {
				return o1.getScriptPath().compareToIgnoreCase(o2.getScriptPath());
			}
		});

		delayColumn.setSortable(true);
		columnSortHandler.setComparator(delayColumn, new Comparator<ExecService>() {
			public int compare(ExecService o1, ExecService o2) {
				return o1.getDefaultDelay() - o2.getDefaultDelay();
			}
		});

	
		// updates the columns size
		table.setColumnWidth(serviceNameColumn, 250.0, Unit.PX);
		table.setColumnWidth(typeColumn, 100.0, Unit.PX);
		table.setColumnWidth(scriptPathColumn, 100.0, Unit.PX);
		table.setColumnWidth(enabledColumn, 100.0, Unit.PX);

		// Add the columns.
		table.addColumn(serviceNameColumn, "Service name");
		table.addColumn(typeColumn, "Type");
		table.addColumn(enabledColumn, "Enabled");
		table.addColumn(scriptPathColumn, "Script path");
		table.addColumn(delayColumn, "Default delay");

		return table;

	}

	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData()
	{
		final String param = "service=" + this.serviceId;
		JsonClient js = new JsonClient();
		if (serviceId != 0) {
			js.retrieveData(JSON_URL, param, this);
		} else {
			js.retrieveData(JSON_URL, this);
		}
	}

    /**
     * Sorts table by objects date
     */
    public void sortTable() {
        list = new TableSorter<ExecService>().sortById(getList());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Add object as new row to table
     *
     * @param object Resource to be added as new row
     */
    public void addToTable(ExecService object) {
        list.add(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object Resource to be removed as row
     */
    public void removeFromTable(ExecService object) {
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
    public ArrayList<ExecService> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading ExecServices.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading ExecServices started.");
        events.onLoadingStart();
    }

    /**
     * Called when loading successfully finishes.
     */
    public void onFinished(JavaScriptObject jso) {
        setList(JsonUtils.<ExecService>jsoAsList(jso));
        sortTable();
        session.getUiElements().setLogText("ExecServices loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();

    }

    public void insertToTable(int index, ExecService object) {
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

    public void setList(ArrayList<ExecService> list) {
        clearTable();
        this.list.addAll(list);
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<ExecService> getList() {
        return this.list;
    }

}