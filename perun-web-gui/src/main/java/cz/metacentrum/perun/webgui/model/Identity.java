package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * OverlayType for Identity object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class Identity extends JavaScriptObject {

	protected Identity() {}

	/**
	 * Gets ID of user
	 *
	 * @return ID of user
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Gets name of user
	 *
	 * @return name of user
	 */
	public final native String getName() /*-{
		return this.name;
	}-*/;

	/**
	 * Gets users organization
	 *
	 * @return users organization
	 */
	public final native String getOrganization() /*-{
		return this.organization;
	}-*/;


	/**
	 * Get obfuscated email address.
	 *
	 * @return obfuscated email address
	 */
	public final native String getEmail() /*-{
		return this.email;
	}-*/;

	/**
	 * Get list of users external identities
	 *
	 * @return external identities
	 */
	public final native JsArray<ExtSource> getExternalIdentities() /*-{
		return this.identities;
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
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(Identity o)
	{
		return o.getId() == this.getId();
	}

}
