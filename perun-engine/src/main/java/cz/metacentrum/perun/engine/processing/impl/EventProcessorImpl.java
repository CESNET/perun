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
import cz.metacentrum.perun.engine.processing.EventExecServiceResolver;
import cz.metacentrum.perun.engine.processing.EventProcessor;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "eventProcessor")
public class EventProcessorImpl implements EventProcessor {

	private final static Logger log = LoggerFactory.getLogger(EventProcessorImpl.class);

	@Autowired
	private EventExecServiceResolver eventExecServiceResolver;

	@Autowired
	private SchedulingPool schedulingPool;

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private TaskExecutor taskExecutorEventProcessor;

	@Override
	public void receiveEvent(String event) {
		log.info("Current pool size BEFORE event processing:" + schedulingPool.getSize());

		log.debug("Event " + event + " is going to be resolved...");
		List<Pair<List<ExecService>, Facility>> results = new ArrayList<Pair<List<ExecService>, Facility>>();

		//FIXME: Disabled because it can cause race condition. See RT#33803
		if (false) {
			//if (event.contains("forceit")) {  // TODO: Move string constant to a properties file

			try {
				results = eventExecServiceResolver.parseEvent(event);

				log.info("FORCEIT: Returned Pair<List<ExecService>, Facility> pairs:" + results.size());
			} catch (InvalidEventMessageException e) {
				log.error(e.toString());
			} catch (ServiceNotExistsException e) {
				log.error(e.toString());
			} catch (InternalErrorException e) {
				log.error(e.toString());
			} catch (PrivilegeException e) {
				log.error(e.toString());
			}

			for (final Pair<List<ExecService>, Facility> resultTest : results) {
				log.debug("\t Facility[" + resultTest.getRight() + "]");
				log.debug("\t Resolved ExecServices[" + resultTest.getLeft() + "]");

				if (resultTest != null && resultTest.getLeft() != null && resultTest.getRight() != null) {
					for (final ExecService execService : resultTest.getLeft()) {
						log.debug("SCHEDULING vie Force Service Propagation: ExecService[" + execService.getId() + "] : Facility[" + resultTest.getRight() + "]");
						schedulingPool.addToPool(new Pair<ExecService, Facility>(execService, resultTest.getRight()));
						taskExecutorEventProcessor.execute(new Runnable() {
							@Override
							public void run() {
								try {
									taskScheduler.propagateService(execService, new Date(System.currentTimeMillis()), resultTest.getRight());
								} catch (InternalErrorException e) {
									log.error(e.toString());
								}
							}
						});
					}
				}
				log.debug("POOL SIZE:" + schedulingPool.getSize());
			}
		} else {

			try {
				results = eventExecServiceResolver.parseEvent(event);

				log.info("NORMAL: Returned Pair<List<ExecService>, Facility> pairs:" + results.size());
			} catch (InvalidEventMessageException e) {
				log.error(e.toString());
			} catch (ServiceNotExistsException e) {
				log.error(e.toString());
			} catch (InternalErrorException e) {
				log.error(e.toString());
			} catch (PrivilegeException e) {
				log.error(e.toString());
			}

			for (Pair<List<ExecService>, Facility> resultTest : results) {
				log.debug("\t Facility[" + resultTest.getRight() + "]");
				log.debug("\t Resolved ExecServices[" + resultTest.getLeft() + "]");

				if (resultTest != null && resultTest.getLeft() != null && resultTest.getRight() != null) {
					if(resultTest.getRight().getName().equals("alcor.ics.muni.cz") ||
							resultTest.getRight().getName().equals("aldor.ics.muni.cz") ||
							resultTest.getRight().getName().equals("torque.ics.muni.cz") ||
							resultTest.getRight().getName().equals("nympha-cloud.zcu.cz") ||
							resultTest.getRight().getName().equals("ascor.ics.muni.cz")) {
						log.info("IGNORE:  Facility[" + resultTest.getRight() + "]");
						continue;
					}
					for (ExecService execService : resultTest.getLeft()) {
						log.debug("ADD to POOL: ExecService[" + execService.getId() + "] : Facility[" + resultTest.getRight() + "]");
						schedulingPool.addToPool(new Pair<ExecService, Facility>(execService, resultTest.getRight()));
					}
				}
				log.debug("POOL SIZE:" + schedulingPool.getSize());
			}

		}
		log.info("Current pool size AFTER event processing:" + schedulingPool.getSize());
		}

		public void setEventExecServiceResolver(EventExecServiceResolver eventExecServiceResolver) {
			this.eventExecServiceResolver = eventExecServiceResolver;
		}

		public EventExecServiceResolver getEventExecServiceResolver() {
			return eventExecServiceResolver;
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

		public void setTaskExecutorEventProcessor(TaskExecutor taskExecutorEventProcessor) {
			this.taskExecutorEventProcessor = taskExecutorEventProcessor;
		}

	}
