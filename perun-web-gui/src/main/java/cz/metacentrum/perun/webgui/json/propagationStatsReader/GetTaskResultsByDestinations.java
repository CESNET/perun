package cz.metacentrum.perun.webgui.json.propagationStatsReader;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
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

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to ger RichTaskResults by Destinations
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetTaskResultsByDestinations implements JsonCallback, JsonCallbackTable<TaskResult> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "propagationStatsReader/getTaskResultsForDestinations";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<TaskResult> dataProvider = new ListDataProvider<TaskResult>();
	private ArrayList<TaskResult> list = new ArrayList<TaskResult>();
	private PerunTable<TaskResult> table;
	// Selection model
	final MultiSelectionModel<TaskResult> selectionModel = new MultiSelectionModel<TaskResult>(new GeneralKeyProvider<TaskResult>());
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private ArrayList<String> destinations;


	/**
	 * New instance of get tasks results
	 *
	 * @param destinations
	 */
	public GetTaskResultsByDestinations(ArrayList<String> destinations) {
		this.destinations = destinations;
	}

	/**
	 * New instance of get tasks results with external events
	 *
	 * @param destinations
	 * @param events external events
	 */
	public GetTaskResultsByDestinations(ArrayList<String> destinations, JsonCallbackEvents events) {
		this.destinations = destinations;
		this.events = events;
	}

	/**
	 * Return table with task results and start callback
	 *
	 * @return table
	 */
	public CellTable<TaskResult> getTable() {

		retrieveData();
		return getTableDefinition();

	}

	/**
	 * Return table definition
	 *
	 * @return table widget
	 */
	public CellTable<TaskResult> getTableDefinition() {

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
		loaderImage.setEmptyResultMessage("No propagation results found for this destination.");

		table.addIdColumn("Result Id", null, 85);

		// destination column
		TextColumn<TaskResult> destinationColumn = new TextColumn<TaskResult>() {
			@Override
			public String getValue(TaskResult object) {
				return String.valueOf(object.getDestination().getDestination());
			}
		};

		destinationColumn.setSortable(true);
		columnSortHandler.setComparator(destinationColumn, new Comparator<TaskResult>(){
			public int compare(TaskResult o1, TaskResult o2) {
				return o1.getDestination().getDestination().compareToIgnoreCase((o2.getDestination().getDestination()));
			}
		});

		// Type column
		TextColumn<TaskResult> typeColumn = new TextColumn<TaskResult>() {
			@Override
			public String getValue(TaskResult object) {
				return String.valueOf(object.getDestination().getType());
			}
		};

		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<TaskResult>(){
			public int compare(TaskResult o1, TaskResult o2) {
				return o1.getDestination().getType().compareToIgnoreCase(o2.getDestination().getType());
			}
		});

		TextColumn<TaskResult> servColumn = new TextColumn<TaskResult>() {
			@Override
			public String getValue(TaskResult taskResult) {
				return taskResult.getService().getName();
			}
		};
		servColumn.setSortable(true);
		columnSortHandler.setComparator(servColumn, new Comparator<TaskResult>() {
			public int compare(TaskResult o1, TaskResult o2) {
				return o1.getService().getName().compareToIgnoreCase(o2.getService().getName());
			}
		});

		// status column
		TextColumn<TaskResult> statusColumn = new TextColumn<TaskResult>() {
			@Override
			public String getValue(TaskResult object) {
				return String.valueOf(object.getStatus());
			}
		};

		statusColumn.setSortable(true);
		columnSortHandler.setComparator(statusColumn, new Comparator<TaskResult>(){
			public int compare(TaskResult o1, TaskResult o2) {
				return o1.getStatus().compareToIgnoreCase(o2.getStatus());
			}
		});

		// time column
		TextColumn<TaskResult> timeColumn = new TextColumn<TaskResult>() {
			@Override
			public String getValue(TaskResult object) {
				return object.getTimestamp();
			}
		};

		timeColumn.setSortable(true);
		columnSortHandler.setComparator(timeColumn, new Comparator<TaskResult>(){
			public int compare(TaskResult o1, TaskResult o2) {
				return o1.getTimestamp().compareToIgnoreCase(o2.getTimestamp());
			}
		});

		// returnCode column
		TextColumn<TaskResult> returnCodeColumn = new TextColumn<TaskResult>() {
			@Override
			public String getValue(TaskResult object) {
				return String.valueOf(object.getReturnCode());
			}
		};

		// standardMessageCode column
		TextColumn<TaskResult> standardMessageColumn = new TextColumn<TaskResult>() {
			@Override
			public void render(Context context, TaskResult object, SafeHtmlBuilder sb) {
				if (object != null) {
					sb.appendEscapedLines(object.getStandardMessage());
				}
			}
			@Override
			public String getValue(TaskResult object) {
				return String.valueOf(object.getStandardMessage());
			}
		};

		// errorMessageCode column
		TextColumn<TaskResult> errorMessageColumn = new TextColumn<TaskResult>() {
			@Override
			public void render(Context context, TaskResult object, SafeHtmlBuilder sb) {
				if (object != null) {
					sb.appendEscapedLines(object.getErrorMessage());
				}
			}
			@Override
			public String getValue(TaskResult object) {
				return String.valueOf(object.getErrorMessage());
			}
		};

		// Add the other columns.
		//table.addColumn(destinationColumn, "Destination");
		table.addColumn(typeColumn, "Type");
		table.addColumn(servColumn, "Service");
		table.addColumn(statusColumn, "Status");
		table.addColumn(timeColumn, "Time");
		table.addColumn(returnCodeColumn, "Return code");
		//table.addColumn(standardMessageColumn, "Standard Message");
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

		String params = "";
		// serialize parameters
		if (destinations != null && !destinations.isEmpty()) {
			// parse lists
			for (int i=0; i<destinations.size(); i++) {
				if (i != destinations.size()-1) {
					params += "destinations[]=" + destinations.get(i) + "&";
				} else {
					params += "destinations[]=" + destinations.get(i);
				}
			}
		}

		js.retrieveData(JSON_URL, params, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<TaskResult>().sortByRichTaskResultService(getList());
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
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<TaskResult> getList() {
		return this.list;
	}

}
