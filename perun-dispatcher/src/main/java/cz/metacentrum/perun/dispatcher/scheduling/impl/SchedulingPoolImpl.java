package cz.metacentrum.perun.dispatcher.scheduling.impl;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducerFactory;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskSchedule;
import cz.metacentrum.perun.taskslib.service.TaskStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.DelayQueue;

import javax.annotation.Resource;

/**
 * Implementation of SchedulingPool.
 *
 * @see cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool
 *
 * @author Michal Voců
 * @author Michal Babacek
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service("schedulingPool")
public class SchedulingPoolImpl implements SchedulingPool {

	private final static Logger log = LoggerFactory.getLogger(SchedulingPoolImpl.class);

	private PerunSession sess;

	private DelayQueue<TaskSchedule> waitingTasksQueue;
	private DelayQueue<TaskSchedule> waitingForcedTasksQueue;
	private Properties dispatcherProperties;
	private TaskStore taskStore;
	private TasksManagerBl tasksManagerBl;
	private EngineMessageProducerFactory engineMessageProducerFactory;
	private Perun perun;

	public SchedulingPoolImpl() {
	}

	public SchedulingPoolImpl(Properties dispatcherPropertiesBean,
				TaskStore taskStore,
				TasksManagerBl tasksManagerBl,
				EngineMessageProducerFactory engineMessageProducerFactory) {
		this.dispatcherProperties = dispatcherPropertiesBean;
		this.taskStore = taskStore;
		this.tasksManagerBl = tasksManagerBl;
		this.engineMessageProducerFactory = engineMessageProducerFactory;
	}


	// ----- setters -------------------------------------


	public DelayQueue<TaskSchedule> getWaitingTasksQueue() {
		return waitingTasksQueue;
	}

	@Autowired
	public void setWaitingTasksQueue(DelayQueue<TaskSchedule> waitingTasksQueue) {
		this.waitingTasksQueue = waitingTasksQueue;
	}

	public DelayQueue<TaskSchedule> getWaitingForcedTasksQueue() {
		return waitingForcedTasksQueue;
	}

	@Autowired
	public void setWaitingForcedTasksQueue(DelayQueue<TaskSchedule> waitingForcedTasksQueue) {
		this.waitingForcedTasksQueue = waitingForcedTasksQueue;
	}

	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}

	public TaskStore getTaskStore() {
		return taskStore;
	}

	@Autowired
	public void setTaskStore(TaskStore taskStore) {
		this.taskStore = taskStore;
	}

	public TasksManagerBl getTasksManagerBl() {
		return tasksManagerBl;
	}

	@Autowired
	public void setTasksManagerBl(TasksManagerBl tasksManagerBl) {
		this.tasksManagerBl = tasksManagerBl;
	}

	public EngineMessageProducerFactory getEngineMessageProducerPool() {
		return engineMessageProducerFactory;
	}

	@Autowired
	public void setEngineMessageProducerPool(EngineMessageProducerFactory engineMessageProducerPool) {
		this.engineMessageProducerFactory = engineMessageProducerPool;
	}

	public Perun getPerun() {
		return perun;
	}

	@Autowired
	public void setPerun(Perun perun) {
		this.perun = perun;
	}


	// ----- methods -------------------------------------


	@Override
	public Task getTask(int id) {
		return taskStore.getTask(id);
	}

	@Override
	public Task getTask(Facility facility, Service service) {
		return taskStore.getTask(facility, service);
	}

	@Override
	public int getSize() {
		return taskStore.getSize();
	}

	@Override
	public Task addTask(Task task) throws TaskStoreException {
		return taskStore.addTask(task);
	}

	@Override
	public Collection<Task> getAllTasks() {
		return taskStore.getAllTasks();
	}

	@Override
	public List<Task> getTasksWithStatus(TaskStatus... status) {
		return taskStore.getTasksWithStatus(status);
	}

	@Override
	public Task removeTask(Task task) throws TaskStoreException {
		return taskStore.removeTask(task);
	}

	@Override
	public void scheduleTask(Task task, int delayCount) {

		// init session
		try {
			if (sess == null) {
				sess = perun.getPerunSession(new PerunPrincipal(
								dispatcherProperties.getProperty("perun.principal.name"),
								dispatcherProperties.getProperty("perun.principal.extSourceName"),
								dispatcherProperties.getProperty("perun.principal.extSourceType")),
						new PerunClient());
			}
		} catch (InternalErrorException e1) {
			log.error("Error establishing perun session to add task schedule: ", e1);
			return;
		}

		// check if service/facility exists

		boolean removeTask = false;

		try {
			Service service = perun.getServicesManager().getServiceById(sess, task.getServiceId());
			Facility facility = perun.getFacilitiesManager().getFacilityById(sess, task.getFacilityId());
			task.setService(service);
			task.setFacility(facility);
		} catch (ServiceNotExistsException e) {
			log.error("[{}] Task NOT added to waiting queue, service not exists: {}.", task.getId(), task);
			removeTask = true;
		} catch (FacilityNotExistsException e) {
			log.error("[{}] Task NOT added to waiting queue, facility not exists: {}.", task.getId(), task);
			removeTask = true;
		}  catch (InternalErrorException | PrivilegeException e) {
			log.error("[{}] {}", task.getId(), e);
		}

		if (!task.getService().isEnabled() || ((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(task.getService(), task.getFacility())) {
			log.error("[{}] Task NOT added to waiting queue, service is blocked: {}.", task.getId(), task);
			// do not change Task status or any other data !
			if (!removeTask) return;
		}

		try {
			List<Destination> destinations = perun.getServicesManager().getDestinations(sess, task.getService(), task.getFacility());
			if (destinations != null && !destinations.isEmpty()) {
				Iterator<Destination> iter = destinations.iterator();
				while (iter.hasNext()) {
					Destination dest = iter.next();
					if (((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(task.getService(), dest.getId())) {
						iter.remove();
					}
				}
				if (destinations.isEmpty()) {
					// All service destinations were blocked -> Task is denied to be sent to engine just like
					// when service is blocked globally in Perun or on facility as a whole.
					log.debug("[{}] Task NOT added to waiting queue, all its destinations are blocked.", task.getId());
					if (!removeTask) return;
				}
			}

		} catch (ServiceNotExistsException e) {
			log.error("[{}] Task NOT added to waiting queue, service not exists: {}.", task.getId(), task);
			removeTask = true;
		} catch (FacilityNotExistsException e) {
			log.error("[{}] Task NOT added to waiting queue, facility not exists: {}.", task.getId(), task);
			removeTask = true;
		}  catch (InternalErrorException | PrivilegeException e) {
			log.error("[{}] {}", task.getId(), e);
		}

		try {
			List<Service> assignedServices = perun.getServicesManager().getAssignedServices(sess, task.getFacility());
			if (!assignedServices.contains(task.getService())) {
				log.debug("[{}] Task NOT added to waiting queue, service is not assigned to facility any more: {}.", task.getId(), task);
				if (!removeTask) return;
			}
		} catch (FacilityNotExistsException e) {
			removeTask = true;
			log.error("[{}] Task removed from database, facility no longer exists: {}.", task.getId(), task);
		} catch (InternalErrorException | PrivilegeException e) {
			log.error("[{}] Unable to check Service assignment to Facility: {}", task.getId(), e.getMessage());
		}

		if (removeTask) {
			// in memory task belongs to non existent facility/service - remove it and return
			try {
				removeTask(task);
				return;
			} catch (TaskStoreException e) {
				log.error("[{}] Unable to remove Task from pool: {}.", task.getId(), e);
				return;
			}
		}

		// Task is eligible for running - create new schedule

		task.setSourceUpdated(false);

		long newTaskDelay = 0;
		if (!task.isPropagationForced()) {
			// normal tasks are delayed
			try {
				newTaskDelay = Long.parseLong(dispatcherProperties.getProperty("dispatcher.task.delay.time"));
			} catch (NumberFormatException e) {
				log.warn("Could not parse value of dispatcher.task.delay.time property. Using default.");
				newTaskDelay = 30000;
			}
		}
		if (task.isPropagationForced()) {
			delayCount = 0;
		}
		if (delayCount < 0) {
			try {
				delayCount = Integer.parseInt(dispatcherProperties.getProperty("dispatcher.task.delay.count"));
			} catch (NumberFormatException e) {
				log.warn("Could not parse value of dispatcher.task.delay.count property. Using default.");
				delayCount = 4;
			}
		}

		TaskSchedule schedule = new TaskSchedule(newTaskDelay, task);
		schedule.setBase(System.currentTimeMillis());
		schedule.setDelayCount(delayCount);

		// Task was newly planned for propagation, switch state.
		if (!task.getStatus().equals(TaskStatus.WAITING)) {

			task.setStatus(TaskStatus.WAITING);
			task.setSchedule(LocalDateTime.now());
			// clear previous timestamps
			task.setSentToEngine((LocalDateTime) null);
			task.setStartTime((LocalDateTime) null);
			task.setGenStartTime((LocalDateTime) null);
			task.setSendStartTime((LocalDateTime) null);
			task.setEndTime((LocalDateTime) null);
			task.setGenEndTime((LocalDateTime) null);
			task.setSendEndTime((LocalDateTime) null);

			tasksManagerBl.updateTask(task);

		}

		boolean added = false;

		if (schedule.getTask().isPropagationForced()) {
			added = waitingForcedTasksQueue.add(schedule);
		} else {
			added = waitingTasksQueue.add(schedule);
		}

		if (!added) {
			log.error("[{}] Task could not be added to waiting queue. Shouldn't ever happen. Look to javadoc of DelayQueue. {}", task.getId(), schedule);
		} else {
			log.debug("[{}] Task was added to waiting queue: {}", task.getId(), schedule);
		}

	}

	/**
	 * Adds Task and associated dispatcherQueue into scheduling pools internal maps and also to the database.
	 *
	 * @param task            Task which will be added and persisted.
	 * @return Number of Tasks in the pool.
	 * @throws TaskStoreException
	 */
	@Override
	public int addToPool(Task task) throws TaskStoreException {

		if (task.getId() == 0) {
			if (getTask(task.getFacility(), task.getService()) == null) {
				int id = tasksManagerBl.insertTask(task);
				task.setId(id);
				log.debug("[{}] New Task stored in DB: {}", task.getId(), task);
			} else {
				Task existingTask = tasksManagerBl.getTaskById(task.getId());
				if (existingTask == null) {
					int id = tasksManagerBl.insertTask(task);
					task.setId(id);
					log.debug("[{}] New Task stored in DB: {}", task.getId(), task);
				} else {
					tasksManagerBl.updateTask(task);
					log.debug("[{}] Task updated in the pool: {}", task.getId(), task);
				}
			}
		}
		addTask(task);
		log.debug("[{}] Task added to the pool: {}", task.getId(), task);
		return getSize();
	}

	@Override
	public Task removeTask(int id) throws TaskStoreException {
		return taskStore.removeTask(id);
	}

	@Override
	public String getReport() {
		int waiting = getTasksWithStatus(TaskStatus.WAITING).size();
		int planned = getTasksWithStatus(TaskStatus.PLANNED).size();
		int generating = getTasksWithStatus(TaskStatus.GENERATING).size();
		int generated = getTasksWithStatus(TaskStatus.GENERATED).size();
		int generror = getTasksWithStatus(TaskStatus.GENERROR).size();
		int sending = getTasksWithStatus(TaskStatus.SENDING).size();
		int senderror = getTasksWithStatus(TaskStatus.SENDERROR).size();
		int done = getTasksWithStatus(TaskStatus.DONE).size();
		int warning = getTasksWithStatus(TaskStatus.WARNING).size();
		int error = getTasksWithStatus(TaskStatus.ERROR).size();

		return "Dispatcher SchedulingPool Task report:\n" +
				"  WAITING: " + waiting +
				"  PLANNED: " + planned +
				"  GENERATING: " + generating +
				"  GENERATED: " + generated +
				"  GENERROR: " + generror +
				"  SENDING:  " + sending +
				"  SENDEEROR:  " + senderror +
				"  DONE: " + done +
				"  WARNING: " + warning + 
				"  ERROR: " + error;
	}

	@Override
	public void clear() {
		taskStore.clear();
		waitingTasksQueue.clear();
		waitingForcedTasksQueue.clear();
	}

	@Override
	public void reloadTasks() {

		log.debug("Going to reload Tasks from database...");

		this.clear();

		EngineMessageProducer queue = engineMessageProducerFactory.getProducer();

		for (Task task : tasksManagerBl.listAllTasks()) {
			try {
				// just add DB Task to in-memory structure
				addToPool(task);
			} catch (TaskStoreException e) {
				log.error("Adding Task {} and Queue {} into SchedulingPool failed, so the Task will be lost.", task, queue);
			}

			// if service was not in DONE or any kind of ERROR - reschedule now
			// error/done tasks will be rescheduled later by periodic jobs !!
			if (!Arrays.asList(TaskStatus.DONE, TaskStatus.ERROR, TaskStatus.GENERROR, TaskStatus.SENDERROR, TaskStatus.WARNING).contains(task.getStatus())) {
				if (task.getStatus().equals(TaskStatus.WAITING)) {
					// if were in WAITING, reset timestamp to now
					task.setSchedule(LocalDateTime.now());
					tasksManagerBl.updateTask(task);
				}
				scheduleTask(task, 0);
			}

		}

		log.debug("Reload of Tasks from database finished.");

	}

	@Override
	public void closeTasksForEngine() {

		List<Task> tasks = taskStore.getTasksWithStatus(
				TaskStatus.PLANNED,
				TaskStatus.GENERATING,
				TaskStatus.GENERATED,
				TaskStatus.SENDING,
				TaskStatus.WARNING
				);

		// switch all processing tasks to error, remove the engine queue association
		log.debug("Switching processing tasks on engine to ERROR, the engine went down...");
		for (Task task : tasks) {
			log.info("[{}] Switching Task to ERROR, the engine it was running on went down.", task.getId());
			task.setStatus(TaskStatus.ERROR);
		}

	}

	@Override
	public void onTaskStatusChange(int taskId, String status, String milliseconds) {

		Task task = getTask(taskId);
		if (task == null) {
			log.error("[{}] Received status update about Task which is not in Dispatcher anymore, will ignore it.", taskId);
			return;
		}

		TaskStatus oldStatus = task.getStatus();
		task.setStatus(TaskStatus.valueOf(status));
		long ms;
		try {
			ms = Long.valueOf(milliseconds);
		} catch (NumberFormatException e) {
			log.warn("[{}] Timestamp of change '{}' could not be parsed, current time will be used instead.", task.getId(), milliseconds);
			ms = System.currentTimeMillis();
		}
		LocalDateTime changeDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());

		switch (task.getStatus()) {
			case WAITING:
			case PLANNED:
				log.error("[{}] Received status change to {} from Engine, this should not happen.", task.getId(), task.getStatus());
				return;
			case GENERATING:
				task.setStartTime(changeDate);
				task.setGenStartTime(changeDate);
				break;
			case GENERROR:
				task.setEndTime(changeDate);
			case GENERATED:
				task.setGenEndTime(changeDate);
				break;
			case SENDING:
				task.setSendStartTime(changeDate);
				break;
			case DONE:
			case WARNING:
			case SENDERROR:
				task.setSendEndTime(changeDate);
				task.setEndTime(changeDate);
				break;
			case ERROR:
				task.setEndTime(changeDate);
				break;
		}

		tasksManagerBl.updateTask(task);

		log.debug("[{}] Task status changed from {} to {} as reported by Engine: {}.", task.getId(), oldStatus, task.getStatus(), task);

	}

	@Override
	public void onTaskDestinationComplete(String string) {

		if (string == null || string.isEmpty()) {
			log.error("Could not parse TaskResult message from Engine.");
			return;
		}

		try {
			List<PerunBean> listOfBeans = AuditParser.parseLog(string);
			if (!listOfBeans.isEmpty()) {
				TaskResult taskResult = (TaskResult) listOfBeans.get(0);
				log.debug("[{}] Received TaskResult for Task from Engine.", taskResult.getTaskId());
				onTaskDestinationComplete(taskResult);
			} else {
				log.error("No TaskResult found in message from Engine: {}.", string);
			}
		} catch (Exception e) {
			log.error("Could not save TaskResult from Engine {}, {}", string, e.getMessage());
		}

	}

	@Override
	public void onTaskDestinationComplete(TaskResult taskResult) {
		try {
			tasksManagerBl.insertNewTaskResult(taskResult);
		} catch (Exception e) {
			log.error("Could not save TaskResult from Engine, {}, {}", taskResult, e.getMessage());
		}
	}

}
