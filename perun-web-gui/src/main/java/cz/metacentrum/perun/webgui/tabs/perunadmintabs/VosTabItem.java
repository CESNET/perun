package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

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
import cz.metacentrum.perun.webgui.json.vosManager.DeleteVo;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.PerunAdminTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.vostabs.CreateVoTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Page, which displays the Perun VOs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VosTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("VOs");
	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

	/**
	 * Creates a tab instance
	 */
	public VosTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// Get vos request
		final GetVos getVos = new GetVos();

		// Events for reloading when finished
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(getVos);

		// create new VO button
		TabMenu tabMenu = new TabMenu();
		tabMenu.addWidget(UiElements.getRefreshButton(this));
		// add menu to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, buttonTranslation.createVo(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateVoTabItem());
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, buttonTranslation.deleteVo());
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				final ArrayList<VirtualOrganization> itemsToRemove = getVos.getTableSelectedList();
				UiElements.showDeleteConfirm(itemsToRemove, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<itemsToRemove.size(); i++ ) {
							DeleteVo request;
							if(i == itemsToRemove.size() - 1){
								request = new DeleteVo(JsonCallbackEvents.disableButtonEvents(removeButton, events));
							}else{
								request = new DeleteVo(JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.deleteVo(itemsToRemove.get(i).getId(), false);
						}
					}
				});

			}
		});
		tabMenu.addWidget(removeButton);

		// filter
		tabMenu.addFilterWidget(new ExtendedSuggestBox(getVos.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				getVos.filterTable(text);
			}
		}, buttonTranslation.filterVo());

		// get the table with custom onclick
		CellTable<VirtualOrganization> table = getVos.getTable(new FieldUpdater<VirtualOrganization, VirtualOrganization>() {
			@Override
			public void update(int index, VirtualOrganization object, VirtualOrganization value) {
				session.getTabManager().addTab(new VoDetailTabItem(object));
			}
		});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(getVos, table, removeButton);

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
		final int prime = 1471;
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

	public void open()
	{
		// update links
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Virtual organizations", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "vos";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public VosTabItem load(Map<String, String> parameters)
	{
		return new VosTabItem();
	}

}
