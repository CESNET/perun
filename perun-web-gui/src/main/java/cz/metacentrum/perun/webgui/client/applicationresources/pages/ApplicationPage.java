package cz.metacentrum.perun.webgui.client.applicationresources.pages;

import com.google.gwt.user.client.ui.Composite;

/**
 * Application page
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public abstract class ApplicationPage extends Composite{

	/**
	 * User clicked in the menu and opened the page
	 */
	abstract public void menuClick();
}
