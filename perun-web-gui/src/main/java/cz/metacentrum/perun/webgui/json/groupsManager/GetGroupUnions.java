package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonCallbackOracle;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;

import java.util.ArrayList;

/**
 * @author Michal Krajcovic <mkrajcovic@mail.muni.cz>
 */
public class GetGroupUnions implements JsonCallback, JsonCallbackTable<Group>, JsonCallbackOracle<Group> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Selection model
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<>(new GeneralKeyProvider<Group>());
	// JSON URL
	static final private String JSON_URL = "groupsManager/getGroupUnions";
	// Table data provider
	private ListDataProvider<Group> dataProvider = new ListDataProvider<>();
	// The table itself
	private PerunTable<Group> table;
	// List in the table
	private ArrayList<Group> list = new ArrayList<>();
	// FIELD UPDATER - when user clicks on a row
	private FieldUpdater<Group, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(":");
	private ArrayList<Group> fullBackup = new ArrayList<>();
	// Parent group
	private Group group;
	// reverseDirection
	private boolean reverse = false;
	// show subgroups
	private boolean showSubgroups = false;


	/**
	 * Creates a new instance of GroupsManager/getSubGroups method
	 *
	 * @param group Parent group
	 */
	public GetGroupUnions(Group group, boolean reverse) {
		this.group = group;
		this.reverse = reverse;
	}

	/**
	 * Creates a new instance of GroupsManager/getSubGroups method with custom field updater
	 *
	 * @param group Parent group
	 */
	public GetGroupUnions(Group group, boolean reverse, JsonCallbackEvents events) {
		this(group, reverse);
		this.events = events;
	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Group>jsoAsList(jso));
		filterTable("");
		session.getUiElements().setLogText("Group unions loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	@Override
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Group unions");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Group unions started.");
		events.onLoadingStart();
	}

	@Override
	public void retrieveData() {
		final String param = "group=" + this.group.getId() + "&reverseDirection=" + reverse;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	@Override
	public void clearTable() {
		loaderImage.loadingStart();
		list.clear();
		fullBackup.clear();
		oracle.clear();
		selectionModel.clear();
		dataProvider.flush();
		dataProvider.refresh();
	}

	@Override
	public void insertToTable(int index, Group object) {
		list.add(index, object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	@Override
	public void addToTable(Group object) {
		list.add(object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	@Override
	public void removeFromTable(Group object) {
		list.remove(object);
		selectionModel.getSelectedSet().remove(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	@Override
	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setCheckable(boolean checkable) {
		// TODO Auto-generated method stub
	}

	@Override
	public void clearTableSelectedSet() {
		selectionModel.clear();
	}

	@Override
	public ArrayList<Group> getTableSelectedList() {
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	@Override
	public void setList(ArrayList<Group> list) {
		clearTable();
		this.list.addAll(list);
		for (Group g : list) {
			oracle.add(g.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	@Override
	public ArrayList<Group> getList() {
		return this.list;
	}

	@Override
	public CellTable<Group> getTable() {
		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Group>(list);

		// Cell table
		table = new PerunTable<>(list);
		table.setHyperlinksAllowed(false);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ColumnSortEvent.ListHandler<Group> columnSortHandler = new ColumnSortEvent.ListHandler<>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Group>createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Group has no unions.");

		// checkbox column column
		Column<Group, Group> checkBoxColumn = new Column<Group, Group>(
				new PerunCheckboxCell<Group>(true, false, true)) {
			@Override
			public Group getValue(Group object) {
				// Get the value from the selection model.
				GeneralObject go = object.cast();
				go.setChecked(selectionModel.isSelected(object));
				return go.cast();
			}
		};

		// updates the columns size
		table.setColumnWidth(checkBoxColumn, 40.0, Style.Unit.PX);

		// Add the columns

		// Checkbox column header
		CheckboxCell cb = new CheckboxCell();
		Header<Boolean> checkBoxHeader = new Header<Boolean>(cb) {
			public Boolean getValue() {
				return false;//return true to see a checked checkbox.
			}
		};
		checkBoxHeader.setUpdater(new ValueUpdater<Boolean>() {
			public void update(Boolean value) {
				// sets selected to all, if value = true, unselect otherwise
				for(Group obj : list){
					selectionModel.setSelected(obj, value);
				}
			}
		});

		table.addColumn(checkBoxColumn, checkBoxHeader);

		table.addIdColumn("Group ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;
	}

	/**
	 * Sets reverse value and retrieves new data accordingly
	 *
	 * @param reverse
	 */
	public void setReverseAndRefresh(boolean reverse) {
		this.reverse = reverse;
		retrieveData();
	}

	/**
	 * Returns the table with subgroups and custom field updater
	 *
	 * @return
	 */
	public CellTable<Group> getTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}


	@Override
	public void filterTable(String text) {

		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			fullBackup.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (text.equalsIgnoreCase("") && showSubgroups) {
			list.addAll(fullBackup);
		} else {
			for (Group grp : fullBackup) {
				if (!showSubgroups && grp.getName().startsWith(group.getName() + ":")) {
					continue;
				}
				// store facility by filter
				if (text.equalsIgnoreCase("")) {
					list.add(grp);
				} else if (grp.getName().toLowerCase().startsWith(text.toLowerCase()) ||
						grp.getName().toLowerCase().contains(":" + text.toLowerCase())) {
					list.add(grp);
				}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No Group unions matching '" + text + "' found.");
		} else {
			loaderImage.setEmptyResultMessage("Group has no unions.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();
	}

	public void setShowSubgroupsAndRefresh(boolean showSubgroups, String filter) {
		this.showSubgroups = showSubgroups;
		filterTable(filter);
	}

	@Override
	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	@Override
	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	public boolean isReverse() {
		return reverse;
	}

	/**
	 * Sets reverse value, does not retrieve new data after
	 *
	 * @param reverse
	 */
	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

}
