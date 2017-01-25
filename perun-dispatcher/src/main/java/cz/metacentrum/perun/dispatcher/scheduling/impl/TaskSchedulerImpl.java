package cz.metacentrum.perun.dispatcher.scheduling.impl;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.PerunClient;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
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

@org.springframework.stereotype.Service(value = "taskScheduler")
public class TaskSchedulerImpl implements TaskScheduler {
	private final static Logger log = LoggerFactory
			.getLogger(TaskSchedulerImpl.class);

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private DependenciesResolver dependenciesResolver;
	@Autowired
	private ApplicationContext appCtx;
	private PerunBl perun;
	private PerunSession perunSession;
	@Autowired
	private Properties dispatcherPropertiesBean;
	@Autowired
	private DispatcherQueuePool dispatcherQueuePool;
	@Autowired
	private DenialsResolver denialsResolver;
	@Autowired
	private GeneralServiceManager generalServiceManager;


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
	public Boolean scheduleTask(Task task) {
		ExecService execService = task.getExecService();
		Facility facility = task.getFacility();
		Date time = new Date(System.currentTimeMillis());
		DispatcherQueue dispatcherQueue = null;

		if (task.getStatus().equals(TaskStatus.PROCESSING) && !task.isPropagationForced()) {
			log.debug("Task {} already processing, will not schedule again.",
					task.toString());
			return true;
		}

		log.debug("Scheduling TASK " + task.toString());
		try {
			dispatcherQueue = schedulingPool.getQueueForTask(task);
			log.debug("Task {} is assigned to queue {}", task.getId(), (dispatcherQueue == null) ? "null" : dispatcherQueue.getClientID());
		} catch (InternalErrorException e) {
			log.warn("Task {} is not assigned to any queue", task.getId());
		}
		// check if the engine is still registered
		if(dispatcherQueue != null &&
				!dispatcherQueuePool.isThereDispatcherQueueForClient(dispatcherQueue.getClientID())) {
			dispatcherQueue = null;
		}
		if (dispatcherQueue == null) {
			// where should we send the task?
			dispatcherQueue = dispatcherQueuePool.getAvailableQueue();
			if(dispatcherQueue != null) {
				try {
					schedulingPool.setQueueForTask(task, dispatcherQueue);
				} catch (InternalErrorException e) {
					log.error("Could not set client queue for task {}: {}", task.getId(), e.getMessage());
					return true;
				}
				log.debug("Assigned new queue "
						+ dispatcherQueue.getQueueName() + " to task "
						+ task.getId());
			} else {
				// bad luck...
				log.error("Task "
						+ task.toString()
						+ " has no engine assigned and there are no engines registered...");
				return true;
			}
		}

		log.debug("Facility to be processed: " + facility.getId()
				+ ", ExecService to be processed: " + execService.getId());
		

		Boolean abortTask = false;
		try {
			refetchTaskInformation(task);
			List<Service> assignedServices = perun.getServicesManagerBl().getAssignedServices(perunSession, task.getFacility());
			if (!assignedServices.contains(execService.getService())) {
				log.debug("Task {} has no longer service {} assigned, aborting.", task.getId(), execService.getId());
				abortTask = true;
			}
		} catch (FacilityNotExistsException e1) {
			log.debug("Facility {} for task {} no longer exists, aborting", facility.getId(), task.getId());
			abortTask = true;
		} catch (ServiceNotExistsException e1) {
			log.debug("Service {} for task {} no longer exists, aborting", execService.getId(), task.getId());
			abortTask = true;
		} catch (InternalErrorException e1) {
			log.error("Error checking facility or exec service for updates, task will not run now: {}", e1.getMessage());
			return true;
		} catch (PrivilegeException e1) {
			log.error("Error checking facility or exec service for updates, task will not run now: {}", e1.getMessage());
			return true;
		}
		// SEND tasks will be aborted later - we have to go on and try to schedule (and abort)
		// GEN tasks that we depend on...
		if(abortTask && execService.getExecServiceType() == ExecServiceType.GENERATE) {
			// GEN tasks may be aborted immediately
			abortTask(task);
			return false;
		}

		// We have to be carefull from now on - the facility and/or exec service contained
		// in this task may no longer be valid with respect to the actual database (ie. when abortTask == true).
		// On the other hand, the objects themselves are still here, so they may be referenced in code. 
		
		// do not perform further checks for task that is going to be aborted
		if(!abortTask) {

			log.debug("Is the execService ID:" + execService.getId() + " enabled globally?");
			if (execService.isEnabled()) {
				log.debug("   Yes, it is globally enabled.");
			} else {
				log.debug("   No, execService ID: "+ execService.getId() + " is not enabled globally. Task will not run.");
				return true;
			}

			log.debug("   Is the execService ID: " + execService.getId() + " denied on facility ID:" + facility.getId() + "?");
			try {
				if (!denialsResolver.isExecServiceDeniedOnFacility(execService, facility)) {
					log.debug("   No, it is not.");
				} else {
					log.debug("   Yes, the execService ID: " + execService.getId() + " is denied on facility ID: "
							+ facility.getId() + ". Task will not run.");
					return true;
				}
			} catch (InternalErrorException e) {
				log.error("Error getting disabled status for execService, task will not run now.");
				return true;
			}
		}

		List<ExecService> dependantServices = null;
		List<Pair<ExecService, DependencyScope>> dependencies = null;

		// If any of the ExecServices that depends on this one is running
		// PROCESSING
		// we will put the ExecService,Facility pair back to the pool.
		// #################################################################################
		log.debug("   Is there any execService that depends on ["
				+ execService.getId() + "] in \"PROCESSING\" state?");
		dependantServices = dependenciesResolver.listDependantServices(execService);
		boolean proceed = true;
		for (ExecService dependantService : dependantServices) {
			Task dependantServiceTask = schedulingPool.getTask(
					dependantService, facility);
			if (dependantServiceTask != null) {
				if (dependantServiceTask.getStatus().equals(
						TaskStatus.PROCESSING)) {
					log.debug("   There is a service [" + dependantServiceTask.getId()
							+ "] running that depends on this one ["
							+ execService + "], so we put this to sleep...");
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
				log.debug("   We are about to loop over execService ["
						+ execService.getId() + "] dependencies.");
				log.debug("   Number of dependencies:" + dependencies);
				DispatcherQueue dependencyQueue = null;
				for (Pair<ExecService, DependencyScope> dependencyPair : dependencies) {
					ExecService dependency = dependencyPair.getLeft();
					DependencyScope dependencyScope = dependencyPair.getRight();
					Task dependencyServiceTask = schedulingPool.getTask(dependency, facility);
					if (dependencyServiceTask == null) {
						if(abortTask) {
							log.info("   Task {} is going to be aborted, the dependency exec service {} will not be scheduled now.",
									task.getId(), dependency.getId());
						} else {
							// Dependency being NULL is equivalent to being in NONE
							// state.
							log.info("   Last Task [dependency:"
									+ dependency.getId() + ", facility:"
									+ facility.getId()
									+ "] was NULL, we are gonna propagate.");
							if(scheduleItAndWait(dependency, facility, execService,
									dispatcherQueue, time)) {
								// task sucessfully scheduled, nothing to do
							} else {
								// TODO: task aborted - maybe set this one to error?
							}
						}
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
							if(dependencyServiceTask.isPropagationForced()) {
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
					if(abortTask) {
						// the SEND task is going to be aborted now
						abortTask(task);
						return false;
					} else {
						if(dependencyQueue != null && dependencyQueue != dispatcherQueue) {
							log.debug("Changing task {} destination queue to {} to match dependency task",
									task.getId(), dependencyQueue.getClientID());
							try {
								schedulingPool.setQueueForTask(task, dependencyQueue);
							} catch (InternalErrorException e) {
								log.error("Could not change task {} destination queue: {}", 
										task.getId(), e.getMessage());
							}
						
						}
						log.info("   SCHEDULING task [" + task.getId() + "], execService ["
								+ execService.getId() + "] facility ["
								+ facility.getId() + "] as PLANNED.");
						task.setSchedule(time);
						schedulingPool.setTaskStatus(task, TaskStatus.PLANNED);
						sendToEngine(task);
					}
					// manipulateTasks(execService, facility, task);
				} else {
					if(abortTask) {
						// the SEND task is going to be aborted now
						abortTask(task);
						return false;
					} else {
						// If we can not proceed, we just end here.
						// ########################################
						// The current ExecService,Facility pair should be sleeping
						// in SchedulingPool at the moment...
						log.info("   Task {} state set to NONE, will be scheduled again at the next cycle.",
								task.getId());
						schedulingPool.setTaskStatus(task, TaskStatus.NONE);
					}
				}
			} else if (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
				log.debug("   Well, it is not. ExecService of type GENERATE does not have any dependencies by design, so we schedule it immediately.");
				log.info("   SCHEDULING task [" + task.getId() + "], execService [" + execService.getId()
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
			log.debug("   We do not proceed, we put the task [" + task.getId() + "], ["
					+ execService.getId() + "] execService to sleep.");
		}
		return true;
	}

	private void abortTask(Task task) {
		log.debug("Aborting task {}, removing from pool.", task.getId());
		schedulingPool.removeTask(task);
	}

	private void refetchTaskInformation(Task task) throws FacilityNotExistsException, InternalErrorException, PrivilegeException, ServiceNotExistsException {
		// reread facility
		log.debug("Rereading facility and  exec service for task {}", task.getId());
		Facility dbFacility = perun.getFacilitiesManagerBl().getFacilityById(perunSession, task.getFacilityId());
		if(dbFacility == null) {
			throw new FacilityNotExistsException("No facility with id " + task.getFacilityId());
		}
		Boolean taskModified = false;
		if(!dbFacility.equals(task.getFacility())) {
			task.setFacility(dbFacility);
			taskModified = true;
		}
		// reread exec service (and service)
		ExecService dbExecService = generalServiceManager.getExecService(perunSession, task.getExecServiceId());
		if(dbExecService == null) {
			throw new ServiceNotExistsException("No exec service with id " + task.getExecServiceId());
		}
		if(!dbExecService.equals(task.getExecService())) {
			task.setExecService(dbExecService);
			taskModified = true;
		}
		if(taskModified) {
			log.debug("Task components have changed, updating task {}", task.getId());
			schedulingPool.setTaskStatus(task, task.getStatus());
		}
	}

	private Boolean scheduleItAndWait(ExecService dependency, Facility facility,
			ExecService execService, DispatcherQueue dispatcherQueue, Date time) {
		// this is called to schedule dependencies of given task
		Task task = new Task();
		task.setExecService(dependency);
		task.setFacility(facility);
		task.setSchedule(time);
		try {
			schedulingPool.addToPool(task, dispatcherQueue);
			return scheduleTask(task);
		} catch (InternalErrorException e) {
			log.error("Could not schedule new task: " + e.getMessage());
			return false;
		}
		// schedulingPool.setTaskStatus(task, TaskStatus.NONE);
	}

	private Boolean rescheduleTask(Task dependencyServiceTask,
			ExecService execService, DispatcherQueue dispatcherQueue) {
		// task is in the pool already, just go for recursion
		return scheduleTask(dependencyServiceTask);
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
				try {
					schedulingPool.setQueueForTask(task, dispatcherQueue);
				} catch (InternalErrorException e) {
					log.error("Could not assign new queue for task {}: {}", task.getId(), e);
					return;
				}
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
				log.error("No destinations found for task " + task.getId());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			} catch (FacilityNotExistsException e) {
				log.error("Facility for task {} does not exist...", task.getId());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			} catch (PrivilegeException e) {
				log.error("Privilege error accessing the database: "
						+ e.getMessage());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			} catch (InternalErrorException e) {
				log.error("Internal error: " + e.getMessage());
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				return;
			}
		}
		log.debug("Fetched destinations: " + ( (destinations == null) ?  "[]" : destinations.toString()));
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
		if(perun == null) {
			perun = appCtx.getBean("perun",  PerunBl.class);
		}
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
