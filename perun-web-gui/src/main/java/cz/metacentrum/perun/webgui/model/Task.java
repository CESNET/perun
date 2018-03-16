package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.sql.Date;

/**
 * Overlay type for Task object from Perun
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Task extends JavaScriptObject {

	protected Task() {}

	/**
	 * Get ID of Task
	 *
	 * @return ID of Task
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final String getStartTime() {
		if (getStartTimeNative() != 0) {
			return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(new Date((long)getStartTimeNative()));
		} else {
			return "Not yet";
		}
	}

	public final native double getStartTimeNative() /*-{
		if (!(this.startTime)) { return 0; }
		return this.startTime;
	}-*/;

	public final String getEndTime() {
		if (getEndTimeNative() != 0) {
			return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(new Date((long)getEndTimeNative()));
		} else {
			return "Not yet";
		}
	}

	public final native double getEndTimeNative() /*-{
		if (!(this.endTime)) { return 0; }
		return this.endTime;
	}-*/;

	public final native Service getService() /*-{
		return this.service;
	}-*/;

	public final native Facility getFacility() /*-{
		return this.facility;
	}-*/;

	public final String getSchedule() {
		if (getScheduleNative() != 0) {
			return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(new Date((long)getScheduleNative()));
		} else {
			return "Not yet";
		}
	}

	public final native double getScheduleNative() /*-{
		if (!(this.schedule)) { return 0; }
		return this.schedule;
	}-*/;

	public final native int getRecurrence() /*-{
		return this.recurrence;
	}-*/;

	public final native int getDelay() /*-{
		return this.delay;
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
	public final boolean equals(Task o)
	{
		return o.getId() == this.getId();
	}

}
