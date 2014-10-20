package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.sql.Date;

/**
 * Overlay type for TaskResult object from Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class TaskResult extends JavaScriptObject {

	protected TaskResult() {}

	/**
	 * Get ID of TaskResult
	 *
	 * @return ID of TaskResult
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native Destination getDestination() /*-{
		return this.destination;
	}-*/;

	public final native Service getService() /*-{
		return this.service;
	}-*/;

	public final native String getStandardMessage() /*-{
		if (!this.standardMessage) { return ""; }
		return this.standardMessage;
	}-*/;

	public final native String getErrorMessage() /*-{
		if (!this.errorMessage) { return ""; }
		return this.errorMessage;
	}-*/;

	public final String getTimestamp() {
		if (getTimestampNative() != 0) {
			return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(new Date((long)getTimestampNative()));
		} else {
			return "";
		}
	}

	public final native double getTimestampNative() /*-{
		if (!this.timestamp) { return 0; }
		return this.timestamp;
	}-*/;

	public final native int getReturnCode() /*-{
		return this.returnCode;
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
	public final boolean equals(TaskResult o)
	{
		return o.getId() == this.getId();
	}

}
