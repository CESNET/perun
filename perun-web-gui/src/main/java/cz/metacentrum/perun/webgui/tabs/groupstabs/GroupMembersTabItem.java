package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import cz.metacentrum.perun.webgui.json.groupsManager.RemoveMember;
import cz.metacentrum.perun.webgui.json.membersManager.FindCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.membersManager.GetCompleteRichMembers;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToGroupTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoMembersTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Displays a group members
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class GroupMembersTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading members");

	// widget
	final SimplePanel pageWidget = new SimplePanel();
	// members table wrapper
	ScrollPanel tableWrapper = new ScrollPanel();
	
	/**
	 * Group
	 */
	private Group group;
	private int groupId;
	
	// CURRENT TAB STATE
	enum State {
		searching, listAll		
	}
	
	// default state is search
	State state = State.listAll;
	
	// when searching
	private String searchString = "";
	
	/**
	 * Creates a tab instance
	 *
     * @param group
     */
	public GroupMembersTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param groupId
     */
	public GroupMembersTabItem(int groupId){
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
		
		// SET TAB NAME
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": members");

		// MAIN PANEL
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

        // DISABLED CHECKBOX
        final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());
        disabled.setTitle(WidgetTranslation.INSTANCE.showDisabledMembersTitle());

        JsonCallbackEvents disableCheckboxEvent = JsonCallbackEvents.disableCheckboxEvents(disabled);

		// CALLBACKS
		final GetCompleteRichMembers members = new GetCompleteRichMembers(PerunEntity.GROUP, groupId, null, disableCheckboxEvent);
		final FindCompleteRichMembers findMembers = new FindCompleteRichMembers(PerunEntity.GROUP, groupId, "", null, disableCheckboxEvent);
        members.setIndirectCheckable(false);
        findMembers.setIndirectCheckable(false);

		// refreshMembers
		final JsonCallbackEvents refreshMembersEvent = JsonCallbackEvents.refreshTableEvents(members);
		// refreshFindMembers
		final JsonCallbackEvents refreshFindMembersEvent = JsonCallbackEvents.refreshTableEvents(findMembers);
		
		// MENU
		TabMenu tabMenu = new TabMenu();
		boolean isMembersGroup = group.isCoreGroup();

        final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeMemberFromGroup());

		if(!isMembersGroup){

			// ADD
			CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addMemberToGroup(), new ClickHandler() {
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTabToCurrentTab(new AddMemberToGroupTabItem(group), true);
                }
            });
            tabMenu.addWidget(addButton);
	
			// REMOVE
	
			// remove button
			removeButton.addClickHandler(new ClickHandler() {
				@Override
                public void onClick(ClickEvent event) {
					final ArrayList<RichMember> itemsToRemove;
					final JsonCallbackEvents events;
					final JsonCallbackEvents refreshEvents;
					if (state == State.listAll) {
						itemsToRemove = members.getTableSelectedList();
						events = JsonCallbackEvents.disableButtonEvents(removeButton);
						refreshEvents = JsonCallbackEvents.disableButtonEvents(removeButton, refreshMembersEvent);
					} else {
						itemsToRemove = findMembers.getTableSelectedList();
						events = JsonCallbackEvents.disableButtonEvents(removeButton);
						refreshEvents = JsonCallbackEvents.disableButtonEvents(removeButton, refreshFindMembersEvent);
					}
                    String text = "Following members will be removed from group. They will lose access to resources provided by this group.";
                    UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                            for (int i=0; i<itemsToRemove.size(); i++ ) {
                                RemoveMember request;
                                if(i == itemsToRemove.size() - 1){
                                    request = new RemoveMember(refreshEvents);
                                }else{
                                    request = new RemoveMember(events);
                                }
                                request.removeMemberFromGroup(group, itemsToRemove.get(i));
                            }
                        }
                    });
				}
			});
            tabMenu.addWidget(removeButton);
        } else {

            // is core group
            tabMenu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
            Anchor a = new Anchor("<strong>To edit VO members use VO manager section in menu.</strong>", true);
            a.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTab(new VoMembersTabItem(group.getVoId()));
                }
            });
            tabMenu.addWidget(a);

            findMembers.setCheckable(false);
            members.setCheckable(false);

        }

        disabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                members.excludeDisabled(!disabled.getValue());
                members.clearTable();
                members.retrieveData();
            }
        });

		// checkbox click handler
        /* OLD CLICK HANDLER
		disabled.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (state == State.listAll) {
					if (disabled.getValue()) {
						members.excludeDisabled(false);
					} else {
						members.excludeDisabled(true);
					}
					refreshMembersEvent.onFinished(null); // refresh table
				} else {
					if (disabled.getValue()) {
						findMembers.excludeDisabled(false);
					} else {
						findMembers.excludeDisabled(true);
					}
					if (!searchString.equalsIgnoreCase("")) {
						refreshFindMembersEvent.onFinished(null); // refresh table only if there is something to search					
					}
				}
			}
		});
		*/

		// SEARCH FOR BUTTON

        /*
		TextBox searchBox = tabMenu.addSearchBox(new PerunSearchEvent() {
			public void searchFor(String text) {
				searchString = text;
				state = State.searching;
				searchForAction(text, findMembers, disabled);
			}
		}, "Search", "Search for Group members by name, login, email");
		searchBox.setText(searchString);
        */
        final ExtendedSuggestBox box = new ExtendedSuggestBox(members.getOracle());
        box.getSuggestBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
                searchString = box.getSuggestBox().getText().trim();
            }
        });

        tabMenu.addFilterWidget(box, new PerunSearchEvent() {
            public void searchFor(String text) {
                members.filterTable(text);
            }
        }, ButtonTranslation.INSTANCE.filterMembers());

        // set search on finished if necessary (like changing show/hide disabled)
        members.setEvents(new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso) {
                members.filterTable(searchString);
            }
        });

		// LIST ALL BUTTON

        /*
		tabMenu.addButton("List all", SmallIcons.INSTANCE.userGreenIcon(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                state = State.listAll;
                listAllAction(members, disabled);
            }
        });
        */
		
		tabMenu.addWidget(disabled);
		
		vp.add(tabMenu);
        vp.setCellHeight(tabMenu, "30px");
		vp.add(pageWidget);
		
		/* WHEN TAB RELOADS, CHECK THE STATE */
		if(this.state == State.listAll){
			listAllAction(members, removeButton, disabled);
		}else if(this.state == State.searching){
			searchForAction(searchString, findMembers, disabled);
		}
		
		this.contentWidget.setWidget(vp);
		return getWidget();
	
	}
	
	/**
	 * LIST ALL
	 */
	private void listAllAction(final GetCompleteRichMembers members, CustomButton removeButton, CheckBox disabled) {

        members.excludeDisabled(!disabled.getValue());

        removeButton.setEnabled(false);

		// get the table
		CellTable<RichMember> table = members.getTable(new FieldUpdater<RichMember, RichMember>() {
			// when user click on a row -> open new tab
			public void update(int index, RichMember object, RichMember value) {
				// TODO better auth
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), groupId));
			}
		});

        JsonUtils.addTableManagedButton(members, table, removeButton);

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");		

		session.getUiElements().resizePerunTable(tableWrapper, 350, this);

		// add the table to the main panel
		setPageWidget(tableWrapper);
		
	}
	
	/**
	 * SEARCH FOR
	 */
	private void searchForAction(String text, FindCompleteRichMembers findMembers, CheckBox disabled) {
		
		findMembers.excludeDisabled(!disabled.getValue());

		ScrollPanel tableWrapper = new ScrollPanel();

		CellTable<RichMember> table = findMembers.getEmptyTable(new FieldUpdater<RichMember, RichMember>() {
			// when user click on a row -> open new tab
			public void update(int index, RichMember object, RichMember value) {
				// TODO better auth
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), groupId));
			}
		});
		
		table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");		

		session.getUiElements().resizePerunTable(tableWrapper, 350, this);
		
		// add menu and the table to the main panel
		setPageWidget(tableWrapper);
		
		// if not empty - start searching
		if (!text.equalsIgnoreCase("")) {
			findMembers.searchFor(text);			
		}
		
	}
	
	private void setPageWidget(Widget w) {
		this.pageWidget.setWidget(w);

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.userGreenIcon(); 
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

		GroupMembersTabItem create = (GroupMembersTabItem) obj;

		if (groupId != create.groupId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(group, "Members", getUrlWithParameters());
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
	
	public final static String URL = "members";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}
	
	static public GroupMembersTabItem load(Map<String, String> parameters) {
		int gid = Integer.parseInt(parameters.get("id"));
		return new GroupMembersTabItem(gid);
	}

}