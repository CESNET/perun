package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for destination object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class Destination extends JavaScriptObject {

	protected Destination() {}

	/**
	 * Get ID of destination
	 *
	 * @return ID of destination
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Get destination string of destination
	 *
	 * @return destination string of destination
	 */
	public final native String getDestination() /*-{
		return this.destination;
	}-*/;

	/**
	 * Get type of destination
	 *
	 * @return type of destination
	 */
	public final native String getType() /*-{
		if (!this.type) {
			return "";
		}
		return this.type;
	}-*/;

	/**
	 * Get propagation type "PARALLEL, SERIAL, DUMMY"
	 *
	 * @return propagation type
	 */
	public final native String getPropagationType() /*-{
		if (!this.propagationType) {
			return "";
		}
		return this.propagationType;
	}-*/;

	/**
	 * Sets service associated with destination
	 * ONLY FOR RICH-DESTINATION
	 *
	 * @param service service associated with destination
	 */
	public final native void setService(Service service) /*-{
		this.service = service;
	}-*/;

	/**
	 * Get service associated with destination
	 * ONLY FOR RICH-DESTINATION
	 *
	 * @return service
	 */
	public final native Service getService() /*-{
		return this.service;
	}-*/;

	/**
	 * Sets facility associated with destination
	 * ONLY FOR RICH-DESTINATION
	 *
	 * @param facility facility associated with destination
	 */
	public final native void setFacility(Facility facility) /*-{
		this.facility = facility;
	}-*/;

	/**
	 * Get facility associated with destination
	 * ONLY FOR RICH-DESTINATION
	 *
	 * @return facility
	 */
	public final native Facility getFacility() /*-{
		return this.facility;
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
	public final boolean equals(Destination o)
	{
		return o.getId() == this.getId();
	}

}
