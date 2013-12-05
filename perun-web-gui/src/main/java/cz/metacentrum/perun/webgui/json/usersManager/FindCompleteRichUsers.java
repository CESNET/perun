package cz.metacentrum.perun.webgui.json.usersManager;

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
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.RichUserComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Callback to find RichUsers with attributes by name/login/email, optionally users without specific VO
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 84a914c276f213b9675292d95fd6e0d0ae3adc5c $
 */
public class FindCompleteRichUsers implements JsonCallback, JsonCallbackTable<User> {

	// session
	private PerunWebSession session = PerunWebSession.getInstance();
	// json url
	static private final String JSON_URL = "usersManager/findRichUsersWithAttributes";
    static private final String JSON_URL_WITHOUT_VO = "usersManager/findRichUsersWithoutSpecificVoWithAttributes";
	// Data provider
	private ListDataProvider<User> dataProvider = new ListDataProvider<User>();
	// table
	private PerunTable<User> table;
	// table data
	private ArrayList<User> list = new ArrayList<User>();
	// Selection model
	final MultiSelectionModel<User> selectionModel = new MultiSelectionModel<User>(new GeneralKeyProvider<User>());
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// Table field updater
	private FieldUpdater<User, String> tableFieldUpdater;
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage(true, WidgetTranslation.INSTANCE.emptySearchForUsers());

	// parameters
	private String searchString;
	// checkable
	private boolean checkable = true;
    private ArrayList<String> attributes = new ArrayList<String>();
    private boolean hideService = false;
    private boolean hidePerson = false;
    private boolean withoutVo = false;
    private int voId = 0;


    /**
     * Creates a new request
     *
     * @param searchString
     * @param attributes
     */
    public FindCompleteRichUsers(String searchString, ArrayList<String> attributes) {
        this.searchString = searchString;
        // if null use default
        if (attributes == null) {
            this.attributes = JsonUtils.getAttributesListForUserTables();
        } else {
            this.attributes = attributes;
        }
    }

	/**
	 * Creates a new request with custom events
     *
     * @param searchString
     * @param events
     * @param attributes
	 */
	public FindCompleteRichUsers(String searchString, JsonCallbackEvents events, ArrayList<String> attributes) {
		this.events = events;
		this.searchString = searchString;
        // if null use default
        if (attributes == null) {
            this.attributes = JsonUtils.getAttributesListForUserTables();
        } else {
            this.attributes = attributes;
        }
	}

	/**
	 * Returns table of users
	 * @param
	 */
	public CellTable<User> getTable(FieldUpdater<User, String> fu){
		this.tableFieldUpdater = fu;
		return this.getTable();
	}
	
	/**
	 * Returns table of users.
	 * @return
	 */
	public CellTable<User> getTable(){ 
		// retrieve data
		retrieveData();
		return getEmptyTable();
		
	}

