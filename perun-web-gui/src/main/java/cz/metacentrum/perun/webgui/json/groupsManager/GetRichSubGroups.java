package cz.metacentrum.perun.webgui.json.groupsManager;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableInfoCellWithImageResource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

/**
 * GroupsManager/getRichSubGroupsWithAttributesByNames Method
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class GetRichSubGroups implements JsonCallback, JsonCallbackTable<RichGroup>, JsonCallbackOracle<RichGroup> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Attributes which we want to get
	private ArrayList<String> attrNames;
	// Parent group id
	private int parentId;
	// Selection model
	final MultiSelectionModel<RichGroup> selectionModel = new MultiSelectionModel<RichGroup>(new GeneralKeyProvider<RichGroup>());
	// JSON URL
	static final private String JSON_URL = "groupsManager/getRichSubGroupsWithAttributesByNames";
	// Table data provider
	private ListDataProvider<RichGroup> dataProvider = new ListDataProvider<RichGroup>();
	// The table itself
	private PerunTable<RichGroup> table;
	// List in the table
	private ArrayList<RichGroup> list = new ArrayList<RichGroup>();
	// FIELD UPDATER - when user clicks on a row
	private FieldUpdater<RichGroup, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// oracle
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(":");
	private ArrayList<RichGroup> fullBackup = new ArrayList<RichGroup>();

	/**
	 * Creates a new instance of GroupsManager/getRichSubGroupsWithAttributesByNames method
	 *
	 * @param id Parent group id
	 */
	public GetRichSubGroups(int id, ArrayList<String> attrNames) {
		this.parentId = id;
		this.attrNames = attrNames;
	}

	/**
	 * Creates a new instance of GroupsManager/getRichSubGroupsWithAttributesByNames method with custom field updater
	 *
	 * @param id Parent group id
	 */
	public GetRichSubGroups(int id, JsonCallbackEvents events ) {
		this.parentId = id;
		this.events = events;
	}

	/**
	 * Retrieves data via RPC
	 */
	public void retrieveData() {
		String param = "group=" + this.parentId;

		if (!this.attrNames.isEmpty()) {
			// parse list
			for (String attrName : this.attrNames) {
				param += "&attrNames[]="+attrName;
			}
		}

		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Returns the table with subgroups and custom field updater
	 * @return
	 */
	public CellTable<RichGroup> getTable(FieldUpdater<RichGroup, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with subgroups.
	 * @return
	 */
	public CellTable<RichGroup> getTable() {

		// retrieve data
		retrieveData();

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
		loaderImage.setEmptyResultMessage("Group has no sub-groups.");

		// checkbox column column
		table.addCheckBoxColumn();
		table.addIdColumn("Group ID", tableFieldUpdater);

		// Add a synchronization clicable icon column.
		Column<RichGroup, RichGroup> syncColumn = new Column<RichGroup, RichGroup>(
				new CustomClickableInfoCellWithImageResource("click")) {
			@Override
			public RichGroup getValue(RichGroup object) {
				return object;
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
		syncColumn.setFieldUpdater(new FieldUpdater<RichGroup, RichGroup>() {
			@Override
			public void update(int index, final RichGroup object, RichGroup value) {

				GetEntityById get = new GetEntityById(PerunEntity.RICH_GROUP, object.getId(), new JsonCallbackEvents() {

					@Override
					public void onFinished(JavaScriptObject jso) {

						final RichGroup object = jso.cast();

						String name, syncEnabled, syncInterval, syncTimestamp, syncSuccessTimestamp, syncState, authGroup;
						name = object.getName();
						if (object.isSyncEnabled()) {
							syncEnabled = "enabled";
						} else {
							syncEnabled = "disabled";
						}
						if (object.getSynchronizationInterval() == null) {
							syncInterval = "N/A";
						} else {

							if (JsonUtils.checkParseInt(object.getSynchronizationInterval())) {
								int time = Integer.parseInt(object.getSynchronizationInterval()) * 5 / 60;
								if (time == 0) {
									time = Integer.parseInt(object.getSynchronizationInterval()) * 5;
									syncInterval = time + " minute(s)";
								} else {
									syncInterval = time + " hour(s)";
								}
							} else {
								syncInterval = object.getSynchronizationInterval();
							}

						}
						if (object.getLastSynchronizationState() == null) {
							if (object.getLastSuccessSynchronizationTimestamp() != null) {
								syncState = "OK";
							} else {
								syncState = "Not synced yet";
							}
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
							syncTimestamp = object.getLastSynchronizationTimestamp().split("\\.")[0];
						}
						if (object.getLastSuccessSynchronizationTimestamp() == null) {
							syncSuccessTimestamp = "N/A";
						} else {
							syncSuccessTimestamp = object.getLastSuccessSynchronizationTimestamp().split("\\.")[0];
						}
						if (Objects.equals(object.getAuthoritativeGroup(),"1")) {
							authGroup = "Yes";
						} else {
							authGroup = "No";
						}

						String html = "Group name: <b>"+ SafeHtmlUtils.fromString(name).asString()+"</b><br>";
						html += "Synchronization: <b>"+SafeHtmlUtils.fromString(syncEnabled).asString()+"</b><br>";

						if (object.isSyncEnabled()) {
							html += "Last sync. state: <b>"+SafeHtmlUtils.fromString(syncState).asString()+"</b><br>";
							html += "Last sync. timestamp: <b>"+SafeHtmlUtils.fromString(syncTimestamp).asString()+"</b><br>";
							html += "Last successful sync. timestamp: <b>"+SafeHtmlUtils.fromString(syncSuccessTimestamp).asString()+"</b><br>";
							html += "Sync. Interval: <b>"+SafeHtmlUtils.fromString(syncInterval).asString()+"</b><br>";
							html += "Authoritative group: <b>"+SafeHtmlUtils.fromString(authGroup).asString()+"</b><br>";
						}

						FlexTable layout = new FlexTable();

						layout.setWidget(0, 0, new HTML("<p>" + new Image(LargeIcons.INSTANCE.informationIcon())));
						layout.setHTML(0, 1, "<p style=\"line-height: 1.2;\">" + html);

						layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

						final CustomButton okButton = new CustomButton("Force synchronization", SmallIcons.INSTANCE.arrowRefreshIcon());
						okButton.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								ForceGroupSynchronization call = new ForceGroupSynchronization(JsonCallbackEvents.disableButtonEvents(okButton));
								call.synchronizeGroup(object.getId());
							}
						});
						if (!session.isVoAdmin(object.getVoId()) && !session.isGroupAdmin(object.getId())) okButton.setEnabled(false);
						okButton.setVisible(object.isSyncEnabled());

						final Confirm c = new Confirm("Group synchronization info", layout, okButton, null, true);
						c.setHideOnButtonClick(false);
						c.setCancelIcon(SmallIcons.INSTANCE.acceptIcon());
						c.setCancelButtonText("OK");
						c.setCancelClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								c.hide();
							}
						});

						c.show();

					}
				});
				get.retrieveData();

			};
		});

		syncColumn.setSortable(true);
		columnSortHandler.setComparator(syncColumn, new Comparator<RichGroup>() {
			@Override
			public int compare(RichGroup o1, RichGroup o2) {
				if (o1 != null && o2 != null) {

					int o1val = 0;
					int o2val = 0;

					if (o1.isSyncEnabled()) o1val = 5;
					if (o2.isSyncEnabled()) o2val = 5;

					if (Objects.equals(o1.getAuthoritativeGroup(),"1")) o1val = o1val + 3;
					if (Objects.equals(o2.getAuthoritativeGroup(),"1")) o2val = o2val + 3;

					return o1val - o2val;
				}
				return 0;
			}
		});

		table.addColumn(syncColumn, "Sync");
		table.setColumnWidth(syncColumn, "70px");

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
	 * @param object Group to be added as new row
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
		setList(JsonUtils.<RichGroup>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Sub-Groups loaded: " + list.size());
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
		// TODO Auto-generated method stub
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
