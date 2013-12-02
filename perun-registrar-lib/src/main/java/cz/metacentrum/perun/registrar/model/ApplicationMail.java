package cz.metacentrum.perun.registrar.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.metacentrum.perun.registrar.model.Application.AppType;

/**
 * Object definition for mail notifications used
 * for registration process (messages to user and vo admins)
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: f3f9f50457a664e0e523f437310788b01e9539bd $
 */
public class ApplicationMail {
	
	// locale const
    public static final Locale EN = new Locale("en");
    public static final Locale CS = new Locale("cs");

    /**
     * Available mail types
     */
    public enum MailType {
    	
    	/**
    	 * Notification for user when application is created
    	 */
    	APP_CREATED_USER,
    	
    	/**
    	 * Notification to VO administrator when application is created
    	 */
    	APP_CREATED_VO_ADMIN,
    	
    	/**
    	 * Notification to user for email address validation
    	 */
    	MAIL_VALIDATION,
    	
    	/**
    	 * Notification to user when application is approved
    	 */
    	APP_APPROVED_USER,
    	
    	/**
    	 * Notification to user when application is rejected
    	 */
    	APP_REJECTED_USER,

        /**
         * Notification to VO administrator if auto approved application ends with error and is not approved.
         */
        APP_ERROR_VO_ADMIN;
    	
    }
    
    /**
     * Object params
     */
    private int id;
    private AppType appType;       // if mail is related to initial or extension application
    private int formId;            // connection to correct application form (VO)
    private MailType mailType;     // to what "action" is notification related
    private boolean send = true; // if sending email is enabled or disabled (enabled by default)
    // localized mail text (EN and CS by default)
    private Map<Locale, MailText> message = new HashMap<Locale, MailText>(3); {
    	message.put(CS,new MailText(CS));
    	message.put(EN,new MailText(EN));
    }
    
    public ApplicationMail(){};
    
    public ApplicationMail(int id,AppType appType, int formId, MailType mailType, boolean send) {
    	this.id = id;
    	this.appType = appType;
    	this.formId = formId;
    	this.mailType = mailType;
    	this.send = send;
    }
    
    public ApplicationMail(int id,AppType appType, int formId, MailType mailType, boolean send, Map<Locale, MailText> message) {
    	this(id, appType, formId, mailType, send);
    	this.message = message;
    }
    
    /**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the appType
	 */
	public AppType getAppType() {
		return appType;
	}

	/**
	 * @param appType the appType to set
	 */
	public void setAppType(AppType appType) {
		this.appType = appType;
	}

	/**
	 * @return the formId
	 */
	public int getFormId() {
		return formId;
	}

	/**
	 * @param formId the formId to set
	 */
	public void setFormId(int formId) {
		this.formId = formId;
	}

	/**
	 * @return the mailType
	 */
	public MailType getMailType() {
		return mailType;
	}

	/**
	 * @param mailType the mailType to set
	 */
	public void setMailType(MailType mailType) {
		this.mailType = mailType;
	}

	/**
	 * @return the send
	 */
	public boolean getSend() {
		return send;
	}

	/**
	 * @param send the send to set
	 */
	public void setSend(boolean send) {
		this.send = send;
	}

	/**
	 * @return the message
	 */
	public Map<Locale, MailText> getMessage() {
		return message;
	}
	
	/**
	 * Return message in specific language 
	 * (empty message if not present)
	 * 
	 * @param locale language
	 * @return the message
	 */
	public MailText getMessage(Locale locale) {
        MailText texts = message.get(locale);
        if(texts==null) {
            texts = new MailText();
            message.put(locale,texts);
        }
        return texts;
    }

	/**
	 * @param message the message to set
	 */
	public void setMessage(Map<Locale, MailText> message) {
		this.message = message;
	}

    /**
     * Return bean name as PerunBean does.
     *
     * @return Class simple name (beanName)
     */
    public String getBeanName() {
        return this.getClass().getSimpleName();
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ApplicationMail))
			return false;
		ApplicationMail other = (ApplicationMail) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
    public String toString() {
    	return this.getClass().getSimpleName()+":[" +
    			"id='" + getId() + '\'' +
    			", appType='" + getAppType().toString() + '\'' +
    			", formId='" + getFormId() + '\'' +
    			", mailType='" + getMailType().toString() + '\'' +
    			", send='" + getSend() + '\'' +
    			", message='" + getMessage().toString() + '\'' +
    			']';
    }

	/**
     * Inner class used for localized texts in mail message
     */
    public static class MailText {
        private Locale locale;
        private String subject;
        private String text;

        public MailText() {
        }
        
        public MailText(Locale locale) {
        	this.locale = locale;
        }

        public MailText(Locale locale, String subject, String text) {
            this.locale = locale;
            this.subject = subject;
            this.text = text;
        }

        public Locale getLocale() {
            return locale;
        }
        
        public void setLocale(Locale locale) {
            this.locale= locale ;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		@Override
        public String toString() {
        	return this.getClass().getSimpleName()+":[" +
        			"locale='" + getLocale() + '\'' +
        			"subject='" + getSubject() + '\'' +
        			", text='" + getText() + '\'' +
        			']';
        }
        
    }

}