package cz.metacentrum.perun.dispatcher.scheduling;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Ensure re-scheduling of DONE/ERROR Tasks, handle stuck Tasks.
 *
 * @author Michal Karm Babacek
 * @author Michalů Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "propagationMaintainer")
public class PropagationMaintainer extends AbstractRunner {

  private static final Logger LOG = LoggerFactory.getLogger(PropagationMaintainer.class);

  /**
   * After how many minutes is processing Task considered as stuck and re-scheduled. Should be above same property for
   * "Engine", which is by default 180.
   */
  private int rescheduleTime = 190;

  private PerunSession perunSession;

  private Perun perun;
  private SchedulingPool schedulingPool;
  private Properties dispatcherProperties;
  private int oldRescheduleHours;

  // ----- setters -------------------------------------

  /**
   * Check all Tasks in waiting, planned or any of processing states and check if have been running for too long.
   */
  private void endStuckTasks() {

    LOG.info("Checking WAITING, PLANNED and PROCESSING tasks...");

    List<Task> suspiciousTasks =
        schedulingPool.getTasksWithStatus(TaskStatus.WAITING, TaskStatus.PLANNED, TaskStatus.GENERATING,
            TaskStatus.GENERATED, TaskStatus.SENDING);

    for (Task task : suspiciousTasks) {

      LocalDateTime soonerTimestamp;
      LocalDateTime laterTimestamp;

      // fill expected timestamps per state
      if (task.getStatus().equals(TaskStatus.WAITING) || task.getStatus().equals(TaskStatus.PLANNED)) {
        soonerTimestamp = task.getSchedule();
        laterTimestamp = task.getSentToEngine();
      } else {
        soonerTimestamp = task.getGenStartTime();
        laterTimestamp = task.getSendStartTime();
      }

      if (soonerTimestamp == null && laterTimestamp == null) {
        LOG.error("[{}, {}] Task presumably in {} state, but does not have a valid timestamps. Switching to ERROR: {}.",
            task.getId(), task.getRunId(), task.getStatus(), task);
        task.setEndTime(LocalDateTime.now());
        task.setStatus(TaskStatus.ERROR);
        ((PerunBl) perun).getTasksManagerBl().updateTask(perunSession, task);
        continue;
      }

      // count how many minutes the task stays in one state

      long howManyMinutesAgo =
          ChronoUnit.MINUTES.between((laterTimestamp == null ? soonerTimestamp : laterTimestamp), LocalDateTime.now());

      // If too much time has passed something is broken
      if (howManyMinutesAgo >= rescheduleTime) {
        LOG.error("[{}, {}] Task is stuck in {} state for more than {} minutes. Switching it to ERROR: {}.",
            task.getId(), task.getRunId(),
            task.getStatus(), rescheduleTime, task);
        task.setEndTime(LocalDateTime.now());
        task.setStatus(TaskStatus.ERROR);
        ((PerunBl) perun).getTasksManagerBl().updateTask(perunSession, task);
      }

    }

  }

  public Properties getDispatcherProperties() {
    return dispatcherProperties;
  }

  public Perun getPerun() {
    return perun;
  }

  public SchedulingPool getSchedulingPool() {
    return schedulingPool;
  }

  /**
   * Reschedule Tasks in DONE/WARNING state if their - source was updated - OR haven't run for X hours - OR have no end
   * time set Reschedule also WAITING tasks (only when their source was updated).
   */
  private void rescheduleDoneTasks() {

    // Reschedule tasks in DONE that haven't been running for quite a while
    LOG.info("Checking DONE/WARNING/WAITING tasks...");

    for (Task task : schedulingPool.getTasksWithStatus(TaskStatus.DONE, TaskStatus.WARNING, TaskStatus.WAITING)) {

      LocalDateTime tooManyHoursAgo = LocalDateTime.now().minusHours(oldRescheduleHours);

      if (task.isSourceUpdated()) {
        // source data has changed - re-schedule task
        LOG.info("[{}, {}] Task in {} state will be rescheduled, source data changed.", task.getId(),
            task.getRunId(), task.getStatus());
        schedulingPool.scheduleTask(task, -1);
      } else {
        // data hasn't changed => check if its not too old
        if (task.getEndTime() == null || task.getEndTime().isBefore(tooManyHoursAgo)) {
          // don't re-schedule waiting tasks, since their 'end time' is always NULL
          // they will get re-scheduled if stuck by endStuckTasks()
          if (!TaskStatus.WAITING.equals(task.getStatus())) {
            LOG.info("[{}, {}] Task in {} state will be rescheduled, hasn't run for {} hours.", task.getId(),
                task.getRunId(),
                task.getStatus(), oldRescheduleHours);
            schedulingPool.scheduleTask(task, -1);
          }
        } else {
          LOG.trace("[{}, {}] Task has finished recently or source data hasn't changed, leaving it for now.",
              task.getId(), task.getRunId());
        }

      }

    }
  }

