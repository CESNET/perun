package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import cz.metacentrum.perun.webgui.json.JsonUtils;

import java.util.ArrayList;

/**
 * Overlay type for registrar: ApplicationFormItem
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class ApplicationFormItem extends JavaScriptObject {

	protected ApplicationFormItem() {}

	/**
	 * Get ID
	 * @return id
	 */
	public final native int getId() /*-{
		if(typeof this.id == "undefined") return 0;
		return this.id;
	}-*/;

	/**
	 * Get shortname
	 * @return shortname
	 */
	public final native String getShortname() /*-{
		if(typeof this.shortname == "undefined") return "";
		return this.shortname;
	}-*/;

	/**
	 * Set shortname
	 * @param shortname
	 */
	public final native void setShortname(String shortname) /*-{
		this.shortname = shortname;
	}-*/;

	/**
	 * Is required
	 * @return required
	 */
	public final native boolean isRequired() /*-{
		if(typeof this.required == "undefined") return false;
		return this.required;
	}-*/;

	/**
	 * Set required
	 */
	public final native void setRequired(boolean required) /*-{
		this.required = required;
	}-*/;

	/**
	 * Get type
	 * @return type
	 */
	public final native String getType() /*-{
		return this.type;
	}-*/;

	/**
	 * Get federationAttribute
	 * @return federationAttribute
	 */
	public final native String getFederationAttribute() /*-{
		if(typeof this.federationAttribute == "undefined") return "";
		return this.federationAttribute;
	}-*/;

	/**
	 * Set federationAttribute
	 */
	public final native void setFederationAttribute(String federationAttribute) /*-{
		this.federationAttribute = federationAttribute;
	}-*/;

	/**
	 * Get perunDestinationAttribute
	 * @return perunDestinationAttribute
	 */
	public final native String getPerunDestinationAttribute() /*-{
		if(typeof this.perunDestinationAttribute == "undefined") return "";
		return this.perunDestinationAttribute;
	}-*/;

	/**
	 * Set perunDestinationAttribute
	 */
	public final native void setPerunDestinationAttribute(String perunDestinationAttribute) /*-{
		this.perunDestinationAttribute = perunDestinationAttribute;
	}-*/;

	/**
	 * Get regex
	 * @return regex
	 */
	public final native String getRegex() /*-{
		if(typeof this.regex == "undefined") return "";
		return this.regex;
	}-*/;

	/**
	 * Set regex
	 */
	public final native void setRegex(String regex) /*-{
		this.regex = regex;
	}-*/;

	/**
	 * List of applicationTypes
	 * @return applicationTypes
	 */
	public final native JsArrayString getApplicationTypes() /*-{
		return this.applicationTypes;
	}-*/;

	/**
	 * Set applicationTypes
	 */
	public final native void setApplicationTypes(JsArrayString applicationTypes) /*-{
		this.applicationTypes = applicationTypes;
	}-*/;

	/**
	 * Set applicationTypes
	 */
	public final native void setApplicationTypes(JavaScriptObject applicationTypes) /*-{
		this.applicationTypes = applicationTypes;
	}-*/;

	/**
	 * get ordnum
	 * @return ordnum
	 */
	public final native int getOrdnum() /*-{
		return this.ordnum;
	}-*/;

	/**
	 * Set ordnum
	 */
	public final native void setOrdnum(int ordnum) /*-{
		this.ordnum = ordnum;
	}-*/;

	/**
	 * Get ItemTexts
	 * @return
	 */
	public final native ItemTexts getItemTexts(String locale) /*-{
		if(!(locale in this.i18n)){
			this.i18n[locale] = {locale: locale, errorMessage : "", help : "", label : "", options : ""};
		}
		return this.i18n[locale];
	}-*/;

	/**
	 * Get locales
	 *
	 * @return array of present locales
	 */
	public final ArrayList<String> getLocales() {
		return JsonUtils.listFromJsArrayString(getLocalesNative());
	}

	/**
	 * Get locales
	 *
	 * @return array of present locales
	 */
	public final native JsArrayString getLocalesNative() /*-{
		if(typeof this.i18n !== 'undefined' && this.i18n !== null){
			return Object.keys(this.i18n)
		}
		return null;
	}-*/;

	/**
	 * Set item texts
	 */
	public final native void setItemTexts(String locale, ItemTexts itemTexts) /*-{
		this.i18n[locale] = itemTexts;
	}-*/;

	/**
	 * get for delete state
	 * @return for delete
	 */
	public final native boolean isForDelete() /*-{
		if (!this.forDelete) { return false; }
		return this.forDelete;
	}-*/;

	/**
	 * Set deletion state
	 * @param del true = delete / false = keep (default)
	 */
	public final native void setForDelete(boolean del) /*-{
		this.forDelete = del;
	}-*/;

	/**
	 * Return TRUE if item item was edited
	 * (this property is for GUI purpose only)
	 *
	 * @return edited
	 */
	public final native boolean wasEdited() /*-{
		if (!this.edited) { return false; }
		return this.edited;
	}-*/;

	/**
	 * Set edited state
	 * (this property is for GUI purpose only)
	 *
	 * @param edited true = edited / false = not edited (default)
	 */
	public final native void setEdited(boolean edited) /*-{
		this.edited = edited;
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
	public final boolean equals(ApplicationFormItem o)
	{
		return o.getId() == this.getId();
	}


}
