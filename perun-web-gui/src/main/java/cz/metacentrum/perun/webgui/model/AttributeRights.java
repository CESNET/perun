package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Overlay object type for Rights above attribute (definition)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AttributeRights extends JavaScriptObject {

	protected AttributeRights() {}

	public final static native AttributeRights create(int attrId, String role) /*-{
		var obj = new Object();
		obj.attributeId = attrId;
		obj.role = role;
		return obj;
	}-*/;

	/**
	 * Get ID
	 * @return id of attribute
	 */
	public final native int getAttributeId() /*-{
		return this.attributeId;
	}-*/;

	/**
	 * Set attribute ID
	 * @param id ID of attribute
	 */
	public final native void setAttributeId(int id) /*-{
		this.attributeId = id;
	}-*/;

	/**
	 * Get Role which belongs to this right-attribute
	 * @return role
	 */
	public final native String getRole() /*-{
		return this.role;
	}-*/;

	/**
	 * Set Role which belongs to this right-attribute
	 * @param role
	 */
	public final native void setRole(String role) /*-{
		this.role = role;
	}-*/;

	/**
	 * Get rights which belongs to this role-attribute
	 * @return rights above attribute
	 */
	public final native JsArrayString getRights() /*-{
		return this.rights;
	}-*/;

	/**
	 * Set rights which belongs to this role-attribute
	 * @param read
	 * @param write
	 */
	public final native void setRights(boolean read, boolean write) /*-{
		this.rights = new Array();
		if (read) {
			this.rights.push("READ");
		}
		if (write) {
			this.rights.push("WRITE");
		}
	}-*/;

}
