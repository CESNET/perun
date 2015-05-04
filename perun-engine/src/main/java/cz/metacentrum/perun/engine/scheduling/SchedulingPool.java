package cz.metacentrum.perun.engine.scheduling;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface SchedulingPool {

	/**
	 * Add to the pool
	 * 
	 * @param pair
	 * @return current pool size
	 */
	@Deprecated
	int addToPool(Pair<ExecService, Facility> pair);

	/**
	 * Get all pairs from the pool. NOTE: This action will empty the pool!
	 * 
	 * @return
	 */
	@Deprecated
	List<Pair<ExecService, Facility>> emptyPool();

	/**
	 * Size
	 * 
	 * @return current pool size
	 */
	int getSize();

	void close();

	/**
	 * Add Task to the waiting list.
	 * 
	 * @param task
	 * @return
	 */
	int addToPool(Task task);

	/**
	 * Get list of Tasks in Planned state
	 * 
	 * @return list of tasks
	 */
	List<Task> getPlannedTasks();

	/**
	 * Get list of Tasks to be scheduled
	 * 
	 * @return list of tasks
	 */
	List<Task> getNewTasks();

	List<Task> getProcessingTasks();

	List<Task> getErrorTasks();

	List<Task> getDoneTasks();

	/**
	 * Set status of given task
	 * 
	 * @param task
	 * @param status
	 */
	void setTaskStatus(Task task, TaskStatus status);

	Task getTaskById(int id);

	void removeTask(Task task);

	void reloadTasks(int engineID);

}
