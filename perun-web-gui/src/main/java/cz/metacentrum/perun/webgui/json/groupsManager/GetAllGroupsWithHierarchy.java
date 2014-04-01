package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunGroupCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Ajax query to get all groups in hierarchical structure
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAllGroupsWithHierarchy implements JsonCallback {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO id
	private int voId;
	// JSON URL
	static private final String JSON_URL = "groupsManager/getAllGroupsWithHierarchy";
	// Selection model
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<Group>(new GeneralKeyProvider<Group>());
	// Groups table data provider
	private ListDataProvider<Group> dataProvider = new ListDataProvider<Group>();
	// Groups table
	private CellTable<Group> table = new CellTable<Group>();
	// Groups table list
	private ArrayList<Group> list = new ArrayList<Group>();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Group, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// groups to HL.
	private Set<Group> groupsToHighlight;
	// Whether to check child groups when parent checked.
	private boolean checkChildGroups = true;
	private Set<Group> previousSelectedSet = new HashSet<Group>();

	private boolean coreGroupsCheckable = false;



	/**
	 * Creates a new callback
	 *
	 * @param id ID of VO for which we want groups for
	 */
	public GetAllGroupsWithHierarchy(int id) {
		this.voId = id;
	}

	/**
	 * Creates a new callback
	 *
	 * @param id ID of VO for which we want groups for
	 * @param events external events
	 */
	public GetAllGroupsWithHierarchy(int id, JsonCallbackEvents events) {
		this.voId = id;
		this.events = events;
	}

	/**
	 * Returns table with groups in hierarchical structure and with custom field updater
	 *
	 * @param fu Custom field updater
	 * @return table widget
	 */
	public CellTable<Group> getTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with groups in hierarchical structure and with custom field updater
	 *
	 * @return table widget
	 */
	public CellTable<Group> getTable() {

		// retrieve data
		retrieveData();

		dataProvider = new ListDataProvider<Group>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);
		table.setVisibleRange(0, 1000);
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Group> createCheckboxManager());

		// Updates the selection model = when selection changed, highlights the group children
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				highlightRowsWhenChanged();
			}
		});

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// Checkbox column. This table will uses a checkbox column for
		// selection.

		Column<Group, Group> checkBoxColumn = new Column<Group, Group>(
				new PerunCheckboxCell<Group>(true, false, coreGroupsCheckable)) {
			@Override
			public Group getValue(Group object) {
				// Get the value from the selection model.
				object.setChecked(selectionModel.isSelected(object));
				return object;
			}
		};

		// Create ID column.
		Column<Group, String> idColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Group, String>() {
					public String getValue(Group g) {
						return String.valueOf(g.getId());
					}
				}, tableFieldUpdater);

		// Name column
		Column<Group, Group> nameColumn = new Column<Group, Group>(new PerunGroupCell()) {
			@Override
			public Group getValue(Group object) {
				return object;
			}
		};
		nameColumn.setFieldUpdater(new FieldUpdater<Group, Group>() {
			public void update(int index, Group object, Group value) {
				tableFieldUpdater.update(index, object, value.getName());
			}
		});


		// Create description column.
		Column<Group, String> descriptionColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Group, String>() {
					public String getValue(Group group) {
						return group.getDescription();
					}
				},tableFieldUpdater);


		// updates the columns size
		table.setColumnWidth(checkBoxColumn, 40.0, Unit.PX);
		table.setColumnWidth(idColumn, 80.0, Unit.PX);

		// Add the columns.
		table.addColumn(checkBoxColumn,
				SafeHtmlUtils.fromSafeConstant("<br/>"));
		table.addColumn(idColumn, "ID");
		table.addColumn(nameColumn, "Name");
		table.addColumn(descriptionColumn, "Description");

		return table;
	}

	/**
	 * Highlights the ancestors of selected groups
	 */
	private void highlightRowsWhenChanged()
	{
		groupsToHighlight = new HashSet<Group>();

		// the previous now contains only that groups, which were selected before and not now
		this.previousSelectedSet.removeAll(this.selectionModel.getSelectedSet());

		// add parents first
		recursiveAddGroupsToHighlight(this.selectionModel.getSelectedSet(), this.previousSelectedSet);

		// sets the new styles
		table.setRowStyles(new RowStyles<Group>() {

			public String getStyleNames(Group row, int rowIndex) {
				// if contains, display highlighted
				if(groupsToHighlight.contains(row)){
					return "perun-ancestor-row";
				}

				return null;
			}
		});

		// redraw the table
		table.redraw();

		// set the previous selected set
		this.previousSelectedSet = this.selectionModel.getSelectedSet();
	}

	/**
	 * Search for the ancestors of the groups in the list of Groups in the query instance.
	 *
	 * @param groups all groups to search for
	 * @param groupsToExclude groups excluded from search
	 */
	private void recursiveAddGroupsToHighlight(Set<Group> groups, Set<Group> groupsToExclude)
	{
		Set<Group> toSearchIn = new HashSet<Group>();

		// goes through all groups and finds, whether any of them has parent in group
		for(Group group : this.list)
		{

			// if parent in exlcude list, uncheck this
			if(groupsToExclude.contains(group.getParentGroup()))
			{
				this.selectionModel.setSelected(group, false);

				// force the group children to uncheck
				groupsToExclude.add(group);
				continue;
			}

			// if group parent is highlighted
			if(groups.contains(group.getParentGroup())){

				// if not already there
				if(!this.groupsToHighlight.contains(group)){

					// highlight this group
					this.groupsToHighlight.add(group);

					// search in ancestors
					toSearchIn.add(group);

					// check it?
					if(this.checkChildGroups){
						this.selectionModel.setSelected(group, true);
					}
				}
			}
		}
		// if not empty, search in them
		if(!toSearchIn.isEmpty()){
			recursiveAddGroupsToHighlight(toSearchIn, groupsToExclude);
		}
	}

	/**
	 * Clear all table content
	 */
	public void clearTable()
	{
		loaderImage.loadingStart();
		list.clear();
	}

	/**
	 * Return selected groups rom list
	 *
	 * @return list of checked groups
	 */
	public ArrayList<Group> getTableSelectedList(){

		ArrayList<Group> list = new ArrayList<Group>();
		for (Group group : this.selectionModel.getSelectedSet()) {
			list.add(group);
		}
		return list;
	}

	/**
	 * Clears selected set
	 */
	public void clearTableSelectedSet(){
		this.selectionModel.clear();
	}

	/**
	 * Returns parent selected set ("blue groups").
	 * @return
	 */
	public Set<Group> getParentSelectedSet()
	{
		Set<Group> parentSelectedSet = new HashSet<Group>();

		for(Group g : selectionModel.getSelectedSet()){
			if(!selectionModel.getSelectedSet().contains(g.getParentGroup()))
			{
				parentSelectedSet.add(g);
			}
		}

		return parentSelectedSet;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		final String param = "vo=" + this.voId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Updates the table with returned objects
	 *
	 * @param jso JS Array with objects returned from RPC
	 */
	private void rawUpdateGroupTable(JavaScriptObject jso)
	{
		dataProvider.getList().clear();
		recursiveGroups(jso, null, null, 0);
	}

	/**
	 * Loads recursively the groups
	 * Tries to parse the JSO as an array, if not succeeded, add the object.
	 *
	 * @param jso returned objects (array) from RPC
	 * @param parentGroup parent group
	 * @param previousGroup previouse group
	 * @param weight indent
	 * @return group or null
	 */
	private Group recursiveGroups(JavaScriptObject jso, Group parentGroup, Group previousGroup, int weight)
	{
		// if null
		if(jso == null)
		{
			return null;
		}

		// ? array
		if(!JsonUtils.isJsArray(jso)){
			// not array - add to list
			ArrayList<Group> list = this.list;
			Group g = (Group) jso;
			g.setObjectType("Group");  // set type
			g.setIndent(weight);

			// new parent
			parentGroup = previousGroup;
			g.setParentGroup(parentGroup);

			list.add(g);
			return g;
		}

		// if no parent set
		if(weight == 2 && parentGroup == null){
			parentGroup = previousGroup;
		}

		// array conversion
		JsArray<JavaScriptObject> groupsList = JsonUtils.jsoAsArray(jso);
		for (int i = 0; i < groupsList.length(); i++) {
			Group tmpGroup = recursiveGroups(groupsList.get(i), parentGroup, previousGroup, weight + 1);
			if(tmpGroup != null){
				previousGroup = tmpGroup;
			}
		}

		return null;
	}

	/**
	 * Add object as new row to table
	 *
	 * @param grp object to be added as new row
	 */
	public void addToTable(Group grp) {
		list.add(grp);
	}

	// TODO: put these methods into common class ??
	/**
	 * Add object as new row to table at specified position
	 *
	 * @param grp group object to be added as new row
	 * @param index index in list where to insert
	 */
	public void insertToTable(Group grp, int index) {
		// cover errors
		if (index >= list.size() || index<0 ) {
			// add to end
			list.add(grp);
			return;
		}
		ArrayList<Group> helpList = new ArrayList<Group>();
		// empty list, add as first
		if (list.isEmpty()) { list.add(grp); }
		else {
			// add before index
			for (int i=0; i<index; i++) {
				helpList.add(list.get(i));
			}
			// add at index
			helpList.add(grp);
			// add after index
			for (int i=index; i<list.size(); i++) {
				helpList.add(list.get(i));
			}
			// save list
			list.clear();
			list.addAll(helpList);
		}
	}

	/**
	 * Retrieves groups: administrators, members
	 */
	private void retrieveAdminsAndMembers()
	{
		// add member and admins groups
		JsonCallbackEvents externalEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				Group grp = jso.cast();
				grp.setIndent(1);
				insertToTable(grp, 0);
			}
		};

		GetGroupByName members = new GetGroupByName("members" , voId, externalEvents);
		members.retrieveData();
		//GetGroupByName admins = new GetGroupByName("administrators" ,voId, externalEvents);
		//admins.retrieveData();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param grp object to be removed as row
	 */
	public void removeFromTable(Group grp) {
		for (int i=0; i<list.size(); i++) {
			if (list.get(i).getId() == grp.getId()) {
				list.remove(i);
			}
		}
	}

	/**
	 * Called when an error occurs.
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading groups in selected VO.");
		loaderImage.loadingError(error);
		this.events.onError(error);
	}

	/**
	 * Called when loading starts.
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading groups in selected VO started.");
		this.events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 *
	 * @param jso javascript objects (array) returned from RPC
	 */
	public void onFinished(JavaScriptObject jso) {

		// adds members and admins
		retrieveAdminsAndMembers();

		// updates table
		rawUpdateGroupTable(jso);

		session.getUiElements().setLogText("Loading groups in selected VO finished.");
		loaderImage.loadingFinished();
		this.events.onFinished(jso);
	}

	/**
	 * Sets custom events
	 *
	 * @param externalEvent external events
	 */
	public void setEvents(JsonCallbackEvents externalEvent) {
		events = externalEvent;
	}

	/**
	 * Sets, whether to check child groups when selecting
	 */
	public void setCheckChildGroups(boolean checkChildGroups){
		this.checkChildGroups = checkChildGroups;
	}

	/**
	 * Return list of items from table
	 */
	public ArrayList<Group> getList() {
		return list;
	}

	/**
	 * Sets whether core groups can be checked
	 * @param checkable
	 */
	public void setCoreGroupsCheckable(boolean checkable)
	{
		this.coreGroupsCheckable = checkable;
	}

}
