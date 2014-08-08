package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.registrartabs.ApplicationDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.*;

import java.util.Map;

/**
 * Pages, which are in VO admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class VosTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "vo";

	/**
	 * Creates a new instance of pages
	 */
	public VosTabs(){}

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
		if (tab.equals(VosSelectTabItem.URL)) {
			session.getTabManager().addTab(new VosSelectTabItem(), open);
			return true;
		}

		// which page?
		if (tab.equals(VoDetailTabItem.URL))
		{
			session.getTabManager().addTab(VoDetailTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoMembersTabItem.URL))
		{
			session.getTabManager().addTab(VoMembersTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoManagersTabItem.URL))
		{
			session.getTabManager().addTab(VoManagersTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoGroupsTabItem.URL))
		{
			session.getTabManager().addTab(VoGroupsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoResourcesTabItem.URL))
		{
			session.getTabManager().addTab(VoResourcesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoSettingsTabItem.URL))
		{
			session.getTabManager().addTab(VoSettingsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoExtSourcesTabItem.URL))
		{
			session.getTabManager().addTab(VoExtSourcesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoApplicationsTabItem.URL))
		{
			session.getTabManager().addTab(VoApplicationsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoApplicationFormSettingsTabItem.URL))
		{
			session.getTabManager().addTab(VoApplicationFormSettingsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(ApplicationDetailTabItem.URL))
		{
			session.getTabManager().addTab(ApplicationDetailTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoResourcesPropagationsTabItem.URL))
		{
			session.getTabManager().addTab(VoResourcesPropagationsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(VoResourcesTagsTabItem.URL))
		{
			session.getTabManager().addTab(VoResourcesTagsTabItem.load(parameters), open);
			return true;
		}

		return false;

	}

}
