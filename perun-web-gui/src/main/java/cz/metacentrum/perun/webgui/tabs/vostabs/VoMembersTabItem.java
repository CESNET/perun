package cz.metacentrum.perun.webgui.tabs.vostabs;

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
import cz.metacentrum.perun.webgui.json.membersManager.DeleteMember;
import cz.metacentrum.perun.webgui.json.membersManager.FindCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.membersManager.GetCompleteRichMembers;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToVoTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.CreateServiceMemberInVoTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * VO Members page
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class VoMembersTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading VO members");
	
	// data
	private VirtualOrganization vo;
	
	// members table wrapper
	ScrollPanel tableWrapper = new ScrollPanel();
    // find members table wrapper
    ScrollPanel tableWrapper2 = new ScrollPanel();

	// widget
	final SimplePanel pageWidget = new SimplePanel();
	
	// CURRENT TAB STATE
	enum State {
		searching, listAll		
	}
	
	// default state is search
	State state = State.searching;
	
	// when searching
	private String searchString = "";

	private int voId;

	/**
	 * Creates a tab instance
	 *
     * @param vo
     */
	public VoMembersTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param voId
     */
	public VoMembersTabItem(int voId){
		this.voId = voId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                vo = jso.cast();
            }
        };
        new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}
	
	public boolean isPrepared(){
		return !(vo == null);
	}
	
	public Widget draw() {
		
		// SET TAB NAME
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": members");
		
		// MAIN PANEL
		final VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// MENU
		TabMenu tabMenu = new TabMenu();
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

        // DISABLED CHECKBOX
        final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());
        disabled.setTitle(WidgetTranslation.INSTANCE.showDisabledMembersTitle());

        // CALLBACKS
		final GetCompleteRichMembers members = new GetCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, voId, null);
		final FindCompleteRichMembers findMembers = new FindCompleteRichMembers(PerunEntity.VIRTUAL_ORGANIZATION, voId, "", null);
		
		// ADD
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addMemberToVo(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                session.getTabManager().addTabToCurrentTab(new AddMemberToVoTabItem(voId), true);
            }
        });
        tabMenu.addWidget(addButton);

		// REMOVE
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeMemberFromVo());
        tabMenu.addWidget(removeButton);

        final CustomButton addServiceButton = new CustomButton(ButtonTranslation.INSTANCE.createServiceMemberButton(), ButtonTranslation.INSTANCE.createServiceMember(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().addTabToCurrentTab(new CreateServiceMemberInVoTabItem(vo));
            }
        });
        tabMenu.addWidget(addServiceButton);

        // refreshMembers
		final JsonCallbackEvents refreshMembersEvent = JsonCallbackEvents.refreshTableEvents(members);
		// refreshFindMembers
		final JsonCallbackEvents refreshFindMembersEvent = JsonCallbackEvents.refreshTableEvents(findMembers);
		
		// add click handler for remove button
		removeButton.addClickHandler(new ClickHandler() {
			@Override
            public void onClick(ClickEvent event) {

				// state specific events
				final ArrayList<RichMember> membersForRemoving;
				final JsonCallbackEvents events;
				final JsonCallbackEvents refreshEvents;
				if (state == State.listAll) {
					membersForRemoving = members.getTableSelectedList();
					events = JsonCallbackEvents.disableButtonEvents(removeButton);
					refreshEvents = JsonCallbackEvents.disableButtonEvents(removeButton, refreshMembersEvent);
				} else {
					membersForRemoving = findMembers.getTableSelectedList();
					events = JsonCallbackEvents.disableButtonEvents(removeButton);
					refreshEvents = JsonCallbackEvents.disableButtonEvents(removeButton, refreshFindMembersEvent);
				}
                String text = "Following members will be removed from VO and their settings will be lost.<p>You can consider changing their status to \"DISABLED\", which will prevent them from accessing VO resources.";
                UiElements.showDeleteConfirm(membersForRemoving, text, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                        for (int i = 0; i< membersForRemoving.size(); i++) {
                            DeleteMember request;
                            if (i == membersForRemoving.size() - 1) {
                                request = new DeleteMember(refreshEvents);
                            } else {
                                request = new DeleteMember(events);
                            }
                            request.deleteMember(membersForRemoving.get(i).getId());
                        }
                    }
                });
			}
		});

		// checkbox click handler
		disabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (state == State.listAll) {
                    members.excludeDisabled(!disabled.getValue());
                    members.clearTable();
                    members.retrieveData();
                } else {
                    findMembers.excludeDisabled(!disabled.getValue());
                    if (!"".equalsIgnoreCase(searchString)) {
                        findMembers.searchFor(searchString);
                    }
                }
            }
        });
		
		// SEARCH FOR BUTTON

        final CustomButton searchButton = TabMenu.getPredefinedButton(ButtonType.SEARCH, ButtonTranslation.INSTANCE.searchMemberInVo());
		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			public void searchFor(String text) {
				searchString = text;
				state = State.searching;
				searchForAction(text, findMembers, disabled, removeButton, searchButton);
			}
		}, searchButton);
		searchBox.getTextBox().setText(searchString);

		// LIST ALL BUTTON
		final CustomButton listAllButton = TabMenu.getPredefinedButton(ButtonType.LIST_ALL_MEMBERS, ButtonTranslation.INSTANCE.listAllMembersInVo());
        listAllButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                state = State.listAll;
                searchString = "";
                searchBox.getTextBox().setText(searchString);
                listAllAction(members, disabled, removeButton, listAllButton);
            }
        });
        tabMenu.addWidget(listAllButton);
		
		tabMenu.addWidget(disabled);
		firstTabPanel.add(pageWidget);
		
		/* WHEN TAB RELOADS, CHECK THE STATE */
		if(this.state == State.listAll){
			listAllAction(members, disabled, removeButton, listAllButton);
		}else if(this.state == State.searching){
			searchForAction(searchString, findMembers, disabled, removeButton, searchButton);
		}

		this.contentWidget.setWidget(firstTabPanel);
		return getWidget();
		
	}
	
	/**
	 * LIST ALL
	 */
	private void listAllAction(final GetCompleteRichMembers members, CheckBox disabled, CustomButton removeButton, CustomButton listAllButton) {

        members.setEvents(JsonCallbackEvents.disableButtonEvents(listAllButton, JsonCallbackEvents.disableCheckboxEvents(disabled)));
        members.excludeDisabled(!disabled.getValue());
        members.clearTable();

        // get the table
		CellTable<RichMember> table = members.getTable(new FieldUpdater<RichMember, String>() {
            // when user click on a row -> open new tab
            public void update(int index, RichMember object, String value) {
                session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), 0));
            }
        });

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");

        removeButton.setEnabled(false);
        JsonUtils.addTableManagedButton(members, table, removeButton);

		session.getUiElements().resizePerunTable(tableWrapper, 350, this);

		// add the table to the main panel
		setPageWidget(tableWrapper);
		
	}
	
	/**
	 * SEARCH FOR
	 */
	private void searchForAction(String text, FindCompleteRichMembers findMembers, CheckBox disabled, CustomButton removeButton, CustomButton searchButton) {

        findMembers.setEvents(JsonCallbackEvents.disableButtonEvents(searchButton, JsonCallbackEvents.disableCheckboxEvents(disabled)));
        findMembers.excludeDisabled(!disabled.getValue());
        if (!searchString.equals("")) {
            // clear if search not empty
            findMembers.clearTable();
        }

		CellTable<RichMember> table = findMembers.getEmptyTable(new FieldUpdater<RichMember, String>() {
            // when user click on a row -> open new tab
            public void update(int index, RichMember object, String value) {
                session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), 0));
            }
        });
		
		table.addStyleName("perun-table");
		tableWrapper2.setWidget(table);
		tableWrapper2.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizePerunTable(tableWrapper2, 350, this);
		
		// add menu and the table to the main panel
		setPageWidget(tableWrapper2);
		
		// if not empty - start searching
        if (!"".equalsIgnoreCase(text)) {
            findMembers.searchFor(text);
        }

        removeButton.setEnabled(false);
        JsonUtils.addTableManagedButton(findMembers, table, removeButton);

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
		return SmallIcons.INSTANCE.userGreenIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 59;
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
		VoMembersTabItem other = (VoMembersTabItem) obj;
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
        session.getUiElements().getBreadcrumbs().setLocation(vo, "Members", getUrlWithParameters());
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
	
	public final static String URL = "members";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}
	
	static public VoMembersTabItem load(Map<String, String> parameters)
	{
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoMembersTabItem(voId);
	}
	
	static public VoMembersTabItem load(VirtualOrganization vo)
	{
		return new VoMembersTabItem(vo);
	}

}