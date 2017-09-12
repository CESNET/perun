package cz.metacentrum.perun.webgui.json.servicesManager;

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
import cz.metacentrum.perun.webgui.json.keyproviders.DestinationKeyProvider;
import cz.metacentrum.perun.webgui.model.Destination;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get destinations for selected facility and service
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class GetDestinations implements JsonCallback, JsonCallbackTable<Destination>, JsonCallbackOracle<Destination> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url for services
	private final String JSON_URL = "servicesManager/getDestinations";
	// Data provider and tables
	private ListDataProvider<Destination> dataProvider = new ListDataProvider<Destination>();
	private PerunTable<Destination> table;
	private ArrayList<Destination> list = new ArrayList<Destination>();
	// Selection model
	final MultiSelectionModel<Destination> selectionModel = new MultiSelectionModel<Destination>(new DestinationKeyProvider());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Destination, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private Facility facility = null;
	private Service service = null;
	private boolean showFac = false;
	private boolean checkable = true;
	// oracle support
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" .-");
	private ArrayList<Destination> fullBackup = new ArrayList<Destination>();

	/**
	 * Create new ajax query
	 *
	 * @param facility facility to get destinations for
	 * @param service service to get destinations for
	 */
	public GetDestinations(Facility facility, Service service) {
		this.facility = facility;
		this.service = service;
	}

	/**
	 * Create new ajax query with custom events
	 *
	 * @param facility facility to get destinations for
	 * @param service service to get destinations for
	 * @param events custom events
	 */
	public GetDestinations(Facility facility, Service service, JsonCallbackEvents events) {
		this.events = events;
		this.facility = facility;
		this.service = service;
	}

	/**
	 * Returns table with destinations and custom onClick
	 *
	 * @param fu field updater
	 * @return table widget
	 */
	public CellTable<Destination> getTable(FieldUpdater<Destination, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with destinations
	 * @return table widget
	 */
	public CellTable<Destination> getTable()
	{
		// retrieves data
		retrieveData();
		return getEmptyTable();
	}

	/**
	 * Returns empty table
	 * @return table widget
	 */
	public CellTable<Destination> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Destination>(list);

		// Cell table
		table = new PerunTable<Destination>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Destination> columnSortHandler = new ListHandler<Destination>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Destination> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		if(this.checkable)
		{
			// checkbox column column
			table.addCheckBoxColumn();
		}

		//add id column
		table.addIdColumn("Destination ID", tableFieldUpdater);

		// DESTINATION COLUMN
		Column<Destination, String> destinationColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Destination,String>() {
			public String getValue(Destination dest) {
				return dest.getDestination();
			}
		}, tableFieldUpdater);

		// TYPE COLUMN
		Column<Destination, String> typeColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Destination,String>() {
			public String getValue(Destination dest) {
				return dest.getType().toUpperCase();
			}
		}, tableFieldUpdater);

		// SERVICE COLUMN
		Column<Destination, String> serviceColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Destination,String>() {
			public String getValue(Destination dest) {
				if (dest.getService() != null ) { return dest.getService().getName(); }
				else { return ""; }
			}
		}, tableFieldUpdater);

		// FACILITY COLUMN
		Column<Destination, String> facilityColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Destination, String>() {
			public String getValue(Destination dest) {
				if (dest.getService() != null ) { return dest.getFacility().getName(); }
				else { return ""; }
			}
		}, tableFieldUpdater);

		// PROPAGATION TYPE
		Column<Destination, String> propColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Destination, String>() {
			public String getValue(Destination dest) {
				return dest.getPropagationType();
			}
		}, tableFieldUpdater);

		destinationColumn.setSortable(true);
		columnSortHandler.setComparator(destinationColumn, new Comparator<Destination>() {
			public int compare(Destination o1, Destination o2) {
				return o1.getDestination().compareToIgnoreCase(o2.getDestination());
			}
		});

		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<Destination>() {
			public int compare(Destination o1, Destination o2) {
				return o1.getType().compareToIgnoreCase(o2.getType());
			}
		});

		serviceColumn.setSortable(true);
		columnSortHandler.setComparator(serviceColumn, new Comparator<Destination>() {
			public int compare(Destination o1, Destination o2) {
				return o1.getService().getName().compareToIgnoreCase(o2.getService().getName());
			}
		});

		facilityColumn.setSortable(true);
		columnSortHandler.setComparator(facilityColumn, new Comparator<Destination>() {
			public int compare(Destination o1, Destination o2) {
				return o1.getFacility().getName().compareToIgnoreCase(o2.getFacility().getName());
			}
		});

		propColumn.setSortable(true);
		columnSortHandler.setComparator(propColumn, new Comparator<Destination>() {
			public int compare(Destination o1, Destination o2) {
				return o1.getPropagationType().compareToIgnoreCase(o2.getPropagationType());
			}
		});


		// updates the columns size
		table.setColumnWidth(serviceColumn, 200.0, Unit.PX);
		table.setColumnWidth(facilityColumn, 200.0, Unit.PX);

		// Add the columns.


		table.addColumn(serviceColumn, "Service");

		if (showFac) {
			table.removeColumn(serviceColumn);
			table.addColumn(facilityColumn, "Facility");
		}

		table.addColumn(destinationColumn, "Destination");
		table.addColumn(typeColumn, "Type");
		table.addColumn(propColumn, "Propagation type");

		return table;

	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Destination>().sortByService(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Destination to be added as new row
	 */
	public void addToTable(Destination object) {
		list.add(object);
		if (object.getService() != null) {
			oracle.add(object.getService().getName());
		}
		oracle.add(object.getDestination());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Destination to be removed as row
	 */
	public void removeFromTable(Destination object) {
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
	public ArrayList<Destination> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading destinations");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading destinations started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		ArrayList<Destination> dest = JsonUtils.jsoAsList(jso);
		for (Destination d : dest) {
			d.setFacility(facility);
			d.setService(service);
			addToTable(d);
		}
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Destinations loaded: " + list.size());
		events.onFinished(jso);
	}

	public void insertToTable(int index, Destination object) {
		list.add(index, object);
		if (object.getService() != null) {
			oracle.add(object.getService().getName());
		}
		oracle.add(object.getDestination());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<Destination> list) {
		clearTable();
		this.list.addAll(list);
		for (Destination object : list) {
			if (object.getService() != null) {
				oracle.add(object.getService().getName());
			}
			oracle.add(object.getDestination());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Destination> getList() {
		return this.list;
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData(){
		String param = "service="+service.getId()+"&facility="+facility.getId();
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sets different external events after creating this query
	 *
	 * @param externalEvent external events
	 */
	public void setEvents(JsonCallbackEvents externalEvent) {
		events = externalEvent;
	}

	/**
	 * Sets service object for this callback
	 *
	 * @param service service to get destinations for
	 */
	public void setService(Service service) {
		this.service = service;
	}

	/**
	 * Sets facility object for this callback
	 *
	 * @param facility facility to get destinations for
	 */
	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	/**
	 * Switch view to facility column
	 */
	public void showFacilityColumn(){
		showFac = true;
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
			for (Destination dst : fullBackup){
				// store facility by filter
				if (dst.getService() != null) {
					if (dst.getDestination().toLowerCase().contains(text.toLowerCase()) || dst.getService().getName().toLowerCase().contains(text.toLowerCase())) {
						list.add(dst);
					}
				} else {
					if (dst.getDestination().toLowerCase().contains(text.toLowerCase())) {
						list.add(dst);
					}
				}
			}
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

}
