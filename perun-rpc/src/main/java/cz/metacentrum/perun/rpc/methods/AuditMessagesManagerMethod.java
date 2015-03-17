package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;
import java.util.Map;

public enum AuditMessagesManagerMethod implements ManagerMethod {

	/*#
	 * Returns messages from audit's logs.
	 * @param count int Messages limit
	 * @return List<AuditMessage> Audit messages
	 */
	/*#
	 * Returns reasonable number of messages from audit's logs.
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
	 * Returns less than count or equals to count messages from audit's logs.
	 *
	 * Important: This variant does not guarantee returning just count of messages! It returns messages by Id from max_id to max_id-count (can be less then count messages)
	 *
	 * @param count int Count of returned messages
	 * @return List<AuditMessage> list of audit's messages
	 */
	getMessagesByCount {
		@Override
		public List<AuditMessage> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getAuditMessagesManager().getMessagesByCount(ac.getSession(), parms.readInt("count"));
		}
	},

	/*#
	 * Returns list of messages from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName String consumer to get messages for
	 * @return List<String> list of messages
	 */
	pollConsumerMessages {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getAuditMessagesManager().pollConsumerMessages(ac.getSession(), parms.readString("consumerName"));
		}
	},

	/*#
	 * Returns list of full messages from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName String consumer to get messages for
	 * @return List<String> list of full messages
	 */
	pollConsumerFullMessages {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getAuditMessagesManager().pollConsumerFullMessages(ac.getSession(), parms.readString("consumerName"));
		}
	},

	/*#
	 * Returns list of messages for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName String consumer to get messages for
	 * @return List<String> list of messages for parser
	 */
	pollConsumerMessagesForParserSimple {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getAuditMessagesManager().pollConsumerMessagesForParserSimple(ac.getSession(), parms.readString("consumerName"));
		}
	},

	/*#
	 * Returns list of auditMessages for parser from audit's log which id is bigger than last processed id.
	 *
	 * @param consumerName String consumer to get messages for
	 * @return List<AuditMessage> list of auditMessages for parser
	 */
	pollConsumerMessagesForParser {
		@Override
		public List<AuditMessage> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getAuditMessagesManager().pollConsumerMessagesForParser(ac.getSession(), parms.readString("consumerName"));
		}
	},

	/*#
	 * Creates new auditer consumer with last processed id which equals auditer log max id.
	 *
	 * @param consumerName String new name for consumer
	 */
	createAuditerConsumer {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			ac.getAuditMessagesManager().createAuditerConsumer(ac.getSession(), parms.readString("consumerName"));
			return null;
		}
	},

	/*#
	 * Get all auditer consumers from database. In map is String = name and Integer = lastProcessedId.
	 */
	getAllAuditerConsumers {
		@Override
		public Map<String, Integer> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getAuditMessagesManager().getAllAuditerConsumers(ac.getSession());
		}
	},

	/*#
	 * Get last message id from auditer_log.
	 */
	getLastMessageId {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getAuditMessagesManager().getLastMessageId(ac.getSession());
		}
	},

	/*#
	 * Logs an auditer message
	 *
	 * @param msg String Message to be logged
	 */
	log {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getAuditMessagesManager().log(ac.getSession(), parms.readString("msg"));
			return null;
		}
	};
}
