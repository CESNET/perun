package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.cell.client.FieldUpdater;
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
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.servicesManager.DeleteService;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServices;
import cz.metacentrum.perun.webgui.model.Service;
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
 * Services management for Perun Admin
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class ServicesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Services");

	/**
	 * Creates a tab instance
	 */
	public ServicesTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// create widget for the whole page
		VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%", "100%");

		// create widget for menu on page
		TabMenu tabMenu = new TabMenu();
		tabMenu.addWidget(UiElements.getRefreshButton(this));

		// get services
		final GetServices services = new GetServices();

		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(services);

		// get the table of services with custom field updater (lines are clickable and open service details)
		CellTable<Service> table = services.getTable(new FieldUpdater<Service, String>() {
			// when user click on a row -> open new tab
			public void update(int index, Service object, String value) {
				session.getTabManager().addTab(new ServiceDetailTabItem(object));
			}
		});

		// create button
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createService(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new CreateServiceTabItem());
			}
		}));

		final CustomButton deleteButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteSelectedServices());

		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// get selected items
				final ArrayList<Service> itemsToRemove  = services.getTableSelectedList();
				UiElements.showDeleteConfirm(itemsToRemove, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i=0; i<itemsToRemove.size(); i++ ) {
							DeleteService request;
							if(i == itemsToRemove.size()-1){
								request = new DeleteService(JsonCallbackEvents.disableButtonEvents(deleteButton, events));
							}else{
								request = new DeleteService(JsonCallbackEvents.disableButtonEvents(deleteButton));
							}
							request.deleteService(itemsToRemove.get(i).getId());
						}
					}
				});
			}
		});
		tabMenu.addWidget(deleteButton);

		tabMenu.addFilterWidget(new ExtendedSuggestBox(services.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				services.filterTable(text);
			}
		}, "Filter services by name");

		// add menu to page itself
		mainTab.add(tabMenu);
		mainTab.setCellHeight(tabMenu, "30px");

		// add styling to table with services
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		mainTab.add(sp);

		deleteButton.setEnabled(false);
		JsonUtils.addTableManagedButton(services, table, deleteButton);

		session.getUiElements().resizePerunTable(sp, 350, this);

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
		final int prime = 1109;
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
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Services", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "list";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public ServicesTabItem load(Map<String, String> parameters)
	{
		return new ServicesTabItem();
	}
}
