package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunStatus;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.groupsManager.GetGroupsCount;
import cz.metacentrum.perun.webgui.json.membersManager.GetMembersCount;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetResourcesCount;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.CreateGroupTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToResourceTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToVoTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.CreateServiceMemberInVoTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.InviteUserTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

/**
 * View VO overview details
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VoOverviewTabItem implements TabItem {

	/**
	 * VO
	 */
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
	private Label titleWidget = new Label("Loading vo");
	// VO name label
	private Label voNameLabel = new Label();
	// VO short name label
	private Label voShortNameLabel = new Label();
	// VO ID label
	private Label voIdLabel = new Label();
	private int voId;

	/**
	 * Creates a new view VO class
	 *
	 * @param vo
	 */
	public VoOverviewTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
		setLabels();
	}

	/**
	 * Creates a new view VO class
	 *
	 * @param voId
	 */
	public VoOverviewTabItem(int voId){
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

	private void setLabels() {
		this.titleWidget.setText(vo.getName());
		this.voNameLabel.setText(vo.getName());
		this.voIdLabel.setText(String.valueOf(vo.getId()));
		this.voShortNameLabel.setText(vo.getShortName());
	}

	public Widget draw() {

		// main panel
		ScrollPanel scroll = new ScrollPanel();
		final VerticalPanel vp2 = new VerticalPanel();
		vp2.setStyleName("perun-table");
		scroll.setWidget(vp2);
		scroll.setStyleName("perun-tableScrollPanel");
		vp2.setSpacing(5);

		// tools panel
		final DisclosurePanel tools = new DisclosurePanel();
		tools.setWidth("100%");
		tools.setOpen(true);
		FlexTable toolsHeader = new FlexTable();
		toolsHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.settingToolsIcon()));
		toolsHeader.setWidget(0, 1, new HTML("<h3>Quick tools</h3>"));
		toolsHeader.setTitle("Click to show/hide VO quick tools");
		tools.setHeader(toolsHeader);

		// tools panel content
		FlexTable toolsLayout = new FlexTable();
		toolsLayout.setSize("100%","100%");
		toolsLayout.setCellSpacing(5);
		tools.setContent(toolsLayout);

		toolsLayout.getFlexCellFormatter().setWidth(0, 0, "220px");

		toolsLayout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(2, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(2, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(3, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(3, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(4, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(5, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(5, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(6, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		toolsLayout.getFlexCellFormatter().setAlignment(6, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);

		CustomButton addMember = new CustomButton(ButtonTranslation.INSTANCE.addMemberButton()+"…", ButtonTranslation.INSTANCE.addMemberToVo(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new AddMemberToVoTabItem(vo), true);
			}
		});
		CustomButton addServiceMember = new CustomButton(ButtonTranslation.INSTANCE.createServiceMemberButton()+"…", ButtonTranslation.INSTANCE.createServiceMember(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new CreateServiceMemberInVoTabItem(vo));
			}
		});
		CustomButton inviteUser = new CustomButton("Invite member…", "Invite person to become member of your Virtual organization.", SmallIcons.INSTANCE.emailAddIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new InviteUserTabItem(vo, null));
			}
		});
		CustomButton addManager = new CustomButton(ButtonTranslation.INSTANCE.addManagerButton()+"…", ButtonTranslation.INSTANCE.addManagerToVo(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new AddVoManagerTabItem(vo), true);
			}
		});
		CustomButton createGroup = new CustomButton(ButtonTranslation.INSTANCE.createGroupButton()+"…", ButtonTranslation.INSTANCE.createGroup(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new CreateGroupTabItem(vo));
			}
		});
		CustomButton addToResource = new CustomButton(ButtonTranslation.INSTANCE.addMemberToResourceButton()+"…", ButtonTranslation.INSTANCE.addMemberToResource(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new AddMemberToResourceTabItem(vo), true);
			}
		});

		toolsLayout.setWidget(0, 0, addMember);
		toolsLayout.setWidget(1, 0, addServiceMember);
		toolsLayout.setWidget(2, 0, inviteUser);
		toolsLayout.setWidget(3, 0, addManager);
		toolsLayout.setWidget(4, 0, createGroup);
		toolsLayout.setWidget(5, 0, addToResource);

		if (!session.isVoAdmin(voId)) addMember.setEnabled(false);
		if (!session.isVoAdmin(voId)) addServiceMember.setEnabled(false);
		if (!session.isVoAdmin(voId)) inviteUser.setEnabled(false);
		if (!session.isVoAdmin(voId)) addManager.setEnabled(false);
		if (!session.isVoAdmin(voId)) createGroup.setEnabled(false);
		if (!session.isVoAdmin(voId)) addToResource.setEnabled(false);

		toolsLayout.setHTML(0, 1, "Add new member into your VO. Candidates can be searched for in VO's external sources or among user already existing in Perun.");
		toolsLayout.setHTML(1, 1, "Create new member which represent service account (account usually used by more users with separate login and password).");
		toolsLayout.setHTML(2, 1, "Invite person to become member of your Virtual organization.");
		toolsLayout.setHTML(3, 1, "Add new manager which can manage your VO in Perun.");
		toolsLayout.setHTML(4, 1, "Create new group in your VO.");
		toolsLayout.setHTML(5, 1, "Add selected member to specific resource (grant some type of access to Facility resources).");

		vp2.add(tools);

		// statistics - json

		// call the request for number of members
		final GetMembersCount countMembers = new GetMembersCount(vo.getId(), null);
		final GetMembersCount countValidMembers = new GetMembersCount(vo.getId(), PerunStatus.VALID);
		final GetMembersCount countInvalidMembers = new GetMembersCount(vo.getId(), PerunStatus.INVALID);
		final GetMembersCount countSuspendedMembers = new GetMembersCount(vo.getId(), PerunStatus.SUSPENDED);
		final GetMembersCount countExpiredMembers = new GetMembersCount(vo.getId(), PerunStatus.EXPIRED);
		final GetMembersCount countDisabledMembers = new GetMembersCount(vo.getId(), PerunStatus.DISABLED);

		// call the request for number of resources
		final GetResourcesCount countResources = new GetResourcesCount(vo.getId());
		// call the request for number of groups
		final GetGroupsCount countGroups = new GetGroupsCount(vo.getId());

		// statistics
		final DisclosurePanel statistics = new DisclosurePanel();
		statistics.setWidth("100%");
		//statistics.setOpen(true);
		FlexTable statHeader = new FlexTable();
		statHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.statisticsIcon()));
		statHeader.setWidget(0, 1, new HTML("<h3>Statistics</h3>"));
		statHeader.setTitle("Click to show/hide VO statistics");
		statistics.setHeader(statHeader);

		final FlexTable vosTable = new FlexTable();
		vosTable.addStyleName("statisticsTable");

		statHeader.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (statistics.getContent() == null) {

					countMembers.retrieveData();
					countValidMembers.retrieveData();
					countInvalidMembers.retrieveData();
					countSuspendedMembers.retrieveData();
					countExpiredMembers.retrieveData();
					countDisabledMembers.retrieveData();

					countResources.retrieveData();
					countGroups.retrieveData();

					statistics.setContent(vosTable);
				}
			}
		});

		vosTable.setWidget(0, 0, new HTML("<strong>" + "Members" + "</strong>"));
		vosTable.setWidget(0, 1, countMembers.getMembersCountLabel());
		vosTable.setWidget(1, 0, new HTML(" - valid"));
		vosTable.setWidget(1, 1, countValidMembers.getMembersCountLabel());
		vosTable.setWidget(2, 0, new HTML(" - invalid"));
		vosTable.setWidget(2, 1, countInvalidMembers.getMembersCountLabel());
		vosTable.setWidget(3, 0, new HTML(" - suspended"));
		vosTable.setWidget(3, 1, countSuspendedMembers.getMembersCountLabel());
		vosTable.setWidget(4, 0, new HTML(" - expired"));
		vosTable.setWidget(4, 1, countExpiredMembers.getMembersCountLabel());
		vosTable.setWidget(5, 0, new HTML(" - disabled"));
		vosTable.setWidget(5, 1, countDisabledMembers.getMembersCountLabel());

		vosTable.setWidget(6, 0, new HTML("<strong>" + "Resources" + "</strong>"));
		vosTable.setWidget(6, 1, countResources.getResourcesCountLabel());

		vosTable.setWidget(7, 0, new HTML("<strong>" + "Groups" + "</strong>"));
		vosTable.setWidget(7, 1, countGroups.getGroupsCountLabel());

		vp2.add(statistics);

		session.getUiElements().resizeSmallTabPanel(scroll, 350, this);

		contentWidget.setWidget(scroll);

		return getWidget();
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
		final int prime = 1321;
		int result = 1;
		result = prime * result + voId;
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
		VoOverviewTabItem other = (VoOverviewTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isVoObserver(voId)) {
			return true;
		} else {
			return false;
		}

	}

}
