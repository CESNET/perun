package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetExtSources;
import cz.metacentrum.perun.webgui.json.extSourcesManager.LoadExtSourcesDefinitions;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.tabs.PerunAdminTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * ExtSourcesManager for Perun administrator
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ExtSourcesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("External sources");


	/**
	 * Creates a tab instance
	 */
	public ExtSourcesTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// create main panel for content
		VerticalPanel mainPage = new VerticalPanel();
		mainPage.setWidth("100%");

		// create new instance for jsonCall getExtSources
		final GetExtSources getExtSources = new GetExtSources();
		getExtSources.setCheckable(false);

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));
		menu.addFilterWidget(new ExtendedSuggestBox(getExtSources.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				getExtSources.filterTable(text);
			}
		}, "Filter external sources by name or type");

		final CustomButton loadButton = new CustomButton("Load ext sources", "Load ext sources definitions from a local file.", SmallIcons.INSTANCE.worldIcon());
		loadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadExtSourcesDefinitions loadCall = new LoadExtSourcesDefinitions(JsonCallbackEvents.disableButtonEvents(loadButton, JsonCallbackEvents.refreshTableEvents(getExtSources)));
				loadCall.retrieveData();
			}
		});

		menu.addWidget(loadButton);

		// get CellTable from jsonCall
		CellTable<ExtSource> extSourcesTable = getExtSources.getTable();
		extSourcesTable.setStyleName("perun-table");
		ScrollPanel scrollTable = new ScrollPanel(extSourcesTable);
		scrollTable.addStyleName("perun-tableScrollPanel");

		// put page into scroll panel
		mainPage.add(menu);
		mainPage.setCellHeight(menu, "30px");
		mainPage.add(scrollTable);

		session.getUiElements().resizePerunTable(scrollTable, 350, this);
		this.contentWidget.setWidget(mainPage);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return  SmallIcons.INSTANCE.worldIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 929;
		int result = 1;
		result = prime * result * 135;
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
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "External sources", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "extsrc";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public ExtSourcesTabItem load(Map<String, String> parameters)
	{
		return new ExtSourcesTabItem();
	}
}
