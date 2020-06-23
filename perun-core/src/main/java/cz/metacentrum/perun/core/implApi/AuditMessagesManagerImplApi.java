package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;
import java.util.Map;

/**
 * This interface represents AuditMessagesManagerImpl methods.
 *
 * @author Pavel Zlámal
 */
public interface AuditMessagesManagerImplApi {

	/**
	 * Returns exact number of newest audit messages defined by 'count' param (disregarding message IDs).
	 * If there is less messages present, then all of them are returned.
	 *
	 * @param perunSession perun session
	 * @param count        Count of returned messages.
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditMessage> getMessages(PerunSession perunSession, int count);

	/**
	 * Returns all messages with IDs within the range from max(ID) to (max(ID)-count), where number of returned messages
	 * is equal or less than 'count' param, because some IDs could be skipped in the sequence.
	 *
	 * @param perunSession perun session
	 * @param count Number of IDs to subtract from max(ID)
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count);

	/**
	 * Returns list of <b>AuditMessages</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName);

	/**
	 * Returns list of <b>AuditMessages</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @param lastProcessedId id of the last message 
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName, int lastProcessedId);

	/**
	 * Returns list of <b>AuditEvents</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName);

	/**
	 * Returns list of <b>AuditEvents</b> from audit log with IDs > lastProcessedId for registered auditer consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName consumer to get messages for
	 * @param lastProcessedId id of the last message 
	 * @return List of audit messages
	 * @throws InternalErrorException When implementation fails
	 */
	List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName, int lastProcessedId);

	/**
	 * Creates new auditer consumer with last processed id which equals current auditer log max id.
	 *
	 * @param perunSession perun session
	 * @param consumerName new name for consumer
	 * @throws InternalErrorException When implementation fails
	 */
	void createAuditerConsumer(PerunSession perunSession, String consumerName);

	/**
	 * Get all registered auditer consumers as map name(String)=lastProcessedId(Integer).
	 *
	 * @param perunSession perun session
	 * @return Mapping of auditer consumer names to their last processed ID.
	 * @throws InternalErrorException When implementation fails
	 */
	Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession);

	/**
	 * Get ID of last (newest) message in audit log (max_id).
	 *
	 * @param perunSession perun session
	 * @return ID of last (newest) message.
	 * @throws InternalErrorException When implementation fails
	 */
	int getLastMessageId(PerunSession perunSession);

	/**
	 * Set ID of last processed message for specified consumer.
	 *
	 * @param perunSession perun session
	 * @param consumerName    name of consumer
	 * @param lastProcessedId id of last processed message in consumer
	 * @throws InternalErrorException When implementation fails
	 */
	@Deprecated
	void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId);

	/**
	 * Get count of all messages in audit log.
	 *
	 * @param perunSession perun session
	 * @return Count of all messages in audit log
	 * @throws InternalErrorException When implementation fails
	 */
	int getAuditerMessagesCount(PerunSession perunSession);

	/**
	 * Returns TRUE if auditer consumer with specified name exist.
	 *
	 * @param session PerunSession
	 * @param consumerName Name of consumer to check
	 * @return TRUE if consumer exists by name / FALSE otherwise
	 * @throws InternalErrorException When implementation fails
	 */
	boolean checkAuditerConsumerExists(PerunSession session, String consumerName);

}
