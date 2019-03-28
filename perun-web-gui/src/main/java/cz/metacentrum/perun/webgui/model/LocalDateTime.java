package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

public class LocalDateTime extends JavaScriptObject {

	protected LocalDateTime() {
	}

	// {"year":2019,"month":"MARCH","monthValue":3,"dayOfMonth":28,"hour":6,"minute":16,"second":6,"nano":0,"dayOfWeek":"THURSDAY","dayOfYear":87,"chronology":{"calendarType":"iso8601","id":"ISO"}}

	public final native int getYear() /*-{
		return this.year;
	}-*/;

	public final native String getMonthName() /*-{
		return this.month;
	}-*/;

	public final native int getMonthValue() /*-{
		return this.monthValue;
	}-*/;

	public final native int getDayOfMonth() /*-{
		return this.dayOfMonth;
	}-*/;

	public final native int getHour() /*-{
		return this.hour;
	}-*/;

	public final native int getMinute() /*-{
		return this.minute;
	}-*/;

	public final native int getSecond() /*-{
		return this.second;
	}-*/;

	public final native int getNano() /*-{
		return this.nano;
	}-*/;

	public final native String getDayOfWeek() /*-{
		return this.dayOfWeek;
	}-*/;

	public final native int getDayOfYear() /*-{
		return this.dayOfYear;
	}-*/;

	public final native String getChronologyId() /*-{
		return this.chronology.id;
	}-*/;

	public final native String getChronologyCalendarType() /*-{
		return this.chronology.calendarType;
	}-*/;

	public final String printValue() {

		return getYear() + " " +
				getMonthName().substring(0,1) + getMonthName().substring(1,3).toLowerCase() +
				" " + ((getDayOfMonth() < 10) ? "0" : "") + getDayOfMonth() +
				" " + ((getHour() < 10) ? "0" : "") + getHour() +
				":" + ((getMinute() < 10) ? "0" : "") + getMinute() +
				":" + ((getSecond() < 10) ? "0" : "")+ getSecond();

	}

}
