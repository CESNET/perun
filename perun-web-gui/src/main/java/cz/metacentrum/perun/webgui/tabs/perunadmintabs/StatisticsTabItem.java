package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunStatus;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetGroupsCount;
import cz.metacentrum.perun.webgui.json.membersManager.GetMembersCount;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetResourcesCount;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.PerunAdminTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Statistics page
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class StatisticsTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Statistics");

	/**
	 * Creates a tab instance
	 */
	public StatisticsTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// PAGE CONTENTS
		ScrollPanel scroll = new ScrollPanel();
		final VerticalPanel vp = new VerticalPanel();
		vp.setStyleName("perun-table");
		vp.setSpacing(5);
		scroll.setWidget(vp);
		scroll.setStyleName("perun-tableScrollPanel");

		session.getUiElements().resizePerunTable(scroll, 350, this);

		final DisclosurePanel vosStatistics = new DisclosurePanel();
		vosStatistics.setWidth("100%");
		//vosStatistics.setOpen(true);
		FlexTable vosHeader = new FlexTable();
		vosHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.buildingIcon()));
		vosHeader.setWidget(0, 1, new HTML("<h3>Virtual Organizations</h3>"));
		vosHeader.setTitle("Click to show/hide VOs statistics");
		vosStatistics.setHeader(vosHeader);

		vp.add(vosStatistics);

		// the general statistics table
		final FlexTable statisticsTable = new FlexTable();
		statisticsTable.addStyleName("statisticsTable");

		// the VOs table
		final FlexTable vosTable = new FlexTable();
		vosTable.addStyleName("statisticsTable");
		vosTable.setWidget(0, 0, new HTML("<strong>" + "VO name" + "</strong>"));
		vosTable.setWidget(0, 1, new HTML("<strong>" + "Members" + "</strong>"));
		vosTable.setWidget(0, 2, new HTML("<strong>" + "Valid members" + "</strong>"));
		vosTable.setWidget(0, 3, new HTML("<strong>" + "Invalid members" + "</strong>"));
		vosTable.setWidget(0, 4, new HTML("<strong>" + "Suspended members" + "</strong>"));
		vosTable.setWidget(0, 5, new HTML("<strong>" + "Expired members" + "</strong>"));
		vosTable.setWidget(0, 6, new HTML("<strong>" + "Disabled members" + "</strong>"));
		vosTable.setWidget(0, 7, new HTML("<strong>" + "Groups" + "</strong>"));
		vosTable.setWidget(0, 8, new HTML("<strong>" + "Resources" + "</strong>"));

		// vos events - adds the VOs to the table and calls how many members the VO has
		JsonCallbackEvents vosEvents = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				// conversion
				ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
				vos = new TableSorter<VirtualOrganization>().sortByName(vos);

				// iterating VOs
				for(int i = 0; i<vos.size(); i++){
					VirtualOrganization vo = vos.get(i);

					// call the request for number of members
					GetMembersCount countMembers = new GetMembersCount(vo.getId(), null);
					countMembers.retrieveData();

					GetMembersCount countValidMembers = new GetMembersCount(vo.getId(), PerunStatus.VALID);
					countValidMembers.retrieveData();

					GetMembersCount countInvalidMembers = new GetMembersCount(vo.getId(), PerunStatus.INVALID);
					countInvalidMembers.retrieveData();

					GetMembersCount countSuspendedMembers = new GetMembersCount(vo.getId(), PerunStatus.SUSPENDED);
					countSuspendedMembers.retrieveData();

					GetMembersCount countExpiredMembers = new GetMembersCount(vo.getId(), PerunStatus.EXPIRED);
					countExpiredMembers.retrieveData();

					GetMembersCount countDisabledMembers = new GetMembersCount(vo.getId(), PerunStatus.DISABLED);
					countDisabledMembers.retrieveData();

					// call the request for number of resources
					GetResourcesCount countResources = new GetResourcesCount(vo.getId());
					countResources.retrieveData();

					GetGroupsCount countGroups= new GetGroupsCount(vo.getId());
					countGroups.retrieveData();

					//adds the VO to the table
					vosTable.setText(i + 1, 0, vo.getName());
					vosTable.setWidget(i + 1, 1, countMembers.getMembersCountHyperlink());
					vosTable.setWidget(i + 1, 2, countValidMembers.getMembersCountHyperlink());
					vosTable.setWidget(i + 1, 3, countInvalidMembers.getMembersCountHyperlink());
					vosTable.setWidget(i + 1, 4, countSuspendedMembers.getMembersCountHyperlink());
					vosTable.setWidget(i + 1, 5, countExpiredMembers.getMembersCountHyperlink());
					vosTable.setWidget(i + 1, 6, countDisabledMembers.getMembersCountHyperlink());
					vosTable.setWidget(i + 1, 7, countGroups.getGroupsCountHyperlink());
					vosTable.setWidget(i + 1, 8, countResources.getResourcesCountHyperlink());

				}

			}
		};

		// requests
		final GetVos vos = new GetVos(vosEvents);

		vosHeader.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (vosStatistics.getContent() == null) {

					// retrieve data
					vosStatistics.setContent(vosTable);
					vos.retrieveData();

				}
			}
		});

		this.contentWidget.setWidget(scroll);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.statisticsIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 967;
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

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Statistics", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "stats";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public StatisticsTabItem load(Map<String, String> parameters)
	{
		return new StatisticsTabItem();
	}
}
