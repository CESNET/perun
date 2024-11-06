package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Object definition for mail notifications used for registration process (messages to user and vo admins)
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ApplicationMail {

  // locale const
  public static final Locale EN = new Locale("en");
  public static final Locale CS = getNativeLanguage();
  /**
   * Object params
   */
  private int id;
  private AppType appType;       // if mail is related to initial or extension application
  private int formId;            // connection to correct application form (VO)
  private MailType mailType;     // to what "action" is notification related
  private boolean send = true; // if sending email is enabled or disabled (enabled by default)
  // localized mail text (EN and CS by default)
  private Map<Locale, MailText> message = new HashMap<Locale, MailText>();
  private Map<Locale, MailText> htmlMessage = new HashMap<Locale, MailText>();

  {
    if (CS != null) {
      message.put(CS, new MailText(CS));
    }
    message.put(EN, new MailText(EN));
  }

  {
    if (CS != null) {
      htmlMessage.put(CS, new MailText(CS, true));
    }
    htmlMessage.put(EN, new MailText(EN, true));
  }

  public ApplicationMail() {
  }

  public ApplicationMail(int id, AppType appType, int formId, MailType mailType, boolean send) {
    this.id = id;
    this.appType = appType;
    this.formId = formId;
    this.mailType = mailType;
    this.send = send;
  }

  public ApplicationMail(int id, AppType appType, int formId, MailType mailType, boolean send,
                         Map<Locale, MailText> message) {
    this(id, appType, formId, mailType, send);
    this.message = message;
  }

  public ApplicationMail(int id, AppType appType, int formId, MailType mailType, boolean send,
                         Map<Locale, MailText> message, Map<Locale, MailText> htmlMessage) {
    this(id, appType, formId, mailType, send);
    this.message = message;
    this.htmlMessage = htmlMessage;
  }

  /**
   * Return code of native language defined in config file. Return NULL if no native language set.
   *
   * @return String representation of native language
   */
  public static Locale getNativeLanguage() {
    try {
      String loc = BeansUtils.getCoreConfig().getNativeLanguage().split(",")[0];
      if (loc != null && loc.trim().isEmpty()) {
        return null;
      }
      return new Locale(loc);
    } catch (Exception ex) {
      return null;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ApplicationMail other)) {
      return false;
    }
    return id == other.id;
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
   * Return bean name as PerunBean does.
   *
   * @return Class simple name (beanName)
   */
  public String getBeanName() {
    return this.getClass().getSimpleName();
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
   * Return html message in specific language (empty message if not present)
   *
   * @param locale language
   * @return the message
   */
  public MailText getHtmlMessage(Locale locale) {
    MailText texts = htmlMessage.get(locale);
    if (texts == null) {
      texts = new MailText();
      message.put(locale, texts);
    }
    return texts;
  }

  /**
   * @return the html message
   */
  public Map<Locale, MailText> getHtmlMessage() {
    return htmlMessage;
  }

  /**
   * @param htmlMessage the html message to set
   */
  public void setHtmlMessage(Map<Locale, MailText> htmlMessage) {
    this.htmlMessage = htmlMessage;
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
   * Return message in specific language (empty message if not present)
   *
   * @param locale language
   * @return the message
   */
  public MailText getMessage(Locale locale) {
    MailText texts = message.get(locale);
    if (texts == null) {
      texts = new MailText();
      message.put(locale, texts);
    }
    return texts;
  }

  /**
   * @return the message
   */
  public Map<Locale, MailText> getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(Map<Locale, MailText> message) {
    this.message = message;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ":[" + "id='" + getId() + '\'' + ", appType='" + getAppType().toString() +
           '\'' + ", formId='" + getFormId() + '\'' + ", mailType='" + getMailType().toString() + '\'' + ", send='" +
           getSend() + '\'' + ", message='" + getMessage().toString() + '\'' + ", htmlMessage='" +
           getHtmlMessage().toString() + '\'' + ']';
  }

  /**
   * Available mail types
   */
  public enum MailType {

    /**
     * Notification for user when application is created
     */
    APP_CREATED_USER,

    /**
     * Notification for user when group application is created and it can be approved (when the user becomes member in
     * VO).
     */
    APPROVABLE_GROUP_APP_USER,

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
    APP_ERROR_VO_ADMIN,

    /**
     * Notification to User with invitation to VO / group
     */
    USER_INVITE,

    /**
     * Notification to User with pre-approved invitation to group
     */
    USER_PRE_APPROVED_INVITE

  }

  /**
   * Inner class used for localized texts in mail message
   */
  public static class MailText {
    private Locale locale;
    private boolean htmlFormat;
    private String subject;
    private String text;

    public MailText() {
    }

    public MailText(Locale locale) {
      this.locale = locale;
    }

    public MailText(Locale locale, boolean htmlFormat) {
      this.locale = locale;
      this.htmlFormat = htmlFormat;
    }

    public MailText(Locale locale, String subject, String text) {
      this.locale = locale;
      this.subject = subject;
      this.text = text;
    }

    public MailText(Locale locale, boolean htmlFormat, String subject, String text) {
      this.locale = locale;
      this.htmlFormat = htmlFormat;
      this.subject = subject;
      this.text = text;
    }

    public boolean getHtmlFormat() {
      return htmlFormat;
    }

    public void setHtmlFormat(boolean htmlFormat) {
      this.htmlFormat = htmlFormat;
    }

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }

    public String getSubject() {
      return subject;
    }

    public void setSubject(String subject) {
      this.subject = subject;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName() + ":[" + "locale='" + getLocale() + '\'' + "subject='" + getSubject() +
             '\'' + ", text='" + getText() + '\'' + ']';
    }

  }

}
