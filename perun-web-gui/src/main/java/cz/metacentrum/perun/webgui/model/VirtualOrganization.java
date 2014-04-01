package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type for VirtualOrganization object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class VirtualOrganization extends JavaScriptObject {

	protected VirtualOrganization() { }

	// JSNI methods to get Virtual organization data.
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

		/**
		 * Get All filled VO attributes
		 * THIS PROPERTY MUST BE FILLED MANUALLY,
		 * it's not part of VO object by default
		 *
		 * @return list of filled attributes
		 */
		public final native JsArray<Attribute> getAttributes() /*-{
			return this.attributes;
		}-*/;

		public final native void setAttributes(JsArray<Attribute> list) /*-{
			this.attributes = list;
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
		public final boolean equals(VirtualOrganization o)
		{
			return o.getId() == this.getId();
		}

}
