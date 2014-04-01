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
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.model.Task;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get RichTasks for service
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
@Deprecated
public class GetServiceRichTasks implements JsonCallback, JsonCallbackTable<Task> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "propagationStatsReader/getServiceRichTasks";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<Task> dataProvider = new ListDataProvider<Task>();
	private ArrayList<Task> list = new ArrayList<Task>();
	private PerunTable<Task> table;
	// Selection model
	final MultiSelectionModel<Task> selectionModel = new MultiSelectionModel<Task>(new GeneralKeyProvider<Task>());

	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// entities
	private int facilityId = 0;
	private Service service = null;
	private FieldUpdater<Task, String> tableFieldUpdater;

	/**
	 * New instance of get tasks results
	 *
	 * @param facilityId Facility ID
	 */
	public GetServiceRichTasks(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * New instance of get tasks results with external events
	 *
	 * @param facilityId Facility ID
	 * @param events external events
	 */
	public GetServiceRichTasks(int facilityId, JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.events = events;
	}

	/**
	 * Return table with tasks - starts RPC call
	 *
	 * @return table
	 */
	public CellTable<Task> getTable() {
		retrieveData();
		return getEmptyTable();
	}

	/**
	 * Return table with tasks and custom field updater
	 *
	 * @return table widget
	 */
	public CellTable<Task> getEmptyTable(FieldUpdater<Task, String> tfu) {

		this.tableFieldUpdater=tfu;
		return getEmptyTable();

	}

	/**
	 * Return table with tasks
	 *
	 * @return table widget
	 */
	public CellTable<Task> getEmptyTable() {

		// Table data provider.
		dataProvider = new ListDataProvider<Task>(list);

		// Cell table
		table = new PerunTable<Task>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Task> columnSortHandler = new ListHandler<Task>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Task> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// checkbox column column
		table.addCheckBoxColumn();

		table.addIdColumn("Task Id", tableFieldUpdater);

		// Service column
		Column<Task, String> serviceColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Task, String>() {
					public String getValue(Task task) {
						return String.valueOf(task.getExecService().getService().getName());
					}
				}, tableFieldUpdater);

		// Service Type column
		Column<Task, String> serviceTypeColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Task, String>() {
					public String getValue(Task task) {
						return String.valueOf(task.getExecService().getType());
					}
				}, tableFieldUpdater);


		// status column
		Column<Task, String> statusColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Task, String>() {
					public String getValue(Task task) {
						return String.valueOf(task.getStatus());
					}
				}, tableFieldUpdater);

		// start COLUMN
		TextColumn<Task> startTimeColumn = new TextColumn<Task>() {
			public String getValue(Task result) {
				return result.getStartTime();
			}
		};

		// end COLUMN
		TextColumn<Task> endTimeColumn = new TextColumn<Task>() {
			public String getValue(Task result) {
				return result.getEndTime();
			}
		};

		// schedule COLUMN
		TextColumn<Task> scheduleColumn = new TextColumn<Task>() {
			public String getValue(Task result) {
				return result.getSchedule();
			}
		};

		// Add the columns.
		table.addColumn(serviceColumn, "Service");
		table.addColumn(serviceTypeColumn, "Type");
		table.addColumn(statusColumn, "Status");
		table.addColumn(scheduleColumn, "Scheduled");
		table.addColumn(startTimeColumn, "Started");
		table.addColumn(endTimeColumn, "Ended");

		// set row styles based on task state
		table.setRowStyles(new RowStyles<Task>(){
			public String getStyleNames(Task row, int rowIndex) {

				if (row.getStatus().equalsIgnoreCase("NONE")) {
					return "rowdarkgreen";
				}
				else if (row.getStatus().equalsIgnoreCase("DONE")){
					return "rowgreen";
				}
				else if (row.getStatus().equalsIgnoreCase("PROCESSING")){
					return "rowyellow";
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
		js.retrieveData(JSON_URL, "facility="+facilityId+"&service="+service.getId(),this);
	}

	/**
	 * Sorts table by objects service name
	 */
	public void sortTable() {
		list = new TableSorter<Task>().sortByService(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(Task object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(Task object) {
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
	public ArrayList<Task> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Tasks.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Tasks started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Task>jsoAsList(jso));
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Tasks loaded: " + list.size());
		events.onFinished(jso);

	}

	public void insertToTable(int index, Task object) {
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

	public void setList(ArrayList<Task> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Task> getList() {
		return this.list;
	}

	/**
	 * Set service for callback
	 *
	 * @param serv service
	 */
	public void setService(Service serv){
		this.service = serv;
	}

}
