package cz.metacentrum.perun.webgui.tabs;

/**
 * Interface for a tab with its own URL which appears in address line
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public interface TabItemWithUrl extends TabItem {

	/**
	 * Return only part with its own code name
	 * Eg. "detail" for VoDetailTabItem
	 *
	 * @return
	 */
	public String getUrl();

	/**
	 * Returns built URL with all parameters
	 *
	 * @return
	 */
	public String getUrlWithParameters();
}
