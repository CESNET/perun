package cz.metacentrum.perun.dispatcher.scheduling.impl;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducerPool;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskSchedule;
import cz.metacentrum.perun.taskslib.service.ResultManager;
import cz.metacentrum.perun.taskslib.service.TaskManager;
import cz.metacentrum.perun.taskslib.service.TaskStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	private final Map<Integer, EngineMessageProducer> enginesByTaskId = new HashMap<>();
	private PerunSession sess;

	private DelayQueue<TaskSchedule> waitingTasksQueue;
	private DelayQueue<TaskSchedule> waitingForcedTasksQueue;
	private Properties dispatcherProperties;
	private TaskStore taskStore;
	private TaskManager taskManager;
	private ResultManager resultManager;
	private EngineMessageProducerPool engineMessageProducerPool;
	private GeneralServiceManager generalServiceManager;
	private Perun perun;

	public SchedulingPoolImpl() {
	}

	public SchedulingPoolImpl(Properties dispatcherPropertiesBean, TaskStore taskStore,
	                          TaskManager taskManager, EngineMessageProducerPool engineMessageProducerPool) {
		this.dispatcherProperties = dispatcherPropertiesBean;
		this.taskStore = taskStore;
		this.taskManager = taskManager;
		this.engineMessageProducerPool = engineMessageProducerPool;
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

	public TaskManager getTaskManager() {
		return taskManager;
	}

	@Autowired
	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public ResultManager getResultManager() {
		return resultManager;
	}

	@Autowired
	public void setResultManager(ResultManager resultManager) {
		this.resultManager = resultManager;
	}

	public EngineMessageProducerPool getEngineMessageProducerPool() {
		return engineMessageProducerPool;
	}

	@Autowired
	public void setEngineMessageProducerPool(EngineMessageProducerPool engineMessageProducerPool) {
		this.engineMessageProducerPool = engineMessageProducerPool;
	}

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	@Autowired
	public void setGeneralServiceManager(GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
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

		if (!task.getService().isEnabled() || generalServiceManager.isServiceBlockedOnFacility(task.getService(), task.getFacility())) {
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
					if (generalServiceManager.isServiceBlockedOnDestination(task.getService(), dest.getId())) {
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
			task.setSchedule(new Date(System.currentTimeMillis()));
			// clear previous timestamps
			task.setSentToEngine(null);
			task.setStartTime(null);
			task.setGenStartTime(null);
			task.setSendStartTime(null);
			task.setEndTime(null);
			task.setGenEndTime(null);
			task.setSendEndTime(null);

			taskManager.updateTask(task);

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
	 * @param engineMessageProducer dispatcherQueue associated with the Task which will be added and persisted.
	 * @return Number of Tasks in the pool.
	 * @throws InternalErrorException Thrown if the Task could not be persisted.
	 * @throws TaskStoreException
	 */
	@Override
	public int addToPool(Task task, EngineMessageProducer engineMessageProducer) throws InternalErrorException, TaskStoreException {

		int engineId = (engineMessageProducer == null) ? -1 : engineMessageProducer.getClientID();
		if (task.getId() == 0) {
			if (getTask(task.getFacility(), task.getService()) == null) {
				int id = taskManager.scheduleNewTask(task, engineId);
				task.setId(id);
				log.debug("[{}] New Task stored in DB: {}", task.getId(), task);
			} else {
				Task existingTask = taskManager.getTaskById(task.getId());
				if (existingTask == null) {
					int id = taskManager.scheduleNewTask(task, engineId);
					task.setId(id);
					log.debug("[{}] New Task stored in DB: {}", task.getId(), task);
				} else {
					taskManager.updateTask(task);
					log.debug("[{}] Task updated in the pool: {}", task.getId(), task);
				}
			}
		}
		addTask(task);
		enginesByTaskId.put(task.getId(), engineMessageProducer);
		log.debug("[{}] Task added to the pool: {}", task.getId(), task);
		return getSize();
	}

	@Override
	public Task removeTask(int id) throws TaskStoreException {
		return taskStore.removeTask(id);
	}

	@Override
	public EngineMessageProducer getEngineMessageProducerForTask(Task task) throws InternalErrorException {
		if (task == null) {
			log.error("Supplied Task is null.");
			throw new IllegalArgumentException("Task cannot be null");
		}
		EngineMessageProducer entry = enginesByTaskId.get(task.getId());
		if (entry == null) {
			throw new InternalErrorException("No Task with ID " + task.getId());
		}
		return entry;
	}


	@Override
	public List<Task> getTasksForEngine(int clientID) {
		List<Task> result = new ArrayList<>();
		for (Map.Entry<Integer, EngineMessageProducer> entry : enginesByTaskId.entrySet()) {
			if (entry.getValue() != null && clientID == entry.getValue().getClientID()) {
				result.add(getTask(entry.getKey()));
			}
		}
		return result;
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
				"  ERROR: " + error;
	}

	@Override
	public void clear() {
		taskStore.clear();
		enginesByTaskId.clear();
		waitingTasksQueue.clear();
		waitingForcedTasksQueue.clear();
	}

	@Override
	public void reloadTasks() {

		log.debug("Going to reload Tasks from database...");

		this.clear();

		for (Pair<Task, Integer> pair : taskManager.listAllTasksAndClients()) {
			Task task = pair.getLeft();
			EngineMessageProducer queue = engineMessageProducerPool.getProducerByClient(pair.getRight());
			try {
				// just add DB Task to in-memory structure
				addToPool(task, queue);
			} catch (InternalErrorException | TaskStoreException e) {
				log.error("Adding Task {} and Queue {} into SchedulingPool failed, so the Task will be lost.", task, queue);
			}

			// if service was not in DONE or any kind of ERROR - reschedule now
			// error/done tasks will be rescheduled later by periodic jobs !!
			if (!Arrays.asList(TaskStatus.DONE, TaskStatus.ERROR, TaskStatus.GENERROR, TaskStatus.SENDERROR).contains(task.getStatus())) {
				if (task.getStatus().equals(TaskStatus.WAITING)) {
					// if were in WAITING, reset timestamp to now
					task.setSchedule(new Date(System.currentTimeMillis()));
					taskManager.updateTask(task);
				}
				scheduleTask(task, 0);
			}

		}

		log.debug("Reload of Tasks from database finished.");

	}

	@Override
	public void setEngineMessageProducerForTask(Task task, EngineMessageProducer messageProducer) throws InternalErrorException {
		Task found = getTask(task.getId());
		if (found == null) {
			throw new InternalErrorException("no task by id " + task.getId());
		} else {
			enginesByTaskId.put(task.getId(), messageProducer);
		}
		// if queue is removed, set -1 to task as it's done on task creation if queue is null
		int queueId = (messageProducer != null) ? messageProducer.getClientID() : -1;
		taskManager.updateTaskEngine(task, queueId);
	}

	@Override
	public void closeTasksForEngine(int clientID) {

		List<Task> tasks = getTasksForEngine(clientID);
		List<TaskStatus> engineStates = new ArrayList<>();
		engineStates.add(TaskStatus.PLANNED);
		engineStates.add(TaskStatus.GENERATING);
		engineStates.add(TaskStatus.GENERATED);
		engineStates.add(TaskStatus.SENDING);

		// switch all processing tasks to error, remove the engine queue association
		log.debug("Switching processing tasks on engine {} to ERROR, the engine went down...", clientID);
		for (Task task : tasks) {
			if (engineStates.contains(task.getStatus())) {
				log.info("[{}] Switching Task to ERROR, the engine it was running on went down.", task.getId());
				task.setStatus(TaskStatus.ERROR);
			}
			try {
				setEngineMessageProducerForTask(task, null);
			} catch (InternalErrorException e) {
				log.error("[{}] Could not remove dispatcher queue for task: {}.", task.getId(), e.getMessage());
			}
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
		Date changeDate = new Date(ms);

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
			case SENDERROR:
				task.setSendEndTime(changeDate);
				task.setEndTime(changeDate);
				break;
			case ERROR:
				task.setEndTime(changeDate);
				break;
		}

		taskManager.updateTask(task);

		log.debug("[{}] Task status changed from {} to {} as reported by Engine: {}.", task.getId(), oldStatus, task.getStatus(), task);

	}

	@Override
	public void onTaskDestinationComplete(int clientID, String string) {

		if (string == null || string.isEmpty()) {
			log.error("Could not parse TaskResult message from Engine {}.", clientID);
			return;
		}

		try {
			List<PerunBean> listOfBeans = AuditParser.parseLog(string);
			if (!listOfBeans.isEmpty()) {
				TaskResult taskResult = (TaskResult) listOfBeans.get(0);
				log.debug("[{}] Received TaskResult for Task from Engine {}.", taskResult.getTaskId(), clientID);
				resultManager.insertNewTaskResult(taskResult, clientID);
			} else {
				log.error("No TaskResult found in message from Engine {}: {}.", clientID, string);
			}
		} catch (Exception e) {
			log.error("Could not save TaskResult from Engine {}, {}, {}", clientID, string, e.getMessage());
		}

	}

}
