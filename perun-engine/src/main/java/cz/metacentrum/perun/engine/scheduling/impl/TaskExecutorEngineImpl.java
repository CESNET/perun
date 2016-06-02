package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.scheduling.ExecutorEngineWorker;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.engine.scheduling.TaskStatus.TaskDestinationStatus;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "taskExecutorEngine")
/* @Transactional */
public class TaskExecutorEngineImpl implements TaskExecutorEngine {
	private final static Logger log = LoggerFactory
			.getLogger(TaskExecutorEngineImpl.class);

	/*
	 * @Autowired private TaskManager taskManager;
	 * 
	 * @Autowired private TaskResultDao taskResultDao;
	 */
	@Autowired
	private TaskExecutor taskExecutorGenWorkers;
	@Autowired
	private TaskExecutor taskExecutorSendWorkers;
	@Autowired
	private BeanFactory beanFactory;
	@Autowired
	private DependenciesResolver dependencyResolver;
	@Autowired
	private TaskStatusManager taskStatusManager;
	@Autowired
	private SchedulingPool schedulingPool;
	
	final int MAX_RUNNING_GEN = 20;
	final int MAX_RUNNING = 1000;
	
	@Override
	public void beginExecuting() {
		int currentlyRunningGenTasks = 0;

		// TODO count gen tasks when they run and finish
		for(Task task : schedulingPool.getProcessingTasks()) {
			if(task.getExecService().getExecServiceType().equals(ExecServiceType.GENERATE)) {
				currentlyRunningGenTasks++;
			}
		}
		log.debug("There are " + currentlyRunningGenTasks + " running gen tasks.");
		if(currentlyRunningGenTasks >= MAX_RUNNING_GEN) {
			log.warn("Reached the maximum number of concurrently running gen tasks.");
		}
		
		// run all tasks in scheduled state
		Date now = new Date(System.currentTimeMillis());
		for (Task task : schedulingPool.getPlannedTasks()) {
			/*
			if(schedulingPool.getProcessingTasks().size() > MAX_RUNNING) {
				log.warn("Reached the maximum number of concurrently running tasks.");
				break;
			}
			if(currentlyRunningGenTasks >= MAX_RUNNING_GEN) {
				continue;
			}
			*/
			log.debug("TASK " + task.toString() + " is to be run at "
					+ task.getSchedule() + ", now is " + now);
			if (task.getSchedule().before(now)) {
				log.debug("TASK " + task.toString() + " is going to run");
				runTask(task);
			}
		}
		/*
		 * int executorWorkersCreated = 0;
		 * log.debug("Begin execution process..."); List<Task> tasks = null;
		 * Date olderThen = null; Date youngerThen = null; long hour = 3600000;
		 * long year = hour * 8760; olderThen = new
		 * Date(System.currentTimeMillis() - year); youngerThen = new
		 * Date(System.currentTimeMillis() + hour); tasks =
		 * taskManager.listTasksScheduledBetweenDates(olderThen, youngerThen,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); if
		 * (tasks != null) { log.info("There are " + tasks.size() +
		 * " tasks between dates [" + olderThen + "] and [" + youngerThen +
		 * "]"); for (Task task : tasks) { // We are about to execute only these
		 * tasks that are in the PLANNED state. if
		 * (task.getStatus().equals(TaskStatus.PLANNED)) { ExecService
		 * execService = task.getExecService();
		 * 
		 * // set task to PROCESSING state
		 * task.setStatus(TaskStatus.PROCESSING); task.setStartTime(new
		 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
		 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); //
		 * TODO switch the state when following core failed somewhere // (for
		 * example when break is called)
		 * 
		 * if
		 * (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
		 * log.debug("I'm gonna create worker for ExecService GENERATE, ID:" +
		 * execService.getId()); ExecutorEngineWorker executorEngineWorker =
		 * createExecutorEngineWorker(); executorWorkersCreated++;
		 * executorEngineWorker.setTask(task);
		 * executorEngineWorker.setExecService(execService); //A dedicated
		 * executor for GENERATE
		 * taskExecutorGenWorkers.execute(executorEngineWorker); } else if
		 * (execService.getExecServiceType().equals(ExecServiceType.SEND)) {
		 * log.debug( "I'm gonna create worker for ExecService SEND, ID:" +
		 * execService.getId()); switch(task.getType()) { case SERIAL:
		 * executorWorkersCreated = startSerialWorkers(task); break; case
		 * PARALLEL: executorWorkersCreated = startParallelWorkers(task); break;
		 * default: break; } if(executorWorkersCreated < 0) { continue; } } else
		 * { throw new IllegalArgumentException(
		 * "ExecService type has to be either SEND or GENERATE."); } } } } else
		 * { log.debug("There are no tasks between dates [" + olderThen +
		 * "] and [" + youngerThen + "]"); }
		 * log.info("Executing process ended. Created workers:" +
		 * executorWorkersCreated);
		 */
	}

