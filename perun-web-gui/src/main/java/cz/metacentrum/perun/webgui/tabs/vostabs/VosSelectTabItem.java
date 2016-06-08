package cz.metacentrum.perun.webgui.tabs.vostabs;

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
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Page, which displays the Perun VOs for users to select them
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class VosSelectTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Select VO");
	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

	/**
	 * Creates a tab instance
	 */
	public VosSelectTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// Get vos request
		final GetVos getVos = new GetVos();
		getVos.setCheckable(false);

		TabMenu tabMenu = new TabMenu();
		tabMenu.addWidget(UiElements.getRefreshButton(this));
		// add menu to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		if (session.isVoAdmin()) {
			// do not display to VO observer
			tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createVo(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					session.getTabManager().addTabToCurrentTab(new CreateVoTabItem());
				}
			}));
		}

		// filter
		tabMenu.addFilterWidget(new ExtendedSuggestBox(getVos.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				getVos.filterTable(text);
			}
		}, buttonTranslation.filterVo());

		tabMenu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
		tabMenu.addWidget(new HTML("<strong>Please select VO you want to manage.</strong>"));

		final TabItem tab = this;
		// get the table with custom onclick
		CellTable<VirtualOrganization> table = getVos.getTable(new FieldUpdater<VirtualOrganization, VirtualOrganization>() {
			@Override
			public void update(int i, VirtualOrganization virtualOrganization, VirtualOrganization virtualOrganization2) {
				session.getTabManager().addTab(new VoDetailTabItem(virtualOrganization));
				session.getTabManager().closeTab(tab, false);
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		// add the table to the main panel
		firstTabPanel.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, 0, this);

		this.contentWidget.setWidget(firstTabPanel);

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
		final int prime = 1621;
		int result = 1;
		result = prime * result + 341;
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
		// update links in both
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.VO_ADMIN, "Select VO", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin() || session.isVoObserver()) {
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

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public VosSelectTabItem load(Map<String, String> parameters) {
		return new VosSelectTabItem();
	}

}
