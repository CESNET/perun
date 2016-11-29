package cz.metacentrum.perun.dispatcher.scheduling.impl;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueuePool;
import cz.metacentrum.perun.dispatcher.scheduling.DenialsResolver;
import cz.metacentrum.perun.dispatcher.scheduling.DependenciesResolver;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDependencyDao.DependencyScope;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskSchedule;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.DelayQueue;


@org.springframework.stereotype.Service(value = "taskScheduler")
public class TaskSchedulerImpl implements TaskScheduler {
	private final static Logger log = LoggerFactory
			.getLogger(TaskSchedulerImpl.class);

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private DependenciesResolver dependenciesResolver;
	@Autowired
	private Perun perun;
	private PerunSession perunSession;
	@Autowired
	private Properties dispatcherPropertiesBean;
	@Autowired
	private DispatcherQueuePool dispatcherQueuePool;
	@Autowired
	private DenialsResolver denialsResolver;


	/**
	 * This method runs in separate thread perpetually trying to take tasks from delay queue, blocking if none are available.
	 * If there is Task ready, we check if it source was updated. If it was, we put the task back to the queue (This
	 * can happen only limited number of times). If on the other hand it was not updated we perform additional checks using
	 * method scheduleTask.
	 */
	@Override
	public void run() {
		try {
			initPerunSession();
		} catch (InternalErrorException e1) {
			String message = "Dispatcher was unable to initialize Perun session.";
			log.error(message, e1);
			throw new RuntimeException(message, e1);
		}
		log.debug("pool contains {} tasks in total", schedulingPool.getSize());
		log.debug("   {} tasks are going to be processed", schedulingPool.getWaitingTasks().size());
		DelayQueue<TaskSchedule> waitingTasksQueue = schedulingPool.getWaitingTasksQueue();
		while (true) {
			TaskSchedule schedule;
			try {
				schedule = waitingTasksQueue.take();
			} catch (InterruptedException e) {
				String message = "Thread was interrupted, cannot continue.";
				log.error(message, e);
				throw new RuntimeException(message, e);
			}
			Task task = schedule.getTask();
			if (task.getExecService().getExecServiceType()
					.equals(ExecServiceType.SEND)) {
				boolean sendToEngine = true;
				if (task.isSourceUpdated()) {
					if (schedule.getDelayCount() > 0) {
						schedulingPool.addTaskSchedule(task, schedule.getDelayCount() - 1, true);
						sendToEngine = false;
					}
				}
				if (sendToEngine) {
					scheduleTask(task);
				}
			}
			// GEN tasks are scheduled only as dependencies
		}
	}

