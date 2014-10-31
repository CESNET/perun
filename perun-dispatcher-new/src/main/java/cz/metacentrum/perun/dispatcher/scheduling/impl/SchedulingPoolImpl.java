package cz.metacentrum.perun.dispatcher.scheduling.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueuePool;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.service.TaskManager;

// TODO: this shares a lot of code with engine.SchedulingPoolImpl - create abstract base with implementation specific indexes

@org.springframework.stereotype.Service("schedulingPool")
public class SchedulingPoolImpl implements SchedulingPool {

	private final static Logger log = LoggerFactory
			.getLogger(SchedulingPoolImpl.class);

	private Map<Integer, Pair<Task, DispatcherQueue>> tasksById = new ConcurrentHashMap<Integer, Pair<Task, DispatcherQueue>>();
	private Map<Pair<ExecService, Facility>, Task> tasksByServiceAndFacility = new ConcurrentHashMap<Pair<ExecService, Facility>, Task>();
	private Map<TaskStatus, List<Task>> pool = new EnumMap<TaskStatus, List<Task>>(
			TaskStatus.class);

	@Autowired
	private TaskManager taskManager;
	@Autowired
	private DispatcherQueuePool dispatcherQueuePool;

	public SchedulingPoolImpl() {
		for (TaskStatus status : TaskStatus.class.getEnumConstants()) {
			pool.put(status, new ArrayList<Task>());
		}
	}

	@Override
	public int getSize() {
		return tasksById.size();
	}

	@Override
	public int addToPool(Task task, DispatcherQueue dispatcherQueue)
			throws InternalErrorException {
		if (task.getId() == 0) {
			// this task was created new, so we have to check the
			// ExecService,Facility pair
			synchronized (tasksByServiceAndFacility) {
				if (!tasksByServiceAndFacility
						.containsKey(new Pair<ExecService, Facility>(task
								.getExecService(), task.getFacility()))) {
					log.debug("Adding new task to pool " + task);
					if (null == task.getStatus()) {
						task.setStatus(TaskStatus.NONE);
					}
					try {
						int id = taskManager.scheduleNewTask(task,
								dispatcherQueue.getClientID());
						task.setId(id);
					} catch (InternalErrorException e) {
						log.error("Error storing task " + task
								+ " into database: " + e.getMessage());
						throw new InternalErrorException(
								"Could not assign id to newly created task", e);
					}
					tasksByServiceAndFacility.put(
							new Pair<ExecService, Facility>(task
									.getExecService(), task.getFacility()),
							task);
					tasksById.put(task.getId(),
							new Pair<Task, DispatcherQueue>(task,
									dispatcherQueue));
					pool.get(task.getStatus()).add(task);
				} else {
					log.debug("There already is task for given ExecService and Facility pair");
				}
			}
		} else {
			// weird - we should not be adding tasks with id present...
			synchronized (tasksById) {
				if (!tasksById.containsKey(task.getId())) {
					log.debug("Adding task to pool " + task);
					if (null == task.getStatus()) {
						task.setStatus(TaskStatus.NONE);
					}
					tasksById.put(task.getId(),
							new Pair<Task, DispatcherQueue>(task,
									dispatcherQueue));
					tasksByServiceAndFacility.put(
							new Pair<ExecService, Facility>(task
									.getExecService(), task.getFacility()),
							task);
					pool.get(task.getStatus()).add(task);
				}
			}
			try {
				Task existingTask = taskManager.getTaskById(task.getId(),
						dispatcherQueue.getClientID());
				if (existingTask == null) {
					taskManager.scheduleNewTask(task,
							dispatcherQueue.getClientID());
				} else {
					taskManager.updateTask(task, dispatcherQueue.getClientID());
				}
			} catch (InternalErrorException e) {
				log.error("Error storing task " + task + " into database: "
						+ e.getMessage());
			}
		}
		return getSize();
	}

	@Override
	public Task getTaskById(int id) {
		Pair<Task, DispatcherQueue> entry = tasksById.get(id);
		if (entry == null) {
			return null;
		} else {
			return entry.getLeft();
		}
	}

	@Override
	public void removeTask(Task task) {
		Pair<Task, DispatcherQueue> val;
		synchronized (pool) {
			pool.get(task.getStatus()).remove(task);
			val = tasksById.remove(task.getId());
			tasksByServiceAndFacility.remove(new Pair(task.getExecService(),
					task.getFacility()));
		}
		taskManager.removeTask(task.getId(), val.getRight().getClientID());

	}

	@Override
	public Task getTask(ExecService execService, Facility facility) {
		return tasksByServiceAndFacility.get(new Pair<ExecService, Facility>(
				execService, facility));
	}

	@Override
	public DispatcherQueue getQueueForTask(Task task)
			throws InternalErrorException {
		Pair<Task, DispatcherQueue> entry = tasksById.get(task.getId());
		if (entry == null) {
			throw new InternalErrorException("no such task");
		}
		return entry.getRight();
	}

	@Override
	public void setTaskStatus(Task task, TaskStatus status) {
		TaskStatus old = task.getStatus();
		task.setStatus(status);
		// move task to the appropriate place
		if (!old.equals(status)) {
			pool.get(old).remove(task);
			pool.get(status).add(task);
		}
		taskManager.updateTask(task);
	}

	@Override
	public List<Task> getTasksForEngine(int clientID) {
		List<Task> result = new ArrayList<Task>();
		for (Pair<Task, DispatcherQueue> value : tasksById.values()) {
			if (clientID == value.getRight().getClientID()) {
				result.add(value.getLeft());
			}
		}
		return result;
	}

	@Override
	public List<Task> getWaitingTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.NONE));
	}

	@Override
	public List<Task> getDoneTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.DONE));
	}

	@Override
	public List<Task> getErrorTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.ERROR));
	}

	@Override
	public List<Task> getProcessingTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.PROCESSING));
	}

	@Override
	public List<Task> getPlannedTasks() {
		return new ArrayList<Task>(pool.get(TaskStatus.PLANNED));
	}

	@Override
	public void clear() {
		synchronized (tasksById) {
			tasksById.clear();
			tasksByServiceAndFacility.clear();
			for (TaskStatus status : TaskStatus.class.getEnumConstants()) {
				pool.get(status).clear();
			}
		}
		// taskManager.removeAllTasks();
	}

	@Override
	public void reloadTasks() {
		log.debug("Going to reload tasks from database...");
		this.clear();
		for (Pair<Task, Integer> pair : taskManager.listAllTasksAndClients()) {
			Task task = pair.getLeft();
			TaskStatus status = task.getStatus();
			if (status == null) {
				task.setStatus(TaskStatus.NONE);
			}
			if (!pool.get(task.getStatus()).contains(task.getId())) {
				pool.get(task.getStatus()).add(task);
			}
			// XXX should this be synchronized too?
			tasksById.put(
					task.getId(),
					new Pair<Task, DispatcherQueue>(task, dispatcherQueuePool
							.getDispatcherQueueByClient(pair.getRight())));
			tasksByServiceAndFacility.put(
					new Pair<ExecService, Facility>(task.getExecService(), task
							.getFacility()), task);
			// TODO: what about possible duplicates?
			log.debug("Added task " + task.toString());
		}
		log.info("Pool contains: ");
		for (TaskStatus status : TaskStatus.class.getEnumConstants()) {
			log.info("  - {} tasks in state {}", pool.get(status).size(),
					status.toString());
		}
	}

	@Override
	public void setQueueForTask(Task task, DispatcherQueue queueForTask) {
		tasksById.get(task.getId()).put(task, queueForTask);
	}

}
