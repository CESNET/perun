package cz.metacentrum.perun.webgui.client.mainmenu;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.attributestabs.AttributeDefinitionsTabItem;
import cz.metacentrum.perun.webgui.tabs.cabinettabs.PublicationsTabItem;
import cz.metacentrum.perun.webgui.tabs.cabinettabs.UsersPublicationsTabItem;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.*;
import cz.metacentrum.perun.webgui.tabs.groupstabs.*;
import cz.metacentrum.perun.webgui.tabs.perunadmintabs.*;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamBlacklistTabItem;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamMembersTabItem;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamSelectTabItem;
import cz.metacentrum.perun.webgui.tabs.servicestabs.ServicePackagesTabItem;
import cz.metacentrum.perun.webgui.tabs.servicestabs.ServicesTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.*;
import cz.metacentrum.perun.webgui.tabs.vostabs.*;
import cz.metacentrum.perun.webgui.widgets.AdvancedStackPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for main left menu - contains sections: Perun admin, VO admin, Group admin, Facility admin, User
 * Contains MainMenuItems
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MainMenu {

	private AdvancedStackPanel menuStackPanel = new AdvancedStackPanel();
	private PerunWebSession session = PerunWebSession.getInstance();

	private Map<Integer, MainMenuSection> sectionsMap = new HashMap<Integer, MainMenuSection>();
	private Map<Integer, Integer> sectionsIds = new HashMap<Integer, Integer>();

	static public final int PERUN_ADMIN = 0;
	static public final int SECURITY_ADMIN = 1;
	static public final int VO_ADMIN = 2;
	static public final int GROUP_ADMIN = 3;
	static public final int FACILITY_ADMIN = 4;
	static public final int USER = 5;
	static public final int MENU_WIDTH = 203;

	/**
	 * New menu instance
	 */
	public MainMenu(){

		menuStackPanel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {

				int menuPosition = menuStackPanel.getSelectedIndex();
				for(int i = 0; i < 6; i++){
					if(sectionsIds.containsKey(i)){
						if(sectionsIds.get(i) == menuPosition)
		{
			// THIS ENSURE ALL LINKS IN MENU ARE ALWAYS CURRENT WHEN MENU IS OPENED
			updateHeaders();
			updateLinks(i);

			// IF NO TAB IS OPENED - OPEN ITS DEFAULT PAGE
			if (session.getTabManager().getActiveTab() == null) {
				// skip for perun admin menu
				if (PERUN_ADMIN != i) {
					MainMenuSection menuSec = sectionsMap.get(i);
					if (menuSec != null) {
						session.getTabManager().addTab(menuSec.getTabItem());
					}
				}
			}

			return;
		}
					}
				}
			}
		});

	}

	/**
	 * This MUST be called when GUI is loading
	 */
	public void prepare(){

		// Clears previous
		this.sectionsMap.clear();
		this.sectionsIds.clear();
		this.menuStackPanel.clear();

		// Stack panel settings
		menuStackPanel.setWidth(MENU_WIDTH+"px");
		menuStackPanel.addStyleName("menuStackPanel");

		LargeIcons iconsLarge = LargeIcons.INSTANCE;
		int i = 0;

		// SECTION PERUN ADMIN
		if(session.isPerunAdmin())
		{
			MainMenuSection perunAdmin = new MainMenuSection("Perun admin", new VosTabItem(), iconsLarge.perunIcon(), PERUN_ADMIN);

			this.sectionsMap.put(PERUN_ADMIN, perunAdmin);
			this.sectionsIds.put(PERUN_ADMIN, i);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					buildPerunAdminMenu();
				}
			});
			i++;
			menuStackPanel.add(perunAdmin.getWidget(), perunAdmin.getHeader(), true);
		}

		// SECTION PERUN ADMIN
		if(session.isSecurityAdmin())
		{
			MainMenuSection secAdmin = new MainMenuSection("Security admin", new SecurityTeamSelectTabItem(), iconsLarge.userPoliceEnglandIcon(), PERUN_ADMIN);

			this.sectionsMap.put(SECURITY_ADMIN, secAdmin);
			this.sectionsIds.put(SECURITY_ADMIN, i);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					buildSecurityAdminMenu();
				}
			});
			i++;
			menuStackPanel.add(secAdmin.getWidget(), secAdmin.getHeader(), true);
		}

		// SECTION VO ADMIN
		if(session.isVoAdmin() || session.isVoObserver() || session.isPerunAdmin())
		{
			MainMenuSection voAdmin = new MainMenuSection("VO manager", new VosSelectTabItem(), iconsLarge.buildingIcon(), VO_ADMIN);
			this.sectionsMap.put(VO_ADMIN, voAdmin);
			this.sectionsIds.put(VO_ADMIN, i);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					buildVoAdminMenu();
				}
			});

			i++;
			menuStackPanel.add(voAdmin.getWidget(), voAdmin.getHeader(), true);
		}

		// SECTION GROUP ADMIN
		if(session.isGroupAdmin() || session.isVoAdmin() || session.isPerunAdmin())
		{
			MainMenuSection groupAdmin = new MainMenuSection("Group manager", new GroupsTabItem(null), iconsLarge.groupIcon(), GROUP_ADMIN);

			this.sectionsMap.put(GROUP_ADMIN, groupAdmin);
			this.sectionsIds.put(GROUP_ADMIN, i);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					buildGroupAdminMenu();
				}
			});

			i++;
			menuStackPanel.add(groupAdmin.getWidget(), groupAdmin.getHeader(), true);
		}

		// SECTION FACILITY ADMIN
		if(session.isFacilityAdmin() || session.isPerunAdmin())
		{
			MainMenuSection facilityAdmin = new MainMenuSection("Facility manager", new FacilitiesSelectTabItem(), iconsLarge.databaseServerIcon(), FACILITY_ADMIN);

			this.sectionsMap.put(FACILITY_ADMIN, facilityAdmin);
			this.sectionsIds.put(FACILITY_ADMIN, i);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					buildFacilityAdminMenu();
				}
			});

			i++;
			menuStackPanel.add(facilityAdmin.getWidget(), facilityAdmin.getHeader(), true);
		}

		// SECTION USER - ALWAYS
		MainMenuSection user = new MainMenuSection("User", null, iconsLarge.userGrayIcon(), USER);
		this.sectionsMap.put(USER, user);
		this.sectionsIds.put(USER, i);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				buildUserMenu();
			}
		});
		i++;
		menuStackPanel.add(user.getWidget(), user.getHeader(), true);

	}

	/**
	 * Open menu by role WITHOUT any refresh
	 *
	 * @param role menu to open
	 * @return true if menu present and opened
	 */
	public boolean openMenu(int role) {
		return openMenu(role, false);
	}

	/**
	 * Open menu by role WITH possibility of updating links
	 *
	 * @param role menu to open
	 * @param update TRUE if links should be updated, false otherwise
	 * @return true if opened, false otherwise
	 */
	public boolean openMenu(int role, boolean update) {

		if(!sectionsMap.containsKey(role)){
			return false;
		}
		menuStackPanel.showStack(this.sectionsIds.get(role));
		if (update) {
			updateHeaders();
			updateLinks(role);
		}
		return true;

	}

	/**
	 * Get whole menu widget
	 *
	 * @return menu widget
	 */
	public Widget getWidget()
	{
		return this.menuStackPanel;
	}

	/**
	 * Updates text in menu section headers
	 */
	private void updateHeaders(){

		// for all sections
		for(Map.Entry<Integer, MainMenuSection> entry : sectionsMap.entrySet()){
			MainMenuSection section = entry.getValue();
			menuStackPanel.setStackText(sectionsIds.get(entry.getKey()), section.getHeader(), true);
		}
	}

	/**
	 * Update links in menu sections by role
	 * @param role menu to update
	 */
	public void updateLinks(int role) {

		// update the section's links
		switch (role) {
			case PERUN_ADMIN: buildPerunAdminMenu();
			case SECURITY_ADMIN: buildSecurityAdminMenu();
			case VO_ADMIN: buildVoAdminMenu();
			case GROUP_ADMIN: buildGroupAdminMenu();
			case FACILITY_ADMIN: buildFacilityAdminMenu();
			case USER: buildUserMenu();
		}

	}

	/**
	 * Update link is all menu sections
	 */
	public void updateLinks() {

		// menu links
		buildPerunAdminMenu();
		buildSecurityAdminMenu();
		buildVoAdminMenu();
		buildGroupAdminMenu();
		buildFacilityAdminMenu();
		buildUserMenu();
		updateHeaders();

	}

	/**
	 * Rebuild whole VO ADMIN menu
	 */
	private void buildVoAdminMenu()
	{
		MainMenuSection menu = sectionsMap.get(VO_ADMIN);
		if(menu == null) return;
		menu.clear();

		VirtualOrganization vo = session.getActiveVo();
		TabItemWithUrl detail = null;
		TabItemWithUrl admins = null;
		TabItemWithUrl groups = null;
		TabItemWithUrl members = null;
		TabItemWithUrl resources = null;
		TabItemWithUrl extsources = null;
		TabItemWithUrl settings = null;
		TabItemWithUrl applications = null;
		TabItemWithUrl applicationForm = null;
		TabItemWithUrl propagations = null;
		TabItemWithUrl tags = null;
		String voName = "VO overview";

		if(vo != null){
			voName = vo.getName();
			detail = new VoDetailTabItem(session.getActiveVo());
			admins = new VoManagersTabItem(session.getActiveVo());
			groups = new VoGroupsTabItem(session.getActiveVo());
			members = new VoMembersTabItem(session.getActiveVo());
			resources = new VoResourcesTabItem(session.getActiveVo());
			extsources = new VoExtSourcesTabItem(session.getActiveVo());
			settings = new VoSettingsTabItem(session.getActiveVo());
			applications = new VoApplicationsTabItem(session.getActiveVo());
			applicationForm = new VoApplicationFormSettingsTabItem(session.getActiveVo());
			propagations = new VoResourcesPropagationsTabItem(session.getActiveVo());
			tags = new VoResourcesTagsTabItem(session.getActiveVo());
		}
		menuStackPanel.setStackText(sectionsIds.get(VO_ADMIN), menu.getHeader(), true);

		menu.addItem(new MainMenuItem("Select VO", new VosSelectTabItem(), SmallIcons.INSTANCE.buildingIcon()));
		menu.addSplitter();

		menu.addItem(new MainMenuItem(voName, detail, SmallIcons.INSTANCE.buildingIcon()));
		menu.addItem(new MainMenuItem("Members", members, SmallIcons.INSTANCE.userGreenIcon()));
		menu.addItem(new MainMenuItem("Groups", groups, SmallIcons.INSTANCE.groupIcon()));
		menu.addItem(new MainMenuItem("Resources", resources, SmallIcons.INSTANCE.serverGroupIcon()));
		menu.addItem(new MainMenuItem("Applications", applications, SmallIcons.INSTANCE.applicationFromStorageIcon()));

		menu.setDisplayAdvanced(getAdvancedStateFromBrowser(VO_ADMIN+"", menu));

		if (menu.isDisplayAdvanced()) {
			menu.addItem(new MainMenuItem("Application form", applicationForm, SmallIcons.INSTANCE.applicationFormIcon()));
			menu.addItem(new MainMenuItem("Resource tags", tags, SmallIcons.INSTANCE.tagOrangeIcon()));
			menu.addItem(new MainMenuItem("Resources state", propagations, SmallIcons.INSTANCE.arrowRightIcon()));
			menu.addItem(new MainMenuItem("Settings", settings, SmallIcons.INSTANCE.settingToolsIcon()));
			menu.addItem(new MainMenuItem("Managers", admins, SmallIcons.INSTANCE.administratorIcon()));
			menu.addItem(new MainMenuItem("External sources", extsources, SmallIcons.INSTANCE.worldIcon()));
			menu.addAdvancedLink(vo != null);
		} else {
			menu.addAdvancedLink(vo != null);
		}

	}

	/**
	 * Rebuild whole SECURITY ADMIN menu
	 */
	private void buildSecurityAdminMenu()
	{
		MainMenuSection menu = sectionsMap.get(SECURITY_ADMIN);
		if(menu == null) return;
		menu.clear();

		SecurityTeam team = session.getActiveSecurityTeam();

		menu.addItem(new MainMenuItem("Select SecurityTeam", new SecurityTeamSelectTabItem(), SmallIcons.INSTANCE.userPoliceEnglandIcon()));
		menu.addSplitter();

		if (team != null) {

			menu.addItem(new MainMenuItem(team.getName(), new SecurityTeamDetailTabItem(team), SmallIcons.INSTANCE.userPoliceEnglandIcon()));
			menu.addItem(new MainMenuItem("Members", new SecurityTeamMembersTabItem(team), SmallIcons.INSTANCE.userGreenIcon()));
			menu.addItem(new MainMenuItem("Blacklist", new SecurityTeamBlacklistTabItem(team), SmallIcons.INSTANCE.firewallIcon()));

		} else {

			menu.addItem(new MainMenuItem("Detail", null, SmallIcons.INSTANCE.userPoliceEnglandIcon()));
			menu.addItem(new MainMenuItem("Members", null, SmallIcons.INSTANCE.userGreenIcon()));
			menu.addItem(new MainMenuItem("Blacklist", null, SmallIcons.INSTANCE.firewallIcon()));

		}

		menuStackPanel.setStackText(sectionsIds.get(SECURITY_ADMIN), menu.getHeader(), true);

	}


	/**
	 * Rebuild whole PERUN ADMIN menu
	 */
	private void buildPerunAdminMenu()
	{
		MainMenuSection menu = sectionsMap.get(PERUN_ADMIN);
		if(menu == null) return;
		menu.clear();
		menu.addItem(new MainMenuItem("Virtual organizations", new VosTabItem(), SmallIcons.INSTANCE.buildingIcon()));
		menu.addItem(new MainMenuItem("Facilities", new FacilitiesTabItem(), SmallIcons.INSTANCE.databaseServerIcon()));
		menu.addItem(new MainMenuItem("Services", new ServicesTabItem(), SmallIcons.INSTANCE.trafficLightsIcon()));
		menu.addItem(new MainMenuItem("Services packages", new ServicePackagesTabItem(), SmallIcons.INSTANCE.packageIcon()));
		menu.addItem(new MainMenuItem("Attributes", new AttributeDefinitionsTabItem(), SmallIcons.INSTANCE.attributesDisplayIcon()));
		menu.addItem(new MainMenuItem("Users", new UsersTabItem(), SmallIcons.INSTANCE.userGrayIcon()));
		menu.addItem(new MainMenuItem("Publications", new PublicationsTabItem(), SmallIcons.INSTANCE.booksIcon()));
		menu.addItem(new MainMenuItem("Owners", new OwnersTabItem(), SmallIcons.INSTANCE.userSilhouetteIcon()));
		menu.addItem(new MainMenuItem("External sources", new ExtSourcesTabItem(), SmallIcons.INSTANCE.worldIcon()));
		menu.addItem(new MainMenuItem("Propagations", new PropagationsTabItem(), SmallIcons.INSTANCE.arrowRightIcon()));
		menu.addItem(new MainMenuItem("Searcher", new SearcherTabItem(), SmallIcons.INSTANCE.magnifierIcon()));
		menu.addItem(new MainMenuItem("Audit log", new AuditLogTabItem(), SmallIcons.INSTANCE.reportEditIcon()));
		menu.addItem(new MainMenuItem("Statistics", new StatisticsTabItem(), SmallIcons.INSTANCE.statisticsIcon()));

	}

	/**
	 * Rebuild whole GROUP ADMIN menu
	 */
	private void buildGroupAdminMenu() {

		MainMenuSection menu = sectionsMap.get(GROUP_ADMIN);
		if(menu == null) return;
		Group group = session.getActiveGroup();
		menu.clear();

		TabItemWithUrl detail = null;
		TabItemWithUrl members = null;
		TabItemWithUrl admins = null;
		TabItemWithUrl subgroups = null;
		TabItemWithUrl relations = null;
		TabItemWithUrl resources = null;
		TabItemWithUrl settings = null;
		TabItemWithUrl applications = null;
		TabItemWithUrl applicationForm = null;
		TabItemWithUrl extSources = null;
		String groupName = "Group overview";

		if(group != null){
			groupName = group.getName();
			detail = new GroupDetailTabItem(group);
			members =  new GroupMembersTabItem(group);
			resources = new GroupResourcesTabItem(group);
			settings = new GroupSettingsTabItem(group);
			if (!group.isCoreGroup()) {
				// DO NOT allow applications for members/admins groups
				applications = new GroupApplicationsTabItem(group);
				applicationForm = new GroupApplicationFormSettingsTabItem(group);
				admins = new GroupManagersTabItem(group);
				subgroups = new SubgroupsTabItem(group);
				relations = new GroupRelationsTabItem(group);
				extSources = new GroupExtSourcesTabItem(group);
			}
		}

		menu.addItem(new MainMenuItem("Select group", new GroupsTabItem(session.getActiveVo()), SmallIcons.INSTANCE.groupIcon()));
		menu.addSplitter();

		menuStackPanel.setStackText(sectionsIds.get(GROUP_ADMIN), menu.getHeader(), true);

		menu.addItem(new MainMenuItem(groupName, detail, SmallIcons.INSTANCE.groupIcon()));
		menu.addItem(new MainMenuItem("Members", members, SmallIcons.INSTANCE.userGreenIcon()));
		menu.addItem(new MainMenuItem("Subgroups", subgroups, SmallIcons.INSTANCE.groupGoIcon()));
		menu.addItem(new MainMenuItem("Relations", relations, SmallIcons.INSTANCE.groupLinkIcon()));
		menu.addItem(new MainMenuItem("Resources", resources, SmallIcons.INSTANCE.serverGroupIcon()));
		menu.addItem(new MainMenuItem("Applications", applications , SmallIcons.INSTANCE.applicationFromStorageIcon()));

		menu.setDisplayAdvanced(getAdvancedStateFromBrowser(GROUP_ADMIN+"", menu));

		if (menu.isDisplayAdvanced()) {
			menu.addItem(new MainMenuItem("Application form", applicationForm , SmallIcons.INSTANCE.applicationFormIcon()));
			menu.addItem(new MainMenuItem("Settings", settings, SmallIcons.INSTANCE.settingToolsIcon()));
			menu.addItem(new MainMenuItem("Managers", admins, SmallIcons.INSTANCE.administratorIcon()));
			menu.addItem(new MainMenuItem("External sources", extSources, SmallIcons.INSTANCE.worldIcon()));
			menu.addAdvancedLink(group != null);
		} else {
			menu.addAdvancedLink(group != null);
		}

	}

	/**
	 * Rebuild whole FACILITY ADMIN menu
	 */
	private void buildFacilityAdminMenu()
	{
		MainMenuSection menu = sectionsMap.get(FACILITY_ADMIN);
		if(menu == null) return;

		menu.clear();

		Facility facility = session.getActiveFacility();

		TabItemWithUrl detail = null;
		TabItemWithUrl resources = null;
		TabItemWithUrl admins = null;
		TabItemWithUrl status = null;
		TabItemWithUrl destinations = null;
		TabItemWithUrl settings = null;
		TabItemWithUrl hosts = null;
		TabItemWithUrl allowedGroups = null;
		TabItemWithUrl owners = null;
		TabItemWithUrl security = null;
		TabItemWithUrl black = null;
		TabItemWithUrl allPropagations = new FacilitiesPropagationsTabItem();
		String facilityName = "Facility overview";

		if(facility != null){
			facilityName = facility.getName();
			detail = new FacilityDetailTabItem(facility);
			resources = new FacilityResourcesTabItem(facility);
			admins = new FacilityManagersTabItem(facility);
			status = new FacilityStatusTabItem(facility);
			destinations = new FacilityDestinationsTabItem(facility);
			settings = new FacilitySettingsTabItem(facility);
			hosts = new FacilityHostsTabItem(facility);
			allowedGroups = new FacilityAllowedGroupsTabItem(facility);
			security = new FacilitySecurityTeamsTabItem(facility);
			owners = new FacilityOwnersTabItem(facility);
			black = new FacilityBlacklistTabItem(facility);
		}

		menu.addItem(new MainMenuItem("Select facility", new FacilitiesSelectTabItem(), SmallIcons.INSTANCE.databaseServerIcon()));
		menu.addSplitter();

		menu.addItem(new MainMenuItem(facilityName, detail, SmallIcons.INSTANCE.databaseServerIcon()));
		menu.addItem(new MainMenuItem("Resources", resources, SmallIcons.INSTANCE.serverGroupIcon()));
		menu.addItem(new MainMenuItem("Allowed groups", allowedGroups, SmallIcons.INSTANCE.groupIcon()));
		menu.addItem(new MainMenuItem("Services status", status, SmallIcons.INSTANCE.serverInformationIcon()));
		menu.addItem(new MainMenuItem("Services settings", settings, SmallIcons.INSTANCE.settingToolsIcon()));

		menu.setDisplayAdvanced(getAdvancedStateFromBrowser(FACILITY_ADMIN+"", menu));

		if (menu.isDisplayAdvanced()) {
			menu.addItem(new MainMenuItem("Services destinations", destinations, SmallIcons.INSTANCE.serverGoIcon()));
			menu.addItem(new MainMenuItem("Hosts", hosts, SmallIcons.INSTANCE.serverIcon()));
			menu.addItem(new MainMenuItem("Managers", admins, SmallIcons.INSTANCE.administratorIcon()));
			menu.addItem(new MainMenuItem("Security teams", security, SmallIcons.INSTANCE.userPoliceEnglandIcon()));
			menu.addItem(new MainMenuItem("Blacklist", black, SmallIcons.INSTANCE.firewallIcon()));
			menu.addItem(new MainMenuItem("Owners", owners, SmallIcons.INSTANCE.userSilhouetteIcon()));
			menu.addItem(new MainMenuItem("All facilities states", allPropagations, SmallIcons.INSTANCE.arrowRightIcon()));
			menu.addAdvancedLink(facility != null);
		} else {
			menu.addAdvancedLink(facility != null);
		}

	}

	/**
	 * Rebuild whole USER menu
	 */
	private void buildUserMenu() {

		MainMenuSection menu = sectionsMap.get(USER);
		if(menu == null) return;

		// !! menu link must lead always only to "OWN ROLE" of logged user !!
		User user = session.getActiveUser();

		menu.clear();

		TabItemWithUrl changer = null;
		TabItemWithUrl detail = null;
		TabItemWithUrl settings = null;
		TabItemWithUrl resources = null;
		TabItemWithUrl authentications = null;
		TabItemWithUrl applications = null;
		TabItemWithUrl publications = null;
		TabItemWithUrl services = null;
		TabItemWithUrl sponsored = null;

		if (user != null) {
			detail = new SelfDetailTabItem(user);
			settings = new SelfVosTabItem(user);
			resources = new SelfResourcesSettingsTabItem(user);
			authentications = new SelfAuthenticationsTabItem(user);
			if (!user.isServiceUser()) {
				// publications can be reported by normal people only
				publications = new UsersPublicationsTabItem(user);
			}
			applications = new SelfApplicationsTabItem(user);
			if (session.getEditableUsers().size() > 1) {
				services = new SelfServiceUsersTabItem(user);
			}
			if (session.getEditableSponsoredUsers().size() > 0) {
				sponsored = new SelfSponsoredUsersTabItem(user);
			}
		} else {
			detail = new IdentitySelectorTabItem();
		}

		// display user changer for PerunAdmin or user with service identities
		if (session.isPerunAdmin() || session.getEditableUsers().size() > 1) {
			changer = new IdentitySelectorTabItem();
		}
		menu.addItem(new MainMenuItem("Select identity", changer, SmallIcons.INSTANCE.userGrayIcon()));
		menu.addSplitter();

		menu.setTabItem(detail);
		menu.addItem(new MainMenuItem((user != null) ? user.getFullNameWithTitles() : "My profile", detail, SmallIcons.INSTANCE.userGrayIcon()));
		menu.addItem(new MainMenuItem("VO settings", settings, SmallIcons.INSTANCE.buildingIcon()));
		menu.addItem(new MainMenuItem("Resources settings", resources, SmallIcons.INSTANCE.settingToolsIcon()));
		menu.addItem(new MainMenuItem("Authentication", authentications, SmallIcons.INSTANCE.keyIcon()));
		menu.addItem(new MainMenuItem("Publications", publications, SmallIcons.INSTANCE.booksIcon()));
		menu.addItem(new MainMenuItem("Applications", applications, SmallIcons.INSTANCE.applicationFromStorageIcon()));

		if (user.isServiceUser()) {
			menu.addItem(new MainMenuItem("Associated users", services, SmallIcons.INSTANCE.userRedIcon()));
			if (user.isSponsoredUser()) {
				menu.addItem(new MainMenuItem("Sponsors", sponsored, SmallIcons.INSTANCE.userGrayIcon()));
			}
		} else {
			menu.addItem(new MainMenuItem("Service identities", services, SmallIcons.INSTANCE.userRedIcon()));
			if (user.isSponsoredUser()) {
				menu.addItem(new MainMenuItem("Sponsors", sponsored, SmallIcons.INSTANCE.userGrayIcon()));
			} else {
				menu.addItem(new MainMenuItem("Sponsored users", sponsored, SmallIcons.INSTANCE.userGrayIcon()));
			}
		}

		menuStackPanel.setStackText(sectionsIds.get(USER), menu.getHeader(), true);

	}

	/**
	 * Shows/hides advance items in all menus
	 *
	 * @param advanced TRUE = show / FALSE = hide
	 */
	public void showAdvanced(boolean advanced) {

		for(Map.Entry<Integer, MainMenuSection> entry : sectionsMap.entrySet()) {
			saveAdvancedStateToBrowser(entry.getKey()+"", advanced);
			entry.getValue().setDisplayAdvanced(advanced);
			updateLinks(entry.getKey());
		}

	}

	/**
	 * Safely stores "advanced view" state to browser memory
	 *
	 * @param menu menu to save advanced view for
	 * @param displayAdvanced TRUE = display advanced / FALSE = hide advanced
	 */
	public void saveAdvancedStateToBrowser(String menu, boolean displayAdvanced) {

		try {
			Storage storage = Storage.getLocalStorageIfSupported();
			if (storage != null) {
				storage.setItem("urn:perun:gui:preferences:menu-"+menu, String.valueOf(displayAdvanced));
			}
		} catch (Exception ex) {
			// storage is blocked but supported
		}

	}

	/**
	 * Safely retrieve "advanced view" state from browser memory
	 * for selected menu section
	 *
	 * @param menu menu to get advanced view state for
	 * @return advanced view state if stored / JsonUtils.isExtendedInfoVisible() otherwise
	 */
	public boolean getAdvancedStateFromBrowser(String menu, MainMenuSection section) {

		try {
			Storage storage = Storage.getLocalStorageIfSupported();
			if (storage != null) {

				// we must also write so we could get an error if storage is supported but size set to 0.
				storage.setItem("urn:perun:gui:preferences:localStorageCheck", "checked");

				String value = storage.getItem("urn:perun:gui:preferences:menu-"+menu);
				if (value != null && !value.isEmpty()) {
					return Boolean.parseBoolean(value);
				} else {
					return JsonUtils.isExtendedInfoVisible();
				}
			}
		} catch (Exception ex) {
			// storage is blocked but supported
		}

		return section.isDisplayAdvanced();

	}

	/**
	 * Set different default tab, which is opened when section opens
	 * and no tab is opened in gui
	 *
	 * @param role menu to set default tab to.
	 * @param item tab to set
	 */
	public void setMenuTabItem(int role, TabItemWithUrl item) {

		if(!sectionsMap.containsKey(role)) return;

		MainMenuSection menuSection = sectionsMap.get(role);
		menuSection.setTabItem(item);

	}

}
