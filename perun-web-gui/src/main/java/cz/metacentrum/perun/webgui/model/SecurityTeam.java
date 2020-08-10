package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for SecurityTeam object from Perun
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class SecurityTeam extends JavaScriptObject {

	protected SecurityTeam() { }

	// JSNI methods to get Resource data
	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final native void setName(String newName) /*-{
		this.name = newName;
	}-*/;

	public final native String getDescription() /*-{
		return this.description;
	}-*/;

	public final native void setDescription(String newDesc) /*-{
		this.description = newDesc;
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
	 * VALID, INVALID, EXPIRED, DISABLED
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
	public final boolean equals(SecurityTeam o)
	{
		return o.getId() == this.getId();
	}

}
