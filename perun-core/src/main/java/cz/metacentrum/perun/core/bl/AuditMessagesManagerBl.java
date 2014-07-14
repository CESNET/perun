package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

import java.util.List;

/**
 * UsersManager manages users.
 *
 * @author Michal Stava
 */
public interface AuditMessagesManagerBl {

	/**
	 * Returns countOfMessages messages from audit's logs.
	 *
	 * @param perunSession
	 * @param count Count of returned messages.
	 * @return list of audit's messages
	 * @throws InternalErrorException
	 */
	List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Return less than count or equals to count messages from audit's logs.
	 *
	 * Important: This variant do not guarantee returning just count of messages!
	 *						Return messages by Id from max_id to max_id-count (can be less then count messages)
	 *
	 * @param perunSession
	 * @param count Count of returned messages
	 * @return list of audit's messages
	 * @throws InternalErrorException
	 */
	public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Log auditer message
	 *
	 * @param sess
	 * @param message
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void log(PerunSession sess, String message) throws InternalErrorException;
}
