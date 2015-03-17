package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

import java.util.List;
import java.util.Map;

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
	List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Returns list of messages from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName consumer to get messages for
	 * @return list of messages
	 * @throws InternalErrorException
	 */
	List<String> pollConsumerMessages(String consumerName) throws InternalErrorException;

	/**
	 * Returns list of full messages from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName consumer to get messages for
	 * @return list of full messages
	 * @throws InternalErrorException
	 */
	List<String> pollConsumerFullMessages(String consumerName) throws InternalErrorException;

	/**
	 * Returns list of messages for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName consumer to get messages for
	 * @return list of messages for parser
	 * @throws InternalErrorException
	 */
	List<String> pollConsumerMessagesForParserSimple(String consumerName) throws InternalErrorException;

	/**
	 * Returns list of auditMessages for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName consumer to get messages for
	 * @return list of auditMessages for Ldap
	 * @throws InternalErrorException
	 */
	List<AuditMessage> pollConsumerMessagesForParser(String consumerName) throws InternalErrorException;

	/**
	 * Creates new auditer consumer with last processed id which equals auditer log max id.
	 *
	 * @param consumerName new name for consumer
	 * @throws InternalErrorException
	 */
	void createAuditerConsumer(String consumerName) throws InternalErrorException;

	/**
	 * Log auditer message
	 *
	 * @param sess
	 * @param message
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void log(PerunSession sess, String message) throws InternalErrorException;

	/**
	 * Get all auditer consumers like name=last_processed_id (map) from database.
	 *
	 * @param sess
	 * @return map string to integer where string is name of consumer and int is last_processed_id of consumer
	 * @throws InternalErrorException
	 */
	Map<String, Integer> getAllAuditerConsumers(PerunSession sess) throws InternalErrorException;

	/**
	 * Get last message id from auditer_log.
	 *
	 * @return last message id
	 * @throws InternalErrorException
	 */
	int getLastMessageId() throws InternalErrorException;
}
