package cz.metacentrum.perun.dispatcher.scheduling.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;

import cz.metacentrum.perun.core.api.PerunClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.scheduling.DependenciesResolver;
import cz.metacentrum.perun.dispatcher.scheduling.PropagationMaintainer;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;
//import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.ResultManager;
import cz.metacentrum.perun.taskslib.service.TaskManager;

@org.springframework.stereotype.Service(value = "propagationMaintainer")
public class PropagationMaintainerImpl implements PropagationMaintainer {
	private final static Logger log = LoggerFactory
			.getLogger(PropagationMaintainerImpl.class);

	/*
	 * @Autowired private TaskManager taskManager;
	 * 
	 * @Autowired private ResultManager resultManager;
	 * 
	 * @Autowired private TaskResultDao taskResultDao;
	 * 
	 * @Autowired private EngineManager engineManager;
	 */
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private DependenciesResolver dependenciesResolver;
	@Autowired
	private TaskScheduler taskScheduler;
	@Autowired
	private Perun perun;
	@Autowired
	private ResultManager resultManager;
	/*
	 * @Autowired private GeneralServiceManager generalServiceManager;
	 */
	@Autowired
	private Properties dispatcherPropertiesBean;
	private PerunSession perunSession;

	/**
	 * TODO: Improve logic here: i.e.: stuck ExecutorEngine threads vs. Time-Out
	 * etc...
	 */
	@Override
	public void checkResults() {

		try {
			perunSession = perun
					.getPerunSession(new PerunPrincipal(
							dispatcherPropertiesBean.getProperty("perun.principal.name"),
							dispatcherPropertiesBean
									.getProperty("perun.principal.extSourceName"),
							dispatcherPropertiesBean
									.getProperty("perun.principal.extSourceType")),
							new PerunClient());
		} catch (InternalErrorException e1) {
			// TODO Auto-generated catch block
			log.error(
					"Error establishing perun session to check tasks propagation status: ",
					e1);
			return;
		}

		checkFinishedTasks();

		rescheduleErrorTasks();

		endStuckTasks();

		rescheduleOldDoneTasks();

	}

	private void checkFinishedTasks() {
		/*  no need to spam the log file
		 * 	
		for (Task task : schedulingPool.getDoneTasks()) {
			log.debug("Task " + task.toString() + " is done.");
		}
		 */
	}

