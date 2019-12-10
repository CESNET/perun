package cz.metacentrum.perun.webgui.json.vosManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.MemberCandidate;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.PerunCheckboxCell;
import cz.metacentrum.perun.webgui.widgets.cells.PerunStatusCell;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Ajax query for finding complete candidates in External sources or Users from Perun so they can become VO members
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetCompleteCandidates implements JsonCallback, JsonCallbackTable<MemberCandidate> {

	// JSON URL
	private final String JSON_URL = "vosManager/getCompleteCandidates";
	private ArrayList<String> attributes = JsonUtils.getAttributesListForUserTables();

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// Data provider.
	private ListDataProvider<MemberCandidate> dataProvider = new ListDataProvider<MemberCandidate>();
	// Table itself
	private PerunTable<MemberCandidate> table;
	// Table list
	private ArrayList<MemberCandidate> list = new ArrayList<MemberCandidate>();
	// Selection model
	final SingleSelectionModel<MemberCandidate> selectionModel = new SingleSelectionModel<MemberCandidate>();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, WidgetTranslation.INSTANCE.emptySearchForCandidates());
	// Custom events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int voId;
	private int groupId = 0;
	private String searchString = "";


	/**
	 * Creates a new instance of the request
	 *
	 * @param voId for which VO we search candidates for
	 * @param groupId
	 * @param searchString part of the user's login
	 */
	public GetCompleteCandidates(int voId, int groupId, String searchString) {
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
	public GetCompleteCandidates(int voId, int groupId, String searchString, JsonCallbackEvents events) {
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

			// search for adding to VO
			JsonClient js = new JsonClient(); // set 1 minute timeout for searching external sources
			if (searchString != null && !searchString.isEmpty()) {

				String param = "vo="+voId+"&searchString="+searchString;
				if (!attributes.isEmpty()) {
					// parse lists
					for (String attribute : attributes) {
						param += "&attrNames[]=" + attribute;
					}
				}

				js.retrieveData(JSON_URL, param, this);
			}

		} else {

			// search for adding to Group
			JsonClient js = new JsonClient();
			if (searchString != null && !searchString.isEmpty()) {

				String param = "group="+groupId+"&searchString="+searchString;
				if (!attributes.isEmpty()) {
					// parse lists
					for (String attribute : attributes) {
						param += "&attrNames[]=" + attribute;
					}
				}

				js.retrieveData(JSON_URL, param, this);
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
	public CellTable<MemberCandidate> getTable(){

		retrieveData();
		return getEmptyTable();

	}

	/**
	 * Returns the empty table widget
	 *
	 * @return table widget
	 */
	public CellTable<MemberCandidate> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<MemberCandidate>(list);

		// Cell table
		table = new PerunTable<MemberCandidate>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<MemberCandidate> columnSortHandler = new ListHandler<MemberCandidate>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// Table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<MemberCandidate> createCheckboxManager());

		// Set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// checkbox column column
		Column<MemberCandidate, MemberCandidate> checkBoxColumn = new Column<MemberCandidate, MemberCandidate>(
				new PerunCheckboxCell<MemberCandidate>(true, false, (groupId == 0))) {
			@Override
			public MemberCandidate getValue(MemberCandidate object) {
				// Get the value from the selection model.
				GeneralObject go = object.cast();
				go.setChecked(selectionModel.isSelected(object));
				return go.cast();
			}
		};

		// updates the columns size
		table.setColumnWidth(checkBoxColumn, 40.0, Style.Unit.PX);

		table.addColumn(checkBoxColumn);

		// is member
		TextColumn<MemberCandidate> memberColumn = new TextColumn<MemberCandidate>() {
			@Override
			public String getValue(MemberCandidate candidate) {

				if (groupId == 0) {
					if (candidate.getMember() != null) return "Member of VO";
				} else {
					if (candidate.getMember() != null &&
							candidate.getMember().getSourceGroupId() != 0 &&
							"DIRECT".equalsIgnoreCase( candidate.getMember().getMembershipType())) return "Member of Group";
					if (candidate.getMember() != null &&
							candidate.getMember().getSourceGroupId() != 0 &&
							"INDIRECT".equalsIgnoreCase( candidate.getMember().getMembershipType())) return "Indirect member of Group";
					if (candidate.getMember() != null) return "Member of VO";
				}
				return "";

			}
		};

		// Status column
		final Column<MemberCandidate, String> statusColumn = new Column<MemberCandidate, String>(
				new PerunStatusCell()) {
			@Override
			public String getValue(MemberCandidate object) {
				if (object.getMember() != null) {
					return object.getMember().getStatus();
				} else {
					return null;
				}
			}
		};

		// Full Name Column
		TextColumn<MemberCandidate> fullNameColumn = new TextColumn<MemberCandidate>() {
			@Override
			public String getValue(MemberCandidate candidate) {
				return getFullNameColumnValue(candidate);
			}
		};

		// E-mail column
		TextColumn<MemberCandidate> emailColumn = new TextColumn<MemberCandidate>() {
			@Override
			public String getValue(MemberCandidate candidate) {
				return getEmailColumnValue(candidate);
			}
		};

		// Login Column
		TextColumn<MemberCandidate> loginColumn = new TextColumn<MemberCandidate>() {
			@Override
			public String getValue(MemberCandidate candidate) {
				return getLoginFromExtSourceOrAllLogins(candidate);
			}
		};

		// Ext Source Column
		TextColumn<MemberCandidate> extSourceColumn = new TextColumn<MemberCandidate>() {
			@Override
			public String getValue(MemberCandidate candidate) {
				return getExtSourceNameOrOrganizationColumn(candidate);
			}
		};

		// already Column
		TextColumn<MemberCandidate> alreadyInPerunColumn = new TextColumn<MemberCandidate>() {
			@Override
			public String getValue(MemberCandidate candidate) {
				if (candidate.getRichUser() != null) {
					return "Local";
				} else {
					return "External identity";
				}
			}
		};

		alreadyInPerunColumn.setSortable(true);
		columnSortHandler.setComparator(alreadyInPerunColumn, new Comparator<MemberCandidate>() {
			public int compare(MemberCandidate o1, MemberCandidate o2) {
				String o1Val = "External identity";
				String o2Val = "External identity";
				if (o1.getRichUser() != null) {
					o1Val = "Local";
				}
				if (o2.getRichUser() != null) {
					o2Val = "Local";
				}
				return o1Val.compareTo(o2Val);
			}
		});

		memberColumn.setSortable(true);
		columnSortHandler.setComparator(memberColumn, new Comparator<MemberCandidate>() {
			public int compare(MemberCandidate o1, MemberCandidate o2) {
				String o1Val = "1";
				String o2Val = "1";
				if (o1.getMember() != null) o1Val = "0";
				if (o2.getMember() != null) o2Val = "0";
				o1Val += getFullNameColumnValue(o1);
				o2Val += getFullNameColumnValue(o2);

				return o1Val.compareTo(o2Val);
			}
		});

		loginColumn.setSortable(true);
		columnSortHandler.setComparator(loginColumn, new Comparator<MemberCandidate>() {
			public int compare(MemberCandidate o1, MemberCandidate o2) {
				return getLoginFromExtSourceOrAllLogins(o1).compareToIgnoreCase(getLoginFromExtSourceOrAllLogins(o2));
			}
		});

		fullNameColumn.setSortable(true);
		columnSortHandler.setComparator(fullNameColumn, new Comparator<MemberCandidate>() {
			public int compare(MemberCandidate o1, MemberCandidate o2) {
				return getFullNameColumnValue(o1).compareToIgnoreCase(getFullNameColumnValue(o2));
			}
		});

		extSourceColumn.setSortable(true);
		columnSortHandler.setComparator(extSourceColumn, new Comparator<MemberCandidate>() {
			public int compare(MemberCandidate o1, MemberCandidate o2) {
				return getExtSourceNameOrOrganizationColumn(o1).compareToIgnoreCase(getExtSourceNameOrOrganizationColumn(o2));
			}
		});

		emailColumn.setSortable(true);
		columnSortHandler.setComparator(emailColumn, new Comparator<MemberCandidate>() {
			public int compare(MemberCandidate o1, MemberCandidate o2) {
				return getEmailColumnValue(o1).compareToIgnoreCase(getEmailColumnValue(o2));
			}
		});

		table.addColumnSortHandler(columnSortHandler);

		// Add the columns.
		table.addColumn(statusColumn, "Status");
		table.addColumn(fullNameColumn, "Full name");
		table.addColumn(extSourceColumn, "Organization or Ext source");
		table.addColumn(emailColumn, "E-mail");
		table.addColumn(loginColumn, "Logins");
		table.addColumn(memberColumn, "Already member");
		table.addColumn(alreadyInPerunColumn, "Source");

		return table;

	}

	/**
	 * Returns selected Candidates
	 *
	 * @return selected candidate
	 */
	public MemberCandidate getSelected() {
		return selectionModel.getSelectedObject();
	}

	/**
	 * Sorts table by objects Names
	 */
	public void sortTable() {
		list = new TableSorter<MemberCandidate>().sortByStatusAndName(getList());
		dataProvider.flush();
		dataProvider.refresh();
	}

	/**
	 * Called when the query successfully finishes
	 *
	 * @param jso The JavaScript object returned from RPC
	 */
	public void onFinished(JavaScriptObject jso) {
		setList(JsonUtils.<MemberCandidate>jsoAsList(jso));
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

	public void insertToTable(int index, MemberCandidate object) {
		list.add(index, object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void addToTable(MemberCandidate object) {
		list.add(object);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public void removeFromTable(MemberCandidate object) {
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

	public ArrayList<MemberCandidate> getTableSelectedList() {
		if (selectionModel.getSelectedObject() != null) {
			return JsonUtils.toList(selectionModel.getSelectedObject());
		} else {
			return new ArrayList<MemberCandidate>();
		}
	}

	public void setList(ArrayList<MemberCandidate> list) {
		this.list.addAll(list);
		dataProvider.flush();
		dataProvider.refresh();
	}

	public ArrayList<MemberCandidate> getList() {
		return this.list;
	}

	public void setEvents(JsonCallbackEvents events){
		this.events = events;
	}

	public void setSelected(MemberCandidate candidate){
		selectionModel.setSelected(candidate, true);
	}

	public static String getFullNameColumnValue(MemberCandidate candidate) {

		if (candidate.getRichUser() != null) {
			User user = candidate.getRichUser();
			return user.getFullNameWithTitles();
		}
		return candidate.getCandidate().getDisplayName();

	}

	public static String getExtSourceNameOrOrganizationColumn(MemberCandidate candidate) {

		if (candidate.getRichUser() != null) {

			User user = candidate.getRichUser();

			Attribute at = user.getAttribute("urn:perun:user:attribute-def:def:organization");
			if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
				return at.getValue();
			}
			return "";

		}

		// translate ext source name if possible

		if (candidate.getCandidate().getUserExtSource().getExtSource().getType().equalsIgnoreCase("cz.metacentrum.perun.core.impl.ExtSourceX509")) {
			return Utils.convertCertCN(candidate.getCandidate().getUserExtSource().getExtSource().getName());
		} else if (candidate.getCandidate().getUserExtSource().getExtSource().getType().equals("cz.metacentrum.perun.core.impl.ExtSourceIdp")) {
			return Utils.translateIdp(candidate.getCandidate().getUserExtSource().getExtSource().getName());
		} else {
			return candidate.getCandidate().getUserExtSource().getExtSource().getName();
		}

	}

	public static String getLoginFromExtSourceOrAllLogins(MemberCandidate candidate) {

		if (candidate.getRichUser() != null) {
			User user = candidate.getRichUser();
			return user.getLogins();
		}

		String logins = candidate.getCandidate().getLogins();
		if (logins == null || logins.isEmpty()) {
			logins = candidate.getCandidate().getUserExtSource().getLogin();
		}
		return logins;

	}

	public static String getEmailColumnValue(MemberCandidate candidate) {

		if (candidate.getRichUser() != null) {
			User user = candidate.getRichUser();

			Attribute at = user.getAttribute("urn:perun:user:attribute-def:def:preferredMail");
			if (at != null && at.getValue() != null && !"null".equalsIgnoreCase(at.getValue())) {
				return at.getValue().replace(",", " ");
			}
			return "";

		}

		if (candidate.getCandidate().getEmail() == null) {
			return "";
		} else {
			return candidate.getCandidate().getEmail();
		}

	}

}
