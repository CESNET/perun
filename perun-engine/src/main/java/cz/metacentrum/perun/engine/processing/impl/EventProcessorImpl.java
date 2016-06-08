package cz.metacentrum.perun.engine.processing.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.engine.processing.EventParser;
import cz.metacentrum.perun.engine.processing.EventProcessor;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "eventProcessor")
public class EventProcessorImpl implements EventProcessor {

	private final static Logger log = LoggerFactory
			.getLogger(EventProcessorImpl.class);

	@Autowired
	private EventParser eventParser;

	@Autowired
	private SchedulingPool schedulingPool;

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private TaskExecutor taskExecutorEventProcessor;

	@Override
	public void receiveEvent(String event) {
		log.info("Current pool size BEFORE event processing:"
				+ schedulingPool.getSize());

		log.debug("Event " + event + " is going to be resolved...");

		Task task = null;
		try {
			task = eventParser.parseEvent(event);

		} catch (InvalidEventMessageException e) {
			log.error(e.toString());
		} catch (ServiceNotExistsException e) {
			log.error(e.toString());
		} catch (InternalErrorException e) {
			log.error(e.toString());
		} catch (PrivilegeException e) {
			log.error(e.toString());
		}

		if(task == null) {
			return;
		}
		
		// FIXME: Disabled because it can cause race condition. See RT#33803
		if (false) {
			// if (event.contains("forceit")) { // TODO: Move string constant to
			// a properties file

			log.debug("\t Facility[" + task.getFacility() + "]");
			log.debug("\t Resolved ExecService[" + task.getExecService() + "]");

			if (task != null && task.getFacility() != null
					&& task.getExecService() != null) {
				// log.debug("SCHEDULING vie Force Service Propagation: ExecService["
				// + execService.getId() + "] : Facility[" + results.getRight()
				// + "]");
				schedulingPool.addToPool(task);
				final Task ntask = task;
				taskExecutorEventProcessor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							taskScheduler.propagateService(ntask, new Date(
									System.currentTimeMillis()));
						} catch (InternalErrorException e) {
							log.error(e.toString());
						}
					}
				});
			}
			log.debug("POOL SIZE:" + schedulingPool.getSize());
		} 

		log.debug("\t Facility[" + task.getFacility() + "]");
		log.debug("\t Resolved ExecService[" + task.getExecService() + "]");

		if (task.getFacility() != null && task.getExecService() != null) {
			// log.debug("ADD to POOL: ExecService[" +
			// results.getLeft().getId() + "] : Facility[" +
			// results.getRight() + "]");
			Task currentTask = schedulingPool.getTaskById(task.getId());
			if(currentTask == null) {
				// task.setSourceUpdated(false);
				schedulingPool.addToPool(task);
				currentTask = task;
			} else {
				// currentTask.setSourceUpdated(true);
				log.debug("Resetting current task destination list to {}", task.getDestinations());
				currentTask.setDestinations(task.getDestinations());
				currentTask.setPropagationForced(task.isPropagationForced());
			}
			if(currentTask.isPropagationForced()) {
				final Task ntask = currentTask;
				try {
					taskExecutorEventProcessor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								taskScheduler.propagateService(ntask, new Date(
										System.currentTimeMillis()));
							} catch (InternalErrorException e) {
								log.error(e.toString());
							}
						}
					});
				} catch(Exception e) {
					log.error("Error queuing task to executor: " + e.toString());
				}
			}
		}
		log.debug("POOL SIZE:" + schedulingPool.getSize());

		log.info("Current pool size AFTER event processing:"
				+ schedulingPool.getSize());
	}

	public void setEventParser(EventParser eventParser) {
		this.eventParser = eventParser;
	}

	public EventParser getEventParser() {
		return eventParser;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public TaskExecutor getTaskExecutorEventProcessor() {
		return taskExecutorEventProcessor;
	}

	public void setTaskExecutorEventProcessor(
			TaskExecutor taskExecutorEventProcessor) {
		this.taskExecutorEventProcessor = taskExecutorEventProcessor;
	}

}
