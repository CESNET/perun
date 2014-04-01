package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.cabinettabs.*;

import java.util.Map;

/**
 * Cabinet tabs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CabinetTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "cab";

	/**
	 * Creates a new instance of pages
	 */
	public CabinetTabs(){}

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


		if (tab.equals(AddAuthorTabItem.URL)) {
			session.getTabManager().addTab(AddAuthorTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(AddPublicationsTabItem.URL)) {
			session.getTabManager().addTab(AddPublicationsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(AllAuthorsTabItem.URL)) {
			session.getTabManager().addTab(AllAuthorsTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(AllCategoriesTabItem.URL)) {
			session.getTabManager().addTab(AllCategoriesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(AllPublicationsTabItem.URL)) {
			session.getTabManager().addTab(AllPublicationsTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(PublicationDetailTabItem.URL)) {
			session.getTabManager().addTab(PublicationDetailTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(PublicationsTabItem.URL)) {
			session.getTabManager().addTab(PublicationsTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(PublicationSystemsTabItem.URL)) {
			session.getTabManager().addTab(PublicationSystemsTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(UsersPublicationsTabItem.URL)) {
			session.getTabManager().addTab(UsersPublicationsTabItem.load(parameters), open);
			return true;
		}
		if (tab.equals(CreateThanksTabItem.URL)) {
			session.getTabManager().addTab(CreateThanksTabItem.load(parameters), open);
			return true;
		}

		return false;



	}

}
