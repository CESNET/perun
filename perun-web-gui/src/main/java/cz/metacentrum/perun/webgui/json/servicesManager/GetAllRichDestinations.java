package cz.metacentrum.perun.webgui.json.servicesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
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

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all rich destinations for selected facility or service
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 96e0726384e9a35f4ffab2d462ea9dc1600c6644 $
 */

public class GetAllRichDestinations implements JsonCallback, JsonCallbackTable<Destination>, JsonCallbackOracle<Destination> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url for services
	private final String JSON_URL = "servicesManager/getAllRichDestinations";
	// Data provider and tables
	private ListDataProvider<Destination> dataProvider = new ListDataProvider<Destination>();
	private PerunTable<Destination> table;
	private ArrayList<Destination> list = new ArrayList<Destination>();
	// Selection model
	final MultiSelectionModel<Destination> selectionModel = new MultiSelectionModel<Destination>(new DestinationKeyProvider());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private Facility facility = null;
	private Service service = null;
	private boolean showFac = false;
	private boolean showServ = true; // display service column by default
	private boolean checkable = true;
	// oracle support
	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private ArrayList<Destination> fullBackup = new ArrayList<Destination>();

	/**
	 * Create new ajax query
	 *
	 * @param facility facility to get destinations for
	 * @param service service to get destinations for
	 */
	public GetAllRichDestinations(Facility facility, Service service) {
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
	public GetAllRichDestinations(Facility facility, Service service, JsonCallbackEvents events) {
		this.events = events;
		this.facility = facility;
		this.service = service;
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

        if (showFac) {
            loaderImage.setEmptyResultMessage("Service has no destination.");
        } else {
            loaderImage.setEmptyResultMessage("Facility has no service destination.");
        }

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
		table.addIdColumn("Destination ID", null);

		// DESTINATION COLUMN
		TextColumn<Destination> destinationColumn = new TextColumn<Destination>(){
			public String getValue(Destination object) {
				return object.getDestination();
			}
		};

		// TYPE COLUMN
		TextColumn<Destination> typeColumn = new TextColumn<Destination>(){
			public String getValue(Destination object) {
				return object.getType();
			}
		};

		// SERVICE COLUMN
		TextColumn<Destination> serviceColumn = new TextColumn<Destination>(){
			public String getValue(Destination object) {
				return object.getService().getName();
			}
		};

		// FACILITY COLUMN
		TextColumn<Destination> facilityColumn = new TextColumn<Destination>(){
			public String getValue(Destination object) {
				return object.getFacility().getName() + " ("+object.getFacility().getType()+")";
			}
		};

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


		// updates the columns size
		table.setColumnWidth(serviceColumn, 250.0, Unit.PX);
		table.setColumnWidth(facilityColumn, 250.0, Unit.PX);

		// Add the columns.

		if (showServ) {
			table.addColumn(serviceColumn, "Service");
		}
		if (showFac) {
			table.addColumn(facilityColumn, "Facility");
		}
		
		table.addColumn(destinationColumn, "Destination");
		table.addColumn(typeColumn, "Type");

		return table;

	}

    /**
     * Sorts table by objects Name
     */
    public void sortTable() {
        if (service == null) {
            list = new TableSorter<Destination>().sortByFacilityName(getList());
        } else {
            list = new TableSorter<Destination>().sortByService(getList());
        }
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
        oracle.add(object.getDestination());
        if (service == null) {
            oracle.add(object.getService().getName());
        } else {
            oracle.add(object.getFacility().getName());
        }
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
        selectionModel.clear();
        oracle.clear();
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
        setList(JsonUtils.<Destination>jsoAsList(jso));
        sortTable();
        session.getUiElements().setLogText("Destinations loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();
    }

    public void insertToTable(int index, Destination object) {
        list.add(index, object);
        oracle.add(object.getDestination());
        if (service == null) {
            oracle.add(object.getService().getName());
        } else {
            oracle.add(object.getFacility().getName());
        }
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
        for (Destination d : list) {
            oracle.add(d.getDestination());
            if (service == null) {
                oracle.add(d.getService().getName());
            } else {
                oracle.add(d.getFacility().getName());
            }
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
		String param = "";
		if (service != null) {
			param = "service="+service.getId();
		} else if (facility != null) {
			param = "facility="+facility.getId();	
		}
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
	 * (clears facility object)
	 * 
	 * @param service service to get destinations for
	 */
	public void setService(Service service) {
		this.service = service;
		this.facility = null;
	}

	/**
	 * Sets facility object for this callback
	 * (clears service object)
	 * 
	 * @param facility facility to get destinations for
	 */
	public void setFacility(Facility facility) {
		this.facility = facility;
		this.service = null;
	}

	/**
	 * Allow facility column
	 */
	public void showFacilityColumn(boolean show){
		showFac = show;
	}

	/**
	 * Allow service column (true by default)
	 */
	public void showServiceColumn(boolean show){
		showServ = show;
	}

	public void filterTable(String text){

		// always clear selected items
		selectionModel.clear();
		
		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			for (Destination dst : getList()){
				fullBackup.add(dst);
			}	
		}
		if (text.equalsIgnoreCase("")) {
			setList(fullBackup);
		} else {
			getList().clear();
			for (Destination dst : fullBackup){
				// store facility by filter
				if (service == null) {
					if (dst.getDestination().toLowerCase().startsWith(text.toLowerCase()) || dst.getService().getName().toLowerCase().startsWith(text.toLowerCase())) {
						addToTable(dst);
					}	
				} else {
					if (dst.getDestination().toLowerCase().startsWith(text.toLowerCase()) || dst.getFacility().getName().toLowerCase().startsWith(text.toLowerCase())) {
						addToTable(dst);
					}	
				}
			}
			if (getList().isEmpty()) {
				loaderImage.loadingFinished();
			}
            dataProvider.flush();
            dataProvider.refresh();
		}

        loaderImage.loadingFinished();
		
	}

	public MultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	public void setOracle(MultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}