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
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get services which are denied on facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetServicesBlockedOnFacility implements JsonCallback, JsonCallbackTable<Service> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// facility ID
	private int facilityId = 0;
	// JSON URL
	private static final String JSON_URL = "generalServiceManager/getServicesBlockedOnFacility";
	// Selection model for the table
	final MultiSelectionModel<Service> selectionModel = new MultiSelectionModel<Service>(new GeneralKeyProvider<Service>());
	// Table data provider.
	private ListDataProvider<Service> dataProvider = new ListDataProvider<Service>();
	// Cell table
	private PerunTable<Service> table;
	// List of services
	private ArrayList<Service> list = new ArrayList<Service>();
	// Table field updater
	private FieldUpdater<Service, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * Creates a new callback
	 *
	 * @param id ID of facility
	 */
	public GetServicesBlockedOnFacility(int id) {
		this.facilityId = id;
	}

	/**
	 * Creates a new callback
	 *
	 * @param id ID of facility
	 * @param events external events
	 */
	public GetServicesBlockedOnFacility(int id, JsonCallbackEvents events) {
		this.facilityId = id;
		this.events = events;
	}

	/**
	 * Returns the table with services
	 *
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<Service> getTable(FieldUpdater<Service, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with services
	 *
	 * @return CellTable widget
	 */
	public CellTable<Service> getTable() {

		// retrieves data
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

		if(this.checkable) {
			// checkbox column column
			table.addCheckBoxColumn();
		}

		//add id column
		table.addIdColumn("Service ID", tableFieldUpdater);

		// Create service name column.
		Column<Service, String> serviceNameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
			public String getValue(Service object) {
				return object.getName();
			}
		},this.tableFieldUpdater);

		// Create enabled column
		Column<Service, String> localEnabledColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
			public String getValue(Service object) {
				// translate hack
				return object.isLocalEnabled();
			}
		},this.tableFieldUpdater);

		// Create enabled column
		Column<Service, String> enabledColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
			public String getValue(Service object) {
				// translate hack
				if (object.isEnabled()) { return "Enabled"; }
				else { return "Disabled"; }
			}
		},this.tableFieldUpdater);

		// Create script path column
		Column<Service, String> scriptPathColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
			public String getValue(Service object) {
				return String.valueOf(object.getScriptPath());
			}
		},this.tableFieldUpdater);

		// Create delay column
		Column<Service, String> delayColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Service, String>() {
			public String getValue(Service object) {
				return String.valueOf(object.getDelay());
			}
		},this.tableFieldUpdater);

		serviceNameColumn.setSortable(true);
		columnSortHandler.setComparator(serviceNameColumn, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		enabledColumn.setSortable(true);
		columnSortHandler.setComparator(enabledColumn, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return String.valueOf(o1.isEnabled()).compareToIgnoreCase(String.valueOf(o2.isEnabled()));
			}
		});

		localEnabledColumn.setSortable(true);
		columnSortHandler.setComparator(localEnabledColumn, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return o1.isLocalEnabled().compareToIgnoreCase(o2.isLocalEnabled());
			}
		});

		scriptPathColumn.setSortable(true);
		columnSortHandler.setComparator(scriptPathColumn, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return o1.getScriptPath().compareToIgnoreCase(o2.getScriptPath());
			}
		});

		delayColumn.setSortable(true);
		columnSortHandler.setComparator(delayColumn, new Comparator<Service>() {
			public int compare(Service o1, Service o2) {
				return o1.getDelay() - o2.getDelay();
			}
		});

		// updates the columns size
		table.setColumnWidth(serviceNameColumn, 250.0, Unit.PX);
		table.setColumnWidth(scriptPathColumn, 100.0, Unit.PX);
		table.setColumnWidth(localEnabledColumn, 100.0, Unit.PX);
		table.setColumnWidth(enabledColumn, 100.0, Unit.PX);

		// Add the columns.
		table.addColumn(serviceNameColumn, "Service name");
		table.addColumn(localEnabledColumn, "On Facility");
		table.addColumn(enabledColumn, "Globally");
		table.addColumn(scriptPathColumn, "Script path");
		table.addColumn(delayColumn, "Default delay");

		return table;

	}

	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData()
	{
		final String param = "facility=" + this.facilityId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<Service>().sortById(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(Service object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
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
		session.getUiElements().setLogErrorText("Error while loading Services.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Services started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		for (Service e : JsonUtils.<Service>jsoAsList(jso)){
			e.setLocalEnabled("Disabled");
			addToTable(e);
		}
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Services loaded: " + list.size());
		events.onFinished(jso);

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
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Service> getList() {
		return this.list;
	}

}
