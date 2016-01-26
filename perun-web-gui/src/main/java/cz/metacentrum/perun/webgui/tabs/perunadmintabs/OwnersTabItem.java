package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

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
import cz.metacentrum.perun.webgui.json.ownersManager.DeleteOwner;
import cz.metacentrum.perun.webgui.json.ownersManager.GetOwners;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.tabs.PerunAdminTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Owners Management for Perun Admin
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class OwnersTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Owners");

	/**
	 * Creates a tab instance
	 */
	public OwnersTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		// tab content
		final VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%", "100%");

		// horizontal menu
		TabMenu tabMenu = new TabMenu();
		tabMenu.addWidget(UiElements.getRefreshButton(this));

		final GetOwners owners = new GetOwners();

		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(owners);

		// create button
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createOwner(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateOwnerTabItem());
			}
		}));

		// remove button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteOwner());
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<Owner> itemsToRemove = owners.getTableSelectedList();
				String text = "Following owners will be removed.";
				UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<itemsToRemove.size(); i++ ) {
							DeleteOwner request;
							if(i == itemsToRemove.size() - 1){
								request = new DeleteOwner(JsonCallbackEvents.disableButtonEvents(removeButton, events));
							} else {
								request = new DeleteOwner(JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.deleteOwner(itemsToRemove.get(i).getId());
						}
					}
				});
			}}
			);
		tabMenu.addWidget(removeButton);

		tabMenu.addFilterWidget(new ExtendedSuggestBox(owners.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				owners.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterOwners());

		// adding menu to the page
		mainTab.add(tabMenu);
		mainTab.setCellHeight(tabMenu, "30px");


		CellTable<Owner> table = owners.getTable();
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel();
		sp.add(table);
		sp.addStyleName("perun-tableScrollPanel");

		mainTab.add(sp);

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(owners, table, removeButton);

		// resize perun table to correct size on screen
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
		return SmallIcons.INSTANCE.userSilhouetteIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 941;
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
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Owners", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "owners";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return PerunAdminTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public OwnersTabItem load(Map<String, String> parameters)
	{
		return new OwnersTabItem();
	}

}
