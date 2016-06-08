package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.groupstabs.*;

import java.util.Map;

/**
 * Groups
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class GroupsTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "grp";

	/**
	 * Creates a new instance of pages
	 */
	public GroupsTabs(){}

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

		if (tab.equals(GroupSettingsTabItem.URL)) {
			session.getTabManager().addTab(GroupSettingsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(GroupDetailTabItem.URL)) {
			session.getTabManager().addTab(GroupDetailTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(GroupMembersTabItem.URL)) {
			session.getTabManager().addTab(GroupMembersTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(GroupResourceRequiredAttributesTabItem.URL)) {
			session.getTabManager().addTab(GroupResourceRequiredAttributesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(GroupsTabItem.URL)) {
			session.getTabManager().addTab(GroupsTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(SubgroupsTabItem.URL)) {
			session.getTabManager().addTab(SubgroupsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(GroupManagersTabItem.URL)) {
			session.getTabManager().addTab(GroupManagersTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(GroupResourcesTabItem.URL)) {
			session.getTabManager().addTab(GroupResourcesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(GroupApplicationsTabItem.URL))
		{
			session.getTabManager().addTab(GroupApplicationsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(GroupApplicationFormSettingsTabItem.URL))
		{
			session.getTabManager().addTab(GroupApplicationFormSettingsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(GroupExtSourcesTabItem.URL))
		{
			session.getTabManager().addTab(GroupExtSourcesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(AddGroupExtSourceTabItem.URL))
		{
			session.getTabManager().addTab(AddGroupExtSourceTabItem.load(parameters), open);
			return true;
		}

		return false;

	}

}
