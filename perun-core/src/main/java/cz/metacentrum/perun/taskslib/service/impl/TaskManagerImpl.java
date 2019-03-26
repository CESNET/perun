package cz.metacentrum.perun.taskslib.service.impl;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.service.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
@Transactional
@org.springframework.stereotype.Service(value = "taskManager")
public class TaskManagerImpl implements TaskManager {
	private static final Logger log = LoggerFactory.getLogger(TaskManagerImpl.class);

	@Autowired
	private TaskDao taskDao;

	@Override
	public int scheduleNewTask(Task task, int engineID) {
		return taskDao.scheduleNewTask(task, engineID);
	}

	@Override
	public void insertTask(Task task, int engineID) {
		taskDao.insertTask(task, engineID);
	}

	@Override
	public Task getTask(Service service, Facility facility, int engineID) {
		return taskDao.getTask(service, facility);
	}

	@Override
	public Task getTask(int serviceId, int facilityId, int engineID) {
		return taskDao.getTask(serviceId, facilityId, engineID);
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
	public void updateTask(Task task, int engineID) {
		taskDao.updateTask(task, engineID);
	}

	@Override
	public void updateTask(Task task) {
		taskDao.updateTask(task);
	}

	@Override
	public void updateTaskEngine(Task task, int engineID) throws InternalErrorException {
		taskDao.updateTaskEngine(task, engineID);
	}

	@Override
	public boolean isThereSuchTask(Service service, Facility facility, int engineID) {
		return taskDao.isThereSuchTask(service, facility, engineID);
	}

	@Override
	public void removeTask(Service service, Facility facility, int engineID) {
		taskDao.removeTask(service, facility, engineID);
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
