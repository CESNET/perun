package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.AssignService;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedServices;
import cz.metacentrum.perun.webgui.json.resourcesManager.RemoveService;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServices;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * FACILITY ADMINISTRATOR - create Resource wizard - page 2
 * Assign services to new resource
 *
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateFacilityResourceManageServicesTabItem implements TabItem {

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
	private Label titleWidget = new Label("Manage services");

	//data
	private Facility facility;
	private Resource resource;
	private int resourceId;
	private int facilityId;

	/**
	 * Create new instance
	 *
	 * @param facility facility to have resource added
	 * @param resource created resource definition
	 */
	public CreateFacilityResourceManageServicesTabItem(Facility facility, Resource resource){
		this.facility = facility;
		this.resource = resource;
		this.facilityId = facility.getId();
		this.resourceId = resource.getId();
	}

	/**
	 * Create new instance
	 *
	 * @param facilityId facility to have resource added
	 * @param resourceId created resource definition
	 */
	public CreateFacilityResourceManageServicesTabItem(final int facilityId, final int resourceId){
		this.facilityId = facilityId;
		this.resourceId = resourceId;
		new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				resource = jso.cast();
				new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
					public void onFinished(JavaScriptObject jso){
						facility = jso.cast();
					}
				}).retrieveData();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return (facility != null && resource != null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()) + ": assign and configure services");

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		// buttons
		final CustomButton assignButton = new CustomButton("Assign selected", SmallIcons.INSTANCE.addIcon());
		assignButton.setTitle("Assign selected service to resource (if already assigned - nothing happens)");
		final CustomButton removeButton = new CustomButton("Remove selected", SmallIcons.INSTANCE.deleteIcon());
		removeButton.setTitle("Remove selected service from resource (if already removed - nothing happens)");
		CustomButton finishButton = new CustomButton("Finish assigning", SmallIcons.INSTANCE.acceptIcon());
		finishButton.setTitle("Finishes services assigning and configuration");
		// listbox with services
		final ListBoxWithObjects<Service> servicesListbox = new ListBoxWithObjects<Service>();
		// checkbox to swith offered services
		final CheckBox switchBox = new CheckBox("Show already assigned", false);
		switchBox.setTitle("Switching between 'all' and 'already assigned' services in menu");

		// fill menu
		menu.addWidget(finishButton);
		menu.addWidget(assignButton);
		menu.addWidget(removeButton);
		menu.addWidget(new HTML("<strong>Selected&nbsp;service: </strong>"));
		menu.addWidget(servicesListbox);
		menu.addWidget(switchBox);

		// fill listbox with services (used for both callbacks)
		JsonCallbackEvents fillEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				ArrayList<Service> services = JsonUtils.jsoAsList(jso);
				services = new TableSorter<Service>().sortByName(services);
				servicesListbox.clear();
				for (Service serv : services) {
					servicesListbox.addItem(serv);
				}
				if (servicesListbox.isEmpty()) {
					servicesListbox.addItem("No service assigned");
					if (vp.getWidgetCount() > 2)  { vp.remove(2); vp.remove(1); }
					return;
				}
				if (vp.getWidgetCount() > 2) { vp.remove(2); vp.remove(1); }
				vp.add(new HTML("<hr>"));
				vp.add(new ResourceSettingsTabItem(resource, servicesListbox.getSelectedObject()).draw());
				vp.setCellHeight(vp.getWidget(2), "100%");
			}
		};

		// callbacks
		final GetServices services = new GetServices(fillEvents);
		final GetAssignedServices assignedServices = new GetAssignedServices(resource.getId(), fillEvents);
		services.retrieveData();

		// switching event between services
		servicesListbox.addChangeHandler(new ChangeHandler(){
			public void onChange(ChangeEvent event) {
				// remove attributes management if there was
				if (vp.getWidgetCount() > 2)  { vp.remove(2); vp.remove(1); }
				// add attributes management
				vp.add(new HTML("<hr>"));
				vp.add(new ResourceSettingsTabItem(resource, servicesListbox.getSelectedObject()).draw());
				vp.setCellHeight(vp.getWidget(2), "100%");
			}
		});

		// switching between offered services (assigned - all)
		switchBox.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (switchBox.getValue() == false) {
					servicesListbox.clear();
					services.retrieveData();
				} else {
					servicesListbox.clear();
					assignedServices.retrieveData();
				}
			}
		});

		// button events
		assignButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (servicesListbox.isEmpty()) {
					Confirm c = new Confirm("No Service selected", new Label("You must select a service first."), true);
					c.show();
					return;
				}
				AssignService request = new AssignService(JsonCallbackEvents.disableButtonEvents(assignButton));
				request.assignService(servicesListbox.getSelectedObject().getId(), resource.getId());
			}
		});
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (servicesListbox.isEmpty()) {
					Confirm c = new Confirm("No Service selected", new Label("You must select a service first."), true);
					c.show();
					return;
				}
				RemoveService request = new RemoveService(JsonCallbackEvents.disableButtonEvents(removeButton));
				request.removeService(servicesListbox.getSelectedObject().getId(), resource.getId());
			}
		});

		final TabItem tab = this;

		finishButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				session.getTabManager().closeTab(tab);
			}
		});

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
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + facilityId;
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
		CreateFacilityResourceManageServicesTabItem other = (CreateFacilityResourceManageServicesTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
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

		if (session.isFacilityAdmin(facility.getId())) {
			return true;
		} else {
			return false;
		}

	}

}
