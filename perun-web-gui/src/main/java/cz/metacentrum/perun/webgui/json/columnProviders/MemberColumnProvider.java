package cz.metacentrum.perun.webgui.json.columnProviders;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ListDataProvider;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.comparators.GeneralComparator;
import cz.metacentrum.perun.webgui.json.comparators.RichMemberComparator;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.MembersTabs;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.memberstabs.ChangeGroupStatusTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.ChangeStatusTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.PerunTable;
import cz.metacentrum.perun.webgui.widgets.cells.CustomClickableTextCellWithAuthz;
import cz.metacentrum.perun.webgui.widgets.cells.PerunStatusCell;
import java.util.ArrayList;

/**
 * Provide columns definitions for RichMember object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberColumnProvider {

  private PerunTable<RichMember> table;
  private FieldUpdater<RichMember, RichMember> fieldUpdater;
  private ListDataProvider<RichMember> dataProvider;
  private ArrayList<RichMember> backupList;

  /**
   * New instance of MemberColumnProvider
   *
   * @param table        table to add columns to
   * @param fieldUpdater field updater used when cell is "clicked"
   */
  public MemberColumnProvider(PerunTable<RichMember> table, FieldUpdater<RichMember, RichMember> fieldUpdater) {
    this.table = table;
    this.fieldUpdater = fieldUpdater;
  }

  /**
   * New instance of MemberColumnProvider
   *
   * @param dataProvider associated with table (for refresh purpose)
   * @param table        table to add columns to
   * @param fieldUpdater field updater used when cell is "clicked"
   */
  public MemberColumnProvider(ListDataProvider<RichMember> dataProvider, ArrayList<RichMember> backupList,
                              PerunTable<RichMember> table, FieldUpdater<RichMember, RichMember> fieldUpdater) {
    this.dataProvider = dataProvider;
    this.backupList = backupList;
    this.table = table;
    this.fieldUpdater = fieldUpdater;
  }

  public static IsClickableCell<GeneralObject> getDefaultClickableAuthz() {

    return new IsClickableCell<GeneralObject>() {

      @Override
      public boolean isClickable(GeneralObject object) {
        RichMember rm = object.cast();
        return (PerunWebSession.getInstance().isVoAdmin(rm.getVoId()) ||
            PerunWebSession.getInstance().isVoObserver(rm.getVoId()));
      }

      @Override
      public String linkUrl(GeneralObject object) {
        RichMember rm = object.cast();
        return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + MemberDetailTabItem.URL + "?id=" + rm.getId() +
            "&active=1";
      }

    };

  }

  public void addIdColumn(IsClickableCell authz) {
    addIdColumn(authz, 0);
  }

  public void addIdColumn(IsClickableCell authz, int width) {

    // create column
    Column<RichMember, RichMember> idColumn =
        JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "id"),
            new JsonUtils.GetValue<RichMember, RichMember>() {
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
      table.getColumnSortHandler()
          .setComparator(idColumn, new RichMemberComparator(RichMemberComparator.Column.MEMBER_ID));

    }

  }

  public void addUserIdColumn(IsClickableCell authz) {
    addUserIdColumn(authz, 0);
  }

  public void addUserIdColumn(IsClickableCell authz, int width) {

    // create column
    Column<RichMember, RichMember> idColumn =
        JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "userId"),
            new JsonUtils.GetValue<RichMember, RichMember>() {
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
      table.getColumnSortHandler()
          .setComparator(idColumn, new RichMemberComparator(RichMemberComparator.Column.USER_ID));

    }

  }

  public void addNameColumn(IsClickableCell authz) {
    addNameColumn(authz, 0);
  }

  public void addNameColumn(IsClickableCell authz, int width) {

    // create column
    Column<RichMember, RichMember> nameColumn =
        JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "name"),
            new JsonUtils.GetValue<RichMember, RichMember>() {
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
    table.getColumnSortHandler()
        .setComparator(nameColumn, new RichMemberComparator(RichMemberComparator.Column.USER_FULL_NAME));

  }

  public void addOrganizationColumn(IsClickableCell authz) {
    addOrganizationColumn(authz, 0);
  }

  public void addOrganizationColumn(IsClickableCell authz, int width) {

    // create column
    Column<RichMember, RichMember> organizationColumn =
        JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "organization"),
            new JsonUtils.GetValue<RichMember, RichMember>() {
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
    table.getColumnSortHandler()
        .setComparator(organizationColumn, new RichMemberComparator(RichMemberComparator.Column.ORGANIZATION));

  }

  public void addEmailColumn(IsClickableCell authz) {
    addEmailColumn(authz, 0);
  }

  public void addEmailColumn(IsClickableCell authz, int width) {

    // create column
    Column<RichMember, RichMember> emailColumn =
        JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "email"),
            new JsonUtils.GetValue<RichMember, RichMember>() {
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
    table.getColumnSortHandler()
        .setComparator(emailColumn, new RichMemberComparator(RichMemberComparator.Column.EMAIL));

  }

  public void addLoginsColumn(IsClickableCell authz) {
    addLoginsColumn(authz, 0);
  }

  public void addLoginsColumn(IsClickableCell authz, int width) {

    // create column
    Column<RichMember, RichMember> loginsColumn =
        JsonUtils.addColumn(new CustomClickableTextCellWithAuthz<RichMember>(authz, "logins"),
            new JsonUtils.GetValue<RichMember, RichMember>() {
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
    final Column<RichMember, String> statusColumn = new Column<RichMember, String>(
        new PerunStatusCell()) {
      @Override
      public String getValue(RichMember object) {
        return object.getStatus();
      }
    };
    // own onClick tab for changing member's status
    statusColumn.setFieldUpdater(new FieldUpdater<RichMember, String>() {
      @Override
      public void update(final int index, final RichMember object, final String value) {
        PerunWebSession.getInstance().getTabManager()
            .addTabToCurrentTab(new ChangeStatusTabItem(object.cast(), new JsonCallbackEvents() {
              @Override
              public void onFinished(JavaScriptObject jso) {

                // fixme - since we pass this event to more tabs and update expiration (set attributes)
                //  passed object might not be relevant for this action
                if (jso == null) {
                  return;
                }

                Member m = jso.cast();
                // set status to object in cell to change rendered value
                object.setStatus(m.getStatus());
                // forcefully set status to objects in lists,
                // because they are not updated during .update() on cell
                for (RichMember rm : dataProvider.getList()) {
                  if (rm.getId() == m.getId()) {
                    rm.setStatus(m.getStatus());
                  }
                }
                if (backupList != null) {
                  for (RichMember rm : backupList) {
                    if (rm.getId() == m.getId()) {
                      rm.setStatus(m.getStatus());
                    }
                  }
                }
                dataProvider.refresh();
                dataProvider.flush();
              }
            }));
      }
    });

    // add column
    table.addColumn(statusColumn, "Status");
    if (width != 0) {
      table.setColumnWidth(statusColumn, width, Style.Unit.PX);
    }

    // status column sortable
    statusColumn.setSortable(true);
    table.getColumnSortHandler()
        .setComparator(statusColumn, new GeneralComparator<RichMember>(GeneralComparator.Column.STATUS));

  }

  public void addGroupStatusColumn(IsClickableCell authz, int groupId) {
    addGroupStatusColumn(authz, groupId, 0);
  }

  public void addGroupStatusColumn(IsClickableCell authz, int groupId, int width) {

    // Status column
    final Column<RichMember, String> statusColumn = new Column<RichMember, String>(
        new PerunStatusCell()) {
      @Override
      public String getValue(RichMember object) {
        return object.getGroupStatus();
      }
    };
    // own onClick tab for changing member's status
    statusColumn.setFieldUpdater(new FieldUpdater<RichMember, String>() {
      @Override
      public void update(final int index, final RichMember object, final String value) {

        if ("INDIRECT".equalsIgnoreCase(object.getMembershipType())) {

          UiElements.generateInfo("Can't change group membership status!",
              "INDIRECT members can't have their group membership status changed directly." +
                  "<p>Please change members group status in all sourcing groups." +
                  "<p>In order to expire member in a group, member must be set to EXPIRED in all sourcing groups (sub-groups and groups in relation)." +
                  "<p>In order to validate member, at least one sourcing group must have member with VALID group membership status.");

        } else {

          PerunWebSession.getInstance().getTabManager()
              .addTabToCurrentTab(new ChangeGroupStatusTabItem(object, groupId, new JsonCallbackEvents() {
                @Override
                public void onFinished(JavaScriptObject jso) {
                  Member m = jso.cast();
                  // set status to object in cell to change rendered value
                  object.setGroupStatus(m.getGroupStatus());
                  // forcefully set status to objects in lists,
                  // because they are not updated during .update() on cell
                  for (RichMember rm : dataProvider.getList()) {
                    if (rm.getId() == m.getId()) {
                      rm.setGroupStatus(m.getGroupStatus());
                    }
                  }
                  if (backupList != null) {
                    for (RichMember rm : backupList) {
                      if (rm.getId() == m.getId()) {
                        rm.setGroupStatus(m.getGroupStatus());
                      }
                    }
                  }
                  dataProvider.refresh();
                  dataProvider.flush();
                }
              }));

        }

      }
    });

    // add column
    table.addColumn(statusColumn, "Group Status");
    if (width != 0) {
      table.setColumnWidth(statusColumn, width, Style.Unit.PX);
    }

    // status column sortable
    statusColumn.setSortable(true);
    table.getColumnSortHandler()
        .setComparator(statusColumn, new RichMemberComparator(RichMemberComparator.Column.GROUP_STATUS));

  }

}
