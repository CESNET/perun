package cz.metacentrum.perun.webgui.tabs.memberstabs;

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
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.AddMember;
import cz.metacentrum.perun.webgui.json.membersManager.FindCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.membersManager.GetCompleteRichMembers;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Add member to group page
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 5a3fd18e8a537aa8201159988ee4701463f0acba $
 */
public class AddMemberToGroupTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading group");

	// data
	private Group group;

	// members table wrapper
	ScrollPanel tableWrapper = new ScrollPanel();

	// widget
	final SimplePanel pageWidget = new SimplePanel();

	// CURRENT TAB STATE
	enum State {
		searching, listAll		
	}

	// default state is with pages
	State state = State.searching;

	// when searching
	private String searchString = "";

	private int groupId;

	/**
	 * Creates a tab instance
	 *
     * @param group
     */
	public AddMemberToGroupTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param groupId
     */
	public AddMemberToGroupTabItem(int groupId){
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
		
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": add member");
		
		// MAIN PANEL
		final VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");
		
		boolean isMembersGroup = group.isCoreGroup();

		// if members or admins group, hide
		if(isMembersGroup){
			
			firstTabPanel.add(new HTML("<p>Group \""+group.getName()+"\" can't have members managed from Group admin. Please use VO admin section.</p>"));
			
			this.contentWidget.setWidget(firstTabPanel);
			return getWidget();
			
		}

		// MENU
		TabMenu tabMenu = new TabMenu();
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		// CALLBACKS
		final GetCompleteRichMembers members;
		final FindCompleteRichMembers findMembers;

        // search through whole VO
        members = new GetCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, group.getVoId(), null);
        findMembers = new FindCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, group.getVoId(), "", null);

        // ADD
		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedMemberToGroup());
		// close tab event
		final TabItem tab = this;
		// click handler
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// state specific events
				ArrayList<RichMember> membersToAdd;
				final JsonCallbackEvents events;
				if (state == State.listAll) {
					membersToAdd = members.getTableSelectedList();
				} else {
					membersToAdd = findMembers.getTableSelectedList();
				}
                if (UiElements.cantSaveEmptyListDialogBox(membersToAdd)) {
                    // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                    for (int i=0; i<membersToAdd.size(); i++ ) {
                        if (i == membersToAdd.size()-1) {
                            AddMember request = new AddMember(JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
                            request.addMemberToGroup(groupId, membersToAdd.get(i).getId());
                        } else {
                            AddMember request = new AddMember(JsonCallbackEvents.disableButtonEvents(addButton));
                            request.addMemberToGroup(groupId, membersToAdd.get(i).getId());
                        }
                    }
                }
			}
		});
        tabMenu.addWidget(addButton);

        tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));
		
		// refreshMembers
		final JsonCallbackEvents refreshMembersEvent = JsonCallbackEvents.refreshTableEvents(members);
		// refreshFindMembers
		final JsonCallbackEvents refreshFindMembersEvent = JsonCallbackEvents.refreshTableEvents(findMembers);
		
		// DISABLED CHECKBOX
		final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());
		disabled.setTitle(WidgetTranslation.INSTANCE.showDisabledMembersTitle());
		
		// checkbox click handler
		disabled.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {		
				if (state == State.listAll) {
					// STATE LIST ALL
					if (disabled.getValue()) {
                           members.excludeDisabled(false);
                        } else {
                           members.excludeDisabled(true);
                        }
					members.clearTable();
                    members.retrieveData();
				} else {
					 if (disabled.getValue()) {
                            findMembers.excludeDisabled(false);
                        } else {
                            findMembers.excludeDisabled(true);
                        }
					if (!searchString.equalsIgnoreCase("")) {
                        findMembers.clearTable();
                        findMembers.retrieveData();
					}
				}
			}
		});
		
		// SEARCH FOR BUTTON
		TextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
            public void searchFor(String text) {
                searchString = text;
                state = State.searching;
                searchForAction(text, findMembers, disabled, addButton);
            }
        }, ButtonTranslation.INSTANCE.searchMemberInParentGroup());
		searchBox.setText(searchString);

		// LIST ALL BUTTON
		// TODO - if will ever be used, table managed button must be switched between states
		TabMenu.getPredefinedButton(ButtonType.LIST_ALL_MEMBERS, ButtonTranslation.INSTANCE.listAllMembersInGroup(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                state = State.listAll;
                listAllAction(members, disabled);
            }
        });
		
		tabMenu.addWidget(disabled);
		firstTabPanel.add(pageWidget);
		
		/* WHEN TAB RELOADS, CHECK THE STATE */
		if(this.state == State.listAll){
			listAllAction(members, disabled);
		} else if(this.state == State.searching){
			searchForAction(searchString, findMembers, disabled, addButton);
		}

		this.contentWidget.setWidget(firstTabPanel);
		return getWidget();
		
	}
	
	/**
	 * LIST ALL
	 */
	private void listAllAction(final GetCompleteRichMembers members, CheckBox disabled) {

		if (disabled.getValue()) {
			members.excludeDisabled(false);
		} else {
			members.excludeDisabled(true);
		}
		
		// get the table
		CellTable<RichMember> table = members.getTable(new FieldUpdater<RichMember, String>() {
			// when user click on a row -> open new tab
			public void update(int index, RichMember object, String value) {
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), groupId));
			}
		});	

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");		

		// do not use resizePerunTable() when tab is in overlay - wrong width is calculated
		session.getUiElements().resizeSmallTabPanel(tableWrapper, 350, this);

		// add the table to the main panel
		setPageWidget(tableWrapper);
		
	}
	
	/**
	 * SEARCH FOR
	 */
	private void searchForAction(String text, FindCompleteRichMembers findMembers, CheckBox disabled, CustomButton addButton) {
		
		if (disabled.getValue()) {
			findMembers.excludeDisabled(false);
		} else {
			findMembers.excludeDisabled(true);
		}
		
		ScrollPanel tableWrapper = new ScrollPanel();

		CellTable<RichMember> table = findMembers.getEmptyTable(new FieldUpdater<RichMember, String>() {
			// when user click on a row -> open new tab
			public void update(int index, RichMember object, String value) {
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), groupId));
			}
		});

        addButton.setEnabled(false);
        JsonUtils.addTableManagedButton(findMembers, table, addButton);

        table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");		

		session.getUiElements().resizeSmallTabPanel(tableWrapper, 350, this);
		
		// add menu and the table to the main panel
		setPageWidget(tableWrapper);
		
		// if not empty - start searching
		if (!text.equalsIgnoreCase("")) {
			findMembers.searchFor(text);			
		}
		
	}

	private void setPageWidget(Widget w)
	{
		this.pageWidget.setWidget(w);

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
		final int prime = 59;
		int result = 1;
		result = prime * result + groupId;
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
		AddMemberToGroupTabItem other = (AddMemberToGroupTabItem) obj;
		if (groupId != other.groupId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
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
	
	public final static String URL = "add-to-grp";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?grp=" + groupId;
	}
	
	static public AddMemberToGroupTabItem load(Map<String, String> parameters)
	{
		int gid = Integer.parseInt(parameters.get("grp"));
		return new AddMemberToGroupTabItem(gid);
	}

}