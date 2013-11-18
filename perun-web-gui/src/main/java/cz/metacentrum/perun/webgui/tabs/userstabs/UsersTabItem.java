package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.json.usersManager.GetCompleteRichUsers;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Page with Users for Perun Admin
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class UsersTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Users");

	// users query
	private FindCompleteRichUsers users;

	/**
	 * Search string
	 */
	private String searchString = "";

	/**
	 * Creates a tab instance
     */
	public UsersTabItem(){ }
	
	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {


        CustomButton searchButton = new CustomButton("Search", ButtonTranslation.INSTANCE.searchUsers(), SmallIcons.INSTANCE.findIcon());

		this.users = new FindCompleteRichUsers("", JsonCallbackEvents.disableButtonEvents(searchButton),null);

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

        // HORIZONTAL MENU
        TabMenu tabMenu = new TabMenu();

        // search textbox
        TextBox searchBox = tabMenu.addSearchWidget(new PerunSearchEvent() {
            @Override
            public void searchFor(String text) {
                startSearching(text);
                searchString = text;
            }
        }, searchButton);

		// get the table
		final CellTable<User> table = users.getTable(new FieldUpdater<User, String>() {
			public void update(int index, User object, String value) {
				// opens the tab
				session.getTabManager().addTab(new UserDetailTabItem(object));
			}
		});


		// if some text has been searched before
		if(!searchString.equals(""))
		{
			searchBox.setText(searchString);
			startSearching(searchString);
		}

        /*
		Button b1 = tabMenu.addButton("List all", SmallIcons.INSTANCE.userGrayIcon(), new ClickHandler(){
			public void onClick(ClickEvent event) {
				GetCompleteRichUsers callback = new GetCompleteRichUsers(new JsonCallbackEvents(){
					public void onLoadingStart() {
						table.setEmptyTableWidget(new AjaxLoaderImage().loadingStart());
					}
					public void onFinished(JavaScriptObject jso) {
						users.setList(JsonUtils.<User>jsoAsList(jso));
						users.sortTable();
                        table.setEmptyTableWidget(new AjaxLoaderImage().loadingFinished());
					}
                    public void onError(PerunError error){
                        users.clearTable();
                        table.setEmptyTableWidget(new AjaxLoaderImage().loadingError(error));
                    }
				});
				callback.retrieveData();
			}
		});
		b1.setTitle("List of all users in Perun");
        */

		final CustomButton withoutVoButton = new CustomButton(ButtonTranslation.INSTANCE.listUsersWithoutVoButton(), ButtonTranslation.INSTANCE.listUsersWithoutVo(), SmallIcons.INSTANCE.userRedIcon());
        withoutVoButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				GetCompleteRichUsers callback = new GetCompleteRichUsers(null, new JsonCallbackEvents(){
					public void onLoadingStart() {
                        users.clearTable();
						table.setEmptyTableWidget(new AjaxLoaderImage().loadingStart());
                        withoutVoButton.setProcessing(true);
					}
					public void onFinished(JavaScriptObject jso) {
                        users.setList(JsonUtils.<User>jsoAsList(jso));
                        users.sortTable();
                        table.setEmptyTableWidget(new AjaxLoaderImage().loadingFinished());
                        withoutVoButton.setProcessing(false);
					}
                    public void onError(PerunError error){
                        table.setEmptyTableWidget(new AjaxLoaderImage().loadingError(error));
                        withoutVoButton.setProcessing(false);
                    }
				});
                callback.getWithoutVo(true);
				callback.retrieveData();
			}
		});
        tabMenu.addWidget(withoutVoButton);

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
        session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Users", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) { 
			return true; 
		} else {
			return false;
		}

	}
	
	public final static String URL = "users";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}
	
	static public UsersTabItem load(Map<String, String> parameters)
	{
		return new UsersTabItem();
	}

}