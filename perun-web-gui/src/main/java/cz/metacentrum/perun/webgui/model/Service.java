package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for Service object
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Service extends JavaScriptObject {

	protected Service() {}

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
	 * Set service name
	 *
	 * @param name name of service
	 */
	public final native void setName(String name) /*-{
		this.name = name;
	}-*/;

	/**
	 * Gets description of service
	 *
	 * @return description of service
	 */
	public final native String getDescription()  /*-{
		return this.description;
	}-*/;

	/**
	 * Set service description
	 *
	 * @param description description of service
	 */
	public final native void setDescription(String description) /*-{
		this.description = description;
	}-*/;


	/**
	 * Get status of service (enabled/disabled)
	 *
	 * @return status of service
	 */
	public final native boolean isEnabled() /*-{
		return this.enabled;
	}-*/;

	/**
	 * Get status of service (enabled/disabled) for facility listed as denial.
	 * This parameter is normally empty and must be filled by callback !!
	 *
	 * @return status of service on facility
	 */
	public final native String isLocalEnabled() /*-{
		if (!this.localEnabled) {
			return "Enabled";
		}
		return this.localEnabled;
	}-*/;

	/**
	 * Set status of service (enabled/disabled) for facility listed as denial.
	 * This parameter is normally empty and must be filled by callback !!
	 *
	 * @param enabled TRUE = service enabled / FALSE = service disabled
	 */
	public final native void setLocalEnabled(String enabled) /*-{
		this.localEnabled = enabled;
	}-*/;

	/**
	 * Get default delay value of service
	 *
	 * @return default delay value of service
	 */
	public final native int getDelay() /*-{
		return this.delay;
	}-*/;

	/**
	 * Set default delay value of service
	 *
	 * @param delay default delay value of service
	 */
	public final native void setDelay(int delay) /*-{
		this.delay = delay;
	}-*/;

	/**
	 * Get default recurrence value of service
	 *
	 * @return default recurrence value of service
	 */
	public final native int getRecurrence() /*-{
		return this.recurrence;
	}-*/;

	/**
	 * Set default recurrence value of service
	 *
	 * @param recurrence default recurrence value of service
	 */
	public final native void setRecurrence(int recurrence) /*-{
		this.recurrence = recurrence;
	}-*/;

	/**
	 * Get path to propagations scripts of service
	 *
	 * @return path to propagations scripts of service
	 */
	public final native String getScriptPath() /*-{
		return this.script;
	}-*/;

	/**
	 * Set path to propagations scripts of service
	 *
	 * @param script path to propagations scripts of service
	 */
	public final native void setScriptPath(String script) /*-{
		this.script = script;
	}-*/;

	/**
	 * Sets status of service
	 *
	 * @param value (enabled = true / disabled = false)
	 */
	public final native void setEnabled(boolean value) /*-{
		this.enabled = value;
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
	public final boolean equals(Service o)
	{
		return o.getId() == this.getId();
	}

}
