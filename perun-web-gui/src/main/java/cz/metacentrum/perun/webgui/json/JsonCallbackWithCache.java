package cz.metacentrum.perun.webgui.json;

/**
 * If the JsonCallback can use cache
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public interface JsonCallbackWithCache {

	/**
	 * Return true if cache is enabled
	 *
	 * @return true = enabled / false = disabled
	 */
	boolean isCacheEnabled();

	/**
	 * Enable using of cache
	 *
	 * @param cache true = enable / false = disable
	 */
	void setCacheEnabled(boolean cache);

}
