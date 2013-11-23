package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all facility owners
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 9a36d0278e8d44666dbc40ddacdde585d192a992 $
 */
public class GetFacilityOwners implements JsonCallback, JsonCallbackTable<Owner> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "facilitiesManager/getOwners";
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// data providers
	private ListDataProvider<Owner> dataProvider = new ListDataProvider<Owner>();
	private ArrayList<Owner> list = new ArrayList<Owner>();
	private PerunTable<Owner> table;
	// Selection model
	final MultiSelectionModel<Owner> selectionModel = new MultiSelectionModel<Owner>(new GeneralKeyProvider<Owner>());
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// facility
	private Facility facility;
	// checkable
	private boolean checkable = true;

	/**
	 * New instance of get facility owners
	 * 
	 * @param fac Facility
	 */
	public GetFacilityOwners(Facility fac) {
		this.facility = fac;
	}

	/**
	 * New instance of get facility owners with external events
	 * 
	 * @param fac Facility
	 * @param events external events
	 */
	public GetFacilityOwners(Facility fac, JsonCallbackEvents events) {
		this.facility = fac;
		this.events = events;
	}

	/**
	 * Return table with facility owners - starts RPC call
	 *  
	 * @return table widget
	 */
	public CellTable<Owner> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Owner>(list);

		// Cell table
		table = new PerunTable<Owner>(list);
		
		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

        loaderImage.setEmptyResultMessage("Facility has no owners.");

		// Sorting
		ListHandler<Owner> columnSortHandler = new ListHandler<Owner>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);
		
		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Owner> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		
		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();			
		}
		
		table.addIdColumn("Owner Id");
		
		table.addNameColumn(null);

		// CONTACT COLUMN
		TextColumn<Owner> contactColumn = new TextColumn<Owner>() {
			public String getValue(Owner owner) {
				return String.valueOf(owner.getContact());
			}
		};

		table.addColumn(contactColumn, "Contact");

		contactColumn.setSortable(true);
		columnSortHandler.setComparator(contactColumn, new Comparator<Owner>(){
			public int compare(Owner o1, Owner o2) {
				return o1.getContact().compareToIgnoreCase(o2.getContact());
			}
		});

        // OWNER TYPE COLUMN
        TextColumn<Owner> typeColumn = new TextColumn<Owner>() {
            public String getValue(Owner owner) {
                return Owner.getTranslatedType(owner.getType());
            }
        };

        table.addColumn(typeColumn, "Type");

        typeColumn.setSortable(true);
        columnSortHandler.setComparator(typeColumn, new Comparator<Owner>(){
            public int compare(Owner o1, Owner o2) {
                return Owner.getTranslatedType(o1.getType()).compareToIgnoreCase(Owner.getTranslatedType(o2.getType()));
            }
        });
		
		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "facility="+facility.getId(), this);
	}

    /**
     * Sorts table by objects date
     */
    public void sortTable() {
        list = new TableSorter<Owner>().sortByName(getList());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Add object as new row to table
     *
     * @param object Owner to be added as new row
     */
    public void addToTable(Owner object) {
        list.add(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object Owner to be removed as row
     */
    public void removeFromTable(Owner object) {
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
    public ArrayList<Owner> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading Owners.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading Owners started.");
        events.onLoadingStart();
    }

    /**
     * Called when loading successfully finishes.
     */
    public void onFinished(JavaScriptObject jso) {
        setList(JsonUtils.<Owner>jsoAsList(jso));
        sortTable();
        session.getUiElements().setLogText("Owners loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();

    }

    public void insertToTable(int index, Owner object) {
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

    public void setList(ArrayList<Owner> list) {
        clearTable();
        this.list.addAll(list);
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<Owner> getList() {
        return this.list;
    }

}