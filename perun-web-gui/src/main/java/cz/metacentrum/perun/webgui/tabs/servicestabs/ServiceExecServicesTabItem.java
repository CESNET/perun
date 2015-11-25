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
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.DeleteExecService;
import cz.metacentrum.perun.webgui.json.generalServiceManager.ListExecServices;
import cz.metacentrum.perun.webgui.json.generalServiceManager.UpdateExecService;
import cz.metacentrum.perun.webgui.model.ExecService;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.ServicesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab with ExecServices management for selected service
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ServiceExecServicesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading exec services");


	// data
	private Service service;
	private int serviceId;

	/**
	 * Tab with ExecServices management for selected service
	 *
	 * @param service service to get ExecServices for
	 */
	public ServiceExecServicesTabItem(Service service){
		this.service = service;
		this.serviceId = service.getId();
	}

	/**
	 * Tab with ExecServices management for selected service
	 *
	 * @param serviceId service to get ExecServices for
	 */
	public ServiceExecServicesTabItem(int serviceId){
		this.serviceId = serviceId;
		new GetEntityById(PerunEntity.SERVICE, serviceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				service = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(service == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(service.getName()) + ": exec services");

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		final ListExecServices callback = new ListExecServices(serviceId);
		CellTable<ExecService> table = callback.getTable(new FieldUpdater<ExecService, String>(){
			public void update(int index, ExecService object, String value) {
				// manage details of exec services (dependency, status, update values?)
				session.getTabManager().addTab(new ViewExecServiceTabItem(object));
			}
		});

		// refresh event after deletion
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(callback);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createExecService(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new AddExecServiceTabItem(service));
			}
		}));

		final CustomButton deleteButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.deleteSelectedExecServices());
		menu.addWidget(deleteButton);
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<ExecService> execToRemove = callback.getTableSelectedList();
				UiElements.showDeleteConfirm(execToRemove, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i=0; i<execToRemove.size(); i++ ) {
							if (i == execToRemove.size()-1) {
								DeleteExecService request = new DeleteExecService(JsonCallbackEvents.disableButtonEvents(deleteButton,events));
								request.deleteExecService(execToRemove.get(i).getId());
							} else {
								DeleteExecService request = new DeleteExecService(JsonCallbackEvents.disableButtonEvents(deleteButton));
								request.deleteExecService(execToRemove.get(i).getId());
							}
						}
					}
				});
			}
		});

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizePerunTable(sp, 350, this);

		final CustomButton enable = TabMenu.getPredefinedButton(ButtonType.ENABLE, "Enable selected exec service(s)");
		final CustomButton disable = TabMenu.getPredefinedButton(ButtonType.DISABLE, "Disable selected exec service(s)");

		menu.addWidget(enable);
		menu.addWidget(disable);

		enable.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				ArrayList<ExecService> list = callback.getTableSelectedList();
				for (int i=0; i<list.size(); i++) {

					ExecService e = list.get(i);
					if (i<list.size()-1) {
						// any call
						if (!e.isEnabled()) {
							UpdateExecService request = new UpdateExecService(JsonCallbackEvents.disableButtonEvents(enable));
							e.setEnabled(true);
							request.updateExecService(e);
						}
					} else {
						// last call
						if (!e.isEnabled()) {
							UpdateExecService request = new UpdateExecService(JsonCallbackEvents.disableButtonEvents(enable, events));
							e.setEnabled(true);
							request.updateExecService(e);
						}

					}
				}
			}
		});

		disable.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				ArrayList<ExecService> list = callback.getTableSelectedList();
				for (int i=0; i<list.size(); i++) {

					ExecService e = list.get(i);
					if (i<list.size()-1) {
						// any call
						if (e.isEnabled()) {
							UpdateExecService request = new UpdateExecService(JsonCallbackEvents.disableButtonEvents(disable));
							e.setEnabled(false);
							request.updateExecService(e);
						}
					} else {
						// last call
						if (e.isEnabled()) {
							UpdateExecService request = new UpdateExecService(JsonCallbackEvents.disableButtonEvents(disable, events));
							e.setEnabled(false);
							request.updateExecService(e);
						}

					}
				}
			}
		});

		deleteButton.setEnabled(false);
		enable.setEnabled(false);
		disable.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, deleteButton);
		JsonUtils.addTableManagedButton(callback, table, enable);
		JsonUtils.addTableManagedButton(callback, table, disable);

		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		// add tabs to the main panel
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
		final int prime = 1091;
		int result = 1;
		result = prime * result + serviceId;
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
		ServiceExecServicesTabItem other = (ServiceExecServicesTabItem) obj;
		if (serviceId != other.serviceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN);
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "exec";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + serviceId;
	}

	static public ServiceExecServicesTabItem load(Map<String, String> parameters) {
		int id = Integer.parseInt(parameters.get("id"));
		return new ServiceExecServicesTabItem(id);
	}

}