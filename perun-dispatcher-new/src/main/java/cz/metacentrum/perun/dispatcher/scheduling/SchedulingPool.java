package cz.metacentrum.perun.dispatcher.scheduling;

import java.util.Collection;
import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * 
 * @author Michal Voců
 * 
 * Contains:
 *   - database of Tasks and their states
 *   - mapping of Tasks to engines (dispatcherQueue)
 *   
 */
public interface SchedulingPool {

    /**
     * Size
     * 
     * @return current pool size
     */
    int getSize();

    /**
     * Add Task to the waiting list.
     * 
     * @param task
     * @param dispatcherQueue 
     * @return
     * @throws InternalErrorException 
     */
    int addToPool(Task task, DispatcherQueue dispatcherQueue) throws InternalErrorException;

	Task getTaskById(int id);

	void removeTask(Task task);

	List<Task> getWaitingTasks();

	Task getTask(ExecService execService, Facility facility);

	DispatcherQueue getQueueForTask(Task task) throws InternalErrorException;

	void setQueueForTask(Task task, DispatcherQueue queueForTask);

	void setTaskStatus(Task task, TaskStatus status);

	List<Task> getTasksForEngine(int clientID);

	List<Task> getDoneTasks();

	List<Task> getErrorTasks();

	List<Task> getProcessingTasks();

	List<Task> getPlannedTasks();

	void clear();

	void reloadTasks();

}
