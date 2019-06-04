package cz.metacentrum.perun.webgui.json.resourcesManager;

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
import cz.metacentrum.perun.webgui.model.ResourceTag;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Axaj query to get Rich Resources for VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetRichResources implements JsonCallback, JsonCallbackTable<RichResource>, JsonCallbackOracle<RichResource> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO id
	private int voId;
	// JSON URL
	static private final String JSON_URL = "resourcesManager/getRichResources";
	// Selection model
	final MultiSelectionModel<RichResource> selectionModel = new MultiSelectionModel<RichResource>(
			new GeneralKeyProvider<RichResource>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<RichResource> dataProvider = new ListDataProvider<RichResource>();
	// Table itself
	private PerunTable<RichResource> table;
	// Table list
	private ArrayList<RichResource> list = new ArrayList<RichResource>();
	// Table field updater
	private FieldUpdater<RichResource, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle support
	private ArrayList<RichResource> fullBackup = new ArrayList<RichResource>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" .-");

	private boolean checkable = true;

	/**
	 * Creates a new getResources method instance
	 *
	 * @param id VO ID
	 */
	public GetRichResources(int id) {
		this.voId = id;
	}

	/**
	 * Creates a new getResources method instance
	 *
	 * @param id VO ID
	 * @param events Custom events
	 */
	public GetRichResources(int id, JsonCallbackEvents events) {
		this.voId = id;
		this.events = events;
	}

	/**
	 * Returns the celltable with custom onclick
	 * @param fu Field updater
	 * @return
	 */
	public CellTable<RichResource> getTable(FieldUpdater<RichResource, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns just the celltable
	 * @return
	 */
	public CellTable<RichResource> getTable() {

		// retrieve data
		retrieveData();

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

		loaderImage.setEmptyResultMessage("VO has no resources.");

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Resource ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);

		// FACILITY COLUMN
		Column<RichResource, String> facilityColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichResource, String>() {
					public String getValue(RichResource object) {
						return object.getFacility().getName();
					}
				}, tableFieldUpdater);

		facilityColumn.setSortable(true);
		columnSortHandler.setComparator(facilityColumn, new Comparator<RichResource>(){
			public int compare(RichResource arg0, RichResource arg1) {
				return (arg0.getFacility().getName()).compareToIgnoreCase(arg1.getFacility().getName());
			}
		});
		table.addColumn(facilityColumn, "Facility");

		// TAGS COLUMN
		Column<RichResource, String> tagsColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichResource, String>() {
					public String getValue(RichResource object) {

						ArrayList<ResourceTag> tags = object.getResourceTags();
						if (tags != null && !tags.isEmpty()) {
							String s = "";
							tags = new TableSorter<ResourceTag>().sortByName(tags);
							for (ResourceTag tag : tags) {
								s += tag.getName() + ", ";
							}
							s = s.substring(0, s.length()-2);
							return s;
						} else {
							return "";
						}
					}
				}, tableFieldUpdater);

		// TODO - sorting
		table.addColumn(tagsColumn, "Tags");
		table.setColumnWidth(tagsColumn, "200px");

		table.addDescriptionColumn(tableFieldUpdater);

		return table;
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		final String param = "vo=" + this.voId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<RichResource>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(RichResource object) {
		list.add(object);
		oracle.add(object.getName());
		for (ResourceTag rt : object.getResourceTags()) {
			oracle.add(rt.getName()+" (tag)");
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(RichResource object) {
		for (RichResource rr : list) {
			if (rr.getId() == object.getId()) list.remove(rr);
		}
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
	public ArrayList<RichResource> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading VO resources.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading VO resources started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<RichResource>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Resources loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, RichResource object) {
		list.add(index, object);
		oracle.add(object.getName());
		for (ResourceTag rt : object.getResourceTags()) {
			oracle.add(rt.getName()+" (tag)");
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

	public void setList(ArrayList<RichResource> list) {
		clearTable();
		this.list.addAll(list);
		for (RichResource r : list) {
			oracle.add(r.getName());
			for (ResourceTag rt : r.getResourceTags()) {
				oracle.add(rt.getName()+" (tag)");
			}
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<RichResource> getList() {
		return this.list;
	}

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
			for (RichResource res : fullBackup){
				// store resources by filter
				if (res.getName().toLowerCase().contains(filter.toLowerCase())) {
					list.add(res);
				} else {
					for (ResourceTag r : res.getResourceTags()) {
						// remove " (tag)" from tag name
						if (r.getName().contains(filter.substring(0, (filter.length() > 6) ? filter.length()-6 : filter.length()).trim())) {
							list.add(res);
							break;
						}
					}
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No resource matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("VO has no resources.");
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

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

	public MultiSelectionModel<RichResource> getSelectionModel() {
		return this.selectionModel;
	}

}
