package cz.metacentrum.perun.webgui.json.propagationStatsReader;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
import cz.metacentrum.perun.webgui.model.TaskResult;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to ger RichTaskResults by Task
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetRichTaskResultsByTask implements JsonCallback, JsonCallbackTable<TaskResult>, JsonCallbackOracle<TaskResult> {

	private static final int ERROR_MESSAGE_LIMIT = 150;
	private static final int STANDARD_MESSAGE_LIMIT = 40;

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "propagationStatsReader/getTaskResultsForGUIByTaskOnlyNewest";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<TaskResult> dataProvider = new ListDataProvider<TaskResult>();
	private ArrayList<TaskResult> list = new ArrayList<TaskResult>();
	private PerunTable<TaskResult> table;
	private FieldUpdater<TaskResult, String> fieldUpdater;
	// Selection model
	final MultiSelectionModel<TaskResult> selectionModel = new MultiSelectionModel<TaskResult>(new GeneralKeyProvider<TaskResult>());
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private int taskId = 0;

	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private ArrayList<TaskResult> fullBackup = new ArrayList<TaskResult>();

	/**
	 * New instance of get tasks results
	 */
	public GetRichTaskResultsByTask(int taskId) {
		this.taskId = taskId;
	}

	/**
	 * New instance of get tasks results with external events
	 *
	 * @param events external events
	 */
	public GetRichTaskResultsByTask(int taskId, JsonCallbackEvents events) {
		this.taskId = taskId;
		this.events = events;
	}

	/**
	 * Return table with task results and start callback
	 *
	 * @return table
	 */
	public CellTable<TaskResult> getTable() {

		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Return table with task results and start callback
	 *
	 * @return table
	 */
	public CellTable<TaskResult> getTable(FieldUpdater<TaskResult, String> fieldUpdater) {
		this.fieldUpdater = fieldUpdater;
		retrieveData();
		return getEmptyTable();
	}

	/**
	 * Return table definition
	 *
	 * @return table widget
	 */
	public CellTable<TaskResult> getEmptyTable() {

		// Table data provider.
		dataProvider = new ListDataProvider<TaskResult>(list);

		// Cell table
		table = new PerunTable<TaskResult>(list);
		table.removeRowCountChangeHandler();

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<TaskResult> columnSortHandler = new ListHandler<TaskResult>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<TaskResult> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No propagation results found.");

		table.addIdColumn("Result Id", fieldUpdater, 85);

		// destination column
		Column<TaskResult, String> destinationColumn = JsonUtils.addColumn(object ->
				String.valueOf(object.getDestination().getDestination()), fieldUpdater);

		destinationColumn.setSortable(true);

		columnSortHandler.setComparator(destinationColumn, (o1, o2) ->
				TableSorter.smartCompare(o1.getDestination().getDestination(), o2.getDestination().getDestination()));

		// Type column
		Column<TaskResult, String> typeColumn = JsonUtils.addColumn(object ->
				String.valueOf(object.getDestination().getType().toUpperCase()), fieldUpdater);

		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<TaskResult>(){
			public int compare(TaskResult o1, TaskResult o2) {
				int comp = o1.getDestination().getType().compareToIgnoreCase((o2.getDestination().getType()));
				if (comp == 0) {
					return new Date((long)o2.getTimestampNative()).compareTo(new Date((long)o1.getTimestampNative()));
				}
				return comp;
			}
		});

		Column<TaskResult, String> servColumn = JsonUtils.addColumn(object ->
				String.valueOf(object.getService().getName()), fieldUpdater);

		servColumn.setSortable(true);
		columnSortHandler.setComparator(servColumn, new Comparator<TaskResult>() {
			public int compare(TaskResult o1, TaskResult o2) {
				int comp = o1.getService().getName().compareToIgnoreCase((o2.getService().getName()));
				if (comp == 0) {
					return new Date((long)o2.getTimestampNative()).compareTo(new Date((long)o1.getTimestampNative()));
				}
				return comp;
			}
		});

		// status column
		Column<TaskResult, String> statusColumn = JsonUtils.addColumn(object ->
				String.valueOf(object.getStatus()), fieldUpdater);

		statusColumn.setSortable(true);
		columnSortHandler.setComparator(statusColumn, new Comparator<TaskResult>(){
			public int compare(TaskResult o1, TaskResult o2) {
				int comp = o1.getStatus().compareToIgnoreCase(o2.getStatus());
				if (comp == 0) {
					return new Date((long)o2.getTimestampNative()).compareTo(new Date((long)o1.getTimestampNative()));
				}
				return comp;
			}
		});

		// time column
		Column<TaskResult, String> timeColumn = JsonUtils.addColumn(object ->
				String.valueOf(object.getTimestamp()), fieldUpdater);

		timeColumn.setSortable(true);
		columnSortHandler.setComparator(timeColumn, new Comparator<TaskResult>(){
			public int compare(TaskResult o1, TaskResult o2) {
				return new Date((long)o2.getTimestampNative()).compareTo(new Date((long)o1.getTimestampNative()));
			}
		});

		// returnCode column
		Column<TaskResult, String> returnCodeColumn = JsonUtils.addColumn(object ->
				String.valueOf(object.getReturnCode()), fieldUpdater);

		// standardMessageCode column
		Column<TaskResult, TaskResult> standardMessageColumn = JsonUtils.addCustomCellColumn(new AbstractCell<TaskResult>() {
			@Override
			public void render(Context context, TaskResult taskResult, SafeHtmlBuilder safeHtmlBuilder) {
				if (taskResult != null) {
					if (taskResult.getStandardMessage().length() > STANDARD_MESSAGE_LIMIT) {
						safeHtmlBuilder.appendEscapedLines(taskResult.getStandardMessage().substring(0, STANDARD_MESSAGE_LIMIT) + "…");
					} else {
						safeHtmlBuilder.appendEscapedLines(taskResult.getStandardMessage());
					}
				}
			}
		}, null);

		// errorMessageCode column
		Column<TaskResult, TaskResult> errorMessageColumn = JsonUtils.addCustomCellColumn(new AbstractCell<TaskResult>() {
			@Override
			public void render(Context context, TaskResult object, SafeHtmlBuilder sb) {
				if (object != null) {
					if (object.getErrorMessage().length() > ERROR_MESSAGE_LIMIT) {
						sb.appendEscapedLines(object.getErrorMessage().substring(0, ERROR_MESSAGE_LIMIT) + "…");
					} else {
						sb.appendEscapedLines(object.getErrorMessage());
					}
				}
			}
		}, null);

		// Add the other columns.
		table.addColumn(destinationColumn, "Destination");
		table.addColumn(typeColumn, "Type");
		table.addColumn(servColumn, "Service");
		table.addColumn(statusColumn, "Status");
		table.addColumn(timeColumn, "Time");
		table.addColumn(returnCodeColumn, "Return code");
		table.addColumn(standardMessageColumn, "Standard Message");
		table.addColumn(errorMessageColumn, "Error Message");

		// set row styles based on task state
		table.setRowStyles(new RowStyles<TaskResult>(){
			public String getStyleNames(TaskResult row, int rowIndex) {

				if (row.getStatus().equalsIgnoreCase("DONE")) {
					return "rowgreen";
				}
				else if (row.getStatus().equalsIgnoreCase("DENIED")){
					return "rowyellow";
				}
				else if (row.getStatus().equalsIgnoreCase("FATAL_ERROR")){
					return "rowred";
				}
				else if (row.getStatus().equalsIgnoreCase("ERROR")){
					return "roworange";
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
		js.retrieveData(JSON_URL, "task="+taskId, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<TaskResult>().sortByDestination(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(TaskResult object) {
		list.add(object);
		oracle.add(object.getDestination().getDestination());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(TaskResult object) {
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
		oracle.clear();
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
	public ArrayList<TaskResult> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Task results.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Task results started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<TaskResult>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Task results loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();

	}

	public void insertToTable(int index, TaskResult object) {
		list.add(index, object);
		oracle.add(object.getDestination().getDestination());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		// TODO Auto-generated method stub
	}

	public void setList(ArrayList<TaskResult> list) {
		clearTable();
		this.list.addAll(list);
		for (TaskResult r : list) {
			oracle.add(r.getDestination().getDestination());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<TaskResult> getList() {
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
			for (TaskResult result : fullBackup){
				if (result.getDestination().getDestination().toLowerCase().startsWith(filter)) {
					list.add(result);
				}
			}
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	@Override
	public UnaccentMultiWordSuggestOracle getOracle() {
		return oracle;
	}

	@Override
	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
