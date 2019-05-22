package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.cabinettabs.UsersPublicationsTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.Map;

/**
 * Page with user's details for user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfDetailTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading user");

	private TabPanelForTabItems tabPanel;

	private User user;
	private int userId = 0;

	/**
	 * Creates a tab instance
	 */
	public SelfDetailTabItem(){
		this.user = session.getActiveUser();
		this.userId = user.getId();
		this.tabPanel = new TabPanelForTabItems(this);
	}

	/**
	 * Creates a tab instance with custom user
	 * @param user
	 */
	public SelfDetailTabItem(User user){
		this.user = user;
		this.userId = user.getId();
		this.tabPanel = new TabPanelForTabItems(this);
	}

	/**
	 * Creates a tab instance with custom user
	 * @param userId
	 */
	public SelfDetailTabItem(int userId) {
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				user = jso.cast();
			}
		}).retrieveData();
		this.tabPanel = new TabPanelForTabItems(this);
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()));

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// The table
		AbsolutePanel dp = new AbsolutePanel();
		//dp.setStyleName("decoration");
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		if (user.isServiceUser()) {
			menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userRedIcon()));
		} else {
			menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userGrayIcon()));
		}
		Label userName = new Label();
		userName.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles(), 40));
		userName.setStyleName("now-managing");
		userName.setTitle(user.getFullNameWithTitles());
		menu.setWidget(0, 1, userName);

		menu.setHTML(0, 2, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, 2, "25px");

		int column = 3;

		final TabItem tab = this;
		final JsonCallbackEvents events = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				user = jso.cast();
				tab.open();
				tab.draw();
			}
		};

		CustomButton change = new CustomButton("", "Edit user", SmallIcons.INSTANCE.applicationFormEditIcon());
		change.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new EditUserDetailsTabItem(user, events));
			}
		});
		menu.setWidget(0, column,  change);

		column++;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;

		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">"+user.getId()+"</span>");
			column++;

			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
		}

		String type = "Person";
		type = user.isServiceUser() ? "Service" : "Person";
		type = user.isSponsoredUser() ? "Sponsored" : "Person";
		menu.setHTML(0, column, "<strong>User type:</strong><br/><span class=\"inputFormInlineComment\">"+type+"</span>");

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		tabPanel.clear();

		SelfPersonalTabItem item = new SelfPersonalTabItem(user);
		item.setParentPanel(tabPanel);
		tabPanel.add(item, "Overview");

		SelfVosTabItem vosTab = new SelfVosTabItem(user);
		vosTab.setParentPanel(tabPanel);
		tabPanel.add(vosTab, "VO settings");

		tabPanel.add(new SelfResourcesSettingsTabItem(user), "Resources settings");
		tabPanel.add(new SelfAuthenticationsTabItem(user), "Authentication");

		if (!user.isServiceUser()) {
			tabPanel.add(new UsersPublicationsTabItem(user), "Publications");
		}
		tabPanel.add(new SelfApplicationsTabItem(user), "Applications");
		if (!user.isServiceUser()) {
			tabPanel.add(new SelfServiceUsersTabItem(user), "Service identities");
		} else {
			tabPanel.add(new SelfServiceUsersTabItem(user), "Associated users");
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

	/**
	 * Method used for setting user when tab supposed to be refreshed
	 * @param user
	 */
	public void setUser(User user){
		this.user = user;
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.userGrayIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1231;
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
		if (this.userId != ((SelfDetailTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.setActiveUser(user);
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "info";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
	}

	static public SelfDetailTabItem load(Map<String, String> parameters) {
		if (parameters.containsKey("id")) {
			int uid = Integer.parseInt(parameters.get("id"));
			if (uid != 0) {
				return new SelfDetailTabItem(uid);
			}
		}
		return new SelfDetailTabItem();
	}

}
