package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Page with Users for perun admin but in "User section"
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class IdentitySelectorTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Identity selector");

	// users query
	private FindCompleteRichUsers users;

	/**
	 * Search string
	 */
	private String searchString = "";

	/**
	 * Creates a tab instance
     */
	public IdentitySelectorTabItem(){}
	
	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {
		
		this.users = new FindCompleteRichUsers("", null);
		users.setCheckable(false);

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu tabMenu = new TabMenu();

		// this tab to close
		final IdentitySelectorTabItem tab = this;

		// get the table
		final CellTable<User> table = users.getTable(new FieldUpdater<User, String>() {
			public void update(int index, User object, String value) {
				
				 // save user into session
				session.getPerunPrincipal().setUser(object);
				// notify
				session.getUiElements().setLogSuccessText("User identity loaded: " + object.getFullNameWithTitles());
				// open selected user detail
				session.getTabManager().addTab(new SelfDetailTabItem(object));
				// close this tab
				session.getTabManager().closeTab(tab);

			}
		});

		// search textbox
		TextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
            @Override
            public void searchFor(String text) {
                startSearching(text);
                searchString = text;
            }
        }, ButtonTranslation.INSTANCE.searchUsers());

		// if some text has been searched before
		if(!searchString.equals(""))
		{
			searchBox.setText(searchString);
			startSearching(searchString);
		}

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");
		firstTabPanel.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(firstTabPanel);
		
		return getWidget();
	}

	/**
	 * Starts the search for users
	 */
	protected void startSearching(String text){
		users.searchFor(text);	
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

	/**
	 * @param obj
	 */
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

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.USER, true);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "Identity selector", getUrlWithParameters());
	}
	
	public boolean isAuthorized() {

		if (session.isPerunAdmin()) { 
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
	
	public String getUrlWithParameters()
	{
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}
	
	static public IdentitySelectorTabItem load(Map<String, String> parameters)
	{
		return new IdentitySelectorTabItem();
	}

}