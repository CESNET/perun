package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationDataById;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;

import java.util.Map;

/**
 * User's application detail
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */

public class SelfApplicationDetailTabItem implements TabItem, TabItemWithUrl{

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
    private Label titleWidget = new Label("Loading application");

    private Application application;
    private int applicationId = 0;


    /**
     * Creates a tab instance
     * @param application
     */
    public SelfApplicationDetailTabItem(Application application){
        this.application = application;
        this.applicationId = application.getId();
    }

    /**
     * Creates a tab instance
     * @param applicationId
     */
    public SelfApplicationDetailTabItem(int applicationId) {
        this.applicationId = applicationId;
        new GetEntityById(PerunEntity.APPLICATION, applicationId, new JsonCallbackEvents() {
            public void onFinished(JavaScriptObject jso) {
                application = jso.cast();
            }
        }).retrieveData();
    }

    public boolean isPrepared(){
        return !(application == null);
    }

    public Widget draw() {

        this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(application.getVo().getName()) + " application");
        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");

        vp.add(new HTML("<h3>Application</h3>"));
        // app info
        FlexTable ft = new FlexTable();
        ft.setCellPadding(10);
        int i = 0;

        ft.setHTML(i, 0, "<strong>" + "Submitted" + "</strong>");
        ft.setHTML(i, 1, application.getCreatedAt());
        i++;


        ft.setHTML(i, 0, "<strong>" + "Type" + "</strong>");
        ft.setHTML(i, 1, application.getType());
        i++;


        ft.setHTML(i, 0, "<strong>" + "State" + "</strong>");
        ft.setHTML(i, 1, application.getState());

        vp.add(ft);

        //data
        vp.add(new HTML("<h3>Contents</h3>"));
        GetApplicationDataById data = new GetApplicationDataById(applicationId);
        data.setShowAdminItems(false);
        data.retrieveData();
        vp.add(data.getContents());

        // wrap
        ScrollPanel sp = new ScrollPanel(vp);
        sp.setSize("100%", "100%");
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
        result = prime * result * applicationId;
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
        if (this.applicationId != ((SelfApplicationDetailTabItem)obj).applicationId)
            return false;

        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open() {
        session.getUiElements().getMenu().openMenu(MainMenu.USER);
        // FIXME - there currently isn't way to get to application detail without beeing a user in perun,
        // so getting user from application should be safe.
        session.getUiElements().getBreadcrumbs().setLocation(MainMenu.USER, "My applications", UsersTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"appls?id="+application.getUser().getId(), "Application detail", getUrlWithParameters());
    }

    public boolean isAuthorized() {

        if (application.getUser() != null) {
            // user known
            if (session.isSelf(application.getUser().getId())) {
                return true;
            }
        } else {
            // user unknown
            if (session.isPerunAdmin()) {
                return true;
            }
        }
        return false;

    }


    public final static String URL = "appl-detail";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters()
    {
        return  UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + applicationId;
    }

    static public SelfApplicationDetailTabItem load(Map<String, String> parameters) {

        if (parameters.containsKey("id")) {
            int appid = Integer.parseInt(parameters.get("id"));
            if (appid != 0) {
                return new SelfApplicationDetailTabItem(appid);
            }
        }
        return null;
    }

}