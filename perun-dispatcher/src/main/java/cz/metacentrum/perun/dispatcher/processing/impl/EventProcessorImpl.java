package cz.metacentrum.perun.dispatcher.processing.impl;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueuePool;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventExecServiceResolver;
import cz.metacentrum.perun.dispatcher.processing.EventLogger;
import cz.metacentrum.perun.dispatcher.processing.EventProcessor;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;
import cz.metacentrum.perun.dispatcher.processing.SmartMatcher;
import cz.metacentrum.perun.dispatcher.scheduling.DenialsResolver;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

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
	private EventQueue eventQueue;
	@Autowired
	private DispatcherQueuePool dispatcherQueuePool;
	@Autowired
	private EventLogger eventLogger;
	@Autowired
	private SmartMatcher smartMatcher;
	@Autowired
	private TaskExecutor taskExecutor;
	private EvProcessor evProcessor;
	@Autowired
	private EventExecServiceResolver eventExecServiceResolver;
	@Autowired
	private DenialsResolver denialsResolver;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskScheduler taskScheduler;

	public class EvProcessor implements Runnable {
		private boolean running = true;

		@Override
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("DEBUG LEVEL ENABLED:" + log.isDebugEnabled());
			}

			while (running) {
				try {
					Event event = eventQueue.poll();
					if (event != null) {
						if (log.isDebugEnabled()) {
							log.debug("Events in Queue(" + eventQueue.size()
									+ ").Dispatchers("
									+ dispatcherQueuePool.poolSize()
									+ ").Processing event...");
						}
						boolean orphan = true;
						for (DispatcherQueue dispatcherQueue : dispatcherQueuePool.getPool()) {
							long timeStamp = 0;
							if (log.isDebugEnabled()) {
								timeStamp = System.currentTimeMillis();
							}
							if (smartMatcher.doesItMatch(event, dispatcherQueue)) {
								orphan = false;
								createTask(dispatcherQueue, event);
								eventLogger.logEvent(event, dispatcherQueue.getClientID());
								if (log.isDebugEnabled()) {
									long timeStamp2 = System.currentTimeMillis();
									log.debug("MATCH OK (took "
											+ (timeStamp2 - timeStamp)
											+ "ms) for "
											+ dispatcherQueue.getClientID()
											+ " AND " + event.toString());
								}
								break;
							}
							if (log.isDebugEnabled()) {
								long timeStamp2 = System.currentTimeMillis();
								log.debug("NO MATCH (took "
										+ (timeStamp2 - timeStamp) + "ms) for "
										+ dispatcherQueue.getClientID()
										+ " AND " + event.toString());
							}
						}
						if (orphan) {
							eventLogger.logEvent(event, -1);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				try {
					// TODO: Remove?
					Thread.sleep(10);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		private void createTask(DispatcherQueue dispatcherQueue, Event event)
				throws ServiceNotExistsException, InvalidEventMessageException,
				InternalErrorException, PrivilegeException {
			// MV: this was the original behaviour
			// dispatcherQueue.sendMessage(event.toString());

			// Resolve the services in event, send the resulting <ExecService,
			// Facility> pairs to engine
			List<Pair<List<ExecService>, Facility>> resolvedServices = eventExecServiceResolver
					.parseEvent(event.toString());
			for (Pair<List<ExecService>, Facility> service : resolvedServices) {
				// String facility = service.getRight().serializeToString();
				Facility facility = service.getRight();
				for (ExecService execService : service.getLeft()) {
					// dispatcherQueue.sendMessage("[" + facility + "][" +
					// execService.getId() + "]");

					log.debug("Is the execService ID:" + execService.getId()
							+ " enabled globally?");
					if (execService.isEnabled()) {
						log.debug("   Yes, it is globally enabled.");
					} else {
						log.debug("   No, execService ID:"
								+ execService.getId()
								+ " is not enabled globally.");
						continue;
					}

					log.debug("   Is the execService ID:" + execService.getId()
							+ " denied on facility ID:" + facility.getId()
							+ "?");
					if (!denialsResolver.isExecServiceDeniedOnFacility(
							execService, facility)) {
						log.debug("   No, it is not.");
					} else {
						log.debug("   Yes, the execService ID:"
								+ execService.getId()
								+ " is denied on facility ID:"
								+ facility.getId() + "?");
						continue;
					}

					// check for presence of task for this <execService,
					// facility> pair
					// NOTE: this must be atomic enough to not create duplicate
					// tasks in schedulingPool (are we running in parallel
					// here?)
					Task task = schedulingPool.getTask(execService, facility);
					if (task != null) {
						// there already is a task in schedulingPool
						log.debug("  Task is in the pool already.");
						/*
						if (!(task.getStatus().equals(Task.TaskStatus.PLANNED) || task
								.getStatus().equals(Task.TaskStatus.PROCESSING))) {
							log.debug("  Task is not PLANNED or PROCESSING, removing destinations to refetch them later on.");
							task.setDestinations(null);
						}
						*/
						log.debug("  Removing destinations from existing task to refetch them later on.");
						task.setDestinations(null);
						// signal that task needs to regenerate data
						task.setSourceUpdated(true);
						task.setPropagationForced(false);
						task.setRecurrence(0);
					} else {
						// no such task yet, create one
						task = new Task();
						task.setFacility(facility);
						task.setExecService(execService);
						task.setStatus(TaskStatus.NONE);
						task.setRecurrence(0);
						task.setSchedule(new Date(System.currentTimeMillis()));
						task.setSourceUpdated(false);
						task.setPropagationForced(false);
						schedulingPool.addToPool(task, dispatcherQueue);
						log.debug("  Created new task and added to the pool.");
					}
					if (event.getData().contains("force propagation:")) {
						task.setPropagationForced(true);
						final Task task_final = task;
						// expedite task processing
						taskExecutor.execute(new Runnable() {
							@Override
							public void run() {
								log.debug("  Force scheduling the task.");
								taskScheduler.scheduleTask(task_final);
							}
						});
					}
				}
			}
		}

		public void stop() {
			running = false;
		}
	}

	@Override
	public void startProcessingEvents() {
		try {
			evProcessor = new EvProcessor();
			taskExecutor.execute(evProcessor);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	@Override
	public void stopProcessingEvents() {
		evProcessor.stop();
	}

	public EventQueue getEventQueue() {
		return eventQueue;
	}

	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}

	public DispatcherQueuePool getDispatcherQueuePool() {
		return dispatcherQueuePool;
	}

	public void setDispatcherQueuePool(DispatcherQueuePool dispatcherQueuePool) {
		this.dispatcherQueuePool = dispatcherQueuePool;
	}

	public EventLogger getEventLogger() {
		return eventLogger;
	}

	public void setEventLogger(EventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	public SmartMatcher getSmartMatcher() {
		return smartMatcher;
	}

	public void setSmartMatcher(SmartMatcher smartMatcher) {
		this.smartMatcher = smartMatcher;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public EvProcessor getEvProcessor() {
		return evProcessor;
	}

	public void setEvProcessor(EvProcessor evProcessor) {
		this.evProcessor = evProcessor;
	}

	public EventExecServiceResolver getEventExecServiceResolver() {
		return eventExecServiceResolver;
	}

	public void setEventExecServiceResolver(
			EventExecServiceResolver eventExecServiceResolver) {
		this.eventExecServiceResolver = eventExecServiceResolver;
	}

	public DenialsResolver getDenialsResolver() {
		return denialsResolver;
	}

	public void setDenialsResolver(DenialsResolver denialsResolver) {
		this.denialsResolver = denialsResolver;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

}
