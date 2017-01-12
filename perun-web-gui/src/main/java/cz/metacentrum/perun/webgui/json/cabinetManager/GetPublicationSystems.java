package cz.metacentrum.perun.webgui.json.cabinetManager;

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
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PublicationSystem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Finds All publication systems in Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetPublicationSystems implements JsonCallback, JsonCallbackTable<PublicationSystem> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "cabinetManager/getPublicationSystems";
	// Data provider
	private ListDataProvider<PublicationSystem> dataProvider = new ListDataProvider<PublicationSystem>();
	// table
	private PerunTable<PublicationSystem> table;
	// table data
	private ArrayList<PublicationSystem> list = new ArrayList<PublicationSystem>();
	// Selection model
	final MultiSelectionModel<PublicationSystem> selectionModel = new MultiSelectionModel<PublicationSystem>(new GeneralKeyProvider<PublicationSystem>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<PublicationSystem, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	private boolean checkable = true;

	/**
	 * Creates a new request
	 */
	public GetPublicationSystems() {}

	/**
	 * Creates a new request
	 *
	 * @param events external events
	 */
	public GetPublicationSystems(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table with publication systems
	 * @param fu field updater
	 */
	public CellTable<PublicationSystem> getTable(FieldUpdater<PublicationSystem, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	public CellTable<PublicationSystem> getTable() {
		// retrieves data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns table of all publication systems
	 * @return table
	 */
	public CellTable<PublicationSystem> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<PublicationSystem>(list);

		// Cell table
		table = new PerunTable<PublicationSystem>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<PublicationSystem> columnSortHandler = new ListHandler<PublicationSystem>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<PublicationSystem> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No publications systems found.");

		// show checkbox column
		if(this.checkable) {
			table.addCheckBoxColumn();
		}

		// ID COLUMN
		table.addIdColumn("Pub system ID", tableFieldUpdater, 80);

		// NAME COLUMN
		Column<PublicationSystem, String> nameColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<PublicationSystem, String>() {
					public String getValue(PublicationSystem object) {
						return object.getFriendlyName();
					}
				}, this.tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<PublicationSystem>() {
			public int compare(PublicationSystem o1, PublicationSystem o2) {
				return o1.getFriendlyName().compareToIgnoreCase(o2.getFriendlyName());
			}
		});
		table.addColumn(nameColumn, "Friendly name");

		// NAME SPACE COLUMN
		Column<PublicationSystem, String> nameSpaceColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<PublicationSystem, String>() {
					public String getValue(PublicationSystem object) {
						return object.getLoginNamespace();
					}
				}, this.tableFieldUpdater);

		nameSpaceColumn.setSortable(true);
		columnSortHandler.setComparator(nameSpaceColumn, new Comparator<PublicationSystem>() {
			public int compare(PublicationSystem o1, PublicationSystem o2) {
				return o1.getLoginNamespace().compareToIgnoreCase(o2.getLoginNamespace());
			}
		});
		table.addColumn(nameSpaceColumn, "Login namespace");

		// URL COLUMN
		Column<PublicationSystem, String> urlColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<PublicationSystem, String>() {
					public String getValue(PublicationSystem object) {
						return object.getUrl();
					}
				}, this.tableFieldUpdater);

		urlColumn.setSortable(true);
		columnSortHandler.setComparator(urlColumn, new Comparator<PublicationSystem>() {
			public int compare(PublicationSystem o1, PublicationSystem o2) {
				return o1.getUrl().compareToIgnoreCase(o2.getUrl());
			}
		});
		table.addColumn(urlColumn, "URL");

		/*
		 *  DO NOT SHOW USERNAMES AND PASWORDS
		 *

		// USERNAME COLUMN
		Column<PublicationSystem, String> usernameColumn = JsonUtils.addColumn(
		new CustomClickableTextCell(), "",
		new JsonUtils.GetValue<PublicationSystem, String>() {
		public String getValue(PublicationSystem object) {
		return object.getUsername();
		}
		}, this.tableFieldUpdater);

		usernameColumn.setSortable(true);
		columnSortHandler.setComparator(usernameColumn, new Comparator<PublicationSystem>() {
		public int compare(PublicationSystem o1, PublicationSystem o2) {
		return o1.getUsername().compareToIgnoreCase(o2.getUsername());
		}
		});
		table.addColumn(usernameColumn, "Username");

		// PASSWORD COLUMN
		Column<PublicationSystem, String> passColumn = JsonUtils.addColumn(
		new CustomClickableTextCell(), "",
		new JsonUtils.GetValue<PublicationSystem, String>() {
		public String getValue(PublicationSystem object) {
		return object.getPassword();
		}
		}, this.tableFieldUpdater);

		passColumn.setSortable(true);
		columnSortHandler.setComparator(passColumn, new Comparator<PublicationSystem>() {
		public int compare(PublicationSystem o1, PublicationSystem o2) {
		return o1.getPassword().compareToIgnoreCase(o2.getPassword());
		}
		});
		table.addColumn(passColumn, "Password");
		 *
		 *
		 */

		// PARSER TYPE COLUMN
		Column<PublicationSystem, String> typeColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<PublicationSystem, String>() {
					public String getValue(PublicationSystem object) {
						return object.getType();
					}
				}, this.tableFieldUpdater);

		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<PublicationSystem>() {
			public int compare(PublicationSystem o1, PublicationSystem o2) {
				return o1.getType().compareToIgnoreCase(o2.getType());
			}
		});
		table.addColumn(typeColumn, "Parser type");

		return table;

	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL,this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<PublicationSystem>().sortByFriendlyName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object PublicationSystem to be added as new row
	 */
	public void addToTable(PublicationSystem object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object PublicationSystem to be removed as row
	 */
	public void removeFromTable(PublicationSystem object) {
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
	public ArrayList<PublicationSystem> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading publication systems.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading publication systems started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<PublicationSystem>jsoAsList(jso));
		sortTable();
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Publication systems loaded: " + list.size());
		events.onFinished(jso);
	}

	public void insertToTable(int index, PublicationSystem object) {
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

	public void setList(ArrayList<PublicationSystem> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<PublicationSystem> getList() {
		return this.list;
	}

}
