package cz.metacentrum.perun.webgui.json.tasksManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.FacilityStateKeyProvider;
import cz.metacentrum.perun.webgui.model.FacilityState;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get facility propagation state for 1 or All facilities or Facilities related to some VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetFacilityState implements JsonCallback, JsonCallbackTable<FacilityState>, JsonCallbackOracle<FacilityState> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "tasksManager/getFacilityState";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<FacilityState> dataProvider = new ListDataProvider<FacilityState>();
	private ArrayList<FacilityState> list = new ArrayList<FacilityState>();
	private PerunTable<FacilityState> table;
	// Selection model
	final MultiSelectionModel<FacilityState> selectionModel = new MultiSelectionModel<FacilityState>(new FacilityStateKeyProvider());
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private int facilityId = 0;
	private int voId = 0;
	// oracle support
	private ArrayList<FacilityState> fullBackup = new ArrayList<FacilityState>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();

	/**
	 * New instance of get facility state
	 *
	 * @param facilityId - can be 0 if we want all facilities
	 * @param voId - if NOT ZERO, get all facilities related to this VO
	 */
	public GetFacilityState(int facilityId, int voId) {
		this.facilityId = facilityId;
		this.voId = voId;
	}

	/**
	 * New instance of get facility state with external events
	 *
	 * @param facilityId - can be 0 if we want all facilities
	 * @param voId - if NOT ZERO, get all facilities related to this VO
	 * @param events external events
	 */
	public GetFacilityState(int facilityId, int voId, JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.voId = voId;
		this.events = events;
	}

	/**
	 * Return table with owners - starts RPC call
	 *
	 * @return table widget
	 */
	public CellTable<FacilityState> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<FacilityState>(list);

		// Cell table
		table = new PerunTable<FacilityState>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<FacilityState> columnSortHandler = new ListHandler<FacilityState>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<FacilityState> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// facility column
		Column<FacilityState, String> facilityColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<FacilityState, String>() {
					public String getValue(FacilityState object) {
						return String.valueOf(object.getFacility().getName());
					}
				}, new FieldUpdater<FacilityState, String>(){
					public void update(int index, FacilityState object, String value) {
						if (session.isPerunAdmin() || session.isFacilityAdmin(object.getFacility().getId())) {
							session.getTabManager().addTab(new FacilityDetailTabItem(object.getFacility(), 2));
						}
					}
				});

		facilityColumn.setSortable(true);
		columnSortHandler.setComparator(facilityColumn, new Comparator<FacilityState>(){
			public int compare(FacilityState o1, FacilityState o2) {
				return o1.getFacility().getName().compareToIgnoreCase((o2.getFacility().getName()));
			}
		});

		// status column
		Column<FacilityState, String> statusColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<FacilityState, String>() {
					public String getValue(FacilityState object) {
						return String.valueOf(object.getState());
					}
				}, null);

		statusColumn.setSortable(true);
		columnSortHandler.setComparator(statusColumn, new Comparator<FacilityState>(){
			public int compare(FacilityState o1, FacilityState o2) {
				return o1.getState().compareToIgnoreCase(o2.getState());
			}
		});

		/*
		// error column
		Column<FacilityState, String> errorColumn = JsonUtils.addColumn(
		new JsonUtils.GetValue<FacilityState, String>() {
		public String getValue(FacilityState object) {
		Set<String> set = new HashSet<String>();
		for (int i=0; i<object.getTasksResults().length(); i++) {
		if (!set.contains(object.getTasksResults().get(i).getDestination().getDestination())) {
		set.add(object.getTasksResults().get(i).getDestination().getDestination());
		}
		}
		String result = "";
		ArrayList<String> list = new ArrayList<String>();
		for (String dest : set) {
		list.add(dest);
		}
		Collections.sort(list);
		for (String s : list) {
		result = result + s + ", ";
		}
		return result;
		}
		}, null);
		*/

		table.addColumn(facilityColumn, "Facility");
		table.addColumn(statusColumn, "Propagation state");
		//table.addColumn(errorColumn, "Nodes in error");

		// set row styles based on task state
		table.setRowStyles(new RowStyles<FacilityState>(){
			public String getStyleNames(FacilityState row, int rowIndex) {

				if (row.getState().equalsIgnoreCase("NOT_DETERMINED")) {
					return "";
				}
				else if (row.getState().equalsIgnoreCase("OK")){
				
					return "rowgreen";
				}
				else if (row.getState().equalsIgnoreCase("PROCESSING")){
					return "rowyellow";
				}
				else if (row.getState().equalsIgnoreCase("OPEN")){
					return "roworange";
				}
				else if (row.getState().equalsIgnoreCase("ERROR")){
					return "rowred";
				}
				else if (row.getState().equalsIgnoreCase("WARNING")){
					return "rowgreenyellow";
				}
				return "";

			}
		});

		return table;

	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<FacilityState>().sortByFacilityName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object FacilityState to be added as new row
	 */
	public void addToTable(FacilityState object) {
		list.add(object);
		oracle.add(object.getFacility().getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object FacilityState to be removed as row
	 */
	public void removeFromTable(FacilityState object) {
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
	public ArrayList<FacilityState> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading FacilityState");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading FacilityState started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<FacilityState>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("FacilityState loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, FacilityState object) {
		list.add(index, object);
		oracle.add(object.getFacility().getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		// TODO Auto-generated method stub
	}

	public void setList(ArrayList<FacilityState> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<FacilityState> getList() {
		return this.list;
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {

		final JsonCallback passToCallback = this;

		JsonPostClient jsp = new JsonPostClient(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				passToCallback.onFinished(jso);
			}

			@Override
			public void onError(PerunError error) {
				passToCallback.onError(error);
			}

			@Override
			public void onLoadingStart() {
				passToCallback.onLoadingStart();
			}
		});

		if (facilityId != 0 ){
			// get specific facility
			jsp.put("facility", new JSONNumber(facilityId));
			jsp.sendData(JSON_URL);
		} else if (voId == 0) {
			// get all facilities where user is admin
			jsp.sendNativeData("tasksManager/getAllFacilitiesStates", "{}");
		} else {
			// get facilities related to VO
			jsp.put("vo", new JSONNumber(voId));
			jsp.sendData("tasksManager/getAllFacilitiesStates");
		}
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
			for (FacilityState fac : fullBackup){
				// store facility by filter
				if (fac.getFacility().getName().toLowerCase().startsWith(text.toLowerCase())) {
					list.add(fac);
				}
			}
		}
		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
