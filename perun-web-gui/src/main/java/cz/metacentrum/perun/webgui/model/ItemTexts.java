package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for registrar: ItemTexts
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class ItemTexts extends JavaScriptObject {

	protected ItemTexts() {}


	/**
	 * Get label
	 * @return label
	 */
	public final native String getLabel() /*-{
		if(typeof this.label == "undefined") return "";
		return this.label;
	}-*/;

		/**
		 * Set label
		 */
		public final native void setLabel(String label) /*-{
			this.label = label;
		}-*/;

		/**
		 * Get options
		 * @return options
		 */
		public final native String getOptions() /*-{
			if(typeof this.options == "undefined") return "";
			return this.options;
		}-*/;

		/**
		 * Set options
		 */
		public final native void setOptions(String options) /*-{
			this.options = options;
		}-*/;

		/**
		 * Get help
		 * @return help
		 */
		public final native String getHelp() /*-{
			if(typeof this.help == "undefined") return "";
			return this.help;
		}-*/;

		/**
		 * Set help
		 */
		public final native void setHelp(String help) /*-{
			this.help = help;
		}-*/;

		/**
		 * Get errorMessage
		 * @return errorMessage
		 */
		public final native String getErrorMessage() /*-{
			if(typeof this.errorMessage == "undefined") return "";
			return this.errorMessage;
		}-*/;

		/**
		 * Set error message
		 */
		public final native void setErrorMessage(String errorMessage) /*-{
			this.errorMessage = errorMessage;
		}-*/;

		/**
		 * Get locale
		 * @return locale
		 */
		public final native String getLocale() /*-{
			if(typeof this.locale == "undefined") return "";
			return this.locale;
		}-*/;

		/**
		 * Set locale
		 */
		public final native void setLocale(String locale) /*-{
			this.locale = locale;
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

}
