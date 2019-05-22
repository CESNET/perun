package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.AssignService;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedServices;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServices;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Provides page with assign service to resource form
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AssignServiceTabItem implements TabItem {

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
	private Label titleWidget = new Label("Assign service to resource");

	//data
	private int resourceId;
	private Resource resource;

	/**
	 * @param resourceId ID of resource to have group assigned
	 */
	public AssignServiceTabItem(int resourceId){
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 * @param resource resource
	 */
	public AssignServiceTabItem(Resource resource){
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

		titleWidget.setText("Assign service");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();

		final GetServices services = new GetServices();

		final CellTable<Service> table = services.getEmptyTable(new FieldUpdater<Service, String>() {
			public void update(int index, Service object, String value) {
				session.getTabManager().addTab(new ResourceSettingsTabItem(resource, object));
			}
		});

		// remove already assigned services from offering
		JsonCallbackEvents localEvents = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso){
				// second callback
				final GetAssignedServices alreadyAssigned = new GetAssignedServices(resourceId, new JsonCallbackEvents() {
					public void onFinished(JavaScriptObject jso){
						JsArray<Service> srvToRemove = JsonUtils.jsoAsArray(jso);
						for (int i=0; i<srvToRemove.length(); i++) {
							services.removeFromTable(srvToRemove.get(i));
						}
						if (services.getList().size() == 1) {
							table.getSelectionModel().setSelected(services.getList().get(0), true);
						}
					}
				});
				alreadyAssigned.retrieveData();
			}
		};
		services.setEvents(localEvents);


		final TabItem tab = this;

		// button
		final CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.assignSelectedServicesToResource());
		assignButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<Service> servicesToAssign = services.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(servicesToAssign)) {
					for (int i=0; i<servicesToAssign.size(); i++ ) {
						if (i != servicesToAssign.size()-1) {	                 // call json normaly
							AssignService request = new AssignService(JsonCallbackEvents.disableButtonEvents(assignButton));
							request.assignService(servicesToAssign.get(i).getId(), resourceId);
						} else {                                                // last change - call json with update
							AssignService request = new AssignService(JsonCallbackEvents.closeTabDisableButtonEvents(assignButton, tab));
							request.assignService(servicesToAssign.get(i).getId(), resourceId);
						}
					}
				}
			}
		});

		menu.addWidget(assignButton);
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		menu.addFilterWidget(new ExtendedSuggestBox(services.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				services.filterTable(text);
				if (services.getList().size() == 1) {
					table.getSelectionModel().setSelected(services.getList().get(0), true);
				}
			}
		}, "Filter services by name");

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		services.retrieveData();

		assignButton.setEnabled(false);
		JsonUtils.addTableManagedButton(services, table, assignButton);

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);

		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

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
		return SmallIcons.INSTANCE.trafficLightsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1499;
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
		AssignServiceTabItem other = (AssignServiceTabItem) obj;
		if (resourceId != other.resourceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(resource.getFacilityId())) {
			return true;
		} else {
			return false;
		}

	}

}
