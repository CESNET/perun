package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Object definition for audit messages
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class AuditMessage extends JavaScriptObject {

	protected AuditMessage() {}

	/**
	 * Get ID
	 * @return id of message
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Get message
		 * @return message
		 */
		public final native String getEvent() /*-{
			if (!this.event) return "";
			return this.event;
		}-*/;

	public final native String getEventName() /*-{
        if (!this.event) return "";
        return this.event.name;
    }-*/;

	public final native String getAuditEventObjectsMessage() /*-{
    	if (!this.event) return "";
    	var keys = Object.keys(this.event);
    	var message = "";
    	for (i = 0; i < keys.length; i++) {
    	    var key = keys[i];
    	    if (key !== "message" && key !== "name") {
    	        if (message !== "") {
    	            message += "\n\n";
	            }
    	        message += key + ":";
    	        message += JSON.stringify(this.event[key]);
		    }
	    }
    	return message;
	}-*/;

		/**
		 * Get actor
		 * @return actor
		 */
		public final native String getActor() /*-{
			if (!this.actor) return "";
			return this.actor;
		}-*/;

		/**
		 * Get createdAt
		 * @return date & time
		 */
		public final native String getCreatedAt() /*-{
			return this.createdAt;
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
		public final boolean equals(AuditMessage o)
		{
			return (o.getId() == this.getId());
		}

}
