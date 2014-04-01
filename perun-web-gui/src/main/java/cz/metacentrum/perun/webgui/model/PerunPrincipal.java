package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for PerunPrincipal object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class PerunPrincipal extends JavaScriptObject {

	protected PerunPrincipal() {};

	/**
	 * Get login used to log into RPC
	 *
	 * @return login
	 */
	public final native String getActor() /*-{
		return this.actor;
	}-*/;

		/**
		 * Get external source name used to log into RPC
		 *
		 * @return external source name
		 */
		public final native String getExtSource() /*-{
			return this.extSourceName;
		}-*/;

		/**
		 * Get user found in DB based on used login and ext source
		 * Null for PerunAdministrator
		 *
		 * @return logged user
		 */
		public final native User getUser() /*-{
			return this.user;
		}-*/;

		/**
		 * Get external source type used to log into RPC
		 *
		 * @return external source type
		 */
		public final native String getExtSourceType()  /*-{
			return this.extSourceType
		}-*/;

		/**
		 * Get LoA of user in external source used to log into RPC
		 *
		 * @return external source LoA
		 */
		public final native int getExtSourceLoa()  /*-{
			if (!this.extSourceLoa) return 0;
			return this.extSourceLoa
		}-*/;

		/**
		 * Get additional informations provided by IDP in session
		 *
		 * @param shibAttrName - valid shibolleth attribute name:
		 * mail - mail
		 * o - organization
		 * loa - level of assurance
		 * ivenName - given name
		 * cn - common name
		 * displayName - display name
		 * sn - sure name
		 * eppn - another mail ?
		 *
		 * @return attribute value or empty string if not present
		 */
		public final native String getAdditionInformations(String shibAttrName) /*-{
			if(typeof this.additionalInformations[shibAttrName] == "undefined") return "";
			return this.additionalInformations[shibAttrName];
		}-*/;

		/**
		 * Sets user into PerunPrincipal
		 *
		 * @param newUser new user
		 */
		public final native void setUser(User newUser) /*-{
			this.user = newUser;
		}-*/;

		/**
		 * Get users roles
		 *
		 * @return users roles
		 */
		public final native Roles getRoles() /*-{
			return this.roles;
		}-*/;

		/**
		 * Get initialization status. Must be true if login was successful
		 *
		 * @return initialization status
		 */
		public final native boolean isInitialized() /*-{
			return this.authzInitialized;
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

}
