package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.perunadmintabs.*;

import java.util.Map;

/**
 * Perun admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class PerunAdminTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "perun";

	/**
	 * Creates a new instance of pages
	 */
	public PerunAdminTabs(){}

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

		if (tab.equals(VosTabItem.URL)) {
			session.getTabManager().addTab(new VosTabItem(), open);
			return true;
		}

		if (tab.equals(FacilitiesTabItem.URL)) {
			session.getTabManager().addTab(new FacilitiesTabItem(), open);
			return true;
		}

		if (tab.equals(AuditLogTabItem.URL)) {
			session.getTabManager().addTab(AuditLogTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(ExtSourcesTabItem.URL)) {
			session.getTabManager().addTab(ExtSourcesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(PropagationsTabItem.URL)) {
			session.getTabManager().addTab(PropagationsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(StatisticsTabItem.URL)) {
			session.getTabManager().addTab(StatisticsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(SearcherTabItem.URL)) {
			session.getTabManager().addTab(SearcherTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(OwnersTabItem.URL)) {
			session.getTabManager().addTab(OwnersTabItem.load(parameters), open);
			return true;
		}

		return false;

	}

}
