package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;
import java.util.Map;

public enum AuditMessagesManagerMethod implements ManagerMethod {

	/*#
	 * Returns exact number of newest audit messages defined by 'count' param (disregarding message IDs).
	 * If there is less messages present, then all of them are returned.
	 *
	 * @param count int Messages limit
	 * @return List<AuditMessage> Audit messages
	 */
	/*#
	 * Returns 100 newest audit messages from audit log. If there is a less messages than 100,
	 * then all of them are returned.
	 *
	 * @return List<AuditMessage> Audit messages
	 */
	getMessages {
		@Override
		public List<AuditMessage> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("count")) return ac.getAuditMessagesManager().getMessages(ac.getSession(), parms.readInt("count"));
			else return ac.getAuditMessagesManager().getMessages(ac.getSession());
		}
	},

	/*#
	 * Returns all messages with IDs within the range from max(ID) to (max(ID)-count), where number of returned messages
	 * is equal or less than 'count' param, because some IDs could be skipped in the sequence.
	 *
	 * @param count int Number of IDs to subtract from max_id
	 * @return List<AuditMessage> List of audit messages
	 */
	getMessagesByCount {
		@Override
		public List<AuditMessage> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAuditMessagesManager().getMessagesByCount(ac.getSession(), parms.readInt("count"));
		}
	},

	/*#
	 * Returns list of AuditMessages from audit log with IDs > lastProcessedId for registered auditer consumer
	 * specified by consumerName param.
	 *
	 * @param consumerName String Consumer to get messages for
	 * @return List<AuditMessage> List of Audit Messages
	 */
	pollConsumerMessages {
		@Override
		public List<AuditMessage> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAuditMessagesManager().pollConsumerMessages(ac.getSession(), parms.readString("consumerName"));
		}
	},

	/*#
	 * Set ID of last processed message for specified consumer.
	 *
	 * @param consumerName String name of consumer
	 * @param lastProcessedId int id of message to what consumer will be set
	 * @throws InternalErrorException
	 */
	setLastProcessedId {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getAuditMessagesManager().setLastProcessedId(ac.getSession(), parms.readString("consumerName"),
			  parms.readInt("lastProcessedId"));
			return null;
		}
	},

	/*#
	 * Creates new auditer consumer with last processed id which equals current auditer log max id.
	 *
	 * @param consumerName String New name for consumer
	 */
	createAuditerConsumer {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getAuditMessagesManager().createAuditerConsumer(ac.getSession(), parms.readString("consumerName"));
			return null;
		}
	},

	/*#
	 * Get all auditer consumers as a map with key=value pairs like String(name)=Integer(lastProcessedId).
	 *
	 * @return Map<String, Integer> Mapping of all auditer consumers to their last processed message ID.
	 */
	getAllAuditerConsumers {
		@Override
		public Map<String, Integer> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAuditMessagesManager().getAllAuditerConsumers(ac.getSession());
		}
	},

	/*#
	 * Get ID of last (newest) message in auditer logs.
	 *
	 * @return Integer ID of last (newest) message.
	 */
	getLastMessageId {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAuditMessagesManager().getLastMessageId(ac.getSession());
		}
	},

	/*#
	 * Get count of all messages stored in auditer logs.
	 *
	 * @return Integer Count of all messages.
	 */
	getAuditerMessagesCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAuditMessagesManager().getAuditerMessagesCount(ac.getSession());
		}
	},

	/*#
	 * Log arbitrary auditer message/event to the audit log.
	 *
	 * @param msg String Message to be logged
	 */
	log {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			ac.getAuditMessagesManager().log(ac.getSession(), parms.readString("msg"));
			return null;
		}
	}

}
