package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunAppTypeCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Returns list of Group applications
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetApplicationsForGroup implements JsonCallback, JsonCallbackTable<Application>, JsonCallbackOracle<Application> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO id
	private int groupId;
	// JSON URL
	static private final String JSON_URL = "registrarManager/getApplicationsForGroup";
	// Selection model
	final MultiSelectionModel<Application> selectionModel = new MultiSelectionModel<Application>(
			new GeneralKeyProvider<Application>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table data provider
	private ListDataProvider<Application> dataProvider = new ListDataProvider<Application>();
	// Table itself
	private PerunTable<Application> table;
	// Table list
	private ArrayList<Application> list = new ArrayList<Application>();
	// Table field updater
	private FieldUpdater<Application, String> tableFieldUpdater;
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private String state = "";
	private boolean checkable = true;
	// oracle support
	private ArrayList<Application> backupList = new ArrayList<Application>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();

	/**
	 * Creates a new method instance
	 *
	 * @param id Group ID
	 */
	public GetApplicationsForGroup(int id) {
		this.groupId = id;
	}

	/**
	 * Creates a new method instance
	 * @param id VO ID
	 * @param events Custom events
	 */
	public GetApplicationsForGroup(int id, JsonCallbackEvents events) {
		this.groupId = id;
		this.events = events;
	}

	/**
	 * Returns the celltable with custom onclick
	 * @param fu Field updater
	 * @return
	 */
	public CellTable<Application> getTable(FieldUpdater<Application, String> fu)
	{
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns just the celltable
	 * @return
	 */
	public CellTable<Application> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Application>(list);

		// Cell table
		table = new PerunTable<Application>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Application> columnSortHandler = new ListHandler<Application>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Application> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No applications matching search criteria found for this group.");

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("App ID", tableFieldUpdater, 85);

		// DATE COLUMN
		Column<Application, String> dateColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Application, String>() {
					public String getValue(Application object) {
						// return only day
						return object.getCreatedAt().split(" ")[0];
					}
				}, tableFieldUpdater);

		dateColumn.setSortable(true);
		columnSortHandler.setComparator(dateColumn, new Comparator<Application>(){
			public int compare(Application arg0, Application arg1) {
				return arg0.getCreatedAt().compareToIgnoreCase(arg1.getCreatedAt());
			}
		});
		table.addColumn(dateColumn, "Created date");
		table.setColumnWidth(dateColumn, "120px");

		// Type column
		Column<Application, String> typeColumn = new Column<Application, String>(
				new PerunAppTypeCell()) {
			@Override
			public String getValue(Application object) {
				return object.getType();
			}
		};
		typeColumn.setFieldUpdater(tableFieldUpdater);
		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<Application>() {
			public int compare(Application arg0, Application arg1) {
				return (arg0.getType()).compareToIgnoreCase(arg1.getType());
			}
		});
		table.addColumn(typeColumn, "Type");
		table.setColumnWidth(typeColumn, "60px");

		// State column
		Column<Application, String> stateColumn = new Column<Application, String>(new CustomClickableTextCell()) {
			@Override
			public String getValue(Application object) {
				return object.getTranslatedState(object.getState());
			}
			@Override
			public String getCellStyleNames(Cell.Context context, Application object) {

				if ("NEW".equalsIgnoreCase(object.getState())) {
					return super.getCellStyleNames(context, object) + " rowgreen";
				} else if ("VERIFIED".equalsIgnoreCase(object.getState())) {
					return super.getCellStyleNames(context, object) + " rowyellow";
				} else if ("APPROVED".equalsIgnoreCase(object.getState())) {
					return super.getCellStyleNames(context, object) + " rowdarkgreen";
				} else if ("REJECTED".equalsIgnoreCase(object.getState())) {
					return super.getCellStyleNames(context, object) + " rowred";
				} else {
					return super.getCellStyleNames(context, object);
				}
			}
		};
		stateColumn.setFieldUpdater(tableFieldUpdater);
		stateColumn.setSortable(true);
		columnSortHandler.setComparator(stateColumn, new Comparator<Application>() {
			public int compare(Application arg0, Application arg1) {
				return (arg0.getTranslatedState(arg0.getState())).compareToIgnoreCase(arg1.getTranslatedState(arg1.getState()));
			}
		});
		table.addColumn(stateColumn, "State");
		table.setColumnWidth(stateColumn, "120px");

		Column<Application, String> extSourceColumn = JsonUtils.addColumn(
				new ClickableTextCell() {
					@Override
					public void render(
							com.google.gwt.cell.client.Cell.Context context,
							SafeHtml value, SafeHtmlBuilder sb) {
						if (value != null) {
							sb.appendHtmlConstant("<div class=\"customClickableTextCell\">");
							sb.append(value);
							sb.appendHtmlConstant("</div>");
						}
					}
				},
				new JsonUtils.GetValue<Application, String>() {
					public String getValue(Application object) {

						if (object.getUser() != null) {
							return object.getUser().getFullNameWithTitles();
						}
						return Utils.convertCertCN(object.getCreatedBy()) + " / " + Utils.translateIdp(Utils.convertCertCN(object.getExtSourceName()));
					}
				}, tableFieldUpdater);

		extSourceColumn.setSortable(true);
		columnSortHandler.setComparator(extSourceColumn, new Comparator<Application>(){
			public int compare(Application arg0, Application arg1) {
				String compare1 = "";
				String compare2 = "";
				if (arg0.getUser() != null) {
					compare1 = arg0.getUser().getFullName();
				} else {
					compare1 = Utils.convertCertCN(arg0.getCreatedBy()) + " / " + Utils.translateIdp(Utils.convertCertCN(arg0.getExtSourceName()));
				}
				if (arg1.getUser() != null) {
					compare2 = arg1.getUser().getFullName();
				} else {
					compare2 = Utils.convertCertCN(arg1.getCreatedBy()) + " / " + Utils.translateIdp(Utils.convertCertCN(arg1.getExtSourceName()));
				}
				return compare1.compareToIgnoreCase(compare2);
			}
		});
		table.addColumn(extSourceColumn, "Submitted by");

		Column<Application, String> loaColumn = JsonUtils.addColumn(
				new ClickableTextCell() {
					@Override
					public void render(
							com.google.gwt.cell.client.Cell.Context context,
							SafeHtml value, SafeHtmlBuilder sb) {
						if (value != null) {
							sb.appendHtmlConstant("<div class=\"customClickableTextCell\">");
							sb.append(value);
							sb.appendHtmlConstant("</div>");
						}
					}
				},
				new JsonUtils.GetValue<Application, String>() {
					public String getValue(Application object) {
						return String.valueOf(object.getExtSourceLoa());
					}
				}, tableFieldUpdater);

		loaColumn.setSortable(true);
		columnSortHandler.setComparator(loaColumn, new Comparator<Application>(){
			public int compare(Application arg0, Application arg1) {
				return arg0.getExtSourceLoa() - arg1.getExtSourceLoa();
			}
		});
		table.addColumn(loaColumn, "LoA");
		table.setColumnWidth(loaColumn, "40px");

		Column<Application, String> modifiedColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Application, String>() {
					public String getValue(Application object) {
						return Utils.convertCertCN(object.getModifiedBy());
					}
				}, tableFieldUpdater);

		table.addColumn(modifiedColumn, "Modified by");
		modifiedColumn.setSortable(true);
		columnSortHandler.setComparator(modifiedColumn, new Comparator<Application>(){
			public int compare(Application arg0, Application arg1) {
				return Utils.convertCertCN(arg0.getModifiedBy()).compareTo(Utils.convertCertCN(arg1.getModifiedBy()));
			}
		});

		table.setRowStyles(new RowStyles<Application>() {
			public String getStyleNames(Application application, int i) {
				if (application.getType().equalsIgnoreCase("INITIAL")) {
					return "rowlightgreen";
				} else {
					return "rowlightyellow";
				}
			}
		});

		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData()
	{
		String param = "group=" + this.groupId;

		if(state.length() != 0){
			for (String s : state.split(",")) {
				param += "&state[]=" + s;
			}
		}

		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}


	/**
	 * Sorts table by objects date
	 */
	public void sortTable() {
		list = new TableSorter<Application>().sortByIdReversed(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Resource to be added as new row
	 */
	public void addToTable(Application object) {
		list.add(object);
		if (object.getUser() != null) {
			oracle.add(object.getUser().getFullName());
		} else {
			oracle.add(Utils.convertCertCN(object.getCreatedBy()));
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Resource to be removed as row
	 */
	public void removeFromTable(Application object) {
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
		backupList.clear();
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
	public ArrayList<Application> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading Group applications.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Group applications started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Application>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Applications loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Application object) {
		list.add(index, object);
		if (object.getUser() != null) {
			oracle.add(object.getUser().getFullName());
		} else {
			oracle.add(Utils.convertCertCN(object.getCreatedBy()));
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<Application> list) {
		clearTable();
		this.list.addAll(list);
		for (Application a : list) {
			if (a.getUser() != null) {
				oracle.add(a.getUser().getFullName());
			} else {
				oracle.add(Utils.convertCertCN(a.getCreatedBy()));
			}
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Application> getList() {
		return this.list;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void filterTable(String filter){

		// store list only for first time
		if (backupList.isEmpty() || backupList == null) {
			backupList.addAll(list);
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (filter.equalsIgnoreCase("")) {
			list.addAll(backupList);
		} else {
			for (Application app : backupList){
				// store app by filter
				if (app.getUser() != null) {
					if (app.getUser().getFullName().toLowerCase().startsWith(filter.toLowerCase())) {
						list.add(app);
						continue;
					} else if (app.getUser().getLastName().toLowerCase().startsWith(filter.toLowerCase())) {
						list.add(app);
						continue;
					} else if (app.getUser().getFirstName().toLowerCase().startsWith(filter.toLowerCase())) {
						list.add(app);
						continue;
					} else if (app.getUser().getMiddleName().toLowerCase().startsWith(filter.toLowerCase())) {
						list.add(app);
					}
				} else {
					if (app.getCreatedBy().toLowerCase().startsWith(filter.toLowerCase())) {
						list.add(app);
					}
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No applications with username matching '"+filter+"' found for this group.");
		} else {
			loaderImage.setEmptyResultMessage("No applications matching search criteria found for this group.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();
	}

	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

}
