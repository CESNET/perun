package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.usersManager.AddSpecificUserOwner;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab which allow to add new identity to user/service user
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ConnectServiceIdentityTabItem implements TabItem, TabItemWithUrl {

	/**
	 * User id
	 */
	private int userId;
	/**
	 * User
	 */
	private User user;

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

	/**
	 * Creates a new instance
	 *
	 * @param userId
	 */
	public ConnectServiceIdentityTabItem(int userId){
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				user = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a new instance
	 *
	 * @param user
	 */
	public ConnectServiceIdentityTabItem(User user){
		this.userId = user.getId();
		this.user = user;
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return true;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText("Connect identity");

		VerticalPanel content = new VerticalPanel();
		content.setSize("100%", "100%");

		final TabItem tab = this;

		// add button
		final CustomButton addButton;

		if (user.isServiceUser() || user.isSponsoredUser()) {
			addButton = new CustomButton("Connect", "Add selected users to this identity",SmallIcons.INSTANCE.addIcon());
		} else {
			addButton = new CustomButton("Connect", "Add selected identities to user",SmallIcons.INSTANCE.addIcon());
		}

		TabMenu menu = new TabMenu();
		menu.addWidget(addButton);

		content.add(menu);
		content.setCellHeight(menu, "30px");

		final FindCompleteRichUsers call = new FindCompleteRichUsers("", null);
		if (user.isServiceUser()) {
			call.hideService(true);
		}
		if (user.isSponsoredUser()) {
			call.hideSponsored(true);
			call.hideService(true);
		}
		if (!user.isSpecificUser()) {
			call.hidePerson(true);
		}

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {
				// close tab and refresh
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// search textbox
		ExtendedTextBox searchBox = menu.addSearchWidget(new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				call.searchFor(text);
			}
		}, "");

		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {

				ArrayList<User> list = call.getTableSelectedList();
				for (int i = 0; i < list.size(); i++) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
					AddSpecificUserOwner req;
					if (i == list.size() - 1) {
						req = new AddSpecificUserOwner(JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
					} else {
						req = new AddSpecificUserOwner(JsonCallbackEvents.disableButtonEvents(addButton));
					}
					if (user.isServiceUser() || user.isSponsoredUser()) {
						// service user adds user
						req.addSpecificUser(list.get(i), user);
					} else {
						// user adds service users
						req.addSpecificUser(user, list.get(i));
					}
				}
			}
		});

		FieldUpdater<User, String> fieldUpdater = null;
		if (session.isPerunAdmin()) {
			fieldUpdater = new FieldUpdater<User, String>() {
				public void update(int i, User user, String s) {
					session.getTabManager().addTab(new UserDetailTabItem(user));
				}
			};
		}
		CellTable<User> table = call.getTable(fieldUpdater);
		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		addButton.setEnabled(false);
		JsonUtils.addTableManagedButton(call, table, addButton);

		content.add(sp);
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		this.contentWidget.setWidget(new SimplePanel(content));

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1187;
		int result = 1;
		result = prime * result + userId;
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
		ConnectServiceIdentityTabItem other = (ConnectServiceIdentityTabItem) obj;
		if (userId != other.userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{

	}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "connect-identity";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
	}

	static public ConnectServiceIdentityTabItem load(Map<String, String> parameters) {
		int uid = Integer.parseInt(parameters.get("id"));
		return new ConnectServiceIdentityTabItem(uid);
	}

}
