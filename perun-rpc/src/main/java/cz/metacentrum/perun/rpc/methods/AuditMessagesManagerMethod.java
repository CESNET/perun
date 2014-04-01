package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;

public enum AuditMessagesManagerMethod implements ManagerMethod {

	/*#
	 * Returns reasonable number of messages from audit's logs.
	 * @return List<AuditMessage> Audit messages
	 */
	/*#
	 * Returns messages from audit's logs.
	 * @param count int Messages limit
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
	 * Log an auditer message
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
