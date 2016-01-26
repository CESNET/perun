package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.ForceServicePropagation;
import cz.metacentrum.perun.webgui.json.generalServiceManager.GetFacilityAssignedServicesForGUI;
import cz.metacentrum.perun.webgui.json.propagationStatsReader.GetTaskResultsByDestinations;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Return Tab with Task Results loaded by destination name
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class DestinationResultsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Loading Task");
	// if accessed pro perun admin section
	private boolean admin = false;

	// data
	private String destination;
	private int facilityId = 0;
	private Facility facility;
	private VirtualOrganization vo;
	private int voId = 0;

	/**
	 * Creates a tab instance
	 * @param facilityId ID of Facility
	 * @param voId ID of VO
	 * @param destination destination
	 * @param admin TRUE if accessed from perun admin
	 */
	public DestinationResultsTabItem(int facilityId, int voId, String destination, boolean admin){
		this.facilityId = facilityId;
		this.voId = voId;
		this.admin = admin;
		this.destination = destination;
		if (facilityId != 0) {
			new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					facility = jso.cast();
				}
			}).retrieveData();
		}
		if (voId != 0) {
			new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, facilityId, new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					vo = jso.cast();
				}
			}).retrieveData();
		}
	}

	/**
	 * Creates a tab instance
	 * @param facility Facility
	 * @param vo VO
	 * @param destination destination
	 * @param admin TRUE if accessed from perun admin section
	 */
	public DestinationResultsTabItem(Facility facility, VirtualOrganization vo, String destination, boolean admin){
		this.admin = admin;
		this.destination = destination;
		if (facility != null) {
			this.facilityId = facility.getId();
			this.facility = facility;
		}
		if (vo != null) {
			this.voId = vo.getId();
			this.vo = vo;
		}
	}

	public boolean isPrepared(){
		if (facilityId != 0 && voId != 0) {
			return (vo != null && facility != null);
		}
		if (facilityId != 0 && voId == 0) {
			return facility != null;
		}
		if (facilityId == 0 && voId != 0) {
			return vo != null;
		}
		return true;
	}

	public Widget draw() {

		this.titleWidget.setText("Tasks results: "+destination);

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		final ListBoxWithObjects<RichService> listbox = new ListBoxWithObjects<RichService>();

		final CustomButton cb = new CustomButton(ButtonTranslation.INSTANCE.forcePropagationButton(), ButtonTranslation.INSTANCE.forcePropagation(), SmallIcons.INSTANCE.arrowRightIcon());

		GetFacilityAssignedServicesForGUI servCall = new GetFacilityAssignedServicesForGUI(facilityId);
		servCall.setEvents(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso){
				listbox.clear();
				ArrayList<RichService> list = JsonUtils.jsoAsList(jso);
				list = new TableSorter<RichService>().sortByName(list);
				for (RichService s : list){
					if (s.getAllowedOnFacility().equalsIgnoreCase("allowed") && (s.getGenExecService() != null && s.getGenExecService().isEnabled()) && (s.getSendExecService() != null && s.getSendExecService().isEnabled())){
						listbox.addItem(s);
					}
				}
				if (listbox.isEmpty()){
					listbox.addItem("No service available");
					cb.setEnabled(false);
				}
			}
		@Override
		public void onError(PerunError error){
			listbox.clear();
			listbox.addItem("Error while loading");
			cb.setEnabled(false);
		}
		@Override
		public void onLoadingStart() {
			listbox.clear();
			listbox.addItem("Loading...");
		}
		});
		if (facilityId != 0) {
			servCall.retrieveData();
		}

		cb.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				ForceServicePropagation force = new ForceServicePropagation(JsonCallbackEvents.disableButtonEvents(cb));
				force.forcePropagation(facilityId, listbox.getSelectedObject().getId());
			}
		});

		menu.addWidget(cb);
		menu.addWidget(new HTML("<strong>Service: </strong>"));
		menu.addWidget(listbox);

		Anchor a = new Anchor("View facility details >>");
		a.setStyleName("pointer");
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTab(new FacilityDetailTabItem(facility));
			}
		});
		menu.addWidget(a);

		ArrayList<String> dest = new ArrayList<String>();
		dest.add(destination);

		final GetTaskResultsByDestinations callback = new GetTaskResultsByDestinations(dest);
		CellTable<TaskResult> table = callback.getTable();

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
		return SmallIcons.INSTANCE.databaseServerIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 683;
		int result = 1;
		result = prime * result;
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
		DestinationResultsTabItem other = (DestinationResultsTabItem) obj;
		if (!destination.equals(other.destination))
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		if (vo != null) {
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.VO_ADMIN, "Facilities states", VosTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"propags?vo="+voId, "Results: "+destination, getUrlWithParameters());
		} else if (facility != null) {
			if (admin && session.isPerunAdmin()) {
				session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Propagations", PerunAdminTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"propags", "Results: "+destination, getUrlWithParameters());
			}
			session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "All facilities states", FacilitiesTabs.URL+UrlMapper.TAB_NAME_SEPARATOR+"propags", "Results: "+destination, getUrlWithParameters());
		}
	}

	public boolean isAuthorized() {
		if (session.isFacilityAdmin(facilityId) || session.isVoAdmin(voId)) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "dest-result";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl()+"?dest="+destination+"&fid="+facilityId+"&vid="+voId+"&pa="+admin;
	}

	static public DestinationResultsTabItem load(Map<String, String> parameters) {
		String dest = parameters.get("dest");
		int facility = Integer.parseInt(parameters.get("fid"));
		int vo = Integer.parseInt(parameters.get("vid"));
		boolean admin = Boolean.parseBoolean(parameters.get("pa"));
		return new DestinationResultsTabItem(facility, vo, dest, admin);
	}

}
