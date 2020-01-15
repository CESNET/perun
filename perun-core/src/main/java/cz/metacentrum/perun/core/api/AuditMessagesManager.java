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
 * @author Pavel Zlámal
 */
public interface AuditMessagesManager {

	Integer COUNTOFMESSAGES = 100;

	/**
	 * Returns 100 newest audit messages from audit log. If there is a less messages than 100,
	 * then all of them are returned.
	 *
	 * @param perunSession perun session
	 * @return List of 100 newest audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditMessage> getMessages(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Returns exact number of newest audit messages defined by 'count' param (disregarding message IDs).
	 * If there is less messages present, then all of them are returned.
	 *
	 * @param perunSession perun session
	 * @param count        Count of returned messages.
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Returns all messages with IDs within the range from max(ID) to (max(ID)-count), where number of returned messages
	 * is equal or less than 'count' param, because some IDs could be skipped in the sequence.
	 *
	 * @param perunSession perun session
	 * @param count Number of IDs to subtract from max(ID)
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException;

	/**
	 * Returns list of <b>AuditMessages</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When you are not authorized to poll messages
	 */
	@Deprecated
	List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of <b>AuditMessages</b> from audit log with IDs > lastProcessedId given.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @param lastProcessedId id of the last message 
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When you are not authorized to poll messages
	 */
	List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of <b>AuditEvents</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When you are not authorized to poll events
	 */
	@Deprecated
	List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of <b>AuditEvents</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @param lastProcessedId id of the last event
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When you are not authorized to poll events
	 */
	@Deprecated
	List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException;

	/**
	 * Creates new auditer consumer with last processed id which equals current auditer log max id.
	 *
	 * @param perunSession perun session
	 * @param consumerName new name for consumer
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When you are not authorized to create auditer consumer
	 */
	void createAuditerConsumer(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException;

	/**
	 * Log arbitrary audit message.
	 *
	 * @param perunSession perun session
	 * @param message      message to be logged
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When you are not authorized to log arbitrary message
	 */
	void log(PerunSession perunSession, String message) throws InternalErrorException, PrivilegeException;

	/**
	 * Get all registered auditer consumers as map name(String)=lastProcessedId(Integer).
	 *
	 * @param perunSession perun session
	 * @return Mapping of auditer consumer names to their last processed ID.
	 * @throws InternalErrorException When implementation fails
	 */
	Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Get ID of last (newest) message in audit log (max_id).
	 *
	 * @param perunSession perun session
	 * @return ID of last (newest) message.
	 * @throws InternalErrorException When implementation fails
	 */
	int getLastMessageId(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Set ID of last processed message for specified consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName    name of consumer
	 * @param lastProcessedId id of last processed message in consumer
	 * @throws InternalErrorException When implementation fails
	 * @throws PrivilegeException When you are not authorized to set last processed id
	 */
	@Deprecated
	void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException;

	/**
	 * Get count of all messages in audit log.
	 *
	 * @param perunSession perun session
	 * @return Count of all messages in audit log
	 * @throws InternalErrorException When implementation fails
	 */
	int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException;

}
