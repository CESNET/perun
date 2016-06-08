package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.usersManager.GetSpecificUsersByUser;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * Page with Users for perun admin but in "User section"
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class IdentitySelectorTabItem implements TabItem, TabItemWithUrl {

	private PerunWebSession session = PerunWebSession.getInstance();
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Select identity");

	/**
	 * Creates a tab instance
	 */
	public IdentitySelectorTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		final TabItem tab = this;

		HorizontalPanel horizontalSplitter = new HorizontalPanel();
		horizontalSplitter.setHeight("500px");
		horizontalSplitter.setWidth("100%");

		// BASE LAYOUT
		DecoratorPanel dp = new DecoratorPanel();
		FlexTable baseLayout = new FlexTable();
		baseLayout.setCellSpacing(10);
		dp.add(baseLayout);
		baseLayout.setHTML(0, 0, "<p class=\"subsection-heading\">Select base identity</p>");
		baseLayout.setHTML(1, 0, "Your base identity you are currently logged in.");
		baseLayout.getFlexCellFormatter().setStyleName(1, 0, "inputFormInlineComment");

		Anchor userName = new Anchor();
		userName.setText(session.getUser().getFullNameWithTitles());
		userName.addStyleName("now-managing");
		userName.addStyleName("pointer");
		userName.setTitle(session.getUser().getFullNameWithTitles());
		userName.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTab(new SelfDetailTabItem(session.getUser()));
				session.getTabManager().closeTab(tab, false);
			}
		});
		baseLayout.setWidget(2, 0, new Image(LargeIcons.INSTANCE.userGrayIcon()));
		baseLayout.setWidget(2, 1, userName);

		baseLayout.getFlexCellFormatter().setColSpan(0, 0, 2);
		baseLayout.getFlexCellFormatter().setColSpan(1, 0, 2);

		// SERVICE IDENTITIES LAYOUT
		DecoratorPanel dp2 = new DecoratorPanel();
		final FlexTable serviceLayout = new FlexTable();
		serviceLayout.setCellSpacing(10);
		dp2.add(serviceLayout);

		serviceLayout.setHTML(0, 0, "<p class=\"subsection-heading\">Select service identity</p>");
		serviceLayout.setHTML(1, 0, "Service identities you have access to.");
		serviceLayout.getFlexCellFormatter().setStyleName(1, 0, "inputFormInlineComment");

		horizontalSplitter.add(dp);
		horizontalSplitter.setCellWidth(dp, "50%");
		horizontalSplitter.setCellVerticalAlignment(dp, HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalSplitter.setCellHorizontalAlignment(dp, HasHorizontalAlignment.ALIGN_CENTER);

		ScrollPanel sp = new ScrollPanel();
		final FlexTable innerTable = new FlexTable();
		sp.setWidget(innerTable);
		sp.setStyleName("scroll-max-height");
		serviceLayout.setWidget(2, 0, sp);

		if (session.getEditableUsers().size() > 1) {
			// user has service identities
			GetSpecificUsersByUser call = new GetSpecificUsersByUser(session.getUser().getId(), new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					ArrayList<User> list = JsonUtils.jsoAsList(jso);
					if (list != null && !list.isEmpty()) {

						int row = 0;
						for (User u : list) {
							if (u.isSponsoredUser()) continue;
							final User u2 = u;
							innerTable.setWidget(row, 0, new Image(LargeIcons.INSTANCE.userRedIcon()));
							Anchor userName = new Anchor();
							userName.setText(u2.getFullNameWithTitles());
							userName.addStyleName("now-managing");
							userName.addStyleName("pointer");
							userName.setTitle(u2.getFullNameWithTitles());
							userName.addClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									session.getTabManager().addTab(new SelfDetailTabItem(u2));
									session.getTabManager().closeTab(tab, false);
								}
							});
							innerTable.setWidget(row, 1, userName);
							row++;
						}

					} else {
						innerTable.setHTML(0, 0, "You have no service identities");
					}
				}
			@Override
			public void onLoadingStart() {
				innerTable.setWidget(0, 0, new AjaxLoaderImage().loadingStart());
			}
			@Override
			public void onError(PerunError error) {
				innerTable.setWidget(0, 0, new AjaxLoaderImage().loadingError(error));
			}
			});
			call.retrieveData();

			horizontalSplitter.add(dp2);
			horizontalSplitter.setCellWidth(dp2, "50%");
			horizontalSplitter.setCellHorizontalAlignment(dp2, HasHorizontalAlignment.ALIGN_CENTER);
			horizontalSplitter.setCellVerticalAlignment(dp2, HasVerticalAlignment.ALIGN_MIDDLE);

		}

		this.contentWidget.setWidget(horizontalSplitter);

		return getWidget();

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
		final int prime = 1201;
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

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.USER, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "Select identity", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isSelf()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "self-users";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public IdentitySelectorTabItem load(Map<String, String> parameters) {
		return new IdentitySelectorTabItem();
	}

}
