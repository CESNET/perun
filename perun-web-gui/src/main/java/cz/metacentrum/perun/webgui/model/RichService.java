package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Service extension to provide GUI more info. Returned from:
 * GeneralServiceManager.getFacilityAssignedServicesForGUI()
 *
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
public class RichService extends JavaScriptObject {

	protected RichService() {}

	/**
	 * Gets ID of service
	 *
	 * @return id of service
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Gets name of service
		 *
		 * @return name of service
		 */
		public final native String getName()  /*-{
			return this.name;
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

		public final native String getAllowedOnFacility()/*-{
			if (this.allowedOnFacility==true) {
			return "Allowed";
			} else {
			return "Denied";
			}
		}-*/;

		public final native Service getService()/*-{
			return this;
		}-*/;

		/**
		 * Compares to another object
		 * @param o Object to compare
		 * @return true, if they are the same
		 */
		public final boolean equals(Service o)
		{
			return o.getId() == this.getId();
		}

}
