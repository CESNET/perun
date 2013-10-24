package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationsForUser;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * User's applications
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class SelfApplicationsTabItem implements TabItem, TabItemWithUrl{

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

	private User user;
	private int userId = 0;

	/**
	 * Creates a tab instance
     */
	public SelfApplicationsTabItem(){
		this.user = session.getActiveUser();
		this.userId = user.getId();
	}
	
	/**
	 * Creates a tab instance with custom user
     * @param user
     */
	public SelfApplicationsTabItem(User user){
		this.user = user;
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
     * @param userId
     */
    public SelfApplicationsTabItem(int userId) {
        this.userId = userId;
        new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                user = jso.cast();
            }
        }).retrieveData();
    }

	public boolean isPrepared(){
		return !(user == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(user.getFullNameWithTitles().trim())+ ": Applications");

		VerticalPanel bodyContents = new VerticalPanel();
		bodyContents.setWidth("100%");

		// callback
		final GetApplicationsForUser req = new GetApplicationsForUser(user.getId());
		
		req.setCheckable(false);
		
		// tab menu for filtering
		TabMenu tabMenu = new TabMenu();
		tabMenu.addFilterWidget(new SuggestBox(req.getOracle()), new PerunSearchEvent() {
            @Override
            public void searchFor(String text) {
                req.filterTable(text);
            }
        }, ButtonTranslation.INSTANCE.filterByVoOrGroup());

		bodyContents.add(tabMenu);
		
		CellTable<Application> appsTable = req.getTable(new FieldUpdater<Application, String>() {
            @Override
			public void update(int index, Application object, String value) {
				session.getTabManager().addTab(new SelfApplicationDetailTabItem(object));
			}
		});
		appsTable.addStyleName("perun-table");
		
		bodyContents.add(appsTable);

		ScrollPanel sp = new ScrollPanel(bodyContents);
		sp.addStyleName("perun-tableScrollPanel");		
		session.getUiElements().resizePerunTable(sp, 350, this);
		
		this.contentWidget.setWidget(sp);
		return getWidget();

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.applicationFromStorageIcon(); 
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 43;
		result = prime * result * userId;
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
		if (this.userId != ((SelfApplicationsTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
        session.setActiveUser(user);
        session.getUiElements().getMenu().openMenu(MainMenu.USER);
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "My applications", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true; 
		} else {
			return false;
		}

	}

	public final static String URL = "appls";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
	}

	static public SelfApplicationsTabItem load(Map<String, String> parameters) {

		if (parameters.containsKey("id")) {
			int uid = Integer.parseInt(parameters.get("id"));
			if (uid != 0) {
				return new SelfApplicationsTabItem(uid);
			}
		}
		return new SelfApplicationsTabItem();
	}

}