package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.GetRichAdminsWithAttributes;
import cz.metacentrum.perun.webgui.json.authzResolver.RemoveAdmin;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab with list of administrators on facility
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class FacilityManagersTabItem implements TabItem, TabItemWithUrl{

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
	private int facilityId;
	private Facility facility;
	
	/**
	 * Creates a tab instance
	 *
     * @param facility Facility
     */
	public FacilityManagersTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param facilityId ID of facility
     */
	public FacilityManagersTabItem(int facilityId){
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
		
		// set title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+" ("+facility.getType()+"): Managers");

		// content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// Menu
		TabMenu menu = new TabMenu();
		
		// get the table
		final GetRichAdminsWithAttributes jsonCallback = new GetRichAdminsWithAttributes(PerunEntity.FACILITY, facilityId, null);
		CellTable<User> table;
        if (session.isPerunAdmin()) {
            table = jsonCallback.getTable(new FieldUpdater<User, String>() {
                @Override
                public void update(int i, User user, String s) {
                    session.getTabManager().addTab(new UserDetailTabItem(user));
                }
            });
        } else {
            table = jsonCallback.getTable();
        }

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addManagerToFacility(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                session.getTabManager().addTabToCurrentTab(new AddFacilityManagerTabItem(facility), true);
            }
        }));
		
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerFromFacility());
		menu.addWidget(removeButton);
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<User> list = jsonCallback.getTableSelectedList();
                String text = "Following users won't be facility managers anymore and won't be able to manage this facility in Perun.";
                UiElements.showDeleteConfirm(list, text, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                        for (int i=0; i<list.size(); i++) {
                            if (i == list.size()-1) {
                                RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
                                request.removeAdmin(facilityId, list.get(i).getId());
                            } else {
                                RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton));
                                request.removeAdmin(facilityId, list.get(i).getId());
                            }
                        }
                    }
                });
			}
		});

        menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
        menu.addWidget(new HTML("<strong>People with privilege to manage this facility in Perun. They aren't automatically \"roots\" on machine.</strong>"));

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

        removeButton.setEnabled(false);
        JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");		

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
		return SmallIcons.INSTANCE.administratorIcon(); 
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
        FacilityManagersTabItem other = (FacilityManagersTabItem) obj;
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
        session.getUiElements().getBreadcrumbs().setLocation(facility, "Managers", getUrlWithParameters());
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
	
	public final static String URL = "managers";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}
	
	static public FacilityManagersTabItem load(Facility facility)
	{
		return new FacilityManagersTabItem(facility);
	}
	
	static public FacilityManagersTabItem load(Map<String, String> parameters)
	{
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityManagersTabItem(fid);
	}
	
}