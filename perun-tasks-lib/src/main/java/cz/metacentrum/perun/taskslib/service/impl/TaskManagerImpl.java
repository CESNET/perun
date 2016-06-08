package cz.metacentrum.perun.taskslib.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
@Transactional
@Service(value = "taskManager")
public class TaskManagerImpl implements TaskManager {

	@Autowired
	private TaskDao taskDao;

	@Override
	public int scheduleNewTask(Task task, int engineID) throws InternalErrorException {
		return taskDao.scheduleNewTask(task, engineID);
	}

	@Override
	public void insertTask(Task task, int engineID) throws InternalErrorException {
		taskDao.insertTask(task, engineID);
	}

	@Override
	public Task getTask(ExecService execService, Facility facility, int engineID) {
		return taskDao.getTask(execService, facility);
	}

	@Override
	public Task getTask(int execServiceId, int facilityId, int engineID) {
		return taskDao.getTask(execServiceId, facilityId, engineID);
	}

	@Override
	public Task getTaskById(int id, int engineID) {
		return taskDao.getTaskById(id, engineID);
	}

	@Override
	public Task getTaskById(int id) {
		return taskDao.getTaskById(id);
	}

	@Override
	public List<Task> listAllTasks(int engineID) {
		return taskDao.listAllTasks(engineID);
	}

	@Override
	public List<Pair<Task, Integer>> listAllTasksAndClients() {
		return taskDao.listAllTasksAndClients();
	}

	@Override
	public List<Task> listAllTasksForFacility(int facilityID) {
		return taskDao.listAllTasksForFacility(facilityID);
	}

	@Override
	public List<Task> listAllTasksInState(TaskStatus state, int engineID) {
		return taskDao.listAllTasksInState(state, engineID);
	}

	@Override
	public List<Task> listAllTasksNotInState(TaskStatus state, int engineID) {
		return taskDao.listAllTasksNotInState(state, engineID);
	}

	@Override
	public List<Task> listTasksScheduledBetweenDates(Date olderThen, Date youngerThen, int engineID) {
		return taskDao.listTasksScheduledBetweenDates(olderThen, youngerThen, engineID);
	}

	@Override
	public List<Task> listTasksStartedBetweenDates(Date olderThen, Date youngerThen, int engineID) {
		return taskDao.listTasksStartedBetweenDates(olderThen, youngerThen, engineID);
	}

	@Override
	public List<Task> listTasksEndedBetweenDates(Date olderThen, Date youngerThen, int engineID) {
		return taskDao.listTasksEndedBetweenDates(olderThen, youngerThen, engineID);
	}

	@Override
	public void updateTask(Task task, int engineID) {
		taskDao.updateTask(task, engineID);
	}

	@Override
	public void updateTask(Task task) {
		taskDao.updateTask(task);
	}

	@Override
	public boolean isThereSuchTask(ExecService execService, Facility facility, int engineID) {
		return taskDao.isThereSuchTask(execService, facility, engineID);
	}

	@Override
	public void removeTask(ExecService execService, Facility facility, int engineID) {
		taskDao.removeTask(execService, facility, engineID);
	}

	@Override
	public void removeTask(int id, int engineID) {
		taskDao.removeTask(id, engineID);
	}

	@Override
	public void removeTask(int id) {
		taskDao.removeTask(id);
	}

	@Override
	public int countTasks(int engineID) {
		return taskDao.countTasks(engineID);
	}

	public void setTaskDao(TaskDao taskDao) {
		this.taskDao = taskDao;
	}

	public TaskDao getTaskDao() {
		return taskDao;
	}

}
