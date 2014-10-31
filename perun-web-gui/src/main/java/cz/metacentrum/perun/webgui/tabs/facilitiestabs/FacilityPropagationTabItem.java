package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.BanExecServiceOnFacility;
import cz.metacentrum.perun.webgui.json.generalServiceManager.ForceServicePropagation;
import cz.metacentrum.perun.webgui.json.generalServiceManager.FreeDenialOfExecServiceOnFacility;
import cz.metacentrum.perun.webgui.json.generalServiceManager.GetFacilityAssignedServicesForGUI;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.RichService;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.servicestabs.ServiceExecServicesTabItem;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab with services available on facility and option to start their propagation or disable them
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityPropagationTabItem implements TabItem, TabItemWithUrl{

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

	private int lastSelectedIndex = 0;

	private TabLayoutPanel tabPanel;

	/**
	 * Creates a tab instance
	 * @param facility facility to get services from
	 */
	public FacilityPropagationTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 * @param facility facility to get services from
	 * @param tabPanel connection to parent tab panel for switching
	 */
	public FacilityPropagationTabItem(Facility facility, TabLayoutPanel tabPanel){
		this.facility = facility;
		this.facilityId = facility.getId();
		this.tabPanel = tabPanel;
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId
	 */
	public FacilityPropagationTabItem(int facilityId){
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
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Services propagation");

		// content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();

		// get the table
		final GetFacilityAssignedServicesForGUI jsonCallback = new GetFacilityAssignedServicesForGUI(facility.getId());

		final CellTable<RichService> table;
		if (session.isPerunAdmin()) {
			table = jsonCallback.getTable(new FieldUpdater<RichService, String>() {
				public void update(int index, RichService object, String value) {
					Service serv = object.cast();
					session.getTabManager().addTab(new ServiceExecServicesTabItem(serv));
				}});
		} else {
			table = jsonCallback.getTable();
		}

		final CustomButton forceButton = new CustomButton(ButtonTranslation.INSTANCE.forcePropagationButton(), ButtonTranslation.INSTANCE.forcePropagation(), SmallIcons.INSTANCE.arrowRightIcon());
		forceButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				final ArrayList<RichService> forceList = jsonCallback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(forceList)) {

					// TODO - translated Widget
					boolean denied = false;
					VerticalPanel vp = new VerticalPanel();
					vp.add(new HTML("<p>Some services can't be forcefully propagated, because they are <strong>denied on facility</strong>. Please change their state to 'Allowed' before starting force propagation.</p>"));
					for (int i=0; i<forceList.size(); i++ ) {
						if (forceList.get(i).getAllowedOnFacility().equalsIgnoreCase("Denied")) {
							vp.add(new Label(" - "+forceList.get(i).getName()));
							denied = true;
						}
					}
					if (denied) {
						// show conf
						Confirm c = new Confirm("Can't propagated denied services", vp, true);
						c.show();
						return;
					}
				}

				// show propagation status page on last call start
				JsonCallbackEvents events = new JsonCallbackEvents(){
					@Override
					public void onLoadingStart() {
						if (tabPanel != null) {
							tabPanel.selectTab(2);
						} else {
							session.getTabManager().addTab(new FacilityPropagationTabItem(facility));
						}
					}
					@Override
					public void onFinished(JavaScriptObject jso) {
						// unselect all services
						for (RichService service : forceList) {
							jsonCallback.getSelectionModel().setSelected(service, false);
						}
					}
				};

				// starts propagation for all selected services
				for (int i=0; i<forceList.size(); i++ ) {
					if (i != forceList.size()-1) {
						// force propagation
						ForceServicePropagation request = new ForceServicePropagation(JsonCallbackEvents.disableButtonEvents(forceButton));
						request.forcePropagation(facility.getId(), forceList.get(i).getId());
					} else {
						// force propagation with show status page
						ForceServicePropagation request = new ForceServicePropagation(JsonCallbackEvents.disableButtonEvents(forceButton, events));
						request.forcePropagation(facility.getId(), forceList.get(i).getId());
					}
				}

			}
		});
		menu.addWidget(forceButton);

		final ListBox servPart = new ListBox();
		servPart.addItem("Both");
		servPart.addItem("Generate");
		servPart.addItem("Send");
		servPart.setSelectedIndex(lastSelectedIndex);
		servPart.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				lastSelectedIndex = servPart.getSelectedIndex();
			}
		});

		final CustomButton blockButton = new CustomButton(ButtonTranslation.INSTANCE.blockPropagationButton(), ButtonTranslation.INSTANCE.blockServicesOnFacility(), SmallIcons.INSTANCE.stopIcon());
		blockButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {

				final ArrayList<RichService> list = jsonCallback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
					for (int i=0; i<list.size(); i++) {
						// call
						BanExecServiceOnFacility request = new BanExecServiceOnFacility(facilityId, JsonCallbackEvents.disableButtonEvents(blockButton));
						// last event
						JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(blockButton, new JsonCallbackEvents() {
							public void onError(PerunError error) {
								jsonCallback.clearTable();
								jsonCallback.retrieveData();
							}
							public void onFinished(JavaScriptObject jso) {
								jsonCallback.clearTable();
								jsonCallback.retrieveData();
							}
						});
						// which part ?
						if (servPart.getSelectedIndex() == 0) {
							// both parts
							if (list.get(i).getGenExecService() != null) request.banExecService(list.get(i).getGenExecService().getId());
							if (i == list.size()-1) { request.setEvents(events); }
							if (list.get(i).getSendExecService() != null) request.banExecService(list.get(i).getSendExecService().getId());
						} else if (servPart.getSelectedIndex() == 1) {
							// just generate
							if (i == list.size()-1) { request.setEvents(events); }
							if (list.get(i).getGenExecService() != null) request.banExecService(list.get(i).getGenExecService().getId());
						} else if (servPart.getSelectedIndex() == 2) {
							// just send
							if (i == list.size()-1) { request.setEvents(events); }
							if (list.get(i).getSendExecService() != null) request.banExecService(list.get(i).getSendExecService().getId());
						}
					}
				}
			}
		});
		menu.addWidget(blockButton);

		final CustomButton allowButton = new CustomButton(ButtonTranslation.INSTANCE.allowPropagationButton(), ButtonTranslation.INSTANCE.allowServicesOnFacility(), SmallIcons.INSTANCE.acceptIcon());
		allowButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<RichService> list = jsonCallback.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(list)) {
					for (int i=0; i<list.size(); i++) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						FreeDenialOfExecServiceOnFacility request = new FreeDenialOfExecServiceOnFacility(facilityId, JsonCallbackEvents.disableButtonEvents(allowButton));
						// last event
						JsonCallbackEvents events = JsonCallbackEvents.disableButtonEvents(allowButton, new JsonCallbackEvents() {
							public void onError(PerunError error) {
								jsonCallback.clearTable();
								jsonCallback.retrieveData();
							}
							public void onFinished(JavaScriptObject jso) {
								jsonCallback.clearTable();
								jsonCallback.retrieveData();
							}
						});
						// which part ?
						if (servPart.getSelectedIndex() == 0) {
							// both parts
							if (list.get(i).getGenExecService() != null) request.freeDenialOfExecService(list.get(i).getGenExecService().getId());
							if (i == list.size()-1) { request.setEvents(events); }
							if (list.get(i).getSendExecService() != null) request.freeDenialOfExecService(list.get(i).getSendExecService().getId());
						} else if (servPart.getSelectedIndex() == 1) {
							// just generate
							if (i == list.size()-1) { request.setEvents(events); }
							if (list.get(i).getGenExecService() != null) request.freeDenialOfExecService(list.get(i).getGenExecService().getId());
						} else if (servPart.getSelectedIndex() == 2) {
							// just send
							if (i == list.size()-1) { request.setEvents(events); }
							if (list.get(i).getSendExecService() != null) request.freeDenialOfExecService(list.get(i).getSendExecService().getId());
						}
					}
				}
			}
		});
		menu.addWidget(allowButton);

		menu.addWidget(new HTML("<strong>Generate/Send: </strong>"));
		menu.addWidget(servPart);

		allowButton.setEnabled(false);
		blockButton.setEnabled(false);
		forceButton.setEnabled(false);
		JsonUtils.addTableManagedButton(jsonCallback, table, allowButton);
		JsonUtils.addTableManagedButton(jsonCallback, table, blockButton);
		JsonUtils.addTableManagedButton(jsonCallback, table, forceButton);

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		// table.addColumn(statusColumn, "Denial status on facility");

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
		return SmallIcons.INSTANCE.arrowRightIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 39;
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
		FacilityPropagationTabItem other = (FacilityPropagationTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Services propagation", getUrlWithParameters());
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

	public final static String URL = "srvprop";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}

	static public FacilityPropagationTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityPropagationTabItem(fid);
	}

	static public FacilityPropagationTabItem load(Facility fac) {
		return new FacilityPropagationTabItem(fac);
	}

}
