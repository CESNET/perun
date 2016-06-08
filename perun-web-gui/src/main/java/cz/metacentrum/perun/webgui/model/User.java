package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Overlay type for User / RichUser object
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class User extends JavaScriptObject {

	protected User() {}

	/**
	 * Get ID of user
	 *
	 * @return id of user
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Set ID of user
	 *
	 * @param userId of user
	 */
	public final native void setId(int userId) /*-{
		this.id = userId;
	}-*/;

	/**
	 * Get first name of user
	 *
	 * @return first name of user
	 */
	public final native String getFirstName() /*-{
		if (!this.firstName) { return ""; }
		return this.firstName;
	}-*/;

	/**
	 * Set first name
	 *
	 * @param name first name of user
	 */
	public final native String setFirstName(String name) /*-{
		this.firstName = name;
	}-*/;

	/**
	 * Get last name of user
	 *
	 * @return last name of user
	 */
	public final native String getLastName() /*-{
		if (!this.lastName) { return ""; }
		return this.lastName;
	}-*/;

	/**
	 * Set last name
	 *
	 * @param name last name of user
	 */
	public final native String setLastName(String name) /*-{
		this.lastName = name;
	}-*/;

	/**
	 * Get full name of user
	 *
	 * @return last+first name of user
	 */
	public final native String getFullName() /*-{
		return this.lastName + " " + this.firstName;
	}-*/;

	/**
	 * Get ID of user
	 *
	 * @return id of user
	 */
	public final native String getMiddleName() /*-{
		if (!this.middleName) { return ""; }
		return this.middleName;
	}-*/;

	/**
	 * Set middle name
	 *
	 * @param name middle name of user
	 */
	public final native String setMiddleName(String name) /*-{
		this.middleName = name;
	}-*/;

	/**
	 * Get title before name of user
	 *
	 * @return title before name of user
	 */
	public final native String getTitleBefore() /*-{
		if (!this.titleBefore) { return ""; }
		return this.titleBefore;
	}-*/;

	/**
	 * Set title before name
	 *
	 * @param title title of user before name
	 */
	public final native void setTitleBefore(String title) /*-{
		this.titleBefore = title;
	}-*/;

	/**
	 * Get title after name of user
	 *
	 * @return title after name of user
	 */
	public final native String getTitleAfter() /*-{
		if (!this.titleAfter) { return ""; }
		return this.titleAfter;
	}-*/;

	/**
	 * Set title after name
	 *
	 * @param title title of user after name
	 */
	public final native void setTitleAfter(String title) /*-{
		this.titleAfter = title;
	}-*/;

	/**
	 * Get User Ext Sources of user
	 * USE ONLY FOR RICH USER
	 *
	 * @return list of UES
	 */
	public final native JsArray<UserExtSource> getUserExtSources() /*-{
		return this.userExtSources;
	}-*/;

	/**
	 * Get specified user attribute stored in rich user
	 * USE ONLY FOR RICH USER
	 *
	 * @param urn URN of attribute to get
	 * @return user attribute or null if not present
	 */
	public final native Attribute getAttribute(String urn) /*-{
		for(var i in this.userAttributes){
			if(this.userAttributes[i].namespace + ":" + this.userAttributes[i].friendlyName == urn){
				return this.userAttributes[i];
			}
		}
		return null;
	}-*/;

	/**
	 * Gets all logins in external sources associated with user stored in rich member
	 *
	 * @return users logins stored in users UESs
	 */
	public final native String getUserLogins() /*-{
		var logins = "";
		for(var i in this.userAttributes){
			var userAttribute = this.userAttributes[i];
			if(userAttribute.friendlyName.substring(0, 15) == "login-namespace"){
				// parse login namespace
				if (userAttribute.value != null) {
					// append comma
					if(logins.length > 0){
						logins += ", ";
					}
					var parsedNamespace =  userAttribute.friendlyName.substring(16);
					logins += parsedNamespace + ": " + userAttribute.value;
				}
			}
		}
		return logins;
	}-*/;

	/**
	 * Gets all logins stored in user attributes
	 *
	 * @return users logins
	 */
	public final native String getLogins() /*-{
		var logins = "";
		for(var i in this.userAttributes){
			var userAttribute = this.userAttributes[i];
			if(userAttribute.friendlyName.substring(0, 15) == "login-namespace"){
				// process only logins which are not null
				if (userAttribute.value != null) {
					// append comma
					if(logins.length > 0){
						logins += ", ";
					}
					// parse login namespace
					var parsedNamespace =  userAttribute.friendlyName.substring(16);
					logins += parsedNamespace + ": " + userAttribute.value;
				}
			}
		}
		return logins;
	}-*/;

	/**
	 * Get full name with titles of user
	 *
	 * @return full name with titles of user
	 */
	public final native String getFullNameWithTitles() /*-{
		var fullName = "";
		if(this.titleBefore != null){
			fullName += this.titleBefore + " ";
		}
		if(this.firstName != null){
			fullName += this.firstName + " ";
		}
		if(this.middleName != null){
			fullName += this.middleName + " ";
		}
		if(this.lastName != null){
			fullName += this.lastName;
		}
		if(this.titleAfter != null){
			fullName += ", " + this.titleAfter;
		}
		return fullName;
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
	 * Return TRUE if user is "service user".
	 *
	 * @return TRUE = service user / FALSE = standard user (might be sponsored user)
	 */
	public final native boolean isServiceUser() /*-{
		return this.serviceUser;
	}-*/;

	/**
	 * Mark user as service account
	 *
	 * @param service TRUE = service user / FALSE = normal user (might be sponsored user)
	 */
	public final native void setServiceUser(boolean service) /*-{
		return this.serviceUser = service;
	}-*/;

	/**
	 * Return TRUE if user is "sponsored user".
	 *
	 * @return TRUE = sponsored user / FALSE = standard user (might be service user)
	 */
	public final native boolean isSponsoredUser() /*-{
		return this.sponsoredUser;
	}-*/;

	/**
	 * Mark user as service account
	 *
	 * @param sponsored TRUE = sponsored user / FALSE = normal user (might be service user)
	 */
	public final native void setSponsoredUser(boolean sponsored) /*-{
		return this.sponsoredUser = sponsored;
	}-*/;

	/**
	 * Return TRUE if user is service or sponsored user.
	 *
	 * @return TRUE = service / sponsored user / FALSE = standard user
	 */
	public final native boolean isSpecificUser() /*-{
		return this.specificUser;
	}-*/;

	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(User o) {
		return o.getId() == this.getId();
	}

}