	/**
	 * Put the task into PROCESSING state and create workers for all
	 * destinations that have satisfied dependencies (or no dependencies at
	 * all).
	 * 
	 * @param task
	 *            Task to start.
	 * 
	 */
	public void runTask(Task task) {
		schedulingPool.setTaskStatus(task, TaskStatus.PROCESSING);
		task.setStartTime(new Date(System.currentTimeMillis()));
		List<Task> dependencies = dependencyResolver.getDependencies(task);
		// TODO: handle GEN tasks with no destinations
		boolean started = false;
		for (Destination destination : taskStatusManager.getTaskStatus(task)
				.getWaitingDestinations()) {
			// check if all the dependency destinations are done
			boolean proceed = true;
			try {
				for (Task dependency : dependencies) {
					if (taskStatusManager.getTaskStatus(dependency)
							.getDestinationStatus(destination) != TaskDestinationStatus.DONE) {
						log.debug("TASK " + task.toString()
								+ " has unmet dependencies");
						proceed = false;
					}
				}
			} catch (InternalErrorException e) {
				log.error("Error getting dependency status for task {}",
						task.toString());
			}
			if (proceed) {
				try {
					if (task.getExecService().getExecServiceType().equals(ExecServiceType.SEND)) {
						taskStatusManager.getTaskStatus(task).setDestinationStatus(
								destination,
								TaskDestinationStatus.PROCESSING);
					}
				} catch (InternalErrorException e) {
					log.error("Error setting status for destination {} of task {}",
							destination, task.toString());
				}
				try {
					startWorker(task, destination);
					started = true;
				} catch(Exception e) {
					log.error("Error queuing worker for execution: " + e.toString());
				}
			}
		}
		if(!started) {
			log.warn("No worker started for task {}, setting to ERROR", task.getId());
			task.setEndTime(new Date(System.currentTimeMillis()));
			schedulingPool.setTaskStatus(task, TaskStatus.ERROR);
		}
	}

	/**
     * 
     */
	private void startWorker(Task task, Destination destination) {
		log.debug("Starting worker for task " + task.getId()
				+ " and destination " + destination.toString());
		ExecutorEngineWorker executorEngineWorker = createExecutorEngineWorker();
		executorEngineWorker.setTask(task);
		executorEngineWorker.setFacility(task.getFacility());
		executorEngineWorker.setExecService(task.getExecService());
		executorEngineWorker.setDestination(destination);
		executorEngineWorker.setResultListener((TaskResultListener) taskStatusManager);
		if (task.getExecService().getExecServiceType().equals(ExecServiceType.GENERATE)) {
			taskExecutorGenWorkers.execute(executorEngineWorker);
		} else {
			taskExecutorSendWorkers.execute(executorEngineWorker);
		}
	}

