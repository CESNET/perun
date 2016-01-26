package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Returns list of User's applications
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetApplicationsForUser implements JsonCallback, JsonCallbackTable<Application>, JsonCallbackOracle<Application> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// VO id
	private int userId = 0;
	// JSON URL
	static private final String JSON_URL = "registrarManager/getApplicationsForUser";
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

	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private ArrayList<Application> backupList = new ArrayList<Application>();

	/**
	 * Creates a new method instance
	 *
	 * @param id User ID (if = 0 search by actor / extSourceName in session)
	 */
	public GetApplicationsForUser(int id) {
		this.userId = id;
	}

	/**
	 * Creates a new method instance
	 *
	 * @param id User ID (if = 0 search by actor / extSourceName in session)
	 * @param events Custom events
	 */
	public GetApplicationsForUser(int id, JsonCallbackEvents events) {
		this.userId = id;
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

		table.removeRowCountChangeHandler();

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Application> columnSortHandler = new ListHandler<Application>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Application> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("You have no applications submitted.");

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("App ID", tableFieldUpdater, 100);

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
		table.setColumnWidth(dateColumn, "150px");

		// Type column
		Column<Application, String> typeColumn = new Column<Application, String>(
				new CustomClickableTextCell()) {
			@Override
			public String getValue(Application object) {
				return object.getTranslatedType(object.getType());
			}
			@Override
			public String getCellStyleNames(Cell.Context context, Application object) {

				if (tableFieldUpdater != null) {
					return super.getCellStyleNames(context, object) + " pointer";
				} else {
					return super.getCellStyleNames(context, object);
				}

			}
		};
		typeColumn.setFieldUpdater(tableFieldUpdater);
		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<Application>(){
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

				if (tableFieldUpdater != null) {
					return super.getCellStyleNames(context, object) + " pointer";
				} else {
					return super.getCellStyleNames(context, object);
				}

			}
		};
		stateColumn.setFieldUpdater(tableFieldUpdater);
		stateColumn.setSortable(true);
		columnSortHandler.setComparator(stateColumn, new Comparator<Application>(){
			public int compare(Application arg0, Application arg1) {
				return (arg0.getTranslatedState(arg0.getState())).compareToIgnoreCase(arg1.getTranslatedState(arg1.getState()));
			}
		});
		table.addColumn(stateColumn, "State");
		table.setColumnWidth(stateColumn, "120px");

		// VO COLUMN
		Column<Application, String> voColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Application, String>() {
					public String getValue(Application object) {
						return object.getVo().getName();
					}
				}, tableFieldUpdater);

		voColumn.setSortable(true);
		columnSortHandler.setComparator(voColumn, new Comparator<Application>(){
			public int compare(Application arg0, Application arg1) {
				return (arg0.getVo().getName()).compareToIgnoreCase(arg1.getVo().getName());
			}
		});
		table.addColumn(voColumn, "Virtual organization");

		// GROUP COLUMN
		Column<Application, String> groupColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Application, String>() {
					public String getValue(Application object) {
						if (object.getGroup() != null) {
							return object.getGroup().getName();
						} else {
							return "---";
						}
					}
				}, tableFieldUpdater);

		groupColumn.setSortable(true);
		columnSortHandler.setComparator(groupColumn, new Comparator<Application>(){
			public int compare(Application arg0, Application arg1) {
				String name1 = "";
				String name2 = "";
				if (arg0.getGroup() != null) {
					name1 = arg0.getGroup().getName();
				} else {
					name1 = "---";
				}
				if (arg1.getGroup() != null) {
					name2 = arg1.getGroup().getName();
				} else {
					name2 = "---";
				}
				return (name1).compareToIgnoreCase(name2);
			}
		});

		table.addColumn(groupColumn, "Group");

		// FED INFO COLUMN
		/*
			 Column<Application, String> fedInfoColumn = JsonUtils.addColumn(
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
			 return object.getFedInfo();
			 }
			 }, tableFieldUpdater);

			 fedInfoColumn.setSortable(true);
			 columnSortHandler.setComparator(fedInfoColumn, new Comparator<Application>(){
			 public int compare(Application arg0, Application arg1) {
			 return (arg0.getFedInfo()).compareToIgnoreCase(arg1.getFedInfo());
			 }
			 });
			 table.addColumn(fedInfoColumn, "Federation information");
			 */

		table.setRowStyles(new RowStyles<Application>() {
			public String getStyleNames(Application application, int i) {

				if ("NEW".equalsIgnoreCase(application.getState())) {
					return "rowgreen";
				} else if ("VERIFIED".equalsIgnoreCase(application.getState())) {
					return "rowyellow";
				} else if ("APPROVED".equalsIgnoreCase(application.getState())) {
					return "rowdarkgreen";
				} else if ("REJECTED".equalsIgnoreCase(application.getState())) {
					return "rowred";
				}

				return "";

			}
		});

		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {

		String param = "";

		if (userId != 0) {
			param = "id=" + this.userId;
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
		if (object.getVo() != null) {
			oracle.add(object.getVo().getName());
		}
		if (object.getGroup() != null) {
			oracle.add(object.getGroup().getName());
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
		session.getUiElements().setLogErrorText("Error while loading Users applications.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading Users applications started.");
		events.onLoadingStart();
	}

	/**
	 * Called when loading successfully finishes.
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
		if (object.getVo() != null) {
			oracle.add(object.getVo().getName());
		}
		if (object.getGroup() != null) {
			oracle.add(object.getGroup().getName());
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
			if (a.getVo() != null) {
				oracle.add(a.getVo().getName());
			}
			if (a.getGroup() != null) {
				oracle.add(a.getGroup().getName());
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

	@Override
	public void filterTable(String filter) {

		if (backupList == null || backupList.isEmpty()) {
			backupList.addAll(getList());
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (filter.equalsIgnoreCase("")) {
			list.addAll(backupList);
		} else {
			for (Application a : backupList){
				// store app by filter
				if (a.getVo() != null && a.getGroup() == null) {
					if (a.getVo().getName().toLowerCase().startsWith(filter.toLowerCase())) {
						list.add(a);
					}
				} else if (a.getVo() != null && a.getGroup() != null) {
					if (a.getVo().getName().toLowerCase().startsWith(filter.toLowerCase()) ||
							a.getGroup().getName().toLowerCase().startsWith(filter.toLowerCase())) {
						list.add(a);
							}
				}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("You have no applications submitted for VO/group matching '"+filter+"'.");
		}

		loaderImage.loadingFinished();
		dataProvider.flush();
		dataProvider.refresh();
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
	 * Set external events
	 *
	 * @param events events to set
	 */
	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}
