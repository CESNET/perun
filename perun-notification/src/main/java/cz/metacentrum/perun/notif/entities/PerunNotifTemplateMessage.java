package cz.metacentrum.perun.notif.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import org.springframework.jdbc.core.RowMapper;

/**
 * Localized freemarker template
 * <p>
 * User: tomastunkl Date: 10.10.12 Time: 22:33
 * <p>
 * Table pn_template_message
 */
public class PerunNotifTemplateMessage {

  public static final RowMapper<PerunNotifTemplateMessage> PERUN_NOTIF_TEMPLATE_MESSAGE_ROW_MAPPER =
      new RowMapper<PerunNotifTemplateMessage>() {

        public PerunNotifTemplateMessage mapRow(ResultSet rs, int i) throws SQLException {

          PerunNotifTemplateMessage result = new PerunNotifTemplateMessage();
          result.setId(rs.getInt("id"));
          result.setTemplateId(rs.getInt("template_id"));
          result.setLocale((rs.getString("locale") == null) ? null : new Locale(rs.getString("locale")));
          result.setMessage(rs.getString("message"));
          result.setSubject(rs.getString("subject"));

          return result;
        }
      };
  /**
   * Unique identifier
   * <p>
   * Column id Sequence pn_pattern_id_seq
   */
  private int id;
  /**
   * TemplateId which owns templateMessage
   * <p>
   * Column template_id
   */
  private int templateId;
  /**
   * Defines locale of template
   * <p>
   * Column locale
   */
  private Locale locale;
  /**
   * Holds freemarker template
   */
  private String message;
  /**
   * Subject of message
   */
  private String subject;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getTemplateId() {
    return templateId;
  }

  public void setTemplateId(int templateId) {
    this.templateId = templateId;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void update(PerunNotifTemplateMessage messageFromDb) {

    this.setLocale(messageFromDb.getLocale());
    this.setMessage(messageFromDb.getMessage());
    this.setSubject(messageFromDb.getSubject());
  }

  @Override
  public String toString() {
    return "id: " + getId() + " template id: " + getTemplateId() + " locale: " + getLocale().getLanguage()
        + " message: " + getMessage() + " subject: " + getSubject();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PerunNotifTemplateMessage)) {
      return false;
    }

    PerunNotifTemplateMessage that = (PerunNotifTemplateMessage) o;

    if (id != that.id) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + templateId;
    result = 31 * result + (locale != null ? locale.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    result = 31 * result + (locale != null ? locale.hashCode() : 0);
    return result;
  }
}