	// TODO ensure dependant tasks with scope DESTINATION go to the same engine
	// TODO Is there problem a problem with other threads calling this method?
	@Override
	public void scheduleTask(Task task) {
		ExecService execService = task.getExecService();
		Facility facility = task.getFacility();
		Date time = new Date(System.currentTimeMillis());
		DispatcherQueue dispatcherQueue = null;

		if (task.getStatus().equals(TaskStatus.PROCESSING) && !task.isPropagationForced()) {
			log.debug("Task {} already processing, will not schedule again.",
					task.toString());
			return;
		}

		log.debug("Scheduling TASK " + task.toString());
		try {
			dispatcherQueue = schedulingPool.getQueueForTask(task);
			log.debug("Task {} is assigned to queue {}", task.getId(), (dispatcherQueue == null) ? "null" : dispatcherQueue.getClientID());
		} catch (InternalErrorException e) {
			log.warn("Task {} is not assigned to any queue", task.getId());
		}
		// check if the engine is still registered
		if (dispatcherQueue != null &&
				!dispatcherQueuePool.isThereDispatcherQueueForClient(dispatcherQueue.getClientID())) {
			dispatcherQueue = null;
		}
		if (dispatcherQueue == null) {
			// where should we send the task?
			dispatcherQueue = dispatcherQueuePool.getAvailableQueue();
			if (dispatcherQueue != null) {
				try {
					schedulingPool.setQueueForTask(task, dispatcherQueue);
				} catch (InternalErrorException e) {
					log.error("Could not set client queue for task {}: {}", task.getId(), e.getMessage());
					return;
				}
				log.debug("Assigned new queue {} to task {}",
						dispatcherQueue.getQueueName(),
						task.getId());
			} else {
				// bad luck...
				log.error("Task {} has no engine assigned and there are no engines registered...",
						task.toString());
				return;
			}
		}

		log.debug("Facility to be processed: {}, ExecService to be processed: {}",
				facility.getId(),
				execService.getId());
		log.debug("Is the execService ID: {} enabled globally?", execService.getId());
		if (execService.isEnabled()) {
			log.debug("   Yes, it is globally enabled.");
		} else {
			log.debug("   No, execService ID: {} is not enabled globally. Task will not run.", execService.getId());
			return;
		}

		log.debug("   Is the execService ID: {} denied on facility ID: {}?",
				execService.getId(), facility.getId());
		try {
			if (!denialsResolver.isExecServiceDeniedOnFacility(execService, facility)) {
				log.debug("   No, it is not.");
			} else {
				log.debug("   Yes, the execService ID: {} is denied on facility ID: {}. Task will not run.",
						execService.getId(), facility.getId());
				return;
			}
		} catch (InternalErrorException e) {
			log.error("Error getting disabled status for execService, task will not run now.");
			return;
		}

		List<ExecService> dependantServices = null;
		List<Pair<ExecService, DependencyScope>> dependencies = null;

		// If any of the ExecServices that depends on this one is running
		// PROCESSING
		// we will put the ExecService,Facility pair back to the pool.
		// #################################################################################
		log.debug("   Is there any execService that depends on [{}] in \"PROCESSING\" state?", execService.getId());
		dependantServices = dependenciesResolver.listDependantServices(execService);
		boolean proceed = true;
		for (ExecService dependantService : dependantServices) {
			Task dependantServiceTask = schedulingPool.getTask(
					dependantService, facility);
			if (dependantServiceTask != null) {
				if (dependantServiceTask.getStatus().equals(
						TaskStatus.PROCESSING)) {
					log.debug("   There is a service [{}] running that depends on this one [{}], so we put this to sleep...",
							dependantServiceTask.getId(), execService);
					// schedulingPool.addToPool(new Pair<ExecService,
					// Facility>(execService, facility));
					proceed = false;
					break;
				}
				// This is probably wrong, so commenting out:
				//   1) if there is some dispatcher queue active at the moment, dispatcherQueue is not null
				//   2) if there is no dispatcher queue available, no point in setting the same queue another task has
				//   3) the dependency should be handled the other way round, from dependant to dependent
				/*
				 * try {
				 *	if (dispatcherQueue == null &&
				 *			schedulingPool.getQueueForTask(dependantServiceTask) != null) {
				 *		schedulingPool.setQueueForTask(task, schedulingPool.getQueueForTask(dependantServiceTask));
				 *	}
				 * } catch (InternalErrorException e) {
				 *	log.debug("    Failed to set destination queue for task. This is weird, aborting.");
				 *	proceed = false;
				 *}
				 */
			}
		}
		if (proceed) {
			log.debug("   No, it is not. No dependent service is running, we can proceed.");
			// If it is an ExecService of type SEND, we have to check its
			// dependencies.
			// We can skip this for GENERATE type (it has no dependencies by
			// design).
			// ########################################################################
			log.debug("   Check whether the execService [{}] is of type SEND", execService.getId());
			if (execService.getExecServiceType().equals(ExecServiceType.SEND)) {
				log.debug("   Well, it is, so we have to check it's dependencies.");
				// We check the status of all the ExecServices this ExecService
				// depends on.
				//
				//
				// Current approach disregards any SEND/GENERATE differences.
				// Dependency on a GENERATE service is being treated as same as
				// any other SEND dependency
				// but for a small exception regarding ERROR and DONE states,
				// see below:
				//
				// If the dependency is in one of the following states, we do:
				// NONE Schedule it and wait (put this [ExecService,Facility]
				// pair back to the SchedulingPool for a while).
				// PROCESSING Wait
				// ERROR IF dependency is GENERATE THEN DO
				// Schedule it and wait (put this [ExecService,Facility] pair
				// back to the SchedulingPool for a while).
				// ELSE IF dependency is SEND THEN DO
				// End with ERROR. (no point in trying, something is probably
				// amiss on destination nodes...)
				// ELSE
				// throw new IllegalArgumentException
				// FI
				// DONE IF dependency is GENERATE THEN DO
				// Schedule it and wait (put this [ExecService,Facility] pair
				// back to the SchedulingPool for a while).
				//
				// It might look like we get an infinite loop where GENERATE
				// will be in DONE and then rescheduled again and again.
				// It is not so because PropagationMaintainer sets its state to
				// NONE as soon as the SEND, that depends on it,
				// enters either DONE or ERROR states (one of its finite
				// states).
				// ELSE IF dependency is SEND THEN DO
				// Proceed (Yes, no need to schedule this dependency, it is done
				// already and we don't care for how long it has been so at this
				// point.)
				// ELSE
				// throw new IllegalArgumentException
				// FI
				// :-)
				// #######################################################################################################
				proceed = true;
				dependencies = dependenciesResolver.listDependenciesAndScope(execService);
				log.debug("   We are about to loop over execService [{}] dependencies.", execService.getId());
				log.debug("   Number of dependencies:{}", dependencies);
				DispatcherQueue dependencyQueue = null;
				for (Pair<ExecService, DependencyScope> dependencyPair : dependencies) {
					ExecService dependency = dependencyPair.getLeft();
					DependencyScope dependencyScope = dependencyPair.getRight();
					Task dependencyServiceTask = schedulingPool.getTask(dependency, facility);
					if (dependencyServiceTask == null) {
						// Dependency being NULL is equivalent to being in NONE
						// state.
						log.info("   Last Task [dependency:{}, facility:{}] was NULL, we are gonna propagate.", dependency.getId(), facility.getId());
						scheduleItAndWait(dependency, facility, execService,
								dispatcherQueue, time);
						proceed = false;
					} else {
						dependencyServiceTask.setPropagationForced(task.isPropagationForced());
						switch (dependencyServiceTask.getStatus()) {
							case DONE:
								switch (dependency.getExecServiceType()) {
									case GENERATE:
										if (task.isSourceUpdated()) {
											// we need to reschedule the GEN task as the source data has changed
											log.debug("   Dependency ID "
													+ dependency.getId()
													+ " is in DONE and is going to be rescheduled as we need fresh data.");
											rescheduleTask(dependencyServiceTask, execService, dispatcherQueue);
											proceed = false;
										} else {
											log.debug("   Dependency ID "
													+ dependency.getId()
													+ " is in DONE and it is of type GENERATE, we can proceed.");
											// Nothing, we can proceed...
											try {
												dependencyQueue = schedulingPool.getQueueForTask(dependencyServiceTask);
											} catch (InternalErrorException e) {
												log.error("Could not get queue for task {}", dependencyServiceTask.getId());
											}
										}
										break;
									case SEND:
										log.debug("   Dependency ID "
												+ dependencyServiceTask.getId()
												+ " is in DONE and it is of type SEND, we can proceed.");
										// Nothing, we can proceed...
										break;
									default:
										throw new IllegalArgumentException(
												"Unknown ExecService type. Expected GENERATE or SEND.");
								}
								break;
							case ERROR:
								switch (dependency.getExecServiceType()) {
									case GENERATE:
										log.info("   Dependency ID "
												+ dependencyServiceTask.getId()
												+ " is in ERROR and it is of type GENERATE, we are gonna propagate.");
										// scheduleItAndWait(dependency, facility,
										// execService, dispatcherQueue);
										// try to run the generate task again
										rescheduleTask(dependencyServiceTask, execService, dispatcherQueue);
										proceed = false;
										break;
									case SEND:
										log.info("   Dependency ID "
												+ dependencyServiceTask.getId()
												+ " is in ERROR and it is of type SEND, we are gonna end with ERROR.");
										proceed = false;
										// We end Task with error immediately.
										schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
										// manipulateTasks(execService, facility, task);

										// And we set all its GENERATE dependencies as
										// "dirty" by switching them to NONE state.
										// Note: Yes, there might have been some stored
										// from the previous runs...
										// propagationMaintainer.setAllGenerateDependenciesToNone(dependencies,
										// facility);
										break;
									default:
										throw new IllegalArgumentException(
												"Unknown ExecService type. Expected GENERATE or SEND.");
								}
								break;
							case NONE:
								log.info("   Last Task {} [dependency:"
										+ dependency.getId() + ", facility:"
										+ facility.getId()
										+ "] was NONE, we are gonna propagate.", dependencyServiceTask.getId());
								rescheduleTask(dependencyServiceTask, execService,
										dispatcherQueue);
								proceed = false;
								break;
							case PLANNED:
								log.info("   Dependency ID " + dependencyServiceTask.getId()
										+ " is in PLANNED so we are gonna wait.");
								// we do not need to put it back in pool here
								// justWait(facility, execService);
								if (dependencyScope.equals(DependencyScope.SERVICE)) {
									proceed = false;
								} else {
									try {
										dependencyQueue = schedulingPool.getQueueForTask(dependencyServiceTask);
									} catch (InternalErrorException e) {
										log.error("Could not get queue for task {}", dependencyServiceTask.getId());
									}
								}
								break;
							case PROCESSING:
								log.info("   Dependency ID " + dependencyServiceTask.getId()
										+ " is in PROCESSING so we are gonna wait.");
								// we do not need to put it back in pool here
								// justWait(facility, execService);
								if (dependencyScope.equals(DependencyScope.SERVICE)) {
									proceed = false;
								} else {
									try {
										dependencyQueue = schedulingPool.getQueueForTask(dependencyServiceTask);
									} catch (InternalErrorException e) {
										log.error("Could not get queue for task {}", dependencyServiceTask.getId());
									}
								}
								if (dependencyServiceTask.isPropagationForced()) {
									rescheduleTask(dependencyServiceTask, execService, dispatcherQueue);
									// XXX - should we proceed here?
								}
								break;
							default:
								throw new IllegalArgumentException(
										"Unknown Task status. Expected DONE, ERROR, NONE, PLANNED or PROCESSING.");
						}
					}
				}
				// Finally, if we can proceed, we proceed...
				// #########################################
				if (proceed) {
					if (dependencyQueue != null && dependencyQueue != dispatcherQueue) {
						log.debug("Changing task {} destination queue to {} to match dependency task",
								task.getId(), dependencyQueue.getClientID());
						try {
							schedulingPool.setQueueForTask(task, dependencyQueue);
						} catch (InternalErrorException e) {
							log.error("Could not change task {} destination queue: {}",
									task.getId(), e.getMessage());
						}

					}
					log.info("   SCHEDULING task [{}], execService [{}] facility [{}] as PLANNED.",
							new Object[] {task.getId(), execService.getId(), facility.getId()});
					task.setSchedule(time);
					schedulingPool.setTaskStatus(task, TaskStatus.PLANNED);
					sendToEngine(task);
					// manipulateTasks(execService, facility, task);
				} else {
					// If we can not proceed, we just end here.
					// ########################################
					// The current ExecService,Facility pair should be sleeping
					// in SchedulingPool at the moment...
					log.info("   Task {} state set to NONE, will be scheduled again at the next cycle.",
							task.getId());
					schedulingPool.setTaskStatus(task, TaskStatus.NONE);
					schedulingPool.addTaskSchedule(task, -1);
				}
			} else if (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
				log.debug("   Well, it is not. ExecService of type GENERATE does not have any dependencies by design, so we schedule it immediately.");
				log.info("   SCHEDULING task [{}], execService [{}] facility [{}] as PLANNED.",
						new Object[] {task.getId(), execService.getId(), facility.getId()});
				task.setSchedule(time);
				schedulingPool.setTaskStatus(task, TaskStatus.PLANNED);
				sendToEngine(task);
				// manipulateTasks(execService, facility, task);
			} else {
				throw new IllegalArgumentException(
						"Unknown ExecService type. Expected GENERATE or SEND.");
			}
		} else {
			log.debug("   We do not proceed, we put the task [{}], [{}] execService to sleep.",
					task.getId(), execService.getId());
		}

	}

