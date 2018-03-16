package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.taskslib.model.Task;

/**
 * Worker used to execute Tasks GEN script.
 *
 * @author David Šarman
 */
public interface GenWorker extends EngineWorker<Task> {

	@Override
	Task call() throws Exception;

	/**
	 * Return ID of Task associated with this GenWorker
	 *
	 * @return ID of Task
	 */
	Integer getTaskId();

	/**
	 * Return Task associated with this GenWorker
	 *
	 * @return Task
	 */
	Task getTask();

}
