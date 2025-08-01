package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;

/**
 * EventProcessor handling the deletion of any PerunBean object.
 */
public class DeletionEventProcessor extends AbstractEventProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(DeletionEventProcessor.class);

  @Override
  public void processEvent(String msg, MessageBeans beans) {
    for (int beanFlag : beans.getPresentBeansFlags()) {
      try {
        switch (beanFlag) {
          case MessageBeans.GROUP_F:
            LOG.debug("Removing group {}", beans.getGroup());
            perunGroup.removeGroup(beans.getGroup());
            break;

          case MessageBeans.RESOURCE_F:
            LOG.debug("Removing resource {}", beans.getResource());
            perunResource.deleteResource(beans.getResource());
            break;

          case MessageBeans.FACILITY_F:
            // skip if it is actually resource deletion
            if (beans.getResource() != null) {
              break;
            }
            // was facility deletion - proceed
            LOG.debug("Removing facility {}", beans.getFacility());
            perunFacility.deleteFacility(beans.getFacility());
            break;

          case MessageBeans.USER_F:
            LOG.debug("Removing user {}", beans.getUser());
            perunUser.deleteUser(beans.getUser());
            break;

          case MessageBeans.VO_F:
            LOG.debug("Removing VO {}", beans.getVo());
            perunVO.deleteVo(beans.getVo());
            break;

          default:
            break;
        }
      } catch (NamingException | InternalErrorException e) {
        LOG.error("Error removing entry: {}", e.getMessage());
      }
    }
  }

}
