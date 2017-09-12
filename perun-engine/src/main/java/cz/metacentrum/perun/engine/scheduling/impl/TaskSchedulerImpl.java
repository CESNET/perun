package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "taskScheduler")
// @Transactional
public class TaskSchedulerImpl implements TaskScheduler {

	private final static Logger log = LoggerFactory.getLogger(TaskSchedulerImpl.class);

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskStatusManager taskStatusManager;
	@Autowired
	private DependenciesResolver dependenciesResolver;
	@Autowired
	private TaskExecutorEngine taskExecutorEngine;

	@Override
	public void propagateService(Task task, Date time)
			throws InternalErrorException {

		// check if we have destinations for this task
		List<Destination> destinations = task.getDestinations();
		if (task.getExecService().getExecServiceType().equals(ExecServiceType.SEND)
				&& (destinations == null || destinations.isEmpty())) {
			log.info("No destinations found for SEND task {}, marking as ERROR",
					task.getId());
			schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
		} else {
			if(task.getStatus() == TaskStatus.PROCESSING) {
				log.warn("Attempt to schedule already processing task {}, ignoring this.", task.getId());
			} else {
				schedulingPool.setTaskStatus(task, TaskStatus.PLANNED);
				taskStatusManager.clearTaskStatus(task);
				task.setSchedule(time);
				if(task.isPropagationForced()) {
					// we are in special thread anyway...
					taskExecutorEngine.runTask(task);
				}
			}
		}
	}

	@Override
	public void rescheduleTask(Task task) {
		// TODO: Logic regarding the Task status etc...
		// taskManager.updateTask(task,
		// Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
	}

	@Override
	public void processPool() throws InternalErrorException {

		for (Task task : schedulingPool.getNewTasks()) {
			if(task.isPropagationForced()) {
				// this can happen only as a race between this thread and thread created for force-scheduled task
				log.info("Found force-scheduled task {} during normal schedule cycle, resetting force flag",
							task.getId());
				// may not be the best behavior...
				task.setPropagationForced(false);
			}
			log.debug("Propagating task {}, ExecService:Facility : "
					+ task.getExecServiceId() + ":" + task.getFacilityId(), task.getId());
			propagateService(task, new Date(System.currentTimeMillis()));
		}
	}

