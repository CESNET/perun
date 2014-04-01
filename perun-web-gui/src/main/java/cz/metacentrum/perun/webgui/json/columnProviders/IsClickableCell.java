package cz.metacentrum.perun.webgui.json.columnProviders;

/**
 * Determine if cell should be clickable or not by authz
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface IsClickableCell<T> {

	/**
	 * Return TRUE if cell should be drawn as clickable
	 * based on privileges over the base object
	 *
	 * @return TRUE = clickable / False = draw as simple text
	 */
	public boolean isClickable(T object);

	/**
	 * Return destination link if set or empty string
	 *
	 * @return destination link or empty string
	 */
	public String linkUrl(T object);

}