	private void rescheduleErrorTasks() {
		log.info("I am gonna list tasks in ERROR and reschedule if necessary...");

		for (Task task : schedulingPool.getErrorTasks()) {
			if (task.getEndTime() == null) {
				log.error("RECOVERY FROM INCONSISTENT STATE: ERROR task does not have end_time! Setting end_time to task.getDelay + 1.");
				// getDelay is in minutes, therefore we multiply it with 60*1000
				Date endTime = new Date(System.currentTimeMillis()
						- ((task.getDelay() + 1) * 60000));
				task.setEndTime(endTime);
			}
			int howManyMinutesAgo = (int) (System.currentTimeMillis() - task
					.getEndTime().getTime()) / 1000 / 60;
			if(howManyMinutesAgo < 0) {
				log.error("RECOVERY FROM INCONSISTENT STATE: ERROR task appears to have ended in future.");
				Date endTime = new Date(System.currentTimeMillis()
						- ((task.getDelay() + 1) * 60000));
				task.setEndTime(endTime);
				howManyMinutesAgo = task.getDelay() + 1;
			}
			log.info("TASK [" + task + "] in ERROR state completed "
					+ howManyMinutesAgo + " minutes ago.");
			// XXX - apparently this is not what the authors had in mind,
			// commented out
			// check and set recurrence
			// int recurrence = task.getRecurrence() - 1;
			// if(recurrence < 0) {
			// // no more retries, sorry
			// log.info("TASK [ " + task +
			// "] in ERROR state has no more retries, bailing out.");
			// continue;
			// }
			// task.setRecurrence(recurrence);
			// If DELAY time has passed, we reschedule...
			int recurrence = task.getRecurrence() + 1;
			if(recurrence > task.getExecService().getDefaultRecurrence() &&
					howManyMinutesAgo < 60 * 12 &&
					!task.isSourceUpdated()) {
				log.info("TASK [ " + task + "] in ERROR state has no more retries, bailing out.");
			} else if (howManyMinutesAgo >= recurrence * task.getDelay() ||
					task.isSourceUpdated()) {
				// check if service is still assigned on facility
				try {
					List<Service> assignedServices = perun.getServicesManager().getAssignedServices(perunSession, task.getFacility());
					if (assignedServices.contains(task.getExecService().getService())) {
						ExecService execService = task.getExecService();
						Facility facility = task.getFacility();
						if(recurrence > execService.getDefaultRecurrence()) {
							// this ERROR task is rescheduled for being here too long
							task.setRecurrence(0);
							task.setDestinations(null);
							log.info("TASK id " + task.getId() + " is in ERROR state long enough, ");
						}
						task.setRecurrence(recurrence);
						log.info("TASK ["
								+ task
								+ "] in ERROR state is going to be rescheduled: taskScheduler.propagateService(execService:ID "
								+ execService.getId()
								+ ", new Date(System.currentTimeMillis()), facility:ID "
								+ facility.getId() + ");");
						// taskScheduler.propagateService(task, new
						// Date(System.currentTimeMillis()));
						taskScheduler.scheduleTask(task);
						log.info("TASK [" + task
								+ "] in ERROR state has been rescheduled.");

						// Also (to be sure) reschedule all Tasks that depend on
						// this Task
						//
						// While engine starts in state GEN = ERROR, SEND = DONE
						// => GEN will be rescheduled but without this SEND will
						// never be propagated
						List<ExecService> dependantServices = dependenciesResolver.listDependantServices(execService);
						for (ExecService dependantService : dependantServices) {
							Task dependantTask = schedulingPool.getTask(dependantService, facility);
							if (dependantTask == null) {
								dependantTask = new Task();
								dependantTask.setExecService(dependantService);
								dependantTask.setFacility(facility);
								dependantTask.setRecurrence(dependantService.getDefaultRecurrence());
								schedulingPool.addToPool(dependantTask,	schedulingPool.getQueueForTask(task));
								taskScheduler.scheduleTask(dependantTask);
								log.info("{} was rescheduled because it depends on {}",
										dependantTask, task);
							}
						}
					} else {
						// delete this tasks (SEND and GEN) because service is
						// no longer assigned to facility
						schedulingPool.removeTask(task);
						log.warn(
								"Removed TASK {} from database, beacuse service is no longer assigned to this facility.",
								task.toString());
					}
				} catch (FacilityNotExistsException e) {
					schedulingPool.removeTask(task);
					log.error("Removed TASK {} from database, facility no longer exists.",
							task.getId());
				
				} catch (InternalErrorException e) {
					log.error("{}", e);
				} catch (PrivilegeException e) {
					log.error("Consistency error. {}", e);
				}
			}
		}

		/*
		 * Original implementation:
		 * 
		 * //TODO: Take into account Recurrence! for (Task task :
		 * taskManager.listAllTasksInState(TaskStatus.ERROR,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")))) {
		 * if (task.getEndTime() == null) { log.error(
		 * "RECOVERY FROM INCONSISTATE STATE: ERROR task does not have end_time! Setting end_time to task.getDelay + 1."
		 * ); // getDelay is in minutes, therefore we multiply it with 60*1000
		 * Date endTime = new Date(System.currentTimeMillis() -
		 * ((task.getDelay() + 1) * 60000)); task.setEndTime(endTime);
		 * taskManager.updateTask(task,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
		 * int howManyMinutesAgo = (int) (System.currentTimeMillis() -
		 * task.getEndTime().getTime()) / 1000 / 60; log.info("TASK [" + task +
		 * "] in ERROR state completed " + howManyMinutesAgo + " minutes ago.");
		 * //If DELAY time has passed, we reschedule... if (howManyMinutesAgo >=
		 * task.getDelay()) { //check if service is still assigned on facility
		 * try { List<Service> assignedServices =
		 * Rpc.ServicesManager.getAssignedServices(engineManager.getRpcCaller(),
		 * task.getFacility());
		 * if(assignedServices.contains(task.getExecService().getService())) {
		 * try { taskManager.updateTask(task,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
		 * ExecService execService = task.getExecService(); Facility facility =
		 * task.getFacility(); log.info("TASK [" + task +
		 * "] in ERROR state is going to be rescheduled: taskScheduler.propagateService(execService:ID "
		 * + execService.getId() +
		 * ", new Date(System.currentTimeMillis()), facility:ID " +
		 * facility.getId() + ");"); taskScheduler.propagateService(execService,
		 * new Date(System.currentTimeMillis()), facility); log.info("TASK [" +
		 * task + "] in ERROR state has been rescheduled.");
		 * 
		 * //Also (to be sure) reschedule all execServices which depends on this
		 * exec service // //While engine starts in state GEN = ERROR, SEND =
		 * DONE => GEN will be rescheduled but without this SEND will never be
		 * propagated List<ExecService> dependentExecServices =
		 * Rpc.GeneralServiceManager
		 * .listExecServicesDependingOn(engineManager.getRpcCaller(),
		 * execService); if(dependentExecServices != null) { for(ExecService
		 * dependantExecService : dependentExecServices) {
		 * taskScheduler.propagateService(dependantExecService, new
		 * Date(System.currentTimeMillis()), facility);
		 * log.info("{} was rescheduled because it depends on {}",
		 * dependantExecService, execService); } }
		 * 
		 * } catch (InternalErrorException e) { log.error(e.toString(), e); } }
		 * else { //delete this tasks (SEND and GEN) because service is no
		 * longer assigned to facility List<ExecService> execServicesGenAndSend
		 * =
		 * Rpc.GeneralServiceManager.listExecServices(engineManager.getRpcCaller
		 * (), task.getExecService().getService().getId()); for(ExecService
		 * execService : execServicesGenAndSend) { Task taskToDelete =
		 * taskManager.getTask(execService, task.getFacility(),
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
		 * if(taskToDelete!= null) {
		 * resultManager.clearByTask(taskToDelete.getId(),
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
		 * taskManager.removeTask(taskToDelete.getId(),
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
		 * } } } catch(PrivilegeException ex) {
		 * log.error("Consistency error. {}", ex); }
		 * catch(FacilityNotExistsException ex) {
		 * log.error("Consistency error - found task for non-existing facility. {}"
		 * , ex); } catch(ServiceNotExistsException ex) {
		 * log.error("Consistency error - found task for non-existing service. {}"
		 * , ex); } catch(InternalErrorException ex) { log.error("{}", ex); } }
		 * }
		 */
	}

