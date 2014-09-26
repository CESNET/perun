package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableInfoCellWithImageResource;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;

import java.util.ArrayList;

/**
 * Ajax query to get all groups in VO
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class GetAllRichGroups implements JsonCallback, JsonCallbackTable<RichGroup>, JsonCallbackOracle<RichGroup> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO id
	private int voId;
	// attribute names which we want to get
	private ArrayList<String> attrNames;
	// JSON URL
	static private final String JSON_URL = "groupsManager/getAllRichGroupsWithAttributesByNames";
	// Selection model
	final MultiSelectionModel<RichGroup> selectionModel = new MultiSelectionModel<RichGroup>(new GeneralKeyProvider<RichGroup>());
	// RichGroups table data provider
	private ListDataProvider<RichGroup> dataProvider = new ListDataProvider<RichGroup>();
	// RichGroups table
	private PerunTable<RichGroup> table;
	// RichGroups table list
	private ArrayList<RichGroup> list = new ArrayList<RichGroup>();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<RichGroup, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(":");
	private ArrayList<RichGroup> fullBackup = new ArrayList<RichGroup>();
	// checkable core groups
	private boolean coreGroupsCheckable = false;
	private boolean checkable = true;

	/**
	 * Creates a new callback
	 *
	 * @param id ID of VO for which we want groups for
	 * @param attrNames Attribute names which we want to get
	 */
	public GetAllRichGroups(int id, ArrayList<String> attrNames) {
		this.voId = id;
		this.attrNames = attrNames;
	}

	/**
	 * Creates a new callback
	 *
	 * @param id ID of VO for which we want groups for
	 * @param events external events
	 */
	public GetAllRichGroups(int id, JsonCallbackEvents events) {
		this.voId = id;
		this.events = events;
	}

	/**
	 * Returns table with groups in hierarchical structure and with custom field updater
	 *
	 * @param fu Custom field updater
	 * @return table widget
	 */
	public CellTable<RichGroup> getTable(FieldUpdater<RichGroup, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	public CellTable<RichGroup> getEmptyTable(FieldUpdater<RichGroup, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	public CellTable<RichGroup> getTable() {
		// retrieve data
		retrieveData();
		return getEmptyTable();
	}

	/**
	 * Returns table with groups in hierarchical structure and with custom field updater
	 *
	 * @return table widget
	 */
	public CellTable<RichGroup> getEmptyTable() {

		// Table data provider.
		dataProvider = new ListDataProvider<RichGroup>(list);

		// Cell table
		table = new PerunTable<RichGroup>(list);

		table.setHyperlinksAllowed(false);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<RichGroup> columnSortHandler = new ListHandler<RichGroup>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichGroup> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		if (!session.isVoAdmin(voId)) {
			loaderImage.setEmptyResultMessage("You are not manager of any group in this VO.");
		} else {
			loaderImage.setEmptyResultMessage("VO has no groups.");
		}

		Column<RichGroup, RichGroup> checkBoxColumn = new Column<RichGroup, RichGroup>(
				new PerunCheckboxCell<RichGroup>(true, false, coreGroupsCheckable)) {
			@Override
			public RichGroup getValue(RichGroup object) {
				// Get the value from the selection model.
				object.setChecked(selectionModel.isSelected(object));
				return object;
			}
		};


		// updates the columns size
		table.setColumnWidth(checkBoxColumn, 40.0, Unit.PX);

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
				for(RichGroup obj : list){
					if (!obj.isCoreGroup()) {
						selectionModel.setSelected(obj, value);
					}
				}
			}
		});

		if (checkable) {
			table.addColumn(checkBoxColumn,checkBoxHeader);
		}

		table.addIdColumn("Group ID", tableFieldUpdater);

		// Add a synchronization clicable icon column.
		Column<RichGroup, ImageResource> syncColumn = new Column<RichGroup, ImageResource>(
				new CustomClickableInfoCellWithImageResource("click")) {
			@Override
			public ImageResource getValue(RichGroup object) {
				if (object.isSyncEnabled()) {
					if (object.getLastSynchronizationState().equals("OK")) {
						return SmallIcons.INSTANCE.bulletGreenIcon();
					} else {
						return SmallIcons.INSTANCE.bulletRedIcon();
					}
				} else {
					return SmallIcons.INSTANCE.bulletWhiteIcon();
				}
			}
			@Override
			public String getCellStyleNames(Cell.Context context, RichGroup object) {
				if (tableFieldUpdater != null) {
					return super.getCellStyleNames(context, object) + " pointer image-hover";
				} else {
					return super.getCellStyleNames(context, object);
				}
			}
		};
		syncColumn.setFieldUpdater( new FieldUpdater<RichGroup, ImageResource>() {
			@Override
			public void update(int index, RichGroup object, ImageResource value) {
				String name, syncEnabled, syncInterval, syncTimestamp, syncState, authGroup;
				name = object.getName();
				if (object.isSyncEnabled()) {
					syncEnabled = "enabled";
				} else {
					syncEnabled = "disabled";
				}
				if (object.getSynchronizationInterval() == null) {
					syncInterval = "N/A";
				} else {
					syncInterval = object.getSynchronizationInterval() + " hour(s)";
				}
				if (object.getLastSynchronizationState().equals("OK")) {
					syncState = "OK";
				} else {
					if (session.isPerunAdmin()) {
						syncState = object.getLastSynchronizationState();
					} else {
						syncState = "Internal Error";
					}
				}
				if (object.getLastSynchronizationTimestamp() == null) {
					syncTimestamp = "N/A";
				} else {
					syncTimestamp = object.getLastSynchronizationTimestamp().split(".")[0];
				}
				if (object.getAuthoritativeGroup() != null && object.getAuthoritativeGroup().equals("1")) {
					authGroup = "Yes";
				} else {
					authGroup = "No";
				}

				String html = "Group name: <b>"+name+"</b><br>";
				html += "Synchronization: <b>"+syncEnabled+"</b><br>";
				if (object.isSyncEnabled()) {
					html += "Last sync. state: <b>"+syncState+"</b><br>";
					html += "Last sync. timestamp: <b>"+syncTimestamp+"</b><br>";
					html += "Sync. Interval: <b>"+syncInterval+"</b><br>";
					html += "Authoritative group: <b>"+authGroup+"</b><br>";
				}
				UiElements.generateInfo("Group synchronization info", html);
			};
		});
		table.addColumn(syncColumn, "Sync");
		table.setColumnWidth(syncColumn, "30px");

		// set row styles based on: isCoreGroup()
		table.setRowStyles(new RowStyles<RichGroup>(){
			public String getStyleNames(RichGroup row, int rowIndex) {
				if (row.isCoreGroup()) {
					return "bold";
				}
				return "";
			}
		});

		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		return table;
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<RichGroup>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object RichGroup to be added as new row
	 */
	public void addToTable(RichGroup object) {
		list.add(object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object RichGroup to be removed as row
	 */
	public void removeFromTable(RichGroup object) {
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
	public ArrayList<RichGroup> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading all groups.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading all groups started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		clearTable();
		for (RichGroup g : JsonUtils.<RichGroup>jsoAsList(jso)) {
			if (g.isCoreGroup()) {
				insertToTable(0, g);
			} else {
				addToTable(g);
			}
		}
		//sortTable(); groups are sorted from RPC
		session.getUiElements().setLogText("Groups loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, RichGroup object) {
		list.add(index, object);
		oracle.add(object.getName());
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<RichGroup> list) {
		clearTable();
		this.list.addAll(list);
		for (RichGroup g : list) {
			oracle.add(g.getName());
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<RichGroup> getList() {
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
			for (RichGroup grp : fullBackup){
				// store facility by filter
				if (grp.getName().toLowerCase().startsWith(text.toLowerCase()) ||
						grp.getName().toLowerCase().contains(":"+text.toLowerCase())) {
					list.add(grp);
				}
			}
		}

		if (list.isEmpty() && !text.isEmpty()) {
			loaderImage.setEmptyResultMessage("No group matching '"+text+"' found.");
		} else {
			if (!session.isVoAdmin(voId)) {
				loaderImage.setEmptyResultMessage("You are not manager of any group in this VO.");
			} else {
				loaderImage.setEmptyResultMessage("VO has no groups.");
			}
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	public void retrieveData() {
		String param = "vo="+this.voId;

		if (!this.attrNames.isEmpty()) {
			// parse list
			for (String attrName : this.attrNames) {
				param += "&attrNames[]="+attrName;
			}
		}

		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	public void setCoreGroupsCheckable(boolean checkable) {
		coreGroupsCheckable = checkable;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

	public void setVoId(int voId) {
		this.voId = voId;
	}

	public MultiSelectionModel<RichGroup> getSelectionModel() {
		return this.selectionModel;
	}

}