  /**
   * Reschedule Tasks in ERROR if their - source was updated - OR recurrence is <= default recurrence (2) and ended time
   * (minutes) >= delay*(recurrence+1)
   */
  private void rescheduleErrorTasks() {

    LOG.info("Checking ERROR tasks...");

    for (Task task : schedulingPool.getTasksWithStatus(TaskStatus.ERROR, TaskStatus.GENERROR, TaskStatus.SENDERROR)) {

      // error tasks should have correct end time
      if (task.getEndTime() == null) {
        LOG.error("[{}, {}] RECOVERY FROM INCONSISTENT STATE: ERROR task does not have end_time! " +
                  "Setting end_time to task.getDelay + 1.", task.getId(), task.getRunId());
        // getDelay is in minutes
        LocalDateTime endTime = LocalDateTime.now().minusMinutes(task.getDelay() + 1);
        task.setEndTime(endTime);
      }

      long howManyMinutesAgo = ChronoUnit.MINUTES.between(task.getEndTime(), LocalDateTime.now());

      if (howManyMinutesAgo < 0) {
        LOG.error("[{}, {}] RECOVERY FROM INCONSISTENT STATE: ERROR task appears to have ended in future.",
            task.getId(), task.getRunId());
        LocalDateTime endTime = LocalDateTime.now().minusMinutes(task.getDelay() + 1);
        task.setEndTime(endTime);
        howManyMinutesAgo = task.getDelay() + 1;
      }

      LOG.trace("[{}, {}] Task in ERROR state completed {} minutes ago: {}.", task.getId(), task.getRunId(),
          howManyMinutesAgo,
          task);

      // If DELAY time has passed, we reschedule...
      int recurrence = task.getRecurrence() + 1;
      LocalDateTime tooManyHoursAgo = LocalDateTime.now().minusHours(oldRescheduleHours);

      if (task.isSourceUpdated()) {

        // schedule if possible and reset source updated flag
        LOG.info("[{}, {}] Task in {} state will be rescheduled, source data changed.", task.getId(),
            task.getRunId(), task.getStatus());
        schedulingPool.scheduleTask(task, -1);

      } else if (howManyMinutesAgo >= recurrence * task.getDelay() && recurrence <= task.getService().getRecurrence()) {

        // within recurrence, ended more than (recurrence*delay) ago
        // increase recurrence counter if data hasn't changed
        task.setRecurrence(recurrence);

        // schedule if possible and reset source updated flag
        LOG.info("[{}, {}] Task in {} state will be rescheduled, attempt #{}.", task.getId(), task.getRunId(),
            task.getStatus(),
            recurrence);
        schedulingPool.scheduleTask(task, -1);

      } else if (task.getEndTime().isBefore(tooManyHoursAgo)) {

        LOG.info("[{}, {}] Task in {} state will be rescheduled, hasn't run for {} hours.", task.getId(),
            task.getRunId(),
            task.getStatus(),
            oldRescheduleHours);
        // reset recurrence since we must have exceeded it
        task.setRecurrence(0);
        // schedule if possible and reset source updated flag
        schedulingPool.scheduleTask(task, -1);

      }

    }
  }

  // ----- methods -------------------------------------

  /**
   * This method runs in own thread as periodic job which:
   * <p>
   * takes DONE/WARNING Tasks and reschedule them if source data were updated or Task hasn't run for X hours from the
   * last time. takes ERROR Tasks and reschedule them if -- || -- or (end time + (delay * recurrence)) > now takes
   * PROCESSING Tasks and switch them to error if we haven`t heard about result for more than 3 hours.
   */
  @Override
  public void run() {

    try {
      if (perunSession == null) {
        perunSession = perun.getPerunSession(
            new PerunPrincipal(dispatcherProperties.getProperty("perun.principal.name"),
                dispatcherProperties.getProperty("perun.principal.extSourceName"),
                dispatcherProperties.getProperty("perun.principal.extSourceType")), new PerunClient());
      }
    } catch (InternalErrorException e1) {
      LOG.error("Error establishing perun session to check tasks propagation status: ", e1);
      return;
    }

    while (!shouldStop()) {

      rescheduleDoneTasks();

      rescheduleErrorTasks();

      endStuckTasks();

      try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        LOG.error("Error in PropagationMaintainer", ex);
        throw new RuntimeException("Somebody has interrupted us...", ex);
      }

    }

    LOG.debug("PropagationMaintainer has stopped.");

  }

  @Resource(name = "dispatcherPropertiesBean")
  public void setDispatcherProperties(Properties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
    if (dispatcherProperties != null) {
      try {
        rescheduleTime = Integer.parseInt(dispatcherProperties.getProperty("dispatcher.propagation.timeout", "190"));
      } catch (NumberFormatException ex) {
        rescheduleTime = 190;
      }
      try {
        oldRescheduleHours = Integer.parseInt(dispatcherProperties.getProperty("dispatcher.rescheduleInterval", "48"));
      } catch (NumberFormatException ex) {
        oldRescheduleHours = 48;
      }
    }
  }

  @Autowired
  public void setPerun(Perun perun) {
    this.perun = perun;
  }

  @Autowired
  public void setSchedulingPool(SchedulingPool schedulingPool) {
    this.schedulingPool = schedulingPool;
  }

}
