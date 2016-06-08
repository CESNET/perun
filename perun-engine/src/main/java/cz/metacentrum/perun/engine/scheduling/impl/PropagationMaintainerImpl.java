package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.Date;
import java.util.List;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.model.Statistics;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.scheduling.PropagationMaintainer;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
//import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

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
	private JMSQueueManager jmsQueueManager;
	@Autowired
	private TaskStatusManager taskStatusManager;
	@Autowired
	private ThreadPoolTaskScheduler scheduler;

	/**
	 * TODO: Improve logic here: i.e.: stuck ExecutorEngine threads vs. Time-Out
	 * etc...
	 */
	@Override
	public void checkResults() {

		// checkProcessingTasks();

		log.info("Going to check propagation status for " + schedulingPool.getSize() + " tasks");

		endStuckTasks();

		checkFinishedTasks();

		// M.V. - not rescheduling ERROR tasks in engine, leave it to dispatcher
		// rescheduleErrorTasks();

		// rescheduleOldDoneTasks();

	}

	/*
	 * private void checkProcessingTasks() {
	 * log.info("Gonna list tasks in PROCESSING...");
	 * 
	 * for(Task task: schedulingPool.getProcessingTasks()) {
	 * if(task.getExecService
	 * ().getExecServiceType().equals(ExecService.ExecServiceType.GENERATE))
	 * continue; log.info("Gonna check results for Task ID:" + task.getId());
	 * 
	 * }
	 * 
	 * for (Task task : taskManager.listAllTasksInState(TaskStatus.PROCESSING,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")))) {
	 * //skip GEN tasks
	 * if(task.getExecService().getExecServiceType().equals(ExecService
	 * .ExecServiceType.GENERATE)) continue;
	 * log.info("Gonna check results for Task ID:" + task.getId());
	 * 
	 * List<TaskResult> taskResults =
	 * taskResultDao.getTaskResultsByTask(task.getId());
	 * 
	 * List<Destination> destinations = null; try { destinations =
	 * Rpc.ServicesManager.getDestinations(engineManager.getRpcCaller(),
	 * task.getExecService().getService(), task.getFacility()); }
	 * catch(InternalErrorException ex) {
	 * log.error("Can't get destinations. Switching task to ERROR. Cause: {}",
	 * ex); task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
	 * catch(PrivilegeException ex) {
	 * log.error("Can't get destinations. Switching task to ERROR. Cause: {}",
	 * ex); task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
	 * catch(ServiceNotExistsException ex) {
	 * log.error("Service for the task no longer exists. Removing task", ex);
	 * taskManager.removeTask(task.getId(),
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
	 * catch(FacilityNotExistsException ex) {
	 * log.error("Facility for the task no longer exists. Removing task", ex);
	 * taskManager.removeTask(task.getId(),
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
	 * 
	 * switch(task.getType()) {
	 * 
	 * case SERIAL: collectSerialTaskResults(task, taskResults, destinations);
	 * break;
	 * 
	 * case PARALLEL: collectParallelTaskResults(task, taskResults,
	 * destinations); break;
	 * 
	 * default: log.error("Unknown task type. Assuming parallel.");
	 * collectParallelTaskResults(task, taskResults, destinations); break; } } }
	 * 
	 * 
	 * private void collectSerialTaskResults(Task task, List<TaskResult>
	 * taskResults, List<Destination> destinations) { if (taskResults.size() <=
	 * destinations.size()) { // Let's check whether they are all DONE or not...
	 * int amountDone = 0; int amountDenied = 0; int amountError = 0; int
	 * amountFatalError = 0; for (TaskResult taskResult : taskResults) { switch
	 * (taskResult.getStatus()) { case DONE: amountDone++; break; case DENIED:
	 * amountDenied++; break; case ERROR: amountError++; break; case
	 * FATAL_ERROR: amountFatalError++; break; default: throw new
	 * IllegalArgumentException("WTF?! " + taskResult.getStatus().toString()); }
	 * }
	 * 
	 * if (amountDone > 0) { // Super, at least one task is DONE.
	 * log.info("Task ID " + task.getId() +
	 * " has one Tasks_result DONE, so we set it as DONE.");
	 * task.setStatus(TaskStatus.DONE); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
	 * 
	 * //Set its GENERATE dependencies as dirty //TODO: Hmm...what to do in case
	 * of exceptions?
	 * 
	 * try { log.info("I am going to set all ExecService " +
	 * task.getExecServiceId() + " dependencies (the GENERATE ones) to NONE.");
	 * setAllGenerateDependenciesToNone
	 * (dependenciesResolver.listDependencies(task.getExecServiceId()),
	 * task.getFacilityId()); } catch (ServiceNotExistsException e) {
	 * log.error(e.toString(), e); } catch (InternalErrorException e) {
	 * log.error(e.toString(), e); } catch (PrivilegeException e) {
	 * log.error(e.toString(), e); } } else { //TODO Now FATAL_ERROR and ERROR
	 * are being treated exactly the same. Is FATAL_ERROR really necessary? //
	 * Not DONE yet, are there any destinations left? if (taskResults.size() ==
	 * destinations.size()) { // Well, we ended in ERROR... log.info(
	 * "There has been no DONE state Tasks_results, so I am going to set the Task ID"
	 * + task.getId() + " to ERROR."); task.setStatus(TaskStatus.ERROR);
	 * task.setEndTime(new Date(System.currentTimeMillis()));
	 * taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); //Set
	 * its GENERATE dependencies as dirty //TODO: Hmm...what to do in case of
	 * exceptions?
	 * 
	 * try {
	 * setAllGenerateDependenciesToNone(dependenciesResolver.listDependencies
	 * (task.getExecServiceId()), task.getFacilityId()); } catch
	 * (ServiceNotExistsException e) { log.error(e.toString(), e); } catch
	 * (InternalErrorException e) { log.error(e.toString(), e); } catch
	 * (PrivilegeException e) { log.error(e.toString(), e); } } else { // There
	 * are some destinations left to try, schedule it back
	 * task.setStatus(TaskStatus.PLANNED); task.setSchedule(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); } } }
	 * else if (taskResults.size() > destinations.size()) { log.error(
	 * "There are more Task_results then destinations. so I am going to set the Task ID"
	 * + task.getId() + " to ERROR."); task.setStatus(TaskStatus.ERROR);
	 * task.setEndTime(new Date(System.currentTimeMillis()));
	 * taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); //Set
	 * its GENERATE dependencies as dirty //TODO: Hmm...what to do in case of
	 * exceptions? try {
	 * setAllGenerateDependenciesToNone(dependenciesResolver.listDependencies
	 * (task.getExecServiceId()), task.getFacilityId()); } catch
	 * (ServiceNotExistsException e) { log.error(e.toString(), e); } catch
	 * (InternalErrorException e) { log.error(e.toString(), e); } catch
	 * (PrivilegeException e) { log.error(e.toString(), e); } }
	 * 
	 * if(false) { final long THREE_HOUR = 1000 * 60 * 60 * 3; long
	 * timeDifference = System.currentTimeMillis() -
	 * task.getStartTime().getTime(); if(timeDifference > THREE_HOUR) { // //
	 * WARNING!! // // This can be dangerous. We are not sure if there isn't any
	 * slave script running for this task. // log.error("There are only " +
	 * taskResults.size() + " Task_results for Task ID" + task.getId() +
	 * ", but task is in processing too long, so switch task to ERROR");
	 * task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); //Set
	 * its GENERATE dependencies as dirty //TODO: Hmm...what to do in case of
	 * exceptions? try {
	 * setAllGenerateDependenciesToNone(dependenciesResolver.listDependencies
	 * (task.getExecServiceId()), task.getFacilityId()); } catch
	 * (ServiceNotExistsException e) { log.error(e.toString(), e); } catch
	 * (InternalErrorException e) { log.error(e.toString(), e); } catch
	 * (PrivilegeException e) { log.error(e.toString(), e); } }
	 * 
	 * log.info("There are only " + taskResults.size() +
	 * " Task_results for Task ID" + task.getId() +
	 * ", so we ain't gonna do anything."); // Well, we ain't gonna do anything
	 * bro... // TODO: Time out... } }
	 * 
	 * private void collectParallelTaskResults(Task task, List<TaskResult>
	 * taskResults, List<Destination> destinations) { // Do we have the same
	 * number of Destinations as we have TaskResults? if (taskResults.size() ==
	 * destinations.size()) { // Let's check whether they are all DONE or not...
	 * int amountDone = 0; int amountDenied = 0; int amountError = 0; int
	 * amountFatalError = 0; for (TaskResult taskResult : taskResults) { switch
	 * (taskResult.getStatus()) { case DONE: amountDone++; break; case DENIED:
	 * amountDenied++; break; case ERROR: amountError++; break; case
	 * FATAL_ERROR: amountFatalError++; break; default: throw new
	 * IllegalArgumentException("WTF?! " + taskResult.getStatus().toString()); }
	 * }
	 * 
	 * if (amountDone + amountDenied == taskResults.size()) { // Super, all is
	 * DONE or we don't care (DENIED) :-) log.info("Task ID " + task.getId() +
	 * " has all Tasks_results either DONE or DENIED, so we set it as DONE.");
	 * task.setStatus(TaskStatus.DONE); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
	 * 
	 * //Set its GENERATE dependencies as dirty //TODO: Hmm...what to do in case
	 * of exceptions? try { log.info("I am going to set all ExecService " +
	 * task.getExecServiceId() + " dependencies (the GENERATE ones) to NONE.");
	 * 
	 * setAllGenerateDependenciesToNone(dependenciesResolver.listDependencies(task
	 * .getExecServiceId()), task.getFacilityId()); } catch
	 * (ServiceNotExistsException e) { log.error(e.toString(), e); } catch
	 * (InternalErrorException e) { log.error(e.toString(), e); } catch
	 * (PrivilegeException e) { log.error(e.toString(), e); } } else { final
	 * long TWO_HOUR = 1000 * 60 * 60 * 2; long timeDifference =
	 * System.currentTimeMillis() - task.getStartTime().getTime();
	 * if(timeDifference > TWO_HOUR) { // // WARNING!! // // This can be
	 * dangerous. We are not sure if there isn't any slave script running for
	 * this task. // log.error("There are only " + taskResults.size() +
	 * " Task_results for Task ID" + task.getId() +
	 * ", but task is in processing too long, so switch task to ERROR");
	 * task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); //Set
	 * its GENERATE dependencies as dirty //TODO: Hmm...what to do in case of
	 * exceptions? try {
	 * setAllGenerateDependenciesToNone(dependenciesResolver.listDependencies
	 * (task.getExecServiceId()), task.getFacilityId()); } catch
	 * (ServiceNotExistsException e) { log.error(e.toString(), e); } catch
	 * (InternalErrorException e) { log.error(e.toString(), e); } catch
	 * (PrivilegeException e) { log.error(e.toString(), e); } } } } else if
	 * (taskResults.size() > destinations.size()) { log.error(
	 * "There are more Task_results then destinations. so I am going to set the Task ID"
	 * + task.getId() + " to ERROR."); task.setStatus(TaskStatus.ERROR);
	 * task.setEndTime(new Date(System.currentTimeMillis()));
	 * taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); //Set
	 * its GENERATE dependencies as dirty //TODO: Hmm...what to do in case of
	 * exceptions? try {
	 * setAllGenerateDependenciesToNone(dependenciesResolver.listDependencies
	 * (task.getExecServiceId()), task.getFacilityId()); } catch
	 * (ServiceNotExistsException e) { log.error(e.toString(), e); } catch
	 * (InternalErrorException e) { log.error(e.toString(), e); } catch
	 * (PrivilegeException e) { log.error(e.toString(), e); } } else { final
	 * long THREE_HOUR = 1000 * 60 * 60 * 3; long timeDifference =
	 * System.currentTimeMillis() - task.getStartTime().getTime();
	 * if(timeDifference > THREE_HOUR) { // // WARNING!! // // This can be
	 * dangerous. We are not sure if there isn't any slave script running for
	 * this task. // log.error("There are only " + taskResults.size() +
	 * " Task_results for Task ID" + task.getId() +
	 * ", but task is in processing too long, so switch task to ERROR");
	 * task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); //Set
	 * its GENERATE dependencies as dirty //TODO: Hmm...what to do in case of
	 * exceptions? try {
	 * setAllGenerateDependenciesToNone(dependenciesResolver.listDependencies
	 * (task.getExecServiceId()), task.getFacilityId()); } catch
	 * (ServiceNotExistsException e) { log.error(e.toString(), e); } catch
	 * (InternalErrorException e) { log.error(e.toString(), e); } catch
	 * (PrivilegeException e) { log.error(e.toString(), e); } }
	 * 
	 * log.info("There are only " + taskResults.size() +
	 * " Task_results for Task ID" + task.getId() +
	 * ", so we ain't gonna do anything."); // Well, we ain't gonna do anything
	 * bro... // TODO: Time out... } }
	 */

	private void checkFinishedTasks() {
		// report finished tasks back to scheduler
		// clear all tasks we are done with (ie. DONE, ERROR with no recurrence
		// left)
		List<Task> tasklist = schedulingPool.getDoneTasks();
		log.debug("There are {} DONE tasks", tasklist.size());
		for (Task task : tasklist) {
			log.debug("TASK " + task.toString() + " finished");
			try {
				log.debug("TASK reported as finished at "
						+ System.currentTimeMillis());
				jmsQueueManager.reportFinishedTask(task, "Destinations []");
				schedulingPool.removeTask(task);
				log.debug("TASK {} removed from database.", task.getId());
			} catch (JMSException e) {
				log.error("Failed to report finished task " + task.toString()
						+ ": " + e.getMessage());
			}
		}

		tasklist = schedulingPool.getErrorTasks();
		log.debug("There are {} ERROR tasks", tasklist.size());
		for (Task task : tasklist) {

			if (task.getEndTime() == null) {
				log.error("RECOVERY FROM INCONSISTENT STATE: ERROR task does not have end_time! Setting end_time to task.getDelay + 1.");
				// getDelay is in minutes, therefore we multiply it with 60*1000
				Date endTime = new Date(System.currentTimeMillis()
						- ((task.getDelay() + 1) * 60000));
				task.setEndTime(endTime);
			}

			List<Destination> destinations = taskStatusManager.getTaskStatus(
					task).getSuccessfulDestinations();
			List<Destination> failedDestinations = task.getDestinations();
			failedDestinations.removeAll(destinations);

			StringBuilder destinations_s = new StringBuilder("Destinations [");
			if (!failedDestinations.isEmpty()) {
				destinations_s.append(failedDestinations.remove(0)
						.serializeToString());
				for (Destination destination : failedDestinations) {
					destinations_s.append(",");
					destinations_s.append(destination.serializeToString());
				}
			}
			destinations_s.append("]");

			log.debug("TASK " + task.toString()
					+ " finished in error, remaining destinations: "
					+ destinations_s);
			try {
				jmsQueueManager.reportFinishedTask(task,
						destinations_s.toString());
				schedulingPool.removeTask(task);
				log.debug("TASK {} removed from database.", task.getId());
			} catch (JMSException e) {
				log.error("Failed to report finished task " + task.toString()
						+ ": " + e.getMessage());
			}
		}
	}


	@Deprecated
	private void rescheduleErrorTasks() {
		// log.info("I am gonna list tasks in ERROR and reschedule if necessary...");

		for (Task task : schedulingPool.getErrorTasks()) {
			log.debug("TASK " + task.toString() + " finished in error");
			if (task.getEndTime() == null) {
				log.error("RECOVERY FROM INCONSISTENT STATE: ERROR task does not have end_time! Setting end_time to task.getDelay + 1.");
				// getDelay is in minutes, therefore we multiply it with 60*1000
				Date endTime = new Date(System.currentTimeMillis()
						- ((task.getDelay() + 1) * 60000));
				task.setEndTime(endTime);
			}
			int howManyMinutesAgo = (int) (System.currentTimeMillis() - task
					.getEndTime().getTime()) / 1000 / 60;
			log.info("TASK [" + task + "] in ERROR state completed "
					+ howManyMinutesAgo + " minutes ago.");
			// check and set recurrence
			int recurrence = task.getRecurrence() - 1;
			if (recurrence < 0) {
				// no more retries, sorry
				log.info("TASK [ " + task
						+ "] in ERROR state has no more retries, bailing out.");
				schedulingPool.removeTask(task);
				log.debug("TASK {} removed from database.", task.getId());
				continue;
			}
			// If DELAY time has passed, we reschedule...
			if (howManyMinutesAgo >= task.getDelay()) {
				try {
					task.setRecurrence(recurrence);
					ExecService execService = task.getExecService();
					Facility facility = task.getFacility();
					log.info("TASK [	"
							+ task
							+ "] in ERROR state is going to be rescheduled: taskScheduler.propagateService(execService:ID "
							+ execService.getId()
							+ ", new Date(System.currentTimeMillis()), facility:ID "
							+ facility.getId() + ");");
					taskScheduler.propagateService(task,
							new Date(System.currentTimeMillis()));
					log.info("TASK [" + task
							+ "] in ERROR state has been rescheduled.");
					
					// Also (to be sure) reschedule all Tasks that depend on
					// this Task
					//
					// While engine starts in state GEN = ERROR, SEND = DONE
					// => GEN will be rescheduled but without this SEND will
					// never be propagated
					List<Task> dependentTasks = dependenciesResolver
							.getDependants(task);
					if (dependentTasks != null) {
						for (Task dependantTask : dependentTasks) {
							taskScheduler.propagateService(dependantTask,
									new Date(System.currentTimeMillis()));
									log.info(
										"{} was rescheduled because it depends on {}",
										dependantTask, task);
						}
					}
				} catch (InternalErrorException e) {
					log.error("{}", e);
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
		List<Task> suspiciousTasks = schedulingPool.getProcessingTasks();

		log.debug("There are {} PROCESSING tasks", suspiciousTasks.size());
		
		suspiciousTasks.addAll(schedulingPool.getPlannedTasks());

		log.debug("There are {} tasks that are PLANNED or PROCESSING", suspiciousTasks.size());

		for (Task task : suspiciousTasks) {
			log.debug("checking task " + task.toString()
					+ " for staying around too long...");
			// count how many minutes the task stays in one state - if the state
			// is PLANNED count it from when it was scheduled ; if it is
			// PROCESSING count it from when it started
			Date checkDate = task.getStatus().equals(TaskStatus.PLANNED) ? task
					.getSchedule() : task.getStartTime();
			if (checkDate == null) {
				log.error(
						"ERROR: task in state {} has no corresponding timestamp",
						task.getStatus());
				checkDate = new Date(System.currentTimeMillis());
				if (task.getStatus().equals(TaskStatus.PLANNED)) {
					task.setSchedule(checkDate);
				} else {
					task.setStartTime(checkDate);
				}
			}
			Date ended = task.getEndTime();
			TaskStatus status = task.getStatus();
			if(ended != null ||
					status.equals(TaskStatus.DONE) ||
					status.equals(TaskStatus.ERROR)) {
				log.error("ERROR: Task presumably in PLANNED or PROCESSING state, but appears to have ended.");
				cz.metacentrum.perun.engine.scheduling.TaskStatus taskStatus = taskStatusManager.getTaskStatus(task);
				if (taskStatus.isTaskFinished()) {
					schedulingPool.setTaskStatus(task, taskStatus.getTaskStatus());
					log.debug("TASK " + task.getId() + " status set to DONE");
				} else {
					// there is something deeply wrong...
					log.error("ERROR: Task is weird. Switching it to ERROR. {}",
							task);
					task.setEndTime(new Date(System.currentTimeMillis()));
					schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
				}

			}
			
			int howManyMinutesAgo = (int) (System.currentTimeMillis() - checkDate
					.getTime()) / 1000 / 60;

			// If too much time has passed something is broken
			if (howManyMinutesAgo >= 180) {
				log.error(
						"ERROR: Task is stuck in PLANNED or PROCESSING state. Switching it to ERROR. {}",
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
		 * (howManyMinutesAgo >= 180) { log.error(
		 * "ERROR: Task is stucked in PLANNED or PROCESSING state. Switching it to ERROR. {}"
		 * , task); task.setEndTime(new Date(System.currentTimeMillis()));
		 * task.setStatus(TaskStatus.ERROR); taskManager.updateTask(task,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); }
		 * }
		 */
	}

	private void rescheduleOldDoneTasks() {
		// Reschedule SEND tasks in DONE that haven't been running for quite a
		// while
		List<Task> donetasks = schedulingPool.getDoneTasks();

		log.debug("There are {} completed tasks", donetasks.size());

		for (Task task : donetasks) {
			// skip GEN tasks
			if (task.getExecService().getExecServiceType()
					.equals(ExecService.ExecServiceType.GENERATE))
				continue;

			Date twoDaysAgo = new Date(System.currentTimeMillis() - 1000 * 60
					* 24 * 2);
			if (task.getEndTime().before(twoDaysAgo)) {
				// reschedule the task
				try {
					taskScheduler.propagateService(task,
							new Date(System.currentTimeMillis()));
					log.info("TASK ["
							+ task
							+ "] wasn't propagated for more then 2 days. Going to schedule it for propagation now.");
				} catch (InternalErrorException e) {
					log.error(
							"Rescheduling of task which wasn't propagated for more than 2 days failed. {}, Exception: {}",
							task, e);
				}
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

	@Override
	public Statistics getStatistics() {
		throw new UnsupportedOperationException("Nah...");
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

	public JMSQueueManager getJmsQueueManager() {
		return jmsQueueManager;
	}

	public void setJmsQueueManager(JMSQueueManager jmsQueueManager) {
		this.jmsQueueManager = jmsQueueManager;
	}

}
