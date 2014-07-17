package cz.metacentrum.perun.webgui.json.resourcesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get assigned members for specified resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAssignedRichMembers implements JsonCallback, JsonCallbackTable<RichMember>{

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// resource id
	private int resourceId;
	// JSON URL
	static private final String JSON_URL = "resourcesManager/getAssignedRichMembers";
	// Selection model
	final MultiSelectionModel<RichMember> selectionModel = new MultiSelectionModel<RichMember>(new GeneralKeyProvider<RichMember>());
	final SingleSelectionModel<RichMember> singleSelectionModel = new SingleSelectionModel<RichMember>(new GeneralKeyProvider<RichMember>());
	// is table single selection like ?
	boolean singleSelection = false;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<RichMember> dataProvider = new ListDataProvider<RichMember>();
	// Table itself
	private PerunTable<RichMember> table;
	// Table list
	private ArrayList<RichMember> list = new ArrayList<RichMember>();
	// Table field updater
	private FieldUpdater<RichMember, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * Creates a new callback instance
	 *
	 * @param id resource ID
	 */
	public GetAssignedRichMembers(int id) {
		this.resourceId = id;
	}

	/**
	 * Creates a new callback instance
	 *
	 * @param id resource ID
	 * @param events Custom events
	 */
	public GetAssignedRichMembers(int id, JsonCallbackEvents events) {
		this.resourceId = id;
		this.events = events;
	}

	/**
	 * Returns table with assigned members with custom onClick
	 *
	 * @param fu Field updater
	 * @return table widget
	 */
	public CellTable<RichMember> getTable(FieldUpdater<RichMember, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with assigned members
	 *
	 * @return table widget
	 */
	public CellTable<RichMember> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<RichMember>(list);

		// Cell table
		table = new PerunTable<RichMember>(list);
		table.setHyperlinksAllowed(false);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<RichMember> columnSortHandler = new ListHandler<RichMember>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		if (singleSelection) {
			table.setSelectionModel(singleSelectionModel, DefaultSelectionEventManager.<RichMember> createCheckboxManager());
		} else {
			table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichMember> createCheckboxManager());
		}

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Resource has no members assigned.");

		// checkbox column column
		if (checkable){
			table.addCheckBoxColumn();
		}
		table.addIdColumn("RichMember ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;
	}


	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		final String param = "resource=" + this.resourceId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<RichMember>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object RichMember to be added as new row
	 */
	public void addToTable(RichMember object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object RichMember to be removed as row
	 */
	public void removeFromTable(RichMember object) {
		list.remove(object);
		if (singleSelection) {
			singleSelectionModel.setSelected(object, false);
		} else {
			selectionModel.getSelectedSet().remove(object);
		}
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
		singleSelectionModel.clear();
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Clears list of selected items
	 */
	public void clearTableSelectedSet(){
		selectionModel.clear();
		singleSelectionModel.clear();
	}

	/**
	 * Return selected items from list
	 *
	 * @return return list of checked items
	 */
	public ArrayList<RichMember> getTableSelectedList(){
		if (singleSelection) {
			return JsonUtils.setToList(singleSelectionModel.getSelectedSet());
		} else {
			return JsonUtils.setToList(selectionModel.getSelectedSet());
		}
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading assigned members.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading assigned members started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<RichMember>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("RichMembers loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, RichMember object) {
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

	public void setList(ArrayList<RichMember> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<RichMember> getList() {
		return this.list;
	}

	/**
	 * Set single or multiple selection model for table
	 *
	 * @param single TRUE = single selection / FALSE = multiple selection
	 */
	public void setSingleSelection(boolean single){
		singleSelection = single;
	}

}
