package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;

import java.util.List;
import java.util.Map;

/**
 * Manager for work with PerunNotifPoolMessage
 */
public interface PerunNotifPoolMessageManager {

	/**
	 * Saves all poolMessages to db
	 *
	 * @param poolMessages
	 */
	public void savePerunNotifPoolMessages(List<PerunNotifPoolMessage> poolMessages) throws InternalErrorException;

	/**
	 * Creates new perunNotifPoolMessages for templates and perunMessage
	 *
	 * @param templates
	 * @param perunAuditMessage
	 * @return
	 * @throws InternalErrorException
	 */
	public List<PerunNotifPoolMessage> createPerunNotifPoolMessagesForTemplates(Map<Integer, List<PerunNotifTemplate>> templates, PerunNotifAuditMessage perunAuditMessage) throws InternalErrorException;

	/**
	 * Process poolMessages from db. Orders them by templateId and
	 * keyAttributes and checks conditionals set in templates whether sent
	 * notification or not. Procesed messages are removed from db.
	 */
	public void processPerunNotifPoolMessagesFromDb();
}
