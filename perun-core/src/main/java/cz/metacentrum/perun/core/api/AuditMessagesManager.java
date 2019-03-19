package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

import java.util.List;
import java.util.Map;

/**
 * AuditMessagesManager manages audit messages (logs). Entry Logic interface.
 *
 * @author Michal Stava
 */
public interface AuditMessagesManager {

	Integer COUNTOFMESSAGES = 100;

	/**
	 * Returns reasonable number of messages from audit's logs which is determined by the constant <b>CountOfMessages<b/>.
	 *
	 * @param perunSession perun session
	 * @return list of audit's messages
	 */
	List<AuditMessage> getMessages(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Returns x messages from audit's logs, where x = count.
	 *
	 * @param perunSession perun session
	 * @param count        Count of returned messages.
	 * @return list of audit's messages
	 */
	List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Return less than count or equals to count messages from audit's logs.
	 *
	 * <b>IMPORTANT:</b> This variant do not guarantee returning just count of messages!
	 * Return messages by Id from max_id to max_id-count (can be less then count messages).
	 *
	 * @param perunSession perun session
	 * @param count        Count of returned messages
	 * @return list of audit's messages
	 */
	List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Returns list of messages from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return list of messages
	 */
	List<String> pollConsumerMessages(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of <b>full</b> messages from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return list of full messages
	 */
	List<String> pollConsumerFullMessages(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of messages for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return list of messages for parser
	 */
	List<String> pollConsumerMessagesForParserSimple(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of <b>auditMessages</b> for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return list of auditMessages for parser
	 */
	List<AuditMessage> pollConsumerMessagesForParser(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of <b>AuditEvents</b> from audit's log which id is bigger than last processed id.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return list of auditMessages for parser
	 */
	List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Creates new auditer consumer with last processed id which equals auditer log max id.
	 *
	 * @param perunSession perun session
	 * @param consumerName new name for consumer
	 */
	void createAuditerConsumer(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Log auditer message.
	 *
	 * @param perunSession perun session
	 * @param message      message to be logged
	 */
	void log(PerunSession perunSession, String message) throws InternalErrorException, PrivilegeException;

	/**
	 * Get all auditer consumers from database. In map is String = name and Integer = lastProcessedId.
	 *
	 * @param perunSession perun session
	 * @return map string to integer where string is name of consumer and int is last_processed_id of consumer
	 */
	Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Get id of last message from auditer_log.
	 *
	 * @param perunSession perun session
	 * @return last message id
	 */
	int getLastMessageId(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Set last processed ID of message in consumer with consumerName.
	 *
	 * @param consumerName    name of consumer
	 * @param lastProcessedId id of last processed message in consumer
	 */
	void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException;

	/**
	 * Get number of messages in auditer log.
	 *
	 * @return number of messages in auditer log
	 */
	int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException;
}