	/**
	 * Returns empty table definition
	 * @return
	 */
	public CellTable<User> getEmptyTable(){

		// Table data provider.
		dataProvider = new ListDataProvider<User>(list);

		// Cell table
		table = new PerunTable<User>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<User> columnSortHandler = new ListHandler<User>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);

		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<User> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);

		// columns
		if (checkable) {
			table.addCheckBoxColumn();	
		}
		table.addIdColumn("User ID", tableFieldUpdater);

		// NAME COLUMN
		Column<User, String> nameColumn = JsonUtils.addColumn(new JsonUtils.GetValue<User, String>() {
			public String getValue(User user) {
				return user.getFullNameWithTitles(); // display full name with titles
			}
		},tableFieldUpdater);

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return o1.getFullName().compareToIgnoreCase(o2.getFullName());  // sort by name without titles
			}
		});

		// SERVICE COLUMN
		Column<User, String> serviceColumn = JsonUtils.addColumn(new JsonUtils.GetValue<User, String>() {
			public String getValue(User user) {
				if (user.isServiceUser()) {
					return "Service";
				} else {
					return "Person";
				}
			}
		},tableFieldUpdater);

		serviceColumn.setSortable(true);
		columnSortHandler.setComparator(serviceColumn, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return String.valueOf(o1.isServiceUser()).compareToIgnoreCase(String.valueOf(o2.isServiceUser()));  // sort by name without titles
			}
		});

        // Create organization column.
        Column<User, String> organizationColumn = JsonUtils.addColumn(
                new JsonUtils.GetValue<User, String>() {
                    public String getValue(User object) {
                        Attribute at = object.getAttribute("urn:perun:user:attribute-def:def:organization");
                        String value = "";
                        if (at != null) {
                            value = at.getValue();
                        }
                        return value;
                    }
                }, this.tableFieldUpdater);

        // Create e-mail column.
        Column<User, String> emailColumn = JsonUtils.addColumn(
                new JsonUtils.GetValue<User, String>() {
                    public String getValue(User object) {

                        Attribute at = object.getAttribute("urn:perun:user:attribute-def:def:preferredMail");

                        String value = "";

                        if (at != null) {
                            value = at.getValue();
                            // replace "," to " " in emails
                            value = value.replace(",", " ");
                        }

                        return value;
                    }
                }, this.tableFieldUpdater);

        // Create name column.
        Column<User, String> loginsColumn = JsonUtils.addColumn(
                new JsonUtils.GetValue<User, String>() {
                    public String getValue(User object) {
                        return object.getLogins();
                    }
                }, this.tableFieldUpdater);

        organizationColumn.setSortable(true);
        columnSortHandler.setComparator(organizationColumn, new RichUserComparator(RichUserComparator.Column.ORGANIZATION));

        emailColumn.setSortable(true);
        columnSortHandler.setComparator(emailColumn, new RichUserComparator(RichUserComparator.Column.EMAIL));

        // Add the other columns.
        table.addColumn(nameColumn, "Name");
        table.addColumn(organizationColumn, "Organization");
        table.addColumn(emailColumn, "E-mail");
        table.addColumn(loginsColumn, "Logins");
		table.addColumn(serviceColumn, "User type");

		return table;

	}

	/**
	 * Users search
	 * @param query
	 */
	public void searchFor(String query){
		this.searchString = query;
		clearTable();
		retrieveData();
	}

	/**
	 * Retrieves data from RPC
	 */
	public void retrieveData() {

        // skip empty searches
		if(searchString==null || searchString.isEmpty()) return;

		JsonClient js = new JsonClient(120000);

        String param = "searchString=" + this.searchString;
        if (!attributes.isEmpty()) {
            // parse lists
            for (String attribute : attributes) {
                param += "&attrsNames[]=" + attribute;
            }
        }

        if (!withoutVo) {
            js.retrieveData(JSON_URL, param, this);
        } else {
            // users without specific VO
            param += "&vo="+voId;
            js.retrieveData(JSON_URL_WITHOUT_VO, param, this);
        }

	}

    /**
     * Sorts table by objects Name
     */
    public void sortTable() {
        list = new TableSorter<User>().sortByName(getList());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Add object as new row to table
     *
     * @param object user to be added as new row
     */
    public void addToTable(User object) {
        list.add(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object user to be removed as row
     */
    public void removeFromTable(User object) {
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
    public ArrayList<User> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading users.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading users started.");
        events.onLoadingStart();
    }

    /**
     * Called, when operation finishes successfully.
     */
    public void onFinished(JavaScriptObject jso) {
        ArrayList<User> list = JsonUtils.jsoAsList(jso);
        for (User u : list) {
            if (hideService && u.isServiceUser())  {
                // if service hidden, skip service users
            } else if (hidePerson && !u.isServiceUser()) {
                // if person hidden, skip person
            } else {
                addToTable(u);
            }
        }
        sortTable();
        loaderImage.loadingFinished();
        session.getUiElements().setLogText("Users loaded: " + list.size());
        events.onFinished(jso);
    }

    public void insertToTable(int index, User object) {
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

    public void setList(ArrayList<User> list) {
        clearTable();
        this.list.addAll(list);
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<User> getList() {
        return this.list;
    }

	public void setSelected(User user) {
		selectionModel.setSelected(user, true);
	}

	public void setEvents(JsonCallbackEvents event) {
		this.events = event;
	}

	public void setSearchString(String text) {
		this.searchString = text;
	}

    public boolean isHideService() {
        return hideService;
    }

    public void hideService(boolean hideService) {
        this.hideService = hideService;
    }

    public boolean isHidePerson() {
        return hidePerson;
    }

    public void hidePerson(boolean hidePerson) {
        this.hidePerson = hidePerson;
    }

    public void findWithoutVo(boolean without, int voId) {
        if (without) {
            this.voId = voId;
        } else {
            this.voId = 0;
        }
        this.withoutVo= without;
    }

}