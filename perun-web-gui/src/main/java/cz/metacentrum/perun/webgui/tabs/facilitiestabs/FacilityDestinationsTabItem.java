package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

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
import cz.metacentrum.perun.webgui.json.servicesManager.GetAllRichDestinations;
import cz.metacentrum.perun.webgui.json.servicesManager.RemoveDestination;
import cz.metacentrum.perun.webgui.model.Destination;
import cz.metacentrum.perun.webgui.model.Facility;
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
 * Provides tab with destination management for selected Facility
 * FACILITY ADMIN
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityDestinationsTabItem implements TabItem, TabItemWithUrl{

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
	 * @param facility
	 */
	public FacilityDestinationsTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId
	 */
	public FacilityDestinationsTabItem(int facilityId){
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
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Destinations");

		// main content
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		final TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		//callback
		final GetAllRichDestinations callback = new GetAllRichDestinations(facility, null);
		final CellTable<Destination> table = callback.getTable(); // do not make callback yet

		// refresh table events
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(callback);

		// style table
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);

		menu.addWidget(UiElements.getRefreshButton(this));

		// buttons
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addDestination(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddFacilityDestinationTabItem(facility));
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedDestinations());
		menu.addWidget(removeButton);
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<Destination> destForRemoving = callback.getTableSelectedList();
				String text = "<span class=\"serverResponseLabelError\"><strong>Removing destination will stop propagation of service's configuration for this destination/service.</strong></span><p>Following destinations will be removed.";
				UiElements.showDeleteConfirm(destForRemoving, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<destForRemoving.size(); i++ ) {
							if (i == destForRemoving.size()-1) {
								RemoveDestination request = new RemoveDestination(facility.getId(), destForRemoving.get(i).getService().getId(), JsonCallbackEvents.disableButtonEvents(removeButton, events));
								request.removeDestination(destForRemoving.get(i).getDestination(), destForRemoving.get(i).getType());
							} else {
								RemoveDestination request = new RemoveDestination(facility.getId(), destForRemoving.get(i).getService().getId(), JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeDestination(destForRemoving.get(i).getDestination(), destForRemoving.get(i).getType());
							}
						}
					}
				});
			}
		});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, removeButton);

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(callback.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				callback.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterDestination());

		menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
		menu.addWidget(new HTML("<strong>Destinations define, where service's configuration is propagated.</strong>"));

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
		return SmallIcons.INSTANCE.serverGoIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 727;
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
		FacilityDestinationsTabItem other = (FacilityDestinationsTabItem) obj;
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
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Services destinations", getUrlWithParameters());
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

	public final static String URL = "destinations";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facility.getId();
	}

	static public FacilityDestinationsTabItem load(Facility facility)
	{
		return new FacilityDestinationsTabItem(facility);
	}

	static public FacilityDestinationsTabItem load(Map<String, String> parameters)
	{
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityDestinationsTabItem(fid);
	}

}
