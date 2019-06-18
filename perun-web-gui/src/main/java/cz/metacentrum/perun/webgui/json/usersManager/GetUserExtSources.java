package cz.metacentrum.perun.webgui.json.usersManager;

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
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.UserExtSource;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all user's external identities (userExtSources)
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetUserExtSources implements JsonCallback, JsonCallbackTable<UserExtSource> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// User ID
	private int userId;
	// JSON URL
	private final static String JSON_URL = "usersManager/getUserExtSources";
	// Custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// table with user ext sources
	private PerunTable<UserExtSource> table;
	// ext sources list data provider (for easy filling from json call)
	private ListDataProvider<UserExtSource> dataProvider = new ListDataProvider<UserExtSource>();
	// selection model for tables (for easy selection)
	private final MultiSelectionModel<UserExtSource> selectionModel = new MultiSelectionModel<UserExtSource>(new GeneralKeyProvider<UserExtSource>());
	// list of ext sources
	private ArrayList<UserExtSource> list = new ArrayList<UserExtSource>();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	// Table field updater
	private FieldUpdater<UserExtSource, String> tableFieldUpdater;

	/**
	 * Creates a new request instance
	 *
	 * @param id ID of User
	 */
	public GetUserExtSources(int id) {
		this.userId = id;
	}

	/**
	 * Creates a new request instance with custom events
	 *
	 * @param id ID of User
	 * @param events custom events
	 */
	public GetUserExtSources(int id, JsonCallbackEvents events) {
		this.userId = id;
		this.events = events;
	}

	/**
	 * Returns table of users
	 * @param fu
	 */
	public CellTable<UserExtSource> getTable(FieldUpdater<UserExtSource, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Return table with checkboxes containing user external sources
	 *
	 * @return table containing user ext sources
	 */
	public CellTable<UserExtSource> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<UserExtSource>(list);

		// Cell table
		table = new PerunTable<UserExtSource>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<UserExtSource> columnSortHandler = new ListHandler<UserExtSource>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<UserExtSource> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// checkbox column column
		com.google.gwt.user.cellview.client.Column<UserExtSource, UserExtSource> checkBoxColumn = new com.google.gwt.user.cellview.client.Column<UserExtSource, UserExtSource>(
				new PerunCheckboxCell<UserExtSource>(true, false, false)) {
			@Override
			public UserExtSource getValue(UserExtSource object) {
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
				for(UserExtSource obj : list){
					if (!obj.isPersistent()) {
						selectionModel.setSelected(obj, value);
					}
				}
			}
		});

		table.addColumn(checkBoxColumn, checkBoxHeader);

		table.addIdColumn("UES ID", tableFieldUpdater, 100);

		// Name column
		Column<UserExtSource, String> nameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<UserExtSource, String>() {
			public String getValue(UserExtSource extSource) {
				return String.valueOf(extSource.getExtSource().getName());
			}
		}, tableFieldUpdater);

		// Login column
		Column<UserExtSource, String> loginColumn = JsonUtils.addColumn(new JsonUtils.GetValue<UserExtSource, String>() {
			public String getValue(UserExtSource extSource) {
				return String.valueOf(extSource.getLogin());
			}
		}, tableFieldUpdater);

		// LOA column
		Column<UserExtSource, String> loaColumn = JsonUtils.addColumn(new JsonUtils.GetValue<UserExtSource, String>() {
			public String getValue(UserExtSource extSource) {
				return String.valueOf(extSource.getLoa());
			}
		}, tableFieldUpdater);

		// LastAccess column
		Column<UserExtSource, String> lastAccessColumn = JsonUtils.addColumn(new JsonUtils.GetValue<UserExtSource, String>() {
			public String getValue(UserExtSource extSource) {
				if (extSource.getLastAccess() != null && !extSource.getLastAccess().isEmpty()) {
					return extSource.getLastAccess().split("\\.")[0];
				} else {
					return "N/A";
				}
			}
		}, tableFieldUpdater);

		// sort name column
		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new GeneralComparator<UserExtSource>(GeneralComparator.Column.NAME));

		// sort login column
		loginColumn.setSortable(true);
		columnSortHandler.setComparator(loginColumn, new Comparator<UserExtSource>() {
			public int compare(UserExtSource o1, UserExtSource o2) {
				return o1.getLogin().compareTo(o2.getLogin());
			}
		});

		// sort loa column
		loaColumn.setSortable(true);
		columnSortHandler.setComparator(loaColumn, new Comparator<UserExtSource>() {
			public int compare(UserExtSource o1, UserExtSource o2) {
				return o1.getLoa() - o2.getLoa();
			}
		});

		// sort lastAccess column
		lastAccessColumn.setSortable(true);
		columnSortHandler.setComparator(lastAccessColumn, new Comparator<UserExtSource>() {
			public int compare(UserExtSource o1, UserExtSource o2) {
				String la1 = (o1.getLastAccess() != null && !o1.getLastAccess().isEmpty()) ? o1.getLastAccess().split("\\.")[0] : "N/A";
				String la2 = (o2.getLastAccess() != null && !o2.getLastAccess().isEmpty()) ? o2.getLastAccess().split("\\.")[0] : "N/A";
				return la1.compareTo(la2);
			}
		});

		table.addColumn(nameColumn, "External source name");
		table.addColumn(loginColumn, "ID in external source");
		table.addColumn(loaColumn, "Level of assurance");
		table.addColumn(lastAccessColumn, "Last access");

		return table;

	}

	/**
	 * Returns selected items from table as list
	 *
	 * @return list of selected ues
	 */
	public ArrayList<UserExtSource> getTableSelectedList() {
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Clears selected items in table
	 */
	public void clearTableSelectedSet() {
		selectionModel.clear();
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		final String param = "user=" + this.userId;
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, param, this);
	}

	/**
	 * Sorts table by objects Id
	 */
	public void sortTable() {
		list = new TableSorter<UserExtSource>().sortById(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object UserExtSource to be added as new row
	 */
	public void addToTable(UserExtSource object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object UserExtSource to be removed as row
	 */
	public void removeFromTable(UserExtSource object) {
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
	 * Called if successfully finished.
	 *
	 * @param jso javascript objects (array) returned from RPC
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<UserExtSource>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Loading user ext sources finished: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	/**
	 * Called when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading user ext sources for user: " + this.userId);
		events.onError(error);
		loaderImage.loadingError(error);
	}

	/**
	 * Called when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading user ext sources started.");
		events.onLoadingStart();
	}

	public void insertToTable(int index, UserExtSource object) {
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

	public void setList(ArrayList<UserExtSource> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<UserExtSource> getList() {
		return this.list;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}
