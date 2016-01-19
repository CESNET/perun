package cz.metacentrum.perun.dispatcher.scheduling.impl;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.ServicesManager;
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
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDependencyDao.DependencyScope;

;

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
	
	@Override
	public void processPool() throws InternalErrorException {
		initPerunSession();
		log.debug("pool contains " + schedulingPool.getSize()
				+ " tasks in total");
		log.debug("  " + schedulingPool.getWaitingTasks().size()
				+ " tasks are going to be processed");
		for (Task task : schedulingPool.getWaitingTasks()) {
			if (task.getExecService().getExecServiceType()
					.equals(ExecServiceType.SEND)) {
				scheduleTask(task);
			}
			// GEN tasks are scheduled only as dependencies
		}
	}

	// TODO ensure dependant tasks with scope DESTINATION go to the same engine
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
		} catch (InternalErrorException e) {
			log.warn("Task {} is not assigned to any queue", task.toString());
		}
		// TODO: should take into account assignment of dependencies...ie to schedule SEND to the same queue as GEN
		if (dispatcherQueue == null) {
			// where should we send the task?
			if (dispatcherQueuePool.poolSize() > 0) {
				dispatcherQueue = dispatcherQueuePool.getPool().iterator().next();
				schedulingPool.setQueueForTask(task, dispatcherQueue);
				log.debug("Assigned new queue "
						+ dispatcherQueue.getQueueName() + " to task "
						+ task.getId());
			} else {
				// bad luck...
				log.error("Task "
						+ task.toString()
						+ " has no engine assigned and there are no engines registered...");
				return;
			}
		}

		log.debug("Facility to be processed: " + facility.getId()
				+ ", ExecService to be processed: " + execService.getId());
		
		log.debug("Task is assigned to queue " + dispatcherQueue.getClientID());
		
		log.debug("Is the execService ID:" + execService.getId() + " enabled globally?");
		if (execService.isEnabled()) {
			log.debug("   Yes, it is globally enabled.");
		} else {
			log.debug("   No, execService ID: "+ execService.getId() + " is not enabled globally. Task will not run.");
			return;
		}

		log.debug("   Is the execService ID: " + execService.getId() + " denied on facility ID:" + facility.getId() + "?");
		try {
			if (!denialsResolver.isExecServiceDeniedOnFacility(execService, facility)) {
				log.debug("   No, it is not.");
			} else {
				log.debug("   Yes, the execService ID: " + execService.getId() + " is denied on facility ID: "
						+ facility.getId() + ". Task will not run.");
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
		log.debug("   Is there any execService that depends on ["
				+ execService.getId() + "] in \"PROCESSING\" state?");
		dependantServices = dependenciesResolver
				.listDependantServices(execService);
		boolean proceed = true;
		for (ExecService dependantService : dependantServices) {
			Task dependantServiceTask = schedulingPool.getTask(
					dependantService, facility);
			if (dependantServiceTask != null) {
				if (dependantServiceTask.getStatus().equals(
						TaskStatus.PROCESSING)) {
					log.debug("   There is a service [" + dependantService
							+ "] running that depends on this one ["
							+ execService + "], so we put this to sleep...");
					// schedulingPool.addToPool(new Pair<ExecService,
					// Facility>(execService, facility));
					proceed = false;
					break;
				}
				try {
					if (dispatcherQueue == null &&
							schedulingPool.getQueueForTask(dependantServiceTask) != null) {
						schedulingPool.setQueueForTask(task, schedulingPool.getQueueForTask(dependantServiceTask));
					}
				} catch (InternalErrorException e) {
					log.debug("    Failed to set destination queue for task. This is weird, aborting.");
					proceed = false;
				}
			}
		}
		if (proceed) {
			log.debug("   No, it is not. No dependent service is running, we can proceed.");
			// If it is an ExecService of type SEND, we have to check its
			// dependencies.
			// We can skip this for GENERATE type (it has no dependencies by
			// design).
			// ########################################################################
			log.debug("   Check whether the execService ["
					+ execService.getId() + "] is of type SEND");
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
				log.debug("listDependencies #1:" + dependencies);
				log.debug("   We are about to loop over execService ["
						+ execService.getId() + "] dependencies.");
				log.debug("listDependencies #2:" + dependencies);
				log.debug("   Number of dependencies:" + dependencies);
				for (Pair<ExecService, DependencyScope> dependencyPair : dependencies) {
					ExecService dependency = dependencyPair.getLeft();
					DependencyScope dependencyScope = dependencyPair.getRight();
					Task dependencyServiceTask = schedulingPool.getTask(dependency, facility);
					if (dependencyServiceTask == null) {
						// Dependency being NULL is equivalent to being in NONE
						// state.
						log.info("   Last Task [dependency:"
								+ dependency.getId() + ", facility:"
								+ facility.getId()
								+ "] was NULL, we are gonna propagate.");
						scheduleItAndWait(dependency, facility, execService,
								dispatcherQueue, time);
						proceed = false;
					} else {
						dependencyServiceTask.setPropagationForced(task.isPropagationForced());
						switch (dependencyServiceTask.getStatus()) {
						case DONE:
							switch (dependency.getExecServiceType()) {
							case GENERATE:
								if(task.isSourceUpdated()) {
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
								}
								break;
							case SEND:
								log.debug("   Dependency ID "
										+ dependency.getId()
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
										+ dependency.getId()
										+ " is in ERROR and it is of type GENERATE, we are gonna propagate.");
								// scheduleItAndWait(dependency, facility,
								// execService, dispatcherQueue);
								// try to run the generate task again
								rescheduleTask(dependencyServiceTask, execService, dispatcherQueue);
								proceed = false;
								break;
							case SEND:
								log.info("   Dependency ID "
										+ dependency.getId()
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
							log.info("   Last Task [dependency:"
									+ dependency.getId() + ", facility:"
									+ facility.getId()
									+ "] was NONE, we are gonna propagate.");
							rescheduleTask(dependencyServiceTask, execService,
									dispatcherQueue);
							proceed = false;
							break;
						case PLANNED:
							log.info("   Dependency ID " + dependency.getId()
									+ " is in PLANNED so we are gonna wait.");
							// we do not need to put it back in pool here
							// justWait(facility, execService);
							if (dependencyScope.equals(DependencyScope.SERVICE)) {
								proceed = false;
							}
							break;
						case PROCESSING:
							log.info("   Dependency ID " + dependency.getId()
									+ " is in PROCESSING so we are gonna wait.");
							// we do not need to put it back in pool here
							// justWait(facility, execService);
							if (dependencyScope.equals(DependencyScope.SERVICE)) {
								proceed = false;
							}
							if(dependencyServiceTask.isPropagationForced()) {
								rescheduleTask(dependencyServiceTask, execService, dispatcherQueue);
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
					log.info("   SCHEDULING execService ["
							+ execService.getId() + "] facility ["
							+ facility.getId() + "] as PLANNED.");
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
				}
			} else if (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
				log.debug("   Well, it is not. ExecService of type GENERATE does not have any dependencies by design, so we schedule it immediately.");
				log.info("   SCHEDULING execService [" + execService.getId()
						+ "] facility [" + facility.getId() + "] as PLANNED.");
				task.setSchedule(time);
				schedulingPool.setTaskStatus(task, TaskStatus.PLANNED);
				sendToEngine(task);
				// manipulateTasks(execService, facility, task);
			} else {
				throw new IllegalArgumentException(
						"Unknown ExecService type. Expected GENERATE or SEND.");
			}
		} else {
			log.debug("   We do not proceed, we put the ["
					+ execService.getId() + "] execService to sleep.");
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
			log.error("Could not schedule new task: " + e.getMessage());
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
			log.error("No engine set for task " + task.toString()
					+ ", could not send it!");
			return;
		}

		if (dispatcherQueue == null) {
			// where should we send the task?
			if (dispatcherQueuePool.poolSize() > 0) {
				dispatcherQueue = dispatcherQueuePool.getPool().iterator()
						.next();
				schedulingPool.setQueueForTask(task, dispatcherQueue);
				log.debug("Assigned new queue "
						+ dispatcherQueue.getQueueName() + " to task "
						+ task.getId());
			} else {
				// bad luck...
				log.error("Task "
						+ task.toString()
						+ " has no engine assigned and there are no engines registered...");
				return;
			}
		}

		// task|[engine_id]|[task_id][is_forced][exec_service_id][facility]|[destination_list]|[dependency_list]
		// - the task|[engine_id] part is added by dispatcherQueue
		List<Destination> destinations = task.getDestinations();
		if (destinations == null || destinations.isEmpty()) {
			log.debug("No destinations for task " + task.toString()
					+ ", trying to query the database...");
			try {
				initPerunSession();
				destinations = perun.getServicesManager().getDestinations(
						perunSession, task.getExecService().getService(),
						task.getFacility());
			} catch (ServiceNotExistsException e) {
				log.error("No destinations found for task " + task.toString());
				// TODO: remove the task?
			} catch (FacilityNotExistsException e) {
				log.error("Facility for task does not exist..."
						+ task.toString());
				// TODO: remove the task?
			} catch (PrivilegeException e) {
				log.error("Privilege error accessing the database: "
						+ e.getMessage());
			} catch (InternalErrorException e) {
				log.error("Internal error: " + e.getMessage());
			}
		}
		log.debug("Fetched destinations: " + destinations.toString());
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
				+ task.isPropagationForced() + "]["
				+ task.getExecServiceId() + "]["
				+ fixStringSeparators(task.getFacility().serializeToString()) + "]|["
				+ fixStringSeparators(destinations_s.toString()) + "]|[" + dependencies + "]");
		task.setStartTime(new Date(System.currentTimeMillis()));
		task.setEndTime(null);
		schedulingPool.setTaskStatus(task, TaskStatus.PROCESSING);
	}

	private String fixStringSeparators(String data) {
		if(data.contains("|")) {
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

	private void initPerunSession() throws InternalErrorException {
		if (perunSession == null) {
			perunSession = perun
					.getPerunSession(new PerunPrincipal(
							dispatcherPropertiesBean.getProperty("perun.principal.name"),
							dispatcherPropertiesBean
									.getProperty("perun.principal.extSourceName"),
							dispatcherPropertiesBean
									.getProperty("perun.principal.extSourceType")));
		}
	}
}
