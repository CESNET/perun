package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for ExecService objects
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class ExecService extends JavaScriptObject {

	protected ExecService() {}

	/**
	 * Get ID of exec service
	 *
	 * @return ID of exec service
	 */
	public final native int getId() /*-{
        return this.id;
    }-*/;

	/**
	 * Get service associated with exec service
	 *
	 * @return service associated with exec service
	 */
	public final native Service getService() /*-{
        return this.service;
    }-*/;

	/**
	 * Get status of exec service (enabled/disabled)
	 *
	 * @return status of exec service
	 */
	public final native boolean isEnabled() /*-{
        return this.enabled;
    }-*/;

	/**
	 * Get status of exec service (enabled/disabled) for facility listed as denial.
	 * This parameter is normally empty and must be filled by callback !!
	 *
	 * @return status of exec service on facility
	 */
	public final native String isLocalEnabled() /*-{
        if (!this.localEnabled) {
            return "Enabled";
        }
        return this.localEnabled;
    }-*/;

	/**
	 * Set status of exec service (enabled/disabled) for facility listed as denial.
	 * This parameter is normally empty and must be filled by callback !!
	 *
	 * @param enabled TRUE = service enabled / FALSE = service disabled
	 */
	public final native void setLocalEnabled(String enabled) /*-{
        this.localEnabled = enabled;
    }-*/;

	/**
	 * Get default delay value of exec service
	 *
	 * @return default delay value of exec service
	 */
	public final native int getDefaultDelay() /*-{
        return this.defaultDelay;
    }-*/;

	/**
	 * Set default delay value of exec service
	 *
	 * @param delay default delay value of exec service
	 */
	public final native void setDefaultDelay(int delay) /*-{
        this.defaultDelay = delay;
    }-*/;

	/**
	 * Get default recurrence value of exec service
	 *
	 * @return default recurrence value of exec service
	 */
	public final native int getDefaultRecurrence() /*-{
        return this.defaultRecurrence;
    }-*/;

	/**
	 * Get path to propagations scripts of exec service
	 *
	 * @return path to propagations scripts of exec service
	 */
	public final native String getScriptPath() /*-{
        return this.script;
    }-*/;

	/**
	 * Set path to propagations scripts of exec service
	 *
	 * @param script path to propagations scripts of exec service
	 */
	public final native void setScriptPath(String script) /*-{
        this.script = script;
    }-*/;

	/**
	 * Get type of exec service (SEND / GENERATE)
	 *
	 * @return type of exec service
	 */
	public final native String getType() /*-{
        return this.execServiceType;
    }-*/;

	/**
	 * Sets status of exec service
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
	public final boolean equals(ExecService o)
	{
		return o.getId() == this.getId();
	}
}
