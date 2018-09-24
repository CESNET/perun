package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.Map;

/**
 * Overlay type for RichMember object
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class RichMember extends JavaScriptObject {

	protected RichMember(){}

	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native int getVoId() /*-{
		return this.voId;
	}-*/;

	public final native int getUserId() /*-{
		return this.userId;
	}-*/;

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
	 * Get user stored in rich member
	 *
	 * @return user
	 */
	public final native User getUser()/*-{
		return this.user;
	}-*/;

	/**
	 * Set user stored in rich member
	 *
	 * @return user
	 */
	public final native void setUser(User user)/*-{
		this.user = user;
	}-*/;

	/**
	 * Get user attributes stored in rich member
	 *
	 * @return user attributes
	 */
	public final native JsArray<Attribute> getUserAttributes() /*-{
		return this.userAttributes;
	}-*/;

	/**
	 * Get member attributes stored in rich member
	 *
	 * @return member attributes
	 */
	public final native JsArray<Attribute> getMemberAttributes() /*-{
		return this.memberAttributes;
	}-*/;

	/**
	 * Get specified user/member attribute stored in rich member
	 * WORK ONLY WITH MEMBER AND USER ATTRIBUTES
	 *
	 * @param urn whole attribute namespace+name (urn)
	 * @return member or user attribute or NULL if attribute not present
	 */
	public final native Attribute getAttribute(String urn) /*-{
		if (urn.indexOf("urn:perun:user:") !== -1) {
			for(var i in this.userAttributes){
				if(this.userAttributes[i].namespace + ":" + this.userAttributes[i].friendlyName == urn){
					return this.userAttributes[i];
				}
			}
			return null;
		} else if (urn.indexOf("urn:perun:member:") !== -1) {
			for(var i in this.memberAttributes){
				if(this.memberAttributes[i].namespace + ":" + this.memberAttributes[i].friendlyName == urn){
					return this.memberAttributes[i];
				}
			}
			return null;
		}
		return null;
	}-*/;

	/**
	 * Set attribute to RichMember object.
	 * WORK ONLY FOR USER AND MEMBER ATTRIBUTES
	 *
	 * If attribute present, update value
	 * If attribute not present, add to attr list
	 *
	 * @param attribute to set to RichMember object
	 */
	public final native void setAttribute(Attribute attribute) /*-{
		if (attribute == null) return;
		// init fields if empty
		if (this.userAttributes == null) {
			this.userAttributes = [];
		}
		if (this.memberAttributes == null) {
			this.memberAttributes = [];
		}
		var found = false;
		if (attribute.namespace.indexOf("urn:perun:user:") !== -1) {
			// set user attribute
			for(var i in this.userAttributes){
				if(this.userAttributes[i].namespace == attribute.namespace && this.userAttributes[i].friendlyName == attribute.friendlyName){
					this.userAttributes[i].value = attribute.value;
					found = true;
				}
			}
			if (!found) {
				// put whole attribute
				this.userAttributes[this.userAttributes.length] = attribute;
			}
		} else if (attribute.namespace.indexOf("urn:perun:member:") !== -1) {
			// set member attribute
			for(var i in this.memberAttributes){
				if(this.memberAttributes[i].namespace == attribute.namespace && this.memberAttributes[i].friendlyName == attribute.friendlyName){
					this.memberAttributes[i].value = attribute.value;
					found = true;
				}
			}
			if (!found) {
				// put whole attribute
				this.memberAttributes[this.memberAttributes.length] = attribute;
			}
		}
	}-*/;

	/**
	 * Gets all logins stored in user attributes
	 *
	 * @return users logins
	 */
	public final native String getUserLogins() /*-{
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
	 * Gets user ext sources associated with user stored in rich member
	 *
	 * @return users ext sources stored in user
	 */
	public final native ArrayList<UserExtSource> getUserExtSources() /*-{
		return this.userExtSources;
	}-*/;

	/**
	 * Get membership type (context associated on member's retrieval)
	 *
	 * @return membership type (DIRECT, INDIRECT, NOT_DEFINED, ....)
	 */
	public final native String getMembershipType() /*-{
		if (!this.membershipType) {
			return "NOT_DETERMINED";
		} else {
			return this.membershipType;
		}
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
	 * Returns the status of Member in a Group context, by default VALID.
	 * Possible values are VALID and EXPIRED
	 *
	 * @return string which defines group member status
	 */
	public final native String getGroupStatus() /*-{
		return this.groupStatus;
	}-*/;

	/**
	 * Set group membership status (VALID, EXPIRED)
	 *
	 * @param groupStatus string which defines item status
	 */
	public final native void setGroupStatus(String groupStatus) /*-{
		this.groupStatus = groupStatus;
	}-*/;

	/**
	 * Returns map of all Member statuses, which are used to calculate getGroupStatus() value.
	 * Its map of GroupId=>Status
	 * Possible values are VALID and EXPIRED
	 *
	 * @return map of member sourcing statuses
	 */
	public final native GroupStatuses getGroupStatuses() /*-{
		return this.groupStatuses;
	}-*/;

	/**
	 * Set the status of this item in Perun system as String
	 * VALID, INVALID, SUSPENDED, EXPIRED, DISABLED
	 *
	 * @param status string which defines item status
	 */
	public final native void setStatus(String status) /*-{
		this.status = status;
	}-*/;

	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(RichMember o) {
		return (o.getId() == this.getId()) && (o.getUser().getId() == this.getUser().getId());
	}
}
