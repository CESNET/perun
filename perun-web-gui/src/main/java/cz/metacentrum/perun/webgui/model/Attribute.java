package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONValue;
import cz.metacentrum.perun.webgui.json.JsonUtils;

import java.util.Map;

/**
 * OverlayType for Attribute object
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class Attribute extends JavaScriptObject {

	public static int counter = 0;
	
	protected Attribute() {}

	/**
	 * Gets ID of attribute
	 * 
	 * @return ID of attribute
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Gets friendly name of attribute
	 * 
	 * @return friendly name of attribute
	 */
	public final native String getFriendlyName() /*-{
		return this.friendlyName;
	}-*/;

	/**
	 * Gets namespace of attribute
	 * 
	 * @return namespace of attribute
	 */
	public final native String getNamespace() /*-{
		return this.namespace;
	}-*/;
	
	/**
	 * Get whole name of attribute (URN)
	 * 
	 * @return whole name of attribute
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
	 * Get base friendly name of attribute
	 * 
	 * e.g.: urn:perun:user:attribute-def:def:login-namespace:meta
	 * return "login-namespace"
	 * 
	 * if no parameter present, return whole friendlyName
	 * 
	 * @return base friendly name of attribute
	 */
	public final native String getBaseFriendlyName() /*-{
		return this.baseFriendlyName;
	}-*/;

	/**
	 * Get friendly name parameter of attribute
	 * 
	 * e.g.: urn:perun:user:attribute-def:def:login-namespace:meta
	 * return "meta"
	 * 
	 * If no parameter present, return ":";
	 * 
	 * @return friendly name parameter of attribute
	 */
	public final native String getFriendlyNameParameter() /*-{
		return this.friendlyNameParameter;
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
	 * Gets description of attribute
	 * 
	 * @return description of attribute
	 */
	public final native String getDescription() /*-{
		return this.description;
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
	 * Gets tye of attribute
	 * 
	 * @return type of attribute
	 */
	public final native String getType() /*-{
		return this.type;
	}-*/;

    /**
     * Check if attribute value is writable for user or not
     *
     * @return TRUE if writable / FALSE otherwise
     */
    public final native boolean isWritable() /*-{
        if (typeof this.writable == "undefined") {
            // allow since PERUN-CORE can hadle this by itself
            return true;
        }
        return this.writable;
    }-*/;

    public final native void setWritable(boolean write) /*-{
        this.writable = write;
    }-*/;


    /**
	 * Gets value of attribute
	 * 
	 * @return value of attribute
	 */
	public final native String getValue() /*-{
	    
	    if (this.value == null) { return "null"; }
	    
	    return this.value.toString();
		
	}-*/;

	/**
	 * Sets a new value to attribute. String input is checked
	 * before setting for respecting value type
	 * 
	 * @param newValue new value to be set to attribute
	 * @return true if success (correct value)
	 */
	public final native boolean setValue(String newValue) /*-{

		// add trim function
		String.prototype.trim = function()
		{
    		return this.replace(/(^\s*)|(\s*$)/g, "")
		};

		if (this.type == "java.lang.Integer") {

			// true on any number format, false otherwise
			if (!isNaN(parseFloat(newValue)) && isFinite(newValue)) {   
				this.value = parseInt(newValue);
				return true;
			} else {
				return false;
			}
		}

		if (this.type == "java.util.ArrayList") {

			this.value = [];
			var count = 0;
			var input = "";

			for (var i=0; i<newValue.length; i++) {

				// escaped ","
				if (newValue[i] == "\\" && i+1 < newValue.length && newValue[i+1] == ",") {
					i++;                         // skip escape char
					input = input + newValue[i]  // add next
					continue;                    // and continue
				// normal ","
				} else {
					if (newValue[i] == ",") { 
						input = input.trim();          // trim whitespace on sides
						if (input == "") { continue; } // skip empty values
						this.value[count] = input;     // save previous value
						count++;                       // update field counter
						input = "";                    // clear input string
						continue;                      // skip "," and continue
					}
					input = input + newValue[i]  	   // append letter	
				}	
			}
			input = input.trim();               // at end - trim value
			if (input == "") { return false; }  // at end - do not save empty strings
			this.value[count] = input;          // at end - save value

			return true;
		}

		if (newValue == "") { return false; }   // do not save empty strings
		this.value = newValue;

		return true;

	}-*/;
	
	
/*	public final ArrayList<String> getValueAsList(){
		JsArrayString array = (JsArrayString) getValueAsJso();
		return JsonUtils.listFromJsArrayString(array);
	}*/
	
	public native final JsArrayString getValueAsJsArray() /*-{
		return this.value;
	}-*/;
	
	public native final JavaScriptObject getValueAsJso() /*-{
		return this.value;	
	}-*/;
	
	public native final Object getValueAsObject() /*-{
		return this.value;	
	}-*/;
	
	public native final void setValueAsJso(JavaScriptObject valueAsJso) /*-{
		this.value = valueAsJso;	
	}-*/;
	
	public native final void setValueAsString(String str) /*-{
		this.value = str;	
	}-*/;
	
	public native final void setValueAsNumber(int i) /*-{
		this.value = i;	
	}-*/;
	
	public native final void setValueAsJsArray(JsArrayString arr) /*-{
		this.value = arr;	
	}-*/;
	
	public native final void setAttributeValid(boolean valid) /*-{
		this.attributeValid = valid;	
	}-*/;

	public native final boolean isAttributeValid() /*-{
		return this.attributeValid;
	}-*/;

	/**
	 * Returns unique ID for GUI
	 * @return
	 */
	public native final String getGuiUniqueId() /*-{
	
		if(typeof $wnd.perunAttributeCounter == "undefined"){
			$wnd.perunAttributeCounter = 0;
		}

	
		if(typeof this.guiUniqueId == "undefined"){
			this.guiUniqueId = "attr-" + $wnd.perunAttributeCounter;
			$wnd.perunAttributeCounter++;
		}
		
		return this.guiUniqueId;
	}-*/;
	
	
	public final Map<String, JSONValue> getValueAsMap()
	{
		return JsonUtils.parseJsonToMap(getValueAsJso());
	}
	
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
	public final boolean equals(Attribute o) {
		return o.getId() == this.getId();
	}

}