	/**
	 * Get one destination that has not been tried yet and start worker for it.
	 * 
	 * @param task
	 * @return
	 */
	/*
	 * private int startSerialWorkers(Task task) { List<Destination>
	 * destinations = null; List<TaskResult> results = null; try { // get
	 * destinations for which there are no task results destinations =
	 * Rpc.ServicesManager.getDestinations(engineManager.getRpcCaller(),
	 * task.getExecService().getService(), task.getFacility()); results =
	 * taskResultDao.getTaskResultsByTask(task.getId()); for(TaskResult result:
	 * results) { destinations.remove(result.getDestination()); }
	 * if(destinations.isEmpty()) { return 0; } Destination destination =
	 * destinations.get(0); ExecutorEngineWorker executorEngineWorker =
	 * createExecutorEngineWorker(); executorEngineWorker.setTask(task);
	 * executorEngineWorker.setFacility(task.getFacility());
	 * executorEngineWorker.setExecService(task.getExecService());
	 * executorEngineWorker.setDestination(destination); //A dedicated executor
	 * for SEND taskExecutorSendWorkers.execute(executorEngineWorker); } catch
	 * (ServiceNotExistsException e) { // TODO Auto-generated catch block }
	 * catch (FacilityNotExistsException e) { // TODO Auto-generated catch block
	 * } catch (PrivilegeException e) { // TODO Auto-generated catch block }
	 * catch (InternalErrorException e) { // TODO Auto-generated catch block }
	 * return 1; }
	 */
	/**
	 * Start workers for all destinations defined for given task.
	 * 
	 * @param task
	 * @return
	 */
	/*
	 * private int startParallelWorkers(Task task) { int executorWorkersCreated
	 * = 0; Facility facility = null; List<Destination> destinations = null; try
	 * { facility = task.getFacility(); destinations =
	 * Rpc.ServicesManager.getDestinations(engineManager.getRpcCaller(),
	 * task.getExecService().getService(), facility);
	 * 
	 * for (Destination destination : destinations) { ExecutorEngineWorker
	 * executorEngineWorker = createExecutorEngineWorker();
	 * executorWorkersCreated++; executorEngineWorker.setTask(task);
	 * executorEngineWorker.setFacility(facility);
	 * executorEngineWorker.setExecService(task.getExecService());
	 * executorEngineWorker.setDestination(destination); //A dedicated executor
	 * for SEND taskExecutorSendWorkers.execute(executorEngineWorker); }
	 * 
	 * } catch (FacilityNotExistsException e) {
	 * log.error("Skipping this one due to:" + e.toString(), e);
	 * task.setStatus(TaskStatus.ERROR); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); return
	 * -1; } catch (InternalErrorException e) {
	 * log.error("Skipping this one due to:" + e.toString(), e);
	 * task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); return
	 * -1; } catch (PrivilegeException e) {
	 * log.error("Skipping this one due to:" + e.toString(), e);
	 * task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); return
	 * -1; } catch (ServiceNotExistsException e) {
	 * log.error("Skipping this one due to:" + e.toString(), e);
	 * task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); return
	 * -1; } catch (Exception e) { log.error("Skipping this one due to:" +
	 * e.toString(), e); task.setStatus(TaskStatus.ERROR); task.setEndTime(new
	 * Date(System.currentTimeMillis())); taskManager.updateTask(task,
	 * Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))); return
	 * -1; } return executorWorkersCreated; }
	 */

	protected ExecutorEngineWorker createExecutorEngineWorker() {
		ExecutorEngineWorker worker = (ExecutorEngineWorker) this.beanFactory.getBean("executorEngineWorker");
		return worker;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public TaskExecutor getTaskExecutorGenWorkers() {
		return taskExecutorGenWorkers;
	}

	public void setTaskExecutorGenWorkers(TaskExecutor taskExecutorGenWorkers) {
		this.taskExecutorGenWorkers = taskExecutorGenWorkers;
	}

	public TaskExecutor getTaskExecutorSendWorkers() {
		return taskExecutorSendWorkers;
	}

	public void setTaskExecutorSendWorkers(TaskExecutor taskExecutorSendWorkers) {
		this.taskExecutorSendWorkers = taskExecutorSendWorkers;
	}

	public DependenciesResolver getDependencyResolver() {
		return dependencyResolver;
	}

	public void setDependencyResolver(DependenciesResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
	}

	public TaskStatusManager getTaskStatusManager() {
		return taskStatusManager;
	}

	public void setTaskStatusManager(TaskStatusManager taskStatusManager) {
		this.taskStatusManager = taskStatusManager;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

}
