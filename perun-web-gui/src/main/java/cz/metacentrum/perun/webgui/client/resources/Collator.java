package cz.metacentrum.perun.webgui.client.resources;

/**
 * Provides localized comparing of strings 
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
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
	
}