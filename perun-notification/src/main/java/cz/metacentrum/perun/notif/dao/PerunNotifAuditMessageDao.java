package cz.metacentrum.perun.notif.dao;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;

import java.util.List;

/**
 * Dao for work with perunNotifAuditMessageDao in db
 *
 * @author tomas.tunkl
 *
 */
public interface PerunNotifAuditMessageDao {

	/**
	 * Saves message to db and creates perunNotifAuditMessage
	 *
	 * @param message
	 * @return
	 */
	public PerunNotifAuditMessage save(String message);

	/**
	 * Removes perunNotifAuditMessage using id of perunNotifAuditMessage
	 *
	 * @param id
	 */
	public void remove(long id);

	/**
	 * Gets all perun messages from db, these messages were not processed
	 * This can happen for example during restart of server
	 *
	 * @return
	 */
	public List<PerunNotifAuditMessage> getAll();
}
