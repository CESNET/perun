package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.servicesManager.AddServiceToServicesPackage;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServices;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServicesFromServicesPackage;
import cz.metacentrum.perun.webgui.json.servicesManager.RemoveServiceFromServicesPackage;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.model.ServicesPackage;
import cz.metacentrum.perun.webgui.tabs.ServicesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Services packages services management for Perun Admin
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ServicePackageDetailTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Service package detail");
	private int servicePackageId = 0;
	private ServicesPackage servicesPackage;

	/**
	 * Creates a tab instance
	 *
	 * @param servicePackageId ID of service package
	 */
	public ServicePackageDetailTabItem(int servicePackageId){
		this.servicePackageId = servicePackageId;
	}

	/**
	 * Creates a tab instance
	 *
	 * @param servicesPackage ServicesPackage
	 */
	public ServicePackageDetailTabItem(ServicesPackage servicesPackage){
		this.servicesPackage = servicesPackage;
		this.servicePackageId = servicesPackage.getId();
	}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(servicesPackage.getName()) + ": assigned services");

		// create widget for the whole page
		HorizontalPanel mainTab = new HorizontalPanel();
		mainTab.setSize("100%", "100%");

		// get services
		final GetServices services = new GetServices();
		final GetServicesFromServicesPackage assignedServices = new GetServicesFromServicesPackage(servicePackageId);

		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(services);
		final JsonCallbackEvents events2 = JsonCallbackEvents.refreshTableEvents(assignedServices);

		// get the table of services with custom field updater (lines are clickable and open service details)
		CellTable<Service> table = services.getTable(new FieldUpdater<Service, String>() {
			// when user click on a row -> open new tab
			public void update(int index, Service object, String value) {
				session.getTabManager().addTab(new ServiceDetailTabItem(object));
			}
		});

		// get the table of services with custom field updater (lines are clickable and open service details)
		CellTable<Service> table2 = assignedServices.getTable(new FieldUpdater<Service, String>() {
			// when user click on a row -> open new tab
			public void update(int index, Service object, String value) {
				session.getTabManager().addTab(new ServiceDetailTabItem(object));
			}
		});

		// add styling to table with services
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		VerticalPanel vp1 = new VerticalPanel();
		vp1.setSize("100%", "100%");

		TabMenu menu1 = new TabMenu();
		menu1.addFilterWidget(new ExtendedSuggestBox(services.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				services.filterTable(text);
			}
		}, "Filter services by name");

		vp1.add(menu1);
		vp1.setCellHeight(menu1, "30px");
		vp1.add(sp);

		mainTab.add(vp1);
		mainTab.setCellWidth(vp1, "50%");

		// middle content
		final CustomButton add = new CustomButton("Add", "Add services to package", SmallIcons.INSTANCE.arrowRightIcon());
		add.setImageAlign(true);
		add.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<Service> list = services.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
					for (int i=0; i<list.size(); i++ ) {
						AddServiceToServicesPackage request;
						final int ii = i;
						if(i == list.size()-1){
							request = new AddServiceToServicesPackage(JsonCallbackEvents.disableButtonEvents(add, JsonCallbackEvents.mergeEvents(new JsonCallbackEvents(){
								@Override
								public void onFinished(JavaScriptObject jso) {
									services.getSelectionModel().setSelected(list.get(ii), false);
								}
							}, events2)));
						}else{
							request = new AddServiceToServicesPackage(JsonCallbackEvents.disableButtonEvents(add, new JsonCallbackEvents(){
								@Override
								public void onFinished(JavaScriptObject jso) {
									services.getSelectionModel().setSelected(list.get(ii), false);
								}
							}));
						}
						request.addServiceToServicesPackage(servicePackageId, list.get(i).getId());
					}
				}
			}
		});

		final CustomButton remove = new CustomButton("Remove", "Remove services from package", SmallIcons.INSTANCE.arrowLeftIcon());
		remove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ArrayList<Service> list = assignedServices.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
					for (int i=0; i<list.size(); i++ ) {
						RemoveServiceFromServicesPackage request;
						if(i == list.size()-1){
							request = new RemoveServiceFromServicesPackage(JsonCallbackEvents.disableButtonEvents(remove, events2));
						}else{
							request = new RemoveServiceFromServicesPackage(JsonCallbackEvents.disableButtonEvents(remove));
						}
						request.removeServiceFromServicesPackage(servicePackageId, list.get(i).getId());
					}
				}
			}
		});

		final TabItem tab = this;
		CustomButton close = TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().closeTab(tab);
			}
		});

		FlexTable ft = new FlexTable();
		ft.setWidget(0, 0, add);
		ft.setWidget(1, 0, remove);
		ft.setWidget(2, 0, close);
		ft.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		ft.getFlexCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		ft.getFlexCellFormatter().setAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		ft.setWidth("130px");
		ft.setCellSpacing(10);

		mainTab.add(ft);
		mainTab.setCellVerticalAlignment(ft, HasVerticalAlignment.ALIGN_MIDDLE);

		// add styling to table2 with assigned services
		table2.addStyleName("perun-table");
		ScrollPanel sp2 = new ScrollPanel(table2);
		sp2.addStyleName("perun-tableScrollPanel");

		VerticalPanel vp2 = new VerticalPanel();
		vp2.setSize("100%", "100%");

		TabMenu menu2 = new TabMenu();
		menu2.addFilterWidget(new ExtendedSuggestBox(assignedServices.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				assignedServices.filterTable(text);
			}
		}, "Filter services by name");

		vp2.add(menu2);
		vp2.setCellHeight(menu2, "30px");
		vp2.add(sp2);

		mainTab.add(vp2);
		mainTab.setCellWidth(vp2, "50%");

		add.setEnabled(false);
		JsonUtils.addTableManagedButton(services, table, add);

		remove.setEnabled(false);
		JsonUtils.addTableManagedButton(assignedServices, table2, remove);

		session.getUiElements().resizeSmallTabPanel(mainTab, 350, this);
		session.getUiElements().resizePerunTable(sp, 350, this);
		session.getUiElements().resizePerunTable(sp2, 350, this);

		this.contentWidget.setWidget(mainTab);

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
		final int prime = 1093;
		int result = 1;
		result = prime * result + 122341;
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


		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Service package detail", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "packdetail";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + servicePackageId;
	}

	static public ServicePackageDetailTabItem load(Map<String, String> parameters)
	{
		int id = Integer.valueOf(parameters.get("id"));
		return new ServicePackageDetailTabItem(id);
	}
}
