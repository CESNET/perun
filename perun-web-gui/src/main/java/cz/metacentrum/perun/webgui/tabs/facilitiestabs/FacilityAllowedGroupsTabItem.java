package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetAllowedGroups;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetAllowedVos;
import cz.metacentrum.perun.webgui.json.servicesManager.GetFacilityAssignedServices;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Tab with list of allowed VOs and Groups on facility
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilityAllowedGroupsTabItem implements TabItem, TabItemWithUrl {

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
	private int lastSelectedVoId = 0;
	private int lastSelectedServiceId = 0;
	private boolean vosCallDone = false;
	private boolean callDone = false;

	/**
	 * Creates a tab instance
	 * @param facility facility to get allowed Vos from
	 */
	public FacilityAllowedGroupsTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facilityId
	 */
	public FacilityAllowedGroupsTabItem(int facilityId){
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
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": Allowed Groups");

		final ListBoxWithObjects<VirtualOrganization> vosListbox = new ListBoxWithObjects<VirtualOrganization>();
		final ListBoxWithObjects<Service> servicesListbox = new ListBoxWithObjects<Service>();
		final GetAllowedGroups jsonCallback = new GetAllowedGroups(facilityId);
		jsonCallback.setCheckable(false);

		// content
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addWidget(new HTML("<strong>Filter by VO:</strong>"));
		menu.addWidget(vosListbox);
		menu.addWidget(new HTML("<strong>Filter by service:</strong>"));
		menu.addWidget(servicesListbox);

		// get the table
		final GetAllowedVos vosCall = new GetAllowedVos(facilityId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				vosListbox.clear();
				vosListbox.removeAllOption();
				vosListbox.addAllItems(new TableSorter<VirtualOrganization>().sortByName(JsonUtils.<VirtualOrganization>jsoAsList(jso)));
				vosListbox.addAllOption();
				if (lastSelectedVoId == 0) {
					vosListbox.setSelectedIndex(0);
				} else {
					for (VirtualOrganization vo : vosListbox.getAllObjects()) {
						if (vo.getId() == lastSelectedVoId) {
							vosListbox.setSelected(vo, true);
							break;
						}
					}
				}
				jsonCallback.setVos(vosListbox.getAllObjects());
				vosCallDone = true;
			}
			public void onLoadingStart(){
				vosListbox.removeAllOption();
				vosListbox.clear();
				vosListbox.addItem("Loading...");
				vosCallDone = false;
			}
			public void onError(PerunError error) {
				vosListbox.clear();
				vosListbox.removeAllOption();
				vosListbox.addItem("Error while loading");
				vosCallDone = false;
			}
		});
		vosCall.retrieveData();

		GetFacilityAssignedServices servCall = new GetFacilityAssignedServices(facilityId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				servicesListbox.clear();
				servicesListbox.removeAllOption();
				servicesListbox.addAllItems(new TableSorter<Service>().sortByName(JsonUtils.<Service>jsoAsList(jso)));
				servicesListbox.addAllOption();
				if (servicesListbox.isEmpty()) {
					servicesListbox.addItem("No service available on facility");
				}
				if (lastSelectedServiceId == 0) {
					// choose all
					servicesListbox.setSelectedIndex(0);
				} else {
					for (Service s : servicesListbox.getAllObjects()) {
						if (s.getId() == lastSelectedServiceId) {
							servicesListbox.setSelected(s, true);
							break;
						}
					}
				}
				callDone = true;
			}
			public void onLoadingStart(){
				servicesListbox.removeAllOption();
				servicesListbox.clear();
				servicesListbox.addItem("Loading...");
				callDone = false;
			}
			public void onError(PerunError error) {
				servicesListbox.clear();
				servicesListbox.removeAllOption();
				servicesListbox.addItem("Error while loading");
				callDone = false;
			}
		});
		servCall.retrieveData();

		Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
			@Override
			public boolean execute() {
				if (vosCallDone && callDone) {
					jsonCallback.setVoId(lastSelectedVoId);
					jsonCallback.setServiceId(lastSelectedServiceId);
					jsonCallback.retrieveData();
					return false;
				} else {
					return true;
				}
			}
		}, 200);

		vosListbox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				if (vosListbox.getSelectedIndex() > 0) {
					jsonCallback.setVoId(vosListbox.getSelectedObject().getId());
					lastSelectedVoId = vosListbox.getSelectedObject().getId();
				} else {
					jsonCallback.setVoId(0);
					lastSelectedVoId = 0;
				}
				jsonCallback.retrieveData();
			}
		});
		servicesListbox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				if (servicesListbox.getSelectedIndex() > 0) {
					jsonCallback.setServiceId(servicesListbox.getSelectedObject().getId());
					lastSelectedServiceId = servicesListbox.getSelectedObject().getId();
				} else {
					jsonCallback.setServiceId(0);
					lastSelectedServiceId = 0;
				}
				jsonCallback.retrieveData();
			}
		});

		CellTable<Group> table = jsonCallback.getEmptyTable(new FieldUpdater<Group, String>() {
			@Override
			public void update(int i, Group group, String s) {
				if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(group.getId())) {
					session.getTabManager().addTab(new GroupDetailTabItem(group));
				} else {
					// show alert
					UiElements.generateInfo("You are not VO / Group manager of this group", "You MUST be VO manager or Group manager of group: <strong>"+SafeHtmlUtils.fromString(group.getName()).asString()+"</strong> to view it's details.");
				}
			}
		});

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

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
		return SmallIcons.INSTANCE.buildingIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 719;
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
		FacilityAllowedGroupsTabItem other = (FacilityAllowedGroupsTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(facility, "Allowed groups", getUrlWithParameters());
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

	public final static String URL = "allowed-vos";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + facilityId;
	}

	static public FacilityAllowedGroupsTabItem load(Facility facility) {
		return new FacilityAllowedGroupsTabItem(facility);
	}

	static public FacilityAllowedGroupsTabItem load(Map<String, String> parameters) {
		int fid = Integer.parseInt(parameters.get("id"));
		return new FacilityAllowedGroupsTabItem(fid);
	}

}
