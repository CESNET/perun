package cz.metacentrum.perun.webgui.tabs.securitytabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.securityTeamsManager.GetBlacklistWithDescription;
import cz.metacentrum.perun.webgui.json.securityTeamsManager.RemoveUserFromBlacklist;
import cz.metacentrum.perun.webgui.model.Pair;
import cz.metacentrum.perun.webgui.model.SecurityTeam;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.SecurityTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Page with blacklist of SecurityTeam
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class SecurityTeamBlacklistTabItem implements TabItem, TabItemWithUrl {

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
	private SecurityTeam securityTeam;
	private int securityTeamId;

	/**
	 * Creates a tab instance
	 * @param securityTeam SecurityTeam
	 */
	public SecurityTeamBlacklistTabItem(SecurityTeam securityTeam){
		this.securityTeam = securityTeam;
		securityTeamId = securityTeam.getId();
	}

	/**
	 * Creates a tab instance
	 * @param securityTeamId ID of security team
	 */
	public SecurityTeamBlacklistTabItem(int securityTeamId){
		this.securityTeamId = securityTeamId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				securityTeam = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.SECURITY_TEAM, securityTeamId, events).retrieveData();
	}

	public boolean isPrepared(){
		return securityTeam != null;
	}

	public Widget draw() {

		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(securityTeam.getName())+": "+"blacklist");

		// Get vos request
		final GetBlacklistWithDescription teams = new GetBlacklistWithDescription(PerunEntity.SECURITY_TEAM, securityTeamId);

		// Events for reloading when finished
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(teams);

		TabMenu tabMenu = new TabMenu();
		// add menu to the main panel
		firstTabPanel.add(tabMenu);
		firstTabPanel.setCellHeight(tabMenu, "30px");

		tabMenu.addWidget(UiElements.getRefreshButton(this));

		// do not display to sec admins only
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addUsersToBlacklist(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddUserToBlacklistTabItem(securityTeam), true);
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, buttonTranslation.removeSelectedUsersFromBlacklist());
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				final ArrayList<Pair<User,String>> itemsToRemove = teams.getTableSelectedList();
				UiElements.showDeleteConfirm(itemsToRemove, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i = 0; i < itemsToRemove.size(); i++) {
							RemoveUserFromBlacklist request;
							if (i == itemsToRemove.size() - 1) {
								request = new RemoveUserFromBlacklist(securityTeamId, JsonCallbackEvents.disableButtonEvents(removeButton, events));
							} else {
								request = new RemoveUserFromBlacklist(securityTeamId, JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.removeUserFromBlacklist(itemsToRemove.get(i).getLeft().getId());
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
		}, buttonTranslation.filterBlacklist());

		final TabItem tab = this;

		CellTable<Pair<User,String>> table;
		if (session.isPerunAdmin()) {
			// get the table with custom onclick
			table = teams.getTable(new FieldUpdater<Pair<User,String>, String>() {
				@Override
				public void update(int i, Pair<User,String> pair, String string) {
					session.getTabManager().addTab(new UserDetailTabItem(pair.getLeft()));
					session.getTabManager().closeTab(tab, false);
				}
			});
		} else {
			table = teams.getTable();
		}

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
		return SmallIcons.INSTANCE.firewallIcon();
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
		SecurityTeamBlacklistTabItem other = (SecurityTeamBlacklistTabItem) obj;
		if (securityTeamId != other.securityTeamId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.SECURITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(securityTeam, "Blacklist", "");
		if(securityTeam != null){
			session.setActiveSecurityTeam(securityTeam);
		} else {
			session.setActiveSecurityTeamId(securityTeamId);
		}
	}

	public boolean isAuthorized() {
		if (session.isSecurityAdmin(securityTeamId)) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "black";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return SecurityTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + securityTeamId;
	}

	static public SecurityTeamBlacklistTabItem load(Map<String, String> parameters) {
		int securityTeamId = Integer.parseInt(parameters.get("id"));
		return new SecurityTeamBlacklistTabItem(securityTeamId);
	}

}
