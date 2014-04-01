package cz.metacentrum.perun.webgui.json;


/**
 * The JSON request doesn't trigger the alert box if failed
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public interface HiddenJsonCallback {

	/**
	 * Whether should be the callback silent
	 * @return
	 */
	public boolean isHidden();

	/**
	 * Set true for not triggering the alert box if failed
	 * @param hidden
	 */
	public void setHidden(boolean hidden);
}
