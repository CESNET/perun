package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for Host objects
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Host extends JavaScriptObject {

	protected Host() {}

	/**
	 * Get ID of host
	 *
	 * @return ID of host
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Get name of host
		 *
		 * @return name of host
		 */
		public final native String getName() /*-{
			return this.hostname;
		}-*/;

		/**
		 * Returns Perun specific type of object
		 *
		 * @return type of object
		 */
		public final native String getObjectType() /*-{
			if (!this.beanName) {
			return "JavaScriptObject"
			}
			return this.beanName;
		}-*/;

		/**
		 * Sets Perun specific type of object
		 *
		 * @param type type of object
		 */
		public final native void setObjectType(String type) /*-{
			this.beanName = type;
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
		public final boolean equals(Host o)
		{
			return o.getId() == this.getId();
		}
}
