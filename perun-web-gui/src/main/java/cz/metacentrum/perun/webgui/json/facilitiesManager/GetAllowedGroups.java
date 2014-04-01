package cz.metacentrum.perun.webgui.json.facilitiesManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
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
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get allowed groups on facility (filtered by VO or service)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetAllowedGroups implements JsonCallback, JsonCallbackTable<Group> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// IDs
	private int facilityId = 0;
	private int voId = 0;
	private int serviceId = 0;
	// JSON URL
	static private final String JSON_URL = "facilitiesManager/getAllowedGroups";
	// Selection model
	final MultiSelectionModel<Group> selectionModel = new MultiSelectionModel<Group>(new GeneralKeyProvider<Group>());
	// Groups table data provider
	private ListDataProvider<Group> dataProvider = new ListDataProvider<Group>();
	// Groups table
	private PerunTable<Group> table;
	// Groups table list
	private ArrayList<Group> list = new ArrayList<Group>();
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Group, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;
	private ArrayList<VirtualOrganization> vos = new ArrayList<VirtualOrganization>();

	/**
	 * Creates a new callback
	 *
	 * @param id ID of Facility for which we want groups for
	 */
	public GetAllowedGroups (int id) {
		this.facilityId = id;
	}

	/**
	 * Creates a new callback
	 *
	 * @param id ID of Facility for which we want groups for
	 * @param events external events
	 */
	public GetAllowedGroups (int id, JsonCallbackEvents events) {
		this.facilityId = id;
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

	public CellTable<Group> getEmptyTable(FieldUpdater<Group, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	public CellTable<Group> getTable() {
		// retrieve data
		retrieveData();
		return getEmptyTable();
	}

	/**
	 * Returns table with groups in hierarchical structure and with custom field updater
	 *
	 * @return table widget
	 */
	public CellTable<Group> getEmptyTable() {

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
		loaderImage.setEmptyResultMessage("No groups are allowed to access/use this facility.");

		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Group ID", tableFieldUpdater);
		table.addNameColumn(tableFieldUpdater);
		table.addDescriptionColumn(tableFieldUpdater);

		Column<Group, String> vosColumn = new Column<Group, String>(new CustomClickableTextCell()) {
			@Override
			public String getValue(Group group) {
				for (VirtualOrganization v : vos) {
					if (group.getVoId() == v.getId()) {
						return v.getName();
					}
				}
				return String.valueOf(group.getVoId());
			}
		};
		vosColumn.setFieldUpdater(tableFieldUpdater);
		columnSortHandler.setComparator(vosColumn, new Comparator<Group>() {
			@Override
			public int compare(Group o1, Group o2) {
				return o1.getVoId() - o2.getVoId();
			}
		});
		vosColumn.setSortable(true);

		table.addColumn(vosColumn, "Virtual organization");

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
		session.getUiElements().setLogErrorText("Error while loading allowed groups.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading allowed groups started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Group>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Groups loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Group object) {
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

	public void setList(ArrayList<Group> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Group> getList() {
		return this.list;
	}

	public void retrieveData() {

		String param = "facility=" + this.facilityId;
		if (voId != 0) {
			param = param+"&vo="+voId;
		}
		if (serviceId != 0) {
			param = param+"&service="+serviceId;
		}
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

	public void setVoId(int voId) {
		this.voId = voId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public void setVos(ArrayList<VirtualOrganization> vos){
		this.vos = vos;
	}

}
