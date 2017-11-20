package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunStatus;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.columnProviders.IsClickableCell;
import cz.metacentrum.perun.webgui.json.columnProviders.MemberColumnProvider;
import cz.metacentrum.perun.webgui.json.keyproviders.RichMemberKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;

import java.util.ArrayList;

/**
 * Ajax query to get all RichMembers of VO / Group with list of selected attributes
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetCompleteRichMembers implements JsonCallback, JsonCallbackTable<RichMember>, JsonCallbackOracle<RichMember> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	private static final String JSON_URL = "membersManager/getCompleteRichMembers";
	// VO ID
	private PerunEntity entity;
	private int entityId;
	private ArrayList<String> attributes = new ArrayList<String>();

	// Selection model for the table
	final MultiSelectionModel<RichMember> selectionModel = new MultiSelectionModel<RichMember>(new RichMemberKeyProvider());
	// Table data provider.
	private ListDataProvider<RichMember> dataProvider = new ListDataProvider<RichMember>();
	// Cell table
	private PerunTable<RichMember> table;
	// List of members
	private ArrayList<RichMember> list = new ArrayList<RichMember>();
	// Table field updater
	private FieldUpdater<RichMember, RichMember> tableFieldUpdater;
	private boolean checkable = true;
	private boolean indirectCheckable = true; // for backward compatibility TRUE
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	// oracle
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private ArrayList<RichMember> backupList = new ArrayList<RichMember>();

	private ArrayList<PerunStatus> allowedStatuses = new ArrayList<PerunStatus>();

	/**
	 * Creates a new instance of the method
	 *
	 * @param entity PerunEntity (VO/Group)
	 * @param id entityId
	 * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
	 */
	public GetCompleteRichMembers(PerunEntity entity, int id, ArrayList<String> attributes) {
		this.entity = entity;
		this.entityId = id;
		// if null use default
		if (attributes == null) {
			this.attributes = JsonUtils.getAttributesListForMemberTables();
		} else {
			this.attributes = attributes;
		}
		// by default load without "disabled"
		for (PerunStatus s : PerunStatus.values()) {
			if (!s.equals(PerunStatus.DISABLED) && !s.equals(PerunStatus.EXPIRED)) {
				allowedStatuses.add(s);
			}
		}
	}

	/**
	 * Creates a new instance of the method
	 *
	 * @param entity PerunEntity (VO/Group)
	 * @param id entityId
	 * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
	 * @param events external events
	 */
	public GetCompleteRichMembers(PerunEntity entity, int id, ArrayList<String> attributes, JsonCallbackEvents events) {
		this(entity, id, attributes);
		this.events = events;
	}

	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData() {

		String param = "";
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			param = "vo=" + entityId;
		} else if (PerunEntity.GROUP.equals(entity)) {
			param = "group=" + entityId+"&lookingInParentGroup=0";
		} else if (PerunEntity.GROUP_PARENT.equals(entity)) {
			param = "group=" + entityId+"&lookingInParentGroup=1";
		}
		if (!attributes.isEmpty()) {
			// parse lists
			for (String attribute : attributes) {
				param += "&attrsNames[]=" + attribute;
			}
		}
		if (!allowedStatuses.isEmpty()) {
			// parse list
			for (PerunStatus s : allowedStatuses) {
				param += "&allowedStatuses[]="+s.toString();
			}
		}

		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);

	}

	/**
	 * Returns empty table
	 *
	 * @param up Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getEmptyTable(FieldUpdater up) {
		this.tableFieldUpdater = up;
		return this.getEmptyTable();
	}

	/**
	 * Returns the table with member-users
	 *
	 * @param up Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getTable(FieldUpdater up) {
		retrieveData();
		this.tableFieldUpdater = up;
		return this.getEmptyTable(up);
	}

	/**
	 * Returns the table with member-users
	 *
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getTable() {
		retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns empty table
	 *
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getEmptyTable() {

		// Table data provider.
		dataProvider = new ListDataProvider<RichMember>(list);

		// Cell table
		table = new PerunTable<RichMember>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<RichMember> columnSortHandler = new ListHandler<RichMember>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichMember> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			loaderImage.setEmptyResultMessage("VO has no members.");
		} else {
			loaderImage.setEmptyResultMessage("Group has no members.");
		}

		Column<RichMember, RichMember> checkBoxColumn = new Column<RichMember, RichMember>(
				new PerunCheckboxCell<RichMember>(true, false, indirectCheckable)) {
			@Override
			public RichMember getValue(RichMember object) {
				// Get the value from the selection model.
				object.setChecked(selectionModel.isSelected(object));
				return object;
			}
		};

		// updates the columns size
		table.setColumnWidth(checkBoxColumn, 40.0, Style.Unit.PX);

		// Add the columns

		// Checkbox column header
		CheckboxCell cb = new CheckboxCell();
		Header<Boolean> checkBoxHeader = new Header<Boolean>(cb) {
			public Boolean getValue() {
				return false; //return true to see a checked checkbox.
			}
		};
		checkBoxHeader.setUpdater(new ValueUpdater<Boolean>() {
			public void update(Boolean value) {
				// sets selected to all, if value = true, unselect otherwise
				for(RichMember obj : list){
					if (!"INDIRECT".equalsIgnoreCase(obj.getMembershipType())) {
						selectionModel.setSelected(obj, value);
					}
				}
			}
		});

		if (checkable) {
			table.addColumn(checkBoxColumn,checkBoxHeader);
		}

		MemberColumnProvider columnProvider = new MemberColumnProvider(dataProvider, backupList, table, tableFieldUpdater);
		IsClickableCell<RichMember> authz = new IsClickableCell<RichMember>() {
			@Override
			public boolean isClickable(RichMember object) {
				return true;
			}

			@Override
			public String linkUrl(RichMember object) {
				return null;
			}
		};

		columnProvider.addIdColumn(authz, 110);
		columnProvider.addUserIdColumn(authz, 110);
		columnProvider.addStatusColumn(authz, 20);
		columnProvider.addNameColumn(authz);
		columnProvider.addOrganizationColumn(authz);
		columnProvider.addEmailColumn(authz);
		columnProvider.addLoginsColumn(authz);

		return table;

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
		oracle.add(object.getUser().getFullName());
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
		backupList.clear();
		list.clear();
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
		setList(new TableSorter<RichMember>().sortByName(JsonUtils.<RichMember>jsoAsList(jso)));
		session.getUiElements().setLogText("Members loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, RichMember object) {
		list.add(index, object);
		oracle.add(object.getUser().getFullName());
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
		for (RichMember m : list) {
			oracle.add(m.getUser().getFullName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<RichMember> getList() {
		return this.list;
	}

	/**
	 * Helper method to set events later
	 * @param externalEvents
	 */
	public void setEvents(JsonCallbackEvents externalEvents) {
		events = externalEvents;
	}

	@Override
	public void filterTable(String filter) {

		// save backup for the first time
		if (backupList.isEmpty() || backupList == null) {
			backupList.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		// filter table content
		if (filter.equalsIgnoreCase("")) {
			list.addAll(backupList);
		} else {
			for (RichMember m : backupList){
				// store member by filter
				if (m.getUser().getFullName().toLowerCase().startsWith(filter.toLowerCase())) {
					list.add(m);
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No member matching '"+filter+"' found.");
		} else {
			if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
				loaderImage.setEmptyResultMessage("VO has no members.");
			} else {
				loaderImage.setEmptyResultMessage("Group has no members.");
			}
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	@Override
	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	@Override
	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	/**
	 * Exclude disabled members from showing in table
	 * Table must be manually cleared and loaded afterwards !!
	 *
	 * @param exclude TRUE for exclude / FALSE to keep
	 */
	public void excludeDisabled(boolean exclude) {

		allowedStatuses.clear();
		for (PerunStatus s : PerunStatus.values()) {
			if (exclude && (s.equals(PerunStatus.DISABLED) || s.equals(PerunStatus.EXPIRED))) {
				continue;
			} else {
				allowedStatuses.add(s);
			}
		}

	}

	public void setIndirectCheckable(boolean checkable) {
		this.indirectCheckable = checkable;
	}

	/**
	 * Get table selection model.
	 *
	 * @return table selection model
	 */
	public MultiSelectionModel<RichMember> getSelectionModel() {
		return this.selectionModel;
	}

}
