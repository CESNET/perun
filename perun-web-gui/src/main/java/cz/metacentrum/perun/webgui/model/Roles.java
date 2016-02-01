package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

/**
 * Overlay type for Roles object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Roles extends JavaScriptObject {

	protected Roles() {};

	/**
	 * Get role name stored in object
	 *
	 * @return role name
	 */
	public final native boolean hasRole(String role) /*-{
		if (this.hasOwnProperty(role)) {
			return true;
		}
		return false;
	}-*/;

	/**
	 * Check if there are any roles
	 */
	public final native boolean hasAnyRole() /*-{
		for (var key in this) {
			if (this.hasOwnProperty(key)) {
				return true;
			}
		}
		return false;
	}-*/;

	/**
	 * Return all editable entities contained in session.
	 *
	 * @param entityType "Vo, Facility, User, Group"
	 */
	public final native JsArrayInteger getEditableEntities(String entityType) /*-{
		var entities = new Array();
		for (var key in this) {
			if (this.hasOwnProperty(key)) {
				if (typeof this[key][entityType] != "undefined") {
					var ids = this[key][entityType];
					entities = entities.concat(ids);
				}
			}
		}
		return entities;
	}-*/;


	/**
	 * Return all editable entities contained in session.
	 *
	 * @param role VOADMIN, SELF, PERUNADMIN, GROUPADMIN, VOOBSERVER, FACILITYADMIN
	 * @param entityType "Vo, Facility, User, Group, SecurityTeam, Sponsor"
	 */
	public final native JsArrayInteger getEditableEntities(String role, String entityType) /*-{
		var entities = new Array();
		if (this.hasOwnProperty(role)) {
			if (typeof this[role][entityType] != "undefined") {
				var ids = this[role][entityType];
				entities = entities.concat(ids);
			}
		}
		return entities;
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
	public final boolean equals(Roles o)
	{
		return true; //o.getRole().equals(this.getRole());
	}
}
