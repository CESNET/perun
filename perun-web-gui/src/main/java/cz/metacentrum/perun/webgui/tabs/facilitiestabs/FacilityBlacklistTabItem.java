package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.securityTeamsManager.GetBlacklistWithDescription;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Pair;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Provides page with list of blacklisted users
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityBlacklistTabItem implements TabItem, TabItemWithUrl {

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
	private int facilityId = 0;
	private Facility facility;

	/**
	 * Creates a tab instance
	 * @param facilityId
	 */
	public FacilityBlacklistTabItem(int facilityId){
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

	/**
	 * Creates a tab instance
	 * @param facility
	 */
	public FacilityBlacklistTabItem(Facility facility){
		this.facilityId = facility.getId();
		this.facility = facility;
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Blacklist");

		// main panel
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%","100%");

		final GetBlacklistWithDescription securityTeams = new GetBlacklistWithDescription(PerunEntity.FACILITY, facilityId);

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		menu.addFilterWidget(new ExtendedSuggestBox(securityTeams.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				securityTeams.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterSecurityTeam());

		CellTable<Pair<User,String>> table;
		if (session.isPerunAdmin() || session.isSecurityAdmin()) {
			table = securityTeams.getTable(new FieldUpdater<Pair<User,String>, String>() {
				@Override
				public void update(int index, Pair<User,String> object, String value) {
					session.getTabManager().addTab(new UserDetailTabItem(object.getLeft()));
				}
			});
		} else {
			table = securityTeams.getTable();
		}

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add menu and the table to the main panel
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");
		firstTabPanel.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

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
		return  SmallIcons.INSTANCE.firewallIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 739;
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
		FacilityBlacklistTabItem other = (FacilityBlacklistTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Security teams", getUrlWithParameters());
		if(facility != null) {
			session.setActiveFacility(facility);
		} else {
			session.setActiveFacilityId(facilityId);
		}
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facilityId)) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "black";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}

	static public FacilityBlacklistTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityBlacklistTabItem(fid);
	}

}
