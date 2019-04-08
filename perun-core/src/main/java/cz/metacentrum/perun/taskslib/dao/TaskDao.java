package cz.metacentrum.perun.taskslib.dao;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

import java.util.List;

/**
 *
 * @author Michal Karm Babacek
 * JavaDoc coming soon...
 *
 */
public interface TaskDao {

	Task getTask(Service service, Facility facility);

	int insertTask(Task task, int engineID);

	List<Task> listAllTasks();

	List<Pair<Task, Integer>> listAllTasksAndClients();

	/**
	 * Returns all tasks associated with selected facility
	 *
	 * @param facilityId
	 * @return tasks for facility
	 */
	List<Task> listAllTasksForFacility(int facilityId);

	List<Task> listAllTasksInState(Task.TaskStatus state);

	void updateTask(Task task);

	void removeTask(int id);

	int countTasks();

	Task getTask(int serviceId, int facilityId);

	int scheduleNewTask(Task task, int engineID);

	Task getTask(Service service, Facility facility, int engineID);

	Task getTask(int serviceId, int facilityId, int engineID);

	Task getTaskById(int id);

	Task getTaskById(int id, int engineID);

	List<Task> listAllTasks(int engineID);

	List<Task> listAllTasksInState(TaskStatus state, int engineID);

	void updateTask(Task task, int engineID);

	void updateTaskEngine(Task task, int engineID) throws InternalErrorException;

	boolean isThereSuchTask(Service service, Facility facility, int engineID);

	boolean isThereSuchTask(Service service, Facility facility);

	void removeTask(Service service, Facility facility, int engineID);

	void removeTask(Service service, Facility facility);

	void removeTask(int id, int engineID);

	int countTasks(int engineID);

	List<Task> listAllTasksNotInState(TaskStatus state, int engineID);

}
