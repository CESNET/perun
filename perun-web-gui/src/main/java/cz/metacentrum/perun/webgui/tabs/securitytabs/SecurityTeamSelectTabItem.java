package cz.metacentrum.perun.webgui.tabs.securitytabs;

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
import cz.metacentrum.perun.webgui.json.securityTeamsManager.DeleteSecurityTeam;
import cz.metacentrum.perun.webgui.json.securityTeamsManager.GetSecurityTeams;
import cz.metacentrum.perun.webgui.model.SecurityTeam;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.Map;

/**
 * Page, which displays the Perun SecurityTeams for users to select them
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class SecurityTeamSelectTabItem implements TabItem, TabItemWithUrl {

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
	private Label titleWidget = new Label("Select SecurityTeam");
	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

	/**
	 * Creates a tab instance
	 */
	public SecurityTeamSelectTabItem(){}

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
		final GetSecurityTeams teams = new GetSecurityTeams();

		if (!session.isPerunAdmin()) {
			teams.setCheckable(false);
		}

		// Events for reloading when finished
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(teams);

		TabMenu tabMenu = new TabMenu();
		// add menu to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		tabMenu.addWidget(UiElements.getRefreshButton(this));

		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, true, ButtonTranslation.INSTANCE.createSecurityTeam(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new CreateSecurityTeamTabItem());
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, buttonTranslation.deleteSecurityTeam());
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				final ArrayList<SecurityTeam> itemsToRemove = teams.getTableSelectedList();
				UiElements.showDeleteConfirm(itemsToRemove, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < itemsToRemove.size(); i++) {
							DeleteSecurityTeam request;
							if (i == itemsToRemove.size() - 1) {
								request = new DeleteSecurityTeam(JsonCallbackEvents.disableButtonEvents(removeButton, events));
							} else {
								request = new DeleteSecurityTeam(JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.deleteSecurityTeam(itemsToRemove.get(i).getId(), false);
						}
					}
				});

			}
		});
		tabMenu.addWidget(removeButton);

		// filter
		tabMenu.addFilterWidget(new ExtendedSuggestBox(teams.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				teams.filterTable(text);
			}
		}, buttonTranslation.filterSecurityTeam());

		tabMenu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
		tabMenu.addWidget(new HTML("<strong>Please select Team you want to manage.</strong>"));

		final TabItem tab = this;
		// get the table with custom onclick
		CellTable<SecurityTeam> table = teams.getTable(new FieldUpdater<SecurityTeam, String>() {
			@Override
			public void update(int i, SecurityTeam securityTeam, String string) {
				session.getTabManager().addTab(new SecurityTeamDetailTabItem(securityTeam));
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
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
		return SmallIcons.INSTANCE.userPoliceEnglandIcon();
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
		session.getUiElements().getMenu().openMenu(MainMenu.SECURITY_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.SECURITY_ADMIN, "Select SecurityTeam", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isSecurityAdmin()) {
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
		return SecurityTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
	}

	static public SecurityTeamSelectTabItem load(Map<String, String> parameters) {
		return new SecurityTeamSelectTabItem();
	}

}
