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
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
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
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Ajax query to find all RichMembers of VO / Group by searchString with list of selected attributes
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FindCompleteRichMembers implements JsonCallbackSearchFor, JsonCallbackTable<RichMember> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	private static final String JSON_URL = "membersManager/findCompleteRichMembers";
	// VO ID
	private PerunEntity entity;
	private int entityId;
	private ArrayList<String> attributes = new ArrayList<String>();
	private String searchString;

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
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, WidgetTranslation.INSTANCE.emptySearchForMembers());

	private ArrayList<PerunStatus> allowedStatuses = new ArrayList<PerunStatus>();

	/**
	 * Creates a new instance of the method
	 *
	 * @param entity PerunEntity (VO/Group)
	 * @param id entityId
	 * @param searchString name,login or email of member to look for (can't be empty)
	 * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
	 */
	public FindCompleteRichMembers(PerunEntity entity, int id, String searchString, ArrayList<String> attributes) {
		this.entity = entity;
		this.entityId = id;
		this.searchString = searchString;
		// if null use default
		if (attributes == null) {
			this.attributes = JsonUtils.getAttributesListForMemberTables();
		} else {
			this.attributes = attributes;
		}
		allowedStatuses.addAll(Arrays.asList(PerunStatus.values()));
	}

	/**
	 * Creates a new instance of the method
	 *
	 * @param entity PerunEntity (VO/Group)
	 * @param id entityId
	 * @param searchString name,login or email of member to look for (can't be empty)
	 * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
	 * @param events external events
	 */
	public FindCompleteRichMembers(PerunEntity entity, int id, String searchString, ArrayList<String> attributes, JsonCallbackEvents events) {
		this(entity, id, searchString, attributes);
		this.events = events;
	}

	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData() {

		if (searchString == null || searchString.isEmpty()) return;

		String param = "";
		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			param = "vo=" + entityId+"&searchString=" + this.searchString;
		} else if (PerunEntity.GROUP.equals(entity)) {
			param = "group=" + entityId+"&lookingInParentGroup=0&searchString=" + this.searchString;
		}  else if (PerunEntity.GROUP_PARENT.equals(entity)) {
			param = "group=" + entityId+"&lookingInParentGroup=1&searchString=" + this.searchString;
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

	public void searchFor(String searchString) {
		if (searchString != null && !searchString.isEmpty()) {
			loaderImage.setEmptyResultMessage("No member matching '"+searchString+"' found.");
			clearTable();
			this.searchString = searchString;
			retrieveData();
		}
	}

	/**
	 * Returns the table with RichMembers
	 *
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getEmptyTable(FieldUpdater<RichMember, RichMember> fu) {
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	/**
	 * Returns the table with RichMembers and start call
	 *
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getTable(FieldUpdater<RichMember, RichMember> fu) {
		this.retrieveData();
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	/**
	 * Return table with RichMembers and start call
	 *
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getTable() {

		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns table with RichMembers
	 *
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getEmptyTable() {

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
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichMember> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		loaderImage.setEmptyResultMessage("No member found.");

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

		MemberColumnProvider columnProvider = new MemberColumnProvider(dataProvider, null, table, tableFieldUpdater);
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
		searchString = "";
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
		clearTable();
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
	 * Helper method to set events later
	 * @param externalEvents
	 */
	public void setEvents(JsonCallbackEvents externalEvents) {
		events = externalEvents;
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

	/**
	 * Get table selection model.
	 *
	 * @return table selection model
	 */
	public MultiSelectionModel<RichMember> getSelectionModel() {
		return this.selectionModel;
	}

	/**
	 * Set custom message to empty table.
	 *
	 * If message is null, then default text is used.
	 *
	 * @param message message to set
	 */
	public void setCustomEmptyTableMessage(String message) {
		if (message != null) {
			loaderImage = new AjaxLoaderImage(true, message);
		} else {
			loaderImage = new AjaxLoaderImage(true, WidgetTranslation.INSTANCE.emptySearchForMembers());
		}
	}

	public void setIndirectCheckable(boolean checkable) {
		this.indirectCheckable = checkable;
	}

}
