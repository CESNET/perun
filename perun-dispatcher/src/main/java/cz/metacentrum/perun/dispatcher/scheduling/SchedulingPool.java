package cz.metacentrum.perun.dispatcher.scheduling;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskStore;

import java.util.List;

/**
 * In-memory pool of all Tasks. On application start, all Tasks are reloaded from DB.
 *
 * New Tasks are added by EventProcessor, existing Tasks are updated.
 *
 * Tasks can be then pushed to waitingTasksQueue by EventProcessor (new Task), TaskScheduler or PropagationMaintainer.
 *
 * Allows association of Tasks with Engines (EngineMessageProducer queues).
 *
 * @see cz.metacentrum.perun.dispatcher.processing.EventProcessor
 * @see cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler
 * @see cz.metacentrum.perun.dispatcher.scheduling.PropagationMaintainer
 * @see cz.metacentrum.perun.taskslib.model.Task
 *
 * @author Michal Voců
 * @author Michal Babacek
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface SchedulingPool extends TaskStore {

	/**
	 * Add Task to DB and internal scheduling pool.
	 *
	 * @param task Task to be added
	 * @return Current size of pool after adding
	 * @throws InternalErrorException When implementation fails.
	 * @throws TaskStoreException When Task can't be added.
	 */
	int addToPool(Task task) throws InternalErrorException, TaskStoreException;

	/**
	 * Adds supplied Task into DelayQueue and reset its source updated flag to false if Task is eligible for running.
	 *
	 * Forced Tasks will have delay set to 0, other will use system property: "dispatcher.task.delay.time"
	 * Also forced Tasks will have delayCount set to 0.
	 *
	 * Always retrieve Service/Facility from DB to cross-check actual data.
	 * Check if Service/Facility exists and has connection and is not blocked.
	 *
	 * If check fails, Task is not scheduled.
	 * If passes, status is changed to WAITING and timestamps are re-set.
	 * If Task was already in WAITING, timestamps are kept (so we could tell, when it was scheduled first time).
	 *
	 * @param task Task to schedule propagation for
	 * @param delayCount How long to wait before sending to engine
	 */
	void scheduleTask(Task task, int delayCount);

	/**
	 * Loads Tasks persisted in the database into internal scheduling pool maps.
	 * Immediately restart propagation of previously processing Tasks.
	 * Error and Done Tasks might be reschedule later by PropagationMaintainer.
	 */
	void reloadTasks();

	/**
	 * Clear all in-memory state of Tasks. Called during reloading of Tasks from DB.
	 */
	void clear();

	/**
	 * Return string representation of pool content like "TaskStatus = tasks count" for each TaskStatus.
	 *
	 * @return String representation of pool content
	 */
	String getReport();

	/**
	 * Switch all processing Tasks to ERROR if engine was restarted.
	 *
	 */
	void closeTasksForEngine();

	/**
	 * Store change in Task status sent from Engine.
	 *
	 * @param taskId ID of Task to update
	 * @param status TaskStatus to set
	 * @param date Timestamp of change (string)
	 */
	void onTaskStatusChange(int taskId, String status, String date);

	/**
	 * Store TaskResult sent from Engine.
	 *
	 * @param string Serialized TaskResult object
	 */
	void onTaskDestinationComplete(String string);

	/**
	 * Store TaskResult sent from Engine.
	 *
	 * @param taskResult TaskResult object
	 */
	void onTaskDestinationComplete(TaskResult taskResult);

}
