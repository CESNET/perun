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
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get assigned members for specified resource
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAssignedMembers implements JsonCallback, JsonCallbackTable<Member>{

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// resource id
	private int resourceId;
	// JSON URL
	static private final String JSON_URL = "resourcesManager/getAssignedMembers";
	// Selection model
	final MultiSelectionModel<Member> selectionModel = new MultiSelectionModel<Member>(new GeneralKeyProvider<Member>());
	final SingleSelectionModel<Member> singleSelectionModel = new SingleSelectionModel<Member>(new GeneralKeyProvider<Member>());
	// is table single selection like ?
	boolean singleSelection = false;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<Member> dataProvider = new ListDataProvider<Member>();
	// Table itself
	private PerunTable<Member> table;
	// Table list
	private ArrayList<Member> list = new ArrayList<Member>();
	// Table field updater
	private FieldUpdater<Member, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;

	/**
	 * Creates a new callback instance
	 *
	 * @param id resource ID
	 */
	public GetAssignedMembers(int id) {
		this.resourceId = id;
	}

	/**
	 * Creates a new callback instance
	 *
	 * @param id resource ID
	 * @param events Custom events
	 */
	public GetAssignedMembers(int id, JsonCallbackEvents events) {
		this.resourceId = id;
		this.events = events;
	}

	/**
	 * Returns table with assigned members with custom onClick
	 *
	 * @param fu Field updater
	 * @return table widget
	 */
	public CellTable<Member> getTable(FieldUpdater<Member, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with assigned members
	 *
	 * @return table widget
	 */
	public CellTable<Member> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Member>(list);

		// Cell table
		table = new PerunTable<Member>(list);
		table.setHyperlinksAllowed(false);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Member> columnSortHandler = new ListHandler<Member>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		if (singleSelection) {
			table.setSelectionModel(singleSelectionModel, DefaultSelectionEventManager.<Member> createCheckboxManager());
		} else {
			table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Member> createCheckboxManager());
		}

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Resource has no members assigned.");

		// checkbox column column
		if (checkable){
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Member ID", tableFieldUpdater);
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
		list = new TableSorter<Member>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Member to be added as new row
	 */
	public void addToTable(Member object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Member to be removed as row
	 */
	public void removeFromTable(Member object) {
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
	public ArrayList<Member> getTableSelectedList(){
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
		setList(JsonUtils.<Member>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Members loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Member object) {
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

	public void setList(ArrayList<Member> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Member> getList() {
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
