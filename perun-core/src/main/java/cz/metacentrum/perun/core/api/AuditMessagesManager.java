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
	 * Return less than count or equals to count messages from audit's logs.
	 *
	 * Important: This variant do not guarantee returning just count of messages!
	 *						Return messages by Id from max_id to max_id-count (can be less then count messages)
	 *
	 * @param perunSession
	 * @param count Count of returned messages
	 * @return list of audit's messages
	 * @throws InternalErrorException
	 * @throws WrongRangeOfCountException
	 */
	List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException,WrongRangeOfCountException;

	/**
	 * Returns list of messages from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession
	 * @param consumerName consumer to get messages for
	 * @return list of messages
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<String> pollConsumerMessages(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of full messages from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession
	 * @param consumerName consumer to get messages for
	 * @return list of full messages
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<String> pollConsumerFullMessages(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of messages for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession
	 * @param consumerName consumer to get messages for
	 * @return list of messages for parser
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	 List<String> pollConsumerMessagesForParserSimple(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of auditMessages for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession
	 * @param consumerName consumer to get messages for
	 * @return list of auditMessages for parser
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	 List<AuditMessage> pollConsumerMessagesForParser(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Creates new auditer consumer with last processed id which equals auditer log max id.
	 *
	 * @param perunSession
	 * @param consumerName new name for consumer
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	 void createAuditerConsumer(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

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
