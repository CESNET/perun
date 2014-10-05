package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

/**
 * 
 * @author michal
 *
 */
public interface TaskResultListener {

	/**
     * 
     */
    void onTaskDestinationDone(Task task, Destination destination, TaskResult result);
    
    /**
     * 
     */
    void onTaskDestinationError(Task task, Destination destination, TaskResult result);

}
