package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

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
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.propagationStatsReader.ListAllRichTasksForFacility;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Task;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.Map;

/**
 * Shows facility status
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class FacilityStatusTabItem implements TabItem, TabItemWithUrl{

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
	 * Creates a tab instance
     * @param facility facility to get services and status from
     */
	public FacilityStatusTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param facilityId
     */
	public FacilityStatusTabItem(int facilityId){
		this.facilityId = facilityId;
        new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                facility = jso.cast();
            }
        }).retrieveData();
	}
	

	public boolean isPrepared(){
		return !(facility == null);
	}
	
	public Widget draw() {

		// title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+" ("+facility.getType()+"): Propagation status");
		
		// main content
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// get empty table
		final ListAllRichTasksForFacility callback = new ListAllRichTasksForFacility(facility.getId());
        callback.setCheckable(false);

        final CustomButton refreshButton = TabMenu.getPredefinedButton(ButtonType.REFRESH, ButtonTranslation.INSTANCE.refreshPropagationResults(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                callback.clearTable();
                callback.retrieveData();
            }
        });

        callback.setEvents(JsonCallbackEvents.disableButtonEvents(refreshButton));

        final CellTable<Task> table = callback.getTable(new FieldUpdater<Task, String>(){
			// on row click
			public void update(int index, final Task object, String value) {
				// show results
				session.getTabManager().addTab(new TaskResultsTabItem(object));
			}
		});

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");		
		
		TabMenu menu = new TabMenu();
		menu.addWidget(refreshButton);
		
		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);
		
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
		return SmallIcons.INSTANCE.serverInformationIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 41;
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
		FacilityStatusTabItem other = (FacilityStatusTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}
	
	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(facility, "Propagation status", getUrlWithParameters());
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
	
	public final static String URL = "propagation";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facility.getId();
	}
	
	static public FacilityStatusTabItem load(Facility facility) {
		return new FacilityStatusTabItem(facility);
	}
	
	static public FacilityStatusTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityStatusTabItem(fid);
	}
	
}