	@Override
	public int getPoolSize() {
		return schedulingPool.getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cz.metacentrum.perun.engine.scheduling.TaskScheduler#propagateService
	 * (cz.metacentrum.perun.taskslib.model.ExecService, java.util.Date,
	 * cz.metacentrum.perun.core.api.Facility)
	 * 
	 * This basically takes (execService, facility) pair and after some sanity
	 * checks (ie. is the service enabled, is there a task running already, are
	 * dependant tasks running) it recursively schedules the dependencies (adds
	 * them to the scheduling pool) and when it finds task ready to run, it puts
	 * it to the PLANNED state to be picked up by TaskExecutorEngine.
	 */
	@Deprecated
	@Override
	public void propagateService(ExecService execService, Date time,
			Facility facility) throws InternalErrorException {
		/*
		 * log.debug("Facility to be processed: " + facility.getId() +
		 * ", ExecService to be processed: " + execService.getId());
		 * 
		 * // TODO: EDIT: Denials are to be resolved in TaskExecutorEngine class
		 * as well (for each destination)
		 * 
		 * Task previousTask = null; List<ExecService> dependantServices = null;
		 * List<ExecService> dependencies = null;
		 * 
		 * // Is the ExecService enabled? (Global setting) // If it is not, we
		 * drop it and do nothing. //
		 * ############################################
		 * log.debug("Is the execService ID:" + execService.getId() +
		 * " enabled globally?"); if (execService.isEnabled()) {
		 * log.debug("   Yes, it is globally enabled."); // Is the ExecService
		 * denied on this Facility? // If it is, we drop it and do nothing. //
		 * ###########################################
		 * log.debug("   Is the execService ID:" + execService.getId() +
		 * " denied on facility ID:" + facility.getId() + "?"); if
		 * (!denialsResolver.isExecServiceDeniedOnFacility(execService,
		 * facility)) { log.debug("   No, it is not."); // Isn't there the same
		 * ExecService,Facility pair (Task) PROCESSING at the moment? // If it
		 * is, we drop it and do nothing (there is no point in scheduling it
		 * again at the moment). //
		 * #############################################
		 * ################################################
		 * log.debug("   What is the previous task status for the execService ID:"
		 * + execService.getId() + ":facility ID:" + facility.getId() +
		 * " couple?"); previousTask = taskManager.getTask(execService,
		 * facility,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); if
		 * (previousTask == null ||
		 * !previousTask.getStatus().equals(TaskStatus.PROCESSING)) { log.debug(
		 * "   The previous status IS either null or IS NOT \"PROCESSING\".");
		 * // If any of the ExecServices that depends on this one is running
		 * PROCESSING // we will put the ExecService,Facility pair back to the
		 * pool. //
		 * #############################################################
		 * ####################
		 * log.debug("   Is there any execService that depends on [" +
		 * execService.getId() + "] in \"PROCESSING\" state?"); try {
		 * dependantServices =
		 * dependenciesResolver.listDependantServices(execService); } catch
		 * (ServiceNotExistsException e) { log.error(e.toString()); } catch
		 * (InternalErrorException e) { log.error(e.toString()); } catch
		 * (PrivilegeException e) { log.error(e.toString()); } catch (Exception
		 * e) { log.error(e.toString()); } boolean proceed = true; for
		 * (ExecService dependantService : dependantServices) { Task
		 * dependantServiceTask = taskManager.getTask(dependantService,
		 * facility,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); if
		 * (dependantServiceTask != null) { if
		 * (dependantServiceTask.getStatus().equals(TaskStatus.PROCESSING)) {
		 * log.debug("   There is a service [" + dependantService +
		 * "] running that depends on this one [" + execService +
		 * "], so we put this to sleep..."); schedulingPool.addToPool(new
		 * Pair<ExecService, Facility>(execService, facility)); proceed = false;
		 * break; } } } if (proceed) { log.debug(
		 * "   No, it is not. No dependent service is running, we can proceed."
		 * ); // If it is an ExecService of type SEND, we have to check its
		 * dependencies. // We can skip this for GENERATE type (it has no
		 * dependencies by design). //
		 * ##########################################
		 * ##############################
		 * log.debug("   Check whether the execService [" + execService.getId()
		 * + "] is of type SEND"); if
		 * (execService.getExecServiceType().equals(ExecServiceType.SEND)) {
		 * log.debug("   Well, it is, so we have to check it's dependencies.");
		 * // We check the status of all the ExecServices this ExecService
		 * depends on. // //
		 * BEGIN-FORMER-APPROACH-WHERE-GENERATE-WAS-TREATED-DIFFERENTLY // If
		 * the dependency is of type GENERATE: // NONE - Schedule and Proceed //
		 * PLANNED - Wait, lets put this [ExecService,Facility] pair back to the
		 * SchedulingPool for a while. // PROCESSING - Wait, lets put this
		 * [ExecService,Facility] pair back to the SchedulingPool for a while.
		 * // ERROR - Schedule and Proceed (ending on ERROR would cause
		 * dead-lock...) // DONE - Schedule and Proceed // // If the dependency
		 * is of type SEND: // NONE - Schedule it and wait (put this
		 * [ExecService,Facility] pair back to the SchedulingPool for a while).
		 * // PLANNED - Wait, lets put this [ExecService,Facility] pair back to
		 * the SchedulingPool for a while. // PROCESSING - Wait, lets put this
		 * [ExecService,Facility] pair back to the SchedulingPool for a while.
		 * // ERROR - End with ERROR. // DONE - Proceed // // TaskExecutorEngine
		 * is responsible for ensuring the sequence GENERATE -> SEND. //
		 * END-FORMER-APPROACH-WHERE-GENERATE-WAS-TREATED-DIFFERENTLY // //
		 * Current approach disregards any SEND/GENERATE differences. //
		 * Dependency on a GENERATE service is being treated as same as any
		 * other SEND dependency // but for a small exception regarding ERROR
		 * and DONE states, see below: // //If the dependency is in one of the
		 * following states, we do: // NONE Schedule it and wait (put this
		 * [ExecService,Facility] pair back to the SchedulingPool for a while).
		 * // PLANNED Wait, lets put this [ExecService,Facility] pair back to
		 * the SchedulingPool for a while. // PROCESSING Wait, lets put this
		 * [ExecService,Facility] pair back to the SchedulingPool for a while.
		 * // ERROR IF dependency is GENERATE THEN DO // Schedule it and wait
		 * (put this [ExecService,Facility] pair back to the SchedulingPool for
		 * a while). // ELSE IF dependency is SEND THEN DO // End with ERROR.
		 * (no point in trying, something is probably amiss on destination
		 * nodes...) // ELSE // throw new IllegalArgumentException // FI // DONE
		 * IF dependency is GENERATE THEN DO // Schedule it and wait (put this
		 * [ExecService,Facility] pair back to the SchedulingPool for a while).
		 * // // It might look like we get an infinite loop where GENERATE will
		 * be in DONE and then rescheduled again and again. // It is not so
		 * because PropagationMaintainer sets its state to NONE as soon as the
		 * SEND, that depends on it, // enters either DONE or ERROR states (one
		 * of its finite states). // ELSE IF dependency is SEND THEN DO //
		 * Proceed (Yes, no need to schedule this dependency, it is done already
		 * and we don't care for how long it has been so at this point.) // ELSE
		 * // throw new IllegalArgumentException // FI // :-) //
		 * ################
		 * ######################################################
		 * ################################# proceed = true; try { dependencies
		 * = dependenciesResolver.listDependencies(execService);
		 * log.debug("listDependencies #1:" + dependencies); } catch
		 * (ServiceNotExistsException e) {
		 * log.error("listDependencies: ERROR execService[" +
		 * execService.getId() + "], service[" +
		 * execService.getService().getId() + "]: " + e.toString()); } catch
		 * (InternalErrorException e) { log.error("listDependencies: ERROR" +
		 * e.toString()); } catch (PrivilegeException e) {
		 * log.error("listDependencies: ERROR" + e.toString()); }
		 * log.debug("   We are about to loop over execService [" +
		 * execService.getId() + "] dependencies.");
		 * log.debug("listDependencies #2:" + dependencies);
		 * log.debug("   Number of dependencies:" + dependencies); for
		 * (ExecService dependency : dependencies) { Task dependencyServiceTask
		 * = taskManager.getTask(dependency, facility,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); if
		 * (dependencyServiceTask == null) { //Dependency being NULL is
		 * equivalent to being in NONE state.
		 * log.info("   Last Task [dependency:" + dependency.getId() +
		 * ", facility:" + facility.getId() +
		 * "] was NULL, we are gonna propagate."); scheduleItAndWait(dependency,
		 * facility, execService); proceed = false; } else { switch
		 * (dependencyServiceTask.getStatus()) { case DONE: switch
		 * (dependency.getExecServiceType()) { case GENERATE:
		 * log.debug("   Dependency ID " + dependency.getId() +
		 * " is in DONE and it is of type GENERATE, we can proceed."); //
		 * Nothing, we can proceed... break; case SEND:
		 * log.debug("   Dependency ID " + dependency.getId() +
		 * " is in DONE and it is of type SEND, we can proceed."); // Nothing,
		 * we can proceed... break; default: throw new IllegalArgumentException(
		 * "Unknown ExecService type. Expected GENERATE or SEND."); } break;
		 * case ERROR: switch (dependency.getExecServiceType()) { case GENERATE:
		 * log.info("   Dependency ID " + dependency.getId() +
		 * " is in ERROR and it is of type GENERATE, we are gonna propagate.");
		 * scheduleItAndWait(dependency, facility, execService); proceed =
		 * false; break; case SEND: log.info("   Dependency ID " +
		 * dependency.getId() +
		 * " is in ERROR and it is of type SEND, we are gonna end with ERROR.");
		 * proceed = false; // We end Task with error immediately. Task task =
		 * new Task(); task.setDelay(execService.getDefaultDelay());
		 * task.setExecService(execService); task.setFacility(facility);
		 * task.setRecurrence(execService.getDefaultRecurrence());
		 * task.setSchedule(time); task.setStatus(TaskStatus.ERROR);
		 * manipulateTasks(execService, facility, task);
		 * 
		 * // And we set all its GENERATE dependencies as "dirty" by switching
		 * them to NONE state. // Note: Yes, there might have been some stored
		 * from the previous runs...
		 * propagationMaintainer.setAllGenerateDependenciesToNone(dependencies,
		 * facility); break; default: throw new IllegalArgumentException(
		 * "Unknown ExecService type. Expected GENERATE or SEND."); } break;
		 * case NONE: log.info("   Last Task [dependency:" + dependency.getId()
		 * + ", facility:" + facility.getId() +
		 * "] was NONE, we are gonna propagate."); scheduleItAndWait(dependency,
		 * facility, execService); proceed = false; break; case PLANNED:
		 * log.info("   Dependency ID " + dependency.getId() +
		 * " is in PLANNED so we are gonna wait."); justWait(facility,
		 * execService); proceed = false; break; case PROCESSING:
		 * log.info("   Dependency ID " + dependency.getId() +
		 * " is in PROCESSING so we are gonna wait."); justWait(facility,
		 * execService); proceed = false; break; default: throw new
		 * IllegalArgumentException(
		 * "Unknown Task status. Expected DONE, ERROR, NONE, PLANNED or PROCESSING."
		 * ); } } } // Finally, if we can proceed, we proceed... //
		 * ######################################### if (proceed) {
		 * log.info("   SCHEDULING execService [" + execService.getId() +
		 * "] facility [" + facility.getId() + "] as PLANNED."); Task task = new
		 * Task(); task.setDelay(execService.getDefaultDelay());
		 * task.setExecService(execService); task.setFacility(facility);
		 * task.setRecurrence(execService.getDefaultRecurrence());
		 * task.setSchedule(time); task.setStatus(TaskStatus.PLANNED);
		 * task.setType(getTaskType(execService, facility));
		 * manipulateTasks(execService, facility, task); } else { // If we can
		 * not proceed, we just end here. //
		 * ######################################## //The current
		 * ExecService,Facility pair should be sleeping in SchedulingPool at the
		 * moment... } } else if
		 * (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
		 * log.debug(
		 * "   Well, it is not. ExecService of type GENERATE does not have any dependencies by design, so we schedule it immediately."
		 * ); log.info("   SCHEDULING execService [" + execService.getId() +
		 * "] facility [" + facility.getId() + "] as PLANNED."); Task task = new
		 * Task(); task.setDelay(execService.getDefaultDelay());
		 * task.setExecService(execService); task.setFacility(facility);
		 * task.setRecurrence(execService.getDefaultRecurrence());
		 * task.setSchedule(time); task.setStatus(TaskStatus.PLANNED);
		 * task.setType(getTaskType(execService, facility));
		 * manipulateTasks(execService, facility, task); } else { throw new
		 * IllegalArgumentException
		 * ("Unknown ExecService type. Expected GENERATE or SEND."); } } else {
		 * log.debug("   We do not proceed, we put the [" + execService.getId()
		 * + "] execService to sleep."); } } else { log.debug(
		 * "   The previous status is \"PROCESSING\". We won't do anything."); }
		 * } else { log.debug("   Yes, the execService ID:" +
		 * execService.getId() + " is denied on facility ID:" + facility.getId()
		 * + "?"); } } else { log.debug("   No, execService ID:" +
		 * execService.getId() + " is not enabled globally."); }
		 */
	}

	@Override
	@Deprecated
	public void propagateServices(
			Pair<List<ExecService>, Facility> servicesFacility)
			throws InternalErrorException {
		for (ExecService execService : servicesFacility.getLeft()) {
			propagateService(execService, new Date(System.currentTimeMillis()),
					servicesFacility.getRight());
		}
	}

	/*
	 * @Transactional private void manipulateTasks(ExecService execService,
	 * Facility facility, Task task) { Task storedTask =
	 * taskManager.getTask(execService, facility,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
	 * 
	 * if (storedTask == null) { try { log.debug("Scheduling a new Task:" +
	 * task.toString()); taskManager.scheduleNewTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
	 * catch (InternalErrorException e) { log.error(e.toString(), e); } } else {
	 * // Clean-up old task results // TODO: Is this simple clean-up really OK?
	 * taskResultDao.clearByTask(storedTask.getId()); // Update task
	 * task.setId(storedTask.getId()); log.debug("Updating an old Task:" +
	 * task.toString()); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); } }
	 */

	private void scheduleItAndWait(ExecService dependency, Facility facility,
			ExecService execService) throws InternalErrorException {
		// We are gonna propagate this dependency...
		propagateService(dependency, new Date(System.currentTimeMillis()),
				facility);
		// ...and wait...
		log.debug("   ExecService to be put to sleep in schedulingPool: execService ["
				+ execService.getId() + "]");
		schedulingPool.addToPool(new Pair<ExecService, Facility>(execService,
				facility));
	}

	private void justWait(Facility facility, ExecService execService) {
		// we are gonna wait...
		log.debug("   ExecService to be put to sleep in schedulingPool: execService ["
				+ execService.getId() + "]");
		schedulingPool.addToPool(new Pair<ExecService, Facility>(execService,
				facility));
	}

	/*
	 * public TaskManager getTaskManager() { return taskManager; }
	 * 
	 * public void setTaskManager(TaskManager taskManager) { this.taskManager =
	 * taskManager; }
	 */
	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public DependenciesResolver getDependenciesResolver() {
		return dependenciesResolver;
	}

	public void setDependenciesResolver(
			DependenciesResolver dependenciesResolver) {
		this.dependenciesResolver = dependenciesResolver;
	}


}
