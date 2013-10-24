package cz.metacentrum.perun.webgui.json.membersManager;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunStatus;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.comparators.RichMemberComparator;
import cz.metacentrum.perun.webgui.json.keyproviders.RichMemberKeyProvider;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.cells.PerunStatusCell;
import cz.metacentrum.perun.webgui.widgets.PerunTable;

import java.util.ArrayList;

/**
 * Ajax query to get all RichMembers of VO / Group with list of selected attributes
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class GetCompleteRichMembers implements JsonCallback, JsonCallbackTable<RichMember>, JsonCallbackOracle<RichMember> {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();
	// JSON URL
	private static final String JSON_URL = "membersManager/getCompleteRichMembers";
	// VO ID
    private PerunEntity entity;
	private int entityId;
    private ArrayList<String> attributes = new ArrayList<String>();

	// Selection model for the table
	final MultiSelectionModel<RichMember> selectionModel = new MultiSelectionModel<RichMember>(new RichMemberKeyProvider());
	// Table data provider.
	private ListDataProvider<RichMember> dataProvider = new ListDataProvider<RichMember>();
	// Cell table
	private PerunTable<RichMember> table;
	// List of members
	private ArrayList<RichMember> list = new ArrayList<RichMember>();
	// Table field updater
	private FieldUpdater<RichMember, String> tableFieldUpdater;
	private boolean checkable = true;
	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	// loader image
	private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

    // oracle
    private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
    private ArrayList<RichMember> backupList = new ArrayList<RichMember>();

    private ArrayList<PerunStatus> allowedStatuses = new ArrayList<PerunStatus>();

	/**
	 * Creates a new instance of the method
	 *
     * @param entity PerunEntity (VO/Group)
	 * @param id entityId
     * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
     */
	public GetCompleteRichMembers(PerunEntity entity, int id, ArrayList<String> attributes) {
        this.entity = entity;
		this.entityId = id;
        // if null use default
        if (attributes == null) {
            this.attributes = JsonUtils.getAttributesListForMemberTables();
        } else {
            this.attributes = attributes;
        }
        // by default load without "disabled"
        for (PerunStatus s : PerunStatus.values()) {
            if (!s.equals(PerunStatus.DISABLED) && !s.equals(PerunStatus.EXPIRED)) {
                allowedStatuses.add(s);
            }
        }
	}

	/**
	 * Creates a new instance of the method
	 *
     * @param entity PerunEntity (VO/Group)
     * @param id entityId
     * @param attributes list of attributes urns (null if default / empty if all / explicit for selection)
	 * @param events external events
	 */
	public GetCompleteRichMembers(PerunEntity entity, int id, ArrayList<String> attributes, JsonCallbackEvents events) {
        this(entity, id, attributes);
        this.events = events;
	}

	/**
	 * Retrieves members from RPC
	 */
	public void retrieveData() {

        String param = "";
        if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
            param = "vo=" + entityId;
        } else if (PerunEntity.GROUP.equals(entity)) {
            param = "group=" + entityId+"&lookingInParentGroup=0";
        } else if (PerunEntity.GROUP_PARENT.equals(entity)) {
            param = "group=" + entityId+"&lookingInParentGroup=1";
        }
        if (!attributes.isEmpty()) {
            // parse lists
            for (String attribute : attributes) {
                param += "&attrsNames[]=" + attribute;
            }
        }
        if (!allowedStatuses.isEmpty()) {
            // parse list
            for (PerunStatus s : allowedStatuses) {
                param += "&allowedStatuses[]="+s.toString();
            }
        }

		JsonClient js = new JsonClient(120000);
		js.retrieveData(JSON_URL, param, this);
		
	}

	/**
	 * Returns the table with member-users
	 * 
	 * @param fu Custom field updater
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getTable(FieldUpdater<RichMember, String> fu) {
		this.tableFieldUpdater = fu;
		return this.getTable();
	}

	/**
	 * Returns the table with member-users
	 * 
	 * @return CellTable widget
	 */
	public CellTable<RichMember> getTable() {
		
		retrieveData();
	
		// Table data provider.
		dataProvider = new ListDataProvider<RichMember>(list);

		// Cell table
		table = new PerunTable<RichMember>(list);

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Sorting
		ListHandler<RichMember> columnSortHandler = new ListHandler<RichMember>(dataProvider.getList());
		table.addColumnSortHandler(columnSortHandler);
		
		// table selection
		table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<RichMember> createCheckboxManager());

		// set empty content & loader
		table.setEmptyTableWidget(loaderImage);
		
		// checkbox column column
		if (checkable) {
			table.addCheckBoxColumn();	
		}
		
		// Status column 
		Column<RichMember, String> statusColumn = new Column<RichMember, String>(
				new PerunStatusCell()) {
			@Override
			public String getValue(RichMember object) {
				return object.getStatus();
			}
		};
		// own onClick tab for changing member's status
		statusColumn.setFieldUpdater(new FieldUpdater<RichMember,String>(){
			public void update(int index, final RichMember object, String value) {
				FlexTable widget = new FlexTable();
				final ListBox lb = new ListBox(false);
				lb.addItem("VALID", "VALID");
				lb.addItem("INVALID", "INVALID");
				lb.addItem("SUSPENDED", "SUSPENDED");
				lb.addItem("EXPIRED", "EXPIRED");
				lb.addItem("DISABLED", "DISABLED");
				widget.setHTML(0, 0, "<strong>Status: </strong>");
				widget.setWidget(0, 1, lb);
				
				// pick which one is already set
				for (int i=0; i<lb.getItemCount(); i++) {
					if (lb.getItemText(i).equalsIgnoreCase(object.getStatus())) {
						lb.setSelectedIndex(i);
					}
				}
				
				Confirm conf = new Confirm("Change member's status: "+object.getUser().getFullName(), widget, true);
				conf.setCancelButtonText("Cancel");
				conf.setOkButtonText("Change status");
				conf.setOkClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						SetStatus call = new SetStatus(object.getId(), new JsonCallbackEvents(){
							public void onFinished(JavaScriptObject jso) {
								clearTable();
								retrieveData();
							}
							public void onError(PerunError error) {
								clearTable();
								retrieveData();
							}
						});
						call.setStatus(lb.getValue(lb.getSelectedIndex()));
					}
				});
				conf.show();
			}
		});
		
		// status column sortable
		statusColumn.setSortable(true);
		columnSortHandler.setComparator(statusColumn, new GeneralComparator<RichMember>(GeneralComparator.Column.STATUS));
		                
		//table.addColumn(checkBoxColumn, checkBoxHeader);
		table.addColumn(statusColumn, "Status");
		table.setColumnWidth(statusColumn, 20, Unit.PX);

		// Create member ID column.
		Column<RichMember, String> memberIdColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichMember, String>() {
					public String getValue(RichMember object) {
						return String.valueOf(object.getId());
					}
				}, this.tableFieldUpdater);
		
		// Create User ID column.
		Column<RichMember, String> userIdColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichMember, String>() {
					public String getValue(RichMember object) {
						return String.valueOf(object.getUser().getId());
					}
				}, this.tableFieldUpdater);
		
		columnSortHandler.setComparator(memberIdColumn, new RichMemberComparator(RichMemberComparator.Column.MEMBER_ID));
		memberIdColumn.setSortable(true);
		
		
		userIdColumn.setSortable(true);
		columnSortHandler.setComparator(userIdColumn,  new RichMemberComparator(RichMemberComparator.Column.USER_ID));
		
		
		table.setColumnWidth(memberIdColumn, 110.0, Unit.PX);
		table.setColumnWidth(userIdColumn, 110.0, Unit.PX);
		
		// adding columns
		if(JsonUtils.isExtendedInfoVisible()){
			table.addColumn(memberIdColumn, "Member ID");
			table.addColumn(userIdColumn,  "User ID");
		}

		// Create name column.
		Column<RichMember, String> nameColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichMember, String>() {
					public String getValue(RichMember object) {
						return object.getUser().getFullName();
					}
				}, this.tableFieldUpdater);

		// Create organization column.
        Column<RichMember, String> organizationColumn = JsonUtils.addColumn(
                new JsonUtils.GetValue<RichMember, String>() {
                    public String getValue(RichMember object) {

                        Attribute at = object.getAttribute("urn:perun:member:attribute-def:def:organization");
                        if (at == null || at.getValue().equalsIgnoreCase("null")) {
                            at = object.getAttribute("urn:perun:user:attribute-def:def:organization");
                        }
                        String value = "";

                        if (at != null) {
                            value = at.getValue();
                        }
                        if (value.equalsIgnoreCase("null")) {
                            return "";
                        }
                        return value;
                    }
                }, this.tableFieldUpdater);
		
		// Create e-mail column.
		Column<RichMember, String> emailColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichMember, String>() {
					public String getValue(RichMember object) {

						Attribute at = object.getAttribute("urn:perun:user:attribute-def:def:preferredMail");
						if (at == null || at.getValue().equalsIgnoreCase("null")) {
							at = object.getAttribute("urn:perun:member:attribute-def:def:mail");
						}
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
		Column<RichMember, String> loginsColumn = JsonUtils.addColumn(
				new JsonUtils.GetValue<RichMember, String>() {
					public String getValue(RichMember object) {
						return object.getUserLogins();
					}
				}, this.tableFieldUpdater);

		organizationColumn.setSortable(true);
		columnSortHandler.setComparator(organizationColumn, new RichMemberComparator(RichMemberComparator.Column.ORGANIZATION));

		emailColumn.setSortable(true);
		columnSortHandler.setComparator(emailColumn, new RichMemberComparator(RichMemberComparator.Column.EMAIL));

		nameColumn.setSortable(true);
		columnSortHandler.setComparator(nameColumn, new RichMemberComparator(RichMemberComparator.Column.USER_FULL_NAME));

		// updates the columns size
        table.setColumnWidth(emailColumn, 240, Unit.PX);

		// Add the other columns.
		table.addColumn(nameColumn, "Name");
        table.addColumn(organizationColumn, "Organization");
		table.addColumn(emailColumn, "E-mail");
		table.addColumn(loginsColumn, "Logins");
		return table;
		
	}

    /**
     * Sorts table by objects date
     */
    public void sortTable() {
        list = new TableSorter<RichMember>().sortByName(getList());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Add object as new row to table
     *
     * @param object Resource to be added as new row
     */
    public void addToTable(RichMember object) {
        list.add(object);
        oracle.add(object.getUser().getFullName());
        dataProvider.flush();
        dataProvider.refresh();
    }

    /**
     * Removes object as row from table
     *
     * @param object Resource to be removed as row
     */
    public void removeFromTable(RichMember object) {
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
    public ArrayList<RichMember> getTableSelectedList(){
        return JsonUtils.setToList(selectionModel.getSelectedSet());
    }

    /**
     * Called, when an error occurs
     */
    public void onError(PerunError error) {
        session.getUiElements().setLogErrorText("Error while loading Members.");
        loaderImage.loadingError(error);
        events.onError(error);
    }

    /**
     * Called, when loading starts
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading Members started.");
        events.onLoadingStart();
    }

    /**
     * Called when loading successfully finishes.
     */
    public void onFinished(JavaScriptObject jso) {
        // sorted backup
        backupList = new TableSorter<RichMember>().sortByName(JsonUtils.<RichMember>jsoAsList(jso));
        setList(backupList);
        session.getUiElements().setLogText("Members loaded: " + list.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();
    }

    public void insertToTable(int index, RichMember object) {
        list.add(index, object);
        backupList.add(index, object);
        oracle.add(object.getUser().getFullName());
        dataProvider.flush();
        dataProvider.refresh();
    }

    public void setEditable(boolean editable) {
        // TODO Auto-generated method stub
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public void setList(ArrayList<RichMember> list) {
        clearTable();
        this.list.addAll(list);
        for (RichMember m : list) {
            oracle.add(m.getUser().getFullName());
        }
        dataProvider.flush();
        dataProvider.refresh();
    }

    public ArrayList<RichMember> getList() {
        return this.list;
    }

	/**
	 * Helper method to set events later
	 * @param externalEvents
	 */
	public void setEvents(JsonCallbackEvents externalEvents) {
		events = externalEvents;
	}

    @Override
    public void filterTable(String filter) {

        // always clear selected items
        selectionModel.clear();
        list.clear();

        // filter table content
        if (filter.equalsIgnoreCase("")) {
            if (backupList.isEmpty()) {
                // table is empty - try to reload data

                // TODO - SOLVE THIS

                //clearTable();
                //retrieveData();
                return;
            } else {
                // not empty, filter data
                list.addAll(backupList);
            }
        } else {
            for (RichMember m : backupList){
                // store member by filter
                if (m.getUser().getFullName().toLowerCase().startsWith(filter.toLowerCase())) {
                    addToTable(m);
                }
            }
            if (getList().isEmpty()) {
                loaderImage.loadingFinished();
            }
        }
        dataProvider.flush();
        dataProvider.refresh();
    }

    @Override
    public MultiWordSuggestOracle getOracle() {
        return this.oracle;
    }

    @Override
    public void setOracle(MultiWordSuggestOracle oracle) {
        this.oracle = oracle;
    }

    /**
     * Exclude disabled members from showing in table
     * Table must be manually cleared and loaded afterwards !!
     *
     * @param exclude TRUE for exclude / FALSE to keep
     */
    public void excludeDisabled(boolean exclude) {

        allowedStatuses.clear();
        for (PerunStatus s : PerunStatus.values()) {
            if (exclude && (s.equals(PerunStatus.DISABLED) || s.equals(PerunStatus.EXPIRED))) {
                continue;
            } else {
                allowedStatuses.add(s);
            }
        }

    }

}