package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

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
		if (getStartTimeNative() != null) {
			return getStartTimeNative().printValue();
		} else {
			return "Not yet";
		}
	}

	public final native LocalDateTime getStartTimeNative() /*-{
		if (!(this.startTime)) { return null; }
		return this.startTime;
	}-*/;

	public final String getEndTime() {
		if (getEndTimeNative() != null) {
			return getEndTimeNative().printValue();
		} else {
			return "Not yet";
		}
	}

	public final native LocalDateTime getEndTimeNative() /*-{
		if (!(this.endTime)) { return null; }
		return this.endTime;
	}-*/;

	public final native Service getService() /*-{
		return this.service;
	}-*/;

	public final native Facility getFacility() /*-{
		return this.facility;
	}-*/;

	public final String getSchedule() {
		if (getScheduleNative() != null) {
			return getScheduleNative().printValue();
		} else {
			return "Not yet";
		}
	}

	public final native LocalDateTime getScheduleNative() /*-{
		if (!(this.schedule)) { return null; }
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
