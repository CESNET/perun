package cz.metacentrum.perun.notif.entities;

import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import org.springframework.jdbc.core.RowMapper;

/**
 * Represents one receiver of message, is defined in template
 *
 * Table pn_receiver
 *
 * @author tomas.tunkl
 *
 */
public class PerunNotifReceiver {

	/**
	 * Unique identifier of receiver
	 *
	 * Column id
	 */
	private Integer id;

	/**
	 * Type of receiver
	 *
	 * Column type_of_receiver
	 */
	private PerunNotifTypeOfReceiver typeOfReceiver;

	/**
	 * Defines target of receiver, usually contains function to get email or
	 * jabber number...
	 *
	 * Column target
	 */
	private String target;

	/**
	 * Template Id to which receiver is connected
	 *
	 * Column template_id
	 */
	private Integer templateId;

	/**
	 * Locale of receiver - determines a language, which is used for sending messages
	 *
	 * Column locale
	 */
	private String locale;

	public PerunNotifTypeOfReceiver getTypeOfReceiver() {
		return typeOfReceiver;
	}

	public void setTypeOfReceiver(PerunNotifTypeOfReceiver typeOfReceiver) {
		this.typeOfReceiver = typeOfReceiver;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

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

	public void update(PerunNotifReceiver newReceiver) {
		this.setTarget(newReceiver.getTarget());
		this.setTypeOfReceiver(newReceiver.getTypeOfReceiver());
		this.setLocale(newReceiver.getLocale());
	}

	@Override
	public String toString() {
		return "id: " + getId() + " target: " + getTarget() + " type_of_receiver: " + getTypeOfReceiver() + " template id: " + getTemplateId() + " locale: " + getLocale();
	}

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

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (typeOfReceiver != null ? typeOfReceiver.hashCode() : 0);
		result = 31 * result + (target != null ? target.hashCode() : 0);
		result = 31 * result + (templateId != null ? templateId.hashCode() : 0);
		result = 31 * result + (locale != null ? locale.hashCode() : 0);
		return result;
	}
}
