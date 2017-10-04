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
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Author;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Finds new authors in Perun. Used by users to add co-authors for reported Publication
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class FindNewAuthors implements JsonCallback, JsonCallbackTable<Author> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "cabinetManager/findNewAuthors";
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
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, WidgetTranslation.INSTANCE.emptySearchForUsers());

	private boolean checkable = true;

	private String searchString = "";

	/**
	 * Creates a new request
	 *
	 * @param searchString string to search by
	 */
	public FindNewAuthors(String searchString) {
		this.searchString = searchString;
	}

	/**
	 * Creates a new request
	 *
	 * @param searchString search string to search by
	 * @param events external events
	 */
	public FindNewAuthors(String searchString, JsonCallbackEvents events) {
		this.searchString = searchString;
		this.events = events;
	}

	/**
	 * Returns table with publications
	 * @param fu field updater
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
	 * @param fu
	 * @return table
	 */
	public CellTable<Author> getEmptyTable(FieldUpdater<Author, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	/**
	 * Returns table of users publications
	 * @return table
	 */
	public CellTable<Author> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Author>(list);

		// Cell table
		table = new PerunTable<Author>(list);
		table.removeRowCountChangeHandler();

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Author> columnSortHandler = new ListHandler<Author>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Author> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No users found.");

		// show checkbox column
		if(this.checkable) {
			// checkbox column column
			table.addCheckBoxColumn();
		}

		// ID COLUMN
		Column<Author, String> idColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Author, String>() {
					public String getValue(Author object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);

		idColumn.setSortable(true);
		columnSortHandler.setComparator(idColumn, new Comparator<Author>(){
			public int compare(Author o1, Author o2) {
				return o1.getId()-o2.getId();
			}
		});
		table.addColumn(idColumn, "User Id");

		// Name COLUMN
		Column<Author, String> nameColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Author, String>() {
					public String getValue(Author object) {
						return String.valueOf(object.getFullName());
					}
				}, this.tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<Author>(){
			public int compare(Author o1, Author o2) {
				return o1.getFullName().compareToIgnoreCase(o2.getFullName());
			}
		});
		table.addColumn(nameColumn, "Name");

		// organization
		Column<Author, String> organizationColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Author, String>() {
					public String getValue(Author object) {
						String val1 = object.getAttribute("urn:perun:user:attribute-def:def:organization").getValue();
						if (val1 == null || val1.equalsIgnoreCase("null")) val1 = "";
						return val1;
					}
				}, this.tableFieldUpdater);

		organizationColumn.setSortable(true);
		columnSortHandler.setComparator(organizationColumn, new Comparator<Author>(){
			public int compare(Author o1, Author o2) {
				String val1 = o1.getAttribute("urn:perun:user:attribute-def:def:organization").getValue();
				String val2 = o2.getAttribute("urn:perun:user:attribute-def:def:organization").getValue();
				if (val1 == null || val1.equalsIgnoreCase("null")) val1 = "";
				if (val2 == null || val2.equalsIgnoreCase("null")) val2 = "";
				return val1.compareToIgnoreCase(val2);
			}
		});
		table.addColumn(organizationColumn, "Organization");

		// mail
		Column<Author, String> emailColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Author, String>() {
					public String getValue(Author object) {
						String val1 = object.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue();
						if (val1 == null || val1.equalsIgnoreCase("null")) val1 = "";
						return val1;
					}
				}, this.tableFieldUpdater);

		emailColumn.setSortable(true);
		columnSortHandler.setComparator(emailColumn, new Comparator<Author>(){
			public int compare(Author o1, Author o2) {
				String val1 = o1.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue();
				String val2 = o2.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue();
				if (val1 == null || val1.equalsIgnoreCase("null")) val1 = "";
				if (val2 == null || val2.equalsIgnoreCase("null")) val2 = "";
				return val1.compareToIgnoreCase(val2);
			}
		});
		table.addColumn(emailColumn, "Email");

		/*
		Column<Author, String> loginsColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Author, String>() {
					public String getValue(Author object) {
						String val1 = object.getAttribute("urn:perun:user:attribute-def:def:login-namespace:einfra").getValue();
						if (val1 == null || val1.equalsIgnoreCase("null")) val1 = "";
						return val1;
					}
				}, this.tableFieldUpdater);

		table.addColumn(loginsColumn, "Login");
		*/

		return table;
	}

	/**
	 * Users search
	 * @param query
	 */
	public void searchFor(String query){

		if (query == null || query.isEmpty()) return;
		loaderImage.setEmptyResultMessage("No user matching '"+query+"' found.");
		this.searchString = query;
		clearTable();
		retrieveData();
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		if(this.searchString==null || searchString.isEmpty()) return;
		loaderImage.loadingStart();
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "searchString=" + this.searchString, this);
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
		session.getUiElements().setLogErrorText("Error while searching authors.");
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
		loaderImage.loadingFinished();
		session.getUiElements().setLogText("Authors found: " + list.size());
		events.onFinished(jso);
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

}
