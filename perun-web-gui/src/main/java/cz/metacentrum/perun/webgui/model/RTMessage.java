package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for RTMessage object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class RTMessage extends JavaScriptObject {

	protected RTMessage() { }

	// JSNI methods to get Message data
	public final native String getMemberPreferredEmail() /*-{
		return this.memberPreferredEmail;
	}-*/;

		public final native int getTicketNumber() /*-{
			return this.ticketNumber;
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
		public final boolean equals(RTMessage o)
		{
			return o.getTicketNumber() == this.getTicketNumber();
		}
}
