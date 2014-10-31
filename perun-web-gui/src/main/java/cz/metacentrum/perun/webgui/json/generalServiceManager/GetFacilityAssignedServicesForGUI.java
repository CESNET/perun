package cz.metacentrum.perun.webgui.json.generalServiceManager;

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
import cz.metacentrum.perun.webgui.model.RichService;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all services on facility with allowedOnFacility property filled.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetFacilityAssignedServicesForGUI implements JsonCallback, JsonCallbackTable<RichService> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "generalServiceManager/getFacilityAssignedServicesForGUI";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<RichService, String> tableFieldUpdater;
	// data providers
	private ListDataProvider<RichService> dataProvider = new ListDataProvider<RichService>();
	private ArrayList<RichService> list = new ArrayList<RichService>();
	private PerunTable<RichService> table;
	// Selection model
	final MultiSelectionModel<RichService> selectionModel = new MultiSelectionModel<RichService>(new GeneralKeyProvider<RichService>());
	private int facilityId = 0;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	/**
	 * New instance of callback
	 *
	 * @param facilityId ID of facility to get RichServices for
	 */
	public GetFacilityAssignedServicesForGUI(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * New instance of callback with custom events
	 *
	 * @param facilityId ID of facility to get services for
	 * @param events custom events
	 */
	public GetFacilityAssignedServicesForGUI(int facilityId,JsonCallbackEvents events) {
		this.events = events;
		this.facilityId = facilityId;
	}

	/**
	 * Returns table of assigned services on facility with custom onClick
	 *
	 * @param fu custom onClick (field updater)
	 */
	public CellTable<RichService> getTable(FieldUpdater<RichService, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Return table with assigned services on facility
	 *
	 * @return table widget
	 */
	public CellTable<RichService> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<RichService>(list);

		// Cell table
		table = new PerunTable<RichService>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<RichService> columnSortHandler = new ListHandler<RichService>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichService> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Can't configure services propagation. No service assigned to this facility.");

		// checkbox column column
		/*
			 Column<RichService, RichService> checkBoxColumn = new Column<RichService, RichService>(
			 new PerunCheckboxCell<RichService>(true, false, false)) {
			 @Override
			 public RichService getValue(RichService object) {
		// Get the value from the selection model.
		// object must be general to set checked
		GeneralObject obj = object.cast();
		obj.setChecked(selectionModel.isSelected(object));
		return object;
			 }
			 };

			 table.addColumn(checkBoxColumn);
			 table.setColumnWidth(checkBoxColumn, "60px");
			 */

		table.addCheckBoxColumn();

		table.addIdColumn("Service Id", tableFieldUpdater, 110);

		table.addNameColumn(tableFieldUpdater);

		// ALLOWED ON FACILITY COLUMN

		Column<RichService, String> allowedColumn = JsonUtils.addColumn(new JsonUtils.GetValue<RichService, String>() {
			public String getValue(RichService object) {
				return getAllowedValue(object);
			}
		},this.tableFieldUpdater);
		allowedColumn.setSortable(true);
		columnSortHandler.setComparator(allowedColumn, new Comparator<RichService>() {
			public int compare(RichService o1, RichService o2) {
				return (getAllowedValue(o1).compareToIgnoreCase(getAllowedValue(o2)));
			}
		});

		table.addColumn(allowedColumn, "Allowed on facility");

		// ALLOWED GLOBALLY COLUMN

		Column<RichService, String> allowedGloballyColumn = JsonUtils.addColumn(new JsonUtils.GetValue<RichService, String>() {
			public String getValue(RichService object) {
				String gen = "";
				String send = "";

				if (object.getGenExecService() != null) {
					if (object.getGenExecService().isEnabled()==true) {
						gen = "Allowed";
					} else {
						gen = "Denied";
					}
				} else {
					gen = "Not determined";
				}
				if (object.getSendExecService() != null) {
					if (object.getSendExecService().isEnabled()==true) {
						send = "Allowed";
					} else {
						send = "Denied";
					}
				} else {
					send = "Not determined";
				}
				return "GENERATE: "+ gen +" SEND: "+ send;
			}
		},this.tableFieldUpdater);

		table.addColumn(allowedGloballyColumn , "Allowed globally");


		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "id="+facilityId, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<RichService>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(RichService object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(RichService object) {
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
	public ArrayList<RichService> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading RichServices.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading RichServices started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<RichService>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("RichServices loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();

	}

	public void insertToTable(int index, RichService object) {
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

	public void setList(ArrayList<RichService> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<RichService> getList() {
		return this.list;
	}


	/**
	 * Sets different facility ID for callback after creation
	 *
	 * @param facilityId new facility ID
	 */
	public void setFacility(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * Sets events after callback creation
	 *
	 * @param events external events
	 */
	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

	private String getAllowedValue(RichService object) {

		String result = "GENERATE: ";
		if (object.getGenExecService() != null) {
			result += object.getGenAllowedOnFacility();
		} else {
			result += "Not determined";
		}
		result += " SEND: ";
		if (object.getSendExecService() != null) {
			result += object.getSendAllowedOnFacility();
		} else {
			result += "Not determined";
		}
		return result;

	}

	public MultiSelectionModel<RichService> getSelectionModel() {
		return this.selectionModel;
	}

}
