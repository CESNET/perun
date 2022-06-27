package cz.metacentrum.perun.webgui.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONObject;
import cz.metacentrum.perun.webgui.client.localization.ObjectTranslation;
import cz.metacentrum.perun.webgui.json.JsonUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * Overlay type for registrar: ApplicationMail
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ApplicationMail extends JavaScriptObject {

	protected ApplicationMail() {}


	/**
	 * Creates a new object
	 *
	 * @param appType
	 * @param formId
	 * @param mailType
	 * @param send
	 * @param message
	 * @return
	 */
	static public ApplicationMail construct(String appType, int formId, String mailType, boolean send, Map<String, MailText> message) {
		ApplicationMail mail = new JSONObject().getJavaScriptObject().cast();

		mail.setAppType(appType);
		mail.setFormId(formId);
		mail.setMailType(mailType);
		mail.setSend(send);

		for(Map.Entry<String, MailText> entry : message.entrySet()) {
			mail.setMessage(entry.getKey(), entry.getValue());
		}

		return mail;
	}

	/**
	 * Get ID
	 * @return id
	 */
	public final native int getId() /*-{
		return this.id;
	}-*/;

	/**
	 * Connection to correct application form (VO)
	 * @return id
	 */
	public final native int getFormId() /*-{
		return this.formId;
	}-*/;

	/**
	 * Connection to correct application form (VO)
	 * @param id
	 */
	public final native void setFormId(int id) /*-{
		this.formId = id;
	}-*/;

	/**
	 * Get mail type
	 * @return mail type
	 */
	public final native String getMailType() /*-{
		if(typeof this.mailType === 'undefined'){
			this.mailType = "";
		}
		return this.mailType;
	}-*/;

	/**
	 * Set mail type
	 */
	public final native void setMailType(String mailType) /*-{
		this.mailType = mailType;
	}-*/;

	/**
	 * Get app type
	 * @return app type
	 */
	public final native String getAppType() /*-{
		if(typeof this.appType === 'undefined'){
			this.appType = "";
		}
		return this.appType;
	}-*/;

	/**
	 * Set app type
	 */
	public final native void setAppType(String appType) /*-{
		this.appType = appType;
	}-*/;


	/**
	 * Get message
	 * @return MailText
	 */
	public final native MailText getMessage(String locale) /*-{
		if(typeof this.message === 'undefined'){
			this.message = {};
		}
		if(!(locale in this.message)){
			this.message[locale] = {locale: locale, htmlFormat: false, text : "", subject : ""};
		}
		return this.message[locale];
	}-*/;

	/**
	 * Get message of HTML template
	 * @return MailText
	 */
	public final native MailText getMessageHTML(String locale) /*-{
		if(typeof this.htmlMessage === 'undefined'){
			this.htmlMessage = {};
		}
		if(!(locale in this.htmlMessage)){
			this.htmlMessage[locale] = {locale: locale, htmlFormat: true, text : "", subject : ""};
		}
		return this.htmlMessage[locale];
	}-*/;

	/**
	 * Set message
	 * @param locale
	 * @param message
	 */
	public final native void setMessage(String locale, MailText message) /*-{
		if(typeof this.message === 'undefined'){
			this.message = {};
		}
		this.message[locale] = message;
	}-*/;

	/**
	 * Set message
	 * @param locale
	 * @param message
	 */
	public final native void setMessageHTML(String locale, MailText message) /*-{
		if(typeof this.htmlMessage === 'undefined'){
			this.htmlMessage = {};
		}
		this.htmlMessage[locale] = message;
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
	 * Get locales of HTML message
	 *
	 * @return array of present locales
	 */
	public final ArrayList<String> getLocalesHTML() {
		return JsonUtils.listFromJsArrayString(getLocalesNativeHTML());
	}


	/**
	 * Get locales
	 *
	 * @return array of present locales
	 */
	public final native JsArrayString getLocalesNative() /*-{
		if(typeof this.message !== 'undefined' && this.message !== null){
			return Object.getOwnPropertyNames(this.message)
		}
		return null;
	}-*/;

	/**
	 * Get locales of HTML message
	 *
	 * @return array of present locales
	 */
	public final native JsArrayString getLocalesNativeHTML() /*-{
		if(typeof this.htmlMessage !== 'undefined' && this.htmlMessage !== null){
			return Object.getOwnPropertyNames(this.htmlMessage)
		}
		return null;
	}-*/;


	/**
	 * Get sending enabled
	 * @return whether is sending enabled
	 */
	public final native boolean isSend() /*-{
		return this.send;
	}-*/;

	/**
	 * Set sending enabled
	 * @param send is sending enabled
	 */
	public final native void setSend(boolean send) /*-{
		this.send = send;
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
	 * VALID, INVALID, EXPIRED, DISABLED
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
	public final boolean equals(ApplicationMail o)
	{
		return o.getId() == this.getId();
	}

	/**
	 * Return translated version of current mail type or empty string
	 * @return translated mail type
	 */
	public static final String getTranslatedMailType(String type) {

		if ("APP_CREATED_USER".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeAppCreatedUser();
		} else if ("APP_CREATED_VO_ADMIN".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeAppCreatedVoAdmin();
		} else if ("MAIL_VALIDATION".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeMailValidation();
		} else if ("APP_APPROVED_USER".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeAppApprovedUser();
		} else if ("APP_REJECTED_USER".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeAppRejectedUser();
		} else if ("APP_ERROR_VO_ADMIN".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeAppErrorVoAdmin();
		} else if ("USER_INVITE".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeInvitationUser();
		} else if ("APPROVABLE_GROUP_APP_USER".equalsIgnoreCase(type)) {
			return ObjectTranslation.INSTANCE.applicationMailTypeApprovableUser();
		} else {
			return "";
		}

	}

	/**
	 * Define possible values of MailType
	 */
	public enum MailType {
		APP_CREATED_USER,
		APP_CREATED_VO_ADMIN,
		MAIL_VALIDATION,
		APPROVABLE_GROUP_APP_USER,
		APP_APPROVED_USER,
		APP_REJECTED_USER,
		APP_ERROR_VO_ADMIN,
		USER_INVITE
	}

}
