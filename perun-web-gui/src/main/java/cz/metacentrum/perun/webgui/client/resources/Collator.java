package cz.metacentrum.perun.webgui.client.resources;

/**
 * Provides localized comparing of strings
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Collator {

	// instance
	private static final Collator instance = new Collator();

	/**
	 * Return instance of collator
	 *
	 * @return collator
	 */
	public static final Collator getInstance() {
		return instance;
	}

	/**
	 * Compares two strings in localized way
	 */
	public final native int compare(String o1, String o2) /*-{
		return o1.localeCompare(o2);
	}-*/;

	/**
	 * Compares two strings in localized way ignoring case
	 */
	public final native int compareIgnoreCase(String o1, String o2) /*-{
		return o1.toLocaleLowerCase().localeCompare(o2.toLocaleLowerCase());
	}-*/;

}
