package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;
import java.util.Map;

/**
 * AuditMessagesManager manages audit messages (logs). BlImpl Logic interface.
 *
 * @author Michal Stava
 */
public interface AuditMessagesManagerBl {

	/**
	 * Returns countOfMessages messages from audit's logs.
	 *
	 * @param perunSession perun session
	 * @param count Count of returned messages.
	 * @return list of audit's messages
	 * @throws InternalErrorException
	 */
	List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Return less than count or equals to count messages from audit's logs.
	 *
	 * <b>IMPORTANT:</b> This variant do not guarantee returning just count of messages!
	 * Return messages by Id from max_id to max_id-count (can be less then count messages).
	 *
	 * @param perunSession perun session
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
	 * Returns list of <b>full</b> messages from audit's log which id is bigger than last processed id.
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
	 * Returns list of <b>auditMessages</b> for parser from audit's log which id is bigger than last processed id.
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
	 * Log auditer message.
	 *
	 * @param perunSession perunSession
	 * @param message message to be logged
	 * @throws InternalErrorException
	 */
	void log(PerunSession perunSession, String message) throws InternalErrorException;

	/**
	 * Get all auditer consumers from database. In map is String = name and Integer = lastProcessedId.
	 *
	 * @param perunSession perunSession
	 * @return map string to integer where string is name of consumer and int is last_processed_id of consumer
	 * @throws InternalErrorException
	 */
	Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Get id of last message from auditer_log.
	 *
	 * @return last message id
	 * @throws InternalErrorException
	 */
	int getLastMessageId() throws InternalErrorException;

	/**
	 * Set last processed ID of message in consumer with consumerName.
	 *
	 * @param consumerName name of consumer
	 * @param lastProcessedId id of last processed message in consumer
	 * @throws InternalErrorException
	 */
	void setLastProcessedId(String consumerName, int lastProcessedId) throws InternalErrorException;

	/**
	 * Get number of messages in auditer log.
	 *
	 * @param perunSession
	 * @return number of messages in auditer log
	 * @throws InternalErrorException
	 */
	int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException;
}
