package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;

/**
 * GroupsManager/getSubGroups Method
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetSubGroups implements JsonCallback, JsonCallbackTable<Group>, JsonCallbackOracle<Group> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Parent group id
	private int parentId;
	// Selection model
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<Group>(new GeneralKeyProvider<Group>());
	// JSON URL
	static final private String JSON_URL = "groupsManager/getSubGroups";
	// Table data provider
	private ListDataProvider<Group> dataProvider = new ListDataProvider<Group>();
	// The table itself
	private PerunTable<Group> table;
	// List in the table
	private ArrayList<Group> list = new ArrayList<Group>();
	// FIELD UPDATER - when user clicks on a row
	private FieldUpdater<Group, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(":");
	private ArrayList<Group> fullBackup = new ArrayList<Group>();

	/**
	 * Creates a new instance of GroupsManager/getSubGroups method
	 *
	 * @param id Parent group id
	 */
	public GetSubGroups(int id) {
		this.parentId = id;
	}

	/**
	 * Creates a new instance of GroupsManager/getSubGroups method with custom field updater
	 *
	 * @param id Parent group id
	 */
	public GetSubGroups(int id, JsonCallbackEvents events ) {
		this.parentId = id;
		this.events = events;
	}

	/**
	 * Retrieves data via RPC
	 */
	public void retrieveData() {
		final String param = "parentGroup=" + this.parentId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Returns the table with subgroups and custom field updater
	 * @return
	 */
	public CellTable<Group> getTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with subgroups.
	 * @return
	 */
	public CellTable<Group> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Group>(list);

		// Cell table
		table = new PerunTable<Group>(list);
		table.setHyperlinksAllowed(false);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Group> columnSortHandler = new ListHandler<Group>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Group> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Group has no sub-groups.");

		// checkbox column column
		table.addCheckBoxColumn();
		table.addIdColumn("Group ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;

	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Group>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Group to be added as new row
	 */
	public void addToTable(Group object) {
		list.add(object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Group to be removed as row
	 */
	public void removeFromTable(Group object) {
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
	public ArrayList<Group> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Sub-Groups");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Sub-Groups started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Group>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Sub-Groups loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Group object) {
		list.add(index, object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		// TODO Auto-generated method stub
	}

	public void setList(ArrayList<Group> list) {
		clearTable();
		this.list.addAll(list);
		for (Group g : list) {
			oracle.add(g.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Group> getList() {
		return this.list;
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
			for (Group grp : fullBackup){
				// store facility by filter
				if (grp.getName().toLowerCase().startsWith(text.toLowerCase()) ||
						grp.getName().toLowerCase().contains(":"+text.toLowerCase())) {
					list.add(grp);
						}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No sub-group matching '"+text+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("Group has no sub-groups.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
