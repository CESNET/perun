package cz.metacentrum.perun.webgui.json.authzResolver;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.comparators.RichUserComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.GeneralKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get VO/GROUP/FACILITY admins
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 9a01144eeae23b02c6b0591020c774f8f70e6ded $
 */
public class GetRichAdminsWithAttributes implements JsonCallback, JsonCallbackTable<User> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	private static final String GROUP_JSON_URL = "groupsManager/getRichAdminsWithSpecificAttributes";
    private static final String VO_JSON_URL = "vosManager/getRichAdminsWithSpecificAttributes";
    private static final String FACILITY_JSON_URL = "facilitiesManager/getRichAdminsWithSpecificAttributes";

    // entity ID
	private int entityId;
    private ArrayList<String> attributes = new ArrayList<String>();
	// Selection model for the table
	final MultiSelectionModel<User> selectionModel = new MultiSelectionModel<User>(new GeneralKeyProvider<User>());
	// Table data provider.
	private ListDataProvider<User> dataProvider = new ListDataProvider<User>();
	// Cell table
	private PerunTable<User> table;
	// List of members
	private ArrayList<User> list = new ArrayList<User>();
	// Table field updater
	private FieldUpdater<User, String> tableFieldUpdater;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();
	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();
    private PerunEntity entity;
    private boolean checkable = true; // default true

	/**
	 * Creates a new instance of callback
	 *
     * @param entity entity
	 * @param id entity ID
     * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
     */
	public GetRichAdminsWithAttributes(PerunEntity entity, int id, ArrayList<String> attributes) {
		this.entity = entity;
		this.entityId = id;
        // if null use default
        if (attributes == null) {
            this.attributes = JsonUtils.getAttributesListForUserTables();
        } else {
            this.attributes = attributes;
        }
	}

	/**
	 * Creates a new instance of callback
	 *
     * @param entity entity
     * @param id entity ID
     * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
     * @param events events
     */
	public GetRichAdminsWithAttributes(PerunEntity entity, int id, ArrayList<String> attributes, JsonCallbackEvents events) {
		this(entity, id, attributes);
		this.events = events;
	}

	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData() {

        JsonClient js = new JsonClient(60000);
        String param;

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            param = "vo="+entityId;
            if (!attributes.isEmpty()) {
                // parse lists
                for (String attribute : attributes) {
                    param += "&specificAttributes[]=" + attribute;
                }
            }
            js.retrieveData(VO_JSON_URL, param, this);
        } else if (entity.equals(PerunEntity.GROUP)) {
            param = "group="+entityId;
            if (!attributes.isEmpty()) {
                // parse lists
                for (String attribute : attributes) {
                    param += "&specificAttributes[]=" + attribute;
                }
            }
            js.retrieveData(GROUP_JSON_URL, param, this);
        } else if (entity.equals(PerunEntity.FACILITY)) {
            param = "facility="+entityId;
            if (!attributes.isEmpty()) {
                // parse lists
                for (String attribute : attributes) {
                    param += "&specificAttributes[]=" + attribute;
                }
            }
            js.retrieveData(FACILITY_JSON_URL, param, this);
        }




	}

	/**
	 * Returns the table with member-users
	 * 
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<User> getTable(FieldUpdater<User, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with member-users
	 * 
	 * @return CellTable widget
	 */
	public CellTable<User> getTable() {

		// Retrieves data
		this.retrieveData();

		// Table data provider.
		dataProvider = new ListDataProvider<User>(list);

		// Cell table
		table = new PerunTable<User>(list);
		
		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            loaderImage.setEmptyResultMessage("VO has no managers.");
        } else if (entity.equals(PerunEntity.GROUP)) {
            loaderImage.setEmptyResultMessage("Group has no managers.");
        } else if (entity.equals(PerunEntity.FACILITY)) {
            loaderImage.setEmptyResultMessage("Facility has no managers.");
        }

		// Sorting
		ListHandler<User> columnSortHandler = new ListHandler<User>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);
		
		// Table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<User> createCheckboxManager());

		// Set empty content & loader
		table.setEmptyTableWidget(loaderImage);

        // Checkbox column column
        if (checkable) {
            table.addCheckBoxColumn();
        }

		// Create User ID column.
		Column<User, String> userIdColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<User, String>() {
					public String getValue(User object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);
		

		userIdColumn.setSortable(true);
		columnSortHandler.setComparator(userIdColumn, new GeneralComparator<User>(GeneralComparator.Column.ID));
		
		table.setColumnWidth(userIdColumn, 110.0, Unit.PX);
		
		if(JsonUtils.isExtendedInfoVisible()){
			table.addColumn(userIdColumn,  "User ID");
		}

        table.setHyperlinksAllowed(false);
        table.addNameColumn(tableFieldUpdater);

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
        table.addColumn(organizationColumn, "Organization");
        table.addColumn(emailColumn, "E-mail");
        table.addColumn(loginsColumn, "Logins");

		return table;
		
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
     * @param object User to be added as new row
     */
    public void addToTable(User object) {
        list.add(object);
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object User to be removed as row
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
        session.getUiElements().setLogErrorText("Error while loading admins.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading admins started.");
        events.onLoadingStart();
    }

    /**
     * Called, when operation finishes successfully.
     */
    public void onFinished(JavaScriptObject jso) {
        setList(JsonUtils.<User>jsoAsList(jso));
        sortTable();
        session.getUiElements().setLogText("Admins loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();
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

	/**
	 * Sets external events after callback creation
	 * 
	 * @param externalEvents external events
	 */
	public void setEvents(JsonCallbackEvents externalEvents) {
		events = externalEvents;
	}

}