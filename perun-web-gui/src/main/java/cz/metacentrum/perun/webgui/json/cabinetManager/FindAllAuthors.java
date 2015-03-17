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
import cz.metacentrum.perun.webgui.model.Author;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Finds All authors in Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FindAllAuthors implements JsonCallback, JsonCallbackTable<Author> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "cabinetManager/findAllAuthors";
	// Data provider
	private ListDataProvider<Author> dataProvider = new ListDataProvider<Author>();
	// table
	private PerunTable<Author> table;
	// table data
	private ArrayList<Author> list = new ArrayList<Author>();
	// Selection model
	final MultiSelectionModel<Author> selectionModel = new MultiSelectionModel<Author>(new GeneralKeyProvider<Author>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Author, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	private boolean checkable = false;


	/**
	 * Creates a new request
	 */
	public FindAllAuthors() {}

	/**
	 * Creates a new request
	 *
	 * @param events external events
	 */
	public FindAllAuthors(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table with publications
	 * @param
	 */
	public CellTable<Author> getTable(FieldUpdater<Author, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	public CellTable<Author> getTable() {

		// retrieves data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns table of users publications
	 * @return
	 */
	public CellTable<Author> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Author>(list);

		// Cell table
		table = new PerunTable<Author>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Author> columnSortHandler = new ListHandler<Author>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Author> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No authors found.");

		table.setHyperlinksAllowed(false);

		// show checkbox column
		if(this.checkable) {
			// checkbox column column
			table.addCheckBoxColumn();
		}

		// ID COLUMN
		table.addIdColumn("User Id", tableFieldUpdater, 90);
		table.addNameColumn(tableFieldUpdater);

		// publications count COLUMN
		Column<Author, String> pubCountColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Author, String>() {
					public String getValue(Author object) {
						return String.valueOf(object.getPublicationsCount());
					}
				}, this.tableFieldUpdater);
		table.addColumn(pubCountColumn, "#publications");

		// login COLUMN
		Column<Author, String> loginColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Author, String>() {
					public String getValue(Author object) {
						return object.getLogins();
					}
				}, this.tableFieldUpdater);

		loginColumn.setSortable(true);
		columnSortHandler.setComparator(loginColumn, new Comparator<Author>(){
			public int compare(Author o1, Author o2) {
				return o1.getLogins().compareToIgnoreCase(o2.getLogins());
			}
		});
		table.addColumn(loginColumn, "Logins");

		return table;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Author>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Author to be added as new row
	 */
	public void addToTable(Author object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Author to be removed as row
	 */
	public void removeFromTable(Author object) {
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
	public ArrayList<Author> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading authors.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading authors started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Author>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Authors loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Author object) {
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

	public void setList(ArrayList<Author> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Author> getList() {
		return this.list;
	}


	public void setSelected(Author user) {
		selectionModel.setSelected(user, true);
	}

	public void setEvents(JsonCallbackEvents event) {
		this.events = event;
	}

}
