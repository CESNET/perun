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
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetAssignedRichResources;
import cz.metacentrum.perun.webgui.json.resourcesManager.DeleteResource;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.CreateFacilityResourceTabItem;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 *  Tab for listing facility resources
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityResourcesTabItem implements TabItem, TabItemWithUrl{

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
	 * @param facility facility to get resources from
	 */
	public FacilityResourcesTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId
	 */
	public FacilityResourcesTabItem(int facilityId){
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

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// set title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Resources");

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		// get VO resources
		final GetAssignedRichResources resources = new GetAssignedRichResources(facility.getId());

		// custom events for viewResource
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(resources);

		// create resource button
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createResource(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateFacilityResourceTabItem(facility), false);
			}
		}));

		final CustomButton deleteButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteResource());
		menu.addWidget(deleteButton);
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				final ArrayList<RichResource> toDelete = resources.getTableSelectedList();
				String text = "Following resources will be deleted.";
				UiElements.showDeleteConfirm(toDelete, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<toDelete.size(); i++ ) {
							if (i == toDelete.size()-1) {
								DeleteResource request = new DeleteResource(JsonCallbackEvents.disableButtonEvents(deleteButton, events));
								request.deleteResource(toDelete.get(i).getId());
							} else {
								DeleteResource request = new DeleteResource(JsonCallbackEvents.disableButtonEvents(deleteButton));
								request.deleteResource(toDelete.get(i).getId());
							}
						}
					}
				});

			}
		});

		menu.addFilterWidget(new ExtendedSuggestBox(resources.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				resources.filterTable(text);
			}
		}, "Filter resources by name or VO name");

		CellTable<RichResource> table = resources.getTable(new FieldUpdater<RichResource, String>() {
			public void update(int index, RichResource object, String value) {
				session.getTabManager().addTab(new ResourceDetailTabItem(object, facilityId));
			}
		});

		deleteButton.setEnabled(false);
		JsonUtils.addTableManagedButton(resources, table, deleteButton);

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);

		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.add(sp);

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
		return SmallIcons.INSTANCE.serverGroupIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 761;
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
		FacilityResourcesTabItem other = (FacilityResourcesTabItem)obj;
		if (this.facilityId != other.facilityId) {
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Resources", getUrlWithParameters());
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

	public final static String URL = "resources";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facility.getId();
	}

	static public FacilityResourcesTabItem load(Facility facility) {
		return new FacilityResourcesTabItem(facility);
	}

	static public FacilityResourcesTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityResourcesTabItem(fid);
	}

}
