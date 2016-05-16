package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.usersManager.GetSpecificUsersByUser;
import cz.metacentrum.perun.webgui.json.usersManager.GetUsersBySpecificUser;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Tab for managing service identities for users.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class SelfSponsoredUsersTabItem implements TabItem, TabItemWithUrl {

	private PerunWebSession session = PerunWebSession.getInstance();
	private User user;
	private int userId;

	private Label titleWidget = new Label("Loading user");
	private SimplePanel contentWidget = new SimplePanel();

	public SelfSponsoredUsersTabItem(User user) {
		this.user = user;
		this.userId = user.getId();
	}

	public SelfSponsoredUsersTabItem(int userId) {
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				user = jso.cast();
			}
		}).retrieveData();
	}

	@Override
	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		if (user.isSponsoredUser()) {

			// SERVICE TYPE user
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": Sponsors");

			// request
			final GetUsersBySpecificUser request = new GetUsersBySpecificUser(userId);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");

			menu.addWidget(UiElements.getRefreshButton(this));

			// table
			CellTable<User> table;
			if (session.isPerunAdmin()) {
				table = request.getTable(new FieldUpdater<User, String>() {
					public void update(int i, User user, String s) {
						session.getTabManager().addTab(new SelfDetailTabItem(user));
					}
				});
			} else {
				table = request.getTable();
			}

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

			session.getUiElements().resizePerunTable(sp, 350, this);

		} else {

			// PERSON TYPE user
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+": Sponsored users");

			// request
			final GetSpecificUsersByUser request = new GetSpecificUsersByUser(userId);
			request.setHideService(true);

			// menu
			TabMenu menu = new TabMenu();
			vp.add(menu);
			vp.setCellHeight(menu, "30px");
			menu.addWidget(UiElements.getRefreshButton(this));

			// table
			CellTable<User> table;
			if (session.isPerunAdmin()) {
				table = request.getTable(new FieldUpdater<User, String>() {
					public void update(int i, User user, String s) {
						session.getTabManager().addTab(new SelfDetailTabItem(user));
					}
				});
			} else {
				table = request.getTable();
			}

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

			session.getUiElements().resizePerunTable(sp, 350, this);

		}

		contentWidget.setWidget(vp);

		return getWidget();
	}

	@Override
	public Widget getWidget() {
		return contentWidget;
	}

	@Override
	public Widget getTitle() {
		return titleWidget;
	}

	@Override
	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.userGrayIcon();
	}

	@Override
	public boolean multipleInstancesEnabled() {
		return false;
	}

	@Override
	public void open() {
		session.setActiveUser(user);
		session.getUiElements().getMenu().openMenu(MainMenu.USER);
		if (!user.isSponsoredUser()) {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "Sponsored users", getUrlWithParameters());
		} else {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim()), UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId, "Sponsors", getUrlWithParameters());
		}
	}

	@Override
	public boolean isAuthorized() {
		if ((session.isSelf(userId) && !user.isServiceUser()) || session.isSponsor(userId)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isPrepared() {
		return (user != null);
	}

	@Override
	public int hashCode() {
		final int prime = 1259;
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
		if (this.userId != ((SelfSponsoredUsersTabItem)obj).userId)
			return false;

		return true;
	}

	public final static String URL = "sponsored-users";

	@Override
	public String getUrl() {
		return URL;
	}

	@Override
	public String getUrlWithParameters() {
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id="+userId;
	}

	static public SelfSponsoredUsersTabItem load(Map<String, String> parameters) {
		int uid = Integer.parseInt(parameters.get("id"));
		return new SelfSponsoredUsersTabItem(uid);
	}

}
