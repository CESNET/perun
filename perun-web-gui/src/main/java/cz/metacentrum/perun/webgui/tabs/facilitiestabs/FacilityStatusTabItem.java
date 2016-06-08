package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
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
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.BanExecServiceOnFacility;
import cz.metacentrum.perun.webgui.json.generalServiceManager.ForceServicePropagation;
import cz.metacentrum.perun.webgui.json.generalServiceManager.FreeDenialOfExecServiceOnFacility;
import cz.metacentrum.perun.webgui.json.propagationStatsReader.DeleteTask;
import cz.metacentrum.perun.webgui.json.propagationStatsReader.GetFacilityServicesState;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.Map;

/**
 * Shows facility status
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityStatusTabItem implements TabItem, TabItemWithUrl {

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
	 * @param facility facility to get services and status from
	 */
	public FacilityStatusTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId
	 */
	public FacilityStatusTabItem(int facilityId){
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

		// title
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Services status");

		// main content
		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// get empty table
		final GetFacilityServicesState callback = new GetFacilityServicesState(facility.getId());

		CustomButton refreshButton = UiElements.getRefreshButton(this);
		//callback.setEvents(JsonCallbackEvents.disableButtonEvents(refreshButton));

		final CellTable<ServiceState> table = callback.getTable(new FieldUpdater<ServiceState, String>(){
			// on row click
			public void update(int index, final ServiceState object, String value) {
				// show results
				session.getTabManager().addTab(new TaskResultsTabItem(object.getSendTask()));
			}
		});

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		final CustomButton forceButton = new CustomButton(ButtonTranslation.INSTANCE.forcePropagationButton(), ButtonTranslation.INSTANCE.forcePropagation(), SmallIcons.INSTANCE.arrowRightIcon());
		forceButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				final ArrayList<ServiceState> forceList = callback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(forceList)) {

					// TODO - translated Widget
					boolean denied = false;
					VerticalPanel vp = new VerticalPanel();
					vp.add(new HTML("<p>Some services can't be forcefully propagated, because they are <strong>blocked on facility</strong>. Please change their state to 'Allowed' before starting force propagation.</p>"));
					for (int i=0; i<forceList.size(); i++ ) {
						if (forceList.get(i).isBlockedOnFacility() || forceList.get(i).isBlockedGlobally()) {
							vp.add(new Label(" - "+forceList.get(i).getService().getName()));
							denied = true;
						}
					}
					if (denied) {
						// show conf
						Confirm c = new Confirm("Can't propagated blocked services", vp, true);
						c.show();
						return;
					}
				}

				// show propagation status page on last call start
				JsonCallbackEvents events = new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						// unselect all services
						for (ServiceState service : forceList) {
							callback.getSelectionModel().setSelected(service, false);
						}
					}
				};

				// starts propagation for all selected services
				for (int i=0; i<forceList.size(); i++ ) {
					if (i != forceList.size()-1) {
						// force propagation
						ForceServicePropagation request = new ForceServicePropagation(JsonCallbackEvents.disableButtonEvents(forceButton));
						request.forcePropagation(facility.getId(), forceList.get(i).getService().getId());
					} else {
						// force propagation with show status page
						ForceServicePropagation request = new ForceServicePropagation(JsonCallbackEvents.disableButtonEvents(forceButton, events));
						request.forcePropagation(facility.getId(), forceList.get(i).getService().getId());
					}
				}

			}
		});

		final CustomButton blockButton = new CustomButton(ButtonTranslation.INSTANCE.blockPropagationButton(), ButtonTranslation.INSTANCE.blockServicesOnFacility(), SmallIcons.INSTANCE.stopIcon());
		final CustomButton allowButton = new CustomButton(ButtonTranslation.INSTANCE.allowPropagationButton(), ButtonTranslation.INSTANCE.allowServicesOnFacility(), SmallIcons.INSTANCE.acceptIcon());

		final CustomButton deleteButton = new CustomButton(ButtonTranslation.INSTANCE.deleteButton(), ButtonTranslation.INSTANCE.deleteTasks(), SmallIcons.INSTANCE.deleteIcon());

		blockButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<ServiceState> list = callback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i = 0; i < list.size(); i++) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						BanExecServiceOnFacility request = new BanExecServiceOnFacility(facilityId, JsonCallbackEvents.disableButtonEvents(allowButton));
						// last event
						JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(blockButton, JsonCallbackEvents.refreshTableEvents(callback));
						if (i == list.size()-1) request.setEvents(events);
						if (list.get(i).getSendTask() != null) request.banExecService(list.get(i).getSendTask().getExecService().getId());
					}
				}
			}
		});

		allowButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<ServiceState> list = callback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i = 0; i < list.size(); i++) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						FreeDenialOfExecServiceOnFacility request = new FreeDenialOfExecServiceOnFacility(facilityId, JsonCallbackEvents.disableButtonEvents(allowButton));
						// last event
						JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(allowButton, JsonCallbackEvents.refreshTableEvents(callback));
						if (i == list.size()-1) request.setEvents(events);
						if (list.get(i).getSendTask() != null) request.freeDenialOfExecService(list.get(i).getSendTask().getExecService().getId());
					}
				}
			}
		});

		TabMenu menu = new TabMenu();
		menu.addWidget(refreshButton);
		menu.addWidget(forceButton);
		menu.addWidget(allowButton);
		menu.addWidget(blockButton);

		if (session.isPerunAdmin()) {
			menu.addWidget(deleteButton);
			deleteButton.setEnabled(false);
			JsonUtils.addTableManagedButton(callback, table, deleteButton);

			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final ArrayList<ServiceState> list = callback.getTableSelectedList();
					if (UiElements.cantSaveEmptyListDialogBox(list)) {
						UiElements.generateAlert("", "This action will also delete all propagation results. <p>If service is still assigned to any resource, it will be listed in a table.", new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								for (ServiceState ss : list) {
									DeleteTask deleteTask = new DeleteTask(JsonCallbackEvents.disableButtonEvents(deleteButton));
									if (ss.getGenTask() != null) deleteTask.deleteTask(ss.getGenTask().getId());
									deleteTask.setEvents(JsonCallbackEvents.disableButtonEvents(deleteButton, JsonCallbackEvents.refreshTableEvents(callback)));
									if (ss.getSendTask() != null) deleteTask.deleteTask(ss.getSendTask().getId());
								}
							}
						});
					}
				}
			});
		}

		forceButton.setEnabled(false);
		allowButton.setEnabled(false);
		blockButton.setEnabled(false);

		JsonUtils.addTableManagedButton(callback, table, forceButton);
		JsonUtils.addTableManagedButton(callback, table, allowButton);
		JsonUtils.addTableManagedButton(callback, table, blockButton);

		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);

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
		return SmallIcons.INSTANCE.serverInformationIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1373;
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
		FacilityStatusTabItem other = (FacilityStatusTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Services status", getUrlWithParameters());
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

	public final static String URL = "status";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facility.getId();
	}

	static public FacilityStatusTabItem load(Facility facility) {
		return new FacilityStatusTabItem(facility);
	}

	static public FacilityStatusTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityStatusTabItem(fid);
	}

}
