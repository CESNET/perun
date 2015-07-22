package cz.metacentrum.perun.taskslib.service;

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
public interface ResultManager {

	List<TaskResult> getTaskResults();

	List<TaskResult> getTaskResultsByTask(int taskId);

	TaskResult getTaskResultById(int taskResultId);

	int clearByTask(int taskId);

	int clearAll();

	int insertNewTaskResult(TaskResult taskResult, int engineID) throws InternalErrorException;

	List<TaskResult> getTaskResults(int engineID);

	TaskResult getTaskResultById(int taskResultId, int engineID);

	int clearByTask(int taskId, int engineID);

	int clearAll(int engineID);

	int clearOld(int engineID, int numDays) throws InternalErrorException;

	List<TaskResult> getTaskResultsByTask(int taskId, int engineID);

	/**
	 * Returns TaskResults for defined destinations (string representation).
	 *
	 * @param destinationsNames
	 * @return list of task results
	 * @throws InternalErrorException
	 */
	List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames) throws InternalErrorException;
}
