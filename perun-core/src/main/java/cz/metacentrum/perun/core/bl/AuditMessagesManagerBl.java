package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.audit.events.AuditEvent;
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
	 * Returns newest X audit messages, where X is a number specified by count param.
	 *
	 * @param perunSession perun session
	 * @param count        Count of returned messages.
	 * @return List of audit messages
	 */
	List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Returns messages from audit log where param 'count' is applied to message IDs starting with current max_id.
	 * It returns messages by their IDs from max_id to max_id-count (can be less than count messages).
	 *
	 * @param perunSession perun session
	 * @param count int Number of IDs to subtract from max_id
	 * @return List of audit messages
	 */
	List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Returns list of <b>AuditMessages</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return List of audit messages
	 */
	List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) throws InternalErrorException;

	/**
	 * Returns list of <b>AuditEvents</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return List of audit messages
	 */
	List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) throws InternalErrorException;

	/**
	 * Creates new auditer consumer with last processed id which equals current auditer log max id.
	 *
	 * @param perunSession perun session
	 * @param consumerName new name for consumer
	 */
	void createAuditerConsumer(PerunSession perunSession, String consumerName) throws InternalErrorException;

	/**
	 * Log auditer message.
	 *
	 * @param perunSession perun session
	 * @param message      message to be logged
	 */
	void log(PerunSession perunSession, String message) throws InternalErrorException;

	/**
	 * Get all registered auditer consumers as map name(String)=lastProcessedId(Integer).
	 *
	 * @param perunSession perun session
	 * @return Mapping of auditer consumer names to their last processed ID.
	 */
	Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Get ID of last message in auditer log (max_id).
	 *
	 * @param perunSession perun session
	 * @return Last message id
	 */
	int getLastMessageId(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Set last processed ID of message in consumer with consumerName.
	 *
	 * @param consumerName    name of consumer
	 * @param lastProcessedId id of last processed message in consumer
	 */
	void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) throws InternalErrorException;

	/**
	 * Get count of all messages in auditer log.
	 *
	 * @return Count of all messages in auditer log
	 */
	int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException;

}
