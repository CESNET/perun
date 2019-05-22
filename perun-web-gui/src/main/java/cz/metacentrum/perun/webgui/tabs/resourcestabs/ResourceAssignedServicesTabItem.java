package cz.metacentrum.perun.webgui.tabs.resourcestabs;

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
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedServices;
import cz.metacentrum.perun.webgui.json.resourcesManager.RemoveService;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.ResourcesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.servicestabs.ServiceDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page with service on resources managements (assign / remove)
 * Used by FACILITY administrators
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ResourceAssignedServicesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Manage assigned services");

	// data
	private int resourceId;
	private Resource resource;

	/**
	 * Creates a tab instance
	 * @param resourceId ID of resource to get services management for
	 */
	public ResourceAssignedServicesTabItem(int resourceId){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 * @param resource resource to get services management for
	 */
	public ResourceAssignedServicesTabItem(Resource resource){
		this.resource = resource;
		this.resourceId = resource.getId();
	}

	public boolean isPrepared(){
		return !(resource == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()) + ": manage assigned services");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		final GetAssignedServices resourceServices = new GetAssignedServices(resourceId);

		final JsonCallbackEvents localEvents = JsonCallbackEvents.refreshTableEvents(resourceServices);

		CustomButton assignServicesButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.assignServiceToResource(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AssignServiceTabItem(resourceId), true);
			}
		});

		final CustomButton removeServicesButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeServiceFromResource());
		removeServicesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Service> servicesForRemoving = resourceServices.getTableSelectedList();
				String text = "Following services will be removed from resource.";
				UiElements.showDeleteConfirm(servicesForRemoving, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD USE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < servicesForRemoving.size(); i++) {
							if (i == servicesForRemoving.size()-1) {
								RemoveService request = new RemoveService(JsonCallbackEvents.disableButtonEvents(removeServicesButton, localEvents));
								request.removeService(servicesForRemoving.get(i).getId(), resourceId);
							} else {
								RemoveService request = new RemoveService(JsonCallbackEvents.disableButtonEvents(removeServicesButton));
								request.removeService(servicesForRemoving.get(i).getId(), resourceId);
							}
						}
					}
				});
			}
		});

		menu.addWidget(assignServicesButton);
		menu.addWidget(removeServicesButton);

		menu.addFilterWidget(new ExtendedSuggestBox(resourceServices.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				resourceServices.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterServices());

		// display menu only to facility admin
		if (session.isFacilityAdmin(resource.getFacilityId())) {
			resourceServices.setCheckable(true);
			vp.add(menu);
			vp.setCellHeight(menu,"30px");
		} else {
			resourceServices.setCheckable(false);
		}

		CellTable<Service> table;
		if (session.isPerunAdmin()) {
			table = resourceServices.getTable(new FieldUpdater<Service, String>(){
				public void update(int index, Service object, String value) {
					// load detail only for perun admin
					session.getTabManager().addTab(new ServiceDetailTabItem(object));
				}
			});
		} else {
			// not clickable
			table = resourceServices.getTable();
		}

		removeServicesButton.setEnabled(false);
		JsonUtils.addTableManagedButton(resourceServices, table, removeServicesButton);


		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);

		session.getUiElements().resizePerunTable(sp, 400, this);

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
		final int prime = 1511;
		int result = 1;
		result = prime * result + resourceId;
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
		ResourceAssignedServicesTabItem other = (ResourceAssignedServicesTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.setActiveFacilityId(resource.getFacilityId());
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(resource.getFacilityId())) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "manage-services";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return ResourcesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + resourceId;
	}

	static public ResourceAssignedServicesTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		return new ResourceAssignedServicesTabItem(id);
	}

	static public ResourceAssignedServicesTabItem load(Resource resource) {
		return new ResourceAssignedServicesTabItem(resource);
	}

}
