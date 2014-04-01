package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.ResourceTag;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Axaj query to get all resources tags for VO or Resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAllResourcesTags implements JsonCallback, JsonCallbackTable<ResourceTag>, JsonCallbackOracle<ResourceTag> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	static private final String JSON_URL_VO = "resourcesManager/getAllResourcesTagsForVo";
	static private final String JSON_URL_RESOURCE = "resourcesManager/getAllResourcesTagsForResource";
	// Selection model
	final MultiSelectionModel<ResourceTag> selectionModel = new MultiSelectionModel<ResourceTag>(
			new GeneralKeyProvider<ResourceTag>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<ResourceTag> dataProvider = new ListDataProvider<ResourceTag>();
	// Table itself
	private PerunTable<ResourceTag> table;
	// Table list
	private ArrayList<ResourceTag> list = new ArrayList<ResourceTag>();
	// Table field updater
	private FieldUpdater<ResourceTag, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle support
	private ArrayList<ResourceTag> fullBackup = new ArrayList<ResourceTag>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();

	private boolean checkable = true;
	private boolean editable = false;

	private PerunEntity entity;
	private int entityId = 0;


	/**
	 * Creates a new getResources method instance
	 *
	 * @param entity Perun entity VO or Resource
	 * @param id VO ID
	 */
	public GetAllResourcesTags(PerunEntity entity, int id) {
		this.entity = entity;
		this.entityId = id;
	}

	/**
	 * Creates a new getResources method instance
	 *
	 * @param entity Perun entity VO or Resource
	 * @param id VO ID
	 * @param events Custom events
	 */
	public GetAllResourcesTags(PerunEntity entity, int id, JsonCallbackEvents events) {
		this.entity = entity;
		this.entityId = id;
		this.events = events;
	}

	/**
	 * Returns the celltable with custom onclick
	 * @param fu Field updater
	 * @return
	 */
	public CellTable<ResourceTag> getTable(FieldUpdater<ResourceTag, String> fu)
	{
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns just the celltable
	 * @return
	 */
	public CellTable<ResourceTag> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<ResourceTag>(list);

		// Cell table
		table = new PerunTable<ResourceTag>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<ResourceTag> columnSortHandler = new ListHandler<ResourceTag>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<ResourceTag> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			loaderImage.setEmptyResultMessage("VO has no resource tags defined.");
		} else if (PerunEntity.RESOURCE.equals(entity)) {
			loaderImage.setEmptyResultMessage("Resource has no tags assigned.");
		}

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}

		table.addIdColumn("Tag ID", tableFieldUpdater);

		if (editable) {

			// DISPLAY NAME COLUMN
			final Column<ResourceTag, String> nameColumn = new Column<ResourceTag, String>(new TextInputCell()) {
				public String getValue(ResourceTag tag) {
					return tag.getName();
				}
			};

			nameColumn.setFieldUpdater(new FieldUpdater<ResourceTag, String>() {
				@Override
				public void update(int i, final ResourceTag tag, final String s) {
					tag.setName(s.trim());
					selectionModel.setSelected(tag, true);
				}
			});
			nameColumn.setSortable(true);
			columnSortHandler.setComparator(nameColumn, new Comparator<ResourceTag>(){
				public int compare(ResourceTag arg0, ResourceTag arg1) {
					return (arg0.getName().compareToIgnoreCase(arg1.getName()));
				}
			});
			table.addColumn(nameColumn, "Tag name");

		} else {

			// name column
			Column<ResourceTag, String> nameColumn = JsonUtils.addColumn(
					new JsonUtils.GetValue<ResourceTag, String>() {
						public String getValue(ResourceTag object) {
							return object.getName();
						}
					}, tableFieldUpdater);

			nameColumn.setSortable(true);
			columnSortHandler.setComparator(nameColumn, new Comparator<ResourceTag>(){
				public int compare(ResourceTag arg0, ResourceTag arg1) {
					return (arg0.getName().compareToIgnoreCase(arg1.getName()));
				}
			});
			table.addColumn(nameColumn, "Tag name");

		}

		return table;
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			String param = "vo=" + this.entityId;
			JsonClient js = new JsonClient();
			js.retrieveData(JSON_URL_VO, param, this);
		} else if (PerunEntity.RESOURCE.equals(entity)) {
			String param = "resource=" + this.entityId;
			JsonClient js = new JsonClient();
			js.retrieveData(JSON_URL_RESOURCE, param, this);
		}

	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<ResourceTag>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(ResourceTag object) {
		list.add(object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(ResourceTag object) {
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
	public ArrayList<ResourceTag> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading resources tags.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading resources tags started.");
		loaderImage.loadingStart();
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<ResourceTag>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Resources tags loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, ResourceTag object) {
		list.add(index, object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<ResourceTag> list) {
		clearTable();
		this.list.addAll(list);
		for (ResourceTag r : list) {
			oracle.add(r.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<ResourceTag> getList() {
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
			for (ResourceTag res : fullBackup){
				if (res.getName().toLowerCase().startsWith(filter.toLowerCase())) {
					list.add(res);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No resource tag matching '"+filter+"' found.");
		} else {
			if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
				loaderImage.setEmptyResultMessage("VO has no resource tags defined.");
			} else if (PerunEntity.RESOURCE.equals(entity)) {
				loaderImage.setEmptyResultMessage("Resource has no tags assigned.");
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
