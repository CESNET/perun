package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.GetRichAdminsWithAttributes;
import cz.metacentrum.perun.webgui.json.authzResolver.RemoveAdmin;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoManagersTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Group admins page for Group Admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class GroupManagersTabItem implements TabItem, TabItemWithUrl{

    /**
     * Perun web session
     */
    private PerunWebSession session = PerunWebSession.getInstance();

    /**
     * Content widget - should be simple panel
     */
    private SimplePanel contentWidget = new SimplePanel();

    /**
     * Title widget
     */
    private Label titleWidget = new Label("Loading group managers");

    /**
     * Group
     */
    private Group group;
    private int groupId;

    /**
     * Creates a tab instance
     * @param group
     */
    public GroupManagersTabItem(Group group){
        this.group = group;
        this.groupId = group.getId();
    }

    /**
     * Creates a tab instance
     * @param groupId
     */
    public GroupManagersTabItem(int groupId){
        this.groupId = groupId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                group = jso.cast();
            }
        };
        new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
    }


    public boolean isPrepared(){
        return !(group == null);
    }


    public Widget draw() {

        titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": managers");

        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");

        boolean isMembersGroup = group.isCoreGroup();

        // group members
        final GetRichAdminsWithAttributes admins = new GetRichAdminsWithAttributes(PerunEntity.GROUP, groupId, null);

        // Events for reloading when finished
        final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(admins);

        // menu
        TabMenu menu = new TabMenu();
        vp.add(menu);
        vp.setCellHeight(menu, "30px");

        final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerFromGroup());

        if(!isMembersGroup){

            menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addManagerToGroup(), new ClickHandler() {
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTabToCurrentTab(new AddGroupManagerTabItem(group), true);
                }
            }));

            removeButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    final ArrayList<User> itemsToRemove = admins.getTableSelectedList();
                    String text = "Following users won't be group managers anymore.";
                    UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                            for (int i=0; i<itemsToRemove.size(); i++ ) {
                                RemoveAdmin request;
                                if (i == itemsToRemove.size() - 1) {
                                    request = new RemoveAdmin(PerunEntity.GROUP, JsonCallbackEvents.disableButtonEvents(removeButton, events));
                                } else {
                                    request = new RemoveAdmin(PerunEntity.GROUP, JsonCallbackEvents.disableButtonEvents(removeButton));
                                }
                                request.removeAdmin(groupId, itemsToRemove.get(i).getId());
                            }
                        }
                    });
                }
            });
            menu.addWidget(removeButton);

        } else {

            // is core group
            menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
            Anchor a = new Anchor("<strong>To edit VO managers use VO manager section in menu.</strong>", true);
            a.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTab(new VoManagersTabItem(group.getVoId()));
                }
            });
            menu.addWidget(a);

        }

        // get the table
        CellTable<User> table;
        if (session.isPerunAdmin()) {
            table = admins.getTable(new FieldUpdater<User, String>() {
                public void update(int i, User user, String s) {
                    session.getTabManager().addTab(new UserDetailTabItem(user));
                }
            });
        } else {
            table = admins.getTable();
        }

        removeButton.setEnabled(false);
        JsonUtils.addTableManagedButton(admins, table, removeButton);

        table.setStyleName("perun-table");
        ScrollPanel sp = new ScrollPanel();
        sp.add(table);
        sp.setStyleName("perun-tableScrollPanel");
        vp.add(sp);

        session.getUiElements().resizePerunTable(sp, 350, this);

        this.contentWidget.setWidget(vp);

        return getWidget();
    }

    public Widget getWidget() {
        return this.contentWidget;
    }

    public Widget getTitle() {
        return this.titleWidget;
    }

    public ImageResource getIcon() {
        return SmallIcons.INSTANCE.administratorIcon();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupId;
        return result;
    }

    /**
     * @param obj
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        GroupManagersTabItem create = (GroupManagersTabItem) obj;

        if (group != create.group){
            return false;
        }

        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open()
    {
        session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(group, "Managers", getUrlWithParameters());
        if(group != null){
            session.setActiveGroup(group);
            return;
        }
        session.setActiveGroupId(groupId);

    }

    public boolean isAuthorized() {

        if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(groupId)) {
            return true;
        } else {
            return false;
        }

    }


    public final static String URL = "managers";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters()
    {
        return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
    }

    static public GroupManagersTabItem load(Map<String, String> parameters)
    {
        int id = Integer.parseInt(parameters.get("id"));
        return new GroupManagersTabItem(id);
    }
}