package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;

import java.util.List;

/**
 * Manager interface for work with messages send by auditer Is Used to backup
 * messages which are in process of processing. After processing of message,
 * this message should be erased from db.
 *
 * @author tomas.tunkl
 *
 */
public interface PerunNotifAuditMessageManager {

	/**
	 * Saves message from auditer to perunAuditerMessage
	 *
	 * @param messasge
	 * @return
	 */
	public PerunNotifAuditMessage saveMessageToPerunAuditerMessage(String messasge, PerunSession message);

	/**
	 * Removes perunAuditerMessage from db based on id
	 *
	 * @param id
	 */
	public void removePerunAuditerMessageById(long id);

	/**
	 * Gets all perunNotifAuditMessages from db.
	 *
	 * @return
	 */
	public List<PerunNotifAuditMessage> getAll();
}
