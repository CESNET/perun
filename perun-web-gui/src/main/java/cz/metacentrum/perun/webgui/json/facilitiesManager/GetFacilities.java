package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
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
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * FacilitiesManager/getRichFacilities Method Provides getTable().
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class GetFacilities implements JsonCallback, JsonCallbackTable<Facility>, JsonCallbackOracle<Facility> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL = "facilitiesManager/getFacilities";
	static private final String JSON_URL_RICH = "facilitiesManager/getRichFacilities";
	// Data provider.
	private ListDataProvider<Facility> dataProvider = new ListDataProvider<Facility>();
	// The table itself
	private PerunTable<Facility> table;
	// Facilities list
	private ArrayList<Facility> list = new ArrayList<Facility>();
	// Table field updater
	private FieldUpdater<Facility, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// Selection model
	final MultiSelectionModel<Facility> selectionModel = new MultiSelectionModel<Facility>(new GeneralKeyProvider<Facility>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// display checkboxes?
	private boolean checkable = true;
	// oracle support
	private ArrayList<Facility> fullBackup = new ArrayList<Facility>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" .-");
	// get Rich or Normal facilities (default normal)
	private boolean provideRich = false;

	/**
	 * Creates a new instance of the request
	 *
	 * @param provideRich
	 */
	public GetFacilities(boolean provideRich) {
		this.provideRich = provideRich;
	}

	/**
	 * Creates a new instance of the request with custom events
	 *
	 * @param provideRich
	 * @param events
	 */
	public GetFacilities(boolean provideRich, JsonCallbackEvents events) {
		this.provideRich = provideRich;
		this.events = events;
	}

	/**
	 * Get the cell table with Facilities and custom onclick method
	 *
	 * @param fu - custom field updater
	 * @return CellTable with all facilities
	 */
	public CellTable<Facility> getTable(FieldUpdater<Facility, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Gets table with all facilities
	 *
	 * @return CellTable with all facilities
	 */
	public CellTable<Facility> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Facility>(list);

		// Cell table
		table = new PerunTable<Facility>(list);
		table.setHyperlinksAllowed(false); // prevent double-loading when clicked on name of facility in list

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Facility> columnSortHandler = new ListHandler<Facility>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Facility> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("You are not manager of any facility.");

		if(checkable) {
			table.addCheckBoxColumn();
		}

		table.addIdColumn("Facility ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater, 200);

		// Create owners column.
		Column<Facility, String> ownersColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Facility, String>() {
					public String getValue(Facility object) {
						String text = "";
						JsArray<Owner> owners = object.getOwners();
						for (int i=0; i<owners.length(); i++) {
							if ("technical".equals(owners.get(i).getType())) {
								text = text + owners.get(i).getName()+", ";
							}
						}
						if (text.length() >= 2) {
							text = text.substring(0, text.length()-2);
						}
						return text;
					}
				}, this.tableFieldUpdater);

		if (provideRich) {
			table.addColumn(ownersColumn, "Technical owners");
			table.setColumnWidth(ownersColumn, "25%");
		}

		table.addDescriptionColumn(tableFieldUpdater);

		return table;
	}

	/**
	 * Retrieves data
	 */
	public void retrieveData() {
		loaderImage.loadingStart();
		JsonClient js = new JsonClient();
		if (provideRich) {
			js.retrieveData(JSON_URL_RICH, this);
		} else {
			js.retrieveData(JSON_URL, this);
		}

	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<Facility>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(Facility object) {
		list.add(object);
		oracle.add(object.getName());
		if (provideRich) {
			// fill oracle with technical owners
			JsArray<Owner> owners = object.getOwners();
			for (int n=0; n<owners.length(); n++) {
				if ("technical".equals(owners.get(n).getType())) {
					oracle.add(owners.get(n).getName());
				}
			}
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(Facility object) {
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
	public ArrayList<Facility> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Facilities.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Facilities started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Facility>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Facilities loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Facility object) {
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

	public void setList(ArrayList<Facility> list) {
		clearTable();
		this.list.addAll(list);
		for (Facility object : list) {
			oracle.add(object.getName());
			if (provideRich) {
				// fill oracle with technical owners
				JsArray<Owner> owners = object.getOwners();
				for (int n=0; n<owners.length(); n++) {
					if ("technical".equals(owners.get(n).getType())) {
						oracle.add(owners.get(n).getName());
					}
				}
			}
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Facility> getList() {
		return this.list;
	}

	public ArrayList<Facility> getFullBackupList() {
		if (fullBackup.isEmpty() || fullBackup == null) {
			fullBackup.addAll(list);
		}
		return this.fullBackup;
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
			for (Facility fac : fullBackup){
				// store facility by filter
				if (fac.getName().toLowerCase().contains(text.toLowerCase())) {
					list.add(fac);
				} else if (provideRich) {
					// if name doesn't match, try to match owners
					JsArray<Owner> owners = fac.getOwners();
					for (int n=0; n<owners.length(); n++){
						if ("technical".equals(owners.get(n).getType()) &&
								owners.get(n).getName().toLowerCase().equals(text.toLowerCase())) {
							list.add(fac);
								}
					}
				}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No facility matching '"+text+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("You are not manager of any facility.");
		}

		loaderImage.loadingFinished();
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