	private void scheduleItAndWait(ExecService dependency, Facility facility,
								   ExecService execService, DispatcherQueue dispatcherQueue, Date time) {
		// this is called to schedule dependencies of given task
		Task task = new Task();
		task.setExecService(dependency);
		task.setFacility(facility);
		task.setSchedule(time);
		try {
			schedulingPool.addToPool(task, dispatcherQueue);
			scheduleTask(task);
		} catch (InternalErrorException e) {
			log.error("Could not schedule new task: {}", e.getMessage());
		}
		// schedulingPool.setTaskStatus(task, TaskStatus.NONE);
	}

	private void rescheduleTask(Task dependencyServiceTask,
								ExecService execService, DispatcherQueue dispatcherQueue) {
		// task is in the pool already, just go for recursion
		scheduleTask(dependencyServiceTask);
	}

	private void sendToEngine(Task task) {
		DispatcherQueue dispatcherQueue;
		try {
			dispatcherQueue = schedulingPool.getQueueForTask(task);
		} catch (InternalErrorException e1) {
			log.error("No engine set for task {}, could not send it!", task.toString());
			return;
		}

		if (dispatcherQueue == null) {
			// where should we send the task?
			if (dispatcherQueuePool.poolSize() > 0) {
				dispatcherQueue = dispatcherQueuePool.getPool().iterator()
						.next();
				try {
					schedulingPool.setQueueForTask(task, dispatcherQueue);
				} catch (InternalErrorException e) {
					log.error("Could not assign new queue for task {}: {}", task.getId(), e);
					return;
				}
				log.debug("Assigned new queue {} to task {}",
						dispatcherQueue.getQueueName(), task.getId());
			} else {
				// bad luck...
				log.error("Task {} has no engine assigned and there are no engines registered...", task.toString());
				return;
			}
		}

		// task|[engine_id]|[task_id][is_forced][exec_service_id][facility]|[destination_list]|[dependency_list]
		// - the task|[engine_id] part is added by dispatcherQueue
		List<Destination> destinations = task.getDestinations();
		if (destinations == null || destinations.isEmpty()) {
			log.debug("No destinations for task {}, trying to query the database...", task.toString());
			try {
				initPerunSession();
				destinations = perun.getServicesManager().getDestinations(
						perunSession, task.getExecService().getService(),
						task.getFacility());
			} catch (ServiceNotExistsException e) {
				log.error("No destinations found for task {}", task.getId());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			} catch (FacilityNotExistsException e) {
				log.error("Facility for task {} does not exist...", task.getId());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			} catch (PrivilegeException e) {
				log.error("Privilege error accessing the database: {}", e.getMessage());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			} catch (InternalErrorException e) {
				log.error("Internal error: {}", e.getMessage());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			}
		}
		log.debug("Fetched destinations: " + ((destinations == null) ? "[]" : destinations.toString()));
		task.setDestinations(destinations);
		StringBuilder destinations_s = new StringBuilder("Destinations [");
		if (destinations != null) {
			for (Destination destination : destinations) {
				destinations_s.append(destination.serializeToString() + ", ");
			}
		}
		destinations_s.append("]");
		String dependencies = "";
		dispatcherQueue.sendMessage("[" + task.getId() + "]["
				+ task.isPropagationForced() + "]|["
				+ fixStringSeparators(task.getExecService().serializeToString()) + "]|["
				+ fixStringSeparators(task.getFacility().serializeToString()) + "]|["
				+ fixStringSeparators(destinations_s.toString()) + "]|[" + dependencies + "]");
		task.setStartTime(new Date(System.currentTimeMillis()));
		task.setEndTime(null);
		schedulingPool.setTaskStatus(task, TaskStatus.PROCESSING);
	}

	private String fixStringSeparators(String data) {
		if (data.contains("|")) {
			return new String(Base64.encodeBase64(data.getBytes()));
		} else {
			return data;
		}
	}

	@Override
	public int getPoolSize() {
		return schedulingPool.getSize();
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	protected void initPerunSession() throws InternalErrorException {
		if (perunSession == null) {
			perunSession = perun
					.getPerunSession(new PerunPrincipal(
							dispatcherPropertiesBean.getProperty("perun.principal.name"),
							dispatcherPropertiesBean
									.getProperty("perun.principal.extSourceName"),
							dispatcherPropertiesBean
									.getProperty("perun.principal.extSourceType")),
							new PerunClient());
		}
	}
}
