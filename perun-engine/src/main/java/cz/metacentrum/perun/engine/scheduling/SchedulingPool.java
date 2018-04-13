package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskStore;

import java.util.concurrent.BlockingDeque;

/**
 * This class groups all Task queues from Engine, providing means to add new Tasks, cancel/remove present ones, etc.
 */
public interface SchedulingPool extends TaskStore {

	String getReport();

	/**
	 * Method used when SendTasks are being created from parent Task.
	 * It puts number of Task Destinations - created SendTasks, so Engine can
	 * count down when whole Task is completed.
	 *
	 * @param task Parent Task
	 * @param count Number of running SendTasks for a given Task.
	 * @return Value previously associated with given Task, null if there was none.
	 */
	Integer addSendTaskCount(Task task, int count);

	/**
	 * Decreases the count of SendTask running for given Task.
	 * Used when SendTasks finishes executing, so Engine can
	 * count down when whole Task is completed.
	 *
	 * Once count <=1 Task is removed from scheduling pool
	 * and status is reported to Dispatcher.
	 *
	 * @param task Parent Task
	 * @param decrease Number by which we reduce the count (usually one)
	 * @return Return value previously associated with given Task ID.
	 * @throws TaskStoreException Thrown if inconsistency occurred while saving the Task.
	 */
	Integer decreaseSendTaskCount(Task task, int decrease) throws TaskStoreException;

	BlockingDeque<Task> getNewTasksQueue();

	BlockingDeque<Task> getGeneratedTasksQueue();

	TaskResult createTaskResult (int taskId, int destinationId, String stderr, String stdout, int returnCode,
	                             Service service);

}