	private void endStuckTasks() {
		// list all tasks in processing and planned and check if any have beeen
		// running for too long.
		log.info("I am gonna list planned and processing tasks and kill them if necessary...");

		List<Task> suspiciousTasks = schedulingPool.getProcessingTasks();
		suspiciousTasks.addAll(schedulingPool.getPlannedTasks());

		for (Task task : suspiciousTasks) {
			// count how many minutes the task stays in one state - if the state
			// is PLANNED count it from when it was scheduled ; if it is
			// PROCESSING count it from when it started
			Date started = task.getStartTime();
			Date scheduled = task.getSchedule();
			TaskStatus status = task.getStatus();

			if (status == null) {
				log.error("ERROR: Task presumably in PLANNED or PROCESSING state, but does not have a valid status. Switching to ERROR. {}",
						task);
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				continue;
			}

			if (started == null && scheduled == null) {
				log.error("ERROR: Task presumably in PLANNED or PROCESSING state, but does not have a valid scheduled or started time. Switching to ERROR. {}",
						task);
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				continue;
			}

			int howManyMinutesAgo = (int) (System.currentTimeMillis() - (started == null ? scheduled
					: started).getTime()) / 1000 / 60;

			// If too much time has passed something is broken
			if (howManyMinutesAgo >= 60) {
				log.error("ERROR: Task is stuck in PLANNED or PROCESSING state. Switching it to ERROR. {}",
						task);
				task.setEndTime(new Date(System.currentTimeMillis()));
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
			}

		}
		/*
		 * 
		 * List<Task> suspiciousTasks =
		 * taskManager.listAllTasksInState(TaskStatus.PROCESSING,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
		 * suspiciousTasks
		 * .addAll(taskManager.listAllTasksInState(TaskStatus.PLANNED,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))));
		 * for (Task task : suspiciousTasks) { //count how many minutes the task
		 * stays in one state - if the state is PLANNED count it from when it
		 * was scheduled ; if it is PROCESSING count it from when it started int
		 * howManyMinutesAgo = (int) (System.currentTimeMillis() - (
		 * task.getStatus().equals(TaskStatus.PLANNED) ? task.getSchedule() :
		 * task.getStartTime() ).getTime()) / 1000 / 60;
		 * 
		 * //If too much time has passed something is broken if
		 * (howManyMinutesAgo >= 60) { log.error(
		 * "ERROR: Task is stuck in PLANNED or PROCESSING state. Switching it to ERROR. {}"
		 * , task); task.setEndTime(new Date(System.currentTimeMillis()));
		 * task.setStatus(TaskStatus.ERROR); taskManager.updateTask(task,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
		 * }
		 */
	}

