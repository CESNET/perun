package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.resources.Utils;

/**
 * Overlay type for Group object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Group extends JavaScriptObject {

	protected Group() { }

	// JSNI methods to get Group data
	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native void setId(int id) /*-{
		this.id = id;
	}-*/;

	public final native String getName() /*-{
		return this.name;
	}-*/;

	public final native void setName(String name) /*-{
		this.name = name;
	}-*/;

	public final native String getShortName() /*-{
		return this.shortName;
	}-*/;

	public final native void setShortName(String shortName) /*-{
		this.shortName = shortName;
	}-*/;

	public final native String getDescription() /*-{
		return this.description;
	}-*/;

	public final native void setDescription(String text) /*-{
		this.description = text;
	}-*/;

	public final native int getParentGroupId() /*-{
		if (!this.parentGroupId) return 0;
		return this.parentGroupId;
	}-*/;

	public final native void setParentGroupId(int id) /*-{
		this.parentGroupId = id;
	}-*/;

	public final native void setIndent(int indent) /*-{
		this.indent = indent;
	}-*/;

	public final native int getIndent() /*-{
		if(typeof this.indent == "undefined"){
			return 0;
		}
		return this.indent;
	}-*/;

	public final native void setParentGroup(Group group) /*-{
		this.parentGroup = group;
	}-*/;

	public final native Group getParentGroup() /*-{
		return this.parentGroup;
	}-*/;

	/**
	 * Return TRUE if group is core group (members, administrators)
	 *
	 * @return TRUE if core group
	 */
	public final boolean isCoreGroup() {
		if (Utils.vosManagerMembersGroup().equalsIgnoreCase(getName())) {
			return true;
		} else {
			return false;
		}
	}

	public final native void setChecked(boolean value) /*-{
		this.checked = value;
	}-*/;

	public final native boolean isChecked() /*-{
		if(typeof this.checked === 'undefined'){
			this.checked = false;
		}
		return this.checked;
	}-*/;

	/**
	 * Return ID of VO, to which this group belongs
	 * This property might not be always set !!
	 *
	 * @return voId
	 */
	public final native int getVoId() /*-{
		return this.voId;
	}-*/;

	public final native void setVoId(int id) /*-{
		this.voId = id;
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
	public final boolean equals(Group o)
	{
		return o.getId() == this.getId();
	}

}
