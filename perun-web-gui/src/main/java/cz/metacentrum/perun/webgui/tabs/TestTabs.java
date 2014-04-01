package cz.metacentrum.perun.webgui.tabs;

import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.tabs.testtabs.*;

import java.util.Map;

/**
 * Tabs indended only for testing
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class TestTabs {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String URL = "test";

	/**
	 * Creates a new instance of pages
	 */
	public TestTabs(){}

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

		if (tab.equals(TestRtReportingTabItem.URL)) {
			session.getTabManager().addTab(TestRtReportingTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(TestAttributeTableTabItem.URL)) {
			session.getTabManager().addTab(TestAttributeTableTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(TestDataGridTabItem.URL)) {
			session.getTabManager().addTab(TestDataGridTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(TestDataGridAttributesTabItem.URL)) {
			session.getTabManager().addTab(TestDataGridAttributesTabItem.load(parameters), open);
			return true;
		}

		if (tab.equals(TestJSONParserTabItem.URL)) {
			session.getTabManager().addTab(TestJSONParserTabItem.load(parameters), open);
			return true;
		}

		return false;



	}

}
