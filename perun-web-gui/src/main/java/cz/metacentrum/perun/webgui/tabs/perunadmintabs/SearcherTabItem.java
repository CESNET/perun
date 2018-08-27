package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.searcher.GetFacilities;
import cz.metacentrum.perun.webgui.json.searcher.GetMembers;
import cz.metacentrum.perun.webgui.json.searcher.GetResources;
import cz.metacentrum.perun.webgui.json.searcher.GetUsers;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.PerunAdminTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.PerunSearchParametersWidget;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Searcher page
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class SearcherTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Searcher");

	private TabLayoutPanel tabPanel;
	private int lastTabId = 0;

	/**
	 * Creates a tab instance
	 */
	public SearcherTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// main widget panel
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");

		tabPanel = new TabLayoutPanel(33, Style.Unit.PX);
		tabPanel.addStyleName("smallTabPanel");
		final TabItem tab = this;
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				UiElements.runResizeCommands(tab);
			}
		});

		final SimplePanel sp0 = new SimplePanel(); // users
		final SimplePanel sp1 = new SimplePanel(); // facilities
		final SimplePanel sp2 = new SimplePanel(); // members
		final SimplePanel sp3 = new SimplePanel(); // resources

		session.getUiElements().resizeSmallTabPanel(tabPanel, 100, this);

		tabPanel.add(sp0, "Users");
		tabPanel.add(sp2, "Members");
		tabPanel.add(sp1, "Facilities");
		tabPanel.add(sp3, "Resources");

		sp0.setWidget(loadUsersTab());

		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

			public void onSelection(SelectionEvent<Integer> event) {
				UiElements.runResizeCommands(tab);
				setLastTabId(event.getSelectedItem());
				if (0 == event.getSelectedItem()) {
					if (sp0.getWidget() == null) {
						sp0.setWidget(loadUsersTab());
					}
				} else if (1 == event.getSelectedItem()) {
					if (sp2.getWidget() == null) {
						sp2.setWidget(loadMembersTab());
					}
				} else if (2 == event.getSelectedItem()) {
					if (sp1.getWidget() == null) {
						sp1.setWidget(loadFacilitiesTab());
					}
				} else if (3 == event.getSelectedItem()) {
					if (sp3.getWidget() == null) {
						sp3.setWidget(loadResourcesTab());
					}
				}
			}
		});

		tabPanel.selectTab(getLastTabId(), true);  // select and trigger onSelect event

		session.getUiElements().resizePerunTable(tabPanel, 100, this);

		// add tabs to the main panel
		vp.add(tabPanel);
		this.contentWidget.setWidget(vp);

		return getWidget();

	}

	private Widget loadUsersTab() {


		// request
		final GetUsers request = new GetUsers();

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		PerunSearchParametersWidget params = new PerunSearchParametersWidget(PerunEntity.USER, new PerunSearchParametersWidget.SearchEvent() {

			public void search(Map<String, String> map) {

				request.clearParameters();

				for(Map.Entry<String, String> entry : map.entrySet())
				{
					request.addSearchParameter(entry.getKey(), entry.getValue());
				}

				request.search();
			}
		});

		firstTabPanel.add(params);

		// get the table
		final CellTable<User> table = request.getEmptyTable(new FieldUpdater<User, String>() {
			public void update(int index, User object, String value) {
				// opens the tab
				session.getTabManager().addTab(new UserDetailTabItem(object));
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");

		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		firstTabPanel.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);
		return firstTabPanel;

	}

	private Widget loadFacilitiesTab() {

		// request
		final GetFacilities request = new GetFacilities();

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		PerunSearchParametersWidget params = new PerunSearchParametersWidget(PerunEntity.FACILITY, new PerunSearchParametersWidget.SearchEvent() {

			public void search(Map<String, String> map) {

				request.clearParameters();

				for(Map.Entry<String, String> entry : map.entrySet())
				{
					request.addSearchParameter(entry.getKey(), entry.getValue());
				}

				request.search();
			}
		});

		firstTabPanel.add(params);

		// get the table
		final CellTable<Facility> table = request.getEmptyTable(new FieldUpdater<Facility, String>() {
			public void update(int index, Facility object, String value) {
				// opens the tab
				session.getTabManager().addTab(new FacilityDetailTabItem(object));
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");

		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		firstTabPanel.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);
		return firstTabPanel;

	}

	private Widget loadResourcesTab() {

		// request
		final GetResources request = new GetResources();

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		PerunSearchParametersWidget params = new PerunSearchParametersWidget(PerunEntity.RESOURCE, new PerunSearchParametersWidget.SearchEvent() {

			public void search(Map<String, String> map) {

				request.clearParameters();

				for(Map.Entry<String, String> entry : map.entrySet())
				{
					request.addSearchParameter(entry.getKey(), entry.getValue());
				}

				request.search();
			}
		});

		firstTabPanel.add(params);

		// get the table
		final CellTable<Resource> table = request.getEmptyTable(new FieldUpdater<Resource, String>() {
			public void update(int index, Resource object, String value) {
				// opens the tab
				session.getTabManager().addTab(new ResourceDetailTabItem(object.getId(), object.getFacilityId()));
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");

		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		firstTabPanel.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);
		return firstTabPanel;

	}

	private Widget loadMembersTab() {

		// request
		final GetMembers request = new GetMembers(0);

		// MAIN TAB PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		PerunSearchParametersWidget params = new PerunSearchParametersWidget(PerunEntity.MEMBER, new PerunSearchParametersWidget.SearchEvent() {

			public void search(Map<String, String> map) {

				request.clearParameters();

				for(Map.Entry<String, String> entry : map.entrySet())
				{
					request.addSearchParameter(entry.getKey(), entry.getValue());
				}

				request.search();
			}
		});

		final ListBoxWithObjects<VirtualOrganization> vos = new ListBoxWithObjects<VirtualOrganization>();

		vos.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				request.setVoId(vos.getSelectedObject().getId());
			}
		});

		// initial fill listbox and trigger groups loading
		GetVos vosCall = new GetVos(new JsonCallbackEvents(){
			public void onLoadingStart(){
				vos.clear();
				vos.addItem("Loading...");
			}
			public void onFinished(JavaScriptObject jso) {
				vos.clear();
				ArrayList<VirtualOrganization> returnedVos = JsonUtils.jsoAsList(jso);
				returnedVos = new TableSorter<VirtualOrganization>().sortByName(returnedVos);
				if (returnedVos == null || returnedVos.isEmpty()){
					vos.addItem("No VO available");
					return;
				}
				vos.addAllItems(returnedVos);
				request.setVoId(vos.getSelectedObject().getId());
			}
			public void onError(PerunError error){
				vos.clear();
				vos.addItem("Error while loading");
			};
		});
		vosCall.retrieveData();

		TabMenu menu = new TabMenu();
		menu.addWidget(new HTML("<strong>Selected VO:</strong>"));
		menu.addWidget(vos);
		firstTabPanel.add(menu);
		firstTabPanel.add(params);

		// get the table
		final CellTable<Member> table = request.getEmptyTable(new FieldUpdater<Member, String>() {
			public void update(int index, Member object, String value) {
				// opens the tab
				session.getTabManager().addTab(new MemberDetailTabItem(object.getId(), object.getSourceGroupId()));
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");

		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		firstTabPanel.add(sp);
		session.getUiElements().resizePerunTable(sp, 350, this);
		return firstTabPanel;

	}

	/**
	 * Returns ID of last selected subtab in this page
	 *
	 * @return ID of subtab
	 */
	private int getLastTabId(){
		return this.lastTabId;
	}

	/**
	 * Sets ID of subtab as last selected
	 *
	 * @param id
	 */
	private void setLastTabId(int id){
		this.lastTabId = id;
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.magnifierIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 953;
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
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Searcher", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "searcher";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public SearcherTabItem load(Map<String, String> parameters)
	{
		return new SearcherTabItem();
	}
}
