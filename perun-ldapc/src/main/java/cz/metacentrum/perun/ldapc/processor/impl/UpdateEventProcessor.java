package cz.metacentrum.perun.ldapc.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

public class UpdateEventProcessor extends AbstractEventProcessor {

	private final static Logger log = LoggerFactory.getLogger(UpdateEventProcessor.class);

	@Override
	public void processEvent(String msg, MessageBeans beans) {
		for(int beanFlag: beans.getPresentBeansFlags()) {
			try {
				switch(beanFlag) {
				case MessageBeans.GROUP_F:
					log.debug("Updating group {}", beans.getGroup());
					perunGroup.updateGroup(beans.getGroup());
					break;
					
				case MessageBeans.RESOURCE_F:
					log.debug("Updating resource {}", beans.getResource());
					perunResource.updateResource(beans.getResource());
					break;
					
				case MessageBeans.USER_F:
					log.debug("Updating user {}", beans.getUser());
					perunUser.updateUser(beans.getUser());
					break;
					
				case MessageBeans.VO_F:
					log.debug("Updating VO {}", beans.getVo());
					perunVO.updateVo(beans.getVo());
					break;
					
				default:
					break;	
				}
			} catch(NamingException | InternalErrorException e) {
				log.error("Error updating entry: {}", e.getMessage());
			}
		}
	}

}
