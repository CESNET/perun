package cz.metacentrum.perun.dispatcher.processing;

import cz.metacentrum.perun.audit.events.EngineForceEvent;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducerFactory;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class ensure periodic blocking polling of EventQueue with Events parsed from audit messages by AuditerListener.
 * <p>
 * For each Event, Facility and set of affected Services is resolved. If can't be resolved or are empty, Event is
 * discarded.
 * <p>
 * Each Event is converted to Task if possible and added to pool (if new) or updated in pool (if exists). New Tasks are
 * also planned immediately.
 *
 * @author Michal Karm Babacek
 * @author Michal Vocu
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.dispatcher.model.Event
 * @see cz.metacentrum.perun.dispatcher.processing.AuditerListener
 * @see cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool
 */
@org.springframework.stereotype.Service(value = "eventProcessor")
public class EventProcessor extends AbstractRunner {

  private static final Logger LOG = LoggerFactory.getLogger(EventProcessor.class);

  private BlockingQueue<Event> eventQueue;
  private EngineMessageProducerFactory engineMessageProducerFactory;
  private EventServiceResolver eventServiceResolver;
  private SchedulingPool schedulingPool;
  private Perun perun;
  private Properties dispatcherProperties;
  private PerunSession sess;

  // ----- setters -------------------------------------

