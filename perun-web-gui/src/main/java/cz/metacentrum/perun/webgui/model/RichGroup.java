package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import java.util.ArrayList;

/**
 * Overlay type for Group object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class RichGroup extends JavaScriptObject {

	protected RichGroup() { }

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
                public final native void showAttributes() /*-{
			alert(JSON.stringify(this.attributes));
		}-*/;
                public final native JsArray getAttributes() /*-{
			return this.attributes;
		}-*/;
                
                public final native void setAttributes(ArrayList<Attribute> attrs) /*-{
			this.groupAttributes = attrs;
		}-*/;
                
                public final native boolean isSyncEnabled() /*-{
                        for (var id in this.attributes) {
                            if (this.attributes[id].friendlyName === "synchronizationEnabled") {
                                return this.attributes[id].value;
                            }
                        }
                        return false;
                }-*/;
                public final native String getSynchronizationInterval() /*-{
                        for (var id in this.attributes) {
                            if (this.attributes[id].friendlyName === "synchronizationInterval") {
                                return this.attributes[id].value;
                            }
                        }
                        return null;
                }-*/;
                public final native String getLastSynchronizationState() /*-{
                        for (var id in this.attributes) {
                            if (this.attributes[id].friendlyName === "lastSynchronizationState") {
                                return this.attributes[id].value;
                            }
                        }
                        return null;
                }-*/;
                public final native String getLastSynchronizationTimestamp() /*-{
                        for (var id in this.attributes) {
                            if (this.attributes[id].friendlyName === "lastSynchronizationTimestamp") {
                                return this.attributes[id].value;
                            }
                        }
                        return null;
                }-*/;
                public final native String getAuthoritativeGroup() /*-{
                        for (var id in this.attributes) {
                            if (this.attributes[id].friendlyName === "authoritativeGroup") {
                                return this.attributes[id].value;
                            }
                        }
                        return null;
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

		public final native void setParentGroup(RichGroup group) /*-{
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
		public final boolean equals(RichGroup o)
		{
			return o.getId() == this.getId();
		}

}
