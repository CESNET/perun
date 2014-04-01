package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

/**
 * Overlay type for registrar: MailText
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */

public class MailText extends JavaScriptObject {

	protected MailText() {}

	/**
	 * Creates a new MailTexts
	 *
	 * @param locale
	 * @param subject
	 * @param text
	 * @return
	 */
	static public MailText construct(String locale, String subject, String text)
	{
		MailText txt = new JSONObject().getJavaScriptObject().cast();

		txt.setLocale(locale);
		txt.setSubject(subject);
		txt.setText(text);

		return txt;
	}

	/**
	 * Get subject
	 * @return subject
	 */
	public final native String getSubject() /*-{
		if(typeof this.subject === 'undefined') return "";
		return this.subject;
	}-*/;

		/**
		 * Set subject
		 */
		public final native void setSubject(String subject) /*-{
			this.subject = subject;
		}-*/;


		/**
		 * Get text
		 * @return text
		 */
		public final native String getText() /*-{
			if(typeof this.text === 'undefined') return "";
			return this.text;
		}-*/;

		/**
		 * Set text
		 */
		public final native void setText(String text) /*-{
			this.text = text;
		}-*/;



		/**
		 * Get locale
		 * @return locale
		 */
		public final native String getLocale() /*-{
			if(typeof this.locale === 'undefined') return "";
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
