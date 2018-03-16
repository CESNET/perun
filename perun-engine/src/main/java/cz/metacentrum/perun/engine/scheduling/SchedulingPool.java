package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskStore;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

/**
 * This class groups all Task queues from Engine, providing means to add new Tasks, cancel/remove present ones, etc.
 */
public interface SchedulingPool extends TaskStore {

	Future<Task> addGenTaskFutureToPool(Integer id, Future<Task> taskFuture);

	Future<SendTask> addSendTaskFuture(SendTask sendTask, Future<SendTask> sendFuture);
	
	String getReport();

	/**
	 * Method used when SendTasks are being created from parent Task.
	 * It puts number of SendTasks still running under the Tasks id.
	 * @param taskId Id of the Task which count we add.
	 * @param count Number of running sendTasks for a given Task.
	 * @return Value previously associated with given Task's ID, null if there was none.
	 */
	Integer addSendTaskCount(int taskId, int count);

	/**
	 * Decreases the count of SendTask running for given Task.
	 * Used when SendTasks finishes executing.
	 * @param taskId Id of the Task whose SendTask/s finished.
	 * @param decrease Number by which we reduce the count.
	 * @return Return value previously associated with given Task ID.
	 * @throws TaskStoreException Thrown if inconsistency occurred while saving the Task.
	 */
	Integer decreaseSendTaskCount(int taskId, int decrease) throws TaskStoreException;

	BlockingDeque<Task> getNewTasksQueue();

	BlockingDeque<Task> getGeneratedTasksQueue();

	ConcurrentMap<Integer, Future<Task>> getGenTaskFuturesMap();

	Future<Task> getGenTaskFutureById(int id);

	Future<SendTask> removeSendTaskFuture(int taskId, Destination destination) throws TaskStoreException;

	TaskResult createTaskResult (int taskId, int destinationId, String stderr, String stdout, int returnCode,
	                             Service service);

}
