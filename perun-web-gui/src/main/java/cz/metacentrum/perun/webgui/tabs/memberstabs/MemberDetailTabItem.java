package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

import java.util.Map;

/**
 * Displays complex member information
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class MemberDetailTabItem implements TabItem, TabItemWithUrl {

	/**
	 * member
	 */
	private RichMember member;
	private int memberId;
	private int groupId = 0; // by default no group
	// loaded only for breadcrumbs
	private VirtualOrganization vo;
	private Group group;

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
	private Label titleWidget = new Label("Loading member details");
	TabPanelForTabItems tabPanel;

	/**
	 * Constructor
	 *
	 * @param member RichMember object, typically from table
	 */
	public MemberDetailTabItem(RichMember member){
		this.member = member;
		this.memberId = member.getId();
		this.tabPanel = new TabPanelForTabItems(this);
	}

	/**
	 * Constructor
	 *
	 * @param memberId RichMember ID
	 * @param groupId
	 */
	public MemberDetailTabItem(int memberId, int groupId){
		this.memberId = memberId;
		this.tabPanel = new TabPanelForTabItems(this);
		new GetEntityById(PerunEntity.RICH_MEMBER, memberId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				member = jso.cast();
			}
		}).retrieveData();
		this.groupId = groupId;
	}

	public boolean isPrepared(){
		return !(member == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()));

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// The table
		AbsolutePanel dp = new AbsolutePanel();
		//dp.setStyleName("decoration");
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userGreenIcon()));
		Label memberName = new Label();
		memberName.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles(), 40));
		memberName.setStyleName("now-managing");
		memberName.setTitle(member.getUser().getFullNameWithTitles());
		menu.setWidget(0, 1, memberName);

		int column = 2;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;
		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>Member ID:</strong><br/><span class=\"inputFormInlineComment\">"+member.getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
			menu.setHTML(0, column, "<strong>User ID:</strong><br/><span class=\"inputFormInlineComment\">"+member.getUser().getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
		}

		Anchor a = new Anchor("See user detail >>");
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTab(new UserDetailTabItem(member.getUser()));
			}
		});
		if (session.isPerunAdmin()) {
			menu.setWidget(0, column, a);
		}

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		tabPanel.clear();

		tabPanel.add(new MemberOverviewTabItem(member, groupId), "Overview");
		tabPanel.add(new MemberGroupsTabItem(member, groupId), "Groups");
		tabPanel.add(new MemberResourcesTabItem(member, groupId), "Resources");
		tabPanel.add(new MemberApplicationsTabItem(member, groupId), "Applications");
		tabPanel.add(new MemberSettingsTabItem(member, groupId), "Settings");
		if (member.getUser().isServiceUser()) {
			tabPanel.add(new MemberServiceUsersTabItem(member, groupId), "Associated users");
		} else {
			tabPanel.add(new MemberServiceUsersTabItem(member, groupId), "Service identities");
		}
		if (member.getUser().isSponsoredUser()) {
			tabPanel.add(new MemberSponsoredUsersTabItem(member, groupId), "Sponsors");
		} else if (!member.getUser().isSponsoredUser() && !member.getUser().isServiceUser()) {
			tabPanel.add(new MemberSponsoredUsersTabItem(member, groupId), "Sponsored users");
		}

		// Resize must be called after page fully displays
		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				tabPanel.finishAdding();
			}
		});

		vp.add(tabPanel);

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
		return SmallIcons.INSTANCE.userGreenIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 881;
		int result = 1;
		result = prime * result + memberId;
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
		MemberDetailTabItem other = (MemberDetailTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

		if (session.isVoAdmin(member.getVoId())) {
			if (groupId == 0) {
				// accessed from VO admin section
				if (vo == null) {
					// load
					new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, member.getVoId(), new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							vo = jso.cast();
							session.setActiveVo(vo);
							session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
							session.getUiElements().getBreadcrumbs().setLocation(vo, "Members", VosTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"members?id="+vo.getId());
						}
					}).retrieveData();
				} else {
					// display
					session.setActiveVo(vo);
					session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
					session.getUiElements().getBreadcrumbs().setLocation(vo, "Members", VosTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"members?id="+vo.getId());
				}
			} else {
				// accessed from Group admin section
				if (group == null) {
					// load
					new GetEntityById(PerunEntity.GROUP, groupId, new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							group = jso.cast();
							session.setActiveGroup(group);
							session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
							session.getUiElements().getBreadcrumbs().setLocation(group, "Members", GroupsTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"members?id="+group.getId());
						}
					}).retrieveData();
				} else {
					// display
					session.setActiveGroup(group);
					session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
					session.getUiElements().getBreadcrumbs().setLocation(group, "Members", GroupsTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"members?id="+group.getId());
				}
			}
		} else {
			// only group admin in VO
			if (groupId != 0) {
				if (group == null) {
					// load
					new GetEntityById(PerunEntity.GROUP, groupId, new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							group = jso.cast();
							session.setActiveGroup(group);
							session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
							session.getUiElements().getBreadcrumbs().setLocation(group, "Members", GroupsTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"members?id="+group.getId());

						}
					}).retrieveData();
				} else {
					// display
					session.setActiveGroup(group);
					session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
					session.getUiElements().getBreadcrumbs().setLocation(group, "Members", GroupsTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"members?id="+group.getId());
				}
			}
		}

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId()) || session.isVoObserver(member.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "rich-detail";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return MembersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + memberId + "&gid=" + groupId;
	}

	static public MemberDetailTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		int gid = Integer.parseInt(parameters.get("gid"));
		return new MemberDetailTabItem(id, gid);
	}

}
