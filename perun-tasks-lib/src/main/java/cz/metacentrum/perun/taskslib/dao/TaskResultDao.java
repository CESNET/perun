package cz.metacentrum.perun.taskslib.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.TaskResult;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
public interface TaskResultDao {

	/**
	 * List TaskResults
	 *
	 * @return all TaskResults
	 */
	List<TaskResult> getTaskResults();

	/**
	 * List newest TaskResults tied to a certain task
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId);

	/**
	 * List TaskResults tied to a certain task
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTask(int taskId);

	/**
	 * List newest TaskResults tied to a certain task and destination
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTaskAndDestination(int taskId, int destinationId);

	/**
	 * Get TaskResult by its ID
	 *
	 * @param taskResultId
	 * @return
	 */
	TaskResult getTaskResultById(int taskResultId);

	/**
	 * Clear all results tied to a particular Task
	 *
	 * @param taskId
	 * @return number of deleted TaskResults
	 */
	int clearByTask(int taskId);

	/**
	 * Clear all results
	 *
	 * @return number of deleted TaskResults
	 */
	int clearAll();

	int insertNewTaskResult(TaskResult taskResult, int engineID) throws InternalErrorException;

	List<TaskResult> getTaskResults(int engineID);

	TaskResult getTaskResultById(int taskResultId, int engineID);

	int clearByTask(int taskId, int engineID);

	int clearAll(int engineID);

	int clearOld(int engineID, int numDays) throws InternalErrorException;

	List<TaskResult> getTaskResultsByTask(int taskId, int engineID);

	/**
	 * Returns list of tasks results for defined destinations (string representation).
	 *
	 * @param destinationsNames
	 * @return list of tasks results
	 * @throws InternalErrorException
	 */
	List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames) throws InternalErrorException;
}
