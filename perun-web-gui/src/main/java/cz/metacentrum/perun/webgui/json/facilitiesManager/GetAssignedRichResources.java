package cz.metacentrum.perun.webgui.json.facilitiesManager;

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
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all resources assigned to selected facility
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class GetAssignedRichResources implements JsonCallback, JsonCallbackTable<RichResource>, JsonCallbackOracle<RichResource> {

	// params
	static private final String JSON_URL = "facilitiesManager/getAssignedRichResources";
	private int facilityId = 0;
	private PerunWebSession session = PerunWebSession.getInstance();
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private ListDataProvider<RichResource> dataProvider = new ListDataProvider<RichResource>();
	private PerunTable<RichResource> table;
	private ArrayList<RichResource> list = new ArrayList<RichResource>();
	private FieldUpdater<RichResource, String> tableFieldUpdater;
	final MultiSelectionModel<RichResource> selectionModel = new MultiSelectionModel<RichResource>(new GeneralKeyProvider<RichResource>());
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" .-");
	private ArrayList<RichResource> backupList = new ArrayList<RichResource>();
	private boolean checkable = true;

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of service to get facilities for
	 */
	public GetAssignedRichResources(int facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * Creates a new callback
	 *
	 * @param facilityId ID of service to get facilities for
	 * @param events custom events
	 */
	public GetAssignedRichResources(int facilityId, JsonCallbackEvents events) {
		this.facilityId = facilityId;
		this.events = events;
	}

	/**
	 * Returns table with facilities assigned to specified service
	 *
	 * @param fu custom onClick
	 * @return table widget
	 */
	public CellTable<RichResource> getTable(FieldUpdater<RichResource,String> fu) {
		this.tableFieldUpdater = fu;
		this.retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns table with facilities assigned to specified service
	 *
	 * @return table widget
	 */
	public CellTable<RichResource> getTable() {
		this.retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns empty table with facilities assigned to specified service
	 *
	 * @return table widget
	 */
	public CellTable<RichResource> getEmptyTable() {

		// Table data provider.
		dataProvider = new ListDataProvider<RichResource>(list);

		// Cell table
		table = new PerunTable<RichResource>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<RichResource> columnSortHandler = new ListHandler<RichResource>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichResource> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Facility has no resources.");

		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();
		}

		// TABLE CONTENT

		// Create ID column.
		Column<RichResource, String> idColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichResource, String>() {
					public String getValue(RichResource object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);

		columnSortHandler.setComparator(idColumn, new GeneralComparator<RichResource>(GeneralComparator.Column.ID));
		idColumn.setSortable(true);

		// headers
		if(JsonUtils.isExtendedInfoVisible()){
			table.addColumn(idColumn, "Resource ID");
		}

		table.addNameColumn(tableFieldUpdater);

		// VO column.
		Column<RichResource, String> voColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichResource, String>() {
					public String getValue(RichResource object) {
						if(object == null){
							return "";
						}
						return object.getVo().getName();
					}
				}, tableFieldUpdater);

		// VO column comparator
		voColumn.setSortable(true);
		columnSortHandler.setComparator(voColumn, new Comparator<RichResource>() {
			public int compare(RichResource o1, RichResource o2) {
				return o1.getVo().getName().compareToIgnoreCase(o2.getVo().getName());
			}
		});

		table.addColumn(voColumn, "Virtual Organization");

		table.addDescriptionColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		JsonClient cl = new JsonClient();
		cl.retrieveData(JSON_URL, "facility="+facilityId, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<RichResource>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object RichResource to be added as new row
	 */
	public void addToTable(RichResource object) {
		list.add(object);
		oracle.add(object.getName());
		oracle.add(object.getVo().getName());
		oracle.add(object.getVo().getShortName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object RichResource to be removed as row
	 */
	public void removeFromTable(RichResource object) {
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
		backupList.clear();
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
	public ArrayList<RichResource> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading assigned resources.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading assigned resources started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<RichResource>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Assigned resources loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, RichResource object) {
		list.add(index, object);
		oracle.add(object.getName());
		oracle.add(object.getVo().getName());
		oracle.add(object.getVo().getShortName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<RichResource> list) {
		clearTable();
		this.list.addAll(list);
		for (RichResource rr : list) {
			oracle.add(rr.getVo().getName());
			oracle.add(rr.getVo().getShortName());
			oracle.add(rr.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<RichResource> getList() {
		return this.list;
	}

	public UnaccentMultiWordSuggestOracle getOracle(){
		return this.oracle;
	}

	public void filterTable(String text){

		// store list only for first time
		if (backupList.isEmpty() || backupList == null) {
			backupList.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (text.equalsIgnoreCase("")) {
			list.addAll(backupList);
		} else {
			for (RichResource r : backupList){
				if ((r.getName().toLowerCase().contains(text.toLowerCase())) ||
						(r.getVo().getName().toLowerCase().contains(text.toLowerCase())) ||
						(r.getVo().getShortName().toLowerCase().contains(text.toLowerCase()))) {
					list.add(r);
						}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No resource matching '"+text+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("Facility has no resources.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
