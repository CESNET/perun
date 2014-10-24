package cz.metacentrum.perun.webgui.json.cabinetManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.PublicationComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Publication;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Finds publications in external source
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class FindExternalPublications implements JsonCallback, JsonCallbackTable<Publication> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "cabinetManager/findExternalPublications";
	// Data provider
	private ListDataProvider<Publication> dataProvider = new ListDataProvider<Publication>();
	// table
	private PerunTable<Publication> table;
	// table data
	private ArrayList<Publication> list = new ArrayList<Publication>();
	// Selection model
	final MultiSelectionModel<Publication> selectionModel = new MultiSelectionModel<Publication>(new GeneralKeyProvider<Publication>(){
		@Override
		public Object getKey(Publication o) {
			return o.getExternalId();
		}
	});
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

	// parameters
	private int userId;

	private int yearSince = 0;

	private int yearTill = Integer.MAX_VALUE;

	private String namespace = "";

	private boolean checkable = true;


	/**
	 * Creates a new request
	 *
	 * @param userId User ID
	 */
	public FindExternalPublications(int userId) {
		this.userId = userId;
	}

	/**
	 * Creates a new request
	 *
	 * @param userId User ID
	 * @param yearSince Year since
	 * @param yearTill Year till
	 * @param namespace namespace (MU, ZCU)
	 */
	public FindExternalPublications(int userId, int yearSince, int yearTill, String namespace) {
		this(userId);
		this.yearSince = yearSince;
		this.yearTill = yearTill;
		this.namespace = namespace;
	}

	/**
	 * Creates a new request
	 *
	 * @param userId User ID
	 * @param events events
	 */
	public FindExternalPublications(int userId, JsonCallbackEvents events) {
		this(userId);
		this.events = events;
	}

	/**
	 * Creates a new request
	 *
	 * @param userId User ID
	 * @param yearSince Year since
	 * @param yearTill Year till
	 * @param namespace namespace (MU, ZCU)
	 * @param events events
	 */
	public FindExternalPublications(int userId, int yearSince, int yearTill, String namespace, JsonCallbackEvents events) {
		this(userId, yearSince, yearTill, namespace);
		this.events = events;
	}

	/**
	 * Returns table with publications
	 * @return table
	 */
	public CellTable<Publication> getTable(){
		retrieveData();
		return this.getEmptyTable();
	}

	/**
	 * Returns table with publications
	 * @return table
	 */
	public CellTable<Publication> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Publication>(list);

		// Cell table
		table = new PerunTable<Publication>(list);
		table.removeRowCountChangeHandler();
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Publication> columnSortHandler = new ListHandler<Publication>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Publication> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage.prepareToSearch("Please select external pub. system and year range to search for publications for import."));
		loaderImage.setEmptyResultMessage("No publications found in external system.");

		// show checkbox column
		if(this.checkable) {
			table.addCheckBoxColumn();
		}

		/*
		// CATEGORY COLUMN
		ArrayList<String> categories = new ArrayList<String>();
		categories.add("Bodované v RIVu");
		categories.add("Nebodované v RIVu");
		categories.add("Příspěvek do ročenky");
		categories.add("Výjimečné výsledky");
		categories.add("Jiné");

		Column<Publication, String> categoryColumn = new Column<Publication, String>(new SelectionCell(categories)){
		@Override
		public String getValue(Publication object) {
		// category ID as string, 0 if not set
		int id = object.getCategoryId();
		if (id == 0) {
		// set default == 21/Bodované v RIVu to object
		object.setCategoryId(21);
		}
		if (id == 21) {
		return "Bodované v RIVu";
		} else if (id == 22) {
		return "Nebodované v RIVu";
		} else if (id == 23) {
		return "Výjimečné výsledky";
		} else if (id == 24) {
		return "Příspěvek do ročenky";
		} else if (id == 25) {
		return "Jiné";
		} else {
		return String.valueOf(id); // return ID if no match
		}
		}
		};
		categoryColumn.setFieldUpdater(new FieldUpdater<Publication, String>() {
		public void update(int index, Publication object, String value) {

		int id = 0;
		if (value.equalsIgnoreCase("Bodované v RIVu")) {
		id = 21;
		} else if (value.equalsIgnoreCase("Nebodované v RIVu")) {
		id = 22;
		} else if (value.equalsIgnoreCase("Příspěvek do ročenky")) {
		id = 24;
		} else if (value.equalsIgnoreCase("Výjimečné výsledky")) {
		id = 23;
		} else if (value.equalsIgnoreCase("Jiné")) {
		id = 25;
		}
		object.setCategoryId(id);
		selectionModel.setSelected(object, true);

		}
		});
		table.addColumn(categoryColumn, "Category");

		// NOT USEFULL => DISABLED
		// EXTERNAL ID
		TextColumn<Publication> externalIdColumn = new TextColumn<Publication>() {
		public String getValue(Publication object) {
		return String.valueOf(object.getExternalId());
		};
		};
		externalIdColumn.setSortable(true);
		columnSortHandler.setComparator(externalIdColumn, new PublicationComparator(PublicationComparator.Column.EXTERNAL_ID));
		table.addColumn(externalIdColumn, "External Id");
		*/

		// TITLE COLUMN
		TextColumn<Publication> titleColumn = new TextColumn<Publication>() {
			public String getValue(Publication object) {
				return object.getTitle();
			};
		};
		titleColumn.setSortable(true);
		columnSortHandler.setComparator(titleColumn, new PublicationComparator(PublicationComparator.Column.TITLE));
		table.addColumn(titleColumn, "Title");

		// AUTHORS COLUMN
		TextColumn<Publication> authorColumn = new TextColumn<Publication>() {
			public String getValue(Publication object) {
				return object.getAuthorsFormatted();
			};
		};
		authorColumn.setSortable(true);
		columnSortHandler.setComparator(authorColumn, new PublicationComparator(PublicationComparator.Column.AUTHORS));
		table.addColumn(authorColumn, "Authors");

		// YEAR COLUMN
		TextColumn<Publication> yearColumn = new TextColumn<Publication>() {
			public String getValue(Publication object) {
				return String.valueOf(object.getYear());
			};
		};
		yearColumn.setSortable(true);
		columnSortHandler.setComparator(yearColumn, new PublicationComparator(PublicationComparator.Column.YEAR));
		table.addColumn(yearColumn, "Year");

		// ISBN COLUMN
		TextColumn<Publication> isbnColumn = new TextColumn<Publication>() {
			public String getValue(Publication object) {
				return object.getIsbn();
			};
		};
		isbnColumn.setSortable(true);
		columnSortHandler.setComparator(isbnColumn, new PublicationComparator(PublicationComparator.Column.ISBN));
		table.addColumn(isbnColumn, "ISBN");

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
		js.retrieveData(JSON_URL, "user=" + this.userId + "&yearSince=" + yearSince + "&yearTill=" + yearTill+ "&pubSysNamespace="+namespace, this);
		return;
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
	 * Set user Id
	 */
	public void setUser(int userId) {
		this.userId = userId;
	}

	/**
	 * Set year since
	 */
	public void setYearSince(int year) {
		this.yearSince = year;
	}

	/**
	 * Set year till
	 */
	public void setYearTill(int year) {
		this.yearTill = year;
	}

	/**
	 * Set pub sys namespace
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
