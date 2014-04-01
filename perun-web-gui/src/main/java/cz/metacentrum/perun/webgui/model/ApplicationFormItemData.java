package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

/**
 * Overlay type for registrar: ApplicationFormItemData
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class ApplicationFormItemData extends JavaScriptObject {

	protected ApplicationFormItemData() {}

	/**
	 * Creates the new object
	 *
	 * @param formItem
	 * @param shortname
	 * @param value
	 * @param assuranceLevel
	 * @return
	 */
	static public ApplicationFormItemData construct(ApplicationFormItem formItem, String shortname, String value, String prefilled, String assuranceLevel){

		ApplicationFormItemData obj = new JSONObject().getJavaScriptObject().cast();
		obj.setFormItem(formItem);
		obj.setShortname(shortname);
		obj.setValue(value);
		obj.setPrefilledValue(prefilled);
		obj.setAssuranceLevel(assuranceLevel);

		return obj;
	}


	/**
	 * Get formItem
	 * @return formItem
	 */
	public final native ApplicationFormItem getFormItem() /*-{
		return this.formItem;
	}-*/;

		/**
		 * Set formItem
		 */
		public final native void setFormItem(ApplicationFormItem formItem) /*-{
			this.formItem = formItem;
		}-*/;

		/**
		 * Get shorname
		 * @return shortname
		 */
		public final native String getShortname() /*-{
			if(typeof this.shortname == "undefined") return "";
			return this.shortname;
		}-*/;

		/**
		 * Set shorname
		 */
		public final native void setShortname(String shortname) /*-{
			this.shortname = shortname;
		}-*/;

		/**
		 * Get value
		 * @return value
		 */
		public final native String getValue() /*-{
			if(typeof this.value == "undefined") return "";
			return this.value;
		}-*/;

		/**
		 * Set value
		 */
		public final native void setValue(String value) /*-{
			this.value = value;
		}-*/;

		/**
		 * Get prefilled value
		 * @return value
		 */
		public final native String getPrefilledValue() /*-{
			if(typeof this.prefilledValue == "undefined") return "";
			return this.prefilledValue;
		}-*/;

		/**
		 * Set prefilled value
		 */
		public final native void setPrefilledValue(String value) /*-{
			this.prefilledValue = value;
		}-*/;

		/**
		 * Get assuranceLevel
		 * @return assuranceLevel
		 */
		public final native String getAssuranceLevel() /*-{
			if(typeof this.assuranceLevel == "undefined") return "";
			return this.assuranceLevel;
		}-*/;

		/**
		 * Set assuranceLevel
		 */
		public final native void setAssuranceLevel(String assuranceLevel) /*-{
			this.assuranceLevel = assuranceLevel;
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
		public final boolean equals(ApplicationFormItemData o)
		{
			return o.getShortname() == this.getShortname();
		}


}
