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
		log.info("Current pool size BEFORE event processing: {}", schedulingPool.getSize());
		log.debug("Event {} is going to be resolved.", event);

		Task task = null;
		try {
			task = eventParser.parseEvent(event);

		} catch (InvalidEventMessageException | ServiceNotExistsException |
				InternalErrorException | PrivilegeException e) {
			log.error(e.toString());
		}

		if (task == null) {
			return;
		}
		task.setStatus(Task.TaskStatus.PLANNED);

		log.debug("\t Facility[{}]", task.getFacility());
		log.debug("\t Resolved Service[{}]", task.getService());
		if (task.getFacility() != null && task.getService() != null) {
			log.debug("TESTSTRE -> Gonna check if the task {} exists", task);
			Task currentTask = schedulingPool.getTask(task.getId());
			log.debug("TESTSTRE -> Found {}", currentTask);
			if (currentTask == null) {
				try {
					schedulingPool.addTask(task);
				} catch (TaskStoreException e) {
					log.error("Could not save Task {} into Engine SchedulingPool because of {}, it will be ignored",
							task, e);
					// XXX - should probably report ERROR back to dispatcher...
				}
			} else {
				log.debug("Resetting current task destination list to {}", task.getDestinations());
				currentTask.setDestinations(task.getDestinations());
				currentTask.setPropagationForced(task.isPropagationForced());
			}
		}
		log.debug("POOL SIZE: {}", schedulingPool.getSize());
		log.info("Current pool size AFTER event processing: {}", schedulingPool.getSize());
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
