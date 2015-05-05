package cz.metacentrum.perun.rpc.methods;

import java.util.List;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum NotificationManagerMethod implements ManagerMethod {

	/*#
	 * Return PerunNotifReceiver with given id from db.
	 * Object for PerunNotifReceiver.
	 *
	 * @param id int Receiver <code>id</code>
	 * @return PerunNotifReceiver PerunNotifReceiver
	 */
	getPerunNotifReceiverById {
		@Override
		public PerunNotifReceiver call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("id")) {
				return ac.getNotificationManager().getPerunNotifReceiverById(ac.getSession(), parms.readInt("id"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "id");
			}
		}
	},

	/*#
	 * Returns all PerunNotifReceivers from db.
	 *
	 * @return List<PerunNotifReceiver> List of all PerunNotifReceivers
	 */
	getAllPerunNotifReceivers {
		@Override
		public List<PerunNotifReceiver> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getNotificationManager().getAllPerunNotifReceivers(ac.getSession());
		}
	},

	/*#
	 * Saves PerunNotifReceiver to db and creates <code>id</code>.
	 *
	 * @param receiver PerunNotifReceiver PerunNotifReceiver object without <code>id</code>
	 * @return PerunNotifReceiver PerunNotifReceiver with new <code>id</code> set
	 */
	createPerunNotifReceiver {
		@Override
		public PerunNotifReceiver call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifReceiver(ac.getSession(), parms.read("receiver", PerunNotifReceiver.class));
		}
	},

	/*#
	 * Updates receiver in db.
	 *
	 * @param receiver PerunNotifReceiver PerunNotifReceiver to be updated with new properties
	 * @return PerunNotifReceiver Updated perunNotifReceiver
	 */
	updatePerunNotifReceiver {
		@Override
		public PerunNotifReceiver call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("receiver")) {
				return ac.getNotificationManager().updatePerunNotifReceiver(ac.getSession(), parms.read("receiver", PerunNotifReceiver.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "receiver");
			}
		}
	},

	/*#
	 * Removes PerunNotifReceiver from db.
	 *
	 * @param id int PerunNotifReceiver <code>id</code>
	 */
	removePerunNotifReceiverById {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifReceiverById(ac.getSession(), parms.readInt("id"));
			return null;
		}
	},

	/*#
	 * Returns PerunNotifRegex by <code>id</code>, returns also object related to regex.
	 * Methods for PerunNotifRegexp.
	 *
	 * @param id int PerunNotifRegex <code>id</code>
	 * @return PerunNotifRegex PerunNotifRegex
	 */
	getPerunNotifRegexById {
		@Override
		public PerunNotifRegex call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("id")) {
				return ac.getNotificationManager().getPerunNotifRegexById(ac.getSession(), parms.readInt("id"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "id");
			}
		}
	},

	/*#
	 * Returns all PerunNotifRegexes.
	 *
	 * @return List<PerunNotifRegex> List of all PerunNotifRegexes
	 */
	getAllPerunNotifRegexes {
		@Override
		public List<PerunNotifRegex> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getNotificationManager().getAllPerunNotifRegexes(ac.getSession());
		}
	},

	/*#
	 * Saves perunNotifRegex to db and creates <code>id</code>, also saves relation
	 * between regex and object.
	 *
	 * @param regex PerunNotifRegex PerunNotifRegex object without <code>id</code>
	 * @return PerunNotifRegex PerunNotifRegex with new id set
	 */
	createPerunNotifRegex {
		@Override
		public PerunNotifRegex call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifRegex(ac.getSession(), parms.read("regex", PerunNotifRegex.class));
		}
	},

	/*#
	 * Updates PerunNotifRegex in db, also updates relation between regex
	 * and objects.
	 *
	 * @param regex PerunNotifRegex PerunNotifRegex to be updated with new properties
	 * @return PerunNotifRegex Updated PerunNotifRegex
	 */
	updatePerunNotifRegex {
		@Override
		public PerunNotifRegex call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("regex")) {
				return ac.getNotificationManager().updatePerunNotifRegex(ac.getSession(), parms.read("regex", PerunNotifRegex.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "regex");
			}
		}
	},

	/*#
	 * Removes PerunNotifRegex from db, if regex is referenced from template
	 * exception is thrown. Also removes relation between regex and objects.
	 *
	 * @param id int PerunNotifRegex <code>id</code>
	 */
	removePerunNotifRegexById {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			try {
				ac.getNotificationManager().removePerunNotifRegexById(ac.getSession(), parms.readInt("id"));
			} catch (PerunNotifRegexUsedException ex) {
				throw new InternalErrorException("PerunNotifRegexUsedException catched in RPC.", ex);
			}
			return null;
		}
	},

	/*#
	 * Save relation between template and regex if not exists yet.
	 *
	 * @param templateId int Template <code>id</code>
	 * @param regexId int Regex <code>id</code>
	 */
	saveTemplateRegexRelation {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().saveTemplateRegexRelation(ac.getSession(), parms.readInt("templateId"), parms.readInt("regexId"));
			return null;
		}
	},

	/*#
	 * Returns all regexes related to given template.
	 *
	 * @param templateId int Template <code>id</code>
	 * @return List<PerunNotifRegex> List of regexes
	 */
	getRelatedRegexesForTemplate {
		@Override
		public List<PerunNotifRegex> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getNotificationManager().getRelatedRegexesForTemplate(ac.getSession(), parms.readInt("templateId"));
		}
	},

	/*#
	 * Removes relation between PerunNotifRegex and PerunNotifTemplate.
	 *
	 * @param templateId int Template <code>id</code>
	 * @param regexId int Regex <code>id</code>
	 */
	removePerunNotifTemplateRegexRelation {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifTemplateRegexRelation(ac.getSession(), parms.readInt("templateId"), parms.readInt("regexId"));
			return null;
		}
	},

	/*#
	 * Gets PerunNotifTemplateMessage from db.
 	 * Methods for perunNotifTemplateMessage.
 	 *
	 * @param id int PerunNotifTemplateMessage <code>id</code>
	 * @return PerunNotifTemplateMessage PerunNotifTemplateMessage
	 */
	getPerunNotifTemplateMessageById {
		@Override
		public PerunNotifTemplateMessage call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("id")) {
				return ac.getNotificationManager().getPerunNotifTemplateMessageById(ac.getSession(), parms.readInt("id"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "id");
			}
		}
	},

	/*#
	 * Returns all PerunNotifTemplateMessages.
	 *
	 * @return List<PerunNotifTemplateMessage> List of all PerunNotifTemplateMessages
	 */
	getAllPerunNotifTemplateMessages {
		@Override
		public List<PerunNotifTemplateMessage> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getNotificationManager().getAllPerunNotifTemplateMessages(ac.getSession());
		}
	},

	/*#
	 * Saves perunNotifTemplateMessage to db and creates <code>id</code>.
	 *
	 * @param message PerunNotifTemplateMessage PerunNotifTemplateMessage object without <code>id</code>
	 * @return PerunNotifTemplateMessage PerunNotifTemplateMessage with new id set
	 */
	createPerunNotifTemplateMessage {
		@Override
		public PerunNotifTemplateMessage call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifTemplateMessage(ac.getSession(), parms.read("message", PerunNotifTemplateMessage.class));
		}
	},

	/*#
	 * Update perunNotifTemplateMessage in db.
	 *
	 * @param message PerunNotifTemplateMessage PerunNotifTemplateMessage to be updated with new properties
	 * @return PerunNotifTemplateMessage Updated PerunNotifTemplateMessage
	 */
	updatePerunNotifTemplateMessage {
		@Override
		public PerunNotifTemplateMessage call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("message")) {
				return ac.getNotificationManager().updatePerunNotifTemplateMessage(ac.getSession(), parms.read("message", PerunNotifTemplateMessage.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "message");
			}
		}
	},

	/*#
	 * Removes PerunNotifTemplateMessage from db.
	 *
	 * @param id int PerunNotifTemplateMessage <code>id</code>
	 */
	removePerunNotifTemplateMessage {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifTemplateMessage(ac.getSession(), parms.readInt("id"));
			return null;
		}
	},

	/*#
	 * Return perunNotifTemplate from db, return also all filled collections.
	 * Methods for perunNotifTemplate.
	 *
	 * @param id int perunNotifTemplate <code>id</code>
	 * @return PerunNotifTemplate PerunNotifTemplate
	 */
	getPerunNotifTemplateById {
		@Override
		public PerunNotifTemplate call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("id")) {
				return ac.getNotificationManager().getPerunNotifTemplateById(ac.getSession(), parms.readInt("id"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "id");
			}
		}
	},

	/*#
	 * Returns all PerunNotifTemplates.
	 *
	 * @return List<PerunNotifTemplate> List of all PerunNotifTemplates
	 */
	getAllPerunNotifTemplates {
		@Override
		public List<PerunNotifTemplate> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getNotificationManager().getAllPerunNotifTemplates(ac.getSession());
		}
	},

	/*#
	 * Saves PerunNotifTemplate to db and saves all relations to db.
	 *
	 * @param template PerunNotifTemplate PerunNotifTemplate object without <code>id</code>
	 * @return PerunNotifTemplate PerunNotifTemplate with new id set
	 */
	createPerunNotifTemplate {
		@Override
		public PerunNotifTemplate call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifTemplate(ac.getSession(), parms.read("template", PerunNotifTemplate.class));
		}
	},

	/*#
	 * Method will update perunNotifTemplate, also update relations, but not
	 * deletes them
	 *
	 * @param template PerunNotifTemplate PerunNotifTemplate object to be updated with new properties
	 * @return PerunNotifTemplate Updated perunNotifTemplate
	 */
	updatePerunNotifTemplate {
		@Override
		public PerunNotifTemplate call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("template")) {
				return ac.getNotificationManager().updatePerunNotifTemplate(ac.getSession(), parms.read("template", PerunNotifTemplate.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "template");
			}
		}
	},

	/*#
	 * Removes perunNotifTemplate from db.
	 *
	 * @param id int PerunNotifTemplate <code>id</code>
	 */
	removePerunNotifTemplateById {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifTemplateById(ac.getSession(), parms.readInt("id"));
			return null;
		}
	},

	/*#
	 * Stop notifications processing.
	 *
	 * @param id int PerunNotifTemplate ID
	 */
	stopNotifications {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			ac.getNotificationManager().stopNotifications(ac.getSession());
			return null;
		}
	},

	/*#
	 * Start notifications processing.
	 *
	 */
	startNotifications {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			ac.getNotificationManager().startNotifications(ac.getSession());
			return null;
		}
	},

	/*#
	 * Method checks if the notifications module is running at the time.
	 *
	 * @return true if running
	 */
	isNotificationsRunning {
		@Override
		public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getNotificationManager().isNotificationsRunning();
		}
	};

        //Special test method
        //TODO: Is needed to have this method there?
        /*testPerunNotifMessageText {

		@Override
		public String call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("template") && parms.contains("regexIdsPerunBeans")) {
				return ac.getNotificationManager().testPerunNotifMessageText(parms.readString("template"), parms.readMap???);
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "template");
			}
		}

	};*/
}