package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.UserExtSource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

import java.util.Map;

/**
 * Page with Users UserExtSource for Perun Admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class UserExtSourceDetailTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("UserExtSource detail");

	private int userExtSourceId = 0;
	private UserExtSource userExtSource;
	TabPanelForTabItems tabPanel;

	/**
	 * Creates a tab instance
	 */
	public UserExtSourceDetailTabItem(int userExtSourceId) {
		this.userExtSourceId = userExtSourceId;
		this.tabPanel = new TabPanelForTabItems(this);
		new GetEntityById(PerunEntity.USER_EXT_SOURCE, userExtSourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				userExtSource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 */
	public UserExtSourceDetailTabItem(UserExtSource ues) {
		this.userExtSourceId = ues.getId();
		this.userExtSource = ues;
		this.tabPanel = new TabPanelForTabItems(this);
	}

	public boolean isPrepared(){
		return !(userExtSource == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(userExtSource.getLogin().trim()));

		// MAIN TAB PANEL
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// The table
		AbsolutePanel dp = new AbsolutePanel();
		//dp.setStyleName("decoration");
		final FlexTable menu = new FlexTable();
		menu.setCellSpacing(5);

		menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.worldIcon()));
		Label memberName = new Label();
		memberName.setText(Utils.getStrippedStringWithEllipsis(userExtSource.getLogin(), 40));
		memberName.setStyleName("now-managing");
		memberName.setTitle(userExtSource.getLogin());
		menu.setWidget(0, 1, memberName);

		int column = 2;
		menu.setHTML(0, column, "&nbsp;");
		menu.getFlexCellFormatter().setWidth(0, column, "25px");
		column++;
		if (JsonUtils.isExtendedInfoVisible()) {
			menu.setHTML(0, column, "<strong>UES ID:</strong><br/><span class=\"inputFormInlineComment\">"+userExtSource.getId()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
			menu.setHTML(0, column, "<strong>ES name:</strong><br/><span class=\"inputFormInlineComment\">"+ SafeHtmlUtils.fromString((userExtSource.getExtSource().getName() != null) ? userExtSource.getExtSource().getName() : "").asString()+"</span>");
			column++;
			menu.setHTML(0, column, "&nbsp;");
			menu.getFlexCellFormatter().setWidth(0, column, "25px");
			column++;
			menu.setHTML(0, column, "<strong>ES type:</strong><br/><span class=\"inputFormInlineComment\">"+SafeHtmlUtils.fromString((userExtSource.getExtSource().getType() != null) ? userExtSource.getExtSource().getType() : "").asString()+"</span>");
		}

		dp.add(menu);
		vp.add(dp);
		vp.setCellHeight(dp, "30px");

		tabPanel.clear();

		tabPanel.add(new UserExtSourceSettingsTabItem(userExtSource), "Settings");

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
		return SmallIcons.INSTANCE.worldIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 9887;
		int result = 432;
		result = prime * result;
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
		UserExtSourceDetailTabItem other = (UserExtSourceDetailTabItem) obj;
		if (userExtSourceId != other.userExtSourceId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Users", UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl(), "UserExtSource detail", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "user-ext-src";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userExtSourceId;
	}

	static public UserExtSourceDetailTabItem load(Map<String, String> parameters) {
		int uesId = 0;
		if (parameters.containsKey("id")) {
			uesId = Integer.parseInt(parameters.get("id"));
		}
		return new UserExtSourceDetailTabItem(uesId);
	}

}
