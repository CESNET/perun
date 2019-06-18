package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for UserExtSource object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class UserExtSource extends JavaScriptObject {

	protected UserExtSource() { }

	// JSNI methods to get UserExtSources data
	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native String getLogin() /*-{
		return this.login;
	}-*/;

	public final native ExtSource getExtSource() /*-{
		return this.extSource;
	}-*/;

	public final native int getLoa() /*-{
		return this.loa;
	}-*/;

	public final native boolean isPersistent() /*-{
		return this.persistent;
	}-*/;

	public final native String getLastAccess() /*-{
		return this.lastAccess;
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
	public final boolean equals(UserExtSource o)
	{
		return o.getId() == this.getId();
	}

}
