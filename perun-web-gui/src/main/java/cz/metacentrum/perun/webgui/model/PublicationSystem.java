package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for PublicationSystem object from Perun
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PublicationSystem  extends JavaScriptObject {

	protected PublicationSystem() {}

	/**
	 * Returns object ID
	 * @return ID
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

		/**
		 * Returns friendly name
		 * @return name
		 */
		public final native String getFriendlyName() /*-{
			return this.friendlyName;
		}-*/;

		/**
		 * Returns system url
		 * @return url
		 */
		public final native String getUrl() /*-{
			if (this.url == 'empty') { return ""; };
			return this.url;
		}-*/;

		/**
		 * Returns namespace for users logins
		 * @return namespace
		 */
		public final native String getLoginNamespace() /*-{
			if (this.loginNamespace == 'empty') { return ""; };
			return this.loginNamespace;
		}-*/;

		/**
		 * Returns type of pub. sys (parser)
		 * @return type
		 */
		public final native String getType() /*-{
			if (this.type == 'empty') { return ""; };
			return this.type;
		}-*/;

		/**
		 * Returns type of pub. sys (parser)
		 * @return type
		 */
		public final native String getUsername() /*-{
			if (this.type == 'empty') { return ""; };
			return this.username;
		}-*/;

		/**
		 * Returns type of pub. sys (parser)
		 * @return type
		 */
		public final native String getPassword() /*-{
			return this.password;
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
		public final boolean equals(PublicationSystem o)
		{
			return o.getId() == this.getId();
		}


}
