package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.sql.Date;

/**
 * ServiceState object wrapper.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ServiceState extends JavaScriptObject {

	protected ServiceState() {
	}

	public final native int getTaskId() /*-{
		if (!(this.taskId)) { return 0; }
		return this.taskId;
	}-*/;

	public final native Service getService() /*-{
		return this.service;
	}-*/;

	public final native Task getTask() /*-{
		return this.task;
	}-*/;

	public final native boolean isBlockedGlobally() /*-{
		return this.blockedGlobally;
	}-*/;

	public final native boolean isBlockedOnFacility() /*-{
		return this.blockedOnFacility;
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

	/**
	 * Returns the status of this item in Perun system as String
	 * VALID, INVALID, SUSPENDED, EXPIRED, DISABLED
	 *
	 * @return string which defines item status
	 */
	public final native String getStatus() /*-{
		return this.status;
	}-*/;

	public final native String getLastScheduled() /*-{
		if (!(this.lastScheduled)) { return "SEND"; }
		return this.lastScheduled;
	}-*/;

	public final native String getHasDestinations() /*-{
		return (this.hasDestinations === true) ? "" : "Service has no destinations defined.";
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

}
