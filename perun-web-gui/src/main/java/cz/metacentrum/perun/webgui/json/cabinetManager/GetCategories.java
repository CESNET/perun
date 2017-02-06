package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Category;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.TextInputCellWithTabIndex;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Finds All categories in Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetCategories implements JsonCallback, JsonCallbackTable<Category> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "cabinetManager/getCategories";
	// Data provider
	private ListDataProvider<Category> dataProvider = new ListDataProvider<Category>();
	// table
	private PerunTable<Category> table;
	// table data
	private ArrayList<Category> list = new ArrayList<Category>();
	// Selection model
	final MultiSelectionModel<Category> selectionModel = new MultiSelectionModel<Category>(new GeneralKeyProvider<Category>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Category, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	private boolean checkable = true;


	/**
	 * Creates a new request
	 */
	public GetCategories() {}

	/**
	 * Creates a new request
	 *
	 * @param events external events
	 */
	public GetCategories(JsonCallbackEvents events) {
		this.events = events;
	}

	/**
	 * Returns table with publications
	 * @param fu
	 */
	public CellTable<Category> getTable(FieldUpdater<Category, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	public CellTable<Category> getTable() {

		// retrieves data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns table of users publications
	 * @return table
	 */
	public CellTable<Category> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Category>(list);

		// Cell table
		table = new PerunTable<Category>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Category> columnSortHandler = new ListHandler<Category>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Category> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No publications categories found.");

		// tab index for values
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

		// show checkbox column
		if(this.checkable)
		{
			// checkbox column column
			table.addCheckBoxColumn();
		}

		// ID COLUMN
		table.addIdColumn("Category ID", tableFieldUpdater, 80);

		table.addNameColumn(tableFieldUpdater);

		// Rank COLUMN
		Column<Category, String> rankColumn = JsonUtils.addColumn(
				new TextInputCellWithTabIndex(),
				new JsonUtils.GetValue<Category, String>() {
					public String getValue(Category category) {
						return String.valueOf(category.getRank());
					}
				}, new FieldUpdater<Category, String>() {
					public void update(int index, Category object, String newText) {
						// parsing value
						try {
							double value = Double.parseDouble(newText);
							object.setRank(value);
							selectionModel.setSelected(object, true);
						} catch (NumberFormatException ex) {
							selectionModel.setSelected(object, false);
							session.getUiElements().setLogErrorText("Rank mismatch for category: "+object.getName()+" and value: "+newText);
						}
					}
				});


		// Sorting value column
		rankColumn.setSortable(true);
		columnSortHandler.setComparator(rankColumn,
				new Comparator<Category>() {
					public int compare(Category o1, Category o2) {
						// return int diff of doubles
						return (int)(o1.getRank() - o2.getRank());
					}
				});
		table.addColumn(rankColumn, "Rank");

		return table;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Category>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Category to be added as new row
	 */
	public void addToTable(Category object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Category to be removed as row
	 */
	public void removeFromTable(Category object) {
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
	public ArrayList<Category> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading categories.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading categories started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Category>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Categories loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, Category object) {
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

	public void setList(ArrayList<Category> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Category> getList() {
		return this.list;
	}

}
