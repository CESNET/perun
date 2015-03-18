package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.RTMessage;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum RTMessagesManagerMethod implements ManagerMethod {

	/*#
	 * Sends a message to RT.
	 * Member <code>id</code> is sent.
	 *
	 * @param memberId int Member whose e-mail address will be user
	 * @param queue String RT queue
	 * @param subject String Message subject
	 * @param text String Message text
	 * @return RTMessage Confirmation with e-mail address the ticket was created for
	 */
	/*#
	 * Sends a message to RT.
	 * VO <code>id</code> is sent.
	 *
	 * @param voId int VO <code>id</code>
	 * @param queue String RT queue
	 * @param subject String Message subject
	 * @param text String Message text
	 * @return RTMessage Confirmation with e-mail address the ticket was created for
	 */
	/*#
	 * Sends a message to RT.
	 * VO <code>id</code> is sent.
	 * Queue is not sent.
	 *
	 * @param voId int VO <code>id</code>
	 * @param subject String Message subject
	 * @param text String Message text
	 * @return RTMessage Confirmation with e-mail address the ticket was created for
	 */
	/*#
	 * Sends a message to RT.
	 * Only text information is sent.
	 *
	 * @param queue String RT queue
	 * @param subject String Message subject
	 * @param text String Message text
	 * @return RTMessage Confirmation with e-mail address the ticket was created for
	 */
	sentMessageToRT {
		@Override
		public RTMessage call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("memberId")) {
				return ac.getRTMessagesManager().sendMessageToRT(ac.getSession(),
						ac.getMemberById(parms.readInt("memberId")), parms.readString("queue"),
						parms.readString("subject"), parms.readString("text"));
			} else if(parms.contains("voId")) {
				if(parms.contains("queue")) {
					return ac.getRTMessagesManager().sendMessageToRT(ac.getSession(),
							parms.readInt("voId"), parms.readString("queue"),
							parms.readString("subject"), parms.readString("text"));
				} else {
					return ac.getRTMessagesManager().sendMessageToRT(ac.getSession(),
							parms.readInt("voId"), parms.readString("subject"), parms.readString("text"));
				}
			} else {
				return ac.getRTMessagesManager().sendMessageToRT(ac.getSession(),
						parms.readString("queue"),
						parms.readString("subject"), parms.readString("text"));
			}
		}
	};
}
