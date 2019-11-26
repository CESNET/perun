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
import cz.metacentrum.perun.webgui.json.groupsManager.GetGroupMembersCount;
import cz.metacentrum.perun.webgui.json.groupsManager.RemoveMember;
import cz.metacentrum.perun.webgui.json.groupsManager.RemoveMembers;
import cz.metacentrum.perun.webgui.json.membersManager.FindCompleteRichMembers;
import cz.metacentrum.perun.webgui.json.membersManager.GetCompleteRichMembers;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToGroupTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.InviteUserTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Displays a group members
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupMembersTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading Group members");

	// when searching
	private String searchString = "";
	private boolean search = false;
	private boolean wasDisabled = false;
	private CellTable<RichMember> table;
	private boolean onceAsked = false;
	private Widget tableWidget;

	/**
	 * Group
	 */
	private Group group;
	private int groupId;

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

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// SET TAB NAME
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": members");

		// MAIN PANEL
		final VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// MENU
		final TabMenu tabMenu = new TabMenu();
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		// DISABLED CHECKBOX
		final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());
		disabled.setTitle(WidgetTranslation.INSTANCE.showDisabledMembersTitle());
		disabled.setValue(wasDisabled);

		// CALLBACKS
		final GetCompleteRichMembers members = new GetCompleteRichMembers(PerunEntity.GROUP, groupId, null);
		final FindCompleteRichMembers findMembers = new FindCompleteRichMembers(PerunEntity.GROUP, groupId, "", null);
		members.excludeDisabled(!wasDisabled);
		members.setIndirectCheckable(false);
		findMembers.setHideGroupStatusColumn("members".equals(group.getName()));
		findMembers.setIndirectCheckable(false);

		final CustomButton searchButton = TabMenu.getPredefinedButton(ButtonType.SEARCH, ButtonTranslation.INSTANCE.searchMemberInGroup());
		final CustomButton listAllButton = TabMenu.getPredefinedButton(ButtonType.LIST_ALL_MEMBERS, ButtonTranslation.INSTANCE.listAllMembersInGroup());
		if ((!session.isVoAdmin(group.getVoId()) && !session.isGroupAdmin(groupId)) || group.isCoreGroup()) {
			findMembers.setCheckable(false);
		}

		table = findMembers.getEmptyTable(new FieldUpdater<RichMember, RichMember>() {
			// when user click on a row -> open new tab
			public void update(int index, RichMember object, RichMember value) {
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), groupId));
			}
		});

		tableWidget = table.getEmptyTableWidget();

		// refresh
		tabMenu.addWidget(UiElements.getRefreshButton(this));

		// ADD
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addMemberToGroup(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddMemberToGroupTabItem(groupId), true);
			}
		});
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) addButton.setEnabled(false);

		CustomButton inviteButton = new CustomButton("Invite member…", SmallIcons.INSTANCE.emailAddIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new InviteUserTabItem(group.getVoId(), group));
			}
		});
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) inviteButton.setEnabled(false);

		// REMOVE
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeMemberFromGroup());
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) removeButton.setEnabled(false);
		if (!group.isCoreGroup()) {
			tabMenu.addWidget(addButton);
			tabMenu.addWidget(inviteButton);
			tabMenu.addWidget(removeButton);
		}

		// refreshMembers
		final JsonCallbackEvents refreshEvent = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso) {
				if (search) {
					findMembers.searchFor(searchString);
				} else {
					findMembers.clearTable();
					members.retrieveData();
				}
			}
		};

		// add click handler for remove button
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// state specific events
				final ArrayList<RichMember> membersForRemoving = findMembers.getTableSelectedList();
				String text = "Following members will be removed from group. They will lose access to resources provided by this group.";
				UiElements.showDeleteConfirm(membersForRemoving, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						RemoveMembers request = new RemoveMembers(JsonCallbackEvents.disableButtonEvents(removeButton, refreshEvent));
						request.removeMembersFromGroup(group, membersForRemoving);
					}
				});
			}
		});

		final ExtendedTextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
			public void searchFor(String text) {
				table.setEmptyTableWidget(tableWidget);
				searchString = text;
				search = true;
				findMembers.searchFor(text);
			}
		}, searchButton);
		searchBox.getTextBox().setText(searchString);

		// checkbox click handler
		disabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				wasDisabled = disabled.getValue();
				if (search) {
					// case when update but not triggered by button
					searchString = searchBox.getTextBox().getText();
					members.excludeDisabled(!disabled.getValue());
				} else {
					members.excludeDisabled(!disabled.getValue());
					members.retrieveData();
				}
			}
		});

		findMembers.setEvents(JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(searchButton, JsonCallbackEvents.disableCheckboxEvents(disabled)),
				new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						searchBox.getTextBox().setEnabled(true);
						listAllButton.setEnabled(true);
					}

					@Override
					public void onError(PerunError error) {
						searchBox.getTextBox().setEnabled(true);
						listAllButton.setEnabled(true);
					}

					@Override
					public void onLoadingStart() {
						searchBox.getTextBox().setEnabled(false);
						listAllButton.setEnabled(false);
						disabled.setVisible(false);
					}
				}
		));

		members.setEvents(JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(listAllButton, JsonCallbackEvents.disableCheckboxEvents(disabled)),
				new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						// pass data to table handling callback
						findMembers.onFinished(jso);
						((AjaxLoaderImage) table.getEmptyTableWidget()).setEmptyResultMessage("Group has no members.");
						searchBox.getTextBox().setEnabled(true);
						searchButton.setEnabled(true);
					}

					@Override
					public void onError(PerunError error) {
						// pass data to table handling callback
						findMembers.onError(error);
						searchBox.getTextBox().setEnabled(true);
						searchButton.setEnabled(true);
					}

					@Override
					public void onLoadingStart() {
						table.setEmptyTableWidget(tableWidget);
						searchBox.getTextBox().setEnabled(false);
						searchButton.setEnabled(false);
						disabled.setVisible(true);
						// to show progress when reloading
						((AjaxLoaderImage)table.getEmptyTableWidget()).loadingStart();
					}
				}
		));

		// LIST ALL BUTTON
		listAllButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				table.setEmptyTableWidget(tableWidget);
				search = false;
				searchString = "";
				searchBox.getTextBox().setText("");
				findMembers.clearTable();
				members.retrieveData();
			}
		});
		tabMenu.addWidget(listAllButton);

		tabMenu.addWidget(disabled);

		/* WHEN TAB RELOADS, CHECK THE STATE */
		if (search) {

			findMembers.searchFor(searchString);

		} else {

			GetGroupMembersCount count = new GetGroupMembersCount(groupId, new JsonCallbackEvents() {
				@Override
				public void onFinished(JavaScriptObject jso) {

					int membersCount = ((BasicOverlayType)jso.cast()).getInt();
					if (membersCount > 1000) {

						FlexTable panel = new FlexTable();
						panel.setSize("100%", "150px");
						HTML label = new HTML();
						label.setHTML("<h2>Group has "+membersCount+" members. We suggest you to use search instead.</h2><h2>You can search member by name, login, email or click List all to see all members.</h2>");

						panel.setWidget(0, 0, label);
						panel.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
						table.setEmptyTableWidget(panel);

					} else {

						// load all members
						members.excludeDisabled(!disabled.getValue());
						members.retrieveData();

					}
					onceAsked=true;

				}
				@Override
				public void onError(PerunError error) {
					// load all members
					members.excludeDisabled(!disabled.getValue());
					members.retrieveData();
				}
				@Override
				public void onLoadingStart() {
				}
			});

			if (!onceAsked) {
				count.retrieveData();
			} else {
				// load all members
				members.excludeDisabled(!disabled.getValue());
				members.retrieveData();
			}

		}

		ScrollPanel tableWrapper = new ScrollPanel();
		table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizePerunTable(tableWrapper, 350, this);

		// add menu and the table to the main panel
		firstTabPanel.add(tableWrapper);

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(findMembers, table, removeButton);

		this.contentWidget.setWidget(firstTabPanel);
		return getWidget();

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
		final int prime = 827;
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
		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(groupId)) {
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
