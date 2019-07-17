package cz.metacentrum.perun.ldapc.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

public class DeletionEventProcessor extends AbstractEventProcessor {

	private final static Logger log = LoggerFactory.getLogger(DeletionEventProcessor.class);
	
	@Override
	public void processEvent(String msg, MessageBeans beans) {
		for(int beanFlag: beans.getPresentBeansFlags()) {
			try {
				switch(beanFlag) {
				case MessageBeans.GROUP_F:
					log.debug("Removing group {}", beans.getGroup());
					perunGroup.removeGroup(beans.getGroup());
					break;
					
				case MessageBeans.RESOURCE_F:
					log.debug("Removing resource {}", beans.getResource());
					perunResource.deleteResource(beans.getResource());
					break;
					
				case MessageBeans.USER_F:
					log.debug("Removing user {}", beans.getUser());
					perunUser.deleteUser(beans.getUser());
					break;
					
				case MessageBeans.VO_F:
					log.debug("Removing VO {}", beans.getVo());
					perunVO.deleteVo(beans.getVo());
					break;
					
				default:
					break;	
				}
			} catch(NamingException | InternalErrorException e) {
				log.error("Error removing entry: {}", e.getMessage());
			}
		}
	}

}
