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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of SchedulingPool.
 *
 * @author Michal Voců
 * @author Michal Babacek
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool
 */
@org.springframework.stereotype.Service("schedulingPool")
public class SchedulingPoolImpl implements SchedulingPool, InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(SchedulingPoolImpl.class);

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

  @Override
  public Task addTask(Task task) throws TaskStoreException {
    return taskStore.addTask(task);
  }

  /**
   * Adds Task and associated dispatcherQueue into scheduling pools internal maps and also to the database.
   *
   * @param task Task which will be added and persisted.
   * @return Number of Tasks in the pool.
   * @throws TaskStoreException
   */
  @Override
  public int addToPool(Task task) throws TaskStoreException {

    if (task.getId() == 0) {
      if (getTask(task.getFacility(), task.getService()) == null) {
        int id = tasksManagerBl.insertTask(sess, task);
        task.setId(id);
        LOG.debug("[{}] New Task stored in DB: {}", task.getId(), task);
      } else {
        Task existingTask = tasksManagerBl.getTaskById(sess, task.getId());
        if (existingTask == null) {
          int id = tasksManagerBl.insertTask(sess, task);
          task.setId(id);
          LOG.debug("[{}] New Task stored in DB: {}", task.getId(), task);
        } else {
          tasksManagerBl.updateTask(sess, task);
          LOG.debug("[{}] Task updated in the pool: {}", task.getId(), task);
        }
      }
    }
    addTask(task);
    LOG.debug("[{}] Task added to the pool: {}", task.getId(), task);
    return getSize();
  }

  @Override
  public void afterPropertiesSet() {
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
      LOG.error("Error establishing perun session to add task schedule: ", e1);
    }
  }

  @Override
  public void clear() {
    taskStore.clear();
    waitingTasksQueue.clear();
    waitingForcedTasksQueue.clear();
  }

  @Override
  public void closeTasksForEngine() {

    List<Task> tasks = taskStore.getTasksWithStatus(
        TaskStatus.PLANNED,
        TaskStatus.GENERATING,
        TaskStatus.GENERATED,
        TaskStatus.SENDING
    );

    // switch all processing tasks to error, remove the engine queue association
    LOG.debug("Switching processing tasks on engine to ERROR, the engine went down...");
    for (Task task : tasks) {
      LOG.info("[{}, {}] Switching Task to ERROR, the engine it was running on went down.", task.getId(),
          task.getRunId());
      task.setStatus(TaskStatus.ERROR);
    }

  }

  @Override
  public Collection<Task> getAllTasks() {
    return taskStore.getAllTasks();
  }

  public Properties getDispatcherProperties() {
    return dispatcherProperties;
  }

  public EngineMessageProducerFactory getEngineMessageProducerPool() {
    return engineMessageProducerFactory;
  }

  public Perun getPerun() {
    return perun;
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
               "  SENDERROR:  " + senderror +
               "  DONE: " + done +
               "  WARNING: " + warning +
               "  ERROR: " + error;
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

  public TaskStore getTaskStore() {
    return taskStore;
  }

  // --- session init ----------------------------------

  public TasksManagerBl getTasksManagerBl() {
    return tasksManagerBl;
  }

  // ----- methods -------------------------------------

  @Override
  public List<Task> getTasksWithStatus(TaskStatus... status) {
    return taskStore.getTasksWithStatus(status);
  }

  public DelayQueue<TaskSchedule> getWaitingForcedTasksQueue() {
    return waitingForcedTasksQueue;
  }

  public DelayQueue<TaskSchedule> getWaitingTasksQueue() {
    return waitingTasksQueue;
  }

  @Override
  public void onTaskDestinationComplete(String string) {

    if (string == null || string.isEmpty()) {
      LOG.error("Could not parse TaskResult message from Engine.");
      return;
    }

    try {
      List<PerunBean> listOfBeans = AuditParser.parseLog(string);
      if (!listOfBeans.isEmpty()) {
        TaskResult taskResult = (TaskResult) listOfBeans.get(0);
        LOG.debug("[{}, {}] Received TaskResult for Task from Engine.", taskResult.getTaskId(),
            taskResult.getTaskRunId());
        onTaskDestinationComplete(taskResult);
      } else {
        LOG.error("No TaskResult found in message from Engine: {}.", string);
      }
    } catch (Exception e) {
      LOG.error("Could not save TaskResult from Engine {}, {}", string, e.getMessage());
    }

  }

  @Override
  public void onTaskDestinationComplete(TaskResult taskResult) {
    try {
      tasksManagerBl.insertNewTaskResult(sess, taskResult);
    } catch (Exception e) {
      LOG.error("Could not save TaskResult from Engine, {}, {}", taskResult, e.getMessage());
    }
  }

  @Override
  public void onTaskStatusChange(int taskId, String status, String milliseconds) {

    Task task = getTask(taskId);
    if (task == null) {
      LOG.error("[{}] Received status update about Task which is not in Dispatcher anymore, will ignore it.", taskId);
      return;
    }

    TaskStatus oldStatus = task.getStatus();
    task.setStatus(TaskStatus.valueOf(status));
    long ms;
    try {
      ms = Long.valueOf(milliseconds);
    } catch (NumberFormatException e) {
      LOG.warn("[{}, {}] Timestamp of change '{}' could not be parsed, current time will be used instead.",
          task.getId(), task.getRunId(),
          milliseconds);
      ms = System.currentTimeMillis();
    }
    LocalDateTime changeDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());

    switch (task.getStatus()) {
      case WAITING:
      case PLANNED:
        LOG.error("[{}, {}] Received status change to {} from Engine, this should not happen.", task.getId(),
            task.getRunId(), task.getStatus());
        return;
      case GENERATING:
        task.setStartTime(changeDate);
        task.setGenStartTime(changeDate);
        break;
      case GENERROR:
        task.setEndTime(changeDate);
        break;
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
      default:
        break;
    }

    tasksManagerBl.updateTask(sess, task);

    LOG.debug("[{}, {}] Task status changed from {} to {} as reported by Engine: {}.", task.getId(),
        task.getRunId(), oldStatus,
        task.getStatus(), task);

  }

  @Override
  public void reloadTasks() {

    LOG.debug("Going to reload Tasks from database...");

    this.clear();

    EngineMessageProducer queue = engineMessageProducerFactory.getProducer();

    for (Task task : tasksManagerBl.listAllTasks(sess)) {
      try {
        // just add DB Task to in-memory structure
        addToPool(task);
      } catch (TaskStoreException e) {
        LOG.error("Adding Task {} and Queue {} into SchedulingPool failed, so the Task will be lost.", task, queue);
      }

      // if task was in any kind of processing state - reschedule now !!
      // done/error tasks will be rescheduled later by periodic jobs of PropagationMaintainer !!
      if (Arrays.asList(TaskStatus.WAITING, TaskStatus.PLANNED, TaskStatus.GENERATING, TaskStatus.SENDING)
              .contains(task.getStatus())) {
        if (task.getStatus().equals(TaskStatus.WAITING)) {
          // reset timestamp to 'now' for WAITING Tasks, since scheduling task
          // sets this to all tasks except the waiting
          task.setSchedule(LocalDateTime.now());
          tasksManagerBl.updateTask(sess, task);
        }
        scheduleTask(task, 0);
      }

    }

    LOG.debug("Reload of Tasks from database finished.");

  }

  @Override
  public Task removeTask(Task task) throws TaskStoreException {
    return taskStore.removeTask(task);
  }

  @Override
  public Task removeTask(int id, int runId) throws TaskStoreException {
    return taskStore.removeTask(id, runId);
  }

  @Override
  public void scheduleTask(Task task, int delayCount) {

    // check if service/facility exists

    boolean removeTask = false;

    try {
      Service service = perun.getServicesManager().getServiceById(sess, task.getServiceId());
      Facility facility = perun.getFacilitiesManager().getFacilityById(sess, task.getFacilityId());
      task.setService(service);
      task.setFacility(facility);
    } catch (ServiceNotExistsException e) {
      LOG.error("[{}] Task NOT added to waiting queue, service not exists: {}.", task.getId(), task);
      removeTask = true;
    } catch (FacilityNotExistsException e) {
      LOG.error("[{}] Task NOT added to waiting queue, facility not exists: {}.", task.getId(), task);
      removeTask = true;
    } catch (InternalErrorException | PrivilegeException e) {
      LOG.error("[{}] {}", task.getId(), e);
    }

    if (!task.getService().isEnabled() || ((PerunBl) perun).getServicesManagerBl()
                                              .isServiceBlockedOnFacility(task.getService(), task.getFacility())) {
      LOG.error("[{}] Task NOT added to waiting queue, service is blocked: {}.", task.getId(), task);
      // do not change Task status or any other data !
      if (!removeTask) {
        return;
      }
    }

    try {
      List<Destination> destinations =
          perun.getServicesManager().getDestinations(sess, task.getService(), task.getFacility());
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
          LOG.debug("[{}] Task NOT added to waiting queue, all its destinations are blocked.", task.getId());
          if (!removeTask) {
            return;
          }
        }
      } else {
        LOG.debug("[{}] Task NOT added to waiting queue, no destination exists.", task.getId());
        if (!removeTask) {
          return;
        }
      }

    } catch (ServiceNotExistsException e) {
      LOG.error("[{}] Task NOT added to waiting queue, service not exists: {}.", task.getId(), task);
      removeTask = true;
    } catch (FacilityNotExistsException e) {
      LOG.error("[{}] Task NOT added to waiting queue, facility not exists: {}.", task.getId(), task);
      removeTask = true;
    } catch (InternalErrorException | PrivilegeException e) {
      LOG.error("[{}] {}", task.getId(), e);
    }

    try {
      List<Service> assignedServices = perun.getServicesManager().getAssignedServices(sess, task.getFacility());
      if (!assignedServices.contains(task.getService())) {
        LOG.debug("[{}] Task NOT added to waiting queue, service is not assigned to facility any more: {}.",
            task.getId(), task);
        if (!removeTask) {
          return;
        }
      }
    } catch (FacilityNotExistsException e) {
      removeTask = true;
      LOG.error("[{}] Task removed from database, facility no longer exists: {}.", task.getId(), task);
    } catch (InternalErrorException | PrivilegeException e) {
      LOG.error("[{}] Unable to check Service assignment to Facility: {}", task.getId(), e.getMessage());
    }

    if (removeTask) {
      // in memory task belongs to non existent facility/service - remove it and return
      try {
        removeTask(task);
        return;
      } catch (TaskStoreException e) {
        LOG.error("[{}] Unable to remove Task from pool: {}.", task.getId(), e);
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
        LOG.warn("Could not parse value of dispatcher.task.delay.time property. Using default.");
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
        LOG.warn("Could not parse value of dispatcher.task.delay.count property. Using default.");
        delayCount = 4;
      }
    }
    // set propagation specific run id to correlate logs. Log if the task was in an ongoing propagation and was
    // rescheduled with different run id.
    int prevRunId = task.getRunId();
    task = tasksManagerBl.retrieveRunIdForTask(sess, task);
    TaskSchedule schedule = new TaskSchedule(newTaskDelay, task);
    if (prevRunId != 0) {
      LOG.debug("[{}] Task with previous run id: {} rescheduled with run id: {}", task.getId(),
          prevRunId, task.getRunId());
    }
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

      tasksManagerBl.updateTask(sess, task);

    }

    boolean added = false;

    if (schedule.getTask().isPropagationForced()) {
      added = waitingForcedTasksQueue.add(schedule);
    } else {
      added = waitingTasksQueue.add(schedule);
    }

    if (!added) {
      LOG.error(
          "[{}, {}] Task could not be added to waiting queue. Shouldn't ever happen. Look to javadoc of DelayQueue. {}",
          task.getId(), task.getRunId(), schedule);
    } else {
      LOG.debug("[{}, {}] Task was added to waiting queue: {}", task.getId(),
          task.getRunId(), schedule);
    }

  }

  @Resource(name = "dispatcherPropertiesBean")
  public void setDispatcherProperties(Properties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
  }

  @Autowired
  public void setEngineMessageProducerPool(EngineMessageProducerFactory engineMessageProducerPool) {
    this.engineMessageProducerFactory = engineMessageProducerPool;
  }

  @Autowired
  public void setPerun(Perun perun) {
    this.perun = perun;
  }

  @Autowired
  public void setTaskStore(TaskStore taskStore) {
    this.taskStore = taskStore;
  }

  @Autowired
  public void setTasksManagerBl(TasksManagerBl tasksManagerBl) {
    this.tasksManagerBl = tasksManagerBl;
  }

  @Autowired
  public void setWaitingForcedTasksQueue(DelayQueue<TaskSchedule> waitingForcedTasksQueue) {
    this.waitingForcedTasksQueue = waitingForcedTasksQueue;
  }

  @Autowired
  public void setWaitingTasksQueue(DelayQueue<TaskSchedule> waitingTasksQueue) {
    this.waitingTasksQueue = waitingTasksQueue;
  }

}
