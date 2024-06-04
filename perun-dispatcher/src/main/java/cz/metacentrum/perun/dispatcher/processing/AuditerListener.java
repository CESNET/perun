package cz.metacentrum.perun.dispatcher.processing;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import jakarta.annotation.Resource;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class wraps AuditerConsumer for Dispatcher.
 * <p>
 * It ensure continuous reading of audit messages and convert them to Events, which are then pushed to EventQueue for
 * further processing by EventProcessor.
 * <p>
 * Its started by DispatcherManager when Spring context is initialized.
 *
 * @author Michal Babacek
 * @author Michal Vocu
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.dispatcher.processing.EventProcessor
 * @see cz.metacentrum.perun.dispatcher.service.DispatcherManager
 */
@Service(value = "auditerListener")
public class AuditerListener extends AbstractRunner {

  private static final Logger LOG = LoggerFactory.getLogger(AuditerListener.class);

  private BlockingQueue<Event> eventQueue;
  private Properties dispatcherProperties;
  private Perun perun;
  private PerunSession sess;

  // ----- setters -------------------------------------

  public Properties getDispatcherProperties() {
    return dispatcherProperties;
  }

  public BlockingQueue<Event> getEventQueue() {
    return eventQueue;
  }

  public Perun getPerun() {
    return perun;
  }

  @Override
  public void run() {

    boolean whichOfTwoRules = false;

    try {

      try {
        if (sess == null) {
          sess = perun.getPerunSession(new PerunPrincipal(dispatcherProperties.getProperty("perun.principal.name"),
              dispatcherProperties.getProperty("perun.principal.extSourceName"),
              dispatcherProperties.getProperty("perun.principal.extSourceType")), new PerunClient());
        }
      } catch (InternalErrorException e1) {
        LOG.error("Error establishing perun session in AuditerListener.", e1);
        // we can't continue without session
        stop();
      }


      while (!shouldStop()) {
        try {
          for (AuditEvent message : perun.getAuditMessagesManager().pollConsumerEvents(sess, "dispatcher")) {
            // create event for each message
            Event event = new Event();
            event.setTimeStamp(System.currentTimeMillis());
            if (whichOfTwoRules) {
              event.setHeader("portishead");
              whichOfTwoRules = false;
            } else {
              event.setHeader("clockworkorange");
              whichOfTwoRules = true;
            }
            event.setData(message);
            // pass event to queue for further processing
            eventQueue.put(event);
          }
          Thread.sleep(1000);
        } catch (InternalErrorException | PrivilegeException ex) {
          LOG.error("AuditerListener couldn't get AuditEvents.", ex);
          Thread.sleep(1000);
        }
      }
      LOG.debug("AuditerListener has stopped.");
    } catch (InterruptedException e) {
      LOG.error("Error in AuditerListener: {}" + e);
      throw new RuntimeException("Somebody has interrupted us...", e);
    }

  }

  @Resource(name = "dispatcherPropertiesBean")
  public void setDispatcherProperties(Properties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
  }

  @Resource(name = "eventQueue")
  public void setEventQueue(BlockingQueue<Event> eventQueue) {
    this.eventQueue = eventQueue;
  }

  // ----- methods -------------------------------------

  @Autowired
  public void setPerun(Perun perun) {
    this.perun = perun;
  }

}
