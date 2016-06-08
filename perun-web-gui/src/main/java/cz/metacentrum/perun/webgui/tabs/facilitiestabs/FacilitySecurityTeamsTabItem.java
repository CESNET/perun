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
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetAssignedSecurityTeams;
import cz.metacentrum.perun.webgui.json.facilitiesManager.RemoveSecurityTeam;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page security teams settings
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilitySecurityTeamsTabItem implements TabItem, TabItemWithUrl {

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
	public FacilitySecurityTeamsTabItem(int facilityId){
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
	public FacilitySecurityTeamsTabItem(Facility facility){
		this.facilityId = facility.getId();
		this.facility = facility;
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Security teams");

		// main panel
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%","100%");

		final GetAssignedSecurityTeams securityTeams = new GetAssignedSecurityTeams(facilityId);

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.assignSecurityTeam(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AssignSecurityTeamTabItem(facility), true);
			}
		}));

		// remove button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedSecurityTeams());
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<SecurityTeam> list = securityTeams.getTableSelectedList();
				UiElements.showDeleteConfirm(list, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < list.size(); i++) {
							if (i == list.size() - 1) {
								RemoveSecurityTeam request = new RemoveSecurityTeam(JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(securityTeams)));
								request.removeSecurityTeam(facilityId, list.get(i).getId());
							} else {
								RemoveSecurityTeam request = new RemoveSecurityTeam(JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeSecurityTeam(facilityId, list.get(i).getId());
							}
						}
					}
				});
			}
		});
		menu.addWidget(removeButton);

		menu.addFilterWidget(new ExtendedSuggestBox(securityTeams.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				securityTeams.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterSecurityTeam());

		CellTable<SecurityTeam> table;
		if (session.isPerunAdmin() || session.isSecurityAdmin()) {
			table = securityTeams.getTable(new FieldUpdater<SecurityTeam, String>() {
				@Override
				public void update(int index, SecurityTeam object, String value) {
					session.getTabManager().addTab(new SecurityTeamDetailTabItem(object));
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
		return  SmallIcons.INSTANCE.serverIcon();
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
		FacilitySecurityTeamsTabItem other = (FacilitySecurityTeamsTabItem) obj;
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

	public final static String URL = "sec";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}

	static public FacilitySecurityTeamsTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilitySecurityTeamsTabItem(fid);
	}

}
