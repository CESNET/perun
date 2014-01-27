package cz.metacentrum.perun.webgui.json.columnProviders;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonCallbackTable;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.comparators.RichMemberComparator;
import cz.metacentrum.perun.webgui.json.membersManager.SetStatus;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.MembersTabs;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCellWithAuthz;
import cz.metacentrum.perun.webgui.widgets.cells.PerunStatusCell;

/**
 * Provide columns definitions for RichMember object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberColumnProvider {

    private PerunTable<RichMember> table;
    private FieldUpdater<RichMember, RichMember> fieldUpdater;
    private JsonCallbackTable<RichMember> originalCallback;

    /**
     * New instance of MemberColumnProvider
     *
     * @param table table to add columns to
     * @param fieldUpdater field updater used when cell is "clicked"
     */
    public MemberColumnProvider(PerunTable<RichMember> table, FieldUpdater<RichMember, RichMember> fieldUpdater) {
        this.table = table;
        this.fieldUpdater = fieldUpdater;
    }

    /**
     * New instance of MemberColumnProvider
     *
     * @param callback callback associated with table (for refresh purpose)
     * @param table table to add columns to
     * @param fieldUpdater field updater used when cell is "clicked"
     */
    public MemberColumnProvider(JsonCallbackTable<RichMember> callback, PerunTable<RichMember> table, FieldUpdater<RichMember, RichMember> fieldUpdater) {
        this.originalCallback = callback;
        this.table = table;
        this.fieldUpdater = fieldUpdater;
    }

    public void addIdColumn(IsClickableCell authz) {
        addIdColumn(authz, 0);
    }

    public void addIdColumn(IsClickableCell authz, int width) {

        // create column
        Column<RichMember, RichMember> idColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "id"), new JsonUtils.GetValue<RichMember, RichMember>() {
            @Override
            public RichMember getValue(RichMember object) {
                return object;
            }
        }, fieldUpdater);

        // add column only if extended info is visible
        if (JsonUtils.isExtendedInfoVisible()) {

            table.addColumn(idColumn, "Member Id");

            if (width != 0) {
                table.setColumnWidth(idColumn, width, Style.Unit.PX);
            }

            // sort column
            idColumn.setSortable(true);
            table.getColumnSortHandler().setComparator(idColumn, new RichMemberComparator(RichMemberComparator.Column.MEMBER_ID));

        }

    }

    public void addUserIdColumn(IsClickableCell authz) {
        addUserIdColumn(authz, 0);
    }

    public void addUserIdColumn(IsClickableCell authz, int width) {

        // create column
        Column<RichMember, RichMember> idColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "userId"), new JsonUtils.GetValue<RichMember, RichMember>() {
            @Override
            public RichMember getValue(RichMember object) {
                return object;
            }
        }, fieldUpdater);

        // add column only if extended info is visible
        if (JsonUtils.isExtendedInfoVisible()) {

            table.addColumn(idColumn, "User Id");

            if (width != 0) {
                table.setColumnWidth(idColumn, width, Style.Unit.PX);
            }

            // sort column
            idColumn.setSortable(true);
            table.getColumnSortHandler().setComparator(idColumn, new RichMemberComparator(RichMemberComparator.Column.USER_ID));

        }

    }

    public void addNameColumn(IsClickableCell authz) {
        addNameColumn(authz, 0);
    }

    public void addNameColumn(IsClickableCell authz, int width) {

        // create column
        Column<RichMember, RichMember> nameColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "name"), new JsonUtils.GetValue<RichMember, RichMember>() {
            @Override
            public RichMember getValue(RichMember object) {
                return object;
            }
        }, fieldUpdater);

        // add column
        table.addColumn(nameColumn, "Name");
        if (width != 0) {
            table.setColumnWidth(nameColumn, width, Style.Unit.PX);
        }

        // sort column
        nameColumn.setSortable(true);
        table.getColumnSortHandler().setComparator(nameColumn, new RichMemberComparator(RichMemberComparator.Column.USER_FULL_NAME));

    }

    public void addOrganizationColumn(IsClickableCell authz) {
        addOrganizationColumn(authz, 0);
    }

    public void addOrganizationColumn(IsClickableCell authz, int width) {

        // create column
        Column<RichMember, RichMember> organizationColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "organization"), new JsonUtils.GetValue<RichMember, RichMember>() {
            @Override
            public RichMember getValue(RichMember object) {
                return object;
            }
        }, fieldUpdater);

        // add column
        table.addColumn(organizationColumn, "Organization");
        if (width != 0) {
            table.setColumnWidth(organizationColumn, width, Style.Unit.PX);
        }

        // sort column
        organizationColumn.setSortable(true);
        table.getColumnSortHandler().setComparator(organizationColumn, new RichMemberComparator(RichMemberComparator.Column.ORGANIZATION));

    }

    public void addEmailColumn(IsClickableCell authz) {
        addEmailColumn(authz, 0);
    }

    public void addEmailColumn(IsClickableCell authz, int width) {

        // create column
        Column<RichMember, RichMember> emailColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "email"), new JsonUtils.GetValue<RichMember, RichMember>() {
            @Override
            public RichMember getValue(RichMember object) {
                return object;
            }
        }, fieldUpdater);

        // add column
        table.addColumn(emailColumn, "Email");
        if (width != 0) {
            table.setColumnWidth(emailColumn, width, Style.Unit.PX);
        }

        // sort column
        emailColumn.setSortable(true);
        table.getColumnSortHandler().setComparator(emailColumn, new RichMemberComparator(RichMemberComparator.Column.EMAIL));

    }

    public void addLoginsColumn(IsClickableCell authz) {
        addLoginsColumn(authz, 0);
    }

    public void addLoginsColumn(IsClickableCell authz, int width) {

        // create column
        Column<RichMember, RichMember> loginsColumn = JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "logins"), new JsonUtils.GetValue<RichMember, RichMember>() {
            @Override
            public RichMember getValue(RichMember object) {
                return object;
            }
        }, fieldUpdater);

        // add column
        table.addColumn(loginsColumn, "Logins");
        if (width != 0) {
            table.setColumnWidth(loginsColumn, width, Style.Unit.PX);
        }

        // logins are not sortable

        //organizationColumn.setSortable(true);
        //table.getColumnSortHandler().setComparator(organizationColumn, new RichMemberComparator(RichMemberComparator.Column.));

    }

    public void addStatusColumn(IsClickableCell authz) {
        addStatusColumn(authz, 0);
    }

    public void addStatusColumn(IsClickableCell authz, int width) {

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
                                if (originalCallback != null) {
                                    originalCallback.clearTable();
                                    originalCallback.retrieveData();
                                }
                            }
                            public void onError(PerunError error) {
                                if (originalCallback != null) {
                                    originalCallback.clearTable();
                                    originalCallback.retrieveData();
                                }
                            }
                        });
                        call.setStatus(lb.getValue(lb.getSelectedIndex()));
                    }
                });
                conf.show();
            }
        });


        // add column
        table.addColumn(statusColumn, "Status");
        if (width != 0) {
            table.setColumnWidth(statusColumn, width, Style.Unit.PX);
        }

        // status column sortable
        statusColumn.setSortable(true);
        table.getColumnSortHandler().setComparator(statusColumn, new GeneralComparator<RichMember>(GeneralComparator.Column.STATUS));

    }

    public static IsClickableCell<GeneralObject> getDefaultClickableAuthz() {

        return new IsClickableCell<GeneralObject>() {

            @Override
            public boolean isClickable(GeneralObject object) {
                RichMember rm = object.cast();
                return (PerunWebSession.getInstance().isVoAdmin(rm.getVoId()) || PerunWebSession.getInstance().isVoObserver(rm.getVoId()));
            }

            @Override
            public String linkUrl(GeneralObject object) {
                RichMember rm = object.cast();
                return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + MemberDetailTabItem.URL + "?id=" + rm.getId()+"&active=1";
            }

        };

    }

}
