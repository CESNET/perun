package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToGroupTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.AddMemberToVoTabItem;
import cz.metacentrum.perun.webgui.tabs.memberstabs.MemberDetailTabItem;

import java.util.Map;

/**
 * Members tabs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class MembersTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "mem";

	/**
	 * Creates a new instance of pages
	 */
	public MembersTabs(){}

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


		if (tab.equals(AddMemberToGroupTabItem.URL)) {
			session.getTabManager().addTab(AddMemberToGroupTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(AddMemberToVoTabItem.URL)) {
			session.getTabManager().addTab(AddMemberToVoTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(MemberDetailTabItem.URL)) {
			session.getTabManager().addTab(MemberDetailTabItem.load(parameters), open);
			return true;
		}

		return false;



	}

}
