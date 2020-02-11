package cz.metacentrum.perun.dispatcher.scheduling;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

/**
 * Ensure re-scheduling of DONE/ERROR Tasks, handle stuck Tasks.
 *
 * @author Michal Karm Babacek
 * @author Michalů Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "propagationMaintainer")
public class PropagationMaintainer extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(PropagationMaintainer.class);

	/**
	 * After how many minutes is processing Task considered as stuck and re-scheduled.
	 * Should be above same property for "Engine", which is by default 180.
	 */
	private int rescheduleTime = 190;

	private PerunSession perunSession;

	private Perun perun;
	private SchedulingPool schedulingPool;
	private Properties dispatcherProperties;

	// ----- setters -------------------------------------

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	@Autowired
	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public Perun getPerun() {
		return perun;
	}

	@Autowired
	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
		if (dispatcherProperties != null) {
			try {
				rescheduleTime = Integer.parseInt(dispatcherProperties.getProperty("dispatcher.propagation.timeout", "190"));
			} catch (NumberFormatException ex) {
				rescheduleTime = 190;
			}
		}
	}

	// ----- methods -------------------------------------

	/**
	 * This method runs in own thread as periodic job which:
	 *
	 * takes DONE Tasks and reschedule them if source data were updated.
	 * takes ERROR Tasks and reschedule them if -- || -- or (end time + (delay * recurrence)) > now
	 * takes PROCESSING Tasks and switch them to error if we haven`t heard about result for more than 3 hours.
	 */
	@Override
	public void run() {

		try {
			if (perunSession == null) {
				perunSession = perun.getPerunSession(new PerunPrincipal(
								dispatcherProperties.getProperty("perun.principal.name"),
								dispatcherProperties.getProperty("perun.principal.extSourceName"),
								dispatcherProperties.getProperty("perun.principal.extSourceType")),
						new PerunClient());
			}
		} catch (InternalErrorException e1) {
			log.error("Error establishing perun session to check tasks propagation status: ", e1);
			return;
		}

		while(!shouldStop()) {

			rescheduleDoneTasks();

			rescheduleErrorTasks();

			endStuckTasks();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				log.error("Error in PropagationMaintainer: {}", ex);
				throw new RuntimeException("Somebody has interrupted us...", ex);
			}

		}

		log.debug("PropagationMaintainer has stopped.");

	}


	/**
	 * Reschedule Tasks in DONE if their
	 * - source was updated
	 * - OR haven't run for 2 days
	 * - or have no end time set
	 */
	private void rescheduleDoneTasks() {

		// Reschedule tasks in DONE that haven't been running for quite a while
		log.info("Checking DONE tasks...");

		for (Task task : schedulingPool.getTasksWithStatus(TaskStatus.DONE)) {

			LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

			if (task.isSourceUpdated()) {
				log.info("[{}] Task in {} state will be rescheduled, source data changed.", task.getId(), task.getStatus());
				schedulingPool.scheduleTask(task, -1);
			} else if (task.getEndTime() == null || task.getEndTime().isBefore(twoDaysAgo)) {
				log.info("[{}] Task in {} state will be rescheduled, hasn't run for 2 days.", task.getId(), task.getStatus());
				schedulingPool.scheduleTask(task, -1);
			} else {
				log.trace("[{}] Task has finished recently or source data hasn't changed, leaving it for now.", task.getId());
			}

		}
	}

	/**
	 * Reschedule Tasks in ERROR if their
	 * - source was updated
	 * - OR recurrence is <= default recurrence (2) and ended time (minutes) >= delay*(recurrence+1)
	 */
	private void rescheduleErrorTasks() {

		log.info("Checking ERROR tasks...");

		for (Task task : schedulingPool.getTasksWithStatus(TaskStatus.ERROR, TaskStatus.GENERROR, TaskStatus.SENDERROR)) {

			// error tasks should have correct end time
			if (task.getEndTime() == null) {
				log.error("[{}] RECOVERY FROM INCONSISTENT STATE: ERROR task does not have end_time! " +
						"Setting end_time to task.getDelay + 1.", task.getId());
				// getDelay is in minutes
				LocalDateTime endTime = LocalDateTime.now().minusMinutes(task.getDelay() + 1);
				task.setEndTime(endTime);
			}

			long howManyMinutesAgo = ChronoUnit.MINUTES.between(task.getEndTime(), LocalDateTime.now());

			if (howManyMinutesAgo < 0) {
				log.error("[{}] RECOVERY FROM INCONSISTENT STATE: ERROR task appears to have ended in future.", task.getId());
				LocalDateTime endTime = LocalDateTime.now().minusMinutes(task.getDelay() + 1);
				task.setEndTime(endTime);
				howManyMinutesAgo = task.getDelay() + 1;
			}

			log.trace("[{}] Task in ERROR state completed {} minutes ago: {}.", new Object[]{task.getId(), howManyMinutesAgo, task});

			// If DELAY time has passed, we reschedule...
			int recurrence = task.getRecurrence() + 1;
			LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

			if (task.isSourceUpdated()) {

				// schedule if possible and reset source updated flag
				log.info("[{}] Task in {} state will be rescheduled, source data changed.", task.getId(), task.getStatus());
				schedulingPool.scheduleTask(task, -1);

			} else if (howManyMinutesAgo >= recurrence * task.getDelay() && recurrence <= task.getService().getRecurrence()) {

				// within recurrence, ended more than (recurrence*delay) ago
				// increase recurrence counter if data hasn't changed
				task.setRecurrence(recurrence);

				// schedule if possible and reset source updated flag
				log.info("[{}] Task in {} state will be rescheduled, attempt #{}.", task.getId(), task.getStatus(), recurrence);
				schedulingPool.scheduleTask(task, -1);

			} else if (task.getEndTime().isBefore(twoDaysAgo)) {

				log.info("[{}] Task in {} state will be rescheduled, hasn't run for 2 days.", task.getId(), task.getStatus());
				// reset recurrence since we must have exceeded it
				task.setRecurrence(0);
				// schedule if possible and reset source updated flag
				schedulingPool.scheduleTask(task, -1);

			}

		}
	}

	/**
	 * Check all Tasks in waiting, planned or any of processing states and check if have been running for too long.
	 */
	private void endStuckTasks() {

		log.info("Checking WAITING, PLANNED and PROCESSING tasks...");

		List<Task> suspiciousTasks = schedulingPool.getTasksWithStatus(TaskStatus.WAITING, TaskStatus.PLANNED,
				TaskStatus.GENERATING, TaskStatus.GENERATED, TaskStatus.SENDING);

		for (Task task : suspiciousTasks) {

			LocalDateTime soonerTimestamp;
			LocalDateTime laterTimestamp;

			// fill expected timestamps per state
			if (task.getStatus().equals(TaskStatus.WAITING) || task.getStatus().equals(TaskStatus.PLANNED)) {
				soonerTimestamp = task.getSchedule();
				laterTimestamp = task.getSentToEngine();
			} else {
				soonerTimestamp= task.getGenStartTime();
				laterTimestamp = task.getSendStartTime();
			}

			if (soonerTimestamp == null && laterTimestamp == null) {
				log.error("[{}] Task presumably in {} state, but does not have a valid timestamps. Switching to ERROR: {}.",
						new Object[]{task.getId(), task.getStatus(), task});
				task.setEndTime(LocalDateTime.now());
				task.setStatus(TaskStatus.ERROR);
				((PerunBl) perun).getTasksManagerBl().updateTask(task);
				continue;
			}

			// count how many minutes the task stays in one state

			long howManyMinutesAgo = ChronoUnit.MINUTES.between((laterTimestamp == null ? soonerTimestamp : laterTimestamp), LocalDateTime.now());

			// If too much time has passed something is broken
			if (howManyMinutesAgo >= rescheduleTime) {
				log.error("[{}] Task is stuck in {} state for more than {} minutes. Switching it to ERROR: {}.",
						new Object[]{task.getId(), task.getStatus(), rescheduleTime, task});
				task.setEndTime(LocalDateTime.now());
				task.setStatus(TaskStatus.ERROR);
				((PerunBl) perun).getTasksManagerBl().updateTask(task);
			}

		}

	}

}
