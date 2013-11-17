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
	savePerunNotifObject { 

		@Override
		public PerunNotifObject call(ApiCaller ac, Deserializer parms) throws PerunException {	
			ac.stateChangingCheck();
			
			return ac.getNotificationManager().savePerunNotifObject(ac.getPerunNotifObjectById(parms.readInt("object")));
		}

	},
        updatePerunNotifObject { 

		@Override
		public PerunNotifObject call(ApiCaller ac, Deserializer parms) throws PerunException {	
			if (parms.contains("object")) {
				return ac.getNotificationManager().updatePerunNotifObject(ac.getPerunNotifObjectById(parms.readInt("object")));
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
        savePerunNotifReceiver { 

		@Override
		public PerunNotifReceiver call(ApiCaller ac, Deserializer parms) throws PerunException {	
			ac.stateChangingCheck();
			return ac.getNotificationManager().savePerunNotifReceiver(ac.getPerunNotifReceiverById(parms.readInt("receiver")));
		}
	},
        updatePerunNotifReceiver { 

		@Override
		public PerunNotifReceiver call(ApiCaller ac, Deserializer parms) throws PerunException {	
			if (parms.contains("receiver")) {
				return ac.getNotificationManager().updatePerunNotifReceiver(ac.getPerunNotifReceiverById(parms.readInt("receiver")));
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
        savePerunNotifRegex { 

		@Override
		public PerunNotifRegex call(ApiCaller ac, Deserializer parms) throws PerunException {	
			ac.stateChangingCheck();
                            
			return ac.getNotificationManager().savePerunNotifRegex(ac.getPerunNotifRegexById(parms.readInt("regex")));
		}
	},
        updatePerunNotifRegex { 

		@Override
		public PerunNotifRegex call(ApiCaller ac, Deserializer parms) throws PerunException {	
			if (parms.contains("regex")) {
				return ac.getNotificationManager().updatePerunNotifRegex(ac.getPerunNotifRegexById(parms.readInt("regex")));
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
        savePerunNotifTemplateMessage { 

		@Override
		public PerunNotifTemplateMessage call(ApiCaller ac, Deserializer parms) throws PerunException {	
			ac.stateChangingCheck();
                            
			return ac.getNotificationManager().savePerunNotifTemplateMessage(ac.getPerunNotifTemplateMessageById(parms.readInt("message")));
		}
	},
        updatePerunNotifTemplateMessage { 

		@Override
		public PerunNotifTemplateMessage call(ApiCaller ac, Deserializer parms) throws PerunException {	
			if (parms.contains("message")) {
				return ac.getNotificationManager().updatePerunNotifTemplateMessage(ac.getPerunNotifTemplateMessageById(parms.readInt("message")));
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
        savePerunNotifTemplate { 

		@Override
		public PerunNotifTemplate call(ApiCaller ac, Deserializer parms) throws PerunException {	
			ac.stateChangingCheck();
                            
			return ac.getNotificationManager().savePerunNotifTemplate(ac.getPerunNotifTemplateById(parms.readInt("template")));
		}
	},
        updatePerunNotifTemplate { 

		@Override
		public PerunNotifTemplate call(ApiCaller ac, Deserializer parms) throws PerunException {	
			if (parms.contains("template")) {
				return ac.getNotificationManager().updatePerunNotifTemplate(ac.getPerunNotifTemplateById(parms.readInt("template")));
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