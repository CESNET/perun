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
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetHosts;
import cz.metacentrum.perun.webgui.json.facilitiesManager.RemoveHosts;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Host;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page with hosts management for cluster
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class FacilityHostsTabItem implements TabItem, TabItemWithUrl{

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
	 * @param facilityId
	 */
	public FacilityHostsTabItem(int facilityId){
		this.facilityId = facilityId;
		this.titleWidget.setText("Hosts: "+facilityId);
		new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				facility = jso.cast();
			}
		}).retrieveData();
	}


	public boolean isPrepared(){
		return !(facility == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	/**
	 * Creates a tab instance
	 * @param facility
	 */
	public FacilityHostsTabItem(Facility facility){
		this.facilityId = facility.getId();
		this.facility = facility;
	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Hosts");

		// main panel
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%","100%");

		final GetHosts hosts = new GetHosts(facilityId);
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(hosts);

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addHost(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new AddHostsTabItem(facility));
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeHosts());
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Host> hostsForRemoving = hosts.getTableSelectedList();
				String text = "<span class=\"serverResponseLabelError\"><strong>Removing host(s) won't stop services propagation. For this please remove proper 'Services destinations'.</strong></span><p>Following hosts will be removed from facility.";
				UiElements.showDeleteConfirm(hostsForRemoving, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < hostsForRemoving.size(); i++) {
							if (i == hostsForRemoving.size()-1) {
								RemoveHosts request = new RemoveHosts(facilityId, JsonCallbackEvents.disableButtonEvents(removeButton, events));
								request.removeHost(hostsForRemoving.get(i).getId());
							} else {
								RemoveHosts request = new RemoveHosts(facilityId, JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeHost(hostsForRemoving.get(i).getId());
							}
						}
					}});
			}
		});
		menu.addWidget(removeButton);

		menu.addFilterWidget(new ExtendedSuggestBox(hosts.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				hosts.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterHosts());

		// Hosts table
		CellTable<Host> table = hosts.getTable(new FieldUpdater<Host, String>() {
			@Override
			public void update(int index, Host object, String value) {
				session.getTabManager().addTab(new FacilityHostsSettingsTabItem(facility, object));
			}
		});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(hosts, table, removeButton);

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
		final int prime = 743;
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
		FacilityHostsTabItem other = (FacilityHostsTabItem) obj;
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
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Hosts", getUrlWithParameters());
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

	public final static String URL = "hosts";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}

	static public FacilityHostsTabItem load(Map<String, String> parameters)
	{
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityHostsTabItem(fid);
	}

	static public FacilityHostsTabItem load(Facility fac)
	{
		return new FacilityHostsTabItem(fac);
	}
}
