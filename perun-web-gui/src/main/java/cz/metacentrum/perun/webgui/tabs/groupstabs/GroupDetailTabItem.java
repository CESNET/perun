package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichGroup;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

import java.util.Map;

/**
 * Displays a group detail
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupDetailTabItem implements TabItem, TabItemWithUrl{

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

	private Group group = null;
	private int groupId = 0;
	private VirtualOrganization vo = null;
	// sub panels
	TabPanelForTabItems tabPanel;

	/**
	 * Creates a tab instance
	 *
	 * @param group
	 */
	public GroupDetailTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
		tabPanel = new TabPanelForTabItems(this);
	}

	/**
	 * Creates a tab instance
	 *
	 * @param groupId
	 */
	public GroupDetailTabItem(int groupId){
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.RICH_GROUP, groupId, events).retrieveData();
		tabPanel = new TabPanelForTabItems(this);
	}

	public boolean isPrepared(){
		return (group != null);
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()));

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// The table
		AbsolutePanel dp = new AbsolutePanel();
		//dp.setStyleName("decoration");
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		// Add group information
		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.groupIcon()));
		Label groupName = new Label();
		groupName.setText(Utils.getStrippedStringWithEllipsis(group.getName(), 40));
		groupName.setStyleName("now-managing");
		groupName.setTitle(group.getName());
		menu.setWidget(0, 1, groupName);

		final JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				new GetEntityById(PerunEntity.RICH_GROUP, groupId, new JsonCallbackEvents(){
					public void onFinished(JavaScriptObject jso){
						group = jso.cast();
						open();
						draw();
					}
				}).retrieveData();
			}
		};

		menu.setHTML(0, 2, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, 2, "25px");

		int column = 3;

		CustomButton change;
		if (group.isCoreGroup()) {
			change = new CustomButton("", "Core group's name and description can't be changed", SmallIcons.INSTANCE.applicationFormEditIcon());
			change.setEnabled(false);
		} else {
			change = new CustomButton("", ButtonTranslation.INSTANCE.editGroupDetails(), SmallIcons.INSTANCE.applicationFormEditIcon());
		}
		if (!session.isGroupAdmin(groupId) && !session.isVoAdmin(group.getVoId())) change.setEnabled(false);
		change.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new EditGroupDetailsTabItem(group, events));
			}
		});
		menu.setWidget(0, column, change);
		column++;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;

		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">"+group.getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
		}

		/*
		String text = (((RichGroup)group).isSyncEnabled()) ? "Enabled" : "Disabled";

		text += (((RichGroup)group).getAuthoritativeGroup().equals("1")) ? " / Authoritative" : "";

		menu.setHTML(0, column, "<strong>Sync:</strong><br/><span class=\"inputFormInlineComment\">"+text+"</span>");

		column++;
		*/

		menu.setHTML(0, column, "<strong>Description:</strong><br/><span class=\"inputFormInlineComment\">"+group.getDescription()+"&nbsp;</span>");

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		tabPanel.clear();

		tabPanel.add(new GroupMembersTabItem(group), "Members");
		if (!group.isCoreGroup()) {
			tabPanel.add(new SubgroupsTabItem(group), "Subgroups");
		}
		tabPanel.add(new GroupResourcesTabItem(group), "Resources");
		if (!group.isCoreGroup()) {
			tabPanel.add(new GroupApplicationsTabItem(group), "Applications");
			tabPanel.add(new GroupApplicationFormSettingsTabItem(group), "Application form");
		}
		tabPanel.add(new GroupSettingsTabItem(group), "Settings");
		if (!group.isCoreGroup()) {
			// core groups can't have managers
			tabPanel.add(new GroupManagersTabItem(group), "Managers");
		}
		if (!group.isCoreGroup()) {
			// core groups can't have ext sources
			tabPanel.add(new GroupExtSourcesTabItem(group), "External sources");
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
		return SmallIcons.INSTANCE.groupIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 821;
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

		GroupDetailTabItem create = (GroupDetailTabItem) obj;

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

		if (vo == null) {
			new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, group.getVoId(), new JsonCallbackEvents() {
				@Override
				public void onFinished(JavaScriptObject jso) {
					vo = jso.cast();
					session.getUiElements().getBreadcrumbs().setLocation(MainMenu.GROUP_ADMIN, "VO: " + vo.getName(), VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + "detail?id=" + vo.getId(), "Group: " + group.getName(), getUrlWithParameters());
				}

				@Override
				public void onError(PerunError error) {
					session.getUiElements().getBreadcrumbs().setLocation(MainMenu.GROUP_ADMIN, "Group: " + group.getName(), getUrlWithParameters());
				}
			}
			).retrieveData();
		} else {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.GROUP_ADMIN, "VO: "+vo.getName(), VosTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"detail?id="+vo.getId(), "Group: "+group.getName(), getUrlWithParameters());
		}

		if(group != null){
			session.setActiveGroup(group);
		} else {
			session.setActiveGroupId(groupId);
		}
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "detail";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public GroupDetailTabItem load(Map<String, String> parameters) {
		int gid = Integer.parseInt(parameters.get("id"));
		return new GroupDetailTabItem(gid);
	}
}
