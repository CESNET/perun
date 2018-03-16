package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.engine.scheduling.BlockingBoundedMap;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskStore;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.*;

@org.springframework.stereotype.Service(value = "schedulingPool")
public class SchedulingPoolImpl implements SchedulingPool {
	private final static Logger log = LoggerFactory.getLogger(SchedulingPoolImpl.class);
	private final ConcurrentMap<Integer, Future<Task>> genTaskFutures = new ConcurrentHashMap<>();
	private final ConcurrentMap<Integer, ConcurrentMap<Destination, Future<SendTask>>> sendTasks = new ConcurrentHashMap<>();
	private final ConcurrentMap<Integer, Integer> sendTaskCount = new ConcurrentHashMap<>();
	private final BlockingDeque<Task> newTasksQueue = new LinkedBlockingDeque<>();
	private final BlockingDeque<Task> generatedTasksQueue = new LinkedBlockingDeque<>();
	@Autowired
	private TaskStore taskStore;
	@Autowired
	private JMSQueueManager jmsQueueManager;

	public SchedulingPoolImpl() {
	}

	public SchedulingPoolImpl(TaskStore taskStore,  JMSQueueManager jmsQueueManager) {
		this.taskStore = taskStore;
		this.jmsQueueManager = jmsQueueManager;
	}

	public Future<Task> addGenTaskFutureToPool(Integer id, Future<Task> taskFuture) {
		return genTaskFutures.put(id, taskFuture);
	}

	@Override
	public Future<SendTask> addSendTaskFuture(SendTask sendTask, Future<SendTask> sendFuture) {
		ConcurrentMap<Destination, Future<SendTask>> sendTaskFutures = sendTasks.get(sendTask.getId().getLeft());
		if(sendTaskFutures == null) {
			sendTaskFutures = new ConcurrentHashMap<Destination, Future<SendTask>>();
			sendTasks.put(sendTask.getId().getLeft(), sendTaskFutures);
		}
		sendTaskFutures.putIfAbsent(sendTask.getDestination(), sendFuture);
		return sendFuture;
	}

	@Override
	public String getReport() {
		return "Engine SchedulingPool Task report:\n" +
				" PLANNED: " + getTasksWithStatus(WAITING) +
				" GENERATING:" + getTasksWithStatus(GENERATING) +
				" SENDING:" + getTasksWithStatus(SENDING) +
				" SENDTASKCOUNT map: " + sendTaskCount.toString();
	}

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

	/**
	 * Adds new Task to the SchedulingPool.
	 * Only newly received Tasks with PLANNED status can be added.
	 *
	 * @param task Task that will be added to the pool.
	 * @return Task that was added to the pool.
	 */
	public Task addTask(Task task) throws TaskStoreException {
		if (task.getStatus() != PLANNED) {
			throw new IllegalArgumentException("Only Tasks with PLANNED status can be added to SchedulingPool");
		}

		Task addedTask = taskStore.addTask(task);
		if (task.isPropagationForced()) {
			try {
				newTasksQueue.putFirst(task);
			} catch (InterruptedException e) {
				handleInterruptedException(task, e);
			}
		} else {
			try {
				newTasksQueue.put(task);
			} catch (InterruptedException e) {
				handleInterruptedException(task, e);
			}
		}
		return addedTask;
	}

	@Override
	public Collection<Task> getAllTasks() {
		return taskStore.getAllTasks();
	}

	@Override
	public List<Task> getTasksWithStatus(Task.TaskStatus... status) {
		return taskStore.getTasksWithStatus(status);
	}

	@Override
	public Integer addSendTaskCount(int taskId, int count) {
		return sendTaskCount.put(taskId, count);
	}

	@Override
	public Integer decreaseSendTaskCount(int taskId, int decrease) throws TaskStoreException {
		Integer count = sendTaskCount.get(taskId);
		if (count == null) {
			return null;
		} else if (count <= 1) {
			Task task = taskStore.getTask(taskId);
			if (task.getStatus() != SENDERROR) {
				task.setStatus(DONE);
			}
			if(task.getSendEndTime() == null) {
				task.setSendEndTime(new Date(System.currentTimeMillis()));
			}
			try {
				jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
			} catch (JMSException e) {
				log.error("Error while sending final status update for Task with ID {} to Dispatcher", taskId);
			}
			removeTask(taskId);
			return 1;
		} else {
			return sendTaskCount.replace(taskId, count - decrease);
		}
	}

	@Override
	public BlockingDeque<Task> getNewTasksQueue() {
		return newTasksQueue;
	}

	@Override
	public BlockingDeque<Task> getGeneratedTasksQueue() {
		return generatedTasksQueue;
	}

	@Override
	public ConcurrentMap<Integer, Future<Task>> getGenTaskFuturesMap() {
		return genTaskFutures;
	}

	@Override
	public Future<Task> getGenTaskFutureById(int id) {
		return genTaskFutures.get(id);
	}

	@Override
	public Task removeTask(Task task) throws TaskStoreException {
		return removeTask(task.getId());
	}

	@Override
	public Task removeTask(int id) throws TaskStoreException {
		Task removed = taskStore.removeTask(id);
		Future<Task> taskFuture = genTaskFutures.get(id);
		if (taskFuture != null) {
			taskFuture.cancel(true);
		}
		if (removed != null) {
			cancelSendTasks(id);
			sendTaskCount.remove(id);
		}
		return removed;
	}

	@Override
	public void clear() {
		taskStore.clear();
		genTaskFutures.clear();
		sendTasks.clear();
		sendTaskCount.clear();
		newTasksQueue.clear();
		generatedTasksQueue.clear();
	}

	public Future<SendTask> removeSendTaskFuture(int taskId, Destination destination) throws TaskStoreException {
		ConcurrentMap<Destination, Future<SendTask>> destinationSendTasks = sendTasks.get(taskId);
		if (destinationSendTasks != null) {
			Future<SendTask> removed = destinationSendTasks.remove(destination);
			if (removed != null) {
				decreaseSendTaskCount(taskId, 1);
			}
			return removed;
		} else {
			return null;
		}
	}

	private void handleInterruptedException(Task task, InterruptedException e) {
		String errorMessage = "Thread was interrupted while trying to put Task " + task + " into new Tasks queue.";
		log.error(errorMessage, e);
		throw new RuntimeException(errorMessage, e);
	}

	private void cancelSendTasks(int taskId) {
		//TODO: If SendPlanner is currently planning the Task, we may not cancel all sendTasks
		ConcurrentMap<Destination, Future<SendTask>> futureSendTasks = sendTasks.get(taskId);
		if (futureSendTasks == null) {
			return;
		}
		for (Future<SendTask> sendTaskFuture : futureSendTasks.values()) {
			//TODO: Set the with interrupt parameter to true or not?
			sendTaskFuture.cancel(true);
		}
	}

	// TODO this does not belong here, move it somewhere else
	@Override
	public TaskResult createTaskResult (int taskId, int destinationId, String stderr, String stdout, int returnCode,
	                                    Service service) {
		TaskResult taskResult = new TaskResult();
		taskResult.setTaskId(taskId);
		taskResult.setDestinationId(destinationId);
		taskResult.setErrorMessage(stderr);
		taskResult.setStandardMessage(stdout);
		taskResult.setReturnCode(returnCode);
		taskResult.setStatus(returnCode == 0 ? TaskResult.TaskResultStatus.DONE : TaskResult.TaskResultStatus.ERROR);
		taskResult.setTimestamp(new Date(System.currentTimeMillis()));
		taskResult.setService(service);
		return taskResult;
	}
}
