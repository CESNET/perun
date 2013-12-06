package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.Map;

/**
 * Displays a facility
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 1e3169b84c18f7c8da4a14adac69d26925d8da05 $
 */
public class FacilityDetailTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading facility");

	// data
	private Facility facility;
	private int facilityId;

	/**
	 * Small tab panel
	 */
	private TabPanelForTabItems tabPanel;

	/**
	 * Creates a tab instance
     * @param facility
     */
	public FacilityDetailTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
		this.tabPanel = new TabPanelForTabItems(this);
	}
	
	/**
	 * Creates a tab instance
     * @param facility
     * @param tabPositionToOpen
     */
	public FacilityDetailTabItem(Facility facility, int tabPositionToOpen){
		this(facility);
		tabPanel.setLastTabId(tabPositionToOpen);
		
	}
	
	/**
	 * Creates a tab instance
     * @param facilityId
     */
	public FacilityDetailTabItem(int facilityId){
		this.facilityId = facilityId;
        new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                facility = jso.cast();
            }
        }).retrieveData();
		this.tabPanel = new TabPanelForTabItems(this);
	}
	
	public boolean isPrepared(){
		return !(facility == null);
	}

	public Widget draw() {

        this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName()) + " (" + facility.getType() + ")");
		
		// main widget panel
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

        AbsolutePanel dp = new AbsolutePanel();
        final FlexTable menu = new FlexTable();
        menu.setCellSpacing(5);

        // Add facility information
        menu.setWidget(0, 0, new Image(LargeIcons.INSTANCE.databaseServerIcon()));
        Label facilityName = new Label();
        facilityName.setText(Utils.getStrippedStringWithEllipsis(facility.getName(), 40));
        facilityName.setStyleName("now-managing");
        facilityName.setTitle(facility.getName());
        menu.setWidget(0, 1, facilityName);

        menu.setHTML(0, 2, "&nbsp;");
        menu.getFlexCellFormatter().setWidth(0, 2, "25px");

        int column = 3;
        if (JsonUtils.isExtendedInfoVisible()) {
            menu.setHTML(0, column, "<strong>ID:</strong><br/><span class=\"inputFormInlineComment\">"+facility.getId()+"</span>");
            column++;
            menu.setHTML(0, column, "&nbsp;");
            menu.getFlexCellFormatter().setWidth(0, column, "25px");
            column++;
        }

        menu.setHTML(0, column, "<strong>Type:</strong><br/><span class=\"inputFormInlineComment\">"+facility.getType()+"</span>");

        CustomButton cb = new CustomButton("", "Refresh page content", SmallIcons.INSTANCE.updateIcon(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                tabPanel.getSelectedTabItem().draw();
            }
        });
        dp.add(cb);
        cb.getElement().setAttribute("style", "position: absolute; right: 5px; top: 5px;");


        /* uncomment for editing facility name
        also change other button right position from 5 to 50.

        final JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
                    public void onFinished(JavaScriptObject jso){
                        facility = jso.cast();
                        draw();
                    }
                }).retrieveData();
            }
        };

        CustomButton change = new CustomButton("", ButtonTranslation.INSTANCE.editFacilityDetails(), SmallIcons.INSTANCE.applicationFormEditIcon());
        change.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                // prepare confirm content
                session.getTabManager().addTabToCurrentTab(new EditFacilityDetailsTabItem(facility, events));
            }
        });
        dp.add(change);
        change.getElement().setAttribute("style", "position: absolute; right: 5px; top: 5px;");
        */

        dp.add(menu);
        vp.add(dp);
        vp.setCellHeight(dp, "30px");

		// TAB PANEL
		
		tabPanel.clear();
        tabPanel.add(new FacilityResourcesTabItem(facility), "Resources");
        tabPanel.add(new FacilityAllowedGroupsTabItem(facility), "Allowed Groups");
        tabPanel.add(new FacilityStatusTabItem(facility), "Propagation status");
        tabPanel.add(new FacilityPropagationTabItem(facility, tabPanel), "Services propagation");
        tabPanel.add(new FacilitySettingsTabItem(facility), "Services settings");
        tabPanel.add(new FacilityDestinationsTabItem(facility), "Services destinations");
        tabPanel.add(new FacilityHostsTabItem(facility), "Hosts");
        tabPanel.add(new FacilityManagersTabItem(facility), "Managers");
        tabPanel.add(new FacilityOwnersTabItem(facility), "Owners");

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
		return SmallIcons.INSTANCE.databaseServerIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + facilityId;
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
		FacilityDetailTabItem other = (FacilityDetailTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(facility, "", "");
		if(facility != null) {
			session.setActiveFacility(facility);
		} else {
			session.setActiveFacilityId(facilityId);
		}
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facility.getId())) {
			return true; 
		} else {
			return false;
		}

	}

	public final static String URL = "detail";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}
	
	static public FacilityDetailTabItem load(Map<String, String> parameters)
	{
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityDetailTabItem(fid);
	}
	
	static public FacilityDetailTabItem load(Facility fac)
	{
		return new FacilityDetailTabItem(fac);
	}
}