package cz.metacentrum.perun.webgui.client.resources;

import java.util.Comparator;

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


	/**
	 * Safely compares two strings using browser's locale settings.
	 *
	 * @param o1 string to compare
	 * @param o2 string to compare with
	 * @return comparison result used in comparators
	 */
	public static final native int nativeCompare(String o1, String o2) /*-{
		if (o1 == null && o2 != null) return -1;
		if (o2 == null && o1 != null) return 1;
		if (o1 == null && o2 == null) return 0;
		return o1.localeCompare(o2);
	}-*/;

	/**
	 * Return Comparator<String> which uses browser's localeCompare().
	 *
	 * @return localized Comparator<String>
	 */
	public static final Comparator<String> getNativeComparator() {
		return new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1 == null && o2 != null) return -1;
				if (o2 == null && o1 != null) return 1;
				if (o1 == null && o2 == null) return 0;
				return nativeCompare(o1, o2);
			}
		};
	}

}
