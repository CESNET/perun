package cz.metacentrum.perun.taskslib.service;

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
 *         JavaDoc coming soon...
 *
 */
public interface TaskManager {

	int scheduleNewTask(Task task, int engineID) throws InternalErrorException;

	void insertTask(Task task, int engineID) throws InternalErrorException;

	Task getTask(ExecService execService, Facility facility, int engineID);

	Task getTask(int execServiceId, int facilityId, int engineID);

	Task getTaskById(int id, int engineID);

	Task getTaskById(int id);

	List<Task> listAllTasks(int engineID);

	List<Pair<Task, Integer>> listAllTasksAndClients();
	    
	List<Task> listAllTasksForFacility(int facilityID);

	List<Task> listAllTasksInState(TaskStatus state, int engineID);

	List<Task> listTasksScheduledBetweenDates(Date olderThen, Date youngerThen, int engineID);

	List<Task> listTasksStartedBetweenDates(Date olderThen, Date youngerThen, int engineID);

	List<Task> listTasksEndedBetweenDates(Date olderThen, Date youngerThen, int engineID);

	void updateTask(Task task, int engineID);

	void updateTask(Task task);

	boolean isThereSuchTask(ExecService execService, Facility facility, int engineID);

	void removeTask(ExecService execService, Facility facility, int engineID);

	void removeTask(int id, int engineID);

	void removeTask(int id);

	int countTasks(int engineID);

	List<Task> listAllTasksNotInState(TaskStatus state, int engineID);

}
