package cz.metacentrum.perun.engine.processing.impl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.engine.processing.EventParser;
import cz.metacentrum.perun.engine.processing.EventProcessor;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service(value = "eventProcessor")
public class EventProcessorImpl implements EventProcessor {

	private final static Logger log = LoggerFactory
			.getLogger(EventProcessorImpl.class);

	@Autowired
	private EventParser eventParser;

	@Autowired
	private SchedulingPool schedulingPool;

	@Override
	public void receiveEvent(String event) {
		log.debug("Event {} is going to be resolved.", event);

		Task task = null;
		try {
			task = eventParser.parseEvent(event);

		} catch (InvalidEventMessageException | InternalErrorException e) {
			log.error(e.toString());
		}

		if (task == null) {
			log.debug("Task not found in event {}", event);
			return;
		}
		task.setStatus(Task.TaskStatus.PLANNED);

		log.info("Current pool size BEFORE event processing: {}", schedulingPool.getSize());

		log.debug("\t Resolved Facility[{}]", task.getFacility());
		log.debug("\t Resolved Service[{}]", task.getService());
		if (task.getFacility() != null && task.getService() != null) {
			log.debug("[{}] Check if Task exist in SchedulingPool: {}", task.getId(), task);
			Task currentTask = schedulingPool.getTask(task.getId());
			if (currentTask == null) {
				log.debug("[{}] Task not found in SchedulingPool.", task.getId());
				try {
					schedulingPool.addTask(task);
				} catch (TaskStoreException e) {
					log.error("Could not save Task {} into Engine SchedulingPool because of {}, it will be ignored", task, e);
					// FIXME - should probably report ERROR back to dispatcher...
				}
			} else {
				// since we always remove Task from pool at the end and Dispatcher doesn't send partial Destinations,
				// we don't need to update existing Task object !! Let engine finish the processing.
				log.debug("[{}] Task found in SchedulingPool, message skipped.", task.getId(), currentTask);
			}
		}
		log.debug("[{}] POOL SIZE: {}", task.getId(), schedulingPool.getSize());
		log.info("[{}] Current pool size AFTER event processing: {}", task.getId(), schedulingPool.getSize());
	}

	public EventParser getEventParser() {
		return eventParser;
	}

	public void setEventParser(EventParser eventParser) {
		this.eventParser = eventParser;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}
}
