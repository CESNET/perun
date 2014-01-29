package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Command;
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
import cz.metacentrum.perun.webgui.json.membersManager.CreateMember;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.json.vosManager.FindCandidates;
import cz.metacentrum.perun.webgui.model.Candidate;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.MembersTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Add member to VO
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AddMemberToVoTabItem implements TabItem, TabItemWithUrl{

    /**
     * vo id
     */
    private int voId;
    private VirtualOrganization vo;

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
    private Label titleWidget = new Label("Loading VO");

    private boolean searchCandidates = true;
    private String searchString = "";
    private CustomButton addCandidatesButton;
    private CustomButton addUsersButton;

    /**
     * Constructor
     *
     * @param voId ID of VO into which member should be added
     */
    public AddMemberToVoTabItem(int voId){
        this.voId = voId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso)
            {
                vo = jso.cast();
            }
        };
        GetEntityById callback = new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events);
        callback.retrieveData();
    }

    public boolean isPrepared(){
        return !(vo == null);
    }

    /**
     * Constructor
     *
     * @param vo VO into which member should be added
     */
    public AddMemberToVoTabItem(VirtualOrganization vo){
        this.vo = vo;
        this.voId = vo.getId();
    }

    public Widget draw() {

        titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName()) + ": add member");

        // draw the main tab
        final VerticalPanel mainTab = new VerticalPanel();
        mainTab.setSize("100%", "100%");
        // create menu for adding candidate to VO
        final TabMenu tabMenu = new TabMenu();
        mainTab.add(tabMenu); // add menu
        mainTab.setCellHeight(tabMenu, "30px");

        addCandidatesButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedCandidateToVo());
        addUsersButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedCandidateToVo());

        tabMenu.addWidget(addCandidatesButton);

        final TabItem tab = this;
        tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

        // jsonCallback to get candidates
        final FindCandidates candidates = new FindCandidates(voId, "");
        final FindCompleteRichUsers users = new FindCompleteRichUsers("", null);
        users.findWithoutVo(true, voId);

        // search candidate - select if found one
        JsonCallbackEvents selectOneEvent = new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                if (searchCandidates) {
                    // check in candidates table
                    ArrayList<Candidate> array = JsonUtils.jsoAsList(jso);
                    if (array != null && array.size() == 1){
                        candidates.setSelected(array.get(0));
                    }
                    addCandidatesButton.setEnabled(true);
                } else {
                    // check in users table
                    ArrayList<User> array = JsonUtils.jsoAsList(jso);
                    if (array != null && array.size() == 1){
                        users.setSelected(array.get(0));
                    }
                }
            }
            @Override
            public void onLoadingStart() {
                if (searchCandidates) addCandidatesButton.setEnabled(false);
            }
            @Override
            public void onError(PerunError error) {
                if (searchCandidates) addCandidatesButton.setEnabled(true);
            }
        };
        // set event for search
        candidates.setEvents(selectOneEvent);
        users.setEvents(selectOneEvent);

        // tables
        final CellTable<Candidate> candidatesTable = candidates.getTable();
        final CellTable<User> usersTable = users.getEmptyTable();
        final ScrollPanel scrollPanel = new ScrollPanel();

        // add candidate button
        addCandidatesButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Candidate candidateToBeAdded = candidates.getSelected();
                if (candidateToBeAdded == null) {
                    UiElements.cantSaveEmptyListDialogBox(null);
                } else {
                    CreateMember request = new CreateMember(JsonCallbackEvents.closeTabDisableButtonEvents(addCandidatesButton, tab));
                    request.createMember(voId, candidateToBeAdded);
                }
            }});

        addUsersButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ArrayList<User> selected = users.getTableSelectedList();
                if(UiElements.cantSaveEmptyListDialogBox(selected)){
                    // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                    for (int i=0; i<selected.size(); i++) {
                        if (i != selected.size()-1) {
                            // for every
                            CreateMember request = new CreateMember(JsonCallbackEvents.disableButtonEvents(addUsersButton));
                            request.createMember(voId, selected.get(i));
                        } else {
                            // for last
                            CreateMember request = new CreateMember(JsonCallbackEvents.closeTabDisableButtonEvents(addUsersButton, tab));
                            request.createMember(voId, selected.get(i));
                        }
                    }
                }
            }
        });

        // search box
        final TextBox searchBox = new TextBox();
        tabMenu.addWidget(searchBox);
        searchBox.setText(searchString);
        searchBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                searchString = searchBox.getText();
            }
        });

        // focus search box by default
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                searchBox.setFocus(true);
            }
        });

        // search button for candidates
        tabMenu.addWidget(new CustomButton(ButtonTranslation.INSTANCE.searchForMembersInExtSourcesButton(), ButtonTranslation.INSTANCE.searchForMembersInExtSources(), SmallIcons.INSTANCE.findIcon(), new ClickHandler(){
            public void onClick(ClickEvent event) {
                if (UiElements.searchStingCantBeEmpty(searchBox.getText())) {
                    if (scrollPanel.remove(usersTable)) {
                        // switch panels
                        scrollPanel.add(candidatesTable);
                        searchCandidates = true;
                    }
                    tabMenu.addWidget(0, addCandidatesButton);
                    candidates.setSearchString(searchBox.getText());
                    candidates.clearTable();
                    candidates.retrieveData();
                }
            }
        }));

        // search button for users
        tabMenu.addWidget(new CustomButton(ButtonTranslation.INSTANCE.searchForMembersInPerunUsersButton(), ButtonTranslation.INSTANCE.searchForMembersInPerunUsers(), SmallIcons.INSTANCE.findIcon(), new ClickHandler(){
            public void onClick(ClickEvent event) {
                if (UiElements.searchStingCantBeEmpty(searchBox.getText())) {
                    if (scrollPanel.remove(candidatesTable)) {
                        // switch panels
                        scrollPanel.add(usersTable);
                        searchCandidates = false;
                    }
                    tabMenu.addWidget(0, addUsersButton);
                    users.setSearchString(searchBox.getText());
                    users.clearTable();
                    users.retrieveData();
                }
            }
        }));

        addUsersButton.setEnabled(false);
        addCandidatesButton.setEnabled(false);

        JsonUtils.addTableManagedButton(users, usersTable, addUsersButton);

        // tables
        candidatesTable.addStyleName("perun-table");
        usersTable.addStyleName("perun-table");
        if (searchCandidates) {
            scrollPanel.add(candidatesTable);
        } else {
            scrollPanel.add(usersTable);
        }
        scrollPanel.addStyleName("perun-tableScrollPanel");

        // load if stored search string is not empty
        if (searchCandidates) {
            candidates.setSearchString(searchString);
            candidates.retrieveData();
        } else {
            users.setSearchString(searchString);
            users.retrieveData();
        }

        // style
        // do not use resizePerunTable() when tab is in overlay - wrong width is calculated
        session.getUiElements().resizeSmallTabPanel(scrollPanel, 350, this);
        mainTab.add(scrollPanel); // add table to page

        this.contentWidget.setWidget(mainTab);

        return getWidget();
    }

    public Widget getWidget() {
        return this.contentWidget;
    }

    public Widget getTitle() {
        return this.titleWidget;
    }

    public ImageResource getIcon() {
        return SmallIcons.INSTANCE.addIcon();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + voId;
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
        AddMemberToVoTabItem other = (AddMemberToVoTabItem) obj;
        if (voId != other.voId)
            return false;
        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open()
    {
        session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
        if(vo != null){
            session.setActiveVo(vo);
            return;
        }
        session.setActiveVoId(voId);
    }

    public boolean isAuthorized() {

        if (session.isVoAdmin(voId)) {
            return true;
        } else {
            return false;
        }

    }

    public final static String URL = "add-to-vo";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters()
    {
        return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?vo=" + voId;
    }

    static public AddMemberToVoTabItem load(Map<String, String> parameters)
    {
        int gid = Integer.parseInt(parameters.get("vo"));
        return new AddMemberToVoTabItem(gid);
    }

}