package cz.metacentrum.perun.webgui.json.propagationStatsReader;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.ServiceState;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all RichTasks for selected facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetFacilityServicesState implements JsonCallback, JsonCallbackTable<ServiceState> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "propagationStatsReader/getFacilityServicesState";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<ServiceState> dataProvider = new ListDataProvider<ServiceState>();
	private ArrayList<ServiceState> list = new ArrayList<ServiceState>();
	private PerunTable<ServiceState> table;
	// Selection model
	final MultiSelectionModel<ServiceState> selectionModel = new MultiSelectionModel<ServiceState>(new ServiceStateKeyProvider());
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// entities
	private int facilityId = 0;
	private FieldUpdater<ServiceState, String> tableFieldUpdater;
	private ArrayList<ServiceState> backupList = new ArrayList<ServiceState>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private boolean checkable = true; // default is checkable

	/**
	 * New instance of callback
	 *
	 * @param facilityId facility ID
	 */
	public GetFacilityServicesState(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * New instance of callback with external events
	 *
	 * @param facilityId facility ID
	 * @param events external events
	 */
	public GetFacilityServicesState(int facilityId, JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.events = events;
	}

	/**
	 * Return table with ServiceStates - starts RPC call
	 *
	 * @return table
	 */
	public CellTable<ServiceState> getTable() {
		retrieveData();
		return getEmptyTable();
	}

	/**
	 * Return table with ServiceStates and custom field updater
	 *
	 * @return table widget
	 */
	public CellTable<ServiceState> getTable(FieldUpdater<ServiceState, String> tfu) {
		this.tableFieldUpdater=tfu;
		retrieveData();
		return getEmptyTable();
	}

	/**
	 * Return table with ServiceStates
	 *
	 * @return table widget
	 */
	public CellTable<ServiceState> getEmptyTable() {

		// Table data provider.
		dataProvider = new ListDataProvider<ServiceState>(list);

		// Cell table
		table = new PerunTable<ServiceState>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<ServiceState> columnSortHandler = new ListHandler<ServiceState>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<ServiceState> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No service configuration was propagated to this facility.");

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}

		table.addIdColumn("ID", tableFieldUpdater);

		// Service column
		Column<ServiceState, String> serviceColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<ServiceState, String>() {
					public String getValue(ServiceState ServiceState) {
						return String.valueOf(ServiceState.getService().getName());
					}
				}, tableFieldUpdater);

		serviceColumn.setSortable(true);
		columnSortHandler.setComparator(serviceColumn, new Comparator<ServiceState>() {
			@Override
			public int compare(ServiceState o1, ServiceState o2) {
				return o1.getService().getName().compareTo(o2.getService().getName());
			}
		});

		// status column
		Column<ServiceState, String> statusColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<ServiceState, String>() {
					public String getValue(ServiceState serviceState) {
						String appendix = "";
						if (JsonUtils.isExtendedInfoVisible()) {
							appendix = " ("+serviceState.getLastScheduled().toLowerCase()+")";
						}
						if (serviceState.getLastScheduled().equals("GENERATE") && serviceState.getStatus().equals("PROCESSING")) {
							return "PLANNED"+appendix;
						} else if (serviceState.getLastScheduled().equals("GENERATE") && serviceState.getStatus().equals("DONE")) {
							return "PROCESSING"+appendix;
						} else if (serviceState.getLastScheduled().equals("SEND") && serviceState.getStatus().equals("PROCESSING")) {
							return "PROCESSING"+appendix;
						} else {
							return String.valueOf(serviceState.getStatus())+appendix;
						}
					}
				}, tableFieldUpdater);

		statusColumn.setSortable(true);
		columnSortHandler.setComparator(statusColumn, new Comparator<ServiceState>() {
			@Override
			public int compare(ServiceState o1, ServiceState o2) {
				return o1.getStatus().compareTo(o2.getStatus());
			}
		});

		// status column
		Column<ServiceState, String> blockedColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<ServiceState, String>() {
					public String getValue(ServiceState serviceState) {
						if (serviceState.isBlockedOnFacility()) {
							return "BLOCKED";
						} else if (serviceState.isBlockedGlobally()) {
							return "BLOCKED GLOBALLY";
						} else {
							return "ALLOWED";
						}
					}
				}, tableFieldUpdater);

		blockedColumn.setSortable(true);
		columnSortHandler.setComparator(blockedColumn, new Comparator<ServiceState>() {
			@Override
			public int compare(ServiceState o1, ServiceState o2) {
				String val1 = (o1.isBlockedOnFacility()) ? "BLOCKED" : ((o1.isBlockedGlobally()) ? "BLOCKED GLOBALLY" : "ALLOWED");
				String val2 = (o2.isBlockedOnFacility()) ? "BLOCKED" : ((o2.isBlockedGlobally()) ? "BLOCKED GLOBALLY" : "ALLOWED");
				return val1.compareTo(val2);
			}
		});

		// start COLUMN
		TextColumn<ServiceState> startTimeColumn = new TextColumn<ServiceState>() {
			public String getValue(ServiceState result) {
				return result.getStartTime();
			}
		};
		startTimeColumn.setSortable(true);
		columnSortHandler.setComparator(startTimeColumn, new Comparator<ServiceState>() {
			@Override
			public int compare(ServiceState o1, ServiceState o2) {
				return o1.getStartTime().compareTo(o2.getStartTime());
			}
		});

		// end COLUMN
		TextColumn<ServiceState> endTimeColumn = new TextColumn<ServiceState>() {
			public String getValue(ServiceState result) {
				return result.getEndTime();
			}
		};
		endTimeColumn.setSortable(true);
		columnSortHandler.setComparator(endTimeColumn, new Comparator<ServiceState>() {
			@Override
			public int compare(ServiceState o1, ServiceState o2) {
				return o1.getEndTime().compareTo(o2.getEndTime());
			}
		});

		TextColumn<ServiceState> destColumn = new TextColumn<ServiceState>() {
			public String getValue(ServiceState result) {
				return result.getHasDestinations();
			}
		};

		// schedule COLUMN
		TextColumn<ServiceState> scheduleColumn = new TextColumn<ServiceState>() {
			public String getValue(ServiceState result) {
				return result.getSchedule();
			}
		};

		// Add the columns.
		table.addColumn(serviceColumn, "Service");
		table.addColumn(statusColumn, "Status");
		table.addColumn(blockedColumn, "Blocked");
		table.addColumn(startTimeColumn, "Started");
		table.addColumn(endTimeColumn, "Ended");

		// set row styles based on ServiceState state
		table.setRowStyles(new RowStyles<ServiceState>(){
			public String getStyleNames(ServiceState row, int rowIndex) {

				if (row.getStatus().equalsIgnoreCase("NONE")) {
					return "";
				}
				else if (row.getStatus().equalsIgnoreCase("DONE") && row.getLastScheduled().equals("SEND")){
					return "rowgreen";
				}
				else if (row.getStatus().equalsIgnoreCase("DONE") && row.getLastScheduled().equals("GENERATE")){
					return "rowyellow";
				}
				else if (row.getStatus().equalsIgnoreCase("PROCESSING") && row.getLastScheduled().equals("SEND")){
					return "rowyellow";
				}
				else if (row.getStatus().equalsIgnoreCase("PROCESSING")){
					return "rowlightyellow";
				}
				else if (row.getStatus().equalsIgnoreCase("ERROR")){
					return "rowred";
				}
		return "";

			}
		});

		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "facility="+facilityId,this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<ServiceState>().sortByService(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(ServiceState object) {
		list.add(object);
		oracle.add(object.getService().getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(ServiceState object) {
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
	public ArrayList<ServiceState> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading ServiceStates.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading ServiceStates started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<ServiceState>jsoAsList(jso));
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("ServiceStates loaded: " + list.size());
		events.onFinished(jso);

	}

	public void insertToTable(int index, ServiceState object) {
		list.add(index, object);
		oracle.add(object.getService().getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<ServiceState> list) {
		clearTable();
		this.list.addAll(list);
		for (ServiceState object : list) {
			oracle.add(object.getService().getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<ServiceState> getList() {
		return this.list;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

	public void filterTable(String filter){

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
			for (ServiceState tsk : backupList){
				if (tsk.getService().getName().toLowerCase().startsWith(filter.toLowerCase())) {
					list.add(tsk);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No service propagation results matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("No service configuration was propagated to this facility.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	public MultiSelectionModel<ServiceState> getSelectionModel() {
		return this.selectionModel;
	}

	public class ServiceStateKeyProvider implements ProvidesKey<ServiceState> {

		public Object getKey(ServiceState object) {
			// returns ID
			return object.getService().getId();
		}

	}

}
