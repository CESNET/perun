package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskStore;
import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.*;

@org.springframework.stereotype.Service(value = "schedulingPool")
public class SchedulingPoolImpl implements SchedulingPool {

	private final static Logger log = LoggerFactory.getLogger(SchedulingPoolImpl.class);
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

	@Override
	public String getReport() {
		return "Engine SchedulingPool Task report:\n" +
				" PLANNED: " + printListWithWhitespace(getTasksWithStatus(PLANNED)) +
				" GENERATING:" + printListWithWhitespace(getTasksWithStatus(GENERATING)) +
				" SENDING:" + printListWithWhitespace(getTasksWithStatus(SENDING,WARNING,SENDERROR)) +
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
			throw new IllegalArgumentException("Only Tasks with PLANNED status can be added to SchedulingPool.");
		}

		log.debug("[{}] Adding Task to scheduling pool: {}", task.getId(), task);
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
	public Integer addSendTaskCount(Task task, int count) {
		return sendTaskCount.put(task.getId(), count);
	}

	@Override
	public Integer decreaseSendTaskCount(Task task, int decrease) throws TaskStoreException {

		Integer count = sendTaskCount.get(task.getId());
		log.debug("[{}] Task SendTasks count is {}, state {}", task.getId(), count, task.getStatus());

		if (count == null) {
			return null;

		} else if (count <= 1) {

			if (!Objects.equals(task.getStatus(), SENDERROR) &&
					!Objects.equals(task.getStatus(), WARNING)) {
				task.setStatus(DONE);
			}
			if(task.getSendEndTime() == null) {
				task.setSendEndTime(LocalDateTime.now());
			}
			try {
				jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
			} catch (JMSException e) {
				log.error("[{}] Error while sending final status update for Task to Dispatcher", task.getId());
			}
			log.debug("[{}] Trying to remove Task from allTasks since its done ({})", task.getId(), task.getStatus());
			removeTask(task);
			return 1;
		} else {
			log.debug("[{}] Task SendTasks count lowered by {}", task.getId(), decrease);
			return sendTaskCount.replace(task.getId(), count - decrease);
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
	public Task removeTask(Task task) throws TaskStoreException {
		return removeTask(task.getId());
	}

	@Override
	public Task removeTask(int id) throws TaskStoreException {
		log.debug("[{}] Removing Task from scheduling pool.", id);
		Task removed = taskStore.removeTask(id);
		if (removed != null) {
			sendTaskCount.remove(id);
		} else {
			log.debug("[{}] Task was not in TaskStore (all tasks)", id);
		}
		return removed;
	}

	@Override
	public void clear() {
		taskStore.clear();
		sendTaskCount.clear();
		newTasksQueue.clear();
		generatedTasksQueue.clear();
	}

	private void handleInterruptedException(Task task, InterruptedException e) {
		String errorMessage = "Thread was interrupted while trying to put Task " + task + " into new Tasks queue.";
		log.error(errorMessage, e);
		throw new RuntimeException(errorMessage, e);
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
		taskResult.setStatus(returnCode == 0 ? 
				(stderr.isEmpty() ? TaskResult.TaskResultStatus.DONE : TaskResult.TaskResultStatus.WARNING) 
				: TaskResult.TaskResultStatus.ERROR);
		taskResult.setTimestamp(new Date(System.currentTimeMillis()));
		taskResult.setService(service);
		return taskResult;
	}

	private String printListWithWhitespace(List<Task> list) {

		if (list == null) return "[]";
		StringJoiner joiner = new StringJoiner(", ");
		for (Task task : list) {
			if (task != null) joiner.add(String.valueOf(task.getId()));
		}
		return "[" + joiner.toString() + "]";

	}

}
