package cz.metacentrum.perun.notif.entities;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This entity represents one message from auditer. Is used to backup messages
 * during process of processing the message.
 *
 * @author tomas.tunkl
 *
 */
//Table name is pn_audit_message
public class PerunNotifAuditMessage {

	/**
	 * Column id Sequence pn_audit_message_id_seq
	 *
	 * Unique id of message
	 */
	private long id;

	/**
	 * Column message
	 *
	 * Holds audit message
	 */
	private String message;

	/**
	 * Computed from message using auditparser
	 */
	private List<PerunBean> perunBeanList;

	public PerunNotifAuditMessage() {
	}

	public PerunNotifAuditMessage(long id, String message) {
		this.id = id;
		this.message = message;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "id: " + getId() + " message: " + getMessage();
	}

	/**
	 * Parses message from auditer to list of perunBeans, this parsing is
	 * done only once
	 *
	 * @return
	 * @throws InternalErrorException
	 */
	public List<PerunBean> getPerunBeanList() throws InternalErrorException {

		if (perunBeanList == null) {
			perunBeanList = AuditParser.parseLog(message);
		}
		return perunBeanList;
	}

	/**
	 * RowMapper to load entity from db row
	 */
	public static final RowMapper<PerunNotifAuditMessage> PERUN_NOTIF_MESSAGE = new RowMapper<PerunNotifAuditMessage>() {

		public PerunNotifAuditMessage mapRow(ResultSet rs, int i) throws SQLException {

			PerunNotifAuditMessage auditMessage = new PerunNotifAuditMessage();
			auditMessage.setId(rs.getLong("id"));
			auditMessage.setMessage(rs.getString("message"));

			return auditMessage;
		}
	};
}
