package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for ResourceTag object from Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ResourceTag extends JavaScriptObject {

	protected ResourceTag() { }

	// JSNI methods to get ResourceTag data

	public final native int getId() /*-{
		return this.id;
	}-*/;

		public final native String getName() /*-{
			return this.tagName;
		}-*/;

		public final native void setName(String newName) /*-{
			this.tagName = newName;
		}-*/;

		public final native int getVoId() /*-{
			return this.voId;
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
		public final boolean equals(ResourceTag o)
		{
			return o.getId() == this.getId();
		}
}
