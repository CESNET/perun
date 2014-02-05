package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.groupsManager.GetMemberGroups;
import cz.metacentrum.perun.webgui.json.membersManager.GetMemberByUser;
import cz.metacentrum.perun.webgui.json.usersManager.GetVosWhereUserIsMember;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.PerunAttributeTableWidget;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's VO preferences
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: $
 */
public class SelfVosTabItem implements TabItem, TabItemWithUrl {

    PerunWebSession session = PerunWebSession.getInstance();

    private SimplePanel contentWidget = new SimplePanel();
    private Label titleWidget = new Label("Loading user");

    private User user;
    private int userId;

    /**
     * Creates a tab instance
     */
    public SelfVosTabItem(){
        this.user = session.getUser();
        this.userId = user.getId();
    }

    /**
     * Creates a tab instance with custom user
     * @param user
     */
    public SelfVosTabItem(User user){
        this.user = user;
        this.userId = user.getId();
    }

    /**
     * Creates a tab instance with custom user
     * @param userId
     */
    public SelfVosTabItem(int userId) {
        this.userId = userId;
        new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                user = jso.cast();
            }
        }).retrieveData();
    }

    public boolean isPrepared(){
        return !(user == null);
    }

    public Widget draw() {

        this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": VO settings");

        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%","100%");

        final TabMenu menu = new TabMenu();
        final ScrollPanel sp = new ScrollPanel();
        sp.setSize("100%", "100%");
        sp.setStyleName("perun-tableScrollPanel");
        session.getUiElements().resizeSmallTabPanel(sp, 350, this);

        final ListBoxWithObjects<VirtualOrganization> vosListbox = new ListBoxWithObjects<VirtualOrganization>();

        menu.addWidget(new HTML("<strong>Select VO:</strong>"));
        menu.addWidget(vosListbox);
        menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
        menu.addWidget(new HTML("<strong>Select VO to see your settings.</strong>"));

        vp.add(menu);
        vp.setCellHeight(menu, "50px");
        vp.setCellVerticalAlignment(menu, HasVerticalAlignment.ALIGN_MIDDLE);
        vp.add(new HTML("<hr size=\"1\" color=\"#ccc\">"));
        vp.add(sp);

        final GetVosWhereUserIsMember whereMember = new GetVosWhereUserIsMember(userId, new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
                vos = new TableSorter<VirtualOrganization>().sortByName(vos);
                vosListbox.clear();
                if (vos != null && !vos.isEmpty()) {
                    for (VirtualOrganization vo : vos) {
                        vosListbox.addItem(vo);
                    }
                    vosListbox.addChangeHandler(new ChangeHandler() {
                        @Override
                        public void onChange(ChangeEvent event) {
                            sp.setWidget(displayVoSubtab(vosListbox.getSelectedObject()));
                        }
                    });
                    sp.setWidget(displayVoSubtab(vosListbox.getSelectedObject()));
                } else {
                    vosListbox.addItem("No VO available");
                    sp.setWidget(new HTML(new Image(LargeIcons.INSTANCE.errorIcon()) + "<h2>You are not member of any VO.</h2>"));
                }
            }
            @Override
            public void onLoadingStart() {
                vosListbox.clear();
                vosListbox.addItem("Loading...");
            }
            @Override
            public void onError(PerunError error) {
                vosListbox.clear();
                vosListbox.addItem("Error while loading");
                sp.clear();
            }
        });
        whereMember.retrieveData();

        this.contentWidget.setWidget(vp);
        return getWidget();

    }

    /**
     * Internal method which renders subtab for each (selected) VO
     *
     * @param vo VO to render preferences for
     * @return subtab widget
     */
    private Widget displayVoSubtab(final VirtualOrganization vo) {

        final FlexTable layout = new FlexTable();
        layout.setSize("100%", "100%");

        // VO overview

        FlexTable voHeader = new FlexTable();
        voHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
        voHeader.setHTML(0, 1, "<p class=\"subsection-heading\">"+vo.getName()+"</p>");

        final FlexTable voOverview = new FlexTable();
        voOverview.setStyleName("inputFormFlexTableDark");

        GetAttributes voAttrsCall = new GetAttributes(new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                ArrayList<Attribute> attrs = JsonUtils.jsoAsList(jso);
                int i = 0;
                for (Attribute a : attrs) {
                    if (a.getFriendlyName().equalsIgnoreCase("userManualsLink")) {
                        voOverview.setHTML(i, 0, "User's manuals:");
                        Anchor link = new Anchor(a.getValue(), a.getValue());
                        link.getElement().setPropertyString("target", "_blank");
                        voOverview.setWidget(i, 1, link);
                        i++;
                    } else if (a.getFriendlyName().equalsIgnoreCase("dashboardLink")) {
                        voOverview.setHTML(i, 0, "Dashboard:");
                        Anchor link = new Anchor(a.getValue(), a.getValue());
                        link.getElement().setPropertyString("target", "_blank");
                        voOverview.setWidget(i, 1, link);
                        i++;
                    } else if (a.getFriendlyName().equalsIgnoreCase("contactEmail")) {
                        voOverview.setHTML(i, 0, "VO contact:");
                        voOverview.setHTML(i, 1, a.getValue());
                        i++;
                    }
                }
                // no rows in selected result
                if (i < 1) {
                    voOverview.setHTML(i, 0, "VO doesn't provide any details or contacts about itself.");
                    voOverview.getFlexCellFormatter().setStyleName(i, 0, "inputFormInlineComment");
                } else {
                    for (int n=0; n<voOverview.getRowCount(); n++) {
                        voOverview.getFlexCellFormatter().setStyleName(n, 0, "itemName");
                        voOverview.getFlexCellFormatter().setWidth(n, 0, "200px");
                    }
                }
            }
            @Override
            public void onError(PerunError error) {
                voOverview.setHTML(0, 0, "Error while loading");
            }
            @Override
            public void onLoadingStart() {
                voOverview.setWidget(0, 0, new AjaxLoaderImage());
            }

        });
        voAttrsCall.getVoAttributes(vo.getId());
        voAttrsCall.retrieveData();

        // CONTACT INFO

        FlexTable contactHeader = new FlexTable();
        contactHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.vcardIcon()));
        contactHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Contact</p>");

        HashMap<String, Integer> ids = new HashMap<String, Integer>();
        ids.put("user", userId);
        final PerunAttributeTableWidget contactTable = new PerunAttributeTableWidget(ids);
        contactTable.setDark(true);
        contactTable.setDisplaySaveButton(false);

        final GetListOfAttributes attributes = new GetListOfAttributes(new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                contactTable.add(new TableSorter<Attribute>().sortByAttrNameTranslation(JsonUtils.<Attribute>jsoAsList(jso)));
                layout.setWidget(3, 0, contactTable.getSaveButton());
                layout.setWidget(4, 0, contactTable);
            }
        });
        final ArrayList<String> list = new ArrayList<String>();
        list.add("urn:perun:member:attribute-def:def:organization");
        list.add("urn:perun:member:attribute-def:def:workplace");
        list.add("urn:perun:member:attribute-def:opt:researchGroup");
        list.add("urn:perun:member:attribute-def:def:mail");
        list.add("urn:perun:member:attribute-def:def:phone");
        list.add("urn:perun:member:attribute-def:def:address");

        GetMemberByUser mem = new GetMemberByUser(vo.getId(), userId, new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                Member m = jso.cast();
                HashMap<String, Integer> ids = new HashMap<String, Integer>();
                ids.put("member", m.getId());
                attributes.getListOfAttributes(ids, list);
            }
        });
        mem.retrieveData();

        // GROUPS

        FlexTable groupsHeader = new FlexTable();
        groupsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.groupIcon()));
        groupsHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Groups</p>");

        final FlexTable groupsTable = new FlexTable();
        groupsTable.setStyleName("inputFormFlexTable");

        GetMemberByUser memCall = new GetMemberByUser(vo.getId(), userId, new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                Member m = jso.cast();
                GetMemberGroups groupsCall = new GetMemberGroups(m.getId(), new JsonCallbackEvents(){
                    @Override
                    public void onError(PerunError error) {
                        layout.setWidget(3, 1, new AjaxLoaderImage().loadingError(error));
                    }
                    @Override
                    public void onFinished(JavaScriptObject jso) {
                        ArrayList<Group> list = JsonUtils.jsoAsList(jso);
                        if (list.isEmpty() || list == null) {
                            layout.setHTML(3, 1, "You are not member of any group.");
                            return;
                        }
                        layout.setWidget(3, 1, groupsTable);
                        layout.getFlexCellFormatter().setRowSpan(3, 1, 2);
                        layout.getFlexCellFormatter().setVerticalAlignment(3, 1, HasVerticalAlignment.ALIGN_TOP);
                        groupsTable.addStyleName("userDetailTable");
                        groupsTable.setHTML(0, 0, "<strong>Name</strong>");
                        groupsTable.setHTML(0, 1, "<strong>Description</strong>");
                        for (int i=0; i<list.size(); i++){
                            groupsTable.setHTML(i+1, 0, list.get(i).getName());
                            groupsTable.setHTML(i+1, 1, list.get(i).getDescription());
                        }
                    }
                });
                groupsCall.retrieveData();
            }
            @Override
            public void onError(PerunError error) {
                layout.setWidget(3, 1, new AjaxLoaderImage().loadingError(error));
            }
            @Override
            public void onLoadingStart() {
                layout.setWidget(3, 1, new AjaxLoaderImage().loadingStart());
            }
        });
        memCall.retrieveData();

        // RESOURCES SETTINGS

        FlexTable resourcesSettingsHeader = new FlexTable();
        resourcesSettingsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.settingToolsIcon()));
        resourcesSettingsHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Settings</p>");

        FlexTable resourcesSettingsTable = new FlexTable();
        resourcesSettingsTable.setStyleName("inputFormFlexTable");

        Anchor a = new Anchor();
        a.setText("Shell and Quota settings >>");
        a.setStyleName("pointer");
        a.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                session.getTabManager().addTab(new SelfSettingsTabItem(user, vo), true);
            }
        });

        resourcesSettingsTable.setWidget(0, 0, a);

        // FILL LAYOUT

        layout.setWidget(0, 0, voHeader);
        layout.setWidget(1, 0, voOverview);

        layout.setWidget(2, 0, contactHeader);
        layout.setWidget(3, 0, new AjaxLoaderImage());
        layout.setHTML(4, 0, "");

        layout.setWidget(0, 1, resourcesSettingsHeader);
        layout.setWidget(1, 1, resourcesSettingsTable);

        layout.setWidget(2, 1, groupsHeader);
        layout.setWidget(3, 1, new AjaxLoaderImage());

        layout.getFlexCellFormatter().setWidth(0, 0, "50%");

        return layout;

    }

    public Widget getWidget() {
        return this.contentWidget;
    }

    public Widget getTitle() {
        return this.titleWidget;
    }

    public ImageResource getIcon() {
        return SmallIcons.INSTANCE.buildingIcon();
    }

    @Override
    public int hashCode() {
        final int prime = 11;
        int result = 432;
        result = prime * result * userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.userId != ((SelfVosTabItem)obj).userId)
            return false;

        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open() {
        session.setActiveUser(user);
        session.getUiElements().getMenu().openMenu(MainMenu.USER);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "VO settings", getUrlWithParameters());
    }

    public boolean isAuthorized() {

        if (session.isSelf(userId)) {
            return true;
        } else {
            return false;
        }

    }

    public final static String URL = "vos";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters() {
        return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
    }

    static public SelfVosTabItem load(Map<String, String> parameters) {

        if (parameters.containsKey("id")) {
            int uid = Integer.parseInt(parameters.get("id"));
            if (uid != 0) {
                return new SelfVosTabItem(uid);
            }
        }
        return new SelfVosTabItem();
    }

}