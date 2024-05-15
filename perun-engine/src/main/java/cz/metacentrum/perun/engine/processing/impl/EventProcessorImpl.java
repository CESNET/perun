package cz.metacentrum.perun.engine.processing.impl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.engine.processing.EventParser;
import cz.metacentrum.perun.engine.processing.EventProcessor;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service(value = "eventProcessor")
public class EventProcessorImpl implements EventProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(EventProcessorImpl.class);

  @Autowired
  private EventParser eventParser;

  @Autowired
  private SchedulingPool schedulingPool;

  public EventParser getEventParser() {
    return eventParser;
  }

  public SchedulingPool getSchedulingPool() {
    return schedulingPool;
  }

  @Override
  public void receiveEvent(String event) {
    LOG.debug("Event {} is going to be resolved.", event);

    Task task = null;
    try {
      task = eventParser.parseEvent(event);

    } catch (InvalidEventMessageException | InternalErrorException e) {
      LOG.error(e.toString());
    }

    if (task == null) {
      LOG.debug("Task not found in event {}", event);
      return;
    }
    task.setStatus(Task.TaskStatus.PLANNED);

    LOG.info("Current pool size BEFORE event processing: {}", schedulingPool.getSize());

    LOG.debug("\t Resolved Facility[{}]", task.getFacility());
    LOG.debug("\t Resolved Service[{}]", task.getService());
    if (task.getFacility() != null && task.getService() != null) {
      LOG.debug("[{}, {}] Check if Task exist in SchedulingPool: {}", task.getId(), task.getRunId(), task);
      Task currentTask = schedulingPool.getTask(task.getId());
      if (currentTask == null) {
        LOG.debug("[{}, {}] Task not found in SchedulingPool.", task.getId(), task.getRunId());
        try {
          schedulingPool.addTask(task);
        } catch (TaskStoreException e) {
          LOG.error("Could not save Task {} into Engine SchedulingPool because of {}, it will be ignored", task, e);
          // FIXME - should probably report ERROR back to dispatcher...
        }
      } else {
        // since we always remove Task from pool at the end and Dispatcher doesn't send partial Destinations,
        // we don't need to update existing Task object !! Let engine finish the processing.
        LOG.debug("[{}, {}] Task found in SchedulingPool, message skipped.", task.getId(), task.getRunId());
      }
    }
    LOG.debug("[{}, {}] POOL SIZE: {}", task.getId(), task.getRunId(), schedulingPool.getSize());
    LOG.info("[{}, {}] Current pool size AFTER event processing: {}", task.getId(), task.getRunId(),
        schedulingPool.getSize());
  }

  public void setEventParser(EventParser eventParser) {
    this.eventParser = eventParser;
  }

  public void setSchedulingPool(SchedulingPool schedulingPool) {
    this.schedulingPool = schedulingPool;
  }
}
