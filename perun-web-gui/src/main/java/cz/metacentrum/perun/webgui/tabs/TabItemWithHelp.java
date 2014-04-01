package cz.metacentrum.perun.webgui.tabs;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for a tab with help panel, which appears in the right part of the screen
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public interface TabItemWithHelp extends TabItem {

	/**
	 * Returns the help widget, usually a SimplePanel
	 *
	 * @return
	 */
	public Widget getHelpWidget();
}
