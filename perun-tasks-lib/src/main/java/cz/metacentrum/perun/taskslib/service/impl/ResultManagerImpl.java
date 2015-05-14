package cz.metacentrum.perun.taskslib.service.impl;

import java.util.List;

import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.ResultManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
@Transactional
@Service(value = "resultManager")
public class ResultManagerImpl implements ResultManager {

	@Autowired
	private TaskResultDao taskResultDao;

	@Override
	public List<TaskResult> getTaskResults() {
		return taskResultDao.getTaskResults();

	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId) {
		return taskResultDao.getTaskResultsByTask(taskId);

	}

	@Override
	public TaskResult getTaskResultById(int taskResultId) {
		return taskResultDao.getTaskResultById(taskResultId);
	}

	@Override
	public int clearByTask(int taskId) {
		return taskResultDao.clearByTask(taskId);
	}

	@Override
	public int clearAll() {
		return taskResultDao.clearAll();
	}

	@Override
	public int insertNewTaskResult(TaskResult taskResult, int engineID) throws InternalErrorException {
		return taskResultDao.insertNewTaskResult(taskResult, engineID);
	}

	@Override
	public List<TaskResult> getTaskResults(int engineID) {
		return taskResultDao.getTaskResults(engineID);
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId, int engineID) {
		return taskResultDao.getTaskResultById(taskResultId, engineID);
	}

	@Override
	public int clearByTask(int taskId, int engineID) {
		return taskResultDao.clearByTask(taskId, engineID);
	}

	@Override
	public int clearAll(int engineID) {
		return taskResultDao.clearAll(engineID);
	}

	@Override
	public int clearOld(int engineID, int numDays) throws InternalErrorException {
		return taskResultDao.clearOld(engineID, numDays);
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId, int engineID) {
		return taskResultDao.getTaskResultsByTask(taskId, engineID);
	}

	public List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames) throws InternalErrorException {
		return taskResultDao.getTaskResultsForDestinations(destinationsNames);
	}

	public void setTaskResultDao(TaskResultDao taskResultDao) {
		this.taskResultDao = taskResultDao;
	}

	public TaskResultDao getTaskResultDao() {
		return taskResultDao;
	}

}
