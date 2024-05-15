package cz.metacentrum.perun.engine.scheduling.impl;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.DONE;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERATING;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PLANNED;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.SENDERROR;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.SENDING;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.WARNING;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskStore;
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
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service(value = "schedulingPool")
public class SchedulingPoolImpl implements SchedulingPool {

  private static final Logger LOG = LoggerFactory.getLogger(SchedulingPoolImpl.class);
  private final ConcurrentMap<Integer, Integer> sendTaskCount = new ConcurrentHashMap<>();
  private final BlockingDeque<Task> newTasksQueue = new LinkedBlockingDeque<>();
  private final BlockingDeque<Task> generatedTasksQueue = new LinkedBlockingDeque<>();
  @Autowired
  private TaskStore taskStore;
  @Autowired
  private JMSQueueManager jmsQueueManager;

  public SchedulingPoolImpl() {
  }

  public SchedulingPoolImpl(TaskStore taskStore, JMSQueueManager jmsQueueManager) {
    this.taskStore = taskStore;
    this.jmsQueueManager = jmsQueueManager;
  }

  @Override
  public Integer addSendTaskCount(Task task, int count) {
    return sendTaskCount.put(task.getId(), count);
  }

  /**
   * Adds new Task to the SchedulingPool. Only newly received Tasks with PLANNED status can be added.
   *
   * @param task Task that will be added to the pool.
   * @return Task that was added to the pool.
   */
  public Task addTask(Task task) throws TaskStoreException {
    if (task.getStatus() != PLANNED) {
      throw new IllegalArgumentException("Only Tasks with PLANNED status can be added to SchedulingPool.");
    }

    LOG.debug("[{}, {}] Adding Task to scheduling pool: {}", task.getId(), task.getRunId(), task);
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
  public void clear() {
    taskStore.clear();
    sendTaskCount.clear();
    newTasksQueue.clear();
    generatedTasksQueue.clear();
  }

  // TODO this does not belong here, move it somewhere else
  @Override
  public TaskResult createTaskResult(int taskId, int taskRunId, int destinationId, String stderr, String stdout,
                                     int returnCode,
                                     Service service) {
    TaskResult taskResult = new TaskResult();
    taskResult.setTaskId(taskId);
    taskResult.setTaskRunId(taskRunId);
    taskResult.setDestinationId(destinationId);
    taskResult.setErrorMessage(stderr);
    taskResult.setStandardMessage(stdout);
    taskResult.setReturnCode(returnCode);
    taskResult.setStatus(returnCode == 0 ? ((stderr == null || stderr.isEmpty()) ? TaskResult.TaskResultStatus.DONE :
        TaskResult.TaskResultStatus.WARNING) : TaskResult.TaskResultStatus.ERROR);
    taskResult.setTimestamp(new Date(System.currentTimeMillis()));
    taskResult.setService(service);
    return taskResult;
  }

  @Override
  public Integer decreaseSendTaskCount(Task task, int decrease) throws TaskStoreException {

    Integer count = sendTaskCount.get(task.getId());
    LOG.debug("[{}, {}] Task SendTasks count is {}, state {}", task.getId(), task.getRunId(), count, task.getStatus());

    if (count == null) {
      return null;

    } else if (count <= 1) {

      if (!Objects.equals(task.getStatus(), SENDERROR) && !Objects.equals(task.getStatus(), WARNING)) {
        task.setStatus(DONE);
      }
      if (task.getSendEndTime() == null) {
        task.setSendEndTime(LocalDateTime.now());
      }
      try {
        jmsQueueManager.reportTaskStatus(task, task.getStatus(), System.currentTimeMillis());
      } catch (JMSException | InterruptedException e) {
        LOG.error("[{}, {}] Error while sending final status update for Task to Dispatcher", task.getId(),
            task.getRunId());
      }
      LOG.debug("[{}, {}] Trying to remove Task from allTasks since its done ({})", task.getId(),
          task.getRunId(), task.getStatus());
      removeTask(task);
      return 1;
    } else {
      LOG.debug("[{}, {}] Task SendTasks count lowered by {}", task.getId(), task.getRunId(), decrease);
      return sendTaskCount.replace(task.getId(), count - decrease);
    }
  }

  @Override
  public Collection<Task> getAllTasks() {
    return taskStore.getAllTasks();
  }

  @Override
  public BlockingDeque<Task> getGeneratedTasksQueue() {
    return generatedTasksQueue;
  }

  @Override
  public BlockingDeque<Task> getNewTasksQueue() {
    return newTasksQueue;
  }

  @Override
  public String getReport() {
    return "Engine SchedulingPool Task report:\n" + " PLANNED: " +
           printListWithWhitespace(getTasksWithStatus(PLANNED)) + " GENERATING:" +
           printListWithWhitespace(getTasksWithStatus(GENERATING)) + " SENDING:" +
           printListWithWhitespace(getTasksWithStatus(SENDING, WARNING, SENDERROR)) + " SENDTASKCOUNT map: " +
           sendTaskCount.toString();
  }

  @Override
  public int getSize() {
    return taskStore.getSize();
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
  public List<Task> getTasksWithStatus(Task.TaskStatus... status) {
    return taskStore.getTasksWithStatus(status);
  }

  private void handleInterruptedException(Task task, InterruptedException e) {
    String errorMessage = "Thread was interrupted while trying to put Task " + task + " into new Tasks queue.";
    LOG.error(errorMessage, e);
    throw new RuntimeException(errorMessage, e);
  }

  private String printListWithWhitespace(List<Task> list) {

    if (list == null) {
      return "[]";
    }
    StringJoiner joiner = new StringJoiner(", ");
    for (Task task : list) {
      if (task != null) {
        joiner.add(String.valueOf(task.getId()));
      }
    }
    return "[" + joiner.toString() + "]";

  }

  @Override
  public Task removeTask(Task task) throws TaskStoreException {
    return removeTask(task.getId(), task.getRunId());
  }

  @Override
  public Task removeTask(int id, int runId) throws TaskStoreException {
    LOG.debug("[{}, {}] Removing Task from scheduling pool.", id, runId);
    Task removed = taskStore.removeTask(id, runId);
    if (removed != null) {
      sendTaskCount.remove(id);
    } else {
      LOG.debug("[{}, {}] Task was not in TaskStore (all tasks)", id, runId);
    }
    return removed;
  }

}
