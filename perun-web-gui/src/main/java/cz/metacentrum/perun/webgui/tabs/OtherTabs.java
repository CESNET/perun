package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;

import java.util.Map;

/**
 * Other tabs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class OtherTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "other";

	/**
	 * Creates a new instance of pages
	 */
	public OtherTabs(){}

	/**
	 * Loads the page
	 */
	public boolean loadTab(String tab, Map<String, String> parameters) {
		if(tab == null){
			return false;
		}

		// if active
		boolean open = ("1".equals(parameters.get("active")));




		// user home
		if (tab.equals(PageNotFoundTabItem.URL))
		{
			session.getTabManager().addTab(new PageNotFoundTabItem(), open);
			return true;
		}

		return false;

	}

}
