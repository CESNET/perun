package cz.metacentrum.perun.webgui.json.extSourcesManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.UnaccentMultiWordSuggestOracle;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query to get all VO ext sources
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetVoExtSources implements JsonCallback, JsonCallbackTable<ExtSource>, JsonCallbackOracle<ExtSource> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// jsonCallback string
	private final String JSON_URL = "extSourcesManager/getVoExtSources";
	// ext sources stored as cellTable widget
	private PerunTable<ExtSource> table;
	// ext sources list data provider (for easy filling from json call)
	private ListDataProvider<ExtSource> dataProvider = new ListDataProvider<ExtSource>();
	// selection model for tables (for easy selection)
	private final MultiSelectionModel<ExtSource> selectionModel = new MultiSelectionModel<ExtSource>(new GeneralKeyProvider<ExtSource>());
	// list of ext sources
	private ArrayList<ExtSource> list = new ArrayList<ExtSource>();
	// external events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// vo Id
	private int voId = 0;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;
	private ArrayList<ExtSource> fullBackup = new ArrayList<ExtSource>();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle(" ./-");

	/**
	 * Creates a new callback
	 *
	 * @param voId ID of VO to get ext sources for
	 */
	public GetVoExtSources(int voId) {
		this.voId = voId;
	}

	/**
	 * Creates a new callback
	 *
	 * @param voId ID of Vo we want ext sources for
	 * @param events external events
	 */
	public GetVoExtSources(int voId, JsonCallbackEvents events) {
		this.voId = voId;
		this.events = events;
	}

	/**
	 * Return table containing VO external sources
	 *
	 * @return table widget containing VO ext sources
	 */
	public CellTable<ExtSource> getTable() {

		// retrieve data
		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<ExtSource>(list);

		// Cell table
		table = new PerunTable<ExtSource>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<ExtSource> columnSortHandler = new ListHandler<ExtSource>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<ExtSource> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("VO has no external source of members.");

		// content
		if(checkable){
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Ext. source ID", null);
		table.addNameColumn(null);

		// Type column
		TextColumn<ExtSource> typeColumn = new TextColumn<ExtSource>() {
			@Override
			public String getValue(ExtSource extSource) {
				return String.valueOf(renameContent(extSource.getType()));
			}
		};

		// sort type column
		typeColumn.setSortable(true);
		columnSortHandler.setComparator(typeColumn, new Comparator<ExtSource>() {
			public int compare(ExtSource o1, ExtSource o2) {
				return o1.getType().compareToIgnoreCase(o2.getType());
			}
		});

		// Add columns to table
		table.addColumn(typeColumn, "Type");

		// return cellTable
		return table;

	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "vo="+voId, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<ExtSource>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object ExtSource to be added as new row
	 */
	public void addToTable(ExtSource object) {
		list.add(object);
		oracle.add(object.getName());
		oracle.add(renameContent(object.getType()));
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object ExtSource to be removed as row
	 */
	public void removeFromTable(ExtSource object) {
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
	public ArrayList<ExtSource> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading external sources.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading external sources started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<ExtSource>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Loading external sources finished: "+ list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	public void insertToTable(int index, ExtSource object) {
		list.add(index, object);
		oracle.add(object.getName());
		oracle.add(renameContent(object.getType()));
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setList(ArrayList<ExtSource> list) {
		clearTable();
		this.list.addAll(list);
		for (ExtSource object : list) {
			oracle.add(object.getName());
			oracle.add(renameContent(object.getType()));
		}
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<ExtSource> getList() {
		return this.list;
	}

	/**
	 * Return substring on position 40 and uppercase - used for ExtSourceType value
	 *
	 * @param oldString original string value
	 * @return new string starting on position 40 and uppercase
	 */
	private String renameContent(String oldString) {
		String newString = "";
		newString = oldString.substring(40);
		newString = newString.toUpperCase();

		return newString;
	}

	@Override
	public void filterTable(String filter) {

		// store list only for first time
		if (fullBackup.isEmpty() || fullBackup == null) {
			fullBackup.addAll(getList());
		}

		// always clear selected items
		selectionModel.clear();
		list.clear();

		if (filter.equalsIgnoreCase("")) {
			list.addAll(fullBackup);
		} else {
			for (ExtSource src : fullBackup){
				// store ext source if name or type matches
				if ((src.getName().toLowerCase().contains(filter.toLowerCase())) ||
						renameContent(src.getType()).toLowerCase().contains(filter.toLowerCase())) {
					list.add(src);
						}
			}
		}

		if (list.isEmpty() && !filter.isEmpty()) {
			loaderImage.setEmptyResultMessage("No external source matching '"+filter+"' found.");
		} else {
			loaderImage.setEmptyResultMessage("VO has no external source of members.");
		}

		dataProvider.flush();
		dataProvider.refresh();
		loaderImage.loadingFinished();

	}

	@Override
	public UnaccentMultiWordSuggestOracle getOracle() {
		return this.oracle;
	}

	@Override
	public void setOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

	public MultiSelectionModel<ExtSource> getSelectionModel() {
		return this.selectionModel;
	}

}
