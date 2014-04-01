package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceAssignedGroupsTabItem;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceAssignedServicesTabItem;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceSettingsTabItem;

import java.util.Map;

/**
 * Resources tabs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ResourcesTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "res";

	/**
	 * Creates a new instance of pages
	 */
	public ResourcesTabs(){}

	/**
	 * Loads the page
	 *
	 * @return true on success / false otherwise
	 */
	public boolean loadTab(final String tab, final Map<String, String> parameters) {

		if(tab == null){
			return false;
		}
		// if active
		boolean open = ("1".equals(parameters.get("active")));

		if (tab.equals(ResourceAssignedGroupsTabItem.URL)) {
			session.getTabManager().addTab(ResourceAssignedGroupsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(ResourceAssignedServicesTabItem.URL)) {
			session.getTabManager().addTab(ResourceAssignedServicesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(ResourceDetailTabItem.URL)) {
			session.getTabManager().addTab(ResourceDetailTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(ResourceSettingsTabItem.URL)) {
			session.getTabManager().addTab(ResourceSettingsTabItem.load(parameters), open);
			return true;
		}

		return false;



	}

}
