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
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToVoAndGroupTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToVoTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.InviteUserTabItem;
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
	private boolean wasDisabled = false;
	String searchString = "";

	CellTable<RichMember> table;
	Widget tableWidget;

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

	public Widget draw() {

		// SET TAB NAME
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": members");

		// MAIN PANEL
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// DISABLED CHECKBOX
		final CheckBox disabled = new CheckBox(WidgetTranslation.INSTANCE.showDisabledMembers());
		disabled.setTitle(WidgetTranslation.INSTANCE.showDisabledMembersTitle());
		disabled.setValue(wasDisabled);

		// CALLBACKS
		final GetCompleteRichMembers members = new GetCompleteRichMembers(PerunEntity.GROUP, groupId, null, JsonCallbackEvents.disableCheckboxEvents(disabled));
		members.setIndirectCheckable(false);
		members.excludeDisabled(!disabled.getValue());
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) members.setCheckable(false);

		// refreshMembers
		final JsonCallbackEvents refreshMembersEvent = JsonCallbackEvents.refreshTableEvents(members);

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
			if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) addButton.setEnabled(false);
			tabMenu.addWidget(addButton);

			// REMOVE

			// remove button
			removeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final ArrayList<RichMember> itemsToRemove = members.getTableSelectedList();
					final JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(removeButton);
					final JsonCallbackEvents refreshEvents = JsonCallbackEvents.disableButtonEvents(removeButton, refreshMembersEvent);
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
			if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) removeButton.setEnabled(false);
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


		CustomButton inviteButton = new CustomButton("Invite user", SmallIcons.INSTANCE.emailAddIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new InviteUserTabItem(group.getVoId(), group));
			}
		});
		tabMenu.addWidget(inviteButton);

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

		tabMenu.addWidget(disabled);

		vp.add(tabMenu);
		vp.setCellHeight(tabMenu, "30px");
		vp.add(pageWidget);

		/* WHEN TAB RELOADS, CHECK THE STATE */
		listAllAction(members, removeButton, disabled);

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

		GetGroupMembersCount getGroupMembersCount = new GetGroupMembersCount(groupId, new JsonCallbackEvents(){

			@Override
			public void onError(PerunError error){
				table.setEmptyTableWidget(tableWidget);
				members.retrieveData();
			}

			@Override
			public void onLoadingStart(){

				table = members.getEmptyTable(new FieldUpdater<RichMember, RichMember>() {
					// when user click on a row -> open new tab
					public void update(int index, RichMember object, RichMember value) {
						// TODO better auth
						session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), groupId));
					}
				});

				tableWidget = table.getEmptyTableWidget();

			}

			@Override
			public void onFinished(JavaScriptObject jso) {
				BasicOverlayType count = (BasicOverlayType) jso;

				if(count.getInt() > 1000){

					FlexTable panel = new FlexTable();
					panel.setSize("100%", "150px");
					HTML label = new HTML();
					label.setHTML("<h2>Group has more than 1000 members, do you wish to load all of them ?</h2>");
					CustomButton loadAllMembersButton = new CustomButton("Load all members", SmallIcons.INSTANCE.userGreenIcon(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							table.setEmptyTableWidget(tableWidget);
							members.retrieveData();
						}
					});
					panel.setWidget(0, 0, label);
					panel.setWidget(1, 0, loadAllMembersButton);
					panel.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
					panel.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
					table.setEmptyTableWidget(panel);
				}
				else{
					table.setEmptyTableWidget(tableWidget);
					members.retrieveData();
				}

			}


		});

		getGroupMembersCount.retrieveData();

		if (session.isGroupAdmin(groupId) || session.isVoAdmin(group.getVoId())) JsonUtils.addTableManagedButton(members, table, removeButton);

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		tableWrapper.setWidget(table);
		tableWrapper.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizePerunTable(tableWrapper, 350, this);

		// add the table to the main panel
		setPageWidget(tableWrapper);

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
