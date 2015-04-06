package cz.metacentrum.perun.rpc.methods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.notif.managers.PerunNotifNotificationManager;
import cz.metacentrum.perun.core.api.Attribute;
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
import cz.metacentrum.perun.rpc.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum NotificationManagerMethod implements ManagerMethod {

	/*#
	 * Method returns PerunNotifObject from db with given <code>id</code>.
	 * Method for PerunNotifObject.
	 *
	 * @param id int <code>id</code> of PerunNotifObject
	 * @return PerunNotifObject PerunNotifObject
	 */
	getPerunNotifObjectById {
		@Override
		public PerunNotifObject call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("id")) {
				return ac.getNotificationManager().getPerunNotifObjectById(parms.readInt("id"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "id");
			}
		}
	},

	/*#
	 * Returns all PerunNotifObjects.
	 *
	 * @return List<PerunNotifObject> List of all objects
	 */
	getAllPerunNotifObjects {
		@Override
		public List<PerunNotifObject> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getNotificationManager().getAllPerunNotifObjects();
		}
	},

	/*#
	 * Saves PerunNotifObject to db and creates <code>id</code>.
	 *
	 * @param object PerunNotifObject Object without <code>id</code>
	 * @return PerunNotifObject PerunNotifObject with new <code>id</code> set
	 */
	createPerunNotifObject {
		@Override
		public PerunNotifObject call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifObject(parms.read("object", PerunNotifObject.class));
		}
	},

	/*#
	 * Updates perunNotifObject in db.
	 *
	 * @param object PerunNotifObject PerunNotifObject to be updated with new properties
	 * @return PerunNotifObject Updated PerunNotifObject
	 */
	updatePerunNotifObject {
		@Override
		public PerunNotifObject call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("object")) {
				return ac.getNotificationManager().updatePerunNotifObject(parms.read("object", PerunNotifObject.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "object");
			}
		}
	},

	/*#
	 * Removes object and relations to object with regex from db.
	 *
	 * @param id int Object to be deleted
	 */
	removePerunNotifObjectById {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifObjectById(parms.readInt("id"));
			return null;
		}
	},

	/*#
	 * Saves relation between object and regex if not exists.
	 *
	 * @param regexId int Regex <code>id</code>
	 * @param objectId int Object <code>id</code>
	 */
	saveObjectRegexRelation {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().saveObjectRegexRelation(parms.readInt("regexId"), parms.readInt("objectId"));
			return null;
		}
	},

	/*#
	 * Removes relation between object and regex.
	 *
	 * @param regexId int Regex <code>id</code>
	 * @param objectId int Object <code>id</code>
	 */
	removePerunNotifRegexObjectRelation {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifRegexObjectRelation(parms.readInt("regexId"), parms.readInt("objectId"));
			return null;
		}
	},

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
				return ac.getNotificationManager().getPerunNotifReceiverById(parms.readInt("id"));
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
			return ac.getNotificationManager().getAllPerunNotifReceivers();
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

			return ac.getNotificationManager().createPerunNotifReceiver(parms.read("receiver", PerunNotifReceiver.class));
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
			if (parms.contains("receiver")) {
				return ac.getNotificationManager().updatePerunNotifReceiver(parms.read("receiver", PerunNotifReceiver.class));
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

			ac.getNotificationManager().removePerunNotifReceiverById(parms.readInt("id"));
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
				return ac.getNotificationManager().getPerunNotifRegexById(parms.readInt("id"));
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
			return ac.getNotificationManager().getAllPerunNotifRegexes();
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

			return ac.getNotificationManager().createPerunNotifRegex(parms.read("regex", PerunNotifRegex.class));
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
			if (parms.contains("regex")) {
				return ac.getNotificationManager().updatePerunNotifRegex(parms.read("regex", PerunNotifRegex.class));
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
				ac.getNotificationManager().removePerunNotifRegexById(parms.readInt("id"));
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

			ac.getNotificationManager().saveTemplateRegexRelation(parms.readInt("templateId"), parms.readInt("regexId"));
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
			return ac.getNotificationManager().getRelatedRegexesForTemplate(parms.readInt("templateId"));
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

			ac.getNotificationManager().removePerunNotifTemplateRegexRelation(parms.readInt("templateId"), parms.readInt("regexId"));
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
				return ac.getNotificationManager().getPerunNotifTemplateMessageById(parms.readInt("id"));
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
			return ac.getNotificationManager().getAllPerunNotifTemplateMessages();
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

			return ac.getNotificationManager().createPerunNotifTemplateMessage(parms.read("message", PerunNotifTemplateMessage.class));
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
			if (parms.contains("message")) {
				return ac.getNotificationManager().updatePerunNotifTemplateMessage(parms.read("message", PerunNotifTemplateMessage.class));
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

			ac.getNotificationManager().removePerunNotifTemplateMessage(parms.readInt("id"));
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
				return ac.getNotificationManager().getPerunNotifTemplateById(parms.readInt("id"));
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
			return ac.getNotificationManager().getAllPerunNotifTemplates();
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

			return ac.getNotificationManager().createPerunNotifTemplate(parms.read("template", PerunNotifTemplate.class));
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
			if (parms.contains("template")) {
				return ac.getNotificationManager().updatePerunNotifTemplate(parms.read("template", PerunNotifTemplate.class));
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

			ac.getNotificationManager().removePerunNotifTemplateById(parms.readInt("id"));
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
			ac.stateChangingCheck();

			ac.getNotificationManager().stopNotifications();
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
			ac.stateChangingCheck();

			ac.getNotificationManager().startNotifications();
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
			ac.stateChangingCheck();

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