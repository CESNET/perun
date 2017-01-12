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
import cz.metacentrum.perun.webgui.model.Thanks;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get thanks ba publication ID.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class GetRichThanksByPublicationId implements JsonCallback,JsonCallbackTable<Thanks> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "cabinetManager/getRichThanksByPublicationId";
	// Data provider
	private ListDataProvider<Thanks> dataProvider = new ListDataProvider<Thanks>();
	// table
	private PerunTable<Thanks> table;
	// table data
	private ArrayList<Thanks> list = new ArrayList<Thanks>();
	// Selection model
	final MultiSelectionModel<Thanks> selectionModel = new MultiSelectionModel<Thanks>(new GeneralKeyProvider<Thanks>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Thanks, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
	private boolean checkable = true;
	private int publicationId = 0;

	/**
	 * Creates a new request
	 *
	 * @param publicationId
	 */
	public GetRichThanksByPublicationId(int publicationId) {
		this.publicationId = publicationId;
	}

	/**
	 * Creates a new request
	 *
	 * @param publicationId
	 * @param events external events
	 */
	public GetRichThanksByPublicationId(int publicationId, JsonCallbackEvents events) {
		this(publicationId);
		this.events = events;
	}

	public CellTable<Thanks> getTable() {

		retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<Thanks>(list);

		// Cell table
		table = new PerunTable<Thanks>(list);
		table.removeRowCountChangeHandler();

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Thanks> columnSortHandler = new ListHandler<Thanks>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Thanks> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("Publication has no acknowledgements.");

		// checkbox column column
		if (checkable == true){
			table.addCheckBoxColumn();
		}

		table.addIdColumn("Akc Id", tableFieldUpdater);

		// Owner ID column
		Column<Thanks, String> ownerColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Thanks, String>() {
					public String getValue(Thanks thanks) {
						return thanks.getOwnerName();
					}
				}, tableFieldUpdater);

		table.addColumn(ownerColumn, "Acknowledgement");

		// createdBy column
		Column<Thanks, String> createdByColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Thanks, String>() {
					public String getValue(Thanks thanks) {
						return String.valueOf(thanks.getCreatedBy());
					}
				}, tableFieldUpdater);

		table.addColumn(createdByColumn, "Created by");

		return table;

	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Thanks>().sortById(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Thanks to be added as new row
	 */
	public void addToTable(Thanks object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Thanks to be removed as row
	 */
	public void removeFromTable(Thanks object) {
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
	public ArrayList<Thanks> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading thanks.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading thanks started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Thanks>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Thanks loaded: " + list.size());
		loaderImage.loadingFinished();
		events.onFinished(jso);
	}

	public void insertToTable(int index, Thanks object) {
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

	public void setList(ArrayList<Thanks> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Thanks> getList() {
		return this.list;
	}

	public void retrieveData() {
		JsonClient js = new JsonClient();
		js.retrieveData(JSON_URL, "id="+publicationId, this);
	}

}