	private void rescheduleOldDoneTasks() {
		// Reschedule SEND tasks in DONE that haven't been running for quite a
		// while
		log.info("I am gonna list complete tasks and reschedule if they are too old...");

		for (Task task : schedulingPool.getDoneTasks()) {
			// skip GEN tasks
			if (task.getExecService() != null && 
			    task.getExecService().getExecServiceType().equals(ExecService.ExecServiceType.GENERATE)) {
				log.debug(
						"Found finished GEN TASK {} that was not running for a while, leaving it as is.",
						task.toString());
				continue;
			}

			Date twoDaysAgo = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2);
			if (task.isSourceUpdated()) {
				// reschedule the task
				log.info("TASK ["
						+ task
						+ "] data changed. Going to schedule for propagation now.");
				taskScheduler.scheduleTask(task);
			} else 	if (task.getEndTime() == null || task.getEndTime().before(twoDaysAgo)) {
				// reschedule the task
				log.info("TASK ["
						+ task
						+ "] wasn't propagated for more then 2 days. Going to schedule it for propagation now.");
				taskScheduler.scheduleTask(task);
			} else {
				log.info("TASK [" + task + "] has finished recently, leaving it for now.");
			}

		}
		/*
		 * 
		 * for(Task task : taskManager.listAllTasksInState(TaskStatus.DONE,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")))) {
		 * //skip GEN tasks
		 * if(task.getExecService().getExecServiceType().equals(
		 * ExecService.ExecServiceType.GENERATE)) continue;
		 * 
		 * Date twoDaysAgo = new Date(System.currentTimeMillis() - 1000 * 60 *
		 * 24 * 2); if(task.getEndTime().before(twoDaysAgo)) { //reschedule the
		 * task try { taskScheduler.propagateService(task.getExecService(), new
		 * Date(System.currentTimeMillis()), task.getFacility());
		 * log.info("TASK [" + task +
		 * "] wasn't propagated for more then 2 days. Going to schedule it for propagation now."
		 * ); } catch (InternalErrorException e) { log.error(
		 * "Rescheduling of task which wasn't propagated for more than 2 days failed. {}, Exception: {}"
		 * , task, e); } }
		 * 
		 * }
		 */
	}

	/*
	 * @Override public Statistics getStatistics() { throw new
	 * UnsupportedOperationException("Nah..."); }
	 */

	private void setAllGenerateDependenciesToNone(Task task) {
		List<ExecService> dependencies = this.dependenciesResolver.listDependencies(task.getExecService());

		for (ExecService dependencyToBeSetDirty : dependencies) {
			if (dependencyToBeSetDirty.getExecServiceType().equals(ExecServiceType.GENERATE)) {
				Task taskToBeSetDirty = schedulingPool.getTask(dependencyToBeSetDirty, task.getFacility());
				if (taskToBeSetDirty != null) {
					log.debug(
							"Setting GEN dependency task {} to NONE state to regenerate data for completed task {}",
							taskToBeSetDirty, task);
					schedulingPool.setTaskStatus(taskToBeSetDirty, TaskStatus.NONE);
					try {
						schedulingPool.setQueueForTask(taskToBeSetDirty, null);
					} catch (InternalErrorException e) {
						log.error("Could not set destination queue for task {}: {}", task.getId(), e.getMessage());
					}
				}
			}
		}
	}

	@Override
	public void setAllGenerateDependenciesToNone(
			List<ExecService> dependencies, Facility facility) {
		setAllGenerateDependenciesToNone(dependencies, facility.getId());
	}

	@Override
	public void setAllGenerateDependenciesToNone(
			List<ExecService> dependencies, int facilityId) {
		// And we set all its GENERATE dependencies as "dirty" by switching them
		// to NONE state.
		// TODO: Optimize this for cycle out with a 1 clever SQL query ???
		// ^ setAllGenerateDependenciesToNone(ExecService execService, Facility
		// facility) ???
		// TODO:ADD TEST CASE!!!
		/*
		 * TODO: rewrite this for (ExecService dependencyToBeSetDirty :
		 * dependencies) { if
		 * (dependencyToBeSetDirty.getExecServiceType().equals
		 * (ExecServiceType.GENERATE)) { Task taskToBeSetDirty =
		 * taskManager.getTask(dependencyToBeSetDirty.getId(), facilityId,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); if
		 * (taskToBeSetDirty != null) {
		 * taskToBeSetDirty.setStatus(TaskStatus.NONE);
		 * taskManager.updateTask(taskToBeSetDirty,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
		 * } }
		 */
	}

	@Override
	public void closeTasksForEngine(int clientID) {
		List<Task> tasks = schedulingPool.getTasksForEngine(clientID);
	
		// switch all processing tasks to error, remove the engine queue association
		log.debug("Switching PROCESSING tasks on engine {} to ERROR, the engine went down", clientID);
		for(Task task: tasks) {
			if(task.getStatus().equals(TaskStatus.PROCESSING)) {
				log.debug("switching task {} to ERROR, the engine it was running on went down", task.getId());
				schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
			}
			try {
				schedulingPool.setQueueForTask(task, null);
			} catch (InternalErrorException e) {
				log.error("Could not remove output queue for task {}: {}", task.getId(), e.getMessage());
			}
		}
	}
	

	@Override
	public void onTaskComplete(int taskId, int clientID, String status_s,
			String string) {
		Task completedTask = schedulingPool.getTaskById(taskId);

		if (completedTask == null) {
			// eh? how would that be possible?
			log.error("TASK id {} reported as complete, but we do not know it... (yet?)", taskId);
			return;
		}

		TaskStatus status = TaskStatus.NONE;
		if (status_s.equals("ERROR")) {
			status = TaskStatus.ERROR;
		} else if (status_s.equals("DONE")) {
			status = TaskStatus.DONE;
		} else {
			log.error("Engine reported unexpected status {} for task id {}, setting to ERROR",
					status_s, taskId);
			status = TaskStatus.ERROR;
		}

		completedTask.setEndTime(new Date(System.currentTimeMillis()));

		// if we are going to run this task again, make sure to generate up to
		// date data
		if (completedTask.getExecService().getExecServiceType().equals(ExecServiceType.SEND)) {
			try {
				schedulingPool.setQueueForTask(completedTask, null);
			} catch (InternalErrorException e) {
				log.error("Could not set destination queue for task {}: {}", completedTask.getId(), e.getMessage());
			}
			this.setAllGenerateDependenciesToNone(completedTask);
		}

		if (status.equals(TaskStatus.DONE)) {
			// task completed successfully
			// set destination list to null to refetch them later
			completedTask.setDestinations(null);
			schedulingPool.setTaskStatus(completedTask, TaskStatus.DONE);
			completedTask.setRecurrence(0);
			log.debug("TASK {} reported as DONE", completedTask.toString());
			// for GEN tasks, signal SENDs that source data are updated
			if(completedTask.getExecService().getExecServiceType().equals(ExecServiceType.GENERATE)) {
				List<ExecService> dependantServices = dependenciesResolver.listDependantServices(completedTask.getExecService());
				for (ExecService dependantService : dependantServices) {
					Task dependantTask = schedulingPool.getTask(dependantService, completedTask.getFacility());
					if (dependantTask != null && dependantService.getExecServiceType().equals(ExecServiceType.SEND)) {
						dependantTask.setSourceUpdated(false);
					}
					if(completedTask.isPropagationForced() && dependantTask.isPropagationForced()) {
						log.debug("Going to force schedule dependant task " + dependantTask.getId());
						taskScheduler.scheduleTask(dependantTask);
					}
				}
			} 
			completedTask.setPropagationForced(false);
		} else {
			if (string.isEmpty()) {
				// weird - task is in error and no destinations reported as
				// failed...
				log.warn("TASK {} ended in ERROR state with no remaining destinations.",
						completedTask.toString());
			} else {
				// task failed, some destinations remain
				// resolve list of destinations
				List<PerunBean> listOfBeans;
				List<Destination> destinationList = new ArrayList<Destination>();
				try {
					listOfBeans = AuditParser.parseLog(string);
					log.debug("Found list of destination beans: " + listOfBeans);
					for (PerunBean bean : listOfBeans) {
						destinationList.add((Destination) bean);
					}
				} catch (InternalErrorException e) {
					log.error("Could not resolve destination from destination list");
				}
				if(completedTask.getDestinations() != null && 
				   !completedTask.getDestinations().isEmpty()) {
					completedTask.setDestinations(destinationList);
				}
			}
			schedulingPool.setTaskStatus(completedTask, TaskStatus.ERROR);
			log.debug("Task set to ERROR state with remaining destinations: "
					+ completedTask.getDestinations());
		}
	}

	@Override
	public void onTaskDestinationComplete(int clientID, String string) {
		if(string == null || string.isEmpty()) {
			log.error("Could not parse taskresult message from engine " + clientID);
			return;
		}
		
		try {
			List<PerunBean> listOfBeans = AuditParser.parseLog(string);
			if(!listOfBeans.isEmpty()) {
				TaskResult taskResult = (TaskResult)listOfBeans.get(0);
				resultManager.insertNewTaskResult(taskResult, clientID);
			} else {
				log.error("No TaskResult bean found in message {} from engine {}", string, clientID);
			}
		} catch (Exception e) {
			log.error("Could not save taskresult message {} from engine " + clientID, string);
			log.debug("Error storing taskresult message: " + e.getMessage());
		}
	}
	/*
	 * public TaskManager getTaskManager() { return taskManager; }
	 * 
	 * public void setTaskManager(TaskManager taskManager) { this.taskManager =
	 * taskManager; }
	 * 
	 * public TaskResultDao getTaskResultDao() { return taskResultDao; }
	 * 
	 * public void setTaskResultDao(TaskResultDao taskResultDao) {
	 * this.taskResultDao = taskResultDao; }
	 * 
	 * public EngineManager getEngineManager() { return engineManager; }
	 * 
	 * public void setEngineManager(EngineManager engineManager) {
	 * this.engineManager = engineManager; }
	 */

	public DependenciesResolver getDependenciesResolver() {
		return dependenciesResolver;
	}

	public void setDependenciesResolver(
			DependenciesResolver dependenciesResolver) {
		this.dependenciesResolver = dependenciesResolver;
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public Properties getDispatcherPropertiesBean() {
		return dispatcherPropertiesBean;
	}

	public void setDispatcherPropertiesBean(Properties propertiesBean) {
		this.dispatcherPropertiesBean = propertiesBean;
	}

}