  /**
   * Creates Task from Event data. Tries to resolve Service and Facility pairs from Event. Events for non-existing
   * entities are discarded.
   *
   * @param event Event to parse
   * @throws ServiceNotExistsException    When Service from Event doesn't exists anymore
   * @throws InvalidEventMessageException When Message has invalid format.
   * @throws InternalErrorException       When implementation fails
   * @throws PrivilegeException           When dispatcher lack privileges to call core methods
   */
  private void createTaskFromEvent(Event event)
      throws ServiceNotExistsException, InvalidEventMessageException, PrivilegeException {

    Map<Facility, Set<Service>> resolvedServices = eventServiceResolver.resolveEvent(event.getData());

    for (Entry<Facility, Set<Service>> map : resolvedServices.entrySet()) {
      Facility facility = map.getKey();
      for (Service service : map.getValue()) {
        if (!service.isEnabled()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Service disabled: {}.", service);
          } else {
            LOG.info("Service disabled: {}.", service.getId() + " / " + service.getName());
          }
          continue;
        }

        if (((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(service, facility)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Service blocked on Facility: {} , {}.", service, facility);
          } else {
            LOG.info("Service blocked on Facility: {} , {}.", service.getId() + " / " + service.getName(),
                facility.getId() + " / " + facility.getName());
          }
          continue;
        }

        // Check if all destinations are not blocked
        try {

          // init session
          try {
            if (sess == null) {
              sess = perun.getPerunSession(new PerunPrincipal(dispatcherProperties.getProperty("perun.principal.name"),
                  dispatcherProperties.getProperty("perun.principal.extSourceName"),
                  dispatcherProperties.getProperty("perun.principal.extSourceType")), new PerunClient());
            }
          } catch (InternalErrorException e1) {
            LOG.error("Error establishing perun session to create Task from Event: ", e1);
            continue;
          }

          List<Destination> destinations = perun.getServicesManager().getDestinations(sess, service, facility);
          if (destinations != null && !destinations.isEmpty()) {
            Iterator<Destination> iter = destinations.iterator();
            while (iter.hasNext()) {
              Destination dest = iter.next();
              if (((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(service, dest.getId())) {
                iter.remove();
              }
            }
            if (destinations.isEmpty()) {
              // All service destinations were blocked -> Task is denied to be sent to engine just like
              // when service is blocked globally in Perun or on facility as a whole.

              if (LOG.isDebugEnabled()) {
                LOG.debug("{} blocked on all destinations on {}.", service, facility);
              } else {
                LOG.info("Service: {} blocked on all destinations on Facility: {}.",
                    service.getId() + " / " + service.getName(), facility.getId() + " / " + facility.getName());
              }
              continue;
            }
          }

        } catch (ServiceNotExistsException e) {
          LOG.error("Service not exist: {}.", service);
        } catch (FacilityNotExistsException e) {
          LOG.error("Facility not exist: {}.", facility);
        } catch (InternalErrorException | PrivilegeException e) {
          LOG.error("{}", e);
        }

        // check for presence of task for this <Service, Facility> pair
        // NOTE: this must be atomic enough to not create duplicate
        // tasks in schedulingPool (are we running in parallel
        // here?)

        boolean isForced = determineForcedPropagation(event);

        Task task = schedulingPool.getTask(facility, service);

        if (task != null) {
          // there already is a task in schedulingPool
          // signal that task needs to regenerate data and be forced next time
          task.setDestinations(null);
          task.setSourceUpdated(true);
          if (isForced) {
            task.setPropagationForced(true);
          }
          task.setRecurrence(0);
          LOG.info("[{}] Task is already in pool. Re-setting source updated and forced flags, {}.", task.getId(), task);
        } else {
          // no such task yet, create one
          task = new Task();
          task.setFacility(facility);
          task.setService(service);
          task.setStatus(TaskStatus.WAITING);
          task.setRecurrence(0);
          task.setDelay(service.getDelay());
          task.setSchedule(LocalDateTime.now());
          task.setSourceUpdated(false);
          task.setPropagationForced(isForced);
          try {
            schedulingPool.addToPool(task);
            LOG.info("[{}] New Task added to pool. {}.", task.getId(), task);
          } catch (TaskStoreException e) {
            LOG.error("[{}] Could not add Task to pool. Task {} will be lost: {}", task.getId(), task, e);
          }
          schedulingPool.scheduleTask(task, -1);
        }
      }
    }
  }

  /**
   * Return true if event forces service propagation
   *
   * @param event Event to check.
   * @return TRUE = forced propagation / FALSE = normal data change
   */
  private boolean determineForcedPropagation(Event event) {

    return (event.getData() instanceof EngineForceEvent);

  }

  public Properties getDispatcherProperties() {
    return dispatcherProperties;
  }

  public EngineMessageProducerFactory getEngineMessageProducerFactory() {
    return engineMessageProducerFactory;
  }

  public BlockingQueue<Event> getEventQueue() {
    return eventQueue;
  }

  public EventServiceResolver getEventServiceResolver() {
    return eventServiceResolver;
  }

  public Perun getPerun() {
    return perun;
  }

  public SchedulingPool getSchedulingPool() {
    return schedulingPool;
  }

  /**
   * EvProcessor thread, reads EventQueue and convert Events to Tasks, which are added to scheduling pool or updated if
   * already in pool.
   */
  @Override
  public void run() {
    while (!shouldStop()) {
      try {
        Event event = eventQueue.take();
        createTaskFromEvent(event);
        LOG.trace("Remaining events in a Queue = {}", eventQueue.size());
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
    LOG.warn("EventProcessor has stopped.");
  }

  @Resource(name = "dispatcherPropertiesBean")
  public void setDispatcherProperties(Properties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
  }

  @Autowired
  public void setEngineMessageProducerFactory(EngineMessageProducerFactory engineMessageProducerFactory) {
    this.engineMessageProducerFactory = engineMessageProducerFactory;
  }

  @Resource(name = "eventQueue")
  public void setEventQueue(BlockingQueue<Event> eventQueue) {
    this.eventQueue = eventQueue;
  }

  // ----- methods -------------------------------------

  @Autowired
  public void setEventServiceResolver(EventServiceResolver eventServiceResolver) {
    this.eventServiceResolver = eventServiceResolver;
  }

  @Autowired
  public void setPerun(Perun perun) {
    this.perun = perun;
  }

  @Autowired
  public void setSchedulingPool(SchedulingPool schedulingPool) {
    this.schedulingPool = schedulingPool;
  }

}
