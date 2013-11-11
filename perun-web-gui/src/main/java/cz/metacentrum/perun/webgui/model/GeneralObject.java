package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * General object for retrieving type, ID and others
 * common properties from all kind of objects in Perun.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class GeneralObject extends JavaScriptObject {

	protected GeneralObject() { }

	// JSNI methods to get Object data
	
	/**
	 * Returns object id
	 * @return
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;
	/**
	 * Returns Perun specific type of object
	 * 
	 * @return type of object
	 */
	public final native String getObjectType() /*-{
		if (!this.objecttype) {
			if (!this.beanName) {
				return "JavaScriptObject";
			}
			return this.beanName;
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
	 * Sets the status
	 * VALID, INVALID, SUSPENDED, EXPIRED, DISABLED
	 * 
	 * @param status String which defines item status
	 */
	public final native String setStatus(String status) /*-{
		this.status = status;
	}-*/;

    /**
     * Return name parameter or it's equivalent for any kind of PerunBean object
     *
     * @return name
     */
    public final native String getName() /*-{

        if (!this.beanName) {
            return this.name;

        } else {

            if (this.beanName == "RichMember") {
                return this.user.lastName + " " +this.user.firstName;
            } else if (this.beanName == "User") {
                return this.lastName + " " +this.firstName;
            } else if (this.beanName == "RichUser") {
                return this.lastName + " " +this.firstName;
            } else if (this.beanName == "Author") {
                return this.lastName + " " +this.firstName;
            } else if (this.beanName == "ExecService") {
                return this.service.name + " (" + this.execServiceType+")";
            } else if (this.beanName == "AttributeDefinition") {
                return this.displayName;
            } else if (this.beanName == "Attribute") {
                return this.displayName;
            } else if (this.beanName == "Publication") {
                return this.title;
            } else if (this.beanName == "ApplicationMail") {
                try {
                    return @cz.metacentrum.perun.webgui.model.ApplicationMail::getTranslatedMailType(Ljava/lang/String;)(this.mailType);
                } catch(e) {
                    return this.mailType;
                }
            } else if (this.beanName == "UserExtSource") {
                return this.login + " / " + this.extSource.name;
            } else if (this.beanName == "Host") {
                return this.hostname;
            } else if (this.beanName == "RichDestination") {
                return this.destination;
            } else {
                return this.name;
            }
        }

	}-*/;
	
	public final native String getDescription() /*-{
		return this.description;
	}-*/;
	
	public final native String getAttribute(String attrName) /*-{
		return this[attrName] + "";
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
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(GeneralObject o)
	{
		return o.getId() == this.getId();		
	}
	
}