package cz.metacentrum.perun.webgui.json.vosManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.model.Candidate;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query for finding candidates in External sources so they can become VO members
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class FindCandidates implements JsonCallback, JsonCallbackTable<Candidate> {

	// JSON URL
	private final String JSON_URL = "vosManager/findCandidates";
	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Data provider.
	private ListDataProvider<Candidate> dataProvider = new ListDataProvider<Candidate>();
	// Table itself
	private PerunTable<Candidate> table;
	// Table list
	private ArrayList<Candidate> list = new ArrayList<Candidate>();
	// Selection model
	final SingleSelectionModel<Candidate> selectionModel = new SingleSelectionModel<Candidate>();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, WidgetTranslation.INSTANCE.emptySearchForCandidates());
	// Custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int voId;
	private String searchString = "";

	/**
	 * Creates a new instance of the request
	 *
	 * @param voId for which VO we search candidates for
	 * @param searchString part of the user's login
	 */
	public FindCandidates(int voId, String searchString) {
		this.voId = voId;
		this.searchString = searchString;
	}

	/**
	 * Creates a new instance of the request with custom events
	 *
	 * @param events Custom events
	 * @param voId for which VO we search candidates for
	 * @param searchString part of the user's login
	 */
	public FindCandidates(int voId, String searchString, JsonCallbackEvents events) {
		this.events = events;
		this.voId = voId;
		this.searchString = searchString;
	}

	/**
	 * Clears table
	 */
	public void clearTable(){
		loaderImage.loadingStart();
		dataProvider.getList().clear();
	}

	/**
	 * Retrieve data from RPC
	 */
	public void retrieveData() {

		// the request itself
		JsonClient js = new JsonClient(); // set 1 minute timeout for searching external sources
		if (searchString != null && !searchString.isEmpty()) {
			js.retrieveData(JSON_URL, "vo="+voId+"&searchString="+searchString, this);
		}

	}

	/**
	 * Sets a new search string (future member login / name / mail ...)
	 */
	public void searchFor(String searchString) {

		if (searchString == null || searchString.isEmpty()) return;

		loaderImage.setEmptyResultMessage("No user matching '"+searchString+"' found.");
		this.searchString = searchString;

		clearTable();
		retrieveData();

	}

	/**
	 * Returns the table widget with Candidates
	 *
	 * @return table widget
	 */
	public CellTable<Candidate> getTable(){

		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns the empty table widget
	 *
	 * @return table widget
	 */
	public CellTable<Candidate> getEmptyTable(){


		// Table data provider.
		dataProvider = new ListDataProvider<Candidate>(list);

		// Cell table
		table = new PerunTable<Candidate>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Candidate> columnSortHandler = new ListHandler<Candidate>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// Table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Candidate> createCheckboxManager());

		// Set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// Checkbox column column
		table.addCheckBoxColumn();

		// Login Column
		TextColumn<Candidate> loginColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				return candidate.getUserExtSource().getLogin();
			}
		};

		// Full Name Column
		TextColumn<Candidate> fullNameColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				return candidate.getDisplayName();
			}
		};

		// Ext Source Column
		TextColumn<Candidate> extSourceColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				return candidate.getUserExtSource().getExtSource().getName();
			}
		};

		// E-mail column
		TextColumn<Candidate> emailColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				if (candidate.getEmail() == null) {
					return "";
				} else {
					return candidate.getEmail();
				}
			}
		};

		loginColumn.setSortable(true);
		columnSortHandler.setComparator(loginColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return o1.getUserExtSource().getLogin().compareToIgnoreCase(o2.getUserExtSource().getLogin());
			}
		});

		fullNameColumn.setSortable(true);
		columnSortHandler.setComparator(fullNameColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return o1.getFullName().compareToIgnoreCase(o2.getFullName());
			}
		});

		extSourceColumn.setSortable(true);
		columnSortHandler.setComparator(extSourceColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return o1.getUserExtSource().getExtSource().getName().compareToIgnoreCase(o2.getUserExtSource().getExtSource().getName());
			}
		});

		emailColumn.setSortable(true);
		columnSortHandler.setComparator(emailColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return o1.getEmail().compareToIgnoreCase(o2.getEmail());
			}
		});

		table.addColumnSortHandler(columnSortHandler);

		// updates the columns size
		table.setColumnWidth(fullNameColumn, 300.0, Unit.PX);
		table.setColumnWidth(loginColumn, 150.0, Unit.PX);

		// Add the columns.
		table.addColumn(fullNameColumn, "Full name");
		table.addColumn(loginColumn, "Login");
		table.addColumn(emailColumn, "E-mail");
		table.addColumn(extSourceColumn, "Ext Source");

		return table;

	}

	/**
	 * Returns selected Candidates
	 *
	 * @return selected candidate
	 */
	public Candidate getSelected() {
		return selectionModel.getSelectedObject();
	}

	/**
	 * Sorts table by objects Names
	 */
	public void sortTable() {
		list = new TableSorter<Candidate>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Called when the query successfully finishes
	 *
	 * @param jso The JavaScript object returned from RPC
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<Candidate>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Candidates loaded: " + list.size());
		events.onFinished(jso);
		loaderImage.loadingFinished();
	}

	/**
	 * Called when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading candidates.");
		events.onError(error);
		loaderImage.loadingError(error);
	}

	/**
	 * Called when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading candidates started.");
		events.onLoadingStart();
	}

	public void insertToTable(int index, Candidate object) {
		list.add(index, object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void addToTable(Candidate object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void removeFromTable(Candidate object) {
		selectionModel.setSelected(object, false);
		list.remove(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	public void setCheckable(boolean checkable) {
		// TODO Auto-generated method stub
	}

	public void clearTableSelectedSet() {
		// FIXME - cannot clear SingleSelectionModel !!
	}

	public ArrayList<Candidate> getTableSelectedList() {
		// FIXME - not reasonable to get list with 1 candidate !!
		return null;
	}

	public void setList(ArrayList<Candidate> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Candidate> getList() {
		return this.list;
	}

	public void setEvents(JsonCallbackEvents events){
		this.events = events;
	}

	public void setSelected(Candidate candidate){
		selectionModel.setSelected(candidate, true);
	}

}
