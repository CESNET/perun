package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.PublicationComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.model.Thanks;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.CustomImageResourceCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Finds similar RichPublications
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FindSimilarPublications implements JsonCallback, JsonCallbackTable<Publication> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "cabinetManager/findSimilarPublications";
	// Data provider
	private ListDataProvider<Publication> dataProvider = new ListDataProvider<Publication>();
	// table
	private PerunTable<Publication> table;
	// table data
	private ArrayList<Publication> list = new ArrayList<Publication>();
	// Selection model
	final MultiSelectionModel<Publication> selectionModel = new MultiSelectionModel<Publication>(new GeneralKeyProvider<Publication>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Publication, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	private boolean checkable = true;
	private Map<String, Object> ids = new HashMap<String, Object>();

	/**
	 * Creates a new request
	 *
	 * @param ids filtering params
	 */
	public FindSimilarPublications(Map<String, Object> ids) {
		this.ids = ids;
	}

	/**
	 * Creates a new request
	 *
	 * @param ids filtering params
	 * @param events external events
	 */
	public FindSimilarPublications(Map<String, Object> ids, JsonCallbackEvents events) {
		this(ids);
		this.events = events;
	}

	/**
	 * Returns table with publications
	 * @param fu field updater
	 */
	public CellTable<Publication> getTable(FieldUpdater<Publication, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table with publications
	 * @param fu field updater
	 */
	public CellTable<Publication> getEmptyTable(FieldUpdater<Publication, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	}

	/**
	 * Return table of users publications
	 * @return table
	 */
	public CellTable<Publication> getTable() {

		// retrieves data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns table of users publications
	 * @return table
	 */
	public CellTable<Publication> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Publication>(list);

		// Cell table
		table = new PerunTable<Publication>(list);

		// display row-count for perun admin only
		if (!session.isPerunAdmin()) {
			table.removeRowCountChangeHandler();
		}

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Publication> columnSortHandler = new ListHandler<Publication>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Publication> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		loaderImage.setEmptyResultMessage("No similar publications found.");

		// show checkbox column
		if(this.checkable) {
			// checkbox column column
			table.addCheckBoxColumn();
		}

		// ID COLUMN
		table.addIdColumn("Publication ID", tableFieldUpdater, 60);

		Column<Publication, ImageResource> lockedColumn = new Column<Publication, ImageResource>(new CustomImageResourceCell("click")){
			public ImageResource getValue(Publication object) {
				if (object.getLocked() == true) {
					return SmallIcons.INSTANCE.lockIcon();
				} else {
					return SmallIcons.INSTANCE.lockOpenIcon();
				}
			}
			public void onBrowserEvent(final Context context,final Element elem,final Publication object, NativeEvent event) {
				// on click and for perun admin
				if ("click".equals(event.getType()) && session.isPerunAdmin()) {
					final ImageResource value;
					if (object.getLocked() == true) {
						value = SmallIcons.INSTANCE.lockOpenIcon();
						object.setLocked(false);
					} else {
						value = SmallIcons.INSTANCE.lockIcon();
						object.setLocked(true);
					}
					LockUnlockPublications request = new LockUnlockPublications(new JsonCallbackEvents(){
						@Override
						public void onLoadingStart() {
							getCell().setValue(context, elem, SmallIcons.INSTANCE.updateIcon());
						}
					@Override
					public void onFinished(JavaScriptObject jso) {
						// change picture (object already changed)
						getCell().setValue(context, elem, value);
					}
					@Override
					public void onError(PerunError error) {
						// on error switch object back
						if (object.getLocked() == true) {
							object.setLocked(false);
							getCell().setValue(context, elem, SmallIcons.INSTANCE.lockOpenIcon());
						} else {
							object.setLocked(true);
							getCell().setValue(context, elem, SmallIcons.INSTANCE.lockIcon());
						}
					}
					});
					// send request
					ArrayList<Publication> list = new ArrayList<Publication>();
					list.add(object);
					request.lockUnlockPublications(object.getLocked(), list);

				}
			}
		};
		table.addColumn(lockedColumn, "Lock");

		// TITLE COLUMN
		Column<Publication, String> titleColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Publication, String>() {
					public String getValue(Publication object) {
						return object.getTitle();
					}
				}, this.tableFieldUpdater);

		titleColumn.setSortable(true);
		columnSortHandler.setComparator(titleColumn, new PublicationComparator(PublicationComparator.Column.TITLE));
		table.addColumn(titleColumn, "Title");

		// if display authors
		if (ids.containsKey("authors")) {
			if ((Integer)ids.get("authors") == 1) {
				// AUTHORS COLUMN
				Column<Publication, String> authorColumn = JsonUtils.addColumn(
						new JsonUtils.GetValue<Publication, String>() {
							public String getValue(Publication object) {
								return object.getAuthorsFormatted();
							}
						}, this.tableFieldUpdater);

				authorColumn.setSortable(true);
				columnSortHandler.setComparator(authorColumn, new PublicationComparator(PublicationComparator.Column.AUTHORS));
				table.addColumn(authorColumn, "Reported by");
			}
		}

		// YEAR COLUMN
		Column<Publication, String> yearColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Publication, String>() {
					public String getValue(Publication object) {
						return String.valueOf(object.getYear());
					}
				}, this.tableFieldUpdater);

		yearColumn.setSortable(true);
		columnSortHandler.setComparator(yearColumn, new PublicationComparator(PublicationComparator.Column.YEAR));
		table.addColumn(yearColumn, "Year");

		// CATEGORY COLUMN
		Column<Publication, String> categoryColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Publication, String>() {
					public String getValue(Publication object) {
						return object.getCategoryName();
					}
				}, this.tableFieldUpdater);

		categoryColumn.setSortable(true);
		columnSortHandler.setComparator(categoryColumn, new PublicationComparator(PublicationComparator.Column.CATEGORY));
		table.addColumn(categoryColumn, "Category");

		// THANKS COLUMN
		Column<Publication, String> thanksColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Publication, String>() {
					public String getValue(Publication object) {
						String result = "";
						JsArray<Thanks> thks = object.getThanks();
						for (int i=0; i<thks.length(); i++) {
							result += thks.get(i).getOwnerName()+", ";
						}
						if (result.length()>=2) {
							result = result.substring(0, result.length()-2);
						}
						return result;
					}
				}, this.tableFieldUpdater);

		thanksColumn.setSortable(true);
		columnSortHandler.setComparator(thanksColumn, new PublicationComparator(PublicationComparator.Column.THANKS));
		table.addColumn(thanksColumn, "Thanked to");

		// CITE COLUMN
		Column<Publication, String> citaceColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<Publication, String>() {
					public String getValue(Publication object) {
						return "Cite";
					}
				}, new FieldUpdater<Publication, String>(){
					public void update(int index, Publication object, String value) {
						SimplePanel sp = new SimplePanel();
						sp.add(new HTML(object.getMain()));
						Confirm cf = new Confirm("Cite publication", sp, true);
						cf.show();
					};
				});

		table.addColumn(citaceColumn, "Cite");

		return table;
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData()
	{
		JsonClient js = new JsonClient();
		String params = "";
		for (Map.Entry<String, Object> attr : this.ids.entrySet()) {
			params += attr.getKey() + "=" + attr.getValue() + "&";
		}
		js.retrieveData(JSON_URL, params, this);
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Publication>().sortByPublicationTitle(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Publication to be added as new row
	 */
	public void addToTable(Publication object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Publication to be removed as row
	 */
	public void removeFromTable(Publication object) {
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
	public ArrayList<Publication> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading publications.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading publications started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Publication>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Publications loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();

	}

	public void insertToTable(int index, Publication object) {
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

	public void setList(ArrayList<Publication> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Publication> getList() {
		return this.list;
	}

	/**
	 * Sets params map
	 * @param ids
	 */
	public void setIds(Map<String, Object> ids){
		this.ids = ids;
	}

	/**
	 * Set custom events to callback
	 *
	 * @param events custom events
	 */
	public void setEvents(JsonCallbackEvents events) {
		this.events = events;
	}

}
