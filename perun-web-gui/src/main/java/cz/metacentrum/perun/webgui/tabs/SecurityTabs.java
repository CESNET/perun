package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamBlacklistTabItem;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamMembersTabItem;
import cz.metacentrum.perun.webgui.tabs.securitytabs.SecurityTeamSelectTabItem;

import java.util.Map;

/**
 * Pages, which are in SecurityTeam admin
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */

public class SecurityTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "sec";

	/**
	 * Creates a new instance of pages
	 */
	public SecurityTabs(){}

	/**
	 * Loads the page
	 *
	 * @return true on success, false otherwise
	 */
	public boolean loadTab(String tab, Map<String, String> parameters) {

		if(tab == null){
			return false;
		}
		// if active
		boolean open = ("1".equals(parameters.get("active")));

		// for vos list do not bother getting object
		if (tab.equals(SecurityTeamSelectTabItem.URL)) {
			session.getTabManager().addTab(new SecurityTeamSelectTabItem(), open);
			return true;
		}

		// which page?
		if (tab.equals(SecurityTeamDetailTabItem.URL))
		{
			session.getTabManager().addTab(SecurityTeamDetailTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(SecurityTeamBlacklistTabItem.URL))
		{
			session.getTabManager().addTab(SecurityTeamBlacklistTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(SecurityTeamMembersTabItem.URL))
		{
			session.getTabManager().addTab(SecurityTeamMembersTabItem.load(parameters), open);
			return true;
		}

		return false;

	}

}
