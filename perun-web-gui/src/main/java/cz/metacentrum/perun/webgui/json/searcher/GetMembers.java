package cz.metacentrum.perun.webgui.json.searcher;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.memberstabs.ChangeStatusTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.PerunStatusCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Searching for Resources
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GetMembers implements JsonCallback, JsonCallbackTable<Member> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "searcher/getMembersByUserAttributes";
	// Data provider
	private ListDataProvider<Member> dataProvider = new ListDataProvider<Member>();
	// table
	private PerunTable<Member> table;
	// table data
	private ArrayList<Member> list = new ArrayList<Member>();
	// Selection model
	final MultiSelectionModel<Member> selectionModel = new MultiSelectionModel<Member>(new GeneralKeyProvider<Member>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<Member, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, "Enter keywords to search.");

	// checkable
	private boolean checkable = true;

	// private hashmap attributes to search by
	private Map<String, String> attributesToSearchBy = new HashMap<String, String>();

	private int voId = 0;



	/**
	 * Creates a new request
	 */
	public GetMembers(int voId) {
		this.voId = voId;
	}

	/**
	 * Creates a new request with custom events
	 * @param events
	 */
	public GetMembers(int voId, JsonCallbackEvents events) {
		this(voId);
		this.events = events;
	}

	public void setVoId(int voId) {
		this.voId = voId;
	}

	/**
	 * Returns table of users
	 * @param
	 */
	public CellTable<Member> getTable(FieldUpdater<Member, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns table of users.
	 * @return
	 */
	public CellTable<Member> getTable(){
		// retrieve data
		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<Member> getEmptyTable(FieldUpdater<Member, String> fu){
		this.tableFieldUpdater = fu;
		return this.getEmptyTable();
	};

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<Member> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<Member>(list);

		// Cell table
		table = new PerunTable<Member>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<Member> columnSortHandler = new ListHandler<Member>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Member> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// columns
		if (checkable) {
			table.addCheckBoxColumn();
		}
		table.addIdColumn("Member ID", tableFieldUpdater);

		Column<Member, String> voIdColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Member, String>() {
			public String getValue(Member member) {
				return ""+member.getVoId();
			}
		},tableFieldUpdater);
		voIdColumn.setSortable(true);
		columnSortHandler.setComparator(voIdColumn, new Comparator<Member>() {
			public int compare(Member o1, Member o2) {
				return (o1.getVoId()+"").compareTo(o2.getVoId()+"");
			}
		});

		Column<Member, String> userIdColumn = JsonUtils.addColumn(new JsonUtils.GetValue<Member, String>() {
			public String getValue(Member resource) {
				return ""+resource.getUserId();
			}
		},tableFieldUpdater);
		userIdColumn.setSortable(true);
		columnSortHandler.setComparator(userIdColumn, new Comparator<Member>() {
			public int compare(Member o1, Member o2) {
				return (o1.getUserId()+"").compareTo(o2.getUserId()+"");
			}
		});

		table.addColumn(voIdColumn, "Vo ID");
		table.addColumn(userIdColumn, "User ID");

		// Status column
		final Column<Member, String> statusColumn = new Column<Member, String>(
				new PerunStatusCell()) {
			@Override
			public String getValue(Member object) {
				return object.getStatus();
			}
		};
		// own onClick tab for changing member's status
		statusColumn.setFieldUpdater(new FieldUpdater<Member,String>(){
			@Override
			public void update(final int index, final Member object, final String value) {
				PerunWebSession.getInstance().getTabManager().addTabToCurrentTab(new ChangeStatusTabItem(object.cast(), new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						Member m = jso.cast();
						// set status to object in cell to change rendered value
						object.setStatus(m.getStatus());
						dataProvider.refresh();
						dataProvider.flush();
					}
				}));
			}
		});

		// add column
		table.addColumn(statusColumn, "Status");

		// status column sortable
		statusColumn.setSortable(true);
		table.getColumnSortHandler().setComparator(statusColumn, new GeneralComparator<Member>(GeneralComparator.Column.STATUS));

		return table;

	}

	/**
	 * Do search
	 */
	public void search(){

		loaderImage.setEmptyResultMessage("No members found.");

		clearTable();
		retrieveData();
	}

	/**
	 * Add parameter
	 */
	public void addSearchParameter(String name, String value){
		attributesToSearchBy.put(name, value);
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {
		// empty
		if(this.attributesToSearchBy.size() == 0){
			session.getUiElements().setLogText("No keywords.");
			return;
		}

		// ok, start
		loaderImage.loadingStart();

		// build request

		JSONObject attributesWithSearchingValues = new JSONObject();
		for(Map.Entry<String, String> entry : attributesToSearchBy.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();

			attributesWithSearchingValues.put(name, new JSONString(value));
		}

		JSONObject req = new JSONObject();
		req.put("userAttributesWithSearchingValues", attributesWithSearchingValues);
		req.put("vo", new JSONNumber(voId));

		// send request
		JsonPostClient js = new JsonPostClient(new JsonCallbackEvents(){
			public void onError(PerunError error) {
				session.getUiElements().setLogErrorText("Error while loading members.");
				loaderImage.loadingError(error);
				events.onError(error);
			}
			public void onLoadingStart() {
				loaderImage.loadingStart();
				session.getUiElements().setLogText("Loading members started.");
				events.onLoadingStart();
			}
			public void onFinished(JavaScriptObject jso) {
				loaderImage.loadingFinished();
				setList(JsonUtils.<Member>jsoAsList(jso));
				sortTable();
				session.getUiElements().setLogText("Members loaded: " + list.size());
				events.onFinished(jso);
			}


		});
		js.sendData(JSON_URL, req);

		return;
	}

	/**
	 * Sorts table by objects Name
	 */
	public void sortTable() {
		list = new TableSorter<Member>().sortByName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Add object as new row to table
	 *
	 * @param object Member to be added as new row
	 */
	public void addToTable(Member object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Removes object as row from table
	 *
	 * @param object Member to be removed as row
	 */
	public void removeFromTable(Member object) {
		list.remove(object);
		selectionModel.getSelectedSet().remove(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Clear all table content
	 */
	public void clearTable(){
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
	public ArrayList<Member> getTableSelectedList(){
		return JsonUtils.setToList(selectionModel.getSelectedSet());
	}

	/**
	 * Called, when an error occurs
	 */
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading members.");
		loaderImage.loadingError(error);
		events.onError(error);
	}

	/**
	 * Called, when loading starts
	 */
	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading members started.");
		events.onLoadingStart();
	}

	/**
	 * Called, when operation finishes successfully.
	 */
	public void onFinished(JavaScriptObject jso) {
		loaderImage.loadingFinished();
		setList(JsonUtils.<Member>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Members loaded: " + list.size());
		events.onFinished(jso);
	}

	public void insertToTable(int index, Member object) {
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

	public void setList(ArrayList<Member> list) {
		clearTable();
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<Member> getList() {
		return this.list;
	}


	public void setSelected(Member resource) {
		selectionModel.setSelected(resource, true);
	}

	public void setEvents(JsonCallbackEvents event) {
		this.events = event;
	}

	public void clearParameters() {
		this.attributesToSearchBy.clear();
	}


}
