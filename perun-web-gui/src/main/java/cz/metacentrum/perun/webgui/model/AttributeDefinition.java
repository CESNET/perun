package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.resources.AttributesConstants;

import java.util.MissingResourceException;

/**
 * OverlayType for Attribute Definition object
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class AttributeDefinition extends JavaScriptObject {

	protected AttributeDefinition() {}

	/**
	 * Get ID of attribute definition
	 * 
	 * @return id of attribute definition
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Get whole name of attribute definition (URN)
	 * 
	 * @return whole name of attribute definition
	 */
	public final native String getName() /*-{
		return this.namespace+':'+this.friendlyName;
	}-*/;

    /**
     * Get DisplayName of attribute used in GUI,
     * if not present, return friendlyName parameter
     *
     * @return display name of attribute definition
     */
    public final native String getDisplayName() /*-{
        if (!this.displayName) {
            return "";
        } else {
            return this.displayName;
        }
    }-*/;

    /**
     * Set new display name of attribute definition
     *
     * @param displayName new display name of attribute definition
     */
    public final native void setDisplayName(String displayName) /*-{
        this.displayName = displayName;
    }-*/;

    /**
	 * Get friendly name of attribute definition
	 * 
	 * @return friendly name of attribute definition
	 */
	public final native String getFriendlyName() /*-{
		return this.friendlyName;
	}-*/;
	
	/**
	 * Get base friendly name of attribute definition
	 * 
	 * e.g.: urn:perun:user:attribute-def:def:login-namespace:meta
	 * return "login-namespace"
	 * 
	 * if no parameter present, return whole friendlyName
	 * 
	 * @return base friendly name of attribute definition
	 */
	public final native String getBaseFriendlyName() /*-{
		return this.baseFriendlyName;
	}-*/;

	/**
	 * Get friendly name parameter of attribute definition
	 * 
	 * e.g.: urn:perun:user:attribute-def:def:login-namespace:meta
	 * return "meta"
	 * 
	 * If no parameter present, return ":";
	 * 
	 * @return friendly name parameter of attribute definition
	 */
	public final native String getFriendlyNameParameter() /*-{
		return this.friendlyNameParameter;
	}-*/;

	/**
	 * Get namespace of attribute definition
	 * 
	 * @return namespace of attribute definition
	 */
	public final native String getNamespace() /*-{
		return this.namespace;
	}-*/;
	
	/**
	 * Get attribute def. entity (user, member,...)
	 * 
	 * @return entity of attrDef
	 */
	public final native String getEntity() /*-{
		return this.entity;
	}-*/;	

	/**
	 * Get type of attribute definition
	 * 
	 * @return type of attribute definition
	 */
	public final native String getType() /*-{
		return this.type;
	}-*/;

	/**
	 * Get description of attribute definition
	 * 
	 * @return description of attribute definition
	 */
	public final native String getDescription() /*-{
		return this.description;
	}-*/;

    /**
     * Set new description of attribute definition
     *
     * @param desc new description of attribute definition
     */
    public final native void setDescription(String desc) /*-{
        this.description = desc;
    }-*/;

	/**
	 * Return definition type of attribute def.
	 * CORE, DEF, OPT, VIRT or "null" if not present
	 * 
	 * @return definition type
	 */
	public final native String getDefinition() /*-{
		var temp = new Array();
		temp = this.namespace.split(":");
		if (temp[4] == null ) { return "null"; }
		return temp[4];
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
	 * Return translated FriendlyName
	 * 
	 * (if no translation exists, return 
	 * same value as getFriendlyName()).
	 * 
	 * @return translated friendlyName
	 */
	public final String getTranslatedName() {

        try {
            if ("user".equalsIgnoreCase(getEntity()) && "login-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_user_attribute_def_def_login_namespace_name();
                return key+= " "+getFriendlyNameParameter().toUpperCase();
            } else if ("user".equalsIgnoreCase(getEntity()) && "uid-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_user_attribute_def_def_uid_namespace_name();
                return key+= " "+getFriendlyNameParameter().toUpperCase();
            } else if ("group".equalsIgnoreCase(getEntity()) && "unixGroupName-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_group_attribute_def_def_unixGroupName_namespace_name();
                return key+= " "+getFriendlyNameParameter().toUpperCase();
            } else if ("resource".equalsIgnoreCase(getEntity()) && "unixGroupName-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_resource_attribute_def_def_unixGroupName_namespace_name();
                return key+= " "+getFriendlyNameParameter().toUpperCase();
            } else if ("group".equalsIgnoreCase(getEntity()) && "unixGID-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_group_attribute_def_def_unixGID_namespace_name();
                return key+= " "+getFriendlyNameParameter().toUpperCase();
            } else if ("resource".equalsIgnoreCase(getEntity()) && "unixGID-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_resource_attribute_def_def_unixGID_namespace_name();
                return key+= " "+getFriendlyNameParameter().toUpperCase();
            } else {
                String key = getName() + "_name";
                key = key.replace(":", "_").replace("-", "_");
                return AttributesConstants.INSTANCE.getString(key);
            }
        } catch(MissingResourceException ex) {
            return getFriendlyName() + "";
        }
		
	}
	
	/**
	 * Return translated Description
	 * 
	 * (if no translation exists, return 
	 * same value as getDescription()).
	 * 
	 * @return translated Description
	 */
	public final String getTranslatedDescription() {

        try {
            if ("user".equalsIgnoreCase(getEntity()) && "login-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_user_attribute_def_def_login_namespace_description();
                return key+= " "+getFriendlyNameParameter().toUpperCase()+".";
            } else if ("user".equalsIgnoreCase(getEntity()) && "uid-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_user_attribute_def_def_uid_namespace_description();
                return key+= " "+getFriendlyNameParameter().toUpperCase()+".";
            } else if ("group".equalsIgnoreCase(getEntity()) && "unixGroupName-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_group_attribute_def_def_unixGroupName_namespace_description();
                return key+= " "+getFriendlyNameParameter().toUpperCase()+".";
            } else if ("resource".equalsIgnoreCase(getEntity()) && "unixGroupName-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_resource_attribute_def_def_unixGroupName_namespace_description();
                return key+= " "+getFriendlyNameParameter().toUpperCase()+". "+AttributesConstants.INSTANCE.when_resource_is_group_on_facility();
            } else if ("group".equalsIgnoreCase(getEntity()) && "unixGID-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_group_attribute_def_def_unixGID_namespace_description();
                return key+= " "+getFriendlyNameParameter().toUpperCase()+".";
            } else if ("resource".equalsIgnoreCase(getEntity()) && "unixGID-namespace".equalsIgnoreCase(getBaseFriendlyName())) {
                String key = AttributesConstants.INSTANCE.urn_perun_resource_attribute_def_def_unixGID_namespace_description();
                return key+= " "+getFriendlyNameParameter().toUpperCase()+". "+AttributesConstants.INSTANCE.when_resource_is_group_on_facility();
            } else {
                String key = getName() + "_description";
                key = key.replace(":", "_").replace("-", "_");
                return AttributesConstants.INSTANCE.getString(key);
            }
        } catch(MissingResourceException ex) {
            return getDescription() + "";
        }
		
	}
	
	/**
	 * Compares to another object
	 * @param o Object to compare
	 * @return true, if they are the same
	 */
	public final boolean equals(AttributeDefinition o)
	{
		return o.getId() == this.getId();		
	}
	
}