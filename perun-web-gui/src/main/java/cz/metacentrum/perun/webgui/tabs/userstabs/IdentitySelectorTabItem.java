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
import cz.metacentrum.perun.webgui.json.usersManager.GetServiceUsersByUser;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;

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

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

        final TabItem tab = this;

        final FlexTable layout = new FlexTable();
        layout.setWidget(0, 0, new Image(LargeIcons.INSTANCE.userGrayIcon()));

        // always get user this person is logged as.
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
        layout.setWidget(0, 1, userName);
        layout.setHTML(1, 1, "Your base identity you are currently logged in.");
        layout.getFlexCellFormatter().setStyleName(1, 1, "inputFormInlineComment");

        if (session.getEditableUsers().size() > 1) {
            // user has service identities
            GetServiceUsersByUser call = new GetServiceUsersByUser(session.getUser().getId(), new JsonCallbackEvents(){
                @Override
                public void onFinished(JavaScriptObject jso) {
                    ArrayList<User> list = JsonUtils.jsoAsList(jso);
                    if (list != null && !list.isEmpty()) {
                        int row = 2;
                        for (User u : list) {
                            final User u2 = u;
                            layout.setWidget(row, 0, new Image(LargeIcons.INSTANCE.userRedIcon()));
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
                            layout.setWidget(row, 1, userName);
                            row++;
                        }
                    } else {
                        layout.setHTML(2, 1, "");
                    }
                }
                @Override
                public void onLoadingStart() {
                    layout.setWidget(2, 1, new AjaxLoaderImage().loadingStart());
                }
                @Override
                public void onError(PerunError error) {
                    layout.setWidget(2, 1, new AjaxLoaderImage().loadingError(error));
                }
            });
            call.retrieveData();
        }

        // fill page
        firstTabPanel.add(new HTML("<p>Select identity</p>"));
        firstTabPanel.getWidget(0).setStyleName("subsection-heading");
        firstTabPanel.add(layout);
        firstTabPanel.setCellHeight(layout, "100%");

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
		return SmallIcons.INSTANCE.userGrayIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
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