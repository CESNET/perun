package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for ServicesPackage object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ServicesPackage extends JavaScriptObject {

	protected ServicesPackage() {}

	/**
	 * Gets ID of service package
	 *
	 * @return id of service package
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Gets name of service package
		 *
		 * @return name of service package
		 */
		public final native String getName()  /*-{
			return this.name;
		}-*/;

		/**
		 * Gets description of service package
		 *
		 * @return description of service package
		 */
		public final native String getDescription()  /*-{
			return this.description;
		}-*/;

		/**
		 * Set name of service package
		 *
		 * @param name of service package
		 */
		public final native void setName(String name)  /*-{
			this.name = name;
		}-*/;

		/**
		 * Set description of service package
		 *
		 * @param description of service package
		 */
		public final native void setDescription(String description)  /*-{
			this.description = description;
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
		public final boolean equals(ServicesPackage o)
		{
			return o.getId() == this.getId();
		}

}
