package cz.metacentrum.perun.taskslib.service;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;

import java.util.Collection;
import java.util.List;

/**
 * This interface describes basic Task storing functionality, where every Task is uniquely represented by
 * both its ID, and the Facility and Service it contains.
 *
 * Storage is meant to be in-memory pool.
 */
public interface TaskStore {

	/**
	 * Get Task by its ID.
	 *
	 * @param id ID of Task to get
	 * @return Task by its ID
	 */
	Task getTask(int id);

	/**
	 * Get Task by its Facility and Service.
	 *
	 * @param facility Facility to get Task for
	 * @param service Service to get Task for
	 * @return Task by its Facility and Service
	 */
	Task getTask(Facility facility, Service service);

	/**
	 * Get current size of TaskStore (number of Tasks stored).
	 *
	 * @return Number of Tasks stored in a TaskStore
	 */
	int getSize();

	/**
	 * Add Task to TaskStore.
	 *
	 * @param task Task to be added
	 * @return Added Task
	 * @throws TaskStoreException When Task can't be added because of some kind of inconsistency
	 */
	Task addTask(Task task) throws TaskStoreException;

	/**
	 * Get all Tasks present in a TaskStore.
	 *
	 * @return All Tasks from TaskStore
	 */
	Collection<Task> getAllTasks();

	/**
	 * Get all Tasks which are in any of specified statuses.
	 *
	 * @see cz.metacentrum.perun.taskslib.model.Task.TaskStatus
	 *
	 * @param status Array of expected TaskStatuses
	 * @return All Tasks which are in any of expected statuses.
	 */
	List<Task> getTasksWithStatus(Task.TaskStatus... status);

	/**
	 * Remove Task from TaskStore
	 *
	 * @param task Task to be removed
	 * @return Removed Task
	 * @throws TaskStoreException When Task can't be removed because of some kind of inconsistency
	 */
	Task removeTask(Task task) throws TaskStoreException;

	/**
	 * Remove Task from TaskStore by its ID.
	 *
	 * @param id ID of Task to be removed
	 * @return Removed Task
	 * @throws TaskStoreException When Task can't be removed because of some kind of inconsistency
	 */
	Task removeTask(int id) throws TaskStoreException;

	/**
	 * Clear all Tasks from TaskStore.
	 */
	void clear();

}
