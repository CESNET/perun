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

        //Method for PerunNotifObject
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
        getAllPerunNotifObjects {

            @Override
            public List<PerunNotifObject> call(ApiCaller ac, Deserializer parms) throws PerunException {
		return ac.getNotificationManager().getAllPerunNotifObjects();
            }
        },
	createPerunNotifObject {

		@Override
		public PerunNotifObject call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifObject(parms.read("object", PerunNotifObject.class));
		}

	},
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
        removePerunNotifObjectById {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifObjectById(parms.readInt("id"));
			return null;
		}

	},
        saveObjectRegexRelation {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().saveObjectRegexRelation(parms.readInt("regexId"), parms.readInt("objectId"));
			return null;
		}

	},
        removePerunNotifRegexObjectRelation {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifRegexObjectRelation(parms.readInt("regexId"), parms.readInt("objectId"));
			return null;
		}

	},

        //Object for PerunNotifReceiver
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
        getAllPerunNotifReceivers {

            @Override
            public List<PerunNotifReceiver> call(ApiCaller ac, Deserializer parms) throws PerunException {
		return ac.getNotificationManager().getAllPerunNotifReceivers();
            }
        },
        createPerunNotifReceiver {

		@Override
		public PerunNotifReceiver call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifReceiver(parms.read("receiver", PerunNotifReceiver.class));
		}
	},
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
        removePerunNotifReceiverById {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifReceiverById(parms.readInt("id"));
			return null;
		}

	},

        //Methods for PerunNotifRegexp
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
        getAllPerunNotifRegexes {

            @Override
            public List<PerunNotifRegex> call(ApiCaller ac, Deserializer parms) throws PerunException {
		return ac.getNotificationManager().getAllPerunNotifRegexes();
            }
        },
        createPerunNotifRegex {

		@Override
		public PerunNotifRegex call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifRegex(parms.read("regex", PerunNotifRegex.class));
		}
	},
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
        saveTemplateRegexRelation {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().saveTemplateRegexRelation(parms.readInt("templateId"), parms.readInt("regexId"));
			return null;
		}

	},
	getRelatedRegexesForTemplate {

		@Override
		public List<PerunNotifRegex> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getNotificationManager().getRelatedRegexesForTemplate(parms.readInt("templateId"));
		}
	},
        removePerunNotifTemplateRegexRelation {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getNotificationManager().removePerunNotifTemplateRegexRelation(parms.readInt("templateId"), parms.readInt("regexId"));
			return null;
		}

	},

        //Methods for perunNotifTemplateMessage
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
        getAllPerunNotifTemplateMessages {

            @Override
            public List<PerunNotifTemplateMessage> call(ApiCaller ac, Deserializer parms) throws PerunException {
		return ac.getNotificationManager().getAllPerunNotifTemplateMessages();
            }
        },
        createPerunNotifTemplateMessage {

		@Override
		public PerunNotifTemplateMessage call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifTemplateMessage(parms.read("message", PerunNotifTemplateMessage.class));
		}
	},
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
        removePerunNotifTemplateMessage {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

                        ac.getNotificationManager().removePerunNotifTemplateMessage(parms.readInt("id"));
			return null;
		}

	},

        //Methods for perunNotifTemplate
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
        getAllPerunNotifTemplates {

            @Override
            public List<PerunNotifTemplate> call(ApiCaller ac, Deserializer parms) throws PerunException {
		return ac.getNotificationManager().getAllPerunNotifTemplates();
            }
        },
        createPerunNotifTemplate {

		@Override
		public PerunNotifTemplate call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getNotificationManager().createPerunNotifTemplate(parms.read("template", PerunNotifTemplate.class));
		}
	},
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
        removePerunNotifTemplateById {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

                        ac.getNotificationManager().removePerunNotifTemplateById(parms.readInt("id"));
			return null;
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