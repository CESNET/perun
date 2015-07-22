package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * OverlayType for Candidate object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class Candidate extends JavaScriptObject {

	protected Candidate() {}

	/**
	 * Get user ext source of candidate
	 *
	 * @return user ext source of candidate
	 */
	public final native UserExtSource getUserExtSource() /*-{
		return this.userExtSource;
	}-*/;

	/**
	 * Get attributes of candidate from ext source
	 *
	 * @return attributes of candidate from ext source
	 */
	public final native Map<String, String> getAttributes() /*-{
		return this.attributes;
	}-*/;

	/**
	 * Get additionalUserExtSources of candidate from ext source
	 *
	 * @return additionalUserExt sources for candidate from ext source
	 */
	public final native ArrayList<UserExtSource> getAdditionalUserExtSources() /*-{
		return this.additionalUserExtSources
	}-*/;

	/**
	 * Get ID of candidate
	 *
	 * @return ID of candidate
	 */
	public final native String getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Get first name of candidate
	 *
	 * @return first name of candidate
	 */
	public final native String getFirstName() /*-{
		return this.firstName;
	}-*/;

	/**
	 * Get last name of candidate
	 *
	 * @return last name of candidate
	 */
	public final native String getLastName() /*-{
		return this.lastName;
	}-*/;

	/**
	 * Get middle name of candidate
	 *
	 * @return middle name of candidate
	 */
	public final native String getMiddleName() /*-{
		return this.middleName;
	}-*/;

	/**
	 * Get title before name of candidate
	 *
	 * @return title before name of candidate
	 */
	public final native String getTitleBefore() /*-{
		return this.titleBefore;
	}-*/;

	/**
	 * Get title after name of candidate
	 *
	 * @return title after name of candidate
	 */
	public final native String getTitleAfter() /*-{
		return this.TitleAfter;
	}-*/;

	/**
	 * Get full name of candidate
	 *
	 * @return last+first name of candidate
	 */
	public final native String getFullName() /*-{
		return this.lastName + " " + this.firstName;
	}-*/;

	/**
	 * Get display name of candidate
	 *
	 * @return complete view of names and titles of candidate
	 */
	public final native String getDisplayName() /*-{
		var displayName = "";
		if(this.titleBefore != null){
			displayName += this.titleBefore + " ";
		}
		if(this.firstName != null){
			displayName += this.firstName + " ";
		}
		if(this.middleName != null){
			displayName += this.middleName + " ";
		}
		if(this.lastName != null){
			displayName += this.lastName + " ";
		}
		if(this.titleAfter != null){
			displayName += this.titleAfter + " ";
		}
		return displayName;
	}-*/;

	/**
	 * Get email of candidate
	 *
	 * @return email of candidate
	 */
	public final native String getEmail() /*-{
		if (this.attributes['urn:perun:member:attribute-def:def:mail'] != null) {
			return this.attributes['urn:perun:member:attribute-def:def:mail'];
		} else if (this.attributes['urn:perun:user:attribute-def:def:preferredMail'] != null) {
			return this.attributes['urn:perun:user:attribute-def:def:preferredMail'];
		}
		return "";
	}-*/;

	/**
	 * Get attributes of candidate from external source
	 *
	 * @return attributes of candidate from external source
	 */
	public final native String getAttribute(String name) /*-{
		return this.attributes[name];
	}-*/;

	/**
	 * Gets all logins stored in user attributes
	 *
	 * @return users logins
	 */
	public final native String getLogins() /*-{
		var logins = "";
		for (var prop in this.attributes) {
			if (this.attributes.hasOwnProperty(prop)) {
				if (prop.indexOf("urn:perun:user:attribute-def:def:login-namespace:") != -1) {
					if (this.attributes[prop] != null) {
						if(logins.length > 0){
							logins += ", ";
						}
						// parse login namespace
						var parsedNamespace = prop.substring(49);
						logins += parsedNamespace + ": " + this.attributes[prop];
					}
				}
			}
		}
		return logins;
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
	public final boolean equals(Candidate o) {
		return o.getId().equals(this.getId());
	}

}