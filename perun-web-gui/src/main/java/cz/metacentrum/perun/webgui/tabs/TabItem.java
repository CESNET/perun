package cz.metacentrum.perun.webgui.tabs;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for tab, which can be shown in the main tab panel via the TabManager.
 * The attributes of the tab are sent via the constructor.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public interface TabItem{

	/**
	 * Prepares the widget.
	 * It's called
	 *  - when the tab is opened
	 *  - when user clicks on the tab to reload it
	 *  - when user changes extended info settings (showing/hiding IDs)
	 *
	 * @return the tab contents - should be something like: return getWidget();
	 */
	public Widget draw();

	/**
	 * Only returns the widget with tab contents.
	 * It shouldn't do anything else.
	 *
	 * @return the widget
	 */
	public Widget getWidget();

	/**
	 * Returns the tab title as a WIDGET (something like GWT Label)
	 * @return the title
	 */
	public Widget getTitle();

	/**
	 * Returns the icon image resource
	 * @return the icon
	 */
	public ImageResource getIcon();

	/**
	 * When the tabs are the same - the same arguments are passed, it returns false and the tab is not created again.
	 * JsonCallbackEvents can't be compared, we ignore them. We are comparing only IDs and objects.
	 *
	 * @return
	 */
	public boolean equals(Object tabItem);

	/**
	 * When multiple instances enabled, it will always open the new tab
	 * @return
	 */
	public boolean multipleInstancesEnabled();

	/**
	 * Called when a tab is opened (after redraw, if redrawing)
	 */
	public void open();

	/**
	 * Determines if user is authorized to view tab, it's automatically called when
	 * adding tab in TabManager.
	 *
	 * @return true - is authorized / false - not authorized
	 */
	public boolean isAuthorized();

	/**
	 * Checks, whether is the tab prepared and can be used for authorization and drawing.
	 *
	 * @return true when is prepared
	 */
	public boolean isPrepared();

	/**
	 * Whether tab should refresh parent on close. This is just a hint, it can be overridden in
	 * button action implementation (like events to close tab on action finish, where refreshing parent is default).
	 *
	 * @return TRUE refresh / FALSE dont refresh
	 */
	public boolean isRefreshParentOnClose();

	/**
	 * Performs tab specific action on closing
	 */
	public void onClose();

}
