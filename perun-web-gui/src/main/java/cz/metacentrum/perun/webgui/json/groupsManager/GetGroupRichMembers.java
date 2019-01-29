package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.comparators.RichMemberComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.RichMemberKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.memberstabs.ChangeStatusTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.PerunStatusCell;

import java.util.ArrayList;

/**
 * Ajax query to get group rich members
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetGroupRichMembers implements JsonCallback, JsonCallbackTable<RichMember> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// grou ID
	private int groupId;
	// JSON URL
	private static final String JSON_URL = "groupsManager/getGroupRichMembers";
	// Selection model for the table
	final MultiSelectionModel<RichMember> selectionModel = new MultiSelectionModel<RichMember>(new RichMemberKeyProvider());
	// Table data provider.
	private ListDataProvider<RichMember> dataProvider = new ListDataProvider<RichMember>();
	// Cell table
	private PerunTable<RichMember> table;
	// List of members
	private ArrayList<RichMember> list = new ArrayList<RichMember>();
	// Table field updater
	private FieldUpdater<RichMember, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	/**
	 * Creates a new instance of the method
	 *
	 * @param id Group id
	 */
	public GetGroupRichMembers(int id) {
		this.groupId = id;
	}

	/**
	 * Creates a new instance of the method
	 *
	 * @param id Group id
	 * @param events external events
	 */
	public GetGroupRichMembers(int id, JsonCallbackEvents events) {
		this.groupId = id;
		this.events = events;
	}

	/**
	 * Returns the table with rich members of group
	 *
	 * @param fu Custom field updater
	 * @return table widget
	 */
	public CellTable<RichMember> getTable(FieldUpdater<RichMember, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with rich members of group
	 *
	 * @return table widget
	 */
	public CellTable<RichMember> getTable()
	{
		// retrieves data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<RichMember>(list);

		// Cell table
		table = new PerunTable<RichMember>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<RichMember> columnSortHandler = new ListHandler<RichMember>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// Table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichMember> createCheckboxManager());

		// Set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// Checkbox column column
		table.addCheckBoxColumn();

		// Status column
		Column<RichMember, String> statusColumn = new Column<RichMember, String>(
				new PerunStatusCell()) {
			@Override
			public String getValue(RichMember object) {
				return object.getStatus();
			}
		};
		// own onClick tab for changing member's status
		statusColumn.setFieldUpdater(new FieldUpdater<RichMember,String>(){
			public void update(int index, final RichMember object, String value) {
				PerunWebSession.getInstance().getTabManager().addTabToCurrentTab(new ChangeStatusTabItem(object.cast(), new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						clearTable();
 						retrieveData();
					}
				}));
			}
		});
		// status column sortable
		statusColumn.setSortable(true);
		columnSortHandler.setComparator(statusColumn, new GeneralComparator<RichMember>(GeneralComparator.Column.STATUS));

		table.addColumn(statusColumn, "Status");
		table.setColumnWidth(statusColumn, 20, Unit.PX);

		// Create member ID column.
		Column<RichMember, String> memberIdColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichMember, String>() {
					public String getValue(RichMember object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);

		// Create User ID column.
		Column<RichMember, String> userIdColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichMember, String>() {
					public String getValue(RichMember object) {
						return String.valueOf(object.getUser().getId());
					}
				}, this.tableFieldUpdater);

		columnSortHandler.setComparator(memberIdColumn, new RichMemberComparator(RichMemberComparator.Column.MEMBER_ID));
		memberIdColumn.setSortable(true);


		userIdColumn.setSortable(true);
		columnSortHandler.setComparator(userIdColumn,  new RichMemberComparator(RichMemberComparator.Column.USER_ID));

		table.setColumnWidth(memberIdColumn, 110.0, Unit.PX);
		table.setColumnWidth(userIdColumn, 110.0, Unit.PX);

		// headers
		if(JsonUtils.isExtendedInfoVisible()){
			table.addColumn(memberIdColumn, "Member ID");
			table.addColumn(userIdColumn,  "User ID");
		}

		table.addNameColumn(tableFieldUpdater);

		return table;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		final String param = "group=" + this.groupId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<RichMember>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(RichMember object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(RichMember object) {
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
	public ArrayList<RichMember> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Members.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Members started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<RichMember>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Members loaded: " + list.size());
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
		// TODO Auto-generated method stub
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

}
