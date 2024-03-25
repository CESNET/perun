package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

public class UpdateEventProcessor extends AbstractEventProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(UpdateEventProcessor.class);

  @Override
  public void processEvent(String msg, MessageBeans beans) {
    for (int beanFlag : beans.getPresentBeansFlags()) {
      try {
        switch (beanFlag) {
          case MessageBeans.GROUP_F:
            LOG.debug("Updating group {}", beans.getGroup());
            perunGroup.updateGroup(beans.getGroup());
            break;

          case MessageBeans.RESOURCE_F:
            LOG.debug("Updating resource {}", beans.getResource());
            perunResource.updateResource(beans.getResource());
            break;

          case MessageBeans.FACILITY_F:
            LOG.debug("Updating facility {}", beans.getFacility());
            perunFacility.updateFacility(beans.getFacility());
            break;

          case MessageBeans.USER_F:
            LOG.debug("Updating user {}", beans.getUser());
            perunUser.updateUser(beans.getUser());
            break;

          case MessageBeans.VO_F:
            LOG.debug("Updating VO {}", beans.getVo());
            perunVO.updateVo(beans.getVo());
            break;

          default:
            break;
        }
      } catch (NamingException | InternalErrorException e) {
        LOG.error("Error updating entry: {}", e.getMessage());
      }
    }
  }

}
