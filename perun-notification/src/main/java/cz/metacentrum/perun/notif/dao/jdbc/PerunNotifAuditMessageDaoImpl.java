package cz.metacentrum.perun.notif.dao.jdbc;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.notif.dao.PerunNotifAuditMessageDao;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Jdbc implementation of perunNotifAuditMessageDao
 *
 * @author tomas.tunkl
 *
 */
@Repository("perunNotifAuditMessageDao")
public class PerunNotifAuditMessageDaoImpl extends JdbcDaoSupport implements PerunNotifAuditMessageDao {

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifAuditMessageDao.class);

	public PerunNotifAuditMessage save(String message) {

		logger.debug("Saving perunNotifAuditMessage to db: message = {}", message);

		int newPerunNotifAuditMessageId = Utils.getNewId(this.getJdbcTemplate(), "pn_audit_message_id_seq");
		this.getJdbcTemplate().update("INSERT INTO pn_audit_message(id, message) values (?,?)", newPerunNotifAuditMessageId, message);

		logger.debug("PerunNotifAuditMessage saved to db: id = {} message = {}", newPerunNotifAuditMessageId, message);
		return new PerunNotifAuditMessage(newPerunNotifAuditMessageId, message);
	}

	public void remove(long id) {

		logger.debug("Removing perunNotifAuditMessage with id = {}", id);
		this.getJdbcTemplate().update("delete from pn_audit_message where id=?", id);
		logger.debug("PerunNotifAuditMessage with id: {} removed.", id);
	}

	public List<PerunNotifAuditMessage> getAll() {

		logger.debug("Listing all perunNotifAuditMessages.");
		List<PerunNotifAuditMessage> result = this.getJdbcTemplate().query("SELECT * FROM pn_audit_message", PerunNotifAuditMessage.PERUN_NOTIF_MESSAGE);
		logger.debug("Result of list of PerunNotifAuditMessage: {}", result);
		return result;
	}
}
