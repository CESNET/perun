package cz.metacentrum.perun.webgui.json.vosManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.Candidate;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query for finding candidates in External sources or Users from Perun so they can become VO members
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FindCandidatesOrUsersToAddToVo implements JsonCallback, JsonCallbackTable<Candidate> {

	// JSON URL
	private final String JSON_URL = "vosManager/findCandidates";
	static private final String JSON_URL_WITHOUT_VO = "usersManager/findRichUsersWithoutSpecificVoWithAttributes";
	private ArrayList<String> attributes = JsonUtils.getAttributesListForUserTables();

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
	private int groupId = 0;
	private String searchString = "";

	private int callCounter = 0;

	/**
	 * Creates a new instance of the request
	 *
	 * @param voId for which VO we search candidates for
	 * @param groupId
	 * @param searchString part of the user's login
	 */
	public FindCandidatesOrUsersToAddToVo(int voId, int groupId, String searchString) {
		this.voId = voId;
		this.groupId = groupId;
		this.searchString = searchString;
	}

	/**
	 * Creates a new instance of the request with custom events
	 *
	 * @param events Custom events
	 * @param voId for which VO we search candidates for
	 * @param groupId
	 * @param searchString part of the user's login
	 */
	public FindCandidatesOrUsersToAddToVo(int voId, int groupId, String searchString, JsonCallbackEvents events) {
		this.events = events;
		this.voId = voId;
		this.groupId = groupId;
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
		if (groupId == 0) {

			// search Whole VO as VO_ADMIN

			JsonClient js = new JsonClient(); // set 1 minute timeout for searching external sources
			if (searchString != null && !searchString.isEmpty()) {
				js.retrieveData(JSON_URL, "vo="+voId+"&searchString="+searchString, this);
			}

			// the request itself
			JsonClient js2 = new JsonClient(); // set 1 minute timeout for searching external sources
			if (searchString != null && !searchString.isEmpty()) {

				String param = "vo="+voId+"&searchString="+searchString;

				if (!attributes.isEmpty()) {
					// parse lists
					for (String attribute : attributes) {
						param += "&attrsNames[]=" + attribute;
					}
				}

				js2.retrieveData(JSON_URL_WITHOUT_VO, param, this);
			}

		} else {

			// Search only in group ext sources (GROUP_ADMIN)
			JsonClient js = new JsonClient(); // set 1 minute timeout for searching external sources
			if (searchString != null && !searchString.isEmpty()) {
				js.retrieveData(JSON_URL, "group="+groupId+"&searchString="+searchString, this);
			}

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

		// Full Name Column
		TextColumn<Candidate> fullNameColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				return getFullNameColumnValue(candidate);
			}
		};

		// E-mail column
		TextColumn<Candidate> emailColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				return getEmailColumnValue(candidate);
			}
		};

		// Login Column
		TextColumn<Candidate> loginColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				return getLoginFromExtSourceOrAllLogins(candidate);
			}
		};

		// Ext Source Column
		TextColumn<Candidate> extSourceColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				return getExtSourceNameOrOrganizationColumn(candidate);
			}
		};

		// already Column
		TextColumn<Candidate> alreadyInPerunColumn = new TextColumn<Candidate>() {
			@Override
			public String getValue(Candidate candidate) {
				if (candidate.getObjectType().equalsIgnoreCase("RichUser")) {
					return "Local";
				} else {
					return "External identity";
				}
			}
		};

		alreadyInPerunColumn.setSortable(true);
		columnSortHandler.setComparator(alreadyInPerunColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return o1.getObjectType().compareTo(o2.getObjectType());
			}
		});

		loginColumn.setSortable(true);
		columnSortHandler.setComparator(loginColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return getLoginFromExtSourceOrAllLogins(o1).compareToIgnoreCase(getLoginFromExtSourceOrAllLogins(o2));
			}
		});

		fullNameColumn.setSortable(true);
		columnSortHandler.setComparator(fullNameColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return getFullNameColumnValue(o1).compareToIgnoreCase(getFullNameColumnValue(o2));
			}
		});

		extSourceColumn.setSortable(true);
		columnSortHandler.setComparator(extSourceColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return getExtSourceNameOrOrganizationColumn(o1).compareToIgnoreCase(getExtSourceNameOrOrganizationColumn(o2));
			}
		});

		emailColumn.setSortable(true);
		columnSortHandler.setComparator(emailColumn, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return getEmailColumnValue(o1).compareToIgnoreCase(getEmailColumnValue(o2));
			}
		});

		table.addColumnSortHandler(columnSortHandler);

		// Add the columns.
		table.addColumn(fullNameColumn, "Full name");
		table.addColumn(extSourceColumn, "Organization or Ext source");
		table.addColumn(emailColumn, "E-mail");
		table.addColumn(loginColumn, "Logins");

		table.addColumn(alreadyInPerunColumn, "Source");

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
		callCounter--;
		setList(JsonUtils.<Candidate>jsoAsList(jso));
		sortTable();
		session.getUiElements().setLogText("Candidates loaded: " + list.size());
		if (callCounter == 0) {
			events.onFinished(jso);
			loaderImage.loadingFinished();
		}
	}

	/**
	 * Called when an error occurs
	 */
	public void onError(PerunError error) {
		callCounter--;
		session.getUiElements().setLogErrorText("Error while loading candidates.");
		events.onError(error);
		loaderImage.loadingError(error);
	}

	/**
	 * Called when loading starts
	 */
	public void onLoadingStart() {
		callCounter++;
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
		selectionModel.clear();
	}

	public ArrayList<Candidate> getTableSelectedList() {
		if (selectionModel.getSelectedObject() != null) {
			return JsonUtils.toList(selectionModel.getSelectedObject());
		} else {
			return new ArrayList<Candidate>();
		}
	}

	public void setList(ArrayList<Candidate> list) {
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

	public static String getFullNameColumnValue(Candidate candidate) {

		if (candidate.getObjectType().equalsIgnoreCase("RichUser")) {
			User user = candidate.cast();
			return user.getFullNameWithTitles();
		}
		return candidate.getDisplayName();

	}

	public static String getExtSourceNameOrOrganizationColumn(Candidate candidate) {

		if (candidate.getObjectType().equalsIgnoreCase("RichUser")) {

			User user = candidate.cast();

			Attribute at = user.getAttribute("urn:perun:user:attribute-def:def:organization");
			if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
				return at.getValue();
			}
			return "";

		}

		// translate ext source name if possible

		if (candidate.getUserExtSource().getExtSource().getType().equalsIgnoreCase("cz.metacentrum.perun.core.impl.ExtSourceX509")) {
			return Utils.convertCertCN(candidate.getUserExtSource().getExtSource().getName());
		} else if (candidate.getUserExtSource().getExtSource().getType().equals("cz.metacentrum.perun.core.impl.ExtSourceIdp")) {
			return Utils.translateIdp(candidate.getUserExtSource().getExtSource().getName());
		} else {
			return candidate.getUserExtSource().getExtSource().getName();
		}

	}

	public static String getLoginFromExtSourceOrAllLogins(Candidate candidate) {

		if (candidate.getObjectType().equalsIgnoreCase("RichUser")) {
			User user = candidate.cast();
			return user.getLogins();
		}

		String logins = candidate.getLogins();
		if (logins == null || logins.isEmpty()) {
			candidate.getUserExtSource().getLogin();
		}
		return logins;

	}

	public static String getEmailColumnValue(Candidate candidate) {

		if (candidate.getObjectType().equalsIgnoreCase("RichUser")) {
			User user = candidate.cast();

			Attribute at = user.getAttribute("urn:perun:user:attribute-def:def:preferredMail");
			if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
				return at.getValue().replace(",", " ");
			}
			return "";

		}

		if (candidate.getEmail() == null) {
			return "";
		} else {
			return candidate.getEmail();
		}

	}

}
