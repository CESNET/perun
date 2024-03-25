package cz.metacentrum.perun.notif.entities;

import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 * Represents one receiver of message, is defined in template
 * <p>
 * Table pn_receiver
 *
 * @author tomas.tunkl
 */
public class PerunNotifReceiver {

  public static final RowMapper<PerunNotifReceiver> PERUN_NOTIF_RECEIVER = new RowMapper<PerunNotifReceiver>() {

    public PerunNotifReceiver mapRow(ResultSet rs, int i) throws SQLException {

      PerunNotifReceiver receiver = new PerunNotifReceiver();
      receiver.setTarget(rs.getString("target"));
      receiver.setTypeOfReceiver(PerunNotifTypeOfReceiver.resolve(rs.getString("type_of_receiver")));
      receiver.setTemplateId(rs.getInt("template_id"));
      receiver.setLocale(rs.getString("locale"));
      receiver.setId(rs.getInt("id"));

      return receiver;
    }
  };
  /**
   * Unique identifier of receiver
   * <p>
   * Column id
   */
  private Integer id;
  /**
   * Type of receiver
   * <p>
   * Column type_of_receiver
   */
  private PerunNotifTypeOfReceiver typeOfReceiver;
  /**
   * Defines target of receiver, usually contains function to get email or jabber number...
   * <p>
   * Column target
   */
  private String target;
  /**
   * Template Id to which receiver is connected
   * <p>
   * Column template_id
   */
  private Integer templateId;
  /**
   * Locale of receiver - determines a language, which is used for sending messages
   * <p>
   * Column locale
   */
  private String locale;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PerunNotifReceiver)) {
      return false;
    }

    PerunNotifReceiver receiver = (PerunNotifReceiver) o;

    if (id != null ? !id.equals(receiver.id) : receiver.id != null) {
      return false;
    }

    return true;
  }

  public Integer getId() {
    return id;
  }

  public String getLocale() {
    return locale;
  }

  public String getTarget() {
    return target;
  }

  public Integer getTemplateId() {
    return templateId;
  }

  public PerunNotifTypeOfReceiver getTypeOfReceiver() {
    return typeOfReceiver;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (typeOfReceiver != null ? typeOfReceiver.hashCode() : 0);
    result = 31 * result + (target != null ? target.hashCode() : 0);
    result = 31 * result + (templateId != null ? templateId.hashCode() : 0);
    result = 31 * result + (locale != null ? locale.hashCode() : 0);
    return result;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setTemplateId(Integer templateId) {
    this.templateId = templateId;
  }

  public void setTypeOfReceiver(PerunNotifTypeOfReceiver typeOfReceiver) {
    this.typeOfReceiver = typeOfReceiver;
  }

  @Override
  public String toString() {
    return "id: " + getId() + " target: " + getTarget() + " type_of_receiver: " + getTypeOfReceiver() +
           " template id: " + getTemplateId() + " locale: " + getLocale();
  }

  public void update(PerunNotifReceiver newReceiver) {
    this.setTarget(newReceiver.getTarget());
    this.setTypeOfReceiver(newReceiver.getTypeOfReceiver());
    this.setLocale(newReceiver.getLocale());
  }
}
