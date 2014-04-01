package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for Cabinet API: Category
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class Category extends JavaScriptObject {

	protected Category() {}

	/**
	 * Returns object ID
	 * @return ID
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Returns Category name
		 * @return name
		 */
		public final native String getName() /*-{
			return this.name;
		}-*/;

		/**
		 * Returns category rank
		 * @return rank
		 */
		public final native double getRank() /*-{
			return this.rank;
		}-*/;

		/**
		 * Sets category rank
		 *
		 * @param rank double value
		 */
		public final native void setRank(double rank) /*-{
			this.rank = rank;
		}-*/;


		/**
		 * Returns Perun specific type of object
		 *
		 * @return type of object
		 */
		public final native String getObjectType() /*-{
			if (!this.objecttype) {
			return "JavaScriptObject"
			}
			return this.objecttype;
		}-*/;

		/**
		 * Sets Perun specific type of object
		 *
		 * @param type type of object
		 */
		public final native void setObjectType(String type) /*-{
			this.objecttype = type;
		}-*/;

		/**
		 * Returns the status of this item in Perun system as String
		 * VALID, INVALID, SUSPENDED, EXPIRED, DISABLED
		 *
		 * @return string which defines item status
		 */
		public final native String getStatus() /*-{
			return this.status;
		}-*/;

		/**
		 * Compares to another object
		 * @param o Object to compare
		 * @return true, if they are the same
		 */
		public final boolean equals(Category o)
		{
			return o.getId() == this.getId();
		}
}
