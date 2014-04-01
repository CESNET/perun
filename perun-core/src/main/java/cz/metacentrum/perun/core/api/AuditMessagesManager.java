package cz.metacentrum.perun.core.api;

import java.util.List;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongRangeOfCountException;


/**
 * AuditMessagesManager manages audit messages (logs).
 *
 * @author Michal Stava
 */
public interface AuditMessagesManager {

	public final static Integer COUNTOFMESSAGES=100;

	/**
	 * Returns reasonable number of messages from audit's logs which is determined by the constant CountOfMessages.
	 *
	 * @param perunSession
	 * @return list of audit's messages
	 * @throws InternalErrorException
	 * @throws WrongRangeOfCountException
	 */
	List<AuditMessage> getMessages(PerunSession perunSession) throws InternalErrorException,WrongRangeOfCountException;

	/**
	 * Returns x messages from audit's logs, where x = count
	 *
	 * @param perunSession
	 * @param count Count of returned messages.
	 * @return list of audit's messages
	 * @throws InternalErrorException
	 * @throws WrongRangeOfCountException
	 */
	List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException,WrongRangeOfCountException;

	/**
	 * Log auditer message
	 *
	 * @param sess
	 * @param message
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void log(PerunSession sess, String message) throws InternalErrorException, PrivilegeException;
}
