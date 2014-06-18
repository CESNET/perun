package cz.metacentrum.perun.taskslib.dao;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 *
 * @author Michal Karm Babacek
 * JavaDoc coming soon...
 *
 */
public interface TaskDao {

	Task getTask(ExecService execService, Facility facility);

	int insertTask(Task task, int engineID) throws InternalErrorException;

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

	List<Task> listTasksScheduledBetweenDates(Date olderThen, Date youngerThen);

	List<Task> listTasksStartedBetweenDates(Date olderThen, Date youngerThen);

	List<Task> listTasksEndedBetweenDates(Date olderThen, Date youngerThen);

	void updateTask(Task task);

	boolean isThereSuchTask(ExecService execService, Facility facility);

	void removeTask(ExecService execService, Facility facility);

	void removeTask(int id);

	int countTasks();

	Task getTask(int execServiceId, int facilityId);

	int scheduleNewTask(Task task, int engineID) throws InternalErrorException;

	Task getTask(ExecService execService, Facility facility, int engineID);

	Task getTask(int execServiceId, int facilityId, int engineID);

	Task getTaskById(int id);

	Task getTaskById(int id, int engineID);

	List<Task> listAllTasks(int engineID);

	List<Task> listAllTasksInState(TaskStatus state, int engineID);

	List<Task> listTasksScheduledBetweenDates(Date olderThen, Date youngerThen, int engineID);

	List<Task> listTasksStartedBetweenDates(Date olderThen, Date youngerThen, int engineID);

	List<Task> listTasksEndedBetweenDates(Date olderThen, Date youngerThen, int engineID);

	void updateTask(Task task, int engineID);

	boolean isThereSuchTask(ExecService execService, Facility facility, int engineID);

	void removeTask(ExecService execService, Facility facility, int engineID);

	void removeTask(int id, int engineID);

	int countTasks(int engineID);

	List<Task> listAllTasksNotInState(TaskStatus state, int engineID);

